package com.displaytweaks.render;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;

/**
 * Thin wrapper over Minecraft 1.21.11's built-in "gizmo" debug drawing.
 * ignoreOcclusion() is what makes the shapes show through walls.
 */
public final class EspDraw {
	private EspDraw() {}

	private static DrawStyle style(int argb, boolean filled) {
		if (filled) {
			return DrawStyle.filledAndStroked(argb, 2.0f, (argb & 0x00FFFFFF) | 0x50000000);
		}
		return DrawStyle.stroked(argb, 2.0f);
	}

	public static void box(BlockPos pos, int argb, boolean filled) {
		GizmoDrawing.box(pos, style(argb, filled)).ignoreOcclusion();
	}

	public static void box(Box box, int argb, boolean filled) {
		GizmoDrawing.box(box, style(argb, filled)).ignoreOcclusion();
	}

	public static void line(Vec3d from, Vec3d to, int argb) {
		GizmoDrawing.line(from, to, argb, 2.0f).ignoreOcclusion();
	}

	/** Where tracer lines start: just in front of your eyes so they're visible. */
	public static Vec3d tracerStart(ClientPlayerEntity p) {
		return p.getEyePos().add(p.getRotationVec(1.0f).multiply(0.6));
	}
}
