package com.displaytweaks.setting;

import com.google.gson.JsonElement;

public abstract class Setting {
	public final String name;

	protected Setting(String name) {
		this.name = name;
	}

	/** Value for the config file, or null if this setting isn't saved. */
	public JsonElement save() {
		return null;
	}

	public void load(JsonElement e) {}
}
