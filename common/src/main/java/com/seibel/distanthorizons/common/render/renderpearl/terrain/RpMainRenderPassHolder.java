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

import com.mojang.renderpearl.api.commands.RenderPass;

/**
 * Carries Minecraft's active main {@link RenderPass} from the
 * {@code ChunkSectionsToRender.renderGroup} injection point to DH's terrain
 * renderer (D2 方案 A: draw directly into the main pass, no DH FBO).
 */
public class RpMainRenderPassHolder
{
	private static RenderPass currentPass = null;
	
	
	
	public static void setCurrent(RenderPass renderPass) { currentPass = renderPass; }
	public static void clear() { currentPass = null; }
	public static RenderPass getCurrent() { return currentPass; }
	
	
	
}
#endif
