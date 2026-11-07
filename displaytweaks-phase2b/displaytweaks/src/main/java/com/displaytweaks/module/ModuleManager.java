package com.displaytweaks.module;

import com.displaytweaks.setting.*;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
	private static final List<Module> MODULES = new ArrayList<>();

	// ---- tiny helpers so the list below stays readable ----
	private static BoolSetting b(String n, boolean v) { return new BoolSetting(n, v); }
	private static NumberSetting num(String n, double v, double min, double max, double step) { return new NumberSetting(n, v, min, max, step); }
	private static ModeSetting mode(String n, int i, String... m) { return new ModeSetting(n, i, m); }
	private static ColorSetting col(String n, int i) { return new ColorSetting(n, i); }

	private static Module m(String name, String desc, Category cat, Setting... settings) {
		Module mod = new Module(name, desc, cat);
		for (Setting s : settings) mod.add(s);
		return mod;
	}

	public static void init() {
		// NOTE: modules built from m(...) are still placeholders. Their settings
		// exist and can be changed in the GUI, but only take effect once the
		// module is implemented in a later phase.

		// ---------------- Render ----------------
		add(m("Xray", "See chosen blocks through terrain", Category.RENDER,
				num("Opacity %", 0, 0, 100, 5),
				b("Diamond", true), b("Ancient Debris", true), b("Gold", true),
				b("Iron", false), b("Coal", false), b("Copper", false),
				b("Redstone", false), b("Lapis", false), b("Emerald", true),
				b("Spawners", true)));
		add(m("Chest ESP", "Outline containers", Category.RENDER,
				b("Chests", true), b("Barrels", true), b("Shulker Boxes", true), b("Ender Chests", true),
				col("Chest Color", 1), col("Barrel Color", 2), col("Shulker Color", 6), col("Ender Chest Color", 5),
				b("Tracers", false), b("Filled Boxes", false),
				num("Range", 128, 16, 256, 16)));
		add(m("Block ESP", "Outline any block you pick", Category.RENDER,
				mode("Block", 0, "Diamond Ore", "Ancient Debris", "Spawner", "Beacon", "Sugar Cane", "Bamboo", "Sea Pickle", "Bed", "Hopper", "Lodestone"),
				col("Color", 3),
				b("Tracers", false), b("Filled Boxes", false),
				num("Range", 64, 8, 256, 8)));
		add(m("Kelp ESP", "Color kelp by growth stage", Category.RENDER,
				col("Fully Grown Color", 3), col("Growing Color", 2),
				num("Grown At Age", 25, 1, 25, 1),
				b("Show Growing", true), b("Tracers", false),
				num("Range", 64, 8, 256, 8)));
		add(new Fullbright());
		add(m("Freecam", "Detach the camera from your body", Category.RENDER,
				num("Speed", 1.0, 0.1, 5.0, 0.1),
				b("Allow Interaction", false), b("Show Body Marker", true)));
		add(m("Logout Spots", "Mark where players disappeared", Category.RENDER,
				b("Show Names", true), b("Show Time Since", true), b("Show Distance", true),
				col("Marker Color", 0),
				num("Max Spots", 20, 1, 50, 1),
				num("Forget After (min)", 30, 1, 120, 1),
				b("Tracers", false)));

		// ---------------- Movement ----------------
		add(new Flight());
		add(m("Speed", "Move faster", Category.MOVEMENT,
				mode("Mode", 0, "Vanilla", "Strafe"),
				num("Speed", 1.5, 1.0, 5.0, 0.1),
				b("Only On Ground", false), b("Only While Sprinting", false)));
		add(new Sprint());
		add(m("No Fall", "Cancel fall damage", Category.MOVEMENT,
				mode("Mode", 0, "Packet", "Always On Ground"),
				num("Min Fall Distance", 2.5, 0.0, 10.0, 0.5)));

		// ---------------- Player ----------------
		add(m("Step", "Walk up full blocks", Category.PLAYER,
				num("Height", 1.0, 1.0, 3.0, 0.5),
				b("Only While Sprinting", false), b("Not While Sneaking", true)));
		add(m("Reach", "Longer interaction range", Category.PLAYER,
				num("Block Reach", 4.5, 3.0, 6.0, 0.5),
				num("Entity Reach", 3.0, 3.0, 6.0, 0.5)));

		// ---------------- World ----------------
		add(m("Time Changer", "Client-side time of day", Category.WORLD,
				num("Time", 6000, 0, 24000, 500),
				b("Freeze Time", true)));
		add(m("Zoom", "Hold to zoom", Category.WORLD,
				num("Zoom Level", 4, 2, 10, 1),
				b("Smooth Zoom", true), b("Scroll To Adjust", true), b("Cinematic Camera", true)));

		// ---------------- Building ----------------
		add(m("Scaffold", "Place blocks under your feet", Category.BUILDING,
				num("Range", 1, 1, 5, 1),
				num("Delay (ticks)", 1, 0, 10, 1),
				mode("Block Source", 0, "Held Block", "Any Hotbar Block"),
				b("Only While Moving", false), b("Tower (hold Space)", true),
				b("Swing Hand", false), b("Rotate Toward Block", false)));
		add(m("Shape Builder", "Auto-build walls, boxes, pillars", Category.BUILDING,
				mode("Shape", 0, "Wall", "Platform", "Box", "Pillar", "Sphere"),
				num("Size", 5, 1, 20, 1),
				num("Delay (ticks)", 2, 0, 10, 1),
				b("Hollow", false), b("Swing Hand", false)));

		// ---------------- Media ----------------
		add(m("Music Player", "Play audio from a URL (local only)", Category.MEDIA,
				num("Volume %", 50, 0, 100, 5),
				b("Loop", false), b("Fade In/Out", true),
				b("Lower Volume In Chat", false)));
	}

	public static void add(Module module) {
		MODULES.add(module);
	}

	public static List<Module> all() {
		return MODULES;
	}

	public static List<Module> in(Category category) {
		List<Module> out = new ArrayList<>();
		for (Module mod : MODULES) {
			if (mod.category == category) out.add(mod);
		}
		return out;
	}

	public static void tick(MinecraftClient mc) {
		for (Module mod : MODULES) {
			if (mod.isEnabled()) mod.onTick(mc);
		}
	}
}
