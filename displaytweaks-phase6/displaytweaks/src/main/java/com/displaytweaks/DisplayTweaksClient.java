package com.displaytweaks;

import com.displaytweaks.config.ConfigManager;
import com.displaytweaks.gui.ClickGuiScreen;
import com.displaytweaks.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class DisplayTweaksClient implements ClientModInitializer {
	public static final String MOD_ID = "displaytweaks";

	private static KeyBinding openGui;

	@Override
	public void onInitializeClient() {
		ModuleManager.init();
		if (ConfigManager.exists("autosave")) ConfigManager.load("autosave", false);

		// 1.21.9+ requires a KeyBinding.Category instead of a plain string.
		KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));
		openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key." + MOD_ID + ".open_gui",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				category));

		ClientTickEvents.END_WORLD_TICK.register(world -> ModuleManager.worldTick(MinecraftClient.getInstance()));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openGui.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(new ClickGuiScreen());
				}
			}
			ModuleManager.pollKeys(client);
			if (client.player != null && client.world != null) {
				ModuleManager.tick(client);
			}
		});
	}
}
