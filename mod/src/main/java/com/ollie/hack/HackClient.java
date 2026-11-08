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
        try {
            // 1. Initialize Modules
            ModuleManager.init();
            System.out.println("--- OLLIEHACK: Modules loaded ---");

            // 2. Register Keybind
            guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.olliehack.gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.olliehack"
            ));
            System.out.println("--- OLLIEHACK: Keybind registered ---");

            // 3. Tick Event
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                ModuleManager.onTick();
                if (guiKey != null) {
                    while (guiKey.wasPressed()) {
                        client.setScreen(new ClickGUI());
                    }
                }
            });
            System.out.println("--- OLLIEHACK: Tick event registered ---");
            
        } catch (Exception e) {
            System.err.println("--- OLLIEHACK: CRASHED DURING INIT ---");
            e.printStackTrace();
        }
    }
}
