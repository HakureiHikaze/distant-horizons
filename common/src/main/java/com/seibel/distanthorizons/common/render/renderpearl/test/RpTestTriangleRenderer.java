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

package com.seibel.distanthorizons.common.render.renderpearl.test;

#if MC_VER >= MC_26_3_0

import com.mojang.blaze3d.pipeline.PipelineCache;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.GpuFence;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.seibel.distanthorizons.common.render.renderpearl.RpRenderPipelineBuilderWrapper;
import com.seibel.distanthorizons.common.render.renderpearl.RpShaderSource;
import com.seibel.distanthorizons.common.render.renderpearl.RpVertexFormatUtil;
import com.seibel.distanthorizons.common.render.renderpearl.wrappers.RpRenderPassWrapper;
import com.seibel.distanthorizons.common.render.renderpearl.wrappers.texture.RpTextureViewWrapper;
import com.seibel.distanthorizons.common.wrappers.minecraft.MinecraftRenderWrapper;
import com.seibel.distanthorizons.core.logging.DhLogger;
import com.seibel.distanthorizons.core.logging.DhLoggerBuilder;
import com.seibel.distanthorizons.core.render.RenderParams;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.renderPass.IDhTestTriangleRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.joml.Vector4f;

/**
 * Port of {@code BlazeDhTestTriangleRenderer} (26.2): renders a colored
 * triangle into Minecraft's main render target to verify the wrapper layer.
 * Disabled by default (stage 1 exit condition: no impact on the main world);
 * enabled through {@link #setEnabled(boolean)}.
 */
public class RpTestTriangleRenderer implements IDhTestTriangleRenderer
{
	public static final RpTestTriangleRenderer INSTANCE = new RpTestTriangleRenderer();
	
	private static final DhLogger LOGGER = new DhLoggerBuilder().name("RpTestTriangle").build();
	
	
	private final GpuDevice device;
	private boolean enabled = false;
	
	private boolean init = false;
	private RenderPipeline pipeline;
	private PipelineCache pipelineCache;
	private CompiledRenderPipeline compiledPipeline;
	private GpuBuffer vboGpuBuffer;
	private RpTextureViewWrapper mcColorView;
	private RpTextureViewWrapper mcDepthView;
	
	
	
	//=============//
	// constructors //
	//=============//
	//region
	
	private RpTestTriangleRenderer()
	{
		this(null, false);
	}
	
	/** test seam: injects a fake device and controls the enabled flag */
	RpTestTriangleRenderer(GpuDevice device, boolean enabled)
	{
		this.device = device;
		this.enabled = enabled;
	}
	
	//endregion
	
	
	
	//===========//
	// enablement //
	//===========//
	//region
	
	public boolean isEnabled() { return this.enabled; }
	public void setEnabled(boolean enabled) { this.enabled = enabled; }
	
	//endregion
	
	
	
	//=============//
	// initialization //
	//=============//
	//region
	
	private void tryInit()
	{
		if (this.init)
		{
			return;
		}
		this.init = true;
		
		GpuDevice gpuDevice = this.device != null ? this.device : RenderSystem.getDevice();
		
		RpRenderPipelineBuilderWrapper pipelineBuilder = new RpRenderPipelineBuilderWrapper();
		{
			pipelineBuilder.withFaceCulling(false);
			pipelineBuilder.withDepthWrite(false);
			pipelineBuilder.withDepthTest(RpRenderPipelineBuilderWrapper.EDhDepthTest.NONE);
			pipelineBuilder.withColorWrite(true);
			pipelineBuilder.withoutBlend();
			pipelineBuilder.withName("test_renderpearl_triangle");
			
			pipelineBuilder.withVertexShader("test/renderpearl/triangle_vert");
			pipelineBuilder.withFragmentShader("test/renderpearl/triangle_frag");
			
			VertexFormat vertexFormat = RpVertexFormatUtil.createScreenPosColorFormat();
			pipelineBuilder.withVertexFormat(vertexFormat);
			
			pipelineBuilder.withVertexMode(RpRenderPipelineBuilderWrapper.EDhVertexMode.TRIANGLES);
		}
		this.pipeline = pipelineBuilder.build();
		
		// SA-7 / 7B: DH-owned pipeline cache with embedded shader sources
		this.pipelineCache = new PipelineCache(gpuDevice, RpShaderSource.INSTANCE);
		this.compiledPipeline = this.pipelineCache.get(this.pipeline);
		if (this.compiledPipeline == null)
		{
			throw new IllegalStateException("Failed to compile test triangle pipeline [" + this.pipeline.getLocation() + "].");
		}
		
		this.uploadVertexData(gpuDevice);
		
		// cached view wrappers are reused across frames (audit F8)
		this.mcColorView = new RpTextureViewWrapper(gpuDevice);
		this.mcDepthView = new RpTextureViewWrapper(gpuDevice);
	}
	
	private void uploadVertexData(GpuDevice gpuDevice)
	{
		// vertices for the triangle
		float[] vertices = new float[]
			{
				// PosX,Y,    ColorR,G,B,A
				-0.5f, -0.5f,   1.0f, 0.0f, 0.0f, 1.0f,
				0.5f, -0.5f,   0.0f, 1.0f, 0.0f, 1.0f,
				0.0f,  0.5f,   0.0f, 0.0f, 1.0f, 1.0f,
			};
		
		int usage = GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_VERTEX;
		int size = vertices.length * Float.BYTES;
		this.vboGpuBuffer = gpuDevice.createBuffer(this::getRenderPassName, usage, size);
		
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
		byteBuffer.asFloatBuffer().put(vertices);
		byteBuffer.rewind();
		
		CommandEncoder encoder = gpuDevice.createCommandEncoder();
		encoder.writeToBuffer(new GpuBufferSlice(this.vboGpuBuffer, 0, size), byteBuffer);
		encoder.submit();
	}
	
	//endregion
	
	
	
	//========//
	// render //
	//========//
	//region
	
	@Override
	public void render(RenderParams renderParams)
	{
		if (!this.enabled)
		{
			// default off: no GPU resources are created (UT-6)
			return;
		}
		
		this.tryInit();
		
		GpuDevice gpuDevice = this.device != null ? this.device : RenderSystem.getDevice();
		
		this.mcColorView.tryWrap(MinecraftRenderWrapper.INSTANCE.getRenderTarget().getColorTexture());
		this.mcDepthView.tryWrap(MinecraftRenderWrapper.INSTANCE.getRenderTarget().getDepthTexture());
		
		this.renderToTargets(this.mcColorView.getTextureView(), this.mcDepthView.getTextureView());
	}
	
	/** test seam: draws into arbitrary target views without touching Minecraft */
	void renderToTargets(GpuTextureView colorTexture, GpuTextureView depthTexture)
	{
		if (!this.enabled)
		{
			// default off: no GPU resources are created (UT-6)
			return;
		}
		
		this.tryInit();
		
		GpuDevice gpuDevice = this.device != null ? this.device : RenderSystem.getDevice();
		
		try (RpRenderPassWrapper renderPassWrapper = new RpRenderPassWrapper(
			this::getRenderPassName,
			colorTexture,
			depthTexture,
			gpuDevice))
		{
			renderPassWrapper.setVertexBuffer(this.vboGpuBuffer.slice());
			renderPassWrapper.setPipeline(this.compiledPipeline);
			renderPassWrapper.draw(3);
		}
	}
	
	private String getRenderPassName() { return "distantHorizons:RpTestRenderer"; }
	
	//endregion
	
	
	
	//=============//
	// offscreen verification //
	//=============//
	//region
	
	/**
	 * Renders the triangle into an offscreen RGBA texture and reads the pixels
	 * back for verification (FT-3). Returns a width*height*4 RGBA buffer.
	 */
	public ByteBuffer renderOffscreenAndReadback(int width, int height) throws InterruptedException
	{
		this.tryInit();
		
		GpuDevice gpuDevice = this.device != null ? this.device : RenderSystem.getDevice();
		int pixelBytes = width * height * 4;
		
		GpuTexture colorTexture = gpuDevice.createTexture(
			() -> "distantHorizons:testTriangleColor", 
			GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_COPY_SRC,
			GpuFormat.RGBA8_UNORM, width, height, 1, 1);
		GpuTexture depthTexture = gpuDevice.createTexture(
			() -> "distantHorizons:testTriangleDepth",
			GpuTexture.USAGE_RENDER_ATTACHMENT,
			GpuFormat.D32_FLOAT, width, height, 1, 1);
		
		GpuTextureView colorView = gpuDevice.createTextureView(colorTexture);
		GpuTextureView depthView = gpuDevice.createTextureView(depthTexture);
		
		try
		{
			// deterministic background so "outside" pixels are verifiable
			CommandEncoder clearEncoder = gpuDevice.createCommandEncoder();
			clearEncoder.clearColorTexture(colorTexture, new Vector4f(0.0f, 0.0f, 0.0f, 1.0f));
			clearEncoder.submit();
			
			this.renderToTargets(colorView, depthView);
			
			GpuBuffer readbackBuffer = gpuDevice.createBuffer(
				() -> "distantHorizons:testTriangleReadback",
				GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_MAP_READ,
				pixelBytes);
			
			// audit F3: retry the fence wait; a single transient timeout must not fail the verification
			boolean copied = false;
			for (int attempt = 0; attempt < 3 && !copied; attempt++)
			{
				CommandEncoder encoder = gpuDevice.createCommandEncoder();
				GpuFence fence = encoder.createFence();
				encoder.copyTextureToBuffer(colorTexture, readbackBuffer, 0, () -> { }, 0);
				encoder.submit();
				copied = fence.awaitCompletion(5_000_000_000L);
				fence.close();
			}
			
			if (!copied)
			{
				throw new IllegalStateException("Timed out waiting for test triangle texture readback after 3 attempts.");
			}
			
			try (GpuBufferSlice.MappedView mapped = readbackBuffer.map(0, pixelBytes, true, false))
			{
				ByteBuffer result = ByteBuffer.allocate(pixelBytes);
				ByteBuffer data = mapped.data().duplicate();
				data.position(0);
				data.limit(pixelBytes);
				result.put(data);
				result.rewind();
				return result;
			}
			finally
			{
				readbackBuffer.close();
			}
		}
		finally
		{
			colorView.close();
			depthView.close();
			colorTexture.close();
			depthTexture.close();
		}
	}
	
	//endregion
	
	
	
	//========//
	// close  //
	//========//
	//region
	
	/** lifecycle cleanup; stage 2 will wire this to device close / shader reload (audit F5) */
	public void close()
	{
		if (this.mcColorView != null) { this.mcColorView.close(); this.mcColorView = null; }
		if (this.mcDepthView != null) { this.mcDepthView.close(); this.mcDepthView = null; }
		if (this.compiledPipeline != null) { this.compiledPipeline.close(); this.compiledPipeline = null; }
		if (this.pipelineCache != null) { this.pipelineCache.close(); this.pipelineCache = null; }
		if (this.vboGpuBuffer != null) { this.vboGpuBuffer.close(); this.vboGpuBuffer = null; }
		this.init = false;
	}
	
	//endregion
	
	
}
#endif
