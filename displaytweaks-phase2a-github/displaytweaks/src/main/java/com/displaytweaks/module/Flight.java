package com.displaytweaks.module;

import com.displaytweaks.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class Flight extends Module {
	private final NumberSetting speed = add(new NumberSetting("Speed", 2.0, 0.5, 10.0, 0.5));

	public Flight() {
		super("Flight", "Creative-style flying", Category.MOVEMENT);
	}

	@Override
	public void onTick(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null) return;
		p.getAbilities().allowFlying = true;
		p.getAbilities().setFlySpeed(speed.getFloat() * 0.05f);
	}

	@Override
	protected void onDisable() {
		ClientPlayerEntity p = MinecraftClient.getInstance().player;
		if (p == null) return;
		p.getAbilities().setFlySpeed(0.05f);
		if (!p.isCreative() && !p.isSpectator()) {
			p.getAbilities().allowFlying = false;
			p.getAbilities().flying = false;
		}
	}
}
