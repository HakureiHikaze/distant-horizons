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

package com.seibel.distanthorizons.common.render.renderpearl.wrappers.texture;

#if MC_VER >= MC_26_3_0

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;

import java.util.OptionalDouble;

/**
 * Port of {@code BlazeTextureViewWrapper} (26.2): wraps a texture created by
 * Minecraft (e.g. the main render target) and lazily creates its view + sampler.
 */
public class RpTextureViewWrapper implements AutoCloseable
{
	private final GpuDevice device;
	
	private GpuTextureView textureView = null;
	private GpuSampler textureSampler = null;
	
	
	
	//=============//
	// constructors //
	//=============//
	//region
	
	public RpTextureViewWrapper()
	{
		this(RenderSystem.getDevice());
	}
	
	public RpTextureViewWrapper(GpuDevice device)
	{
		this.device = device;
	}
	
	//endregion
	
	
	
	//=======//
	// setup //
	//=======//
	//region
	
	/** does nothing if the texture is already wrapped */
	public void tryWrap(GpuTexture texture)
	{
		if (this.textureView == null
			|| this.textureView.texture() != texture)
		{
			if (this.textureView != null)
			{
				this.textureView.close();
			}
			
			this.textureView = this.device.createTextureView(texture);
		}
		
		if (this.textureSampler == null)
		{
			this.textureSampler = this.device.createSampler(
				AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE,
				FilterMode.LINEAR, FilterMode.LINEAR,
				1,
				OptionalDouble.empty());
		}
	}
	
	//endregion
	
	
	
	//=========//
	// getters //
	//=========//
	//region
	
	public GpuTextureView getTextureView() { return this.textureView; }
	public GpuSampler getTextureSampler() { return this.textureSampler; }
	
	//endregion
	
	
	
	//========//
	// close //
	//========//
	//region
	
	@Override
	public void close()
	{
		if (this.textureView != null)
		{
			this.textureView.close();
			this.textureView = null;
		}
		
		if (this.textureSampler != null)
		{
			this.textureSampler.close();
			this.textureSampler = null;
		}
	}
	
	//endregion
	
	
	
}
#endif
