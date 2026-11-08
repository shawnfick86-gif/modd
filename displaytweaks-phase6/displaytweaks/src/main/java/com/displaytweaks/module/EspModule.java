package com.displaytweaks.module;

import net.minecraft.client.MinecraftClient;

/**
 * Base for outline/tracer modules. Shapes drawn with Minecraft's gizmo system
 * last about one tick, so emit() is called every tick (from both the client tick
 * and the world tick, because the active gizmo collector depends on where in the
 * game loop we are) while rescan() runs only now and then.
 */
public abstract class EspModule extends Module {
	private int timer;

	protected EspModule(String name, String description) {
		super(name, description, Category.RENDER);
	}

	/** Heavy work (scanning chunks). Runs about once a second. */
	protected void rescan(MinecraftClient mc) {}

	/** Draw whatever was found. Runs every tick. */
	protected abstract void emit(MinecraftClient mc);

	protected int rescanInterval() {
		return 20;
	}

	@Override
	public void onTick(MinecraftClient mc) {
		if (mc.player == null || mc.world == null) return;
		if (timer-- <= 0) {
			timer = rescanInterval();
			try {
				rescan(mc);
			} catch (RuntimeException ignored) {
				// a chunk unloading mid-scan etc.; try again next time
			}
		}
		safeEmit(mc);
	}

	@Override
	public void onWorldTick(MinecraftClient mc) {
		if (mc.player == null || mc.world == null) return;
		safeEmit(mc);
	}

	private void safeEmit(MinecraftClient mc) {
		try {
			emit(mc);
		} catch (RuntimeException ignored) {
			// never let a drawing hiccup crash the game
		}
	}
}
