package com.displaytweaks.module;

import com.displaytweaks.render.EspDraw;
import com.displaytweaks.setting.ColorSetting;
import com.displaytweaks.setting.ModeSetting;
import com.displaytweaks.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** Draws a line from your view to nearby players, items, or all living things. */
public class Tracers extends EspModule {
	private final ModeSetting targets = add(new ModeSetting("Targets", 0, "Players", "Items", "All Entities"));
	private final ColorSetting color = add(new ColorSetting("Color", 5));
	private final NumberSetting range = add(new NumberSetting("Range", 128, 16, 256, 16));

	public Tracers() {
		super("Tracers", "Lines to entities");
	}

	@Override
	protected void emit(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.world == null) return;
		Vec3d start = EspDraw.tracerStart(p);
		double r2 = range.get() * range.get();

		if (targets.is("Items")) {
			Box area = p.getBoundingBox().expand(range.get());
			for (ItemEntity item : mc.world.getEntitiesByClass(ItemEntity.class, area, e -> true)) {
				EspDraw.line(start, item.getBoundingBox().getCenter(), color.get());
			}
			return;
		}

		for (Entity e : mc.world.getEntities()) {
			if (e == p || !(e instanceof LivingEntity)) continue;
			if (targets.is("Players") && !(e instanceof net.minecraft.entity.player.PlayerEntity)) continue;
			if (p.squaredDistanceTo(e) > r2) continue;
			EspDraw.line(start, e.getBoundingBox().getCenter(), color.get());
		}
	}
}
