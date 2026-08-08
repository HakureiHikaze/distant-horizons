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

package com.seibel.distanthorizons.common.render.stub;

import com.seibel.distanthorizons.api.enums.config.EDhApiRenderingApi;
import com.seibel.distanthorizons.api.enums.config.EDhApiRenderingEngine;
import com.seibel.distanthorizons.api.interfaces.render.IDhApiCustomRenderRegister;
import com.seibel.distanthorizons.api.interfaces.render.IDhApiRenderableBoxGroup;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiFogRenderParam;
import com.seibel.distanthorizons.api.objects.render.DhApiRenderableBox;
import com.seibel.distanthorizons.common.wrappers.minecraft.MinecraftRenderWrapper;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodBufferContainer;
import com.seibel.distanthorizons.core.logging.DhLogger;
import com.seibel.distanthorizons.core.logging.DhLoggerBuilder;
import com.seibel.distanthorizons.core.render.EDhRenderDepth;
import com.seibel.distanthorizons.core.render.RenderParams;
import com.seibel.distanthorizons.core.render.renderer.StubDebugWireframeRenderer;
import com.seibel.distanthorizons.core.util.objects.SortedArraySet;
import com.seibel.distanthorizons.core.wrapperInterfaces.minecraft.IProfilerWrapper;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.AbstractDhRenderApiDefinition;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.objects.IDhGenericObjectVertexBufferContainer;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.objects.ILodContainerUniformBufferWrapper;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.objects.IVertexBufferWrapper;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.renderPass.IDhFarFadeRenderer;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.renderPass.IDhFogRenderer;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.renderPass.IDhGenericRenderer;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.renderPass.IDhMetaRenderer;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.renderPass.IDhSsaoRenderer;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.renderPass.IDhTerrainRenderer;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.renderPass.IDhTestTriangleRenderer;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.renderPass.IDhVanillaFadeRenderer;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Temporary no-op rendering engine used while the renderpearl port is in progress.
 *
 * <p>
 * Every render method is intentionally empty: the engine creates no GPU resources,
 * uploads no buffers and draws nothing. It exists so the mod can start on MC 26.3
 * without any LOD rendering until stage 1+ of the renderpearl port lands.
 */
public class StubDhRenderApiDefinition extends AbstractDhRenderApiDefinition
{
	private static final StubDebugWireframeRenderer DEBUG_WIREFRAME_RENDERER = new StubDebugWireframeRenderer();
	
	private EDhApiRenderingApi renderApi = null;
	
	
	
	//=========//
	// getters //
	//=========//
	//region
	
	@Override
	public String getEngineName() { return "Stub (renderpearl port pending)"; }
	
	@Override
	public EDhRenderDepth getRenderDepth() { return EDhRenderDepth.REVERSE_Z; }
	
	@Override
	public EDhApiRenderingApi getRenderApi()
	{
		// lazily resolved so constructing the stub doesn't require Minecraft's renderer to be up
		if (this.renderApi == null)
		{
			this.renderApi = MinecraftRenderWrapper.INSTANCE.getMcRenderingApi();
		}
		return this.renderApi;
	}
	
	@Override
	public EDhApiRenderingEngine getRenderingEngine() { return EDhApiRenderingEngine.STUB; }
	
	@Override
	public boolean isNativeRenderer() { return false; }
	
	//endregion
	
	
	
	//=============//
	// constructor //
	//=============//
	//region
	
	public StubDhRenderApiDefinition()
	{
		// no-op; the "rendering engine not enabled" status is logged by DependencySetup when STUB is selected
	}
	
	//endregion
	
	
	
	//============//
	// singletons //
	//============//
	//region
	
	@Override public IDhMetaRenderer getMetaRenderer() { return StubMetaRenderer.INSTANCE; }
	@Override public IDhTerrainRenderer getTerrainRenderer() { return StubTerrainRenderer.INSTANCE; }
	@Override public IDhSsaoRenderer getSsaoRenderer() { return StubSsaoRenderer.INSTANCE; }
	@Override public IDhFogRenderer getFogRenderer() { return StubFogRenderer.INSTANCE; }
	@Override public IDhFarFadeRenderer getFarFadeRenderer() { return StubFarFadeRenderer.INSTANCE; }
	@Override public StubDebugWireframeRenderer getDebugWireframeRenderer() { return DEBUG_WIREFRAME_RENDERER; }
	@Override public IDhVanillaFadeRenderer getVanillaFadeRenderer() { return StubVanillaFadeRenderer.INSTANCE; }
	@Override public IDhTestTriangleRenderer getTestTriangleRenderer() { return StubTestTriangleRenderer.INSTANCE; }
	
	//endregion
	
	
	
	//===========//
	// factories //
	//===========//
	//region
	
	@Override public IDhGenericRenderer createGenericRenderer() { return new StubGenericRenderer(); }
	
	@Override public IVertexBufferWrapper createVboWrapper(String name) { return new StubVertexBufferWrapper(); }
	@Override public ILodContainerUniformBufferWrapper createLodContainerUniformWrapper() { return new StubLodContainerUniformBufferWrapper(); }
	@Override public IDhGenericObjectVertexBufferContainer createGenericVboContainer() { return new StubGenericObjectVertexContainer(); }
	
	//endregion
	
	
	
	//================//
	// no-op singletons //
	//================//
	//region
	
	private static class StubMetaRenderer implements IDhMetaRenderer
	{
		private static final StubMetaRenderer INSTANCE = new StubMetaRenderer();
		@Override public void runRenderPassSetup(RenderParams renderParams) { }
		@Override public void runRenderPassCleanup(RenderParams renderParams) { }
		@Override public void applyToMcTexture(RenderParams renderParams) { }
		@Override public void clearDhDepthAndColorTextures(RenderParams renderParams) { }
	}
	
	private static class StubTerrainRenderer implements IDhTerrainRenderer
	{
		private static final StubTerrainRenderer INSTANCE = new StubTerrainRenderer();
		@Override
		public void render(
			RenderParams renderEventParam, boolean opaquePass,
			SortedArraySet<LodBufferContainer> bufferContainers,
			IProfilerWrapper profiler) { }
	}
	
	private static class StubSsaoRenderer implements IDhSsaoRenderer
	{
		private static final StubSsaoRenderer INSTANCE = new StubSsaoRenderer();
		@Override public void render(RenderParams renderParams) { }
	}
	
	private static class StubFogRenderer implements IDhFogRenderer
	{
		private static final StubFogRenderer INSTANCE = new StubFogRenderer();
		@Override public void render(RenderParams renderParams, DhApiFogRenderParam fogRenderParams) { }
	}
	
	private static class StubFarFadeRenderer implements IDhFarFadeRenderer
	{
		private static final StubFarFadeRenderer INSTANCE = new StubFarFadeRenderer();
		@Override public void render(RenderParams renderParams) { }
	}
	
	private static class StubVanillaFadeRenderer implements IDhVanillaFadeRenderer
	{
		private static final StubVanillaFadeRenderer INSTANCE = new StubVanillaFadeRenderer();
		@Override public void render(RenderParams renderParams) { }
	}
	
	private static class StubTestTriangleRenderer implements IDhTestTriangleRenderer
	{
		private static final StubTestTriangleRenderer INSTANCE = new StubTestTriangleRenderer();
		@Override public void render(RenderParams renderParams) { }
	}
	
	private static class StubGenericRenderer implements IDhGenericRenderer, IDhApiCustomRenderRegister
	{
		@Override public void render(RenderParams renderEventParam, IProfilerWrapper profiler, boolean renderingWithSsao) { }
		@Override public String getVboRenderDebugMenuString() { return "Stub"; }
		@Override public void add(IDhApiRenderableBoxGroup cubeGroup) throws IllegalArgumentException { }
		@Override public IDhApiRenderableBoxGroup remove(long id) { return null; }
		@Override public void close() { }
	}
	
	private static class StubVertexBufferWrapper implements IVertexBufferWrapper
	{
		@Override public void uploadVertexBuffer(ByteBuffer buffer, int vertexCount) { }
		@Override public void uploadIndexBuffer(ByteBuffer buffer, int vertexCount) { }
		@Override public void close() { }
	}
	
	private static class StubLodContainerUniformBufferWrapper implements ILodContainerUniformBufferWrapper
	{
		@Override public void tryUpload(LodBufferContainer bufferContainer) { }
		@Override public void close() { }
	}
	
	private static class StubGenericObjectVertexContainer implements IDhGenericObjectVertexBufferContainer
	{
		@Override public void uploadDataToGpu() { }
		@Override public void updateVertexData(List<DhApiRenderableBox> uploadBoxList) { }
		@Override public EState getState() { return EState.RENDER; }
		@Override public void setState(EState state) { }
		@Override public void close() { }
	}
	
	//endregion
	
	
	
}
