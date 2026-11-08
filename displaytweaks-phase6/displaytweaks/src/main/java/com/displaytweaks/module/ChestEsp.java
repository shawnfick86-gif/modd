package com.displaytweaks.module;

import com.displaytweaks.render.BlockScanner;
import com.displaytweaks.render.EspDraw;
import com.displaytweaks.setting.BoolSetting;
import com.displaytweaks.setting.ColorSetting;
import com.displaytweaks.setting.NumberSetting;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

public class ChestEsp extends EspModule {
	private final BoolSetting chests = add(new BoolSetting("Chests", true));
	private final BoolSetting barrels = add(new BoolSetting("Barrels", true));
	private final BoolSetting shulkers = add(new BoolSetting("Shulker Boxes", true));
	private final BoolSetting enders = add(new BoolSetting("Ender Chests", true));
	private final ColorSetting chestColor = add(new ColorSetting("Chest Color", 1));
	private final ColorSetting barrelColor = add(new ColorSetting("Barrel Color", 2));
	private final ColorSetting shulkerColor = add(new ColorSetting("Shulker Color", 6));
	private final ColorSetting enderColor = add(new ColorSetting("Ender Chest Color", 5));
	private final BoolSetting tracers = add(new BoolSetting("Tracers", false));
	private final BoolSetting filled = add(new BoolSetting("Filled Boxes", false));
	private final NumberSetting range = add(new NumberSetting("Range", 128, 16, 256, 16));

	private record Entry(BlockPos pos, int color) {}

	private volatile List<Entry> found = List.of();

	public ChestEsp() {
		super("Chest ESP", "Outline containers through walls");
	}

	@Override
	protected void rescan(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null) return;
		int r = range.getInt();
		List<Entry> out = new ArrayList<>();
		for (WorldChunk c : BlockScanner.chunks(mc, r)) {
			for (BlockEntity be : c.getBlockEntities().values()) {
				int color;
				if (be instanceof EnderChestBlockEntity) {
					if (!enders.get()) continue;
					color = enderColor.get();
				} else if (be instanceof ShulkerBoxBlockEntity) {
					if (!shulkers.get()) continue;
					color = shulkerColor.get();
				} else if (be instanceof BarrelBlockEntity) {
					if (!barrels.get()) continue;
					color = barrelColor.get();
				} else if (be instanceof ChestBlockEntity) {
					if (!chests.get()) continue;
					color = chestColor.get();
				} else {
					continue;
				}
				BlockPos pos = be.getPos();
				if (BlockScanner.inRange(p, pos, r)) out.add(new Entry(pos, color));
			}
		}
		found = out;
	}

	@Override
	protected void emit(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null) return;
		Vec3d start = EspDraw.tracerStart(p);
		for (Entry e : found) {
			EspDraw.box(e.pos(), e.color(), filled.get());
			if (tracers.get()) EspDraw.line(start, Vec3d.ofCenter(e.pos()), e.color());
		}
	}

	@Override
	protected void onDisable() {
		found = List.of();
	}

	@Override
	public void onSettingChanged() {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (isEnabled() && mc.player != null) rescan(mc);
	}
}
