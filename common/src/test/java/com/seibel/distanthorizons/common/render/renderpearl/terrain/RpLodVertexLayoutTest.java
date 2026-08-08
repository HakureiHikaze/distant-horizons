package com.seibel.distanthorizons.common.render.renderpearl.terrain;

import com.mojang.renderpearl.api.vertex.VertexFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/** UT-2-2: CPU mesh vertex bytes map 1:1 onto the renderpearl LOD layout. */
class RpLodVertexLayoutTest
{
	@Test
	void byteMappingMatchesCoreMeshLayout()
	{
		// 16-byte vertex, written in LodQuadBuilder.writeQuadToBuffer order
		ByteBuffer vertex = ByteBuffer.allocate(16).order(ByteOrder.nativeOrder());
		vertex.putShort((short) 10);
		vertex.putShort((short) 20);
		vertex.putShort((short) 30);
		vertex.putShort((short) 0x1234); // meta
		vertex.put((byte) 1); // r
		vertex.put((byte) 2); // g
		vertex.put((byte) 3); // b
		vertex.put((byte) 4); // a
		vertex.put((byte) 5); // irisBlockMaterialId
		vertex.put((byte) 6); // normalIndex
		vertex.putShort((short) 7); // textureTileId
		vertex.rewind();
		
		ShortBuffer shorts = vertex.asShortBuffer();
		
		Assertions.assertEquals(10, shorts.get(0));
		Assertions.assertEquals(20, shorts.get(1));
		Assertions.assertEquals(30, shorts.get(2));
		Assertions.assertEquals(0x1234, shorts.get(3) & 0xFFFF);
		Assertions.assertEquals(1, vertex.get(8) & 0xFF);
		Assertions.assertEquals(2, vertex.get(9) & 0xFF);
		Assertions.assertEquals(3, vertex.get(10) & 0xFF);
		Assertions.assertEquals(4, vertex.get(11) & 0xFF);
		Assertions.assertEquals(5, vertex.get(12) & 0xFF);
		Assertions.assertEquals(6, vertex.get(13) & 0xFF);
		Assertions.assertEquals(7, shorts.get(7) & 0xFFFF);
		
		// the renderpearl layout must point at the same byte offsets
		VertexFormat format = RpLodVertexLayout.getFormat();
		Assertions.assertEquals(0, format.getElement(RpLodVertexLayout.POSITION_ATTRIBUTE).offset());
		Assertions.assertEquals(6, format.getElement(RpLodVertexLayout.META_ATTRIBUTE).offset());
		Assertions.assertEquals(8, format.getElement(RpLodVertexLayout.COLOR_ATTRIBUTE).offset());
		Assertions.assertEquals(12, format.getElement(RpLodVertexLayout.IRIS_MATERIAL_ATTRIBUTE).offset());
		Assertions.assertEquals(13, format.getElement(RpLodVertexLayout.IRIS_NORMAL_ATTRIBUTE).offset());
		Assertions.assertEquals(14, format.getElement(RpLodVertexLayout.TEXTURE_TILE_ATTRIBUTE).offset());
		Assertions.assertEquals(16, format.getVertexSize());
	}
	
}
