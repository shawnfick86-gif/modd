package com.ollie.hack.modules.render;
import com.ollie.hack.Category; import com.ollie.hack.Module; import net.minecraft.client.MinecraftClient;
public class Fullbright extends Module {
    private double oldGamma;
    public Fullbright() { super("Fullbright", Category.RENDER); }
    @Override public void onEnable() { MinecraftClient mc = MinecraftClient.getInstance(); if (mc.options != null) { oldGamma = mc.options.getGamma().getValue(); mc.options.getGamma().setValue(10.0); } }
    @Override public void onDisable() { MinecraftClient mc = MinecraftClient.getInstance(); if (mc.options != null) mc.options.getGamma().setValue(oldGamma); }
}
