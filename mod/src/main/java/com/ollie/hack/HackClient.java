package com.ollie.hack;
import net.fabricmc.api.ClientModInitializer; import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper; import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil; import org.lwjgl.glfw.GLFW;
public class HackClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        ModuleManager.init();
        registerKeybind("KillAura", GLFW.GLFW_KEY_K); registerKeybind("Fly", GLFW.GLFW_KEY_F);
        registerKeybind("ESP", GLFW.GLFW_KEY_E); registerKeybind("NoFall", GLFW.GLFW_KEY_N);
        registerKeybind("Nuker", GLFW.GLFW_KEY_X);
        ClientTickEvents.END_CLIENT_TICK.register(client -> ModuleManager.onTick());
    }
    private void registerKeybind(String moduleName, int key) {
        KeyBinding kb = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.olliehack." + moduleName.toLowerCase(), InputUtil.Type.KEYSYM, key, "category.olliehack"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> { while (kb.wasPressed()) { Module m = ModuleManager.getModule(moduleName); if (m != null) m.toggle(); } });
    }
}
