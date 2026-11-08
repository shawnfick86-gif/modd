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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Floating category panels (one per Category) with a toggle switch per module.
 * Left click a row: toggle. Right click a row: open its settings window.
 * Mouse wheel scrolls the panels when they don't all fit.
 */
public class ClickGuiScreen extends Screen {
	// panels
	private static final int PANEL_W = 124, HEAD_H = 30, ROW_H = 26, GAP = 8, MARGIN = 10;
	// modal (settings / config) window
	private static final int MW = 300, MH = 262, MPAD = 10, MROW_H = 24, MGAP = 6;
	private static final int VISIBLE_ROWS = 6, SLOTS = 5, CFG_BTN_W = 64;

	private static final int COL_DIM = 0x50000000;
	private static final int COL_PANEL = 0xC0161B2C;
	private static final int COL_MODAL = 0xF0141A28;
	private static final int COL_ROW = 0xFF232A3F;
	private static final int COL_ROW_HOVER = 0xFF2E3650;
	private static final int COL_BADGE = 0xFF2A3350;
	private static final int COL_ACCENT = 0xFF7B5CFF;
	private static final int COL_OFF = 0xFF3A4156;
	private static final int COL_GREEN = 0xFF2E8B57;
	private static final int COL_TEXT = 0xFFFFFFFF;
	private static final int COL_SUB = 0xFF8A93A8;

	private record PanelRect(Category cat, int x, int y, int w, int h) {}

	private final List<PanelRect> panels = new ArrayList<>();
	private int scrollY = 0, maxScroll = 0;

	private Module settingsModule = null;
	private boolean configOpen = false;
	private Module draggingOwner = null;
	private NumberSetting dragging = null;
	private KeybindSetting binding = null;
	private int settingScroll = 0;
	private String configStatus = "";
	private final boolean[] slotExists = new boolean[SLOTS];

	public ClickGuiScreen() {
		super(Text.literal("Display Tweaks"));
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

	private boolean modalOpen() {
		return configOpen || settingsModule != null;
	}

	private void closeModal() {
		settingsModule = null;
		configOpen = false;
		dragging = null;
		draggingOwner = null;
		if (binding != null) {
			binding.listening = false;
			binding = null;
		}
	}

	// ---- helpers --------------------------------------------------------

	private static boolean inside(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	/** Fill with 1px-cut corners so everything looks softly rounded. */
	private static void rr(DrawContext ctx, int x, int y, int w, int h, int color) {
		ctx.fill(x + 1, y, x + w - 1, y + h, color);
		ctx.fill(x, y + 1, x + w, y + h - 1, color);
	}

	private int rowsFor(Category c) {
		return c == Category.CONFIGS ? 1 : ModuleManager.in(c).size();
	}

	// ---- panel layout -----------------------------------------------------

	private void layoutPanels() {
		panels.clear();
		int cols = Math.max(1, (width - 2 * MARGIN + GAP) / (PANEL_W + GAP));
		int totalW = cols * (PANEL_W + GAP) - GAP;
		int startX = (width - totalW) / 2;
		int[] colH = new int[cols];
		Arrays.fill(colH, MARGIN);
		for (Category c : Category.values()) {
			int h = HEAD_H + rowsFor(c) * ROW_H + 6;
			int best = 0;
			for (int k = 1; k < cols; k++) if (colH[k] < colH[best]) best = k;
			panels.add(new PanelRect(c, startX + best * (PANEL_W + GAP), colH[best] - scrollY, PANEL_W, h));
			colH[best] += h + GAP;
		}
		int content = 0;
		for (int v : colH) content = Math.max(content, v);
		maxScroll = Math.max(0, content + MARGIN - height);
		scrollY = Math.max(0, Math.min(maxScroll, scrollY));
	}

	// ---- modal geometry ---------------------------------------------------

	private int mLeft() { return (width - MW) / 2; }
	private int mTop() { return (height - MH) / 2; }
	private int mGridX() { return mLeft() + MPAD; }
	private int mContentW() { return MW - 2 * MPAD; }
	private int mRowY(int i) { return mTop() + 46 + i * (MROW_H + MGAP); }
	private int closeX() { return mLeft() + MW - 24; }
	private int closeY() { return mTop() + 8; }
	private int saveX() { return mGridX() + mContentW() - 2 * CFG_BTN_W - 6; }
	private int loadX() { return mGridX() + mContentW() - CFG_BTN_W; }

	// ---- rendering --------------------------------------------------------

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		// Not calling super.render(): vanilla blur can only run once per frame
		// and we have no vanilla widgets.
		ctx.fill(0, 0, width, height, COL_DIM);
		layoutPanels();
		boolean blocked = modalOpen();
		for (PanelRect p : panels) drawPanel(ctx, p, blocked ? -1 : mouseX, blocked ? -1 : mouseY);

		if (configOpen) drawConfigModal(ctx, mouseX, mouseY);
		else if (settingsModule != null) drawSettingsModal(ctx, mouseX, mouseY);
	}

	private void drawPanel(DrawContext ctx, PanelRect p, int mouseX, int mouseY) {
		rr(ctx, p.x(), p.y(), p.w(), p.h(), COL_PANEL);
		ctx.drawText(textRenderer, p.cat().label, p.x() + 10, p.y() + 7, COL_TEXT, false);
		ctx.drawText(textRenderer, p.cat().subtitle, p.x() + 10, p.y() + 18, COL_SUB, false);

		String cnt = String.valueOf(rowsFor(p.cat()));
		int bw = textRenderer.getWidth(cnt) + 10;
		rr(ctx, p.x() + p.w() - bw - 8, p.y() + 8, bw, 14, COL_BADGE);
		ctx.drawText(textRenderer, cnt, p.x() + p.w() - bw - 3, p.y() + 11, COL_TEXT, false);

		int rx = p.x() + 4, rw = p.w() - 8;
		if (p.cat() == Category.CONFIGS) {
			int ry = p.y() + HEAD_H;
			boolean hover = inside(mouseX, mouseY, rx, ry, rw, ROW_H - 2);
			rr(ctx, rx, ry, rw, ROW_H - 2, hover ? COL_ROW_HOVER : COL_ROW);
			ctx.drawText(textRenderer, "Config Manager", rx + 8, ry + 4, COL_TEXT, false);
			ctx.drawText(textRenderer, "Profiles", rx + 8, ry + 14, COL_SUB, false);
			ctx.drawText(textRenderer, ">", rx + rw - 14, ry + 9, COL_SUB, false);
			return;
		}

		List<Module> mods = ModuleManager.in(p.cat());
		for (int i = 0; i < mods.size(); i++) {
			Module m = mods.get(i);
			int ry = p.y() + HEAD_H + i * ROW_H;
			boolean hover = inside(mouseX, mouseY, rx, ry, rw, ROW_H - 2);
			rr(ctx, rx, ry, rw, ROW_H - 2, hover ? COL_ROW_HOVER : COL_ROW);
			if (m.isEnabled()) ctx.fill(rx, ry + 2, rx + 2, ry + ROW_H - 4, COL_ACCENT);

			ctx.drawText(textRenderer, m.name, rx + 8, ry + 4, COL_TEXT, false);
			String sub = m.bind.get() >= 0 ? KeyUtil.name(m.bind.get()).toUpperCase() : "NONE";
			ctx.drawText(textRenderer, sub, rx + 8, ry + 14, COL_SUB, false);

			// toggle switch
			int sx = rx + rw - 28, sy = ry + (ROW_H - 2 - 10) / 2;
			rr(ctx, sx, sy, 22, 10, m.isEnabled() ? COL_ACCENT : COL_OFF);
			int kx = m.isEnabled() ? sx + 12 : sx + 2;
			ctx.fill(kx, sy + 2, kx + 8, sy + 8, COL_TEXT);
		}
	}

	private void drawModalFrame(DrawContext ctx, String title, int mouseX, int mouseY) {
		ctx.fill(0, 0, width, height, 0x60000000);
		rr(ctx, mLeft(), mTop(), MW, MH, COL_MODAL);
		ctx.fill(mLeft() + 1, mTop(), mLeft() + MW - 1, mTop() + 2, COL_ACCENT);
		ctx.drawText(textRenderer, title, mGridX(), mTop() + 12, COL_TEXT, false);
		boolean xh = inside(mouseX, mouseY, closeX(), closeY(), 16, 16);
		rr(ctx, closeX(), closeY(), 16, 16, xh ? COL_ROW_HOVER : COL_ROW);
		ctx.drawText(textRenderer, "x", closeX() + 5, closeY() + 4, COL_TEXT, false);
	}

	private void drawSettingsModal(DrawContext ctx, int mouseX, int mouseY) {
		drawModalFrame(ctx, settingsModule.name + " settings", mouseX, mouseY);
		ctx.drawText(textRenderer, settingsModule.description, mGridX(), mTop() + 26, COL_SUB, false);

		List<Setting> list = settingsModule.settings();
		clampSettingScroll();
		int shown = Math.min(VISIBLE_ROWS, list.size() - settingScroll);
		for (int i = 0; i < shown; i++) {
			Setting s = list.get(settingScroll + i);
			int x = mGridX(), y = mRowY(i), w = mContentW();
			boolean hover = inside(mouseX, mouseY, x, y, w, MROW_H);
			rr(ctx, x, y, w, MROW_H, hover ? COL_ROW_HOVER : COL_ROW);
			int ty = y + (MROW_H - 8) / 2;
			if (s instanceof BoolSetting bs) {
				ctx.drawText(textRenderer, bs.name, x + 8, ty, COL_TEXT, false);
				int sx = x + w - 30, sy = y + (MROW_H - 10) / 2;
				rr(ctx, sx, sy, 22, 10, bs.get() ? COL_ACCENT : COL_OFF);
				int kx = bs.get() ? sx + 12 : sx + 2;
				ctx.fill(kx, sy + 2, kx + 8, sy + 8, COL_TEXT);
			} else if (s instanceof NumberSetting n) {
				int fill = (int) (w * n.fraction());
				if (fill > 2) rr(ctx, x, y, fill, MROW_H, COL_ACCENT);
				String val = (n.step >= 1 && n.get() == Math.rint(n.get()))
						? String.valueOf((int) n.get()) : String.format("%.1f", n.get());
				ctx.drawText(textRenderer, n.name + ": " + val, x + 8, ty, COL_TEXT, false);
			} else if (s instanceof ModeSetting ms) {
				ctx.drawText(textRenderer, ms.name + ": " + ms.get(), x + 8, ty, COL_TEXT, false);
				ctx.drawText(textRenderer, "<  >", x + w - 30, ty, COL_SUB, false);
			} else if (s instanceof ColorSetting cs) {
				rr(ctx, x + w - 28, y + 5, 20, MROW_H - 10, cs.get());
				ctx.drawText(textRenderer, cs.name + ": " + cs.colorName(), x + 8, ty, COL_TEXT, false);
			} else if (s instanceof KeybindSetting ks) {
				String label = ks.listening ? "Keybind: press a key...  (Esc cancel, Bksp clear)"
						: "Keybind: " + KeyUtil.name(ks.get());
				if (ks.listening) ctx.fill(x, y + 2, x + 2, y + MROW_H - 2, COL_ACCENT);
				ctx.drawText(textRenderer, label, x + 8, ty, COL_TEXT, false);
			} else if (s instanceof ActionSetting as) {
				ctx.fill(x, y + 2, x + 2, y + MROW_H - 2, COL_ACCENT);
				ctx.drawText(textRenderer, as.name, x + 8, ty, COL_TEXT, false);
			}
		}
		String hint = list.size() > VISIBLE_ROWS
				? "Scroll for more  (" + (settingScroll + 1) + "-" + (settingScroll + shown) + " of " + list.size() + ")"
				: "Right click on a mode or color goes backwards   |   Esc: close";
		ctx.drawText(textRenderer, hint, mGridX(), mTop() + MH - 16, COL_SUB, false);
	}

	private void drawConfigModal(DrawContext ctx, int mouseX, int mouseY) {
		drawModalFrame(ctx, "Config Manager", mouseX, mouseY);
		ctx.drawText(textRenderer, "Save and load your settings as profiles", mGridX(), mTop() + 26, COL_SUB, false);
		for (int i = 0; i < SLOTS; i++) {
			int y = mRowY(i);
			int labelW = saveX() - 6 - mGridX();
			rr(ctx, mGridX(), y, labelW, MROW_H, COL_ROW);
			int ty = y + (MROW_H - 8) / 2;
			ctx.drawText(textRenderer, "Profile " + (i + 1) + (slotExists[i] ? "  (saved)" : "  (empty)"),
					mGridX() + 8, ty, COL_TEXT, false);

			boolean sh = inside(mouseX, mouseY, saveX(), y, CFG_BTN_W, MROW_H);
			rr(ctx, saveX(), y, CFG_BTN_W, MROW_H, sh ? 0xFF3AA568 : COL_GREEN);
			ctx.drawText(textRenderer, "Save", saveX() + 20, ty, COL_TEXT, false);

			boolean lh = inside(mouseX, mouseY, loadX(), y, CFG_BTN_W, MROW_H);
			rr(ctx, loadX(), y, CFG_BTN_W, MROW_H, slotExists[i] ? (lh ? 0xFF8F74FF : COL_ACCENT) : COL_ROW);
			ctx.drawText(textRenderer, "Load", loadX() + 20, ty, slotExists[i] ? COL_TEXT : COL_SUB, false);
		}
		if (!configStatus.isEmpty()) ctx.drawText(textRenderer, configStatus, mGridX(), mRowY(SLOTS) + 2, COL_TEXT, false);
		ctx.drawText(textRenderer, "Settings & keybinds auto-save when you close the menu.", mGridX(), mTop() + MH - 28, COL_SUB, false);
		ctx.drawText(textRenderer, "Profiles also remember which modules are switched on.", mGridX(), mTop() + MH - 16, COL_SUB, false);
	}

	// ---- input (1.21.9+ signatures: Click / KeyInput records) ---------------

	private void clampSettingScroll() {
		if (settingsModule == null) return;
		int max = Math.max(0, settingsModule.settings().size() - VISIBLE_ROWS);
		settingScroll = Math.max(0, Math.min(max, settingScroll));
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (settingsModule != null) {
			settingScroll -= (int) Math.signum(verticalAmount);
			clampSettingScroll();
			return true;
		}
		if (!modalOpen()) {
			scrollY -= (int) (verticalAmount * 24);
			scrollY = Math.max(0, Math.min(maxScroll, scrollY));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private void applySlider(NumberSetting n, double mx) {
		double f = (mx - mGridX()) / mContentW();
		f = Math.max(0, Math.min(1, f));
		n.set(n.min + f * (n.max - n.min));
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		double mx = click.x(), my = click.y();
		int button = click.button();

		if (binding != null) {
			binding.listening = false;
			binding = null;
		}

		if (modalOpen()) {
			if (!inside(mx, my, mLeft(), mTop(), MW, MH)
					|| inside(mx, my, closeX(), closeY(), 16, 16)) {
				closeModal();
				return true;
			}
			if (configOpen) {
				for (int i = 0; i < SLOTS; i++) {
					String name = "profile" + (i + 1);
					if (inside(mx, my, saveX(), mRowY(i), CFG_BTN_W, MROW_H)) {
						configStatus = "Profile " + (i + 1) + ": " + ConfigManager.save(name, true);
						refreshSlots();
						return true;
					}
					if (inside(mx, my, loadX(), mRowY(i), CFG_BTN_W, MROW_H)) {
						configStatus = "Profile " + (i + 1) + ": " + ConfigManager.load(name, true);
						return true;
					}
				}
				return true;
			}

			List<Setting> list = settingsModule.settings();
			int shown = Math.min(VISIBLE_ROWS, list.size() - settingScroll);
			for (int i = 0; i < shown; i++) {
				if (!inside(mx, my, mGridX(), mRowY(i), mContentW(), MROW_H)) continue;
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
			return true;
		}

		// panels
		for (PanelRect p : panels) {
			int rx = p.x() + 4, rw = p.w() - 8;
			if (p.cat() == Category.CONFIGS) {
				if (inside(mx, my, rx, p.y() + HEAD_H, rw, ROW_H - 2)) {
					configOpen = true;
					refreshSlots();
					return true;
				}
				continue;
			}
			List<Module> mods = ModuleManager.in(p.cat());
			for (int i = 0; i < mods.size(); i++) {
				if (inside(mx, my, rx, p.y() + HEAD_H + i * ROW_H, rw, ROW_H - 2)) {
					if (button == 0) mods.get(i).toggle();
					else if (button == 1) {
						settingsModule = mods.get(i);
						settingScroll = 0;
					}
					return true;
				}
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
		if (key == GLFW.GLFW_KEY_ESCAPE && modalOpen()) {
			closeModal();
			return true;
		}
		if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
			close();
			return true;
		}
		return super.keyPressed(input);
	}
}
