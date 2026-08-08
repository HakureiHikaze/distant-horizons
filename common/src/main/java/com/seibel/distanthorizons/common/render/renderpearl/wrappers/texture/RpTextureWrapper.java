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
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import org.joml.Vector4fc;

import java.nio.ByteBuffer;
import java.util.OptionalDouble;

/**
 * Port of {@code BlazeTextureWrapper} (26.2) to renderpearl. Owns a
 * {@link GpuTexture} (plus view/sampler) and handles create/resize/write/clear.
 */
public class RpTextureWrapper implements AutoCloseable
{
	private final String name;
	private final GpuDevice device;
	
	private final GpuFormat format;
	private final int usage;
	private final FilterMode samplerFilterMode;
	private final int maxAnisotropy;
	private final int mipLevelCount;
	
	private GpuTexture texture = null;
	private GpuTextureView textureView = null;
	private GpuSampler textureSampler = null;
	
	private int width = -1;
	private int height = -1;
	
	
	
	//=============//
	// constructors //
	//=============//
	//region
	
	public static RpTextureWrapper createDepth(String name)
	{
		return new RpTextureWrapper(name, GpuFormat.D32_FLOAT, GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING, FilterMode.NEAREST, 1, 1);
	}
	
	public static RpTextureWrapper createColor(String name)
	{
		return new RpTextureWrapper(name, GpuFormat.RGBA8_UNORM, GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING, FilterMode.LINEAR, 1, 1);
	}
	
	public RpTextureWrapper(
		String name, GpuFormat format, int usage,
		FilterMode samplerFilterMode, int maxAnisotropy, int mipLevelCount)
	{
		this(name, format, usage, samplerFilterMode, maxAnisotropy, mipLevelCount, RenderSystem.getDevice());
	}
	
	public RpTextureWrapper(
		String name, GpuFormat format, int usage,
		FilterMode samplerFilterMode, int maxAnisotropy, int mipLevelCount,
		GpuDevice device)
	{
		this.name = name;
		this.format = format;
		this.usage = usage;
		this.samplerFilterMode = samplerFilterMode;
		this.maxAnisotropy = maxAnisotropy;
		this.mipLevelCount = mipLevelCount;
		this.device = device;
	}
	
	//endregion
	
	
	
	//============//
	// create/resize //
	//============//
	//region
	
	public boolean isEmpty() { return this.texture == null; }
	
	public boolean tryCreateOrResize(int width, int height)
	{
		if (this.texture != null
			&& this.width == width
			&& this.height == height)
		{
			return false;
		}
		
		if (this.texture != null)
		{
			this.close();
		}
		
		this.texture = this.device.createTexture(this::getName, this.usage, this.format, width, height, 1, this.mipLevelCount);
		this.textureView = this.device.createTextureView(this.texture);
		this.textureSampler = this.device.createSampler(
			AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE,
			this.samplerFilterMode, this.samplerFilterMode,
			this.maxAnisotropy,
			OptionalDouble.empty());
		
		this.width = width;
		this.height = height;
		return true;
	}
	
	//endregion
	
	
	
	//===========//
	// write/clear //
	//===========//
	//region
	
	public void writeToTexture(ByteBuffer data, int mipLevel, int depthOrLayer, int destX, int destY, int width, int height)
	{
		CommandEncoder encoder = this.device.createCommandEncoder();
		encoder.writeToTexture(this.texture, data, mipLevel, depthOrLayer, destX, destY, width, height);
		encoder.submit();
	}
	
	public void clearColor(Vector4fc clearColor)
	{
		CommandEncoder encoder = this.device.createCommandEncoder();
		encoder.clearColorTexture(this.texture, clearColor);
		encoder.submit();
	}
	
	public void clearDepth(double clearDepth)
	{
		CommandEncoder encoder = this.device.createCommandEncoder();
		encoder.clearDepthTexture(this.texture, clearDepth);
		encoder.submit();
	}
	
	//endregion
	
	
	
	//=========//
	// getters //
	//=========//
	//region
	
	public String getName() { return this.name; }
	public GpuTexture getTexture() { return this.texture; }
	public GpuTextureView getTextureView() { return this.textureView; }
	public GpuSampler getTextureSampler() { return this.textureSampler; }
	public int getWidth() { return this.width; }
	public int getHeight() { return this.height; }
	
	//endregion
	
	
	
	//========//
	// close //
	//========//
	//region
	
	@Override
	public void close()
	{
		if (this.textureSampler != null)
		{
			this.textureSampler.close();
			this.textureSampler = null;
		}
		
		if (this.textureView != null)
		{
			this.textureView.close();
			this.textureView = null;
		}
		
		if (this.texture != null)
		{
			this.texture.close();
			this.texture = null;
		}
		
		this.width = -1;
		this.height = -1;
	}
	
	//endregion
	
	
	
}
#endif
