package com.displaytweaks.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** Pick one of several named options. Left click = next, right click = previous. */
public class ModeSetting extends Setting {
	public final String[] modes;
	private int index;

	public ModeSetting(String name, int defaultIndex, String... modes) {
		super(name);
		this.modes = modes;
		this.index = defaultIndex;
	}

	public String get() {
		return modes[index];
	}

	public boolean is(String mode) {
		return modes[index].equalsIgnoreCase(mode);
	}

	public void cycle(int dir) {
		index = Math.floorMod(index + dir, modes.length);
	}

	@Override
	public JsonElement save() {
		return new JsonPrimitive(get());
	}

	@Override
	public void load(JsonElement e) {
		String v = e.getAsString();
		for (int i = 0; i < modes.length; i++) {
			if (modes[i].equals(v)) index = i;
		}
	}
}
