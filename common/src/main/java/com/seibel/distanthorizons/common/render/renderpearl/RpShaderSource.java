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

import com.mojang.renderpearl.api.pipeline.ShaderSource;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import net.minecraft.resources.Identifier;

import java.util.Map;

/**
 * Stage 1 (decision SA-7 / 7B): DH-owned {@link ShaderSource} serving embedded
 * minimal GLSL sources. The formal shader resource system lands in stage 4,
 * at which point this can be replaced by ShaderManager-backed loading.
 */
public class RpShaderSource implements ShaderSource
{
	public static final RpShaderSource INSTANCE = new RpShaderSource();
	
	public static final String TRIANGLE_VERT_ID = "distanthorizons:test/renderpearl/triangle_vert";
	public static final String TRIANGLE_FRAG_ID = "distanthorizons:test/renderpearl/triangle_frag";
	
	private static final String TRIANGLE_VERT_SRC = """
		#version 330
		#extension GL_ARB_separate_shader_objects : require

		layout(location = 0) in vec2 vPosition;
		layout(location = 1) in vec4 vColor;
		layout(location = 0) out vec4 vertexColor;

		void main() {
		    gl_Position = vec4(vPosition, 0.0, 1.0);
		    vertexColor = vColor;
		}
		""";
	
	private static final String TRIANGLE_FRAG_SRC = """
		#version 330
		#extension GL_ARB_separate_shader_objects : require

		layout(location = 0) in vec4 vertexColor;
		layout(location = 0) out vec4 fragColor;

		void main() {
		    fragColor = vertexColor;
		}
		""";
	
	private final Map<String, String> shaderSources = Map.of(
		TRIANGLE_VERT_ID, TRIANGLE_VERT_SRC,
		TRIANGLE_FRAG_ID, TRIANGLE_FRAG_SRC);
	
	
	
	//=========//
	// ShaderSource //
	//=========//
	//region
	
	@Override
	public String get(Identifier id, ShaderType type)
	{
		return this.shaderSources.get(id.toString());
	}
	
	/** fail-fast helper used by unit tests and pipeline setup */
	public boolean hasShader(Identifier id)
	{
		return this.shaderSources.containsKey(id.toString());
	}
	
	//endregion
	
	
	
}
#endif
