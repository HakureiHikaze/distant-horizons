package com.seibel.distanthorizons.common.render.renderpearl;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** UT-2: LOD vertex layout is locked (order/offset/format) and errors are explicit. */
class RpVertexFormatUtilTest
{
	@Test
	void lodVertexFormatOrderAndOffsets()
	{
		VertexFormat format = RpVertexFormatUtil.createLodVertexFormat();
		
		List<String> names = format.getElements().stream()
			.map(e -> e.name())
			.collect(Collectors.toList());
		Assertions.assertEquals(List.of(
			RpVertexFormatUtil.POSITION_ATTRIBUTE,
			RpVertexFormatUtil.META_ATTRIBUTE,
			RpVertexFormatUtil.COLOR_ATTRIBUTE,
			RpVertexFormatUtil.IRIS_MATERIAL_ATTRIBUTE,
			RpVertexFormatUtil.IRIS_NORMAL_ATTRIBUTE,
			RpVertexFormatUtil.TEXTURE_TILE_ATTRIBUTE), names);
		
		Assertions.assertEquals(0, format.getElement(RpVertexFormatUtil.POSITION_ATTRIBUTE).offset());
		Assertions.assertEquals(6, format.getElement(RpVertexFormatUtil.META_ATTRIBUTE).offset());
		Assertions.assertEquals(8, format.getElement(RpVertexFormatUtil.COLOR_ATTRIBUTE).offset());
		Assertions.assertEquals(12, format.getElement(RpVertexFormatUtil.IRIS_MATERIAL_ATTRIBUTE).offset());
		Assertions.assertEquals(13, format.getElement(RpVertexFormatUtil.IRIS_NORMAL_ATTRIBUTE).offset());
		Assertions.assertEquals(14, format.getElement(RpVertexFormatUtil.TEXTURE_TILE_ATTRIBUTE).offset());
		
		Assertions.assertEquals(16, format.getVertexSize());
		Assertions.assertEquals(GpuFormat.RGB16_UINT, format.getElement(RpVertexFormatUtil.POSITION_ATTRIBUTE).format());
		Assertions.assertEquals(GpuFormat.R16_UINT, format.getElement(RpVertexFormatUtil.META_ATTRIBUTE).format());
		Assertions.assertEquals(GpuFormat.RGBA8_UNORM, format.getElement(RpVertexFormatUtil.COLOR_ATTRIBUTE).format());
		Assertions.assertEquals(GpuFormat.R8_UINT, format.getElement(RpVertexFormatUtil.IRIS_MATERIAL_ATTRIBUTE).format());
		Assertions.assertEquals(GpuFormat.R8_UINT, format.getElement(RpVertexFormatUtil.IRIS_NORMAL_ATTRIBUTE).format());
		Assertions.assertEquals(GpuFormat.R16_UINT, format.getElement(RpVertexFormatUtil.TEXTURE_TILE_ATTRIBUTE).format());
	}
	
	@Test
	void screenPosColorFormat()
	{
		VertexFormat format = RpVertexFormatUtil.createScreenPosColorFormat();
		
		Assertions.assertEquals(0, format.getElement(RpVertexFormatUtil.POSITION_ATTRIBUTE).offset());
		Assertions.assertEquals(8, format.getElement(RpVertexFormatUtil.COLOR_ATTRIBUTE).offset());
		Assertions.assertEquals(24, format.getVertexSize());
		Assertions.assertEquals(GpuFormat.RG32_FLOAT, format.getElement(RpVertexFormatUtil.POSITION_ATTRIBUTE).format());
		Assertions.assertEquals(GpuFormat.RGBA32_FLOAT, format.getElement(RpVertexFormatUtil.COLOR_ATTRIBUTE).format());
	}
	
	@Test
	void tooManyAttributesThrows()
	{
		VertexFormat.Builder builder = VertexFormat.builder(0);
		Assertions.assertThrows(IllegalArgumentException.class, () ->
			IntStream.range(0, 17).forEach(i -> builder.addAttribute("attr_" + i, GpuFormat.R8_UINT)));
	}
	
}
