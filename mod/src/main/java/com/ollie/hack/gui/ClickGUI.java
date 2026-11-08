package com.ollie.hack.gui;

import com.ollie.hack.Module;
import com.ollie.hack.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ClickGUI extends Screen {
    public ClickGUI() {
        super(Text.literal("ClickGUI"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw a dark transparent background
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Title
        context.drawTextWithShadow(this.textRenderer, "OllieHack v1.0", 10, 10, 0xFF00FF00);

        int y = 30;
        for (Module m : ModuleManager.modules) {
            // Draw module background box
            boolean isHovered = mouseX >= 10 && mouseX <= 110 && mouseY >= y && mouseY <= y + 15;
            int bgColor = isHovered ? 0xFF333333 : 0xFF000000;
            context.fill(10, y, 110, y + 15, bgColor);

            // Draw module text (Green if ON, White if OFF)
            int textColor = m.isEnabled() ? 0xFF00FF00 : 0xFFFFFFFF;
            context.drawTextWithShadow(this.textRenderer, m.getName(), 15, y + 4, textColor);

            y += 18;
        }
        
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            int y = 30;
            for (Module m : ModuleManager.modules) {
                // Check if mouse is inside the button bounds
                if (mouseX >= 10 && mouseX <= 110 && mouseY >= y && mouseY <= y + 15) {
                    m.toggle();
                    return true;
                }
                y += 18;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false; // Allows game to run in background
    }
}
