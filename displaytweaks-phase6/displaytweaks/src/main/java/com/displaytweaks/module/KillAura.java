package com.displaytweaks.module;

import com.displaytweaks.setting.BoolSetting;
import com.displaytweaks.setting.ModeSetting;
import com.displaytweaks.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/** Automatically attacks the nearest valid target in range. */
public class KillAura extends Module {
	private final NumberSetting range = add(new NumberSetting("Range", 4.0, 2.0, 6.0, 0.5));
	private final ModeSetting targets = add(new ModeSetting("Targets", 0, "Players", "Hostile Mobs", "Both"));
	private final BoolSetting cooldown = add(new BoolSetting("Respect Attack Cooldown", true));
	private final BoolSetting rotate = add(new BoolSetting("Look At Target", true));

	public KillAura() {
		super("Kill Aura", "Auto-attack nearby targets", Category.PVP);
	}

	private boolean valid(Entity e, ClientPlayerEntity p) {
		if (e == p || !(e instanceof LivingEntity le) || !le.isAlive()) return false;
		boolean player = e instanceof PlayerEntity pe && !pe.isSpectator();
		boolean hostile = e instanceof HostileEntity;
		return switch (targets.get()) {
			case "Players" -> player;
			case "Hostile Mobs" -> hostile;
			default -> player || hostile;
		};
	}

	@Override
	public void onTick(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.world == null || mc.interactionManager == null) return;
		if (mc.currentScreen != null) return;

		Entity best = null;
		double bestDist = Double.MAX_VALUE;
		for (Entity e : mc.world.getEntities()) {
			if (!valid(e, p)) continue;
			double d = p.distanceTo(e);
			if (d <= range.get() && d < bestDist) {
				bestDist = d;
				best = e;
			}
		}
		if (best == null) return;

		if (rotate.get()) {
			Vec3d eye = p.getEyePos();
			Vec3d t = best.getBoundingBox().getCenter();
			double dx = t.x - eye.x, dy = t.y - eye.y, dz = t.z - eye.z;
			double flat = Math.sqrt(dx * dx + dz * dz);
			p.setYaw((float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
			p.setPitch((float) -Math.toDegrees(Math.atan2(dy, flat)));
		}
		if (cooldown.get() && p.getAttackCooldownProgress(0.5f) < 1.0f) return;
		mc.interactionManager.attackEntity(p, best);
		p.swingHand(Hand.MAIN_HAND);
	}
}
