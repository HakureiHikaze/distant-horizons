package com.seibel.distanthorizons.common.config;

import com.seibel.distanthorizons.core.config.Config;
import com.seibel.distanthorizons.core.config.LodRenderDistanceLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * UT-3-1/3-4/3-7: stage 3 config model — defaults, level<->radius linkage and
 * the test-triangle switch. Listener changes use setWithoutSaving so no config
 * file is touched in JVM tests.
 */
class Stage3ConfigTest
{
	@AfterEach
	void restoreDefaults()
	{
		Config.Client.Advanced.Graphics.Quality.lodRenderDistanceLevel.setWithoutSaving(LodRenderDistanceLevel.DEFAULT_LEVEL);
		Config.Client.Advanced.Graphics.Quality.lodChunkRenderDistanceRadius.setWithoutSaving(256);
		Config.Client.Advanced.Debugging.testTriangle.setWithoutSaving(false);
		Config.Client.Advanced.Graphics.Fog.enableVanillaFog.setWithoutSaving(false);
	}
	
	@Test // UT-3-1
	void newConfigEntriesHaveExpectedDefaults()
	{
		Assertions.assertEquals(LodRenderDistanceLevel.DEFAULT_LEVEL, Config.Client.Advanced.Graphics.Quality.lodRenderDistanceLevel.get());
		Assertions.assertEquals(256, Config.Client.Advanced.Graphics.Quality.lodChunkRenderDistanceRadius.get());
		Assertions.assertFalse(Config.Client.Advanced.Debugging.testTriangle.get());
	}
	
	@Test // UT-3-4
	void levelAndRadiusStayInSyncWithoutLoop()
	{
		Config.completeDelayedSetup();
		
		// level -> radius (one-way linkage, SA-3-9)
		Config.Client.Advanced.Graphics.Quality.lodRenderDistanceLevel.setWithoutSaving(6);
		Assertions.assertEquals(1024, Config.Client.Advanced.Graphics.Quality.lodChunkRenderDistanceRadius.get());
		
		// setting the same level again must not corrupt the radius
		Config.Client.Advanced.Graphics.Quality.lodRenderDistanceLevel.setWithoutSaving(6);
		Assertions.assertEquals(1024, Config.Client.Advanced.Graphics.Quality.lodChunkRenderDistanceRadius.get());
		
		// level 2 maps to the minimum meaningful radius (64)
		Config.Client.Advanced.Graphics.Quality.lodRenderDistanceLevel.setWithoutSaving(2);
		Assertions.assertEquals(64, Config.Client.Advanced.Graphics.Quality.lodChunkRenderDistanceRadius.get());
	}
	
	@Test // UT-3-7
	void testTriangleSwitchDefaultsOffAndCanBeSet()
	{
		Assertions.assertFalse(Config.Client.Advanced.Debugging.testTriangle.get());
		Config.Client.Advanced.Debugging.testTriangle.setWithoutSaving(true);
		Assertions.assertTrue(Config.Client.Advanced.Debugging.testTriangle.get());
	}
	
	@Test // stage 3 fog toggle: DH disables vanilla fog by default
	void vanillaFogToggleDefaultsOffAndCanBeSet()
	{
		Assertions.assertFalse(Config.Client.Advanced.Graphics.Fog.enableVanillaFog.get());
		Config.Client.Advanced.Graphics.Fog.enableVanillaFog.setWithoutSaving(true);
		Assertions.assertTrue(Config.Client.Advanced.Graphics.Fog.enableVanillaFog.get());
	}
}
