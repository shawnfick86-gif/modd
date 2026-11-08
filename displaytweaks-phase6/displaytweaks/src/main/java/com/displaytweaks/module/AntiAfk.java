package com.displaytweaks.module;

import com.displaytweaks.setting.BoolSetting;
import com.displaytweaks.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;

/** Moves a little now and then so you don't get kicked for idling. */
public class AntiAfk extends Module {
	private final NumberSetting interval = add(new NumberSetting("Interval (seconds)", 30, 5, 120, 5));
	private final BoolSetting jump = add(new BoolSetting("Jump", true));
	private final BoolSetting swing = add(new BoolSetting("Swing Hand", true));
	private int timer;
	private boolean flip;

	public AntiAfk() {
		super("Anti AFK", "Stay active while idle", Category.MEDIA);
	}

	@Override
	public void onTick(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.currentScreen != null) return;
		if (++timer < interval.getInt() * 20) return;
		timer = 0;
		flip = !flip;
		p.setYaw(p.getYaw() + (flip ? 3f : -3f));
		if (swing.get()) p.swingHand(Hand.MAIN_HAND);
		if (jump.get() && p.isOnGround()) p.jump();
	}
}
