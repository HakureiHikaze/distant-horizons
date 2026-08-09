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

import com.mojang.renderpearl.api.pipeline.ShaderSource;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import net.minecraft.resources.Identifier;

import java.util.Map;

/**
 * Stage 2 (SA-7/7B): embedded minimal LOD terrain shaders. No fog/fade/texture
 * atlas in this stage; the fragment shader outputs the vertex color so LOD
 * terrain is visible as flat-colored geometry. Stage 4 adds fog/fade and
 * stage 6 adds lightmap/atlas sampling.
 */
public class RpTerrainShaderSources implements ShaderSource
{
	public static final RpTerrainShaderSources INSTANCE = new RpTerrainShaderSources();
	
	public static final String TERRAIN_VERT_ID = "distanthorizons:terrain/renderpearl/vert";
	public static final String TERRAIN_FRAG_ID = "distanthorizons:terrain/renderpearl/frag";
	
	private static final String TERRAIN_VERT_SRC = """
		#version 330
		#extension GL_ARB_separate_shader_objects : require

		layout(location = 0) in uvec3 vPosition;
		layout(location = 1) in uint meta;
		layout(location = 2) in vec4 vColor;
		layout(location = 3) in uint irisMaterial;
		layout(location = 4) in uint irisNormal;
		layout(location = 5) in uint textureTile;

		layout(location = 0) out vec4 vertexColor;

		layout (std140) uniform vertUniqueUniformBlock
		{
		    vec3 uModelOffset;
		};

		layout (std140) uniform vertSharedUniformBlock
		{
		    bool uIsWhiteWorld;
		    float uWorldYOffset;
		    float uMircoOffset;
		    float uEarthRadius;
		    vec3 uCameraPos;
		    mat4 uCombinedMatrix;
		};

		void main()
		{
		    vec3 vertexWorldPos = vec3(vPosition) + (uModelOffset - uCameraPos);
		    vertexColor = uIsWhiteWorld ? vec4(1.0) : vColor;
		    gl_Position = uCombinedMatrix * vec4(vertexWorldPos, 1.0);
		}
		""";
	
	private static final String TERRAIN_FRAG_SRC = """
		#version 330
		#extension GL_ARB_separate_shader_objects : require

		layout(location = 0) in vec4 vertexColor;
		layout(location = 0) out vec4 fragColor;

		void main()
		{
		    fragColor = vertexColor;
		}
		""";
	
	private final Map<String, String> shaderSources = Map.of(
		TERRAIN_VERT_ID, TERRAIN_VERT_SRC,
		TERRAIN_FRAG_ID, TERRAIN_FRAG_SRC);
	
	
	
	@Override
	public String get(Identifier id, ShaderType type)
	{
		return this.shaderSources.get(id.toString());
	}
	
	public boolean hasShader(Identifier id)
	{
		return this.shaderSources.containsKey(id.toString());
	}
	
	
	
}
#endif
