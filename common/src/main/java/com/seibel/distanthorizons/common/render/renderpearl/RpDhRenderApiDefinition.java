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

package com.seibel.distanthorizons.common.render.renderpearl;

#if MC_VER >= MC_26_3_0

import com.seibel.distanthorizons.api.enums.config.EDhApiRenderingApi;
import com.seibel.distanthorizons.api.enums.config.EDhApiRenderingEngine;
import com.seibel.distanthorizons.common.render.renderpearl.test.RpTestTriangleRenderer;
import com.seibel.distanthorizons.common.render.renderpearl.wrappers.buffer.RpVertexBufferWrapper;
import com.seibel.distanthorizons.common.render.renderpearl.wrappers.uniform.RpLodUniformBufferWrapper;
import com.seibel.distanthorizons.common.render.stub.StubDhRenderApiDefinition;
import com.seibel.distanthorizons.common.wrappers.minecraft.MinecraftRenderWrapper;
import com.seibel.distanthorizons.core.logging.DhLogger;
import com.seibel.distanthorizons.core.logging.DhLoggerBuilder;
import com.seibel.distanthorizons.core.render.EDhRenderDepth;
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

/**
 * MC 26.3 renderpearl engine definition (stage 1).
 *
 * <p>Only the test triangle renderer is real in this stage; every other
 * renderer and the generic object factories stay on the Stub implementations
 * (decision SA-3) to keep the main world behaviour identical to stage 0.
 */
public class RpDhRenderApiDefinition extends AbstractDhRenderApiDefinition
{
	private static final DhLogger LOGGER = new DhLoggerBuilder().name("RpEngine").build();
	
	private final StubDhRenderApiDefinition stub = new StubDhRenderApiDefinition();
	
	private EDhApiRenderingApi renderApi = null;
	
	
	
	//=========//
	// getters //
	//=========//
	//region
	
	@Override
	public String getEngineName() { return "RenderPearl: " + this.getRenderApi(); }
	
	@Override
	public EDhRenderDepth getRenderDepth() { return EDhRenderDepth.REVERSE_Z; }
	
	@Override
	public EDhApiRenderingApi getRenderApi()
	{
		// lazily resolved so construction works in JVM tests and on the server
		if (this.renderApi == null)
		{
			this.renderApi = MinecraftRenderWrapper.INSTANCE.getMcRenderingApi();
		}
		return this.renderApi;
	}
	
	@Override
	public EDhApiRenderingEngine getRenderingEngine() { return EDhApiRenderingEngine.RENDERPEARL; }
	
	@Override
	public boolean isNativeRenderer() { return false; }
	
	//endregion
	
	
	
	//=============//
	// constructor //
	//=============//
	//region
	
	public RpDhRenderApiDefinition()
	{
		LOGGER.info("RenderPearl engine selected; experimental triangle available, main world rendering still disabled.");
	}
	
	//endregion
	
	
	
	//============//
	// singletons //
	//============//
	//region
	
	@Override public IDhMetaRenderer getMetaRenderer() { return this.stub.getMetaRenderer(); }
	@Override public IDhTerrainRenderer getTerrainRenderer() { return this.stub.getTerrainRenderer(); }
	@Override public IDhSsaoRenderer getSsaoRenderer() { return this.stub.getSsaoRenderer(); }
	@Override public IDhFogRenderer getFogRenderer() { return this.stub.getFogRenderer(); }
	@Override public IDhFarFadeRenderer getFarFadeRenderer() { return this.stub.getFarFadeRenderer(); }
	@Override public com.seibel.distanthorizons.core.render.renderer.StubDebugWireframeRenderer getDebugWireframeRenderer() { return this.stub.getDebugWireframeRenderer(); }
	@Override public IDhVanillaFadeRenderer getVanillaFadeRenderer() { return this.stub.getVanillaFadeRenderer(); }
	@Override public IDhTestTriangleRenderer getTestTriangleRenderer() { return RpTestTriangleRenderer.INSTANCE; }
	
	//endregion
	
	
	
	//===========//
	// factories //
	//===========//
	//region
	
	@Override public IDhGenericRenderer createGenericRenderer() { return this.stub.createGenericRenderer(); }
	@Override public IDhGenericObjectVertexBufferContainer createGenericVboContainer() { return this.stub.createGenericVboContainer(); }
	
	@Override public IVertexBufferWrapper createVboWrapper(String name) { return new RpVertexBufferWrapper(name); }
	@Override public ILodContainerUniformBufferWrapper createLodContainerUniformWrapper() { return new RpLodUniformBufferWrapper(); }
	
	//endregion
	
	
	
}
#endif
