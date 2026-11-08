package com.displaytweaks.module;

import com.displaytweaks.setting.BoolSetting;
import com.displaytweaks.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;

/** Raises your step height so you walk up blocks without jumping. */
public class Step extends Module {
	private static final double VANILLA = 0.6;
	private final NumberSetting height = add(new NumberSetting("Height", 1.0, 1.0, 3.0, 0.5));
	private final BoolSetting onlySprint = add(new BoolSetting("Only While Sprinting", false));
	private final BoolSetting notSneaking = add(new BoolSetting("Not While Sneaking", true));

	public Step() {
		super("Step", "Walk up full blocks", Category.PLAYER);
	}

	private static void set(ClientPlayerEntity p, double value) {
		EntityAttributeInstance inst = p.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
		if (inst != null) inst.setBaseValue(value);
	}

	@Override
	public void onTick(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null) return;
		boolean active = !(onlySprint.get() && !p.isSprinting()) && !(notSneaking.get() && p.isSneaking());
		set(p, active ? height.get() : VANILLA);
	}

	@Override
	protected void onDisable() {
		ClientPlayerEntity p = MinecraftClient.getInstance().player;
		if (p != null) set(p, VANILLA);
	}
}
