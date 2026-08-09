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

package com.seibel.distanthorizons.fabric.wrappers.modAccessor;

#if MC_VER >= MC_1_19_4 && MC_VER < MC_26_3_0

import com.seibel.distanthorizons.core.wrapperInterfaces.modAccessor.IIrisAccessor;

#if MC_VER <= MC_1_20_4
import net.coderbot.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
#else
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
#endif

public class IrisAccessor implements IIrisAccessor
{
	@Override
	public String getModName() { return Iris.MODID; }
	
	@Override
	public boolean isShaderPackInUse() { return IrisApi.getInstance().isShaderPackInUse(); }
	
	@Override
	public boolean isRenderingShadowPass() { return IrisApi.getInstance().isRenderingShadowPass(); }
	
}

#elif MC_VER >= MC_26_3_0

import com.seibel.distanthorizons.core.wrapperInterfaces.modAccessor.IIrisAccessor;
import com.seibel.distanthorizons.core.logging.DhLogger;
import com.seibel.distanthorizons.core.logging.DhLoggerBuilder;

public class IrisAccessor implements IIrisAccessor
{
	private static final DhLogger LOGGER = new DhLoggerBuilder().name("IrisAccessor").build();
	
	public IrisAccessor()
	{
		LOGGER.warn("Iris 26.3 accessor is a no-op stub until stage 6; shader pack state will report disabled.");
	}
	
	// Iris 26.3 accessor pending (stage 6); stub keeps the mod compiling without Iris on the classpath
	@Override public String getModName() { return "iris"; }
	@Override public boolean isShaderPackInUse() { return false; }
	@Override public boolean isRenderingShadowPass() { return false; }
	
}

#endif
