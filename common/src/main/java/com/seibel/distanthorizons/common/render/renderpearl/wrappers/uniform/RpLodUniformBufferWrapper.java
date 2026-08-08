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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodBufferContainer;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.objects.ILodContainerUniformBufferWrapper;

/**
 * Port of {@code BlazeLodUniformBufferWrapper} (26.2): uploads a LOD
 * container's model offset into a UBO (uploaded once per container).
 */
public class RpLodUniformBufferWrapper extends RpUniformBufferWrapper implements ILodContainerUniformBufferWrapper
{
	private boolean uploaded = false;
	
	
	
	//=============//
	// constructors //
	//=============//
	//region
	
	public RpLodUniformBufferWrapper()
	{
		this("distantHorizons:LodUniformBuffer", RenderSystem.getDevice());
	}
	
	public RpLodUniformBufferWrapper(String name, GpuDevice device)
	{
		super(name, device);
	}
	
	//endregion
	
	
	
	//========//
	// upload //
	//========//
	//region
	
	@Override
	public void tryUpload(LodBufferContainer bufferContainer)
	{
		if (this.uploaded)
		{
			return;
		}
		
		this
			.putVec3f(
				(float) bufferContainer.minCornerBlockPos.getX(),
				(float) bufferContainer.minCornerBlockPos.getY(),
				(float) bufferContainer.minCornerBlockPos.getZ())
			.uploadMapped();
		
		this.uploaded = true;
	}
	
	//endregion
	
	
	
}
#endif
