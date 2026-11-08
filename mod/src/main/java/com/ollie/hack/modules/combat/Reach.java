package com.ollie.hack.modules.combat;

import com.ollie.hack.Category;
import com.ollie.hack.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.attribute.EntityAttributes;

public class Reach extends Module {
    private double oldEntityReach = 3.0;
    private double oldBlockReach = 4.5;
    private final double newReach = 6.0;

    public Reach() { super("Reach", Category.COMBAT); }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            oldEntityReach = mc.player.getAttributeValue(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE);
            oldBlockReach = mc.player.getAttributeValue(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE);
            mc.player.getAttributeInstance(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE).setBaseValue(newReach);
            mc.player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE).setBaseValue(newReach);
        }
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.getAttributeInstance(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE).setBaseValue(oldEntityReach);
            mc.player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE).setBaseValue(oldBlockReach);
        }
    }
}
