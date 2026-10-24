package com.aetheria.astralruin.client.vfx;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.network.SpawnVfxS2C;
import com.aetheria.astralruin.vfx.VfxType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/** Une instance d'effet visuel cote client. */
public class Vfx {
    static final ResourceLocation SLASH = Aetheria.id("textures/vfx/slash.png");
    static final ResourceLocation LIGHTNING = Aetheria.id("textures/vfx/lightning.png");
    static final ResourceLocation RING = Aetheria.id("textures/vfx/shockwave.png");
    static final ResourceLocation BEAM = Aetheria.id("textures/vfx/beam.png");
    static final ResourceLocation PILLAR = Aetheria.id("textures/vfx/pillar.png");
    static final ResourceLocation FLARE = Aetheria.id("textures/vfx/flare.png");
    static final ResourceLocation CHAIN = Aetheria.id("textures/vfx/chain.png");

    private final SpawnVfxS2C d;
    private int age;
    private List<Vec3> bolt = List.of();

    public Vfx(SpawnVfxS2C data) {
        this.d = data;
    }

    /** @return false quand l'effet est termine. */
    public boolean tick(ClientLevel level) {
        if (d.type() == VfxType.LIGHTNING && age % 2 == 0) bolt = buildBolt(d.a(), d.b(), d.seed() + age / 2);
        return ++age <= d.duration();
    }

    private float life(float pt) {
        return Mth.clamp((age - 1 + pt) / Math.max(1, d.duration()), 0F, 1F);
    }

    private static Vector3f rel(Vec3 p, Vec3 cam) {
        return new Vector3f((float) (p.x - cam.x), (float) (p.y - cam.y), (float) (p.z - cam.z));
    }

    public void render(PoseStack pose, MultiBufferSource buffers, Camera camera, ClientLevel level, float pt) {
        Vec3 cam = camera.getPosition();
        float t = life(pt);
        float time = level.getGameTime() + pt;
        int c = d.color();
        float r = (c >> 16 & 0xFF) / 255F, g = (c >> 8 & 0xFF) / 255F, b = (c & 0xFF) / 255F;
        PoseStack.Pose last = pose.last();
        switch (d.type()) {
            case SLASH -> {
                float fade = t < 0.2F ? t / 0.2F : 1F - (t - 0.2F) / 0.8F;
                float grow = 0.75F + 0.35F * (1F - (1F - t) * (1F - t));
                Vec3 fwd = d.b().normalize();
                Vec3 side = fwd.cross(new Vec3(0, 1, 0));
                if (side.lengthSqr() < 1e-4) side = new Vec3(1, 0, 0);
                side = side.normalize();
                Vec3 up = side.cross(fwd).normalize();
                double roll = Math.toRadians(d.param());
                Vec3 ax = side.scale(Math.cos(roll)).add(up.scale(Math.sin(roll)));
                Vector3f axis = new Vector3f((float) ax.x, (float) ax.y, (float) ax.z);
                Vector3f forward = new Vector3f((float) fwd.x, (float) fwd.y, (float) fwd.z);
                VfxRender.plane(buffers.getBuffer(RenderType.eyes(SLASH)), last, rel(d.a(), cam), axis, forward,
                        d.size() * grow, r * fade, g * fade, b * fade);
            }
            case RING, IMPLOSION -> {
                float k = d.type() == VfxType.RING ? 1F - (1F - t) * (1F - t) * (1F - t) : 1F - t * t;
                float fade = d.type() == VfxType.RING ? 1F - t : Math.min(1F, t * 3F);
                VfxRender.plane(buffers.getBuffer(RenderType.eyes(RING)), last, rel(d.a(), cam),
                        new Vector3f(1, 0, 0), new Vector3f(0, 0, 1), Math.max(0.05F, d.size() * k), r * fade, g * fade, b * fade);
            }
            case FLARE -> {
                float fade = t < 0.15F ? t / 0.15F : 1F - (t - 0.15F) / 0.85F;
                float s = d.size() * (0.8F + 0.25F * Mth.sin(time * 0.9F));
                // meme orientation face camera que les particules vanilla
                var orient = net.minecraft.client.Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation();
                Vector3f left = orient.transform(new Vector3f(1, 0, 0));
                Vector3f upv = orient.transform(new Vector3f(0, 1, 0));
                VfxRender.plane(buffers.getBuffer(RenderType.eyes(FLARE)), last, rel(d.a(), cam), left, upv, s, r * fade, g * fade, b * fade);
            }
            case PILLAR -> {
                float grow = Mth.clamp(t / 0.12F, 0F, 1F), shrink = Mth.clamp((1F - t) / 0.2F, 0F, 1F);
                float radius = d.size() * grow * shrink;
                if (radius < 0.02F) return;
                var vc = buffers.getBuffer(RenderType.eyes(PILLAR));
                Vector3f base = rel(d.a(), cam);
                VfxRender.cylinder(vc, last, base, radius, d.param(), 14, -time * 0.05F, d.param() / 6F, r, g, b);
                VfxRender.cylinder(vc, last, base, radius * 0.45F, d.param(), 10, -time * 0.09F, d.param() / 4F, 1F, 1F, 1F);
            }
            case BEAM -> {
                float fade = Mth.clamp(Math.min(t / 0.1F, (1F - t) / 0.15F), 0F, 1F);
                Vector3f a = rel(d.a(), cam), bb = rel(d.b(), cam);
                float len = a.distance(bb);
                var vc = buffers.getBuffer(RenderType.eyes(BEAM));
                VfxRender.ribbon(vc, last, a, bb, d.size() * fade, -time * 0.15F, len / 3F, r, g, b);
                VfxRender.ribbon(vc, last, a, bb, d.size() * 0.4F * fade, -time * 0.25F, len / 2F, 1F, 1F, 1F);
            }
            case LIGHTNING -> {
                float fade = 1F - t * t;
                var vc = buffers.getBuffer(RenderType.eyes(LIGHTNING));
                for (int i = 0; i + 1 < bolt.size(); i++) {
                    VfxRender.ribbon(vc, last, rel(bolt.get(i), cam), rel(bolt.get(i + 1), cam), d.size(),
                            0, 1, r * fade, g * fade, b * fade);
                }
            }
            case TETHER -> {
                Entity ea = level.getEntity(d.entityA()), eb = level.getEntity(d.entityB());
                if (ea == null || eb == null) return;
                Vec3 pa = ea.getPosition(pt).add(0, ea.getBbHeight() * 0.6, 0);
                Vec3 pb = eb.getPosition(pt).add(0, eb.getBbHeight() * 0.6, 0);
                float fade = Mth.clamp((1F - t) / 0.2F, 0F, 1F);
                float len = (float) pa.distanceTo(pb);
                VfxRender.ribbon(buffers.getBuffer(RenderType.eyes(CHAIN)), last, rel(pa, cam), rel(pb, cam), d.size(),
                        time * 0.1F, len / Math.max(0.2F, d.size() * 2F), r * fade, g * fade, b * fade);
            }
        }
    }

    /** Eclair : deplacement du point milieu + deux fourches. */
    private static List<Vec3> buildBolt(Vec3 from, Vec3 to, int seed) {
        RandomSource rnd = RandomSource.create(seed);
        List<Vec3> pts = new ArrayList<>();
        pts.add(from);
        pts.add(to);
        double offset = from.distanceTo(to) * 0.25;
        for (int gen = 0; gen < 5; gen++) {
            List<Vec3> next = new ArrayList<>();
            for (int i = 0; i + 1 < pts.size(); i++) {
                Vec3 a = pts.get(i), b = pts.get(i + 1);
                Vec3 mid = a.add(b).scale(0.5).add((rnd.nextDouble() - 0.5) * offset, (rnd.nextDouble() - 0.5) * offset,
                        (rnd.nextDouble() - 0.5) * offset);
                next.add(a);
                next.add(mid);
            }
            next.add(pts.get(pts.size() - 1));
            pts = next;
            offset *= 0.55;
        }
        return pts;
    }
}
