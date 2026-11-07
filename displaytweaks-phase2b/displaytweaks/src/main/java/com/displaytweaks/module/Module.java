package com.displaytweaks.module;

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
	private final List<Setting> settings = new ArrayList<>();
	private boolean enabled;

	public Module(String name, String description, Category category) {
		this.name = name;
		this.description = description;
		this.category = category;
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

	/** Called every client tick while in a world and enabled. */
	public void onTick(MinecraftClient mc) {}
}
