package com.displaytweaks.module;

import com.displaytweaks.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Keeps a totem of undying in your offhand. */
public class AutoTotem extends Module {
	private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 2, 0, 20, 1));
	private final NumberSetting belowHealth = add(new NumberSetting("Only Below Health", 20, 2, 20, 1));

	private int timer;

	public AutoTotem() {
		super("Auto Totem", "Keep a totem in your offhand", Category.PVP);
	}

	@Override
	public void onTick(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.interactionManager == null) return;
		if (timer > 0) {
			timer--;
			return;
		}
		if (p.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;
		if (p.getHealth() > belowHealth.get()) return;

		for (int i = 0; i < 36; i++) {
			if (p.getInventory().getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
				// Player screen handler: hotbar 0-8 = slots 36-44, main inventory 9-35 = slots 9-35.
				int slot = i < 9 ? 36 + i : i;
				// SWAP with button 40 swaps that slot with the offhand.
				mc.interactionManager.clickSlot(p.playerScreenHandler.syncId, slot, 40, SlotActionType.SWAP, p);
				timer = delay.getInt();
				return;
			}
		}
	}
}
