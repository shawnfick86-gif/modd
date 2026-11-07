package com.displaytweaks.module;

import com.displaytweaks.setting.BoolSetting;
import com.displaytweaks.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class Flight extends Module {
	private final NumberSetting speed = add(new NumberSetting("Speed", 2.0, 0.5, 10.0, 0.5));
	private final BoolSetting sprintBoost = add(new BoolSetting("Sprint Boost (x1.5)", false));
	private final BoolSetting keepOnLand = add(new BoolSetting("Keep Flight After Disable (creative only)", false));

	public Flight() {
		super("Flight", "Creative-style flying", Category.MOVEMENT);
	}

	@Override
	public void onTick(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null) return;
		float mult = (sprintBoost.get() && p.isSprinting()) ? 1.5f : 1f;
		p.getAbilities().allowFlying = true;
		p.getAbilities().setFlySpeed(speed.getFloat() * 0.05f * mult);
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
