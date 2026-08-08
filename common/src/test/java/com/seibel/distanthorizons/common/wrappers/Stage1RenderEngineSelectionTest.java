package com.seibel.distanthorizons.common.wrappers;

import com.seibel.distanthorizons.api.enums.config.EDhApiRenderingEngine;
import com.seibel.distanthorizons.common.render.renderpearl.RpDhRenderApiDefinition;
import com.seibel.distanthorizons.common.render.renderpearl.test.RpTestTriangleRenderer;
import com.seibel.distanthorizons.common.render.stub.StubDhRenderApiDefinition;
import com.seibel.distanthorizons.core.dependencyInjection.SingletonInjector;
import com.seibel.distanthorizons.core.render.EDhRenderDepth;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.AbstractDhRenderApiDefinition;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.objects.IDhGenericObjectVertexBufferContainer;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.renderPass.IDhMetaRenderer;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.renderPass.IDhTestTriangleRenderer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** UT-7: RENDERPEARL selection + SA-3 stub consistency across all modules. */
class Stage1RenderEngineSelectionTest
{
	@Test
	void renderpearlEngineIsSelectable()
	{
		AbstractDhRenderApiDefinition definition = DependencySetup.createRenderDefinition(EDhApiRenderingEngine.RENDERPEARL);
		
		Assertions.assertNotNull(definition);
		Assertions.assertTrue(definition instanceof RpDhRenderApiDefinition);
		Assertions.assertEquals(EDhApiRenderingEngine.RENDERPEARL, definition.getRenderingEngine());
		Assertions.assertEquals(EDhRenderDepth.REVERSE_Z, definition.getRenderDepth());
	}
	
	@Test
	void nonTriangleRenderersStayOnStub()
	{
		RpDhRenderApiDefinition definition = new RpDhRenderApiDefinition();
		StubDhRenderApiDefinition stub = new StubDhRenderApiDefinition();
		
		Assertions.assertSame(stub.getMetaRenderer(), definition.getMetaRenderer());
		Assertions.assertSame(stub.getTerrainRenderer(), definition.getTerrainRenderer());
		Assertions.assertSame(stub.getSsaoRenderer(), definition.getSsaoRenderer());
		Assertions.assertSame(stub.getFogRenderer(), definition.getFogRenderer());
		Assertions.assertSame(stub.getFarFadeRenderer(), definition.getFarFadeRenderer());
		Assertions.assertSame(stub.getVanillaFadeRenderer(), definition.getVanillaFadeRenderer());
		Assertions.assertSame(stub.getDebugWireframeRenderer(), definition.getDebugWireframeRenderer());
		Assertions.assertSame(RpTestTriangleRenderer.INSTANCE, definition.getTestTriangleRenderer());
		
		// generic factories stay Stub and remain inert (SA-3)
		Assertions.assertDoesNotThrow(() ->
		{
			var generic = definition.createGenericRenderer();
			generic.render(null, null, false);
			generic.add(null);
			generic.remove(1);
			generic.close();
		});
		
		Assertions.assertDoesNotThrow(() ->
		{
			IDhGenericObjectVertexBufferContainer container = definition.createGenericVboContainer();
			container.uploadDataToGpu();
			container.updateVertexData(null);
			container.close();
		});
	}
	
	@Test
	void bindRenderersKeepsModulesInSync()
	{
		RpDhRenderApiDefinition definition = new RpDhRenderApiDefinition();
		definition.bindRenderers();
		
		Assertions.assertSame(definition.getMetaRenderer(), SingletonInjector.INSTANCE.get(IDhMetaRenderer.class));
		Assertions.assertSame(definition.getTerrainRenderer(), SingletonInjector.INSTANCE.get(com.seibel.distanthorizons.core.wrapperInterfaces.render.renderPass.IDhTerrainRenderer.class));
		Assertions.assertSame(RpTestTriangleRenderer.INSTANCE, SingletonInjector.INSTANCE.get(IDhTestTriangleRenderer.class));
		Assertions.assertSame(definition, SingletonInjector.INSTANCE.get(AbstractDhRenderApiDefinition.class));
	}
	
}
