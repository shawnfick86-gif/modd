package com.ollie.hack;

import com.ollie.hack.gui.ClickGUI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class HackClient implements ClientModInitializer {
    private static KeyBinding guiKey;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();

        // Register GUI Key (Right Shift)
        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.olliehack.gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "category.olliehack"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ModuleManager.onTick();

            // Open ClickGUI when Right Shift is pressed
            while (guiKey.wasPressed()) {
                client.setScreen(new ClickGUI());
            }
        });
    }
}
