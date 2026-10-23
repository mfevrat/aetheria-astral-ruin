package com.aetheria.astralruin.client.particle;

import com.aetheria.astralruin.config.AetheriaConfig;
import com.aetheria.astralruin.particle.AstralParticleOptions;
import com.aetheria.astralruin.particle.RuneCirclePreset;
import com.aetheria.astralruin.particle.RuneCirclePreset.Ring;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import com.aetheria.astralruin.Aetheria;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * Dessine un cercle runique en particules, recalcule chaque tick.
 * P(theta, t) = C + q(t) * (r cos theta, 0, r sin theta)
 * q(t) = q_base * Rx(a sin(wt)) * Rz(a sin(0.7wt + 1.3)) * Ry(s t)
 */
public class RuneCircleEmitter {
    private static final float TAU = (float) (Math.PI * 2);
    private static final float HALF_PI = (float) (Math.PI / 2);
    private static final int MAX_PER_TICK = 600;

    private Vec3 center;
    private Vec3 prevCenter;
    private final Quaternionf base;
    private final RuneCirclePreset preset;
    private final float radius;
    private final int duration;
    private float density = 3.0F;
    private int age;

    public RuneCircleEmitter(Vec3 center, Vector3f normal, RuneCirclePreset preset, float radius, int duration) {
        this.center = center;
        this.prevCenter = center;
        Vector3f n = new Vector3f(normal);
        if (n.lengthSquared() < 1e-6F) n.set(0, 1, 0);
        this.base = new Quaternionf().rotationTo(new Vector3f(0, 1, 0), n.normalize());
        this.preset = preset;
        this.radius = radius;
        this.duration = Math.max(1, duration);
    }

    public void setCenter(Vec3 center) {
        this.center = center;
    }

    /** @return false quand l'emetteur est termine et doit etre retire. */
    public boolean tick(ClientLevel level) {
        prevCenter = center;
        if (++age > duration) return false;
        density = 3.0F * AetheriaConfig.PARTICLE_DENSITY.get().floatValue();
        // Avec les decalques, les particules ne servent plus qu'a faire scintiller les traits
        if (AetheriaConfig.RENDER_DECALS.get()) density *= 0.3F;
        ParticleStatus status = Minecraft.getInstance().options.particles().get();
        if (status == ParticleStatus.DECREASED) density *= 0.5F;

        float grow = growFactor(age);
        if (grow <= 0.01F) return true;
        int budget = MAX_PER_TICK;
        List<Ring> rings = preset.rings;
        int count = status == ParticleStatus.MINIMAL ? 1 : rings.size();
        for (int i = 0; i < count && budget > 0; i++) {
            Ring ring = rings.get(i);
            Quaternionf q = ringRotation(ring, age);
            float r = radius * ring.radiusFactor() * grow;
            budget -= switch (ring.kind()) {
                case CIRCLE -> drawCircle(level, q, r, ring.color(), budget);
                case STAR -> drawStar(level, q, r, ring.sides(), ring.step(), ring.color(), budget);
                case RUNES -> drawRunes(level, q, r, ring.sides(), ring.color(), budget);
            };
        }
        return true;
    }

    private float growFactor(float time) {
        if (preset.expanding) return Mth.clamp(time / duration, 0F, 1F);
        float in = easeOutBack(Math.min(1F, time / 15F));
        float out = Mth.clamp((duration - time) / 10F, 0F, 1F);
        return in * out;
    }

    private static float easeOutBack(float t) {
        final float c1 = 1.70158F, c3 = c1 + 1F;
        float u = t - 1F;
        return 1F + c3 * u * u * u + c1 * u * u;
    }

    private Quaternionf ringRotation(Ring ring, float time) {
        float amp = ring.wobble() == 0F ? 0F : 0.35F;
        float tiltX = Mth.sin(time * ring.wobble()) * amp;
        float tiltZ = Mth.sin(time * ring.wobble() * 0.7F + 1.3F) * amp;
        return new Quaternionf(base).rotateX(tiltX).rotateZ(tiltZ).rotateY(ring.spin() * time);
    }

    // =====================================================================
    //  Decalques : un quad texture par anneau, rotation interpolee au partial tick
    // =====================================================================
    private static final float TEXTURE_RADIUS = 0.92F; // rayon du trait dans les textures generees

    private static ResourceLocation texture(Ring ring) {
        return switch (ring.kind()) {
            case CIRCLE -> Aetheria.id("textures/effect/ring.png");
            case RUNES -> Aetheria.id("textures/effect/rune_ring.png");
            case STAR -> Aetheria.id("textures/effect/star_" + ring.sides() + "_" + ring.step() + ".png");
        };
    }

    public void render(PoseStack pose, MultiBufferSource buffers, Vec3 cam, float partialTick) {
        float time = Math.max(0F, age - 1 + partialTick);
        float grow = growFactor(time);
        if (grow <= 0.01F) return;
        float fade = preset.expanding
                ? 1F - Mth.clamp(time / duration, 0F, 1F)
                : Mth.clamp(time / 6F, 0F, 1F) * Mth.clamp((duration - time) / 10F, 0F, 1F);
        if (fade <= 0.01F) return;
        float pulse = 0.85F + 0.15F * Mth.sin(time * 0.3F);

        Vec3 c = prevCenter.lerp(center, partialTick);
        pose.pushPose();
        pose.translate(c.x - cam.x, c.y - cam.y, c.z - cam.z);
        List<Ring> rings = preset.rings;
        for (int i = 0; i < rings.size(); i++) {
            Ring ring = rings.get(i);
            float half = radius * ring.radiusFactor() * grow / TEXTURE_RADIUS;
            if (half < 0.05F) continue;
            pose.pushPose();
            pose.mulPose(ringRotation(ring, time));
            pose.translate(0, i * 0.004F, 0); // evite le z-fighting entre anneaux
            float k = fade * pulse;
            int rgb = ring.color();
            quad(buffers.getBuffer(RenderType.eyes(texture(ring))), pose.last(), half,
                    (rgb >> 16 & 0xFF) / 255F * k, (rgb >> 8 & 0xFF) / 255F * k, (rgb & 0xFF) / 255F * k);
            pose.popPose();
        }
        pose.popPose();
    }

    /** Quad double face dans le plan XZ local. Le melange additif ignore l'alpha : on module la couleur. */
    private static void quad(VertexConsumer vc, PoseStack.Pose last, float h, float r, float g, float b) {
        Matrix4f m = last.pose();
        Matrix3f n = last.normal();
        vertex(vc, m, n, -h, -h, 0, 0, r, g, b);
        vertex(vc, m, n, -h, h, 0, 1, r, g, b);
        vertex(vc, m, n, h, h, 1, 1, r, g, b);
        vertex(vc, m, n, h, -h, 1, 0, r, g, b);
        vertex(vc, m, n, h, -h, 1, 0, r, g, b);
        vertex(vc, m, n, h, h, 1, 1, r, g, b);
        vertex(vc, m, n, -h, h, 0, 1, r, g, b);
        vertex(vc, m, n, -h, -h, 0, 0, r, g, b);
    }

    private static void vertex(VertexConsumer vc, Matrix4f m, Matrix3f n, float x, float z, float u, float v,
                               float r, float g, float b) {
        vc.vertex(m, x, 0F, z).color(r, g, b, 1F).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT).normal(n, 0F, 1F, 0F).endVertex();
    }

    private int drawCircle(ClientLevel level, Quaternionf q, float r, int color, int budget) {
        int n = Math.max(12, Mth.ceil(TAU * r * density));
        int spawned = 0;
        // moitie des points par tick, en alternance : la duree de vie de 3 ticks garde le trait continu
        for (int i = age & 1; i < n && spawned < budget; i += 2) {
            float theta = TAU * i / n;
            spawnLocal(level, q, Mth.cos(theta) * r, 0F, Mth.sin(theta) * r, color, 0.12F);
            spawned++;
        }
        return spawned;
    }

    private int drawStar(ClientLevel level, Quaternionf q, float r, int n, int k, int color, int budget) {
        if (n < 3) return 0;
        float chord = 2F * r * Mth.sin((float) Math.PI * k / n);
        int perEdge = Math.max(4, Mth.ceil(chord * density));
        int spawned = 0;
        for (int j = 0; j < n && spawned < budget; j++) {
            // meme orientation que les textures star_n_k (premier sommet a -90 degres)
            float a0 = TAU * j / n - HALF_PI, a1 = TAU * ((j + k) % n) / n - HALF_PI;
            float x0 = Mth.cos(a0) * r, z0 = Mth.sin(a0) * r;
            float x1 = Mth.cos(a1) * r, z1 = Mth.sin(a1) * r;
            for (int s = age & 1; s <= perEdge && spawned < budget; s += 2) {
                float t = (float) s / perEdge;
                spawnLocal(level, q, Mth.lerp(t, x0, x1), 0F, Mth.lerp(t, z0, z1), color, 0.10F);
                spawned++;
            }
        }
        return spawned;
    }

    /** n marques runiques en croix : trait radial + barre tangente. */
    private int drawRunes(ClientLevel level, Quaternionf q, float r, int n, int color, int budget) {
        int spawned = 0;
        float len = Math.max(0.3F, r * 0.12F);
        for (int j = 0; j < n && spawned < budget; j++) {
            float a = TAU * j / n;
            float cx = Mth.cos(a), cz = Mth.sin(a);
            float tx = -cz, tz = cx;
            for (int s = -2; s <= 2; s++) {
                float d = s / 2F * len * 0.5F;
                spawnLocal(level, q, cx * (r + d), 0F, cz * (r + d), color, 0.09F);
                spawnLocal(level, q, cx * r + tx * d, 0F, cz * r + tz * d, color, 0.09F);
                spawned += 2;
            }
        }
        return spawned;
    }

    private void spawnLocal(ClientLevel level, Quaternionf q, float lx, float ly, float lz, int rgb, float size) {
        Vector3f p = q.transform(new Vector3f(lx, ly, lz));
        level.addParticle(AstralParticleOptions.rgb(rgb, size, 3), true,
                center.x + p.x, center.y + p.y, center.z + p.z, 0, 0, 0);
    }
}
