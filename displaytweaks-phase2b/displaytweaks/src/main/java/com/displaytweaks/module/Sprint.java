package com.displaytweaks.module;

import com.displaytweaks.setting.BoolSetting;
import com.displaytweaks.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class Sprint extends Module {
	private final BoolSetting onlyGround = add(new BoolSetting("Only On Ground", false));
	private final BoolSetting whileSneaking = add(new BoolSetting("Sprint While Sneaking", false));
	private final NumberSetting minHunger = add(new NumberSetting("Min Food Level", 7, 0, 20, 1));

	public Sprint() {
		super("Sprint", "Always sprint while moving forward", Category.MOVEMENT);
	}

	@Override
	public void onTick(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null) return;
		if (!mc.options.forwardKey.isPressed() || p.horizontalCollision) return;
		if (p.isSneaking() && !whileSneaking.get()) return;
		if (onlyGround.get() && !p.isOnGround()) return;
		if (p.getHungerManager().getFoodLevel() < minHunger.get()) return;
		p.setSprinting(true);
	}
}
