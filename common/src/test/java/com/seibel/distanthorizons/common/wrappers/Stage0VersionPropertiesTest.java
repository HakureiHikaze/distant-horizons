package com.seibel.distanthorizons.common.wrappers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Stage 0: verifies the 26.3 version properties file contains the full
 * toolchain baseline required by the development plan.
 */
class Stage0VersionPropertiesTest
{
	@Test
	void versionPropertiesAreComplete() throws IOException
	{
		File propsFile = findVersionPropertiesFile();
		Assertions.assertTrue(propsFile.exists(), "versionProperties/26.3.0.properties not found from: " + System.getProperty("user.dir"));
		
		Properties props = new Properties();
		try (FileInputStream in = new FileInputStream(propsFile))
		{
			props.load(in);
		}
		
		Assertions.assertEquals("25", props.getProperty("java_version"));
		Assertions.assertEquals("26.3-snapshot-7", props.getProperty("minecraft_version"));
		Assertions.assertEquals("0.19.3", props.getProperty("fabric_loader_version"));
		Assertions.assertEquals("0.156.3+26.3", props.getProperty("fabric_api_version"));
		Assertions.assertEquals("26_3", props.getProperty("accessWidenerVersion"));
		Assertions.assertEquals("fabric", props.getProperty("builds_for"));
		Assertions.assertTrue(props.getProperty("compatible_minecraft_versions").contains("26.3-alpha.7"));
	}
	
	private static File findVersionPropertiesFile()
	{
		File dir = new File(System.getProperty("user.dir")).getAbsoluteFile();
		while (dir != null)
		{
			File candidate = new File(dir, "versionProperties/26.3.0.properties");
			if (candidate.exists())
			{
				return candidate;
			}
			dir = dir.getParentFile();
		}
		return new File("versionProperties/26.3.0.properties");
	}
	
}
