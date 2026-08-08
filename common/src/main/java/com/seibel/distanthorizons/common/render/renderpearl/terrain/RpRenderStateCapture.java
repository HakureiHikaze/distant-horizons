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

package com.seibel.distanthorizons.common.render.renderpearl.terrain;

#if MC_VER >= MC_26_3_0

import com.seibel.distanthorizons.common.wrappers.McObjectConverter;
import com.seibel.distanthorizons.core.api.internal.ClientApi;
import com.seibel.distanthorizons.core.wrapperInterfaces.world.IClientLevelWrapper;
import org.joml.Matrix4fc;

/**
 * Fills DH's per-frame render state from 26.3's {@code CameraRenderState}.
 * Extracted from the mixin so it can be unit tested (UT-2-1).
 */
public class RpRenderStateCapture
{
	public static void capture(
		Matrix4fc projectionMatrix,
		Matrix4fc modelViewMatrix,
		float partialTickTime,
		IClientLevelWrapper clientLevelWrapper)
	{
		ClientApi.RENDER_STATE.mcProjectionMatrix = McObjectConverter.convert(projectionMatrix);
		ClientApi.RENDER_STATE.mcModelViewMatrix = McObjectConverter.convert(modelViewMatrix);
		ClientApi.RENDER_STATE.partialTickTime = partialTickTime;
		ClientApi.RENDER_STATE.clientLevelWrapper = clientLevelWrapper;
		ClientApi.RENDER_STATE.vanillaFogEnabled = true;
	}
	
	
	
}
#endif
