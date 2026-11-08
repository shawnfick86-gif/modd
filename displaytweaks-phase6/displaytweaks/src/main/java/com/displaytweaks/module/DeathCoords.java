package com.displaytweaks.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/** Prints where you died in chat so you can go get your stuff. */
public class DeathCoords extends Module {
	private BlockPos last;
	private boolean reported;

	public DeathCoords() {
		super("Death Coords", "Show where you died", Category.MEDIA);
	}

	@Override
	public void onTick(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.world == null) return;
		if (p.isDead()) {
			if (!reported && last != null) {
				String dim = mc.world.getRegistryKey().getValue().toString();
				p.sendMessage(Text.literal("[Death] You died at " + last.getX() + " " + last.getY() + " "
						+ last.getZ() + " (" + dim + ")"), false);
				reported = true;
			}
		} else {
			reported = false;
			last = p.getBlockPos();
		}
	}
}
