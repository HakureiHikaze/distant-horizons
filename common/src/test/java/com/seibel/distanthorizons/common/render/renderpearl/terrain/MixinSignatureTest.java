package com.seibel.distanthorizons.common.render.renderpearl.terrain;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.Options;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import org.joml.Vector4f;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * UT-2-5: compile-time lock on the 26.3-snapshot-7 mixin targets. If the
 * snapshot changes either signature, this file stops compiling (the same drift
 * protection the mixins themselves rely on).
 */
class MixinSignatureTest
{
	@Test
	void mixinTargetSignaturesResolve()
	{
		Assertions.assertNotNull(ChunkSectionsToRender.class);
		Assertions.assertNotNull(LevelRenderer.class);
		Assertions.assertNotNull(OptionsScreen.class);
	}
	
	/** UT-3-5: the stage 3 entry mixin target keeps its layout contract. */
	@Test
	void optionsScreenLayoutContractHolds() throws Exception
	{
		java.lang.reflect.Constructor<OptionsScreen> constructor =
			OptionsScreen.class.getConstructor(Screen.class, Options.class);
		Assertions.assertNotNull(constructor);
		
		java.lang.reflect.Field layoutField = OptionsScreen.class.getDeclaredField("layout");
		layoutField.setAccessible(true);
		Assertions.assertEquals(HeaderAndFooterLayout.class, layoutField.getType());
	}
	
	/** stage 3: the vanilla-fog mixin target keeps its field/method contract. */
	@Test
	void fogRendererContractHolds() throws Exception
	{
		Assertions.assertNotNull(FogRenderer.class.getMethod("getBuffer", FogRenderer.FogMode.class));
		Assertions.assertNotNull(FogRenderer.class.getDeclaredField("fogEnabled"));
	}
	
	/**
	 * Never invoked; the compiler still type-checks these exact signatures,
	 * mirroring the 26.3 mixin injector handlers.
	 */
	@SuppressWarnings("unused")
	private void lockMixinSignatures(
		ChunkSectionsToRender sections,
		ChunkSectionLayerGroup group,
		RenderPass renderPass,
		GpuSampler sampler,
		GpuTextureView atlas,
		LevelRenderer levelRenderer,
		GraphicsResourceAllocator resourceAllocator,
		boolean renderOutline,
		CameraRenderState cameraState,
		GpuBufferSlice terrainFog,
		Vector4f fogColor,
		boolean shouldRenderSky,
		boolean consistentDepthRequired)
	{
		sections.renderGroup(group, renderPass, sampler, atlas, false);
		levelRenderer.render(resourceAllocator, renderOutline, cameraState, terrainFog, fogColor, shouldRenderSky, consistentDepthRequired);
	}
}
