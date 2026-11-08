package com.displaytweaks.module;

import com.displaytweaks.setting.BoolSetting;
import com.displaytweaks.setting.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Places the block in your main hand under your feet as you walk. */
public class Scaffold extends Module {
	private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 1, 0, 10, 1));
	private final NumberSetting ahead = add(new NumberSetting("Place Ahead (blocks)", 0, 0, 3, 1));
	private final BoolSetting onlyMoving = add(new BoolSetting("Only While Moving", false));
	private final BoolSetting swing = add(new BoolSetting("Swing Hand", false));

	private int timer;

	public Scaffold() {
		super("Scaffold", "Place blocks under your feet", Category.BUILDING);
	}

	@Override
	public void onTick(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.world == null || mc.interactionManager == null) return;
		if (mc.currentScreen != null) return;
		if (timer > 0) {
			timer--;
			return;
		}
		if (!(p.getMainHandStack().getItem() instanceof BlockItem)) return;

		Vec3d v = p.getVelocity();
		double hs = Math.sqrt(v.x * v.x + v.z * v.z);
		if (onlyMoving.get() && hs < 0.03) return;

		int n = ahead.getInt();
		for (int k = 0; k <= n; k++) {
			double dx = 0, dz = 0;
			if (k > 0) {
				if (hs < 0.03) break;
				dx = v.x / hs * k;
				dz = v.z / hs * k;
			}
			BlockPos target = BlockPos.ofFloored(p.getX() + dx, p.getY() - 1.0, p.getZ() + dz);
			if (placeAt(mc, p, target)) {
				timer = delay.getInt();
				return;
			}
		}
	}

	private boolean placeAt(MinecraftClient mc, ClientPlayerEntity p, BlockPos target) {
		BlockState here = mc.world.getBlockState(target);
		if (!here.isReplaceable()) return false;

		Direction[] order = {Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP};
		for (Direction d : order) {
			BlockPos neighbor = target.offset(d);
			BlockState ns = mc.world.getBlockState(neighbor);
			if (ns.isAir() || ns.isReplaceable()) continue;
			Direction face = d.getOpposite();
			Vec3d hit = Vec3d.ofCenter(neighbor).add(face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5);
			mc.interactionManager.interactBlock(p, Hand.MAIN_HAND, new BlockHitResult(hit, face, neighbor, false));
			if (swing.get()) p.swingHand(Hand.MAIN_HAND);
			return true;
		}
		return false;
	}
}
