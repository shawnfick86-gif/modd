package com.ollie.hack.modules.render;
import com.ollie.hack.Category; import com.ollie.hack.Module; import net.minecraft.client.MinecraftClient; import net.minecraft.entity.Entity; import net.minecraft.entity.player.PlayerEntity; import java.util.HashMap; import java.util.Map;
public class ESP extends Module {
    private final Map<Entity, Integer> glowingEntities = new HashMap<>();
    public ESP() { super("ESP", Category.RENDER); }
    @Override public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance(); if (mc.world == null || mc.player == null) return;
        for (PlayerEntity player : mc.world.getPlayers()) { if (player != mc.player && !glowingEntities.containsKey(player)) { player.setGlowing(true); glowingEntities.put(player, player.getId()); } }
    }
    @Override public void onDisable() { for (Entity e : glowingEntities.keySet()) { if (e != null) e.setGlowing(false); } glowingEntities.clear(); }
}
