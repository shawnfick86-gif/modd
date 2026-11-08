package com.displaytweaks.module;

import com.displaytweaks.render.EspDraw;
import com.displaytweaks.setting.BoolSetting;
import com.displaytweaks.setting.ColorSetting;
import com.displaytweaks.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;

/** Outlines dropped items through walls. */
public class ItemEsp extends EspModule {
	private final ColorSetting color = add(new ColorSetting("Color", 2));
	private final BoolSetting filled = add(new BoolSetting("Filled Boxes", false));
	private final NumberSetting range = add(new NumberSetting("Range", 64, 8, 128, 8));

	public ItemEsp() {
		super("Item ESP", "Outline dropped items");
	}

	@Override
	protected void emit(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.world == null) return;
		Box area = p.getBoundingBox().expand(range.get());
		for (ItemEntity item : mc.world.getEntitiesByClass(ItemEntity.class, area, e -> true)) {
			EspDraw.box(item.getBoundingBox().expand(0.05), color.get(), filled.get());
		}
	}
}
