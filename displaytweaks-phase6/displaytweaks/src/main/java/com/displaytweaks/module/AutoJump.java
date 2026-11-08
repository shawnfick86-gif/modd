package com.displaytweaks.module;

import com.displaytweaks.setting.BoolSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/** Jumps for you whenever you're on the ground and moving. */
public class AutoJump extends Module {
	private final BoolSetting onlySprint = add(new BoolSetting("Only While Sprinting", false));

	public AutoJump() {
		super("Auto Jump", "Bunny-hop automatically", Category.MOVEMENT);
	}

	@Override
	public void onTick(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.currentScreen != null) return;
		if (!p.isOnGround() || p.isSneaking()) return;
		boolean moving = mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed()
				|| mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed();
		if (!moving) return;
		if (onlySprint.get() && !p.isSprinting()) return;
		p.jump();
	}
}
