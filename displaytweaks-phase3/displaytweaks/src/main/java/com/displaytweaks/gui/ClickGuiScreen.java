package com.displaytweaks.gui;

import com.displaytweaks.config.ConfigManager;
import com.displaytweaks.module.Category;
import com.displaytweaks.module.Module;
import com.displaytweaks.module.ModuleManager;
import com.displaytweaks.setting.ActionSetting;
import com.displaytweaks.setting.BoolSetting;
import com.displaytweaks.setting.ColorSetting;
import com.displaytweaks.setting.KeybindSetting;
import com.displaytweaks.setting.ModeSetting;
import com.displaytweaks.setting.NumberSetting;
import com.displaytweaks.setting.Setting;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Tabs down the left, module buttons in a grid on the right.
 * Left click toggles a module. Right click opens its settings page.
 * The Configs tab saves/loads profiles.
 */
public class ClickGuiScreen extends Screen {
	private static final int W = 440, H = 270;
	private static final int TAB_W = 100, TAB_H = 24, TAB_GAP = 4;
	private static final int PAD = 8, BTN_H = 24, GAP = 6, TITLE_H = 26;
	private static final int VISIBLE_ROWS = 6;
	private static final int SLOTS = 5, CFG_BTN_W = 70;

	private static final int COL_DIM = 0x90000000;
	private static final int COL_PANEL = 0xF0181A20;
	private static final int COL_SIDEBAR = 0xF00F1116;
	private static final int COL_BTN = 0xFF262A33;
	private static final int COL_BTN_HOVER = 0xFF323744;
	private static final int COL_ON = 0xFF2E8B57;
	private static final int COL_ON_HOVER = 0xFF3AA568;
	private static final int COL_ACCENT = 0xFF5B8DEF;
	private static final int COL_TEXT = 0xFFFFFFFF;
	private static final int COL_TEXT_DIM = 0xFF9AA0AE;

	private static Category selected = Category.RENDER;
	private Module settingsModule = null;
	private Module draggingOwner = null;
	private NumberSetting dragging = null;
	private KeybindSetting binding = null;
	private int settingScroll = 0;
	private String configStatus = "";
	private final boolean[] slotExists = new boolean[SLOTS];
	private int left, top;

	public ClickGuiScreen() {
		super(Text.literal("Display Tweaks"));
	}

	@Override
	protected void init() {
		left = (width - W) / 2;
		top = (height - H) / 2;
		refreshSlots();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void removed() {
		if (binding != null) binding.listening = false;
		ConfigManager.save("autosave", false);
	}

	private void refreshSlots() {
		for (int i = 0; i < SLOTS; i++) slotExists[i] = ConfigManager.exists("profile" + (i + 1));
	}

	// ---- layout helpers -------------------------------------------------

	private int tabY(int i) { return top + TITLE_H + i * (TAB_H + TAB_GAP); }
	private int gridX() { return left + TAB_W + PAD; }
	private int contentW() { return W - TAB_W - PAD * 2; }
	private int colWidth() { return (W - TAB_W - PAD * 3) / 2; }
	private int moduleX(int i) { return gridX() + (i % 2) * (colWidth() + PAD); }
	private int moduleY(int i) { return top + TITLE_H + (i / 2) * (BTN_H + GAP); }
	private int rowY(int i) { return top + TITLE_H + 24 + i * (BTN_H + GAP); }
	private int slotY(int i) { return top + TITLE_H + i * (BTN_H + GAP); }
	private int saveX() { return gridX() + contentW() - 2 * CFG_BTN_W - 6; }
	private int loadX() { return gridX() + contentW() - CFG_BTN_W; }

	private int backX() { return gridX(); }
	private int backY() { return top + TITLE_H - 4; }
	private static final int BACK_W = 60, BACK_H = 18;

	private static boolean inside(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	// ---- rendering --------------------------------------------------------

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		// Not calling super.render(): vanilla blur can only run once per frame
		// and we have no vanilla widgets.
		ctx.fill(0, 0, width, height, COL_DIM);
		ctx.fill(left, top, left + W, top + H, COL_PANEL);
		ctx.fill(left, top, left + TAB_W, top + H, COL_SIDEBAR);
		ctx.fill(left, top, left + W, top + 2, COL_ACCENT);
		ctx.drawText(textRenderer, "Display Tweaks", left + PAD, top + 10, COL_TEXT, false);

		Category[] cats = Category.values();
		for (int i = 0; i < cats.length; i++) {
			int y = tabY(i);
			boolean hover = inside(mouseX, mouseY, left + PAD / 2, y, TAB_W - PAD, TAB_H);
			boolean sel = cats[i] == selected && settingsModule == null;
			int bg = sel ? COL_ACCENT : (hover ? COL_BTN_HOVER : COL_BTN);
			ctx.fill(left + PAD / 2, y, left + TAB_W - PAD / 2, y + TAB_H, bg);
			ctx.drawText(textRenderer, cats[i].label, left + PAD + 2, y + (TAB_H - 8) / 2, COL_TEXT, false);
		}

		if (selected == Category.CONFIGS) renderConfigs(ctx, mouseX, mouseY);
		else if (settingsModule != null) renderSettings(ctx, mouseX, mouseY);
		else renderModules(ctx, mouseX, mouseY);
	}

	private void renderModules(DrawContext ctx, int mouseX, int mouseY) {
		List<Module> mods = ModuleManager.in(selected);
		Module hovered = null;
		for (int i = 0; i < mods.size(); i++) {
			Module m = mods.get(i);
			int x = moduleX(i), y = moduleY(i), w = colWidth();
			boolean hover = inside(mouseX, mouseY, x, y, w, BTN_H);
			if (hover) hovered = m;
			int bg = m.isEnabled() ? (hover ? COL_ON_HOVER : COL_ON) : (hover ? COL_BTN_HOVER : COL_BTN);
			ctx.fill(x, y, x + w, y + BTN_H, bg);
			int ty = y + (BTN_H - 8) / 2;
			ctx.drawText(textRenderer, m.name, x + 6, ty, COL_TEXT, false);
			ctx.drawText(textRenderer, "...", x + w - 16, ty, COL_TEXT_DIM, false);
			if (m.bind.get() >= 0) {
				String kn = "[" + KeyUtil.name(m.bind.get()) + "]";
				ctx.drawText(textRenderer, kn, x + w - 22 - textRenderer.getWidth(kn), ty, COL_TEXT_DIM, false);
			}
		}
		String hint = hovered != null ? hovered.description
				: "Left click: toggle   |   Right click: settings & keybind   |   Right Shift / Esc: close";
		ctx.drawText(textRenderer, hint, gridX(), top + H - 14, COL_TEXT_DIM, false);
	}

	private void renderSettings(DrawContext ctx, int mouseX, int mouseY) {
		boolean backHover = inside(mouseX, mouseY, backX(), backY(), BACK_W, BACK_H);
		ctx.fill(backX(), backY(), backX() + BACK_W, backY() + BACK_H, backHover ? COL_BTN_HOVER : COL_BTN);
		ctx.drawText(textRenderer, "< Back", backX() + 8, backY() + 5, COL_TEXT, false);
		ctx.drawText(textRenderer, settingsModule.name + " settings", backX() + BACK_W + 10, backY() + 5, COL_TEXT, false);

		List<Setting> list = settingsModule.settings();
		clampScroll();
		int shown = Math.min(VISIBLE_ROWS, list.size() - settingScroll);
		for (int i = 0; i < shown; i++) {
			Setting s = list.get(settingScroll + i);
			int x = gridX(), y = rowY(i), w = contentW();
			boolean hover = inside(mouseX, mouseY, x, y, w, BTN_H);
			ctx.fill(x, y, x + w, y + BTN_H, hover ? COL_BTN_HOVER : COL_BTN);
			int ty = y + (BTN_H - 8) / 2;
			if (s instanceof BoolSetting bs) {
				if (bs.get()) ctx.fill(x, y, x + 4, y + BTN_H, COL_ON);
				ctx.drawText(textRenderer, bs.name + ": " + (bs.get() ? "On" : "Off"), x + 8, ty, COL_TEXT, false);
			} else if (s instanceof NumberSetting n) {
				int fill = (int) (w * n.fraction());
				ctx.fill(x, y, x + fill, y + BTN_H, COL_ACCENT);
				String val = (n.step >= 1 && n.get() == Math.rint(n.get()))
						? String.valueOf((int) n.get()) : String.format("%.1f", n.get());
				ctx.drawText(textRenderer, n.name + ": " + val, x + 8, ty, COL_TEXT, false);
			} else if (s instanceof ModeSetting ms) {
				ctx.drawText(textRenderer, ms.name + ": " + ms.get(), x + 8, ty, COL_TEXT, false);
				ctx.drawText(textRenderer, "<  >", x + w - 30, ty, COL_TEXT_DIM, false);
			} else if (s instanceof ColorSetting cs) {
				ctx.fill(x + w - 26, y + 5, x + w - 8, y + BTN_H - 5, cs.get());
				ctx.drawText(textRenderer, cs.name + ": " + cs.colorName(), x + 8, ty, COL_TEXT, false);
			} else if (s instanceof KeybindSetting ks) {
				String label = ks.listening ? "Keybind: press a key...  (Esc cancel, Backspace clear)"
						: "Keybind: " + KeyUtil.name(ks.get());
				if (ks.listening) ctx.fill(x, y, x + 4, y + BTN_H, COL_ACCENT);
				ctx.drawText(textRenderer, label, x + 8, ty, COL_TEXT, false);
			} else if (s instanceof ActionSetting as) {
				ctx.fill(x, y, x + 4, y + BTN_H, COL_ACCENT);
				ctx.drawText(textRenderer, as.name, x + 8, ty, COL_TEXT, false);
			}
		}
		if (list.size() > VISIBLE_ROWS) {
			ctx.drawText(textRenderer, "Scroll for more  (" + (settingScroll + 1) + "-" + (settingScroll + shown) + " of " + list.size() + ")",
					gridX(), top + H - 14, COL_TEXT_DIM, false);
		}
	}

	private void renderConfigs(DrawContext ctx, int mouseX, int mouseY) {
		for (int i = 0; i < SLOTS; i++) {
			int y = slotY(i);
			int labelW = saveX() - 6 - gridX();
			ctx.fill(gridX(), y, gridX() + labelW, y + BTN_H, COL_BTN);
			int ty = y + (BTN_H - 8) / 2;
			ctx.drawText(textRenderer, "Profile " + (i + 1) + (slotExists[i] ? "  (saved)" : "  (empty)"),
					gridX() + 8, ty, COL_TEXT, false);

			boolean sh = inside(mouseX, mouseY, saveX(), y, CFG_BTN_W, BTN_H);
			ctx.fill(saveX(), y, saveX() + CFG_BTN_W, y + BTN_H, sh ? COL_ON_HOVER : COL_ON);
			ctx.drawText(textRenderer, "Save", saveX() + 22, ty, COL_TEXT, false);

			boolean lh = inside(mouseX, mouseY, loadX(), y, CFG_BTN_W, BTN_H);
			int lbg = slotExists[i] ? (lh ? COL_ACCENT : 0xFF4A74C9) : COL_BTN;
			ctx.fill(loadX(), y, loadX() + CFG_BTN_W, y + BTN_H, lbg);
			ctx.drawText(textRenderer, "Load", loadX() + 22, ty, slotExists[i] ? COL_TEXT : COL_TEXT_DIM, false);
		}
		if (!configStatus.isEmpty()) {
			ctx.drawText(textRenderer, configStatus, gridX(), slotY(SLOTS) + 2, COL_TEXT, false);
		}
		ctx.drawText(textRenderer, "Settings & keybinds auto-save when you close this menu.",
				gridX(), top + H - 26, COL_TEXT_DIM, false);
		ctx.drawText(textRenderer, "Profiles also remember which modules are switched on.",
				gridX(), top + H - 14, COL_TEXT_DIM, false);
	}

	// ---- input (1.21.9+ signatures: Click / KeyInput records) ---------------

	private void clampScroll() {
		if (settingsModule == null) return;
		int max = Math.max(0, settingsModule.settings().size() - VISIBLE_ROWS);
		settingScroll = Math.max(0, Math.min(max, settingScroll));
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (settingsModule != null && selected != Category.CONFIGS) {
			settingScroll -= (int) Math.signum(verticalAmount);
			clampScroll();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private void applySlider(NumberSetting n, double mx) {
		double f = (mx - gridX()) / contentW();
		f = Math.max(0, Math.min(1, f));
		n.set(n.min + f * (n.max - n.min));
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		double mx = click.x(), my = click.y();
		int button = click.button();

		// any click cancels a pending keybind capture
		if (binding != null) {
			binding.listening = false;
			binding = null;
		}

		Category[] cats = Category.values();
		for (int i = 0; i < cats.length; i++) {
			if (inside(mx, my, left + PAD / 2, tabY(i), TAB_W - PAD, TAB_H)) {
				selected = cats[i];
				settingsModule = null;
				if (selected == Category.CONFIGS) refreshSlots();
				return true;
			}
		}

		if (selected == Category.CONFIGS) {
			for (int i = 0; i < SLOTS; i++) {
				String name = "profile" + (i + 1);
				if (inside(mx, my, saveX(), slotY(i), CFG_BTN_W, BTN_H)) {
					configStatus = "Profile " + (i + 1) + ": " + ConfigManager.save(name, true);
					refreshSlots();
					return true;
				}
				if (inside(mx, my, loadX(), slotY(i), CFG_BTN_W, BTN_H)) {
					configStatus = "Profile " + (i + 1) + ": " + ConfigManager.load(name, true);
					return true;
				}
			}
			return super.mouseClicked(click, doubled);
		}

		if (settingsModule != null) {
			if (inside(mx, my, backX(), backY(), BACK_W, BACK_H)) {
				settingsModule = null;
				return true;
			}
			List<Setting> list = settingsModule.settings();
			int shown = Math.min(VISIBLE_ROWS, list.size() - settingScroll);
			for (int i = 0; i < shown; i++) {
				if (!inside(mx, my, gridX(), rowY(i), contentW(), BTN_H)) continue;
				Setting st = list.get(settingScroll + i);
				int dir = button == 1 ? -1 : 1;
				if (st instanceof BoolSetting bs) {
					bs.toggle();
					settingsModule.onSettingChanged();
				} else if (st instanceof ModeSetting ms) {
					ms.cycle(dir);
					settingsModule.onSettingChanged();
				} else if (st instanceof ColorSetting cs) {
					cs.cycle(dir);
					settingsModule.onSettingChanged();
				} else if (st instanceof NumberSetting n) {
					dragging = n;
					draggingOwner = settingsModule;
					applySlider(n, mx);
				} else if (st instanceof KeybindSetting ks) {
					binding = ks;
					ks.listening = true;
				} else if (st instanceof ActionSetting as) {
					as.run();
				}
				return true;
			}
			return super.mouseClicked(click, doubled);
		}

		List<Module> mods = ModuleManager.in(selected);
		for (int i = 0; i < mods.size(); i++) {
			if (inside(mx, my, moduleX(i), moduleY(i), colWidth(), BTN_H)) {
				if (button == 0) mods.get(i).toggle();
				else if (button == 1) {
					settingsModule = mods.get(i);
					settingScroll = 0;
				}
				return true;
			}
		}
		return super.mouseClicked(click, doubled);
	}

	@Override
	public boolean mouseDragged(Click click, double offsetX, double offsetY) {
		if (dragging != null) {
			applySlider(dragging, click.x());
			return true;
		}
		return super.mouseDragged(click, offsetX, offsetY);
	}

	@Override
	public boolean mouseReleased(Click click) {
		if (dragging != null && draggingOwner != null) draggingOwner.onSettingChanged();
		dragging = null;
		draggingOwner = null;
		return super.mouseReleased(click);
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		int key = input.key();
		if (binding != null) {
			if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) return true; // reserved for the menu
			if (key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_DELETE) binding.set(-1);
			else if (key != GLFW.GLFW_KEY_ESCAPE) binding.set(key);
			binding.listening = false;
			binding = null;
			return true;
		}
		if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
			close();
			return true;
		}
		return super.keyPressed(input);
	}
}
