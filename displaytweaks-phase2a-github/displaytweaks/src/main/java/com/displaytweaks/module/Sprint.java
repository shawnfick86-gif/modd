package com.displaytweaks.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class Sprint extends Module {
	public Sprint() {
		super("Sprint", "Always sprint while moving forward", Category.MOVEMENT);
	}

	@Override
	public void onTick(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null) return;
		if (mc.options.forwardKey.isPressed() && !p.horizontalCollision && !p.isSneaking()
				&& p.getHungerManager().getFoodLevel() > 6) {
			p.setSprinting(true);
		}
	}
}
