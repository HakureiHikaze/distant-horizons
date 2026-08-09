package com.seibel.distanthorizons.common.render.renderpearl.wrappers.buffer;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.seibel.distanthorizons.common.render.renderpearl.RpDhRenderApiDefinition;
import com.seibel.distanthorizons.common.render.renderpearl.TestGpuDevice;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodQuadBuilder;
import com.seibel.distanthorizons.core.dependencyInjection.SingletonInjector;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.AbstractDhRenderApiDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** UT-2-4 + UT-2-6: wrapper lifecycle and global IBO behaviour. */
class RpVertexBufferWrapperTest
{
	@AfterEach
	void clearInjector()
	{
		SingletonInjector.INSTANCE.clear();
	}
	
	/**
	 * UT-2-4: chunk load -> upload; same-size re-upload must reuse the GPU
	 * buffer (no duplicate allocation); resize must recreate and close the old
	 * buffer; close must release GPU resources.
	 */
	@Test
	void uploadLifecycleReusesBuffersAndCloseReleases()
	{
		TestGpuDevice device = new TestGpuDevice();
		RpVertexBufferWrapper wrapper = new RpVertexBufferWrapper("test", device);
		
		wrapper.uploadVertexBuffer(createVertexData(4), 4);
		Assertions.assertEquals(1, device.createBufferCount);
		Assertions.assertTrue(wrapper.isUploaded());
		GpuBuffer firstBuffer = device.createdBuffers.get(0);
		
		// same vertex count -> reuse, no new allocation
		wrapper.uploadVertexBuffer(createVertexData(4), 4);
		Assertions.assertEquals(1, device.createBufferCount, "same-size re-upload must reuse the GPU buffer");
		
		// size change -> recreate, old buffer closed
		wrapper.uploadVertexBuffer(createVertexData(8), 8);
		Assertions.assertEquals(2, device.createBufferCount);
		Assertions.assertTrue(firstBuffer.isClosed(), "old buffer must be closed on resize");
		
		// unload/close -> GPU resources released
		GpuBuffer secondBuffer = device.createdBuffers.get(1);
		wrapper.close();
		Assertions.assertTrue(secondBuffer.isClosed());
	}
	
	/**
	 * UT-2-6: the global IBO covers the maximum quad capacity with int indices
	 * (6 indices per quad, 4 bytes each) and is created exactly once.
	 */
	@Test
	void globalIndexBufferMatchesQuadCapacity()
	{
		TestGpuDevice device = new TestGpuDevice();
		
		GpuBuffer ibo = RpVertexBufferWrapper.createGlobalIndexBuffer(device);
		
		int maxSize = LodQuadBuilder.getMaxBufferByteSize();
		int maxQuadCount = (maxSize / LodQuadBuilder.BYTES_PER_VERTEX) / 4;
		int expectedBytes = maxQuadCount * 6 * Integer.BYTES;
		Assertions.assertEquals(expectedBytes, ibo.size());
		Assertions.assertTrue((ibo.usage() & GpuBuffer.USAGE_INDEX) != 0);
		Assertions.assertEquals(1, device.createBufferCount);
		Assertions.assertEquals(1, device.writeToBufferCount);
	}
	
	/** UT-2-6: single-IBO branch follows the bound render definition. */
	@Test
	void singleIboFlagFollowsRenderDefinition()
	{
		Assertions.assertFalse(RpVertexBufferWrapper.isSingleIbo(), "unbound injector -> per-wrapper IBO");
		
		AbstractDhRenderApiDefinition definition = new RpDhRenderApiDefinition();
		SingletonInjector.INSTANCE.bind(AbstractDhRenderApiDefinition.class, definition);
		Assertions.assertTrue(RpVertexBufferWrapper.isSingleIbo(), "bound RENDERPEARL (non-Mac) -> global IBO");
	}
	
	private static ByteBuffer createVertexData(int vertexCount)
	{
		ByteBuffer data = ByteBuffer.allocateDirect(vertexCount * LodQuadBuilder.BYTES_PER_VERTEX)
			.order(ByteOrder.nativeOrder());
		for (int i = 0; i < vertexCount * (LodQuadBuilder.BYTES_PER_VERTEX / Integer.BYTES); i++)
		{
			data.putInt(i);
		}
		data.rewind();
		return data;
	}
}
