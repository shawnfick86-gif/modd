package com.displaytweaks.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class NumberSetting extends Setting {
	public final double min, max, step;
	private double value;

	public NumberSetting(String name, double value, double min, double max, double step) {
		super(name);
		this.min = min;
		this.max = max;
		this.step = step;
		this.value = value;
	}

	public double get() {
		return value;
	}

	public float getFloat() {
		return (float) value;
	}

	public int getInt() {
		return (int) Math.round(value);
	}

	public void set(double v) {
		v = Math.round(v / step) * step;
		value = Math.max(min, Math.min(max, v));
	}

	/** 0..1 position of the current value, for drawing the slider. */
	public double fraction() {
		return (value - min) / (max - min);
	}

	@Override
	public JsonElement save() {
		return new JsonPrimitive(value);
	}

	@Override
	public void load(JsonElement e) {
		set(e.getAsDouble());
	}
}
