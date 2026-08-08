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

import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.seibel.distanthorizons.common.render.renderpearl.RpVertexFormatUtil;

/**
 * Single source of truth for the LOD terrain vertex layout (stage 2).
 * Attribute order == shader layout(location); byte mapping to the core CPU
 * mesh is locked by UT-2-2.
 */
public class RpLodVertexLayout
{
	public static final String POSITION_ATTRIBUTE = RpVertexFormatUtil.POSITION_ATTRIBUTE;
	public static final String META_ATTRIBUTE = RpVertexFormatUtil.META_ATTRIBUTE;
	public static final String COLOR_ATTRIBUTE = RpVertexFormatUtil.COLOR_ATTRIBUTE;
	public static final String IRIS_MATERIAL_ATTRIBUTE = RpVertexFormatUtil.IRIS_MATERIAL_ATTRIBUTE;
	public static final String IRIS_NORMAL_ATTRIBUTE = RpVertexFormatUtil.IRIS_NORMAL_ATTRIBUTE;
	public static final String TEXTURE_TILE_ATTRIBUTE = RpVertexFormatUtil.TEXTURE_TILE_ATTRIBUTE;
	
	
	
	public static VertexFormat getFormat()
	{
		return RpVertexFormatUtil.createLodVertexFormat();
	}
	
	
	
}
#endif
