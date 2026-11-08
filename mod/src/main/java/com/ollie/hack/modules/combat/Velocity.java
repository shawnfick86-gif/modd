package com.ollie.hack.modules.combat;
import com.ollie.hack.Category; import com.ollie.hack.Module; import net.minecraft.client.MinecraftClient; import net.minecraft.entity.player.PlayerEntity;
public class Velocity extends Module {
    public Velocity() { super("Velocity", Category.COMBAT); }
    @Override public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance(); PlayerEntity player = mc.player; if (player == null) return;
        if (Math.abs(player.getVelocity().x) > 0.5 || Math.abs(player.getVelocity().z) > 0.5) {
            player.setVelocity(player.getVelocity().x * 0.2, player.getVelocity().y, player.getVelocity().z * 0.2);
        }
    }
}
