package com.displaytweaks.mixin;

import com.displaytweaks.module.Xray;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockModelRenderer.class)
public class BlockModelRendererMixin {
	/**
	 * Vanilla asks this once per block face while building a chunk mesh.
	 * With Xray on: chosen blocks always draw, everything else never does.
	 * require = 0 so a future Minecraft change can't crash the game on launch.
	 */
	@Inject(method = "shouldDrawFace", at = @At("HEAD"), cancellable = true, require = 0)
	private static void displaytweaks$xray(BlockRenderView world, BlockState state, boolean cull,
			Direction side, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		Xray x = Xray.INSTANCE;
		if (x != null && x.isEnabled()) {
			cir.setReturnValue(x.isVisible(state.getBlock()));
		}
	}
}
