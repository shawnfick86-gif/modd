package com.displaytweaks.module;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
	private static final List<Module> MODULES = new ArrayList<>();

	public static void init() {
		// Phase 1: placeholders so the GUI is fully usable. Later phases swap
		// these for real implementations (same name + category).
		add(new Module("Xray", "See chosen blocks through terrain", Category.RENDER));
		add(new Module("Chest ESP", "Outline containers", Category.RENDER));
		add(new Module("Block ESP", "Outline any block you pick", Category.RENDER));
		add(new Module("Kelp ESP", "Color kelp by growth stage", Category.RENDER));
		add(new Fullbright());
		add(new Module("Freecam", "Detach the camera from your body", Category.RENDER));
		add(new Module("Logout Spots", "Mark where players disappeared", Category.RENDER));

		add(new Flight());
		add(new Module("Speed", "Move faster", Category.MOVEMENT));
		add(new Sprint());
		add(new Module("No Fall", "Cancel fall damage", Category.MOVEMENT));

		add(new Module("Step", "Walk up full blocks", Category.PLAYER));
		add(new Module("Reach", "Longer interaction range", Category.PLAYER));

		add(new Module("Time Changer", "Client-side time of day", Category.WORLD));
		add(new Module("Zoom", "Hold to zoom", Category.WORLD));

		add(new Module("Scaffold", "Place blocks under your feet", Category.BUILDING));
		add(new Module("Shape Builder", "Auto-build walls, boxes, pillars", Category.BUILDING));

		add(new Module("Music Player", "Play audio from a URL (local only)", Category.MEDIA));
	}

	public static void add(Module module) {
		MODULES.add(module);
	}

	public static List<Module> all() {
		return MODULES;
	}

	public static List<Module> in(Category category) {
		List<Module> out = new ArrayList<>();
		for (Module m : MODULES) {
			if (m.category == category) out.add(m);
		}
		return out;
	}

	public static void tick(MinecraftClient mc) {
		for (Module m : MODULES) {
			if (m.isEnabled()) m.onTick(mc);
		}
	}
}
