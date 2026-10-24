package com.aetheria.astralruin.client.vfx;

import com.aetheria.astralruin.network.SpawnVfxS2C;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.ArrayList;
import java.util.List;

public final class VfxManager {
    private static final List<Vfx> ACTIVE = new ArrayList<>();
    private static final int MAX = 256;

    public static void add(SpawnVfxS2C data) {
        if (ACTIVE.size() < MAX) ACTIVE.add(new Vfx(data));
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            ACTIVE.clear();
            return;
        }
        if (mc.isPaused()) return;
        ACTIVE.removeIf(v -> !v.tick(mc.level));
    }

    public static void render(PoseStack pose, MultiBufferSource buffers, Camera camera, float pt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        for (Vfx v : ACTIVE) v.render(pose, buffers, camera, mc.level, pt);
    }

    private VfxManager() {}
}
