package com.aetheria.astralruin.util;

import com.aetheria.astralruin.network.CameraShakeS2C;
import com.aetheria.astralruin.network.ModNetwork;
import com.aetheria.astralruin.network.SpawnRuneCircleS2C;
import com.aetheria.astralruin.particle.AstralParticleOptions;
import com.aetheria.astralruin.particle.RuneCirclePreset;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Raccourcis cote serveur pour declencher les effets visuels chez les clients proches. */
public final class Fx {
    public static final int GOLD = 0xFFD36B;
    public static final int VIOLET = 0x9B5CFF;
    public static final int WHITE = 0xFFFFFF;
    public static final int PINK = 0xE07CFF;
    private static final double VISUAL_RANGE = 96;
    private static final Vector3f UP = new Vector3f(0, 1, 0);

    public static void circle(ServerLevel level, Vec3 center, Vector3f normal, RuneCirclePreset preset, float radius, int duration) {
        ModNetwork.sendNear(level, center, VISUAL_RANGE, new SpawnRuneCircleS2C(center, normal, preset, radius, duration));
    }

    public static void groundCircle(ServerLevel level, Vec3 center, RuneCirclePreset preset, float radius, int duration) {
        circle(level, center.add(0, 0.08, 0), UP, preset, radius, duration);
    }

    /** Secousse proportionnelle a la distance (pleine a l'epicentre, nulle a radius). */
    public static void shake(ServerLevel level, Vec3 pos, float power, int ticks, double radius) {
        for (ServerPlayer p : level.players()) {
            double d = p.position().distanceTo(pos);
            if (d > radius) continue;
            float scaled = power * (float) (1.0 - d / radius);
            if (scaled > 0.02F) ModNetwork.sendToPlayer(p, new CameraShakeS2C(scaled, ticks));
        }
    }

    /** Ligne de particules entre deux points (faisceaux, telegraphies). */
    public static void line(ServerLevel level, Vec3 from, Vec3 to, int rgb, float size, double step) {
        Vec3 delta = to.subtract(from);
        double len = delta.length();
        if (len < 1e-4) return;
        Vec3 dir = delta.scale(1.0 / len);
        AstralParticleOptions opt = AstralParticleOptions.rgb(rgb, size, 4);
        for (double t = 0; t <= len; t += step) {
            Vec3 p = from.add(dir.scale(t));
            level.sendParticles(opt, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
    }

    /** Explosion de losanges dans toutes les directions. */
    public static void burst(ServerLevel level, Vec3 pos, int rgb, int count, float speed) {
        AstralParticleOptions opt = AstralParticleOptions.rgb(rgb, 0.18F, 14);
        level.sendParticles(opt, pos.x, pos.y, pos.z, count, 0.2, 0.2, 0.2, speed);
    }

    /** Anneau horizontal instantane (sans packet de cercle, utile pour les petits effets). */
    public static void ring(ServerLevel level, Vec3 c, double radius, int rgb, int points) {
        AstralParticleOptions opt = AstralParticleOptions.rgb(rgb, 0.15F, 10);
        for (int i = 0; i < points; i++) {
            float a = (float) (Math.PI * 2) * i / points;
            level.sendParticles(opt, c.x + Mth.cos(a) * radius, c.y, c.z + Mth.sin(a) * radius, 1, 0, 0.02, 0, 0);
        }
    }

    private Fx() {}
}
