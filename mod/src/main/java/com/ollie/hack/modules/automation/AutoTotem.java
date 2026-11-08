package com.ollie.hack.modules.automation;
import com.ollie.hack.Category; import com.ollie.hack.Module; import net.minecraft.client.MinecraftClient; import net.minecraft.item.Items; import net.minecraft.item.ItemStack; import net.minecraft.screen.slot.SlotActionType;
public class AutoTotem extends Module {
    public AutoTotem() { super("AutoTotem", Category.AUTOMATION); }
    @Override public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance(); if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                int slotId = i < 9 ? i + 36 : i;
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slotId, 0, SlotActionType.SWAP, mc.player); return;
            }
        }
    }
}
