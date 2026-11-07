package com.displaytweaks.module;

public enum Category {
	RENDER("Render"),
	MOVEMENT("Movement"),
	PVP("PvP"),
	PLAYER("Player"),
	WORLD("World"),
	BUILDING("Building"),
	MEDIA("Media"),
	CONFIGS("Configs");

	public final String label;

	Category(String label) {
		this.label = label;
	}
}
