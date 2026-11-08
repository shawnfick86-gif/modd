package com.displaytweaks.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** A GLFW key code, -1 = unbound. Every module has one. */
public class KeybindSetting extends Setting {
	private int key = -1;
	public boolean listening;

	public KeybindSetting(String name) {
		super(name);
	}

	public int get() {
		return key;
	}

	public void set(int key) {
		this.key = key;
	}

	@Override
	public JsonElement save() {
		return new JsonPrimitive(key);
	}

	@Override
	public void load(JsonElement e) {
		key = e.getAsInt();
	}
}
