package com.seibel.distanthorizons.common.render.renderpearl.terrain;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.CameraRenderState;
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
