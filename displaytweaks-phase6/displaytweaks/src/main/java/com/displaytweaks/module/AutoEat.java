package com.displaytweaks.module;

import com.displaytweaks.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;

/** Eats the food in your main hand when you get hungry. */
public class AutoEat extends Module {
	private final NumberSetting hunger = add(new NumberSetting("Eat Below Food Level", 14, 1, 19, 1));
	private boolean eating;

	public AutoEat() {
		super("Auto Eat", "Eat held food when hungry", Category.PLAYER);
	}

	@Override
	public void onTick(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.currentScreen != null) return;
		boolean food = p.getMainHandStack().contains(DataComponentTypes.FOOD);
		boolean hungry = p.getHungerManager().getFoodLevel() <= hunger.get();
		if (food && hungry) {
			mc.options.useKey.setPressed(true);
			eating = true;
		} else if (eating) {
			mc.options.useKey.setPressed(false);
			eating = false;
		}
	}

	@Override
	protected void onDisable() {
		if (eating) MinecraftClient.getInstance().options.useKey.setPressed(false);
		eating = false;
	}
}
