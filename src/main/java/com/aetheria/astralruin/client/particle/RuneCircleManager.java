package com.aetheria.astralruin.client.particle;

import com.aetheria.astralruin.config.AetheriaConfig;
import com.aetheria.astralruin.item.AstralArmorItem;
import com.aetheria.astralruin.particle.RuneCirclePreset;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;

/** Cercles actifs cote client (sorts + auras du set Astral) : tick et rendu des decalques. */
public final class RuneCircleManager {
    private static final List<RuneCircleEmitter> ACTIVE = new ArrayList<>();
    private static final Map<UUID, RuneCircleEmitter> AURAS = new HashMap<>();
    private static final int MAX_EMITTERS = 48;

    public static void add(RuneCircleEmitter emitter) {
        if (ACTIVE.size() < MAX_EMITTERS) ACTIVE.add(emitter);
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            ACTIVE.clear();
            AURAS.clear();
            return;
        }
        if (mc.isPaused()) return;
        ACTIVE.removeIf(e -> {
            try {
                return !e.tick(mc.level);
            } catch (Exception ex) {
                com.aetheria.astralruin.client.ClientErrors.report("cercle (tick)", ex);
                return true;
            }
        });
        tickAuras(mc);
    }

    /** Aura de cercles runiques sous les pieds des porteurs du set Astral complet. */
    private static void tickAuras(Minecraft mc) {
        if (!AetheriaConfig.SHOW_AURA.get()) {
            AURAS.clear();
            return;
        }
        Set<UUID> seen = new HashSet<>();
        for (AbstractClientPlayer p : mc.level.players()) {
            if (p.isInvisible() || p.distanceToSqr(mc.player) > 48 * 48 || !AstralArmorItem.hasFullSet(p)) continue;
            seen.add(p.getUUID());
            RuneCircleEmitter aura = AURAS.computeIfAbsent(p.getUUID(), id ->
                    new RuneCircleEmitter(p.position(), new Vector3f(0, 1, 0), RuneCirclePreset.AURA, 1.2F, Integer.MAX_VALUE / 2));
            aura.setCenter(p.position().add(0, 0.06, 0));
            aura.tick(mc.level);
        }
        AURAS.keySet().retainAll(seen);
    }

    public static void render(PoseStack pose, MultiBufferSource buffers, Vec3 cam, float partialTick) {
        if (!AetheriaConfig.RENDER_DECALS.get()) return;
        ACTIVE.removeIf(e -> !safeRender(e, pose, buffers, cam, partialTick));
        AURAS.values().removeIf(e -> !safeRender(e, pose, buffers, cam, partialTick));
    }

    private static boolean safeRender(RuneCircleEmitter e, PoseStack pose, MultiBufferSource buffers, Vec3 cam, float pt) {
        int depth = 0;
        try {
            pose.pushPose();
            depth = 1;
            e.render(pose, buffers, cam, pt);
            return true;
        } catch (Exception ex) {
            com.aetheria.astralruin.client.ClientErrors.report("cercle (rendu)", ex);
            return false;
        } finally {
            if (depth == 1) pose.popPose();
        }
    }

    private RuneCircleManager() {}
}
