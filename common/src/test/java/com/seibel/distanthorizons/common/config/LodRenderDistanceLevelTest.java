package com.seibel.distanthorizons.common.config;

import com.seibel.distanthorizons.core.config.LodRenderDistanceLevel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** UT-3-3: view distance level (2-8) <-> chunk radius mapping. */
class LodRenderDistanceLevelTest
{
	@Test
	void levelToRadiusDoublesPerLevel()
	{
		int expected = 64; // level 2 = 32 << 1
		for (int level = LodRenderDistanceLevel.MIN_LEVEL; level <= LodRenderDistanceLevel.MAX_LEVEL; level++)
		{
			Assertions.assertEquals(expected, LodRenderDistanceLevel.levelToRadius(level), "level " + level);
			expected *= 2;
		}
		Assertions.assertEquals(4096, LodRenderDistanceLevel.levelToRadius(8));
	}
	
	@Test
	void radiusToLevelPicksNearestLevel()
	{
		Assertions.assertEquals(2, LodRenderDistanceLevel.radiusToLevel(64));
		Assertions.assertEquals(3, LodRenderDistanceLevel.radiusToLevel(128));
		Assertions.assertEquals(4, LodRenderDistanceLevel.radiusToLevel(256));
		Assertions.assertEquals(5, LodRenderDistanceLevel.radiusToLevel(512));
		Assertions.assertEquals(8, LodRenderDistanceLevel.radiusToLevel(4096));
		
		// nearest-level rounding for non-exact radii
		Assertions.assertEquals(3, LodRenderDistanceLevel.radiusToLevel(100)); // closer to 128 than 64
		Assertions.assertEquals(4, LodRenderDistanceLevel.radiusToLevel(300)); // closer to 256 than 512
	}
	
	@Test
	void radiusToLevelClampsOutsideRange()
	{
		Assertions.assertEquals(LodRenderDistanceLevel.MIN_LEVEL, LodRenderDistanceLevel.radiusToLevel(1));
		Assertions.assertEquals(LodRenderDistanceLevel.MIN_LEVEL, LodRenderDistanceLevel.radiusToLevel(32));
		Assertions.assertEquals(LodRenderDistanceLevel.MAX_LEVEL, LodRenderDistanceLevel.radiusToLevel(1_000_000));
	}
	
	@Test
	void levelToRadiusClampsAndValidates()
	{
		Assertions.assertEquals(64, LodRenderDistanceLevel.levelToRadius(0));
		Assertions.assertEquals(4096, LodRenderDistanceLevel.levelToRadius(99));
		Assertions.assertTrue(LodRenderDistanceLevel.isValidLevel(2));
		Assertions.assertTrue(LodRenderDistanceLevel.isValidLevel(8));
		Assertions.assertFalse(LodRenderDistanceLevel.isValidLevel(1));
		Assertions.assertFalse(LodRenderDistanceLevel.isValidLevel(9));
	}
}
