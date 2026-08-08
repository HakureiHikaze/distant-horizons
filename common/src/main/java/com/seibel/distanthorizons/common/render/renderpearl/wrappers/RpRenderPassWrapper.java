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

package com.seibel.distanthorizons.common.render.renderpearl.wrappers;

#if MC_VER >= MC_26_3_0

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;

import java.util.function.Supplier;

/**
 * Port of {@code BlazeRenderPassWrapper} (26.2) to the renderpearl
 * {@link RenderPass} model. A render pass is created from a command encoder;
 * {@link #close()} closes the pass and submits the encoder.
 */
public class RpRenderPassWrapper implements AutoCloseable
{
	private final CommandEncoder encoder;
	private final RenderPass renderPass;
	
	
	
	//=============//
	// constructor //
	//=============//
	//region
	
	public RpRenderPassWrapper(
		final Supplier<String> nameGetterFunc,
		final GpuTextureView colorTexture,
		final GpuTextureView depthTexture,
		final GpuDevice device)
	{
		this.encoder = device.createCommandEncoder();
		
		RenderPassDescriptor descriptor = RenderPassDescriptor.builder(nameGetterFunc)
			.withColorAttachment(colorTexture)
			.withDepthAttachment(depthTexture)
			.build();
		this.renderPass = this.encoder.createRenderPass(descriptor);
	}
	
	//endregion
	
	
	
	//=======//
	// setup //
	//=======//
	//region
	
	public void setPipeline(CompiledRenderPipeline pipeline) { this.renderPass.setPipeline(pipeline); }
	
	public void setVertexBuffer(GpuBufferSlice vertexBuffer) { this.renderPass.setVertexBuffer(0, vertexBuffer); }
	public void setIndexBuffer(GpuBuffer indexBuffer, IndexType indexType) { this.renderPass.setIndexBuffer(indexBuffer, indexType); }
	
	public void setUniform(String uniformName, GpuBufferSlice uniformBuffer) { this.renderPass.setUniform(uniformName, uniformBuffer); }
	public void setUniform(String uniformName, GpuTextureView textureView, GpuSampler sampler) { this.renderPass.setUniform(uniformName, textureView, sampler); }
	
	//endregion
	
	
	
	//===========//
	// rendering //
	//===========//
	//region
	
	public void draw(int vertexCount)
	{
		this.renderPass.draw(vertexCount, 1, 0, 0);
	}
	
	public void drawIndexed(int indexCount)
	{
		this.renderPass.drawIndexed(indexCount, 1, 0, 0, 0);
	}
	
	//endregion
	
	
	
	//================//
	// base overrides //
	//================//
	//region
	
	@Override
	public void close()
	{
		this.renderPass.close();
		this.encoder.submit();
	}
	
	//endregion
	
	
	
}
#endif
