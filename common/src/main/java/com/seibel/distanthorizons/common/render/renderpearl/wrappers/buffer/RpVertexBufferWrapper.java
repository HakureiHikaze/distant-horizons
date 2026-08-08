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
import com.seibel.distanthorizons.core.dependencyInjection.SingletonInjector;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.AbstractDhRenderApiDefinition;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.objects.IVertexBufferWrapper;

import java.nio.ByteBuffer;

/**
 * Port of {@code BlazeVertexBufferWrapper} (26.2) to renderpearl.
 * The global single-IBO optimization is deferred to stage 2/6; per-wrapper
 * index buffers honor the {@link AbstractDhRenderApiDefinition#useSingleIbo()}
 * contract (no upload when a single IBO is expected).
 */
public class RpVertexBufferWrapper implements IVertexBufferWrapper
{
	private final String name;
	private final GpuDevice device;
	
	private GpuBuffer vertexGpuBuffer = null;
	private GpuBuffer indexGpuBuffer = null;
	
	private int vertexCount = -1;
	private int indexCount = -1;
	private boolean uploaded = false;
	
	
	
	//=============//
	// constructors //
	//=============//
	//region
	
	public RpVertexBufferWrapper(String name)
	{
		this(name, RenderSystem.getDevice());
	}
	
	/** allows injecting a device without booting Minecraft (used by unit tests) */
	public RpVertexBufferWrapper(String name, GpuDevice device)
	{
		this.name = name;
		this.device = device;
	}
	
	//endregion
	
	
	
	//========//
	// upload //
	//========//
	//region
	
	@Override
	public void uploadVertexBuffer(ByteBuffer vertexBuffer, int vertexCount)
	{
		int oldVertexCount = this.vertexCount;
		
		this.vertexCount = vertexCount;
		// 4 vertices per face, but 6 indices (IE 2 triangles) per face, aka need to multiply by 1.5
		this.indexCount = (int) (vertexCount * 1.5);
		this.uploaded = true;
		
		
		
		if (this.vertexGpuBuffer == null
			// recreating if the size changes is always necessary (even if we only need a smaller amount)
			// due to a bug on Mac where it will attempt to render anything allocated in the buffer
			|| oldVertexCount != vertexCount)
		{
			if (this.vertexGpuBuffer != null)
			{
				this.vertexGpuBuffer.close();
			}
			
			int byteSize = vertexBuffer.limit() - vertexBuffer.position();
			int usage = GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_VERTEX;
			this.vertexGpuBuffer = this.device.createBuffer(this::getName, usage, byteSize);
			
			GpuBufferSlice bufferSlice = new GpuBufferSlice(this.vertexGpuBuffer, 0, byteSize);
			CommandEncoder encoder = this.device.createCommandEncoder();
			encoder.writeToBuffer(bufferSlice, vertexBuffer);
			encoder.submit();
		}
	}
	
	@Override
	public void uploadIndexBuffer(ByteBuffer indexBuffer, int vertexCount)
	{
		int oldIndexCount = this.indexCount;
		// 4 vertices per face, but 6 indices (IE 2 triangles) per face, aka need to multiply by 1.5
		this.indexCount = (int) (vertexCount * 1.5);
		
		if (this.useSingleIbo())
		{
			// ignore index uploading when running a single IBO (global buffer created in stage 2)
			return;
		}
		
		
		
		if (this.indexGpuBuffer == null
			|| oldIndexCount != this.indexCount)
		{
			if (this.indexGpuBuffer != null)
			{
				this.indexGpuBuffer.close();
			}
			
			int byteSize = indexBuffer.limit() - indexBuffer.position();
			int usage = GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_INDEX;
			this.indexGpuBuffer = this.device.createBuffer(this::getIndexBufferName, usage, byteSize);
			
			GpuBufferSlice bufferSlice = new GpuBufferSlice(this.indexGpuBuffer, 0, byteSize);
			CommandEncoder encoder = this.device.createCommandEncoder();
			encoder.writeToBuffer(bufferSlice, indexBuffer);
			encoder.submit();
		}
	}
	
	//endregion
	
	
	
	//=========//
	// getters //
	//=========//
	//region
	
	public String getName() { return this.name; }
	
	public GpuBuffer getVertexGpuBuffer() { return this.vertexGpuBuffer; }
	public GpuBuffer getIndexGpuBuffer() { return this.indexGpuBuffer; }
	
	public int getVertexCount() { return this.vertexCount; }
	public int getIndexCount() { return this.indexCount; }
	public boolean isUploaded() { return this.uploaded; }
	
	private String getIndexBufferName() { return "distantHorizons:LodIndexBuffer"; }
	
	private boolean useSingleIbo()
	{
		try
		{
			AbstractDhRenderApiDefinition renderDefinition = SingletonInjector.INSTANCE.get(AbstractDhRenderApiDefinition.class);
			return renderDefinition != null && renderDefinition.useSingleIbo();
		}
		catch (Exception ignore)
		{
			// not bound yet (e.g. JVM unit tests): fall back to per-wrapper buffers
			return false;
		}
	}
	
	//endregion
	
	
	
	//========//
	// close //
	//========//
	//region
	
	@Override
	public void close()
	{
		if (this.vertexGpuBuffer != null)
		{
			this.vertexGpuBuffer.close();
			this.vertexGpuBuffer = null;
		}
		
		if (this.indexGpuBuffer != null)
		{
			this.indexGpuBuffer.close();
			this.indexGpuBuffer = null;
		}
	}
	
	//endregion
	
	
	
}
#endif
