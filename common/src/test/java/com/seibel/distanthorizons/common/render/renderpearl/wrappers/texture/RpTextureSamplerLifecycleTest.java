package com.seibel.distanthorizons.common.render.renderpearl.wrappers.texture;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.seibel.distanthorizons.common.render.renderpearl.TestGpuDevice;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.OptionalDouble;

/** UT-5: texture/view/sampler lifecycle; re-creation does not leak resources. */
class RpTextureSamplerLifecycleTest
{
	private static final int TEXTURE_USAGE = GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING;
	
	@Test
	void textureCreateResizeClose()
	{
		TestGpuDevice device = new TestGpuDevice();
		RpTextureWrapper texture = new RpTextureWrapper("test", GpuFormat.RGBA8_UNORM, TEXTURE_USAGE, FilterMode.LINEAR, 1, 1, device);
		
		Assertions.assertTrue(texture.tryCreateOrResize(64, 64));
		Assertions.assertEquals(1, device.createTextureCount);
		Assertions.assertEquals(1, device.createTextureViewCount);
		Assertions.assertEquals(1, device.createSamplerCount);
		Assertions.assertEquals(64, texture.getWidth());
		Assertions.assertEquals(64, texture.getHeight());
		
		// same size: no re-creation
		Assertions.assertFalse(texture.tryCreateOrResize(64, 64));
		Assertions.assertEquals(1, device.createTextureCount);
		
		// resize: old texture closed, new one created
		Assertions.assertTrue(texture.tryCreateOrResize(128, 128));
		Assertions.assertEquals(2, device.createTextureCount);
		Assertions.assertTrue(device.createdTextures.get(0).isClosed());
		Assertions.assertFalse(device.createdTextures.get(1).isClosed());
		
		GpuTexture lastTexture = texture.getTexture();
		texture.close();
		Assertions.assertTrue(lastTexture.isClosed());
	}
	
	@Test
	void textureViewWrapRecreateClose()
	{
		TestGpuDevice device = new TestGpuDevice();
		RpTextureViewWrapper view = new RpTextureViewWrapper(device);
		
		GpuTexture first = new TestGpuDevice.TestGpuTexture(GpuFormat.RGBA8_UNORM, 0, 16, 16, 1, 1, "first");
		GpuTexture second = new TestGpuDevice.TestGpuTexture(GpuFormat.RGBA8_UNORM, 0, 16, 16, 1, 1, "second");
		
		view.tryWrap(first);
		Assertions.assertEquals(1, device.createTextureViewCount);
		Assertions.assertEquals(1, device.createSamplerCount);
		GpuTextureView oldView = view.getTextureView();
		
		// same texture: no re-creation
		view.tryWrap(first);
		Assertions.assertEquals(1, device.createTextureViewCount);
		
		// different texture: view re-created, old one closed
		view.tryWrap(second);
		Assertions.assertEquals(2, device.createTextureViewCount);
		Assertions.assertTrue(oldView.isClosed());
		Assertions.assertSame(second, view.getTextureView().texture());
		
		GpuTextureView lastView = view.getTextureView();
		view.close();
		Assertions.assertTrue(lastView.isClosed());
	}
	
	@Test
	void samplerIsCreatedOnce()
	{
		TestGpuDevice device = new TestGpuDevice();
		RpSamplerWrapper sampler = new RpSamplerWrapper("test", device);
		
		sampler.getOrCreateSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, 1, OptionalDouble.empty());
		sampler.getOrCreateSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, 1, OptionalDouble.empty());
		
		Assertions.assertEquals(1, device.createSamplerCount);
		var lastSampler = sampler.getSampler();
		sampler.close();
		Assertions.assertTrue(lastSampler.isClosed());
	}
	
}
