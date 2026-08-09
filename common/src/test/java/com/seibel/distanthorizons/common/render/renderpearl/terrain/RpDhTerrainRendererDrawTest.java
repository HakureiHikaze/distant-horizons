package com.seibel.distanthorizons.common.render.renderpearl.terrain;

import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.ShaderSource;
import com.seibel.distanthorizons.common.render.renderpearl.TestGpuDevice;
import com.seibel.distanthorizons.common.render.renderpearl.wrappers.buffer.RpVertexBufferWrapper;
import com.seibel.distanthorizons.common.render.renderpearl.wrappers.uniform.RpLodUniformBufferWrapper;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodBufferContainer;
import com.seibel.distanthorizons.core.dependencyInjection.SingletonInjector;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.objects.ILodContainerUniformBufferWrapper;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.objects.IVertexBufferWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Audit F3/F4/F6/F7: draw-loop defences, init retry, mixed-state rejection and
 * real draw counts with an injected fake device/pass.
 */
class RpDhTerrainRendererDrawTest
{
	private final TestGpuDevice device = new TestGpuDevice();
	
	@AfterEach
	void resetRenderer()
	{
		RpDhTerrainRenderer.setTestDevice(null);
		RpDhTerrainRenderer.INSTANCE.close();
		RpMainRenderPassHolder.clear();
		SingletonInjector.INSTANCE.clear();
	}
	
	/** F7: uploaded wrapper with an index buffer actually issues an indexed draw. */
	@Test
	void drawBufferWrappersIssuesIndexedDraws()
	{
		RpVertexBufferWrapper vbo = new RpVertexBufferWrapper("lod", device);
		vbo.uploadVertexBuffer(createVertexData(4), 4);
		vbo.uploadIndexBuffer(createIndexData(4), 4);
		
		TestGpuDevice.TestRenderPass pass = new TestGpuDevice.TestRenderPass(device);
		RpDhTerrainRenderer.INSTANCE.drawBufferWrappers(pass, new IVertexBufferWrapper[] { vbo });
		
		Assertions.assertEquals(1, device.setIndexBufferCount);
		Assertions.assertEquals(1, device.drawIndexedCount);
	}
	
	/** F3: no usable index buffer -> the wrapper is skipped, no draw is issued. */
	@Test
	void drawBufferWrappersSkipsWrapperWithoutIndexBuffer()
	{
		RpVertexBufferWrapper vbo = new RpVertexBufferWrapper("lod", device);
		vbo.uploadVertexBuffer(createVertexData(4), 4);
		
		TestGpuDevice.TestRenderPass pass = new TestGpuDevice.TestRenderPass(device);
		RpDhTerrainRenderer.INSTANCE.drawBufferWrappers(pass, new IVertexBufferWrapper[] { vbo });
		
		Assertions.assertEquals(0, device.setIndexBufferCount);
		Assertions.assertEquals(0, device.drawIndexedCount);
	}
	
	/** F4: a failed init is retried (rate-limited) instead of disabling rendering forever. */
	@Test
	void failedInitIsRetriedAfterBackoff() throws Exception
	{
		FailingDevice failingDevice = new FailingDevice();
		RpDhTerrainRenderer.setTestDevice(failingDevice);
		
		RpDhTerrainRenderer.INSTANCE.prepareFrame();
		Assertions.assertEquals(1, failingDevice.compilePipelineCount);
		Assertions.assertFalse(isInitialized());
		
		// immediate retry inside the backoff window must be skipped
		RpDhTerrainRenderer.INSTANCE.prepareFrame();
		Assertions.assertEquals(1, failingDevice.compilePipelineCount, "retry must be rate-limited");
		
		// backoff window elapses, a healthy device is available -> init succeeds
		setLastInitAttemptNano(0);
		RpDhTerrainRenderer.setTestDevice(device);
		RpDhTerrainRenderer.INSTANCE.prepareFrame();
		Assertions.assertEquals(1, device.compilePipelineCount);
		Assertions.assertTrue(isInitialized());
	}
	
	/** F6: only RENDERPEARL uniform containers may be drawn. */
	@Test
	void mixedUniformContainerIsRejected()
	{
		Assertions.assertFalse(RpDhTerrainRenderer.isCompatibleUniformContainer(null));
		Assertions.assertFalse(RpDhTerrainRenderer.isCompatibleUniformContainer(new StubLodContainerUniform()));
		Assertions.assertTrue(RpDhTerrainRenderer.isCompatibleUniformContainer(new RpLodUniformBufferWrapper("test", device)));
	}
	
	
	private static boolean isInitialized() throws Exception
	{
		Field field = RpDhTerrainRenderer.class.getDeclaredField("init");
		field.setAccessible(true);
		return field.getBoolean(RpDhTerrainRenderer.INSTANCE);
	}
	
	private static void setLastInitAttemptNano(long value) throws Exception
	{
		Field field = RpDhTerrainRenderer.class.getDeclaredField("lastInitAttemptNano");
		field.setAccessible(true);
		field.setLong(RpDhTerrainRenderer.INSTANCE, value);
	}
	
	private static ByteBuffer createVertexData(int vertexCount)
	{
		ByteBuffer data = ByteBuffer.allocateDirect(vertexCount * 16).order(ByteOrder.nativeOrder());
		for (int i = 0; i < vertexCount * 4; i++)
		{
			data.putInt(i);
		}
		data.rewind();
		return data;
	}
	
	private static ByteBuffer createIndexData(int vertexCount)
	{
		ByteBuffer data = ByteBuffer.allocateDirect((int) (vertexCount * 1.5) * Integer.BYTES)
			.order(ByteOrder.nativeOrder());
		for (int i = 0; i < vertexCount * 1.5; i++)
		{
			data.putInt(i);
		}
		data.rewind();
		return data;
	}
	
	private static class FailingDevice extends TestGpuDevice
	{
		@Override
		public CompiledRenderPipeline compilePipeline(RenderPipeline pipeline, ShaderSource shaderSource)
		{
			this.compilePipelineCount++;
			throw new RuntimeException("simulated pipeline compile failure");
		}
	}
	
	private static class StubLodContainerUniform implements ILodContainerUniformBufferWrapper
	{
		@Override public void tryUpload(LodBufferContainer bufferContainer) { }
		@Override public void close() { }
	}
}
