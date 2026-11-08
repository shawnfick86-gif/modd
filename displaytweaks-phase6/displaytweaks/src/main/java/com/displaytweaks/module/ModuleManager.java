package com.displaytweaks.module;

import com.displaytweaks.setting.*;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

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
		add(new Xray());
		add(new ChestEsp());
		add(new BlockEsp());
		add(new KelpEsp());
		add(new ItemEsp());
		add(new Tracers());
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

		add(m("Name Tags", "Bigger, clearer name tags", Category.RENDER,
				b("Show Health", true), b("Show Distance", true), num("Scale", 1.0, 0.5, 3.0, 0.1)));
		add(m("HUD", "On-screen module list and info", Category.RENDER,
				b("Module List", true), b("Coordinates", true), b("FPS", false),
				col("Accent Color", 6)));
		add(m("Animations", "Tweak hand and swing animations", Category.RENDER,
				num("Swing Speed", 1.0, 0.5, 2.0, 0.1), b("Smooth Sneak", true)));
		add(m("Free Look", "Hold a key to look around without turning", Category.RENDER,
				num("Sensitivity", 1.0, 0.2, 3.0, 0.1), b("Third Person While Held", true)));

		// ---------------- Movement ----------------
		add(new Flight());
		add(new Speed());
		add(new Sprint());
		add(new SafeWalk());
		add(new AutoWalk());
		add(new AutoJump());
		add(m("No Fall", "Cancel fall damage", Category.MOVEMENT,
				mode("Mode", 0, "Packet", "Always On Ground"),
				num("Min Fall Distance", 2.5, 0.0, 10.0, 0.5)));

		// ---------------- PvP ----------------
		add(new CrystalSpam());
		add(new AutoTotem());
		add(new KillAura());

		// ---------------- Player ----------------
		add(new Step());
		add(new AutoEat());
		add(new AutoRespawn());
		add(m("Fast Place", "Remove the block placing delay", Category.PLAYER,
				num("Delay (ticks)", 0, 0, 4, 1),
				b("Blocks Only", true)));

		// ---------------- World ----------------
		add(m("Time Changer", "Client-side time of day", Category.WORLD,
				num("Time", 6000, 0, 24000, 500),
				b("Freeze Time", true)));
		add(m("Zoom", "Hold to zoom", Category.WORLD,
				num("Zoom Level", 4, 2, 10, 1),
				b("Smooth Zoom", true), b("Scroll To Adjust", true), b("Cinematic Camera", true)));

		// ---------------- Building ----------------
		add(new Scaffold());
		add(m("Shape Builder", "Auto-build walls, boxes, pillars", Category.BUILDING,
				mode("Shape", 0, "Wall", "Platform", "Box", "Pillar", "Sphere"),
				num("Size", 5, 1, 20, 1),
				num("Delay (ticks)", 2, 0, 10, 1),
				b("Hollow", false), b("Swing Hand", false)));

		// ---------------- Media ----------------
		add(new MusicPlayer());
		add(new AntiAfk());
		add(new DeathCoords());
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

	/** Toggle modules whose keybind was just pressed (only when no screen is open). */
	public static void pollKeys(MinecraftClient mc) {
		if (mc.getWindow() == null) return;
		long handle = mc.getWindow().getHandle();
		for (Module mod : MODULES) {
			int key = mod.bind.get();
			if (key < 0) continue;
			boolean down = GLFW.glfwGetKey(handle, key) == GLFW.GLFW_PRESS;
			if (down && !mod.keyDown && mc.currentScreen == null) mod.toggle();
			mod.keyDown = down;
		}
	}

	public static void worldTick(MinecraftClient mc) {
		for (Module mod : MODULES) {
			if (mod.isEnabled()) mod.onWorldTick(mc);
		}
	}

	public static void tick(MinecraftClient mc) {
		for (Module mod : MODULES) {
			if (mod.isEnabled()) mod.onTick(mc);
		}
	}
}
