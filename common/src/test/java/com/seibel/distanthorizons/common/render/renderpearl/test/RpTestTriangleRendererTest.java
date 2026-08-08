package com.seibel.distanthorizons.common.render.renderpearl.test;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.seibel.distanthorizons.common.render.renderpearl.TestGpuDevice;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** UT-6: the experiment entry is disabled by default (zero GPU work) and draws once enabled. */
class RpTestTriangleRendererTest
{
	@Test
	void disabledByDefaultCreatesNoGpuResources()
	{
		TestGpuDevice device = new TestGpuDevice();
		RpTestTriangleRenderer renderer = new RpTestTriangleRenderer(device, false);
		
		renderer.renderToTargets(this.fakeView(), this.fakeView());
		
		Assertions.assertFalse(renderer.isEnabled());
		Assertions.assertEquals(0, device.createBufferCount);
		Assertions.assertEquals(0, device.compilePipelineCount);
		Assertions.assertEquals(0, device.createRenderPassCount);
		Assertions.assertEquals(0, device.drawCount);
	}
	
	@Test
	void enabledInitializesOnceAndDrawsPerFrame()
	{
		TestGpuDevice device = new TestGpuDevice();
		RpTestTriangleRenderer renderer = new RpTestTriangleRenderer(device, true);
		
		GpuTextureView color = this.fakeView();
		GpuTextureView depth = this.fakeView();
		
		renderer.renderToTargets(color, depth);
		Assertions.assertEquals(1, device.compilePipelineCount);
		Assertions.assertEquals(1, device.createBufferCount);
		Assertions.assertEquals(1, device.createRenderPassCount);
		Assertions.assertEquals(1, device.drawCount);
		
		// second frame: pipeline and VBO reused
		renderer.renderToTargets(color, depth);
		Assertions.assertEquals(1, device.compilePipelineCount);
		Assertions.assertEquals(1, device.createBufferCount);
		Assertions.assertEquals(2, device.drawCount);
	}
	
	private GpuTextureView fakeView()
	{
		GpuTexture texture = new TestGpuDevice.TestGpuTexture(GpuFormat.RGBA8_UNORM, 0, 16, 16, 1, 1, "fake");
		return new TestGpuDevice.TestGpuTextureView(texture);
	}
	
}
