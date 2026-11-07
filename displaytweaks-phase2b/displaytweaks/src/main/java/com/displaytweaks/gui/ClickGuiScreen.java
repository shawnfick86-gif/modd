package com.displaytweaks.gui;

import com.displaytweaks.module.Category;
import com.displaytweaks.module.Module;
import com.displaytweaks.module.ModuleManager;
import com.displaytweaks.setting.BoolSetting;
import com.displaytweaks.setting.ColorSetting;
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
 */
public class ClickGuiScreen extends Screen {
	private static final int W = 440, H = 270;
	private static final int TAB_W = 100, TAB_H = 24, TAB_GAP = 4;
	private static final int PAD = 8, BTN_H = 24, GAP = 6, TITLE_H = 26;

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
	private NumberSetting dragging = null;
	private int settingScroll = 0;
	// rows that fit on the settings page
	private static final int VISIBLE_ROWS = 6;
	private int left, top;

	public ClickGuiScreen() {
		super(Text.literal("Display Tweaks"));
	}

	@Override
	protected void init() {
		left = (width - W) / 2;
		top = (height - H) / 2;
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	// ---- layout helpers -------------------------------------------------

	private int tabY(int i) { return top + TITLE_H + i * (TAB_H + TAB_GAP); }
	private int gridX() { return left + TAB_W + PAD; }
	private int contentW() { return W - TAB_W - PAD * 2; }
	private int colWidth() { return (W - TAB_W - PAD * 3) / 2; }
	private int moduleX(int i) { return gridX() + (i % 2) * (colWidth() + PAD); }
	private int moduleY(int i) { return top + TITLE_H + (i / 2) * (BTN_H + GAP); }
	private int rowY(int i) { return top + TITLE_H + 24 + i * (BTN_H + GAP); }

	// back button on the settings page
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

		if (settingsModule != null) renderSettings(ctx, mouseX, mouseY);
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
			ctx.drawText(textRenderer, m.name, x + 6, y + (BTN_H - 8) / 2, COL_TEXT, false);
			if (!m.settings().isEmpty()) {
				ctx.drawText(textRenderer, "...", x + w - 16, y + (BTN_H - 8) / 2, COL_TEXT_DIM, false);
			}
		}
		String hint = hovered != null ? hovered.description
				: "Left click: toggle   |   Right click: settings   |   Right Shift / Esc: close";
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
			}
		}
		if (list.size() > VISIBLE_ROWS) {
			int maxScroll = list.size() - VISIBLE_ROWS;
			ctx.drawText(textRenderer, "Scroll for more  (" + (settingScroll + 1) + "-" + (settingScroll + shown) + " of " + list.size() + ")",
					gridX(), top + H - 14, COL_TEXT_DIM, false);
		}
		if (list.isEmpty()) {
			ctx.drawText(textRenderer, "No settings for this module yet.", gridX(), rowY(0), COL_TEXT_DIM, false);
		}
	}

	// ---- input (1.21.9+ signatures: Click / KeyInput records) ---------------

	private void clampScroll() {
		if (settingsModule == null) return;
		int max = Math.max(0, settingsModule.settings().size() - VISIBLE_ROWS);
		settingScroll = Math.max(0, Math.min(max, settingScroll));
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (settingsModule != null) {
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

		Category[] cats = Category.values();
		for (int i = 0; i < cats.length; i++) {
			if (inside(mx, my, left + PAD / 2, tabY(i), TAB_W - PAD, TAB_H)) {
				selected = cats[i];
				settingsModule = null;
				return true;
			}
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
				if (st instanceof BoolSetting bs) bs.toggle();
				else if (st instanceof ModeSetting ms) ms.cycle(dir);
				else if (st instanceof ColorSetting cs) cs.cycle(dir);
				else if (st instanceof NumberSetting n) {
					dragging = n;
					applySlider(n, mx);
				}
				return true;
			}
			return super.mouseClicked(click, doubled);
		}

		List<Module> mods = ModuleManager.in(selected);
		for (int i = 0; i < mods.size(); i++) {
			if (inside(mx, my, moduleX(i), moduleY(i), colWidth(), BTN_H)) {
				if (button == 0) mods.get(i).toggle();
				else if (button == 1) { settingsModule = mods.get(i); settingScroll = 0; }
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
		dragging = null;
		return super.mouseReleased(click);
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		if (input.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
			close();
			return true;
		}
		return super.keyPressed(input);
	}
}
