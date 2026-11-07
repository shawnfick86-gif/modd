package com.displaytweaks.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** Preset color picker. Left click = next color, right click = previous. */
public class ColorSetting extends Setting {
	public static final String[] NAMES = {"Red", "Orange", "Yellow", "Green", "Cyan", "Blue", "Purple", "Pink", "White"};
	public static final int[] COLORS = {
			0xFFFF4D4D, 0xFFFF9A3D, 0xFFFFE03D, 0xFF3DDC6A, 0xFF3DD6E0,
			0xFF4D8DFF, 0xFFA14DFF, 0xFFFF6FC8, 0xFFFFFFFF};
	private int index;

	public ColorSetting(String name, int defaultIndex) {
		super(name);
		this.index = defaultIndex;
	}

	/** ARGB color. */
	public int get() {
		return COLORS[index];
	}

	public String colorName() {
		return NAMES[index];
	}

	public void cycle(int dir) {
		index = Math.floorMod(index + dir, COLORS.length);
	}

	@Override
	public JsonElement save() {
		return new JsonPrimitive(colorName());
	}

	@Override
	public void load(JsonElement e) {
		String v = e.getAsString();
		for (int i = 0; i < NAMES.length; i++) {
			if (NAMES[i].equals(v)) index = i;
		}
	}
}
