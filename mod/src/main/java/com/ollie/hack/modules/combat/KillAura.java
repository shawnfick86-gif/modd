package com.ollie.hack.modules.combat;
import com.ollie.hack.Category; import com.ollie.hack.Module; import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity; import net.minecraft.util.Hand;
public class KillAura extends Module {
    public KillAura() { super("KillAura", Category.COMBAT); }
    @Override public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance(); if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        for (PlayerEntity target : mc.world.getPlayers()) {
            if (target != mc.player && target.isAlive() && mc.player.distanceTo(target) <= 4.5f) {
                mc.player.lookAt(net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor.EYES, target.getPos());
                mc.interactionManager.attackEntity(mc.player, target); mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }
}
