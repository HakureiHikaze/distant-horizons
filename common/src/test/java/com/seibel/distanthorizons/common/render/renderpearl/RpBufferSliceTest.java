package com.seibel.distanthorizons.common.render.renderpearl;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** UT-3: GpuBufferSlice offset/length/alignment semantics and bounds checks. */
class RpBufferSliceTest
{
	@Test
	void validSlices()
	{
		GpuBuffer buffer = new TestGpuDevice.TestGpuBuffer(100, 0);
		
		GpuBufferSlice full = buffer.slice();
		Assertions.assertEquals(0, full.offset());
		Assertions.assertEquals(100, full.length());
		
		GpuBufferSlice partial = buffer.slice(10, 50);
		Assertions.assertEquals(10, partial.offset());
		Assertions.assertEquals(50, partial.length());
		
		// record method just re-wraps the same buffer
		GpuBufferSlice sub = new GpuBufferSlice(buffer, 0, 100).slice(10, 20);
		Assertions.assertEquals(10, sub.offset());
		Assertions.assertEquals(20, sub.length());
		Assertions.assertSame(buffer, sub.buffer());
	}
	
	@Test
	void invalidSlicesThrow()
	{
		GpuBuffer buffer = new TestGpuDevice.TestGpuBuffer(100, 0);
		
		Assertions.assertThrows(IllegalArgumentException.class, () -> buffer.slice(0, 101));
		Assertions.assertThrows(IllegalArgumentException.class, () -> buffer.slice(-1, 10));
		Assertions.assertThrows(IllegalArgumentException.class, () -> buffer.slice(10, -1));
		Assertions.assertThrows(IllegalArgumentException.class, () -> buffer.slice(95, 10));
	}
	
}
