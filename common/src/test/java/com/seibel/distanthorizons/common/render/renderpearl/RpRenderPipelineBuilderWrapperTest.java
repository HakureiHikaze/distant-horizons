package com.seibel.distanthorizons.common.render.renderpearl;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** UT-1: DH render options are mapped onto the renderpearl pipeline model. */
class RpRenderPipelineBuilderWrapperTest
{
	@Test
	void pipelineOptionMapping()
	{
		RpRenderPipelineBuilderWrapper wrapper = new RpRenderPipelineBuilderWrapper();
		wrapper.withFaceCulling(false);
		wrapper.withDepthWrite(true);
		wrapper.withDepthTest(RpRenderPipelineBuilderWrapper.EDhDepthTest.GREATER);
		wrapper.withColorWrite(true);
		wrapper.withBlend(BlendFunction.TRANSLUCENT);
		wrapper.withName("ut1_pipeline");
		wrapper.withVertexShader("test/ut1_vert");
		wrapper.withFragmentShader("test/ut1_frag");
		wrapper.withVertexFormat(RpVertexFormatUtil.createScreenPosColorFormat());
		wrapper.withVertexMode(RpRenderPipelineBuilderWrapper.EDhVertexMode.TRIANGLES);
		wrapper.withSampler("Sampler0");
		wrapper.withUniformBuffer("Uniform0");
		
		RenderPipeline pipeline = wrapper.build();
		
		Assertions.assertEquals(Identifier.parse("distanthorizons:ut1_pipeline"), pipeline.getLocation());
		Assertions.assertNotNull(pipeline.getDepthStencilState());
		Assertions.assertEquals(CompareOp.GREATER_THAN, pipeline.getDepthStencilState().depthTest());
		Assertions.assertTrue(pipeline.getDepthStencilState().writeDepth());
		Assertions.assertEquals(PrimitiveTopology.TRIANGLES, pipeline.getPrimitiveTopology());
		Assertions.assertFalse(pipeline.isCull());
		Assertions.assertEquals(RpVertexFormatUtil.createScreenPosColorFormat(), pipeline.getVertexFormatBinding(0));
		
		Assertions.assertEquals(1, pipeline.getColorTargetStates().size());
		Assertions.assertEquals(BlendFunction.TRANSLUCENT, pipeline.getColorTargetStates().get(0).blendFunction().orElseThrow());
		Assertions.assertEquals(GpuFormat.RGBA8_UNORM, pipeline.getColorTargetStates().get(0).format());
		Assertions.assertEquals(15, pipeline.getColorTargetStates().get(0).writeMask());
		
		Assertions.assertEquals(1, pipeline.getBindGroupLayouts().size());
		BindGroupLayout layout = pipeline.getBindGroupLayouts().get(0);
		Map<String, UniformType> uniforms = layout.uniforms().stream()
			.collect(Collectors.toMap(BindGroupLayout.UniformDescription::name, BindGroupLayout.UniformDescription::type));
		Assertions.assertEquals(UniformType.COMBINED_IMAGE_SAMPLER, uniforms.get("Sampler0"));
		Assertions.assertEquals(UniformType.UNIFORM_BUFFER, uniforms.get("Uniform0"));
	}
	
	@Test
	void missingFragmentShaderThrows()
	{
		RpRenderPipelineBuilderWrapper wrapper = new RpRenderPipelineBuilderWrapper();
		wrapper.withName("ut1_bad");
		wrapper.withDepthTest(RpRenderPipelineBuilderWrapper.EDhDepthTest.NONE);
		wrapper.withColorWrite(true);
		wrapper.withVertexShader("test/ut1_vert");
		wrapper.withVertexFormat(RpVertexFormatUtil.createScreenPosColorFormat());
		wrapper.withVertexMode(RpRenderPipelineBuilderWrapper.EDhVertexMode.TRIANGLES);
		
		Assertions.assertThrows(IllegalStateException.class, wrapper::build);
	}
	
	@Test
	void invalidPipelineNameThrows()
	{
		RpRenderPipelineBuilderWrapper wrapper = new RpRenderPipelineBuilderWrapper();
		Assertions.assertThrows(IllegalArgumentException.class, () -> wrapper.withName("Has Upper Case"));
	}
	
	@Test
	void depthTestMappingIsExhaustive()
	{
		Assertions.assertEquals(CompareOp.ALWAYS_PASS, this.buildDepthTest(RpRenderPipelineBuilderWrapper.EDhDepthTest.NONE));
		Assertions.assertEquals(CompareOp.LESS_THAN, this.buildDepthTest(RpRenderPipelineBuilderWrapper.EDhDepthTest.LESS));
		Assertions.assertEquals(CompareOp.GREATER_THAN, this.buildDepthTest(RpRenderPipelineBuilderWrapper.EDhDepthTest.GREATER));
	}
	
	private CompareOp buildDepthTest(RpRenderPipelineBuilderWrapper.EDhDepthTest depthTest)
	{
		RpRenderPipelineBuilderWrapper wrapper = new RpRenderPipelineBuilderWrapper();
		wrapper.withName("ut1_depth");
		wrapper.withVertexShader("test/ut1_vert");
		wrapper.withFragmentShader("test/ut1_frag");
		wrapper.withDepthTest(depthTest);
		wrapper.withVertexFormat(RpVertexFormatUtil.createScreenPosColorFormat());
		wrapper.withVertexMode(RpRenderPipelineBuilderWrapper.EDhVertexMode.TRIANGLES);
		return wrapper.build().getDepthStencilState().depthTest();
	}
	
}
