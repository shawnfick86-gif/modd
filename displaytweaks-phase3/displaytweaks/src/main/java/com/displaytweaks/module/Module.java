package com.displaytweaks.module;

import com.displaytweaks.setting.KeybindSetting;
import com.displaytweaks.setting.Setting;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for every feature. To add a feature: extend this, add settings
 * with add(...), override the hooks you need, register in ModuleManager.init().
 */
public class Module {
	public final String name;
	public final String description;
	public final Category category;
	/** Every module gets a keybind as its first setting. */
	public final KeybindSetting bind = new KeybindSetting("Keybind");
	/** Used by ModuleManager for key edge-detection. */
	public boolean keyDown;

	private final List<Setting> settings = new ArrayList<>();
	private boolean enabled;

	public Module(String name, String description, Category category) {
		this.name = name;
		this.description = description;
		this.category = category;
		settings.add(bind);
	}

	public <T extends Setting> T add(T setting) {
		settings.add(setting);
		return setting;
	}

	public List<Setting> settings() {
		return settings;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void toggle() {
		setEnabled(!enabled);
	}

	public void setEnabled(boolean value) {
		if (value == enabled) return;
		enabled = value;
		if (value) onEnable(); else onDisable();
	}

	protected void onEnable() {}

	protected void onDisable() {}

	/** Called after the GUI or a config load changes one of this module's settings. */
	public void onSettingChanged() {}

	/** Called every client tick while in a world and enabled. */
	public void onTick(MinecraftClient mc) {}
}
