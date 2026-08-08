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

package com.seibel.distanthorizons.common.render.renderpearl.wrappers.uniform;

#if MC_VER >= MC_26_3_0

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.seibel.distanthorizons.api.objects.math.DhApiMat4f;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Port of {@code BlazeUniformBufferWrapper} (26.2): builds a Std140-layout
 * CPU buffer and uploads it to a {@link GpuBuffer} bound through
 * {@code RenderPass.setUniform(name, GpuBufferSlice)}.
 */
public class RpUniformBufferWrapper implements AutoCloseable
{
	private final String name;
	private final GpuDevice device;
	
	private ByteBuffer cpuBuffer = ByteBuffer.allocateDirect(256).order(ByteOrder.nativeOrder());
	private Std140Builder builder = Std140Builder.intoBuffer(this.cpuBuffer);
	private int bufferSize = 0;
	
	private GpuBuffer gpuBuffer = null;
	
	
	
	//=============//
	// constructors //
	//=============//
	//region
	
	public RpUniformBufferWrapper(String name)
	{
		this(name, RenderSystem.getDevice());
	}
	
	public RpUniformBufferWrapper(String name, GpuDevice device)
	{
		this.name = name;
		this.device = device;
	}
	
	//endregion
	
	
	
	//=========//
	// putting //
	//=========//
	//region
	
	public RpUniformBufferWrapper putFloat(float value)
	{
		this.ensureCapacity(4);
		this.builder.putFloat(value);
		this.updateSize();
		return this;
	}
	
	public RpUniformBufferWrapper putInt(int value)
	{
		this.ensureCapacity(4);
		this.builder.putInt(value);
		this.updateSize();
		return this;
	}
	
	public RpUniformBufferWrapper putVec2f(float x, float y)
	{
		this.ensureCapacity(8);
		this.builder.putVec2(x, y);
		this.updateSize();
		return this;
	}
	
	public RpUniformBufferWrapper putVec3f(float x, float y, float z)
	{
		this.ensureCapacity(16);
		this.builder.putVec3(x, y, z);
		this.updateSize();
		return this;
	}
	
	public RpUniformBufferWrapper putVec4f(float x, float y, float z, float w)
	{
		this.ensureCapacity(16);
		this.builder.putVec4(x, y, z, w);
		this.updateSize();
		return this;
	}
	
	public RpUniformBufferWrapper putMat4f(DhApiMat4f matrix)
	{
		this.ensureCapacity(64);
		this.builder.putMat4f(new Matrix4f().set(matrix.getValuesAsArray()));
		this.updateSize();
		return this;
	}
	
	//endregion
	
	
	
	//===========//
	// upload //
	//===========//
	//region
	
	public void finishAndUpload()
	{
		if (this.bufferSize == 0)
		{
			return;
		}
		
		if (this.gpuBuffer == null
			|| this.gpuBuffer.size() < this.bufferSize)
		{
			if (this.gpuBuffer != null)
			{
				this.gpuBuffer.close();
			}
			
			int usage = GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_UNIFORM;
			this.gpuBuffer = this.device.createBuffer(this::getName, usage, this.bufferSize);
		}
		
		ByteBuffer data = this.cpuBuffer.duplicate();
		data.position(0);
		data.limit(this.bufferSize);
		
		CommandEncoder encoder = this.device.createCommandEncoder();
		encoder.writeToBuffer(this.gpuBuffer.slice(), data);
		encoder.submit();
	}
	
	/**
	 * Uploads the CPU buffer through a mapped GPU write instead of a command
	 * encoder. Safe to call while a render pass is open (stage 2 terrain
	 * uniforms are updated inside {@code renderGroup}).
	 */
	public void uploadMapped()
	{
		if (this.bufferSize == 0)
		{
			return;
		}
		
		if (this.gpuBuffer == null
			|| this.gpuBuffer.size() < this.bufferSize)
		{
			if (this.gpuBuffer != null)
			{
				this.gpuBuffer.close();
			}
			
			int usage = GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_UNIFORM;
			this.gpuBuffer = this.device.createBuffer(this::getName, usage, this.bufferSize);
		}
		
		ByteBuffer data = this.cpuBuffer.duplicate();
		data.position(0);
		data.limit(this.bufferSize);
		
		try (GpuBufferSlice.MappedView mapped = this.gpuBuffer.map(0, this.bufferSize, false, true))
		{
			ByteBuffer destination = mapped.data().duplicate();
			destination.position(0);
			destination.limit(this.bufferSize);
			destination.put(data);
		}
	}
	
	//endregion
	
	
	
	//=========//
	// helpers //
	//=========//
	//region
	
	private void ensureCapacity(int extraBytes)
	{
		if (this.cpuBuffer.position() + extraBytes <= this.cpuBuffer.capacity())
		{
			return;
		}
		
		int newCapacity = Math.max(this.cpuBuffer.capacity() * 2, this.cpuBuffer.position() + extraBytes);
		ByteBuffer newBuffer = ByteBuffer.allocateDirect(newCapacity).order(ByteOrder.nativeOrder());
		this.cpuBuffer.flip();
		newBuffer.put(this.cpuBuffer);
		this.cpuBuffer = newBuffer;
		this.builder = Std140Builder.intoBuffer(this.cpuBuffer);
	}
	
	private void updateSize()
	{
		this.bufferSize = this.cpuBuffer.position();
	}
	
	//endregion
	
	
	
	//=========//
	// getters //
	//=========//
	//region
	
	public String getName() { return this.name; }
	public GpuBuffer getGpuBuffer() { return this.gpuBuffer; }
	public GpuBufferSlice getBufferSlice() { return this.gpuBuffer == null ? null : this.gpuBuffer.slice(); }
	public int getBufferSize() { return this.bufferSize; }
	
	//endregion
	
	
	
	//========//
	// close //
	//========//
	//region
	
	@Override
	public void close()
	{
		if (this.gpuBuffer != null)
		{
			this.gpuBuffer.close();
			this.gpuBuffer = null;
		}
	}
	
	//endregion
	
	
	
}
#endif
