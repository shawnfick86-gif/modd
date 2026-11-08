package com.displaytweaks.module;

import com.displaytweaks.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/** Clicks Respawn for you. */
public class AutoRespawn extends Module {
	private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 10, 0, 100, 5));
	private int timer;

	public AutoRespawn() {
		super("Auto Respawn", "Respawn automatically", Category.PLAYER);
	}

	@Override
	public void onTick(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null) return;
		if (p.isDead()) {
			if (++timer >= delay.getInt()) {
				p.requestRespawn();
				timer = 0;
			}
		} else {
			timer = 0;
		}
	}
}
