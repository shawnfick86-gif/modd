package com.displaytweaks.module;

public enum Category {
	RENDER("Render"),
	MOVEMENT("Movement"),
	PLAYER("Player"),
	WORLD("World"),
	BUILDING("Building"),
	MEDIA("Media");

	public final String label;

	Category(String label) {
		this.label = label;
	}
}
