/*
 *    This file is part of the Distant Horizons mod
 *    licensed under the GNU LGPL v3 License.
 *
 *    Copyright (C) 2020 James Seibel
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, version 3.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.seibel.distanthorizons.common.wrappers;

import com.seibel.distanthorizons.api.enums.config.EDhApiRenderingEngine;
import com.seibel.distanthorizons.api.interfaces.render.IDhApiCustomRenderObjectFactory;
#if MC_VER < MC_26_3_0
import com.seibel.distanthorizons.common.render.blaze.BlazeDhRenderApiDefinition;
import com.seibel.distanthorizons.common.render.openGl.GlDhRenderApiDefinition;
#endif
import com.seibel.distanthorizons.common.render.stub.StubDhRenderApiDefinition;
import com.seibel.distanthorizons.core.config.Config;
import com.seibel.distanthorizons.api.enums.config.EDhApiRenderingApi;
import com.seibel.distanthorizons.core.render.renderer.GenericRenderObjectFactory;
import com.seibel.distanthorizons.common.wrappers.gui.classicConfig.ClassicConfigGUI;
import com.seibel.distanthorizons.common.wrappers.gui.LangWrapper;
import com.seibel.distanthorizons.common.wrappers.level.KeyedClientLevelManager;
import com.seibel.distanthorizons.common.wrappers.minecraft.MinecraftServerWrapper;
import com.seibel.distanthorizons.core.level.IKeyedClientLevelManager;
import com.seibel.distanthorizons.core.logging.DhLogger;
import com.seibel.distanthorizons.core.logging.DhLoggerBuilder;
import com.seibel.distanthorizons.common.wrappers.block.BlockStateTextureProvider;
import com.seibel.distanthorizons.core.wrapperInterfaces.block.IBlockStateFaceTextureProvider;
import com.seibel.distanthorizons.core.wrapperInterfaces.config.IConfigGui;
import com.seibel.distanthorizons.core.wrapperInterfaces.config.ILangWrapper;
import com.seibel.distanthorizons.common.wrappers.minecraft.MinecraftClientWrapper;
import com.seibel.distanthorizons.common.wrappers.minecraft.MinecraftRenderWrapper;
import com.seibel.distanthorizons.core.dependencyInjection.SingletonInjector;
import com.seibel.distanthorizons.core.wrapperInterfaces.IVersionConstants;
import com.seibel.distanthorizons.core.wrapperInterfaces.IWrapperFactory;
import com.seibel.distanthorizons.core.wrapperInterfaces.minecraft.IMinecraftClientWrapper;
import com.seibel.distanthorizons.core.wrapperInterfaces.minecraft.IMinecraftRenderWrapper;
import com.seibel.distanthorizons.core.wrapperInterfaces.minecraft.IMinecraftSharedWrapper;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.AbstractDhRenderApiDefinition;

/**
 * Binds all necessary dependencies, so we
 * can access them in Core. <br>
 * This needs to be called before any Core classes
 * are loaded.
 *
 * @author James Seibel
 * @author Ran
 * @version 12-1-2021
 */
public class DependencySetup
{
	protected static final DhLogger LOGGER = new DhLoggerBuilder().build();
	
	
	
	public static void createSharedBindings()
	{
		SingletonInjector.INSTANCE.bind(ILangWrapper.class, LangWrapper.INSTANCE);
		SingletonInjector.INSTANCE.bind(IVersionConstants.class, VersionConstants.INSTANCE);
		SingletonInjector.INSTANCE.bind(IWrapperFactory.class, WrapperFactory.INSTANCE);
		SingletonInjector.INSTANCE.bind(IKeyedClientLevelManager.class, new KeyedClientLevelManager());
		SingletonInjector.INSTANCE.bind(IDhApiCustomRenderObjectFactory.class, GenericRenderObjectFactory.INSTANCE);
	}
	
	public static void createServerBindings()
	{ SingletonInjector.INSTANCE.bind(IMinecraftSharedWrapper.class, MinecraftServerWrapper.INSTANCE); }
	
	public static void createClientBindings()
	{
		SingletonInjector.INSTANCE.bind(IMinecraftClientWrapper.class, MinecraftClientWrapper.INSTANCE);
		SingletonInjector.INSTANCE.bind(IMinecraftSharedWrapper.class, MinecraftClientWrapper.INSTANCE);
		SingletonInjector.INSTANCE.bind(IMinecraftRenderWrapper.class, MinecraftRenderWrapper.INSTANCE);
		SingletonInjector.INSTANCE.bind(IConfigGui.class, ClassicConfigGUI.CONFIG_CORE_INTERFACE);
		SingletonInjector.INSTANCE.bind(IBlockStateFaceTextureProvider.class, BlockStateTextureProvider.INSTANCE);
	}
	
	private static boolean renderingApiBindingsSet = false;
	/** will be called from a DH thread, not the render thread */
	public synchronized static void setRenderingApiBindings()
	{
		// shouldn't happen, but there was a single report that this method was triggered twice
		if (renderingApiBindingsSet)
		{
			LOGGER.warn("Rendering bindings already set, skipping. How did this happen?");
			return;
		}
		renderingApiBindingsSet = true;
		
		
		
		EDhApiRenderingEngine renderingApiEnum = Config.Client.Advanced.Graphics.Experimental.renderingEngine.get();
		if (renderingApiEnum == EDhApiRenderingEngine.AUTO)
		{
			IVersionConstants versionConstants = SingletonInjector.INSTANCE.get(IVersionConstants.class);
			renderingApiEnum = versionConstants.getDefaultRenderingEngine();
		}
		
		LOGGER.info("Setting DH Rendering API to: ["+renderingApiEnum+"]...");
		
		
		
		AbstractDhRenderApiDefinition renderDefinition;
		try
		{
			renderDefinition = createRenderDefinition(renderingApiEnum);
		}
		catch (IllegalStateException e)
		{
			// crash if an invalid API is set, but revert the config to AUTO first
			LOGGER.fatal(e.getMessage());
			Config.Client.Advanced.Graphics.Experimental.renderingEngine.set(EDhApiRenderingEngine.AUTO);
			throw e;
		}
		
		// crash if the rendering API set doesn't match Minecraft's
		EDhApiRenderingApi mcRenderApi = MinecraftRenderWrapper.INSTANCE.getMcRenderingApi();
		if (mcRenderApi != renderDefinition.getRenderApi())
		{
			String message = "The Distant Horizons rendering engine ["+renderDefinition.getEngineName()+"]-["+renderDefinition.getRenderApi().name()+"] cannot be used since it's API doesn't match what Minecraft is currently set to use ["+mcRenderApi.name()+"]. Please either change Minecraft's rendering API or Distant Horizons'.";
			LOGGER.fatal(message);
			throw new IllegalStateException(message);
		}
		
		
		renderDefinition.bindRenderers();
		LOGGER.info("DH Rendering successfully bound to: ["+renderDefinition.getEngineName()+"]...");
	}
	
	/**
	 * Maps a {@link EDhApiRenderingEngine} to its concrete implementation.
	 * Kept package-private so it can be unit tested without booting Minecraft.
	 *
	 * @throws IllegalStateException if the engine has no concrete implementation
	 *         or is not supported for the compiled MC version.
	 */
	static AbstractDhRenderApiDefinition createRenderDefinition(EDhApiRenderingEngine renderingApiEnum)
	{
		AbstractDhRenderApiDefinition renderDefinition;
		if (renderingApiEnum == EDhApiRenderingEngine.STUB)
		{
			renderDefinition = new StubDhRenderApiDefinition();
		}
		#if MC_VER < MC_26_3_0
		else if (renderingApiEnum == EDhApiRenderingEngine.OPEN_GL)
		{
			renderDefinition = new GlDhRenderApiDefinition();
		}
		else if (renderingApiEnum == EDhApiRenderingEngine.BLAZE_3D)
		{
			#if MC_VER <= MC_1_21_10
			throw new IllegalStateException("The Distant Horizons rendering engine ["+renderingApiEnum.name()+"] is not supported with this Minecraft config, reverting to ["+ EDhApiRenderingEngine.AUTO+"].");
			#else
			renderDefinition = new BlazeDhRenderApiDefinition();
			#endif
		}
		#endif
		else
		{
			String message = "No ["+ AbstractDhRenderApiDefinition.class.getSimpleName()+"] concrete implementation found for the value: ["+renderingApiEnum+"].";
			LOGGER.fatal(message);
			throw new IllegalStateException(message);
		}
		
		return renderDefinition;
	}
	
	
	
}
