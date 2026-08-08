package com.seibel.distanthorizons.common.render.renderpearl.terrain;

import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.seibel.distanthorizons.common.render.renderpearl.TestGpuDevice;
import com.seibel.distanthorizons.core.util.objects.SortedArraySet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

/** UT-2-3/2-7/2-8: opaque pipeline parameters and renderer defenses. */
class RpDhTerrainRendererTest
{
	@Test
	void opaquePipelineMatchesReverseZContract()
	{
		RenderPipeline pipeline = RpDhTerrainRenderer.buildOpaquePipeline();
		
		Assertions.assertNotNull(pipeline.getDepthStencilState());
		Assertions.assertEquals(CompareOp.GREATER_THAN, pipeline.getDepthStencilState().depthTest());
		Assertions.assertTrue(pipeline.getDepthStencilState().writeDepth());
		Assertions.assertTrue(pipeline.isCull());
		Assertions.assertEquals(PrimitiveTopology.TRIANGLES, pipeline.getPrimitiveTopology());
		Assertions.assertEquals(15, pipeline.getColorTargetStates().get(0).writeMask());
		Assertions.assertTrue(pipeline.getColorTargetStates().get(0).blendFunction().isEmpty());
		Assertions.assertEquals(RpLodVertexLayout.getFormat(), pipeline.getVertexFormatBinding(0));
		
		Map<String, UniformType> uniforms = pipeline.getBindGroupLayouts().get(0).uniforms().stream()
			.collect(Collectors.toMap(u -> u.name(), u -> u.type()));
		Assertions.assertEquals(UniformType.UNIFORM_BUFFER, uniforms.get("vertUniqueUniformBlock"));
		Assertions.assertEquals(UniformType.UNIFORM_BUFFER, uniforms.get("vertSharedUniformBlock"));
	}
	
	@Test
	void renderWithoutPassIsHarmless()
	{
		TestGpuDevice device = new TestGpuDevice();
		RpMainRenderPassHolder.clear();
		
		SortedArraySet<Object> empty = new SortedArraySet<>(Comparator.comparingInt(Object::hashCode));
		Assertions.assertDoesNotThrow(() -> RpDhTerrainRenderer.INSTANCE.render(null, true, (SortedArraySet) empty, null));
		Assertions.assertEquals(0, device.drawCount);
	}
	
	@Test
	void renderWithFakePassAndNoContainersDoesNotDraw()
	{
		TestGpuDevice device = new TestGpuDevice();
		RpMainRenderPassHolder.setCurrent(new TestGpuDevice.TestRenderPass(device));
		try
		{
			SortedArraySet<Object> empty = new SortedArraySet<>(Comparator.comparingInt(Object::hashCode));
			// pipeline init will fail without a real device but must not throw
			Assertions.assertDoesNotThrow(() -> RpDhTerrainRenderer.INSTANCE.render(null, true, (SortedArraySet) empty, null));
			Assertions.assertEquals(0, device.drawCount);
		}
		finally
		{
			RpMainRenderPassHolder.clear();
		}
	}
	
}
