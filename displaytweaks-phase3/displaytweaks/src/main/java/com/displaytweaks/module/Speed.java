package com.displaytweaks.module;

import com.displaytweaks.setting.BoolSetting;
import com.displaytweaks.setting.ModeSetting;
import com.displaytweaks.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class Speed extends Module {
	private final ModeSetting mode = add(new ModeSetting("Mode", 1, "Vanilla", "Strafe"));
	private final NumberSetting speed = add(new NumberSetting("Speed", 1.5, 1.0, 5.0, 0.1));
	private final BoolSetting onlyGround = add(new BoolSetting("Only On Ground", false));
	private final BoolSetting onlySprint = add(new BoolSetting("Only While Sprinting", false));

	public Speed() {
		super("Speed", "Move faster", Category.MOVEMENT);
	}

	@Override
	public void onTick(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null) return;
		if (onlyGround.get() && !p.isOnGround()) return;
		if (onlySprint.get() && !p.isSprinting()) return;

		float fwd = (mc.options.forwardKey.isPressed() ? 1 : 0) - (mc.options.backKey.isPressed() ? 1 : 0);
		float left = (mc.options.leftKey.isPressed() ? 1 : 0) - (mc.options.rightKey.isPressed() ? 1 : 0);
		if (fwd == 0 && left == 0) return;

		Vec3d v = p.getVelocity();
		double cap = 0.2873 * speed.get();

		if (mode.is("Strafe")) {
			double yaw = Math.toRadians(p.getYaw());
			double dx = fwd * -Math.sin(yaw) + left * Math.cos(yaw);
			double dz = fwd * Math.cos(yaw) + left * Math.sin(yaw);
			double len = Math.sqrt(dx * dx + dz * dz);
			dx /= len;
			dz /= len;
			p.setVelocity(dx * cap, v.y, dz * cap);
		} else {
			double m = 1.0 + (speed.get() - 1.0) * 0.15;
			double hx = v.x * m, hz = v.z * m;
			double h = Math.sqrt(hx * hx + hz * hz);
			if (h > cap) {
				hx *= cap / h;
				hz *= cap / h;
			}
			p.setVelocity(hx, v.y, hz);
		}
	}
}
