package com.displaytweaks.module;

import com.displaytweaks.setting.BoolSetting;
import net.minecraft.client.MinecraftClient;

/** Holds the forward key for you. Handy for AFK farms and long trips. */
public class AutoWalk extends Module {
	private final BoolSetting sprint = add(new BoolSetting("Sprint", true));

	public AutoWalk() {
		super("Auto Walk", "Walk forward automatically", Category.MOVEMENT);
	}

	@Override
	public void onTick(MinecraftClient mc) {
		if (mc.player == null || mc.currentScreen != null) return;
		mc.options.forwardKey.setPressed(true);
		if (sprint.get()) mc.player.setSprinting(true);
	}

	@Override
	protected void onDisable() {
		MinecraftClient.getInstance().options.forwardKey.setPressed(false);
	}
}
