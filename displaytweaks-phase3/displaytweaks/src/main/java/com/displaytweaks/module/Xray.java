package com.displaytweaks.module;

import com.displaytweaks.setting.BoolSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hides every block face except those of the blocks you choose. The actual
 * hiding happens in BlockModelRendererMixin, which asks isVisible().
 */
public class Xray extends Module {
	public static Xray INSTANCE;

	private record Entry(BoolSetting setting, Block[] blocks) {}

	private final List<Entry> entries = new ArrayList<>();
	private volatile Set<Block> visible = Set.of();

	public Xray() {
		super("Xray", "See chosen blocks through terrain", Category.RENDER);
		INSTANCE = this;
		ore("Diamond", true, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE);
		ore("Ancient Debris", true, Blocks.ANCIENT_DEBRIS);
		ore("Gold", true, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE,
				Blocks.GOLD_BLOCK, Blocks.RAW_GOLD_BLOCK);
		ore("Iron", false, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE);
		ore("Coal", false, Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE);
		ore("Copper", false, Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE);
		ore("Redstone", false, Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE);
		ore("Lapis", false, Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE);
		ore("Emerald", true, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE);
		ore("Nether Quartz", false, Blocks.NETHER_QUARTZ_ORE);
		ore("Spawners", true, Blocks.SPAWNER);
		rebuildCache();
	}

	private void ore(String name, boolean def, Block... blocks) {
		entries.add(new Entry(add(new BoolSetting(name, def)), blocks));
	}

	private void rebuildCache() {
		Set<Block> set = new HashSet<>();
		for (Entry e : entries) {
			if (e.setting().get()) {
				for (Block b : e.blocks()) set.add(b);
			}
		}
		visible = Set.copyOf(set);
	}

	/** Called from the render thread(s) by the mixin. */
	public boolean isVisible(Block block) {
		return visible.contains(block);
	}

	private static void reloadWorld() {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.worldRenderer != null && mc.world != null) {
			mc.worldRenderer.reload();
		}
	}

	@Override
	protected void onEnable() {
		rebuildCache();
		reloadWorld();
	}

	@Override
	protected void onDisable() {
		reloadWorld();
	}

	@Override
	public void onSettingChanged() {
		rebuildCache();
		if (isEnabled()) reloadWorld();
	}
}
