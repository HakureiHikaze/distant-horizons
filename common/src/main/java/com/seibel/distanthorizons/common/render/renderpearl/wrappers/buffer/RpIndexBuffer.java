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

package com.seibel.distanthorizons.common.render.renderpearl.wrappers.buffer;

#if MC_VER >= MC_26_3_0

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.device.GpuDevice;

import java.nio.ByteBuffer;

/**
 * Standalone index buffer wrapper for renderpearl, used by terrain/generic
 * renderers in later stages. Creation/uploads happen on the render thread.
 */
public class RpIndexBuffer implements AutoCloseable
{
	private final String name;
	private final GpuDevice device;
	
	private GpuBuffer indexGpuBuffer = null;
	private int indexCount = 0;
	
	
	
	//=============//
	// constructors //
	//=============//
	//region
	
	public RpIndexBuffer(String name)
	{
		this(name, RenderSystem.getDevice());
	}
	
	public RpIndexBuffer(String name, GpuDevice device)
	{
		this.name = name;
		this.device = device;
	}
	
	//endregion
	
	
	
	//========//
	// upload //
	//========//
	//region
	
	/**
	 * Contract (audit F7): the caller must pass a buffer positioned at 0 with
	 * limit == byte length.
	 */
	public void uploadIndexBuffer(ByteBuffer indexBuffer, int indexCount)
	{
		if (this.indexGpuBuffer == null
			|| this.indexCount != indexCount)
		{
			if (this.indexGpuBuffer != null)
			{
				this.indexGpuBuffer.close();
			}
			
			int byteSize = indexBuffer.limit() - indexBuffer.position();
			int usage = GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_INDEX;
			this.indexGpuBuffer = this.device.createBuffer(this::getName, usage, byteSize);
			
			GpuBufferSlice bufferSlice = new GpuBufferSlice(this.indexGpuBuffer, 0, byteSize);
			CommandEncoder encoder = this.device.createCommandEncoder();
			encoder.writeToBuffer(bufferSlice, indexBuffer);
			encoder.submit();
		}
		
		this.indexCount = indexCount;
	}
	
	//endregion
	
	
	
	//=========//
	// getters //
	//=========//
	//region
	
	public String getName() { return this.name; }
	public GpuBuffer getBuffer() { return this.indexGpuBuffer; }
	public GpuBufferSlice getSlice() { return this.indexGpuBuffer == null ? null : this.indexGpuBuffer.slice(); }
	public int getIndexCount() { return this.indexCount; }
	
	//endregion
	
	
	
	//========//
	// close //
	//========//
	//region
	
	@Override
	public void close()
	{
		if (this.indexGpuBuffer != null)
		{
			this.indexGpuBuffer.close();
			this.indexGpuBuffer = null;
		}
	}
	
	//endregion
	
	
	
}
#endif
