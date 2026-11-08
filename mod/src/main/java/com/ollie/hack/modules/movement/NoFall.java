package com.ollie.hack.modules.movement;
import com.ollie.hack.Category; import com.ollie.hack.Module; import net.minecraft.client.MinecraftClient;
public class NoFall extends Module {
    public NoFall() { super("NoFall", Category.MOVEMENT); }
    @Override public void onTick() { MinecraftClient mc = MinecraftClient.getInstance(); if (mc.player != null) mc.player.fallDistance = 0.0f; }
}
