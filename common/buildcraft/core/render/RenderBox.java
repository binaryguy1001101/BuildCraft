/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.render;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import buildcraft.core.Box;
import buildcraft.core.DefaultProps;
import buildcraft.core.LaserData;

public class RenderBox {

	private static final ResourceLocation LASER_TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/laser_1.png");

	public static void doRender(TextureManager t, Box box, double x, double y, double z, float f, float f1) {
		GL11.glPushMatrix();
		GL11.glDisable(2896 /* GL_LIGHTING */);
		GL11.glTranslated(x, y, z);

		box.createLaserData();

		for (LaserData l : box.lasersData) {
			l.update();
			GL11.glPushMatrix();
			GL11.glTranslated(l.head.x, l.head.y, l.head.z);
			RenderLaser.doRenderLaser(t, l, LASER_TEXTURE);
			GL11.glPopMatrix();
		}

		GL11.glEnable(2896 /* GL_LIGHTING */);
		GL11.glPopMatrix();
	}

}
