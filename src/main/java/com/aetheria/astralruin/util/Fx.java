package com.aetheria.astralruin.util;

import com.aetheria.astralruin.network.CameraShakeS2C;
import com.aetheria.astralruin.network.ModNetwork;
import com.aetheria.astralruin.network.SpawnRuneCircleS2C;
import com.aetheria.astralruin.particle.AstralParticleOptions;
import com.aetheria.astralruin.particle.RuneCirclePreset;
import com.aetheria.astralruin.network.SpawnVfxS2C;
import com.aetheria.astralruin.particle.AstralParticleOptions.Kind;
import com.aetheria.astralruin.vfx.VfxType;
import net.minecraft.world.entity.Entity;
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

    // =====================================================================
    //  Effets visuels textures (moteur VFX client)
    // =====================================================================
    public static final int ORANGE = 0xFFA040;
    public static final int CYAN = 0x8CE1FF;
    public static final int RED = 0xFF4060;

    public static void vfx(ServerLevel level, VfxType type, Vec3 a, Vec3 b, int entityA, int entityB, int color,
                           float size, int duration, float param) {
        ModNetwork.sendNear(level, a, 128, new SpawnVfxS2C(type, a, b, entityA, entityB, color, size, duration, param,
                level.random.nextInt()));
    }

    /** Arc de lame devant pos, oriente selon dir ; roll en degres (0 = horizontal, 90 = vertical). */
    public static void slash(ServerLevel level, Vec3 center, Vec3 dir, float roll, float radius, int color, int duration) {
        vfx(level, VfxType.SLASH, center, dir, -1, -1, color, radius, duration, roll);
    }

    public static void lightning(ServerLevel level, Vec3 from, Vec3 to, int color, float width, int duration) {
        vfx(level, VfxType.LIGHTNING, from, to, -1, -1, color, width, duration, 0);
    }

    public static void shockwave(ServerLevel level, Vec3 center, float radius, int color, int duration) {
        vfx(level, VfxType.RING, center.add(0, 0.1, 0), center, -1, -1, color, radius, duration, 0);
    }

    public static void implosion(ServerLevel level, Vec3 center, float radius, int color, int duration) {
        vfx(level, VfxType.IMPLOSION, center.add(0, 0.1, 0), center, -1, -1, color, radius, duration, 0);
    }

    public static void beam(ServerLevel level, Vec3 from, Vec3 to, int color, float width, int duration) {
        vfx(level, VfxType.BEAM, from, to, -1, -1, color, width, duration, 0);
    }

    public static void pillar(ServerLevel level, Vec3 base, float height, float radius, int color, int duration) {
        vfx(level, VfxType.PILLAR, base, base, -1, -1, color, radius, duration, height);
    }

    public static void flare(ServerLevel level, Vec3 pos, float size, int color, int duration) {
        vfx(level, VfxType.FLARE, pos, pos, -1, -1, color, size, duration, 0);
    }

    public static void tether(ServerLevel level, Entity a, Entity b, int color, float width, int duration) {
        vfx(level, VfxType.TETHER, a.position(), b.position(), a.getId(), b.getId(), color, width, duration, 0);
    }

    /** Nuage de particules d'une famille donnee. */
    public static void particles(ServerLevel level, Kind kind, Vec3 pos, int count, double spread, double speed,
                                 int color, float scale, int life) {
        level.sendParticles(AstralParticleOptions.of(kind, color, scale, life), pos.x, pos.y, pos.z, count,
                spread, spread, spread, speed);
    }

    /** Impact standard : eclat + onde + braises + eclats de cristal. */
    public static void impact(ServerLevel level, Vec3 pos, float radius, int color) {
        flare(level, pos.add(0, 0.5, 0), radius * 1.2F, color, 10);
        shockwave(level, pos, radius * 1.4F, color, 14);
        particles(level, Kind.EMBER, pos.add(0, 0.4, 0), (int) (12 * radius), radius * 0.3, 0.12, color, 0.18F, 18);
        particles(level, Kind.SHARD, pos.add(0, 0.6, 0), (int) (6 * radius), radius * 0.2, 0.25, color, 0.15F, 30);
    }

    private Fx() {}
}
