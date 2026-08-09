/*
 *    This file is part of the Distant Horizons mod
 *    licensed under the GNU LGPL v3 License.
 *
 *    Copyright (C) 2020 James Seibel
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, version 3.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.seibel.distanthorizons.common.render.renderpearl.terrain;

#if MC_VER >= MC_26_3_0

import com.mojang.blaze3d.pipeline.PipelineCache;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.seibel.distanthorizons.common.render.renderpearl.RpRenderPipelineBuilderWrapper;
import com.seibel.distanthorizons.common.render.renderpearl.wrappers.buffer.RpVertexBufferWrapper;
import com.seibel.distanthorizons.common.render.renderpearl.wrappers.uniform.RpLodUniformBufferWrapper;
import com.seibel.distanthorizons.common.render.renderpearl.wrappers.uniform.RpUniformBufferWrapper;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodBufferContainer;
import com.seibel.distanthorizons.core.logging.DhLogger;
import com.seibel.distanthorizons.core.logging.DhLoggerBuilder;
import com.seibel.distanthorizons.core.render.RenderParams;
import com.seibel.distanthorizons.core.util.objects.SortedArraySet;
import com.seibel.distanthorizons.core.wrapperInterfaces.minecraft.IProfilerWrapper;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.objects.ILodContainerUniformBufferWrapper;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.objects.IVertexBufferWrapper;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.renderPass.IDhTerrainRenderer;

/**
 * Port of {@code BlazeDhTerrainRenderer} (26.2) to renderpearl, D2 方案 A:
 * draws LOD terrain directly into Minecraft's main {@link RenderPass}
 * (provided through {@link RpMainRenderPassHolder} by the renderGroup mixin).
 *
 * <p>Uniform buffers use mapped CPU writes ({@link RpUniformBufferWrapper#uploadMapped()})
 * because renderpearl's frontend forbids command-encoder buffer writes while a
 * render pass is open.
 */
public class RpDhTerrainRenderer implements IDhTerrainRenderer
{
	public static final RpDhTerrainRenderer INSTANCE = new RpDhTerrainRenderer();
	
	private static final DhLogger LOGGER = new DhLoggerBuilder().name("RpTerrain").build();
	
	/** retry delay after a failed pipeline/device init (audit F4) */
	private static final long INIT_RETRY_DELAY_NANO = 1_000_000_000L;
	
	
	private boolean init = false;
	private long lastInitAttemptNano = 0;
	private RenderPipeline opaquePipeline;
	private CompiledRenderPipeline compiledOpaquePipeline;
	private PipelineCache pipelineCache;
	
	private RpUniformBufferWrapper vertSharedUniformBuffer = null;
	
	/** test seam (audit F7): injected fake device used instead of RenderSystem */
	private static GpuDevice testDevice = null;
	
	static void setTestDevice(GpuDevice device) { testDevice = device; }
	
	
	
	//=============//
	// constructor //
	//=============//
	//region
	
	private RpDhTerrainRenderer() { }
	
	//endregion
	
	
	
	//============//
	// init/prepare //
	//============//
	//region
	
	/** compiles the pipeline; must run outside any active render pass */
	public void prepareFrame()
	{
		this.tryInit();
		// explicit global IBO initialization (audit F3): queued here, flushed at
		// LevelRenderer.render HEAD before the main pass opens
		RpVertexBufferWrapper.ensureGlobalIndexBufferQueued();
	}
	
	private void tryInit()
	{
		if (this.init)
		{
			return;
		}
		
		// audit F4: failed inits are retried (rate-limited) instead of disabling
		// rendering forever after a device rebuild / shader reload
		long nowNano = System.nanoTime();
		if (nowNano - this.lastInitAttemptNano < INIT_RETRY_DELAY_NANO)
		{
			return;
		}
		this.lastInitAttemptNano = nowNano;
		
		try
		{
			GpuDevice device = testDevice != null ? testDevice : RenderSystem.getDevice();
			this.opaquePipeline = buildOpaquePipeline();
			
			this.pipelineCache = new PipelineCache(device, RpTerrainShaderSources.INSTANCE);
			this.compiledOpaquePipeline = this.pipelineCache.get(this.opaquePipeline);
			this.vertSharedUniformBuffer = new RpUniformBufferWrapper("vertSharedUniformBlock", device);
			if (this.compiledOpaquePipeline == null)
			{
				LOGGER.error("Failed to compile opaque terrain pipeline [" + this.opaquePipeline.getLocation() + "].");
				this.cleanupFailedInit();
				return;
			}
			this.init = true;
		}
		catch (Throwable e)
		{
			// never crash the frame because of LOD setup problems; rendering is skipped this frame
			LOGGER.error("Failed to initialize opaque terrain renderer.", e);
			this.compiledOpaquePipeline = null;
			this.cleanupFailedInit();
		}
	}
	
	/** releases partial resources after a failed init so the retry starts clean (audit F4) */
	private void cleanupFailedInit()
	{
		if (this.vertSharedUniformBuffer != null)
		{
			this.vertSharedUniformBuffer.close();
			this.vertSharedUniformBuffer = null;
		}
		if (this.pipelineCache != null)
		{
			this.pipelineCache.close();
			this.pipelineCache = null;
		}
		this.compiledOpaquePipeline = null;
	}
	
	/** pipeline configuration is isolated so UT-2-3/2-7 can assert it without a GPU device */
	static RenderPipeline buildOpaquePipeline()
	{
		RpRenderPipelineBuilderWrapper pipelineBuilder = new RpRenderPipelineBuilderWrapper();
		pipelineBuilder.withFaceCulling(true);
		pipelineBuilder.withDepthWrite(true);
		pipelineBuilder.withDepthTest(RpRenderPipelineBuilderWrapper.EDhDepthTest.GREATER);
		pipelineBuilder.withColorWrite(true);
		pipelineBuilder.withPolygonMode(RpRenderPipelineBuilderWrapper.EDhPolygonMode.FILL);
		pipelineBuilder.withoutBlend();
		pipelineBuilder.withName("opaque_terrain");
		
		pipelineBuilder.withVertexShader("terrain/renderpearl/vert");
		pipelineBuilder.withFragmentShader("terrain/renderpearl/frag");
		pipelineBuilder.withVertexFormat(RpLodVertexLayout.getFormat());
		pipelineBuilder.withVertexMode(RpRenderPipelineBuilderWrapper.EDhVertexMode.TRIANGLES);
		pipelineBuilder.withUniformBuffer("vertUniqueUniformBlock");
		pipelineBuilder.withUniformBuffer("vertSharedUniformBlock");
		
		return pipelineBuilder.build();
	}
	
	//endregion
	
	
	
	//========//
	// render //
	//========//
	//region
	
	@Override
	public void render(
		RenderParams renderEventParam,
		boolean opaquePass,
		SortedArraySet<LodBufferContainer> bufferContainers,
		IProfilerWrapper profiler)
	{
		if (!opaquePass)
		{
			// transparent LOD rendering lands in stage 5
			return;
		}
		
		RenderPass renderPass = RpMainRenderPassHolder.getCurrent();
		if (renderPass == null)
		{
			LOGGER.debug("No main render pass available; skipping LOD terrain.");
			return;
		}
		
		this.tryInit();
		if (this.compiledOpaquePipeline == null || this.vertSharedUniformBuffer == null)
		{
			return;
		}
		
		// shared uniforms (mapped write: safe inside the pass)
		this.vertSharedUniformBuffer
			.putInt(0) // uIsWhiteWorld
			.putFloat((float) renderEventParam.worldYOffset) // uWorldYOffset
			.putFloat(0.01f) // uMicroOffset
			.putFloat(0.0f) // uEarthRadius (curvature disabled)
			.putVec3f(
				(float) renderEventParam.exactCameraPosition.x,
				(float) renderEventParam.exactCameraPosition.y,
				(float) renderEventParam.exactCameraPosition.z) // uCameraPos
			.putMat4f(renderEventParam.dhMvmProjMatrix) // uCombinedMatrix
			.uploadMapped();
		
		renderPass.setPipeline(this.compiledOpaquePipeline);
		renderPass.setUniform("vertSharedUniformBlock", this.vertSharedUniformBuffer.getBufferSlice());
		// defensive: vanilla may have left a scissor rect active
		renderPass.disableScissor();
		
		for (int lodIndex = 0; lodIndex < bufferContainers.size(); lodIndex++)
		{
			LodBufferContainer bufferContainer = bufferContainers.get(lodIndex);
			
			// SA-3: never draw with a mixed/stub uniform container (audit F6)
			if (!isCompatibleUniformContainer(bufferContainer.uniformContainer))
			{
				continue;
			}
			
			// per-container model offset UBO (mapped write)
			bufferContainer.uniformContainer.tryUpload(bufferContainer);
			RpLodUniformBufferWrapper lodUniformBuffer = (RpLodUniformBufferWrapper) bufferContainer.uniformContainer;
			renderPass.setUniform("vertUniqueUniformBlock", lodUniformBuffer.getBufferSlice());
			
			this.drawBufferWrappers(renderPass, bufferContainer.vboOpaqueWrappers);
		}
	}
	
	/** audit F6: only RENDERPEARL uniform containers may be drawn */
	static boolean isCompatibleUniformContainer(ILodContainerUniformBufferWrapper uniformContainer)
	{
		return uniformContainer instanceof RpLodUniformBufferWrapper;
	}
	
	/**
	 * Draws all uploaded opaque vertex buffers of one LOD container. Extracted
	 * from {@link #render} so unit tests can assert real draw counts with a
	 * fake render pass (audit F7). If the (global or per-wrapper) index buffer
	 * is not ready yet, the wrapper is skipped instead of issuing an indexed
	 * draw without an index buffer (audit F3).
	 */
	void drawBufferWrappers(RenderPass renderPass, IVertexBufferWrapper[] bufferWrapperList)
	{
		for (int i = 0; i < bufferWrapperList.length; i++)
		{
			IVertexBufferWrapper bufferWrapper = bufferWrapperList[i];
			if (!(bufferWrapper instanceof RpVertexBufferWrapper rpBufferWrapper)
				|| !rpBufferWrapper.isUploaded()
				|| rpBufferWrapper.getVertexCount() == 0)
			{
				continue;
			}
			
			if (rpBufferWrapper.getVertexGpuBuffer() == null
				|| rpBufferWrapper.getVertexGpuBuffer().isClosed())
			{
				continue;
			}
			
			GpuBuffer indexBuffer = rpBufferWrapper.getIndexGpuBuffer();
			if (indexBuffer == null || indexBuffer.isClosed())
			{
				// audit F3: never issue an indexed draw without a usable index buffer
				continue;
			}
			renderPass.setIndexBuffer(indexBuffer, IndexType.INT);
			renderPass.setVertexBuffer(0, rpBufferWrapper.getVertexGpuBuffer().slice());
			renderPass.drawIndexed(rpBufferWrapper.getIndexCount(), 1, 0, 0, 0);
		}
	}
	
	//endregion
	
	
	//========//
	// close  //
	//========//
	//region
	
	/**
	 * Releases all renderer-owned GPU resources. Idempotent; the renderer may be
	 * re-initialized by the next frame (audit F5). Wired to client shutdown in
	 * FabricClientProxy; device-recreation/shader-reload wiring lands with the
	 * resource system stage.
	 */
	public synchronized void close()
	{
		if (this.compiledOpaquePipeline != null)
		{
			this.compiledOpaquePipeline.close();
			this.compiledOpaquePipeline = null;
		}
		if (this.pipelineCache != null)
		{
			this.pipelineCache.close();
			this.pipelineCache = null;
		}
		if (this.vertSharedUniformBuffer != null)
		{
			this.vertSharedUniformBuffer.close();
			this.vertSharedUniformBuffer = null;
		}
		this.opaquePipeline = null;
		this.init = false;
		this.lastInitAttemptNano = 0;
	}
	
	//endregion
	
	
}
#endif
