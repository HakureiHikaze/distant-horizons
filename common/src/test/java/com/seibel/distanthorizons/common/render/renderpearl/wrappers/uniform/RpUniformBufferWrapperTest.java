package com.seibel.distanthorizons.common.render.renderpearl.wrappers.uniform;

import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.seibel.distanthorizons.api.objects.math.DhApiMat4f;
import com.seibel.distanthorizons.common.render.renderpearl.TestGpuDevice;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** UT-4: Std140 layout matches Minecraft's size calculator; uploads reuse the GPU buffer. */
class RpUniformBufferWrapperTest
{
	@Test
	void std140LayoutMatchesMinecraftCalculator()
	{
		TestGpuDevice device = new TestGpuDevice();
		RpUniformBufferWrapper wrapper = new RpUniformBufferWrapper("test", device);
		
		wrapper.putVec3f(1.0f, 2.0f, 3.0f);
		wrapper.putMat4f(new DhApiMat4f());
		wrapper.putFloat(1.5f);
		
		int expected = new Std140SizeCalculator()
			.putVec3()
			.putMat4f()
			.putFloat()
			.get();
		
		Assertions.assertEquals(expected, wrapper.getBufferSize());
	}
	
	@Test
	void uploadReusesGpuBufferAndWritesData()
	{
		TestGpuDevice device = new TestGpuDevice();
		RpUniformBufferWrapper wrapper = new RpUniformBufferWrapper("test", device);
		
		wrapper.putVec2f(1.0f, 2.0f);
		wrapper.putFloat(3.0f);
		wrapper.finishAndUpload();
		
		Assertions.assertEquals(1, device.createBufferCount);
		Assertions.assertEquals(1, device.writeToBufferCount);
		Assertions.assertTrue(device.createdBuffers.get(0).size() >= wrapper.getBufferSize());
		
		wrapper.finishAndUpload();
		Assertions.assertEquals(1, device.createBufferCount, "buffer must be reused");
		Assertions.assertEquals(2, device.writeToBufferCount, "data must be re-uploaded");
	}
	
	@Test
	void emptyBufferDoesNotCreateGpuResources()
	{
		TestGpuDevice device = new TestGpuDevice();
		RpUniformBufferWrapper wrapper = new RpUniformBufferWrapper("test", device);
		
		wrapper.finishAndUpload();
		Assertions.assertEquals(0, device.createBufferCount);
		Assertions.assertEquals(0, device.writeToBufferCount);
	}
	
}
