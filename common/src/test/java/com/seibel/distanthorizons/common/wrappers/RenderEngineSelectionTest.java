package com.seibel.distanthorizons.common.wrappers;

import com.seibel.distanthorizons.api.enums.config.EDhApiRenderingEngine;
import com.seibel.distanthorizons.common.render.stub.StubDhRenderApiDefinition;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.AbstractDhRenderApiDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Stage 0: on MC 26.3 the engine selection must resolve to the no-op Stub engine.
 */
class RenderEngineSelectionTest
{
	@Test
	void stubEngineIsSelected()
	{
		AbstractDhRenderApiDefinition definition = DependencySetup.createRenderDefinition(EDhApiRenderingEngine.STUB);
		Assertions.assertNotNull(definition);
		Assertions.assertTrue(definition instanceof StubDhRenderApiDefinition);
		Assertions.assertEquals(EDhApiRenderingEngine.STUB, definition.getRenderingEngine());
	}
	
	@Test
	void autoResolvesToStubOn26_3()
	{
		// the game's real path: AUTO -> VersionConstants.getDefaultRenderingEngine() -> concrete definition
		EDhApiRenderingEngine recommended = VersionConstants.INSTANCE.getDefaultRenderingEngine();
		Assertions.assertEquals(EDhApiRenderingEngine.STUB, recommended);
		
		AbstractDhRenderApiDefinition definition = DependencySetup.createRenderDefinition(recommended);
		Assertions.assertNotNull(definition);
		Assertions.assertTrue(definition instanceof StubDhRenderApiDefinition);
	}
	
	@Test
	void oldEnginesUnavailableWhenStubIsDefault()
	{
		// only enforced on versions where the Stub engine is the default (26.3+)
		if (VersionConstants.INSTANCE.getDefaultRenderingEngine() != EDhApiRenderingEngine.STUB)
		{
			return;
		}
		
		Assertions.assertThrows(IllegalStateException.class, () -> DependencySetup.createRenderDefinition(EDhApiRenderingEngine.OPEN_GL));
		Assertions.assertThrows(IllegalStateException.class, () -> DependencySetup.createRenderDefinition(EDhApiRenderingEngine.BLAZE_3D));
	}
	
}
