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

package com.seibel.distanthorizons.common.render.renderpearl;

#if MC_VER >= MC_26_3_0

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.vertex.VertexFormat;

/**
 * Defines the LOD vertex attribute set shared by the renderpearl wrapper layer
 * (stage 1) and the terrain renderer (stage 2). Attribute order == the
 * shader's layout(location) order, locked by unit test UT-2.
 *
 * @see com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodQuadBuilder
 */
public class RpVertexFormatUtil
{
	// test triangle // 
	public static final GpuFormat SCREEN_POS_FORMAT = GpuFormat.RG32_FLOAT;
	public static final GpuFormat RGBA_FLOAT_COLOR_FORMAT = GpuFormat.RGBA32_FLOAT;
	
	// LOD terrain //
	public static final GpuFormat SHORT_XYZ_POS_FORMAT = GpuFormat.RGB16_UINT;
	public static final GpuFormat BYTE_PAD_FORMAT = GpuFormat.R8_UINT;
	/** contains light and micro-offset */
	public static final GpuFormat META_FORMAT = GpuFormat.R16_UINT;
	public static final GpuFormat RGBA_UBYTE_COLOR_FORMAT = GpuFormat.RGBA8_UNORM;
	public static final GpuFormat IRIS_MATERIAL_FORMAT = GpuFormat.R8_UINT;
	public static final GpuFormat IRIS_NORMAL_FORMAT = GpuFormat.R8_UINT;
	/** {@link com.seibel.distanthorizons.core.dataObjects.render.textures.BlockTextureRegistry} tile id, 0 = flat color */
	public static final GpuFormat TEXTURE_TILE_FORMAT = GpuFormat.R16_UINT;
	public static final GpuFormat FLOAT_XYZ_POS_FORMAT = GpuFormat.RGB32_FLOAT;
	
	public static final String POSITION_ATTRIBUTE = "vPosition";
	public static final String COLOR_ATTRIBUTE = "vColor";
	public static final String META_ATTRIBUTE = "meta";
	public static final String IRIS_MATERIAL_ATTRIBUTE = "irisMaterial";
	public static final String IRIS_NORMAL_ATTRIBUTE = "irisNormal";
	public static final String TEXTURE_TILE_ATTRIBUTE = "textureTile";
	
	
	
	//==============//
	// format build //
	//==============//
	//region
	
	/** screen-space position + RGBA float color, used by the test triangle */
	public static VertexFormat createScreenPosColorFormat()
	{
		return VertexFormat.builder(0)
			.addAttribute(POSITION_ATTRIBUTE, SCREEN_POS_FORMAT)
			.addAttribute(COLOR_ATTRIBUTE, RGBA_FLOAT_COLOR_FORMAT)
			.build();
	}
	
	/**
	 * LOD terrain vertex format. Attribute order matches the LOD shader's
	 * layout(location) contract; the byte-level mapping to the core CPU mesh
	 * (LodQuadBuilder) is verified in stage 2.
	 */
	public static VertexFormat createLodVertexFormat()
	{
		return VertexFormat.builder(0)
			.addAttribute(POSITION_ATTRIBUTE, SHORT_XYZ_POS_FORMAT)
			.addAttribute(META_ATTRIBUTE, META_FORMAT)
			.addAttribute(COLOR_ATTRIBUTE, RGBA_UBYTE_COLOR_FORMAT)
			.addAttribute(IRIS_MATERIAL_ATTRIBUTE, IRIS_MATERIAL_FORMAT)
			.addAttribute(IRIS_NORMAL_ATTRIBUTE, IRIS_NORMAL_FORMAT)
			.addAttribute(TEXTURE_TILE_ATTRIBUTE, TEXTURE_TILE_FORMAT)
			.build();
	}
	
	//endregion
	
	
	
}
#endif
