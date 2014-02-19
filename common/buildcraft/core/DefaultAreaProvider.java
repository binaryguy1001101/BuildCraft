/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import buildcraft.api.core.IAreaProvider;

public class DefaultAreaProvider implements IAreaProvider {

	int xMin, yMin, zMin, xMax, yMax, zMax;

	public DefaultAreaProvider(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {

		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		this.zMin = zMin;
		this.zMax = zMax;
	}

	@Override
	public float xMin() {
		return xMin;
	}

	@Override
	public float yMin() {
		return yMin;
	}

	@Override
	public float zMin() {
		return zMin;
	}

	@Override
	public float xMax() {
		return xMax;
	}

	@Override
	public float yMax() {
		return yMax;
	}

	@Override
	public float zMax() {
		return zMax;
	}

	@Override
	public void removeFromWorld() {

	}
}
