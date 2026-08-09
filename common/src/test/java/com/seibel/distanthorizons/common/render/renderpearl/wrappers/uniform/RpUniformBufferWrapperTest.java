package com.seibel.distanthorizons.common.render.renderpearl.wrappers.uniform;

import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.seibel.distanthorizons.api.objects.math.DhApiMat4f;
import com.seibel.distanthorizons.common.render.renderpearl.TestGpuDevice;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
		
		// audit F1: a second frame must re-put from offset 0 before uploading again
		wrapper.putVec2f(4.0f, 5.0f);
		wrapper.putFloat(6.0f);
		wrapper.finishAndUpload();
		Assertions.assertEquals(1, device.createBufferCount, "buffer must be reused");
		Assertions.assertEquals(2, device.writeToBufferCount, "data must be re-uploaded");
		Assertions.assertEquals(0, wrapper.getBufferSize(), "write cursor must reset after upload (audit F1)");
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
	
	@Test
	void bufferGrowthBeyondInitialCapacity()
	{
		// audit F2: the CPU buffer must grow beyond the initial 256 bytes
		TestGpuDevice device = new TestGpuDevice();
		RpUniformBufferWrapper wrapper = new RpUniformBufferWrapper("test", device);
		
		for (int i = 0; i < 20; i++)
		{
			wrapper.putMat4f(new DhApiMat4f());
		}
		
		Assertions.assertEquals(20 * 64, wrapper.getBufferSize());
		wrapper.finishAndUpload();
		Assertions.assertEquals(1, device.createBufferCount);
		Assertions.assertTrue(device.createdBuffers.get(0).size() >= wrapper.getBufferSize());
		Assertions.assertEquals(1, device.writeToBufferCount);
	}
	
	@Test
	void mappedUploadDoesNotUseCommandEncoder()
	{
		TestGpuDevice device = new TestGpuDevice();
		RpUniformBufferWrapper wrapper = new RpUniformBufferWrapper("test", device);
		
		wrapper.putVec3f(1.0f, 2.0f, 3.0f);
		wrapper.putMat4f(new DhApiMat4f());
		wrapper.uploadMapped();
		
		Assertions.assertEquals(1, device.createBufferCount);
		Assertions.assertEquals(0, device.writeToBufferCount, "mapped upload must not use the command encoder");
		Assertions.assertTrue((device.createdBuffers.get(0).usage() & com.mojang.renderpearl.api.buffers.GpuBuffer.USAGE_MAP_WRITE) != 0);
		
		// audit F1: re-put from offset 0 before the second upload
		wrapper.putVec3f(7.0f, 8.0f, 9.0f);
		wrapper.putMat4f(new DhApiMat4f());
		wrapper.uploadMapped();
		Assertions.assertEquals(1, device.createBufferCount, "buffer must be reused");
		Assertions.assertEquals(0, device.writeToBufferCount);
		Assertions.assertEquals(0, wrapper.getBufferSize(), "write cursor must reset after upload (audit F1)");
	}
	
	/**
	 * Audit F1 (P0) regression: two frames of put+upload must overwrite from
	 * offset 0. The shader reads the block start every frame, so the second
	 * frame's data must be at the front, the GPU buffer must be reused (no
	 * per-frame recreation), and the CPU write cursor must not grow.
	 */
	@Test
	void repeatedPutAndUploadOverwritesFromOffsetZero()
	{
		TestGpuDevice device = new TestGpuDevice();
		RpUniformBufferWrapper wrapper = new RpUniformBufferWrapper("shared", device);
		
		// frame 1
		wrapper.putVec3f(1.0f, 2.0f, 3.0f);
		wrapper.putMat4f(new DhApiMat4f());
		wrapper.uploadMapped();
		Assertions.assertEquals(1, device.createBufferCount);
		Assertions.assertEquals(0, wrapper.getBufferSize());
		
		// frame 2
		wrapper.putVec3f(9.0f, 8.0f, 7.0f);
		wrapper.putMat4f(new DhApiMat4f());
		wrapper.uploadMapped();
		
		Assertions.assertEquals(1, device.createBufferCount, "GPU buffer must be reused across frames (audit F1)");
		Assertions.assertEquals(0, wrapper.getBufferSize(), "write cursor must not grow across frames");
		
		// the shader reads offset 0..12: must contain frame 2's vec3, not frame 1's
		try (GpuBufferSlice.MappedView view = device.createdBuffers.get(0).map(0, 12, true, false))
		{
			// the GPU buffer is written in native order; read it back the same way
			ByteBuffer data = view.data().order(ByteOrder.nativeOrder());
			Assertions.assertEquals(9.0f, data.getFloat(0), 0.001f);
			Assertions.assertEquals(8.0f, data.getFloat(4), 0.001f);
			Assertions.assertEquals(7.0f, data.getFloat(8), 0.001f);
		}
	}
	
	/**
	 * Audit F8: the shared uniform block written by {@code RpDhTerrainRenderer}
	 * must match the Std140 layout of the terrain shader's
	 * vertSharedUniformBlock (bool + 3 floats + vec3 + mat4 = 96 bytes).
	 */
	@Test
	void sharedUniformBlockLayoutMatchesShaderContract()
	{
		TestGpuDevice device = new TestGpuDevice();
		RpUniformBufferWrapper wrapper = new RpUniformBufferWrapper("vertSharedUniformBlock", device);
		
		wrapper.putInt(0)         // uIsWhiteWorld (bool)
			.putFloat(0.0f)       // uWorldYOffset
			.putFloat(0.01f)      // uMicroOffset
			.putFloat(0.0f)       // uEarthRadius
			.putVec3f(1.0f, 2.0f, 3.0f) // uCameraPos
			.putMat4f(new DhApiMat4f()); // uCombinedMatrix
		
		int expected = new Std140SizeCalculator()
			.putInt()
			.putFloat()
			.putFloat()
			.putFloat()
			.putVec3()
			.putMat4f()
			.get();
		
		Assertions.assertEquals(96, expected);
		Assertions.assertEquals(expected, wrapper.getBufferSize());
		
		// unique block: vec3 + trailing padding = 16 bytes in Std140
		RpUniformBufferWrapper unique = new RpUniformBufferWrapper("vertUniqueUniformBlock", device);
		unique.putVec3f(1.0f, 2.0f, 3.0f);
		unique.putFloat(0.0f);
		Assertions.assertEquals(16, unique.getBufferSize());
	}
	
}
