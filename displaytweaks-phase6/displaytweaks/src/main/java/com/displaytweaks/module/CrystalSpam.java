package com.displaytweaks.module;

import com.displaytweaks.setting.BoolSetting;
import com.displaytweaks.setting.ModeSetting;
import com.displaytweaks.setting.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.List;

/**
 * Hold right click with crystals in hand: places a crystal on the obsidian or
 * bedrock you're looking at, and breaks crystals as fast as the delays allow.
 */
public class CrystalSpam extends Module {
	private final BoolSetting needUse = add(new BoolSetting("Only While Holding Right Click", true));
	private final NumberSetting placeDelay = add(new NumberSetting("Place Delay (ticks)", 1, 0, 10, 1));
	private final NumberSetting breakDelay = add(new NumberSetting("Break Delay (ticks)", 1, 0, 10, 1));
	private final ModeSetting breakMode = add(new ModeSetting("Break", 0, "Crosshair Only", "Nearest In Range"));
	private final NumberSetting range = add(new NumberSetting("Break Range", 4.5, 2.0, 6.0, 0.5));
	private final BoolSetting swing = add(new BoolSetting("Swing Hand", true));

	private int placeTimer, breakTimer;

	public CrystalSpam() {
		super("Crystal Spam", "Rapidly place and break end crystals", Category.PVP);
	}

	@Override
	public void onTick(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.world == null || mc.interactionManager == null) return;
		if (mc.currentScreen != null) return;
		if (needUse.get() && !mc.options.useKey.isPressed()) return;

		placeTimer--;
		breakTimer--;

		// ---- break ----
		if (breakTimer <= 0) {
			Entity target = null;
			if (breakMode.is("Crosshair Only")) {
				if (mc.crosshairTarget instanceof EntityHitResult ehr && ehr.getEntity() instanceof EndCrystalEntity) {
					target = ehr.getEntity();
				}
			} else {
				Box area = p.getBoundingBox().expand(range.get());
				List<EndCrystalEntity> list = mc.world.getEntitiesByClass(EndCrystalEntity.class, area, e -> true);
				double best = Double.MAX_VALUE;
				for (EndCrystalEntity c : list) {
					double d = p.squaredDistanceTo(c);
					if (d < best) {
						best = d;
						target = c;
					}
				}
			}
			if (target != null && p.distanceTo(target) <= range.get()) {
				mc.interactionManager.attackEntity(p, target);
				if (swing.get()) p.swingHand(Hand.MAIN_HAND);
				breakTimer = placeDelayValue(breakDelay);
			}
		}

		// ---- place ----
		if (placeTimer <= 0 && p.getMainHandStack().isOf(Items.END_CRYSTAL)
				&& mc.crosshairTarget instanceof BlockHitResult bhr
				&& bhr.getType() == HitResult.Type.BLOCK
				&& bhr.getSide() == Direction.UP) {
			BlockPos pos = bhr.getBlockPos();
			BlockState st = mc.world.getBlockState(pos);
			if ((st.isOf(Blocks.OBSIDIAN) || st.isOf(Blocks.BEDROCK))
					&& mc.world.isAir(pos.up()) && mc.world.isAir(pos.up(2))
					&& mc.world.getEntitiesByClass(EndCrystalEntity.class, new Box(pos.up()), e -> true).isEmpty()) {
				mc.interactionManager.interactBlock(p, Hand.MAIN_HAND, bhr);
				if (swing.get()) p.swingHand(Hand.MAIN_HAND);
				placeTimer = placeDelayValue(placeDelay);
			}
		}
	}

	private static int placeDelayValue(NumberSetting s) {
		return s.getInt();
	}
}
