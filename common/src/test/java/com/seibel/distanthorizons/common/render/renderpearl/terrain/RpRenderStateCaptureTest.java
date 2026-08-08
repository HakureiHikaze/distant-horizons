package com.seibel.distanthorizons.common.render.renderpearl.terrain;

import com.seibel.distanthorizons.common.wrappers.McObjectConverter;
import com.seibel.distanthorizons.core.api.internal.ClientApi;
import org.joml.Matrix4f;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** UT-2-1: 26.3 camera state is captured into DH's render state. */
class RpRenderStateCaptureTest
{
	@Test
	void captureFillsRenderState()
	{
		Matrix4f projection = new Matrix4f().perspective(1.2f, 1.0f, 0.1f, 1000.0f);
		Matrix4f modelView = new Matrix4f().translation(10.0f, 20.0f, 30.0f);
		
		RpRenderStateCapture.capture(projection, modelView, 0.5f, null);
		
		Assertions.assertArrayEquals(
			McObjectConverter.convert(projection).getValuesAsArray(),
			ClientApi.RENDER_STATE.mcProjectionMatrix.getValuesAsArray(), 0.0001f);
		Assertions.assertArrayEquals(
			McObjectConverter.convert(modelView).getValuesAsArray(),
			ClientApi.RENDER_STATE.mcModelViewMatrix.getValuesAsArray(), 0.0001f);
		Assertions.assertEquals(0.5f, ClientApi.RENDER_STATE.partialTickTime);
		Assertions.assertNull(ClientApi.RENDER_STATE.clientLevelWrapper);
		Assertions.assertTrue(ClientApi.RENDER_STATE.vanillaFogEnabled);
	}
	
}
