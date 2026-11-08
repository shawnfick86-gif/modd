package com.displaytweaks.module;

import com.displaytweaks.render.BlockScanner;
import com.displaytweaks.render.EspDraw;
import com.displaytweaks.setting.BoolSetting;
import com.displaytweaks.setting.ColorSetting;
import com.displaytweaks.setting.ModeSetting;
import com.displaytweaks.setting.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.function.Predicate;

/** Outlines every block of one chosen type within range. */
public class BlockEsp extends EspModule {
	private final ModeSetting block = add(new ModeSetting("Block", 0,
			"Diamond Ore", "Ancient Debris", "Spawner", "Beacon", "Sugar Cane", "Bamboo",
			"Sea Pickle", "Bed", "Hopper", "Lodestone"));
	private final ColorSetting color = add(new ColorSetting("Color", 3));
	private final BoolSetting tracers = add(new BoolSetting("Tracers", false));
	private final BoolSetting filled = add(new BoolSetting("Filled Boxes", false));
	private final NumberSetting range = add(new NumberSetting("Range", 64, 8, 256, 8));

	private volatile List<BlockScanner.Hit> found = List.of();

	public BlockEsp() {
		super("Block ESP", "Outline any block you pick");
	}

	private Predicate<BlockState> predicate() {
		return switch (block.get()) {
			case "Diamond Ore" -> s -> s.isOf(Blocks.DIAMOND_ORE) || s.isOf(Blocks.DEEPSLATE_DIAMOND_ORE);
			case "Ancient Debris" -> s -> s.isOf(Blocks.ANCIENT_DEBRIS);
			case "Spawner" -> s -> s.isOf(Blocks.SPAWNER);
			case "Beacon" -> s -> s.isOf(Blocks.BEACON);
			case "Sugar Cane" -> s -> s.isOf(Blocks.SUGAR_CANE);
			case "Bamboo" -> s -> s.isOf(Blocks.BAMBOO);
			case "Sea Pickle" -> s -> s.isOf(Blocks.SEA_PICKLE);
			case "Bed" -> s -> s.getBlock() instanceof net.minecraft.block.BedBlock;
			case "Hopper" -> s -> s.isOf(Blocks.HOPPER);
			default -> s -> s.isOf(Blocks.LODESTONE);
		};
	}

	@Override
	protected void rescan(MinecraftClient mc) {
		found = BlockScanner.scan(mc, range.getInt(), predicate(), 400);
	}

	@Override
	protected void emit(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null) return;
		Vec3d start = EspDraw.tracerStart(p);
		for (BlockScanner.Hit h : found) {
			EspDraw.box(h.pos(), color.get(), filled.get());
			if (tracers.get()) EspDraw.line(start, Vec3d.ofCenter(h.pos()), color.get());
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
