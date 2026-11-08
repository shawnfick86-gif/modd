package com.displaytweaks.module;

import com.displaytweaks.setting.BoolSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/** Client-side night vision effect. Nothing is sent to the server. */
public class Fullbright extends Module {
	private final BoolSetting hideIcon = add(new BoolSetting("Hide Effect Icon", true));
	private final BoolSetting hideParticles = add(new BoolSetting("Hide Particles", true));

	public Fullbright() {
		super("Fullbright", "Max brightness everywhere", Category.RENDER);
	}

	@Override
	public void onTick(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null) return;
		StatusEffectInstance cur = p.getStatusEffect(StatusEffects.NIGHT_VISION);
		if (cur == null || cur.getDuration() < 300) {
			p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1000, 0,
					false, !hideParticles.get(), !hideIcon.get()));
		}
	}

	@Override
	protected void onDisable() {
		ClientPlayerEntity p = MinecraftClient.getInstance().player;
		if (p != null) p.removeStatusEffect(StatusEffects.NIGHT_VISION);
	}
}
