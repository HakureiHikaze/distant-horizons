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

import java.util.OptionalDouble;

/** Thin wrapper owning a {@link GpuSampler} created through a {@link GpuDevice}. */
public class RpSamplerWrapper implements AutoCloseable
{
	private final String name;
	private final GpuDevice device;
	
	private GpuSampler sampler = null;
	
	
	
	//=============//
	// constructors //
	//=============//
	//region
	
	public RpSamplerWrapper(String name)
	{
		this(name, RenderSystem.getDevice());
	}
	
	public RpSamplerWrapper(String name, GpuDevice device)
	{
		this.name = name;
		this.device = device;
	}
	
	//endregion
	
	
	
	//=========//
	// sampler //
	//=========//
	//region
	
	public GpuSampler getOrCreateSampler(
		AddressMode addressModeU, AddressMode addressModeV,
		FilterMode minFilter, FilterMode magFilter,
		int maxAnisotropy, OptionalDouble maxLod)
	{
		if (this.sampler == null)
		{
			this.sampler = this.device.createSampler(
				addressModeU, addressModeV,
				minFilter, magFilter,
				maxAnisotropy, maxLod);
		}
		
		return this.sampler;
	}
	
	public GpuSampler getSampler() { return this.sampler; }
	public String getName() { return this.name; }
	
	//endregion
	
	
	
	//========//
	// close //
	//========//
	//region
	
	@Override
	public void close()
	{
		if (this.sampler != null)
		{
			this.sampler.close();
			this.sampler = null;
		}
	}
	
	//endregion
	
	
	
}
#endif
