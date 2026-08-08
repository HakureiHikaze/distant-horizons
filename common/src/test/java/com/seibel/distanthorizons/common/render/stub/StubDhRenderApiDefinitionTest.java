package com.seibel.distanthorizons.common.render.stub;

import com.seibel.distanthorizons.api.enums.config.EDhApiRenderingEngine;
import com.seibel.distanthorizons.core.render.EDhRenderDepth;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Stage 0: the Stub (no-op) engine must not throw, must not create any GPU
 * resources and must report itself as the STUB engine.
 */
class StubDhRenderApiDefinitionTest
{
	@Test
	void engineIdentity()
	{
		StubDhRenderApiDefinition stub = new StubDhRenderApiDefinition();
		Assertions.assertEquals(EDhApiRenderingEngine.STUB, stub.getRenderingEngine());
		Assertions.assertEquals(EDhRenderDepth.REVERSE_Z, stub.getRenderDepth());
		Assertions.assertFalse(stub.isNativeRenderer());
		Assertions.assertNotNull(stub.getEngineName());
	}
	
	@Test
	void renderersAreNoOps()
	{
		StubDhRenderApiDefinition stub = new StubDhRenderApiDefinition();
		
		Assertions.assertDoesNotThrow(() -> stub.getMetaRenderer().runRenderPassSetup(null));
		Assertions.assertDoesNotThrow(() -> stub.getMetaRenderer().runRenderPassCleanup(null));
		Assertions.assertDoesNotThrow(() -> stub.getMetaRenderer().applyToMcTexture(null));
		Assertions.assertDoesNotThrow(() -> stub.getMetaRenderer().clearDhDepthAndColorTextures(null));
		
		Assertions.assertDoesNotThrow(() -> stub.getTerrainRenderer().render(null, false, null, null));
		Assertions.assertDoesNotThrow(() -> stub.getSsaoRenderer().render(null));
		Assertions.assertDoesNotThrow(() -> stub.getFogRenderer().render(null, null));
		Assertions.assertDoesNotThrow(() -> stub.getFarFadeRenderer().render(null));
		Assertions.assertDoesNotThrow(() -> stub.getVanillaFadeRenderer().render(null));
		Assertions.assertDoesNotThrow(() -> stub.getTestTriangleRenderer().render(null));
		Assertions.assertDoesNotThrow(() -> stub.getDebugWireframeRenderer().render(null));
	}
	
	@Test
	void factoriesProduceUsableNoOps()
	{
		StubDhRenderApiDefinition stub = new StubDhRenderApiDefinition();
		
		Assertions.assertDoesNotThrow(() ->
		{
			var generic = stub.createGenericRenderer();
			generic.render(null, null, false);
			generic.add(null);
			generic.remove(1);
			generic.close();
		});
		
		Assertions.assertDoesNotThrow(() ->
		{
			var vbo = stub.createVboWrapper("test");
			vbo.uploadVertexBuffer(null, 0);
			vbo.uploadIndexBuffer(null, 0);
			vbo.close();
		});
		
		Assertions.assertDoesNotThrow(() ->
		{
			var ubo = stub.createLodContainerUniformWrapper();
			ubo.tryUpload(null);
			ubo.close();
		});
		
		Assertions.assertDoesNotThrow(() ->
		{
			var container = stub.createGenericVboContainer();
			container.uploadDataToGpu();
			container.updateVertexData(null);
			container.setState(container.getState());
			container.close();
		});
	}
	
}
