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
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PolygonMode;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Port of {@code BlazeRenderPipelineBuilderWrapper} (26.2) to the 26.3
 * renderpearl pipeline model. Maps DH-style render options onto
 * {@link RenderPipeline.Builder}.
 */
public class RpRenderPipelineBuilderWrapper
{
	public static final String NAME_PREFIX = "distanthorizons:";
	
	
	private final RenderPipeline.Builder pipelineBuilder;
	
	
	// variables for specific builder options should be put next to their builder methods for simpler organization
	
	
	
	//=============//
	// constructor //
	//=============//
	//region
	
	public RpRenderPipelineBuilderWrapper()
	{
		this.pipelineBuilder = RenderPipeline.builder();
	}
	
	//endregion
	
	
	
	//==========//
	// building //
	//==========//
	//region
	
	private boolean writeDepth = false;
	public RpRenderPipelineBuilderWrapper withDepthWrite(boolean write)
	{
		this.writeDepth = write;
		return this;
	}
	
	private boolean writeColor = false;
	public RpRenderPipelineBuilderWrapper withColorWrite(boolean write)
	{
		this.writeColor = write;
		return this;
	}
	
	private BlendFunction blendFunction = null;
	public RpRenderPipelineBuilderWrapper withBlend(BlendFunction blendFunction)
	{
		this.blendFunction = blendFunction;
		return this;
	}
	public RpRenderPipelineBuilderWrapper withoutBlend()
	{
		this.blendFunction = null;
		return this;
	}
	
	private EDhDepthTest depthTest;
	public RpRenderPipelineBuilderWrapper withDepthTest(EDhDepthTest depthTest)
	{
		this.depthTest = depthTest;
		return this;
	}
	
	public RpRenderPipelineBuilderWrapper withFaceCulling(boolean culling)
	{
		this.pipelineBuilder.withCull(culling);
		return this;
	}
	
	public RpRenderPipelineBuilderWrapper withPolygonMode(EDhPolygonMode dhMode)
	{
		PolygonMode polygonMode;
		switch (dhMode)
		{
			case FILL:
				polygonMode = PolygonMode.FILL;
				break;
			case WIREFRAME:
				polygonMode = PolygonMode.WIREFRAME;
				break;
				
			default:
				throw new UnsupportedOperationException("No polygonMode defined for type ["+dhMode+"].");
		}
		
		this.pipelineBuilder.withPolygonMode(polygonMode);
		return this;
	}
	
	public RpRenderPipelineBuilderWrapper withName(String name) throws IllegalArgumentException
	{
		// Identifiers must be of a specific format
		if (!isValidIdentifier(name))
		{
			throw new IllegalArgumentException("Non [a-z0-9/._-] character in name: ["+name+"].");
		}
		
		this.pipelineBuilder.withLocation(Identifier.parse(NAME_PREFIX + name));
		return this;
	}
	
	private final ArrayList<String> samplerNames = new ArrayList<>();
	public RpRenderPipelineBuilderWrapper withSampler(String name) throws IllegalArgumentException
	{
		this.samplerNames.add(name);
		return this;
	}
	
	private final ArrayList<String> uniformBufferNames = new ArrayList<>();
	public RpRenderPipelineBuilderWrapper withUniformBuffer(String name) throws IllegalArgumentException
	{
		this.uniformBufferNames.add(name);
		return this;
	}
	
	private VertexFormat vertexFormat = null;
	public RpRenderPipelineBuilderWrapper withVertexFormat(VertexFormat vertexFormat)
	{
		this.vertexFormat = vertexFormat;
		return this;
	}
	
	private EDhVertexMode vertexMode = null;
	public RpRenderPipelineBuilderWrapper withVertexMode(EDhVertexMode vertexMode)
	{
		this.vertexMode = vertexMode;
		return this;
	}
	
	public RpRenderPipelineBuilderWrapper withVertexShader(String scriptResourcePath) { return this.withShader(EDhShaderType.VERTEX, scriptResourcePath); }
	public RpRenderPipelineBuilderWrapper withFragmentShader(String scriptResourcePath) { return this.withShader(EDhShaderType.FRAGMENT, scriptResourcePath); }
	private RpRenderPipelineBuilderWrapper withShader(EDhShaderType shaderType, String scriptResourcePath)
	{
		// shader existence is validated at compile time by the owning PipelineCache/ShaderSource (SA-7/7B)
		Identifier shaderId = Identifier.parse(NAME_PREFIX + scriptResourcePath);
		if (shaderType == EDhShaderType.VERTEX)
		{
			this.pipelineBuilder.withVertexShader(shaderId);
		}
		else
		{
			this.pipelineBuilder.withFragmentShader(shaderId);
		}
		
		return this;
	}
	
	//endregion
	
	
	
	//=====//
	// end //
	//=====//
	//region
	
	public RenderPipeline build() throws UnsupportedOperationException
	{
		// depth/color
		{
			CompareOp compareOp;
			switch (this.depthTest)
			{
				case NONE:
					compareOp = CompareOp.ALWAYS_PASS;
					break;
				case LESS:
					compareOp = CompareOp.LESS_THAN;
					break;
				case GREATER:
					compareOp = CompareOp.GREATER_THAN;
					break;
				
				default:
					throw new UnsupportedOperationException("No depth test defined for type ["+this.depthTest+"].");
			}
			this.pipelineBuilder.withDepthStencilState(new DepthStencilState(compareOp, this.writeDepth));
			
			this.pipelineBuilder.withColorTargetState(
				new ColorTargetState(
					Optional.ofNullable(this.blendFunction),
					GpuFormat.RGBA8_UNORM,
					this.writeColor ? ColorTargetState.WRITE_ALL : ColorTargetState.WRITE_NONE
				)
			);
		}
		
		
		// vertex format
		{
			PrimitiveTopology primitiveTopology;
			switch (this.vertexMode)
			{
				case TRIANGLES:
					primitiveTopology = PrimitiveTopology.TRIANGLES;
					break;
				case TRIANGLE_FAN:
					primitiveTopology = PrimitiveTopology.TRIANGLE_FAN;
					break;
				case LINES:
					primitiveTopology = PrimitiveTopology.DEBUG_LINES;
					break;
				
				default:
					throw new UnsupportedOperationException("No PrimitiveTopology defined for type ["+this.vertexMode+"].");
			}
			
			this.pipelineBuilder.withVertexBinding(0, this.vertexFormat);
			this.pipelineBuilder.withPrimitiveTopology(primitiveTopology);
		}
		
		
		// uniforms & samplers
		{
			BindGroupLayout.Builder bindGroupBuilder = BindGroupLayout.builder();
			
			for (String name : this.samplerNames)
			{
				bindGroupBuilder.withUniform(name, UniformType.COMBINED_IMAGE_SAMPLER);
			}
			
			for (String name : this.uniformBufferNames)
			{
				bindGroupBuilder.withUniform(name, UniformType.UNIFORM_BUFFER);
			}
			
			this.pipelineBuilder.withBindGroupLayout(bindGroupBuilder.build());
		}
		
		
		return this.pipelineBuilder.build();
	}
	
	//endregion
	
	
	
	//================//
	// helper methods //
	//================//
	//region
	
	private static boolean isValidIdentifier(String identifier)
	{
		for (int i = 0; i < identifier.length(); i++)
		{
			char ch = identifier.charAt(i);
			if (!isValidNamespaceChar(ch))
			{
				return false;
			}
		}
		
		return true;
	}
	private static boolean isValidNamespaceChar(final char ch)
	{
		return ch == '_'
			|| ch == '-'
			// only lower case characters
			|| (ch >= 'a' && ch <= 'z')
			|| (ch >= '0' && ch <= '9')
			|| ch == '.';
	}
	
	//endregion
	
	
	
	//================//
	// helper classes //
	//================//
	//region
	
	public enum EDhPolygonMode
	{
		FILL,
		WIREFRAME;
	}
	
	public enum EDhVertexMode
	{
		TRIANGLES,
		TRIANGLE_FAN,
		LINES;
	}
	
	public enum EDhDepthTest
	{
		NONE,
		GREATER,
		LESS;
	}
	
	private enum EDhShaderType
	{
		FRAGMENT,
		VERTEX;
	}
	
	//endregion
	
	
	
}
#endif
