package com.ollie.hack.modules.movement;
import com.ollie.hack.Category; import com.ollie.hack.Module; import net.minecraft.client.MinecraftClient; import net.minecraft.util.math.Vec3d; import net.minecraft.client.network.ClientPlayerEntity;
public class Fly extends Module {
    public Fly() { super("Fly", Category.MOVEMENT); }
    @Override public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance(); ClientPlayerEntity player = mc.player; if (player == null) return;
        player.getAbilities().flying = false; player.setVelocity(0, 0, 0);
        Vec3d forward = Vec3d.fromPolar(0, player.getYaw()).multiply(0.5);
        Vec3d right = Vec3d.fromPolar(0, player.getYaw() + 90).multiply(0.5);
        double y = 0; if (mc.options.jumpKey.isPressed()) y += 0.5; if (mc.options.sneakKey.isPressed()) y -= 0.5;
        Vec3d vel = new Vec3d(0, y, 0);
        if (mc.options.forwardKey.isPressed()) vel = vel.add(forward);
        if (mc.options.backKey.isPressed()) vel = vel.add(forward.multiply(-1));
        if (mc.options.leftKey.isPressed()) vel = vel.add(right);
        if (mc.options.rightKey.isPressed()) vel = vel.add(right.multiply(-1));
        player.setVelocity(vel); player.fallDistance = 0;
    }
}
