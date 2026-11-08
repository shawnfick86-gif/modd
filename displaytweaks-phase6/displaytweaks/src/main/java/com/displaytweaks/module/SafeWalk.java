package com.displaytweaks.module;

import com.displaytweaks.setting.BoolSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** Stops you from walking off ledges, like sneaking without the slowdown. */
public class SafeWalk extends Module {
	private final BoolSetting onlyGround = add(new BoolSetting("Only On Ground", true));

	public SafeWalk() {
		super("Safe Walk", "Don't walk off edges", Category.MOVEMENT);
	}

	@Override
	public void onTick(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.world == null) return;
		if (onlyGround.get() && !p.isOnGround()) return;
		Vec3d v = p.getVelocity();
		if (Math.abs(v.x) + Math.abs(v.z) < 0.01) return;
		Box ahead = p.getBoundingBox().offset(v.x * 3.0, -0.6, v.z * 3.0);
		if (mc.world.isSpaceEmpty(p, ahead)) {
			p.setVelocity(0, v.y, 0);
		}
	}
}
