package com.displaytweaks.setting;

/** A button that runs something when clicked. Not saved to configs. */
public class ActionSetting extends Setting {
	private final Runnable action;

	public ActionSetting(String name, Runnable action) {
		super(name);
		this.action = action;
	}

	public void run() {
		action.run();
	}
}
