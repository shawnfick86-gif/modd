package com.displaytweaks.module;

import com.displaytweaks.render.BlockScanner;
import com.displaytweaks.render.EspDraw;
import com.displaytweaks.setting.BoolSetting;
import com.displaytweaks.setting.ColorSetting;
import com.displaytweaks.setting.NumberSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Marks the growing tip of each kelp stalk. A tip's age counts up to 25; at 25
 * it has stopped growing, so fully grown stalks get a different color.
 */
public class KelpEsp extends EspModule {
	private final ColorSetting grownColor = add(new ColorSetting("Fully Grown Color", 3));
	private final ColorSetting growingColor = add(new ColorSetting("Growing Color", 2));
	private final NumberSetting grownAt = add(new NumberSetting("Grown At Age", 25, 1, 25, 1));
	private final BoolSetting showGrowing = add(new BoolSetting("Show Growing", true));
	private final BoolSetting tracers = add(new BoolSetting("Tracers", false));
	private final NumberSetting range = add(new NumberSetting("Range", 64, 8, 256, 8));

	private volatile List<BlockScanner.Hit> found = List.of();

	public KelpEsp() {
		super("Kelp ESP", "Color kelp by growth stage");
	}

	@Override
	protected void rescan(MinecraftClient mc) {
		found = BlockScanner.scan(mc, range.getInt(), s -> s.isOf(Blocks.KELP), 600);
	}

	@Override
	protected void emit(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null) return;
		Vec3d start = EspDraw.tracerStart(p);
		for (BlockScanner.Hit h : found) {
			int age = h.state().get(Properties.AGE_25);
			boolean grown = age >= grownAt.getInt();
			if (!grown && !showGrowing.get()) continue;
			int color = grown ? grownColor.get() : growingColor.get();
			EspDraw.box(h.pos(), color, true);
			if (tracers.get()) EspDraw.line(start, Vec3d.ofCenter(h.pos()), color);
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
