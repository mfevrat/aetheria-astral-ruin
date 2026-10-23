package com.aetheria.astralruin.client.camera;

import com.aetheria.astralruin.config.AetheriaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.ViewportEvent;

/** Secousse camera a decroissance quadratique. Appele depuis ClientForgeEvents. */
public final class CameraShakeHandler {
    private static float intensity;
    private static int ticksLeft;
    private static int totalTicks = 1;

    public static void shake(float power, int ticks) {
        if (ticksLeft <= 0 || power >= intensity * (ticksLeft / (float) totalTicks)) {
            intensity = Math.min(1.5F, power);
            ticksLeft = totalTicks = Math.max(1, ticks);
        }
    }

    public static void tick() {
        if (ticksLeft > 0) ticksLeft--;
    }

    public static void apply(ViewportEvent.ComputeCameraAngles e) {
        Minecraft mc = Minecraft.getInstance();
        if (ticksLeft <= 0 || mc.level == null) return;
        float pt = (float) e.getPartialTick();
        float t = Mth.clamp((ticksLeft - pt) / totalTicks, 0F, 1F);
        float amp = intensity * t * t * 4F * AetheriaConfig.SHAKE_SCALE.get().floatValue();
        if (amp <= 0.001F) return;
        double time = (mc.level.getGameTime() + pt) * 1.7;
        e.setYaw(e.getYaw() + (float) Math.sin(time * 3.1) * amp);
        e.setPitch(e.getPitch() + (float) Math.sin(time * 4.3 + 1.0) * amp);
        e.setRoll(e.getRoll() + (float) Math.sin(time * 2.2 + 2.0) * amp * 0.5F);
    }

    private CameraShakeHandler() {}
}
