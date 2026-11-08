package com.ollie.hack.modules.movement;
import com.ollie.hack.Category; import com.ollie.hack.Module; import net.minecraft.client.MinecraftClient; import net.minecraft.item.BlockItem; import net.minecraft.item.ItemStack; import net.minecraft.util.Hand; import net.minecraft.util.hit.BlockHitResult; import net.minecraft.util.math.BlockPos; import net.minecraft.util.math.Direction; import net.minecraft.util.math.Vec3d;
public class Scaffold extends Module {
    public Scaffold() { super("Scaffold", Category.MOVEMENT); }
    @Override public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance(); if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        int blockSlot = -1;
        for (int i = 0; i < 9; i++) { if (mc.player.getInventory().getStack(i).getItem() instanceof BlockItem) { blockSlot = i; break; } }
        if (blockSlot == -1) return;
        BlockPos pos = mc.player.getBlockPos().down();
        if (mc.world.getBlockState(pos).isAir()) {
            mc.player.getInventory().selectedSlot = blockSlot;
            Vec3d hitVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, pos, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult); mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
