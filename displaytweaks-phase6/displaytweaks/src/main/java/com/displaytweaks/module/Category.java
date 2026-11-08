package com.displaytweaks.module;

public enum Category {
	PVP("Combat", "Fighting tools"),
	MOVEMENT("Movement", "Get around"),
	PLAYER("Player", "Self & placing"),
	RENDER("Visuals", "HUD & world"),
	WORLD("World", "Environment"),
	BUILDING("Building", "Place blocks"),
	MEDIA("Misc", "Utility"),
	CONFIGS("Settings", "Client options");

	public final String label;
	public final String subtitle;

	Category(String label, String subtitle) {
		this.label = label;
		this.subtitle = subtitle;
	}
}
