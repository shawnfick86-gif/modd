package com.displaytweaks.mixin;

import com.displaytweaks.module.Xray;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric API's Indigo renderer (used when Sodium isn't installed) decides face
 * culling through Block.shouldDrawSide instead of vanilla's BlockModelRenderer.
 * Each hook below targets one exact signature. require = 0 means a signature that
 * doesn't exist in this Minecraft version is simply skipped, never a crash.
 */
@Mixin(Block.class)
public class BlockMixin {
	@Inject(method = "shouldDrawSide(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/BlockPos;)Z",
			at = @At("HEAD"), cancellable = true, require = 0)
	private static void displaytweaks$xrayA(BlockState state, BlockView world, BlockPos pos, Direction side,
			BlockPos otherPos, CallbackInfoReturnable<Boolean> cir) {
		apply(state, cir);
	}

	@Inject(method = "shouldDrawSide(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/BlockPos;)Z",
			at = @At("HEAD"), cancellable = true, require = 0)
	private static void displaytweaks$xrayB(BlockState state, BlockRenderView world, BlockPos pos, Direction side,
			BlockPos otherPos, CallbackInfoReturnable<Boolean> cir) {
		apply(state, cir);
	}

	@Inject(method = "shouldDrawSide(Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z",
			at = @At("HEAD"), cancellable = true, require = 0)
	private static void displaytweaks$xrayC(BlockState state, BlockState other, Direction side,
			CallbackInfoReturnable<Boolean> cir) {
		apply(state, cir);
	}

	private static void apply(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		Xray x = Xray.INSTANCE;
		if (x != null && x.isEnabled()) {
			Xray.hookHit = true;
			cir.setReturnValue(x.isVisible(state.getBlock()));
		}
	}
}
