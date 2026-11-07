package com.displaytweaks.config;

import com.displaytweaks.module.Module;
import com.displaytweaks.module.ModuleManager;
import com.displaytweaks.setting.Setting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Saves/loads every module's settings and keybind to .minecraft/config/displaytweaks/. */
public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private ConfigManager() {}

	private static Path dir() {
		return FabricLoader.getInstance().getConfigDir().resolve("displaytweaks");
	}

	private static Path file(String name) {
		return dir().resolve(name + ".json");
	}

	public static boolean exists(String name) {
		return Files.exists(file(name));
	}

	public static String save(String name, boolean includeEnabled) {
		try {
			JsonObject root = new JsonObject();
			for (Module m : ModuleManager.all()) {
				JsonObject mo = new JsonObject();
				if (includeEnabled) mo.addProperty("enabled", m.isEnabled());
				JsonObject so = new JsonObject();
				for (Setting s : m.settings()) {
					JsonElement e = s.save();
					if (e != null) so.add(s.name, e);
				}
				mo.add("settings", so);
				root.add(m.name, mo);
			}
			Files.createDirectories(dir());
			Files.writeString(file(name), GSON.toJson(root));
			return "Saved.";
		} catch (Exception e) {
			return "Save failed: " + e.getMessage();
		}
	}

	public static String load(String name, boolean applyEnabled) {
		try {
			Path f = file(name);
			if (!Files.exists(f)) return "Nothing saved there yet.";
			JsonObject root = JsonParser.parseString(Files.readString(f)).getAsJsonObject();
			for (Module m : ModuleManager.all()) {
				JsonElement me = root.get(m.name);
				if (me == null || !me.isJsonObject()) continue;
				JsonObject mo = me.getAsJsonObject();
				JsonElement se = mo.get("settings");
				if (se != null && se.isJsonObject()) {
					JsonObject so = se.getAsJsonObject();
					for (Setting s : m.settings()) {
						JsonElement e = so.get(s.name);
						if (e == null) continue;
						try {
							s.load(e);
						} catch (Exception ignored) {
							// bad value in file: keep the default
						}
					}
				}
				m.onSettingChanged();
				if (applyEnabled && mo.has("enabled")) m.setEnabled(mo.get("enabled").getAsBoolean());
			}
			return "Loaded.";
		} catch (Exception e) {
			return "Load failed: " + e.getMessage();
		}
	}
}
