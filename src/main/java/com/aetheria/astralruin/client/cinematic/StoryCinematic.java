package com.aetheria.astralruin.client.cinematic;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.client.camera.CameraShakeHandler;
import com.aetheria.astralruin.client.particle.RuneCircleEmitter;
import com.aetheria.astralruin.client.particle.RuneCircleManager;
import com.aetheria.astralruin.client.vfx.VfxManager;
import com.aetheria.astralruin.client.vfx.VfxRender;
import com.aetheria.astralruin.entity.boss.ValeriusEntity;
import com.aetheria.astralruin.network.SpawnVfxS2C;
import com.aetheria.astralruin.particle.AstralParticleOptions;
import com.aetheria.astralruin.particle.AstralParticleOptions.Kind;
import com.aetheria.astralruin.particle.RuneCirclePreset;
import com.aetheria.astralruin.registry.ModEntities;
import com.aetheria.astralruin.registry.ModItems;
import com.aetheria.astralruin.registry.ModSounds;
import com.aetheria.astralruin.vfx.VfxType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Cinematique d'histoire d'Aetheria (premiere connexion).
 * Un theatre est dessine cote client tres haut dans le ciel (dome de nebuleuse + sol runique) :
 * le monde n'est jamais modifie. Les acteurs sont de vrais modeles du mod rendus sans etre ajoutes au monde.
 *
 * Scenes : 1. La pluie d'etoiles  2. La naissance de la magie  3. La forge du gardien
 *          4. La chute de Valerius  5. Retour au monde (titre).
 */
public final class StoryCinematic {
    public static final int LENGTH = 840;
    private static final int[] SCENE = {0, 170, 340, 520, 700};
    private static final float DOME = 42F, GROUND = 30F;
    private static final ResourceLocation SKY = Aetheria.id("textures/cinematic/stage_sky.png");
    private static final ResourceLocation FLOOR = Aetheria.id("textures/cinematic/stage_ground.png");

    public record Shot(Vec3 pos, Vec3 look, boolean cut) {}
    private record Key(int t, Vec3 pos, Vec3 look) {}

    private static final List<List<Key>> SHOTS = new ArrayList<>();
    private static final List<ArmorStand> ANCIENTS = new ArrayList<>();
    private static final RandomSource RNG = RandomSource.create();
    @Nullable private static ValeriusEntity valerius;
    private static Vec3 stage = Vec3.ZERO;
    private static boolean ancientsAlive;
    private static float corruption;
    private static int lastTick;

    // =====================================================================
    //  Mise en place
    // =====================================================================
    public static void start(Minecraft mc) {
        Player p = mc.player;
        if (p == null || mc.level == null) return;
        Vec3 home = p.position();
        Vec3 eye = p.getEyePosition();
        Vec3 look = eye.add(p.getLookAngle().scale(10));
        Vec3 fwd = Vec3.directionFromRotation(0, p.getYRot());
        stage = new Vec3(p.getX(), 290, p.getZ());
        Vec3 s = stage;
        SHOTS.clear();
        SHOTS.add(List.of(k(0, s.add(-26, 4, -26), s.add(0, 18, 0)), k(80, s.add(-18, 9, -8), s.add(0, 6, 0)),
                k(120, s.add(-13, 6, 10), s.add(0, 1, 0)), k(170, s.add(-8, 3, 8), s.add(0, 1, 0))));
        SHOTS.add(List.of(k(0, s.add(7, 3, 0), s.add(0, 3, 0)), k(60, s.add(0, 4, 7), s.add(0, 3, 0)),
                k(120, s.add(-7, 5, 0), s.add(0, 3.5, 0)), k(170, s.add(0, 8, -10), s.add(0, 4, 0))));
        SHOTS.add(List.of(k(0, s.add(0, 2.5, -14), s.add(0, 3, 0)), k(60, s.add(5, 2, -9), s.add(0, 3, 0)),
                k(110, s.add(0, 2.2, -6.5), s.add(0, 3.2, 0)), k(150, s.add(0, 3.3, -4.6), s.add(0, 3.6, 0)),
                k(180, s.add(0, 6, -12), s.add(0, 3, 0))));
        SHOTS.add(List.of(k(0, s.add(0, 1, -7), s.add(0, 7, 0)), k(70, s.add(7, 4, -6), s.add(0, 5, 0)),
                k(130, s.add(0, 10, -14), s.add(0, 6, 0)), k(180, s.add(0, 16, -20), s.add(0, 8, 0))));
        SHOTS.add(List.of(k(0, home.add(0, 40, 0).subtract(fwd.scale(15)), home.add(fwd.scale(40))),
                k(60, home.add(fwd.scale(4)).add(0, 2, 0), home.add(0, 1.6, 0)),
                k(110, home.add(fwd.scale(4.1)).add(0, 2, 0), home.add(0, 1.6, 0)), k(140, eye, look)));

        // Acteurs : Valerius (forme kneeling de l'animation d'apparition) et trois Anciens en armure Astrale
        valerius = new ValeriusEntity(ModEntities.VALERIUS.get(), mc.level);
        valerius.setCinematicState(true, 1);
        place(valerius, s, 180F);
        ANCIENTS.clear();
        for (int i = 0; i < 3; i++) {
            double a = Math.toRadians(90 + i * 120);
            Vec3 pos = s.add(Math.cos(a) * 8, 0, Math.sin(a) * 8);
            ArmorStand stand = new ArmorStand(EntityType.ARMOR_STAND, mc.level);
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("ShowArms", true);
            tag.putBoolean("NoBasePlate", true);
            CompoundTag poseTag = new CompoundTag();
            poseTag.put("RightArm", floats(-125F, 0F, 8F));
            poseTag.put("LeftArm", floats(-105F, 0F, -12F));
            tag.put("Pose", poseTag);
            stand.load(tag); // Entity#load (public) -> lit ShowArms, NoBasePlate et la pose des bras
            stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.ASTRAL_HELMET.get()));
            stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.ASTRAL_CHESTPLATE.get()));
            stand.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.ASTRAL_LEGGINGS.get()));
            stand.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.ASTRAL_BOOTS.get()));
            stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.ARCHMAGE_STAFF.get()));
            Vec3 to = s.subtract(pos);
            place(stand, pos, (float) (Mth.atan2(-to.x, to.z) * (180.0 / Math.PI)));
            ANCIENTS.add(stand);
        }
        ancientsAlive = true;
        corruption = 0F;
        lastTick = -1;
    }

    private static Key k(int t, Vec3 pos, Vec3 look) { return new Key(t, pos, look); }

    private static net.minecraft.nbt.ListTag floats(float a, float b, float c) {
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        list.add(net.minecraft.nbt.FloatTag.valueOf(a));
        list.add(net.minecraft.nbt.FloatTag.valueOf(b));
        list.add(net.minecraft.nbt.FloatTag.valueOf(c));
        return list;
    }

    private static void place(net.minecraft.world.entity.LivingEntity e, Vec3 pos, float yaw) {
        e.setPos(pos.x, pos.y, pos.z);
        e.setOldPosAndRot();
        e.setYRot(yaw);
        e.yRotO = yaw;
        e.yBodyRot = yaw;
        e.yBodyRotO = yaw;
        e.yHeadRot = yaw;
        e.yHeadRotO = yaw;
    }

    public static void stop() {
        valerius = null;
        ANCIENTS.clear();
        SHOTS.clear();
    }

    // =====================================================================
    //  Camera
    // =====================================================================
    private static int scene(float t) {
        int i = SCENE.length - 1;
        while (i > 0 && t < SCENE[i]) i--;
        return i;
    }

    public static Shot shot(int t) {
        int sc = scene(t);
        if (SHOTS.size() <= sc) return new Shot(stage, stage.add(0, 0, 1), true);
        List<Key> keys = SHOTS.get(sc);
        int local = t - SCENE[sc];
        int i = 0;
        while (i < keys.size() - 2 && keys.get(i + 1).t() <= local) i++;
        Key k1 = keys.get(i), k2 = keys.get(Math.min(i + 1, keys.size() - 1));
        Key k0 = keys.get(Math.max(i - 1, 0)), k3 = keys.get(Math.min(i + 2, keys.size() - 1));
        float u = Mth.clamp((local - k1.t()) / (float) Math.max(1, k2.t() - k1.t()), 0F, 1F);
        u = u * u * (3F - 2F * u);
        return new Shot(catmull(k0.pos(), k1.pos(), k2.pos(), k3.pos(), u),
                catmull(k0.look(), k1.look(), k2.look(), k3.look(), u), local == 0);
    }

    private static Vec3 catmull(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, double t) {
        double t2 = t * t, t3 = t2 * t;
        return p0.scale(-0.5 * t3 + t2 - 0.5 * t).add(p1.scale(1.5 * t3 - 2.5 * t2 + 1.0))
                .add(p2.scale(-1.5 * t3 + 2.0 * t2 + 0.5 * t)).add(p3.scale(0.5 * t3 - 0.5 * t2));
    }

    // =====================================================================
    //  Chronologie
    // =====================================================================
    public static void tick(int t) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || t == lastTick) return;
        lastTick = t;
        Vec3 s = stage;
        if (valerius != null && t >= 380) valerius.tickCount++;
        for (ArmorStand a : ANCIENTS) a.tickCount++;

        // --- Scene 1 : la pluie d'etoiles ---
        if (t >= 4 && t <= 150 && t % 4 == 0) {
            Vec3 impact = s.add(RNG.nextGaussian() * 9, 0.1, RNG.nextGaussian() * 9);
            Vec3 from = impact.add(RNG.nextGaussian() * 12, 45, RNG.nextGaussian() * 12);
            vfx(VfxType.BEAM, from, impact, 0xFFF0C8, 0.5F, 6, 0);
            vfx(VfxType.FLARE, impact.add(0, 0.5, 0), impact, 0xFFD890, 2.6F, 10, 0);
            vfx(VfxType.RING, impact, impact, 0xFFD36B, 4F, 12, 0);
            particles(Kind.EMBER, impact.add(0, 0.3, 0), 8, 0.6, 0xFFB050, 0.25F, 18);
            if (t % 12 == 0) sound(ModSounds.THUNDER.get(), 1.4F + RNG.nextFloat() * 0.4F, 0.35F);
        }
        if (t == 112) sound(ModSounds.METEOR_FALL.get(), 0.8F, 1.0F);
        if (t == 118) vfx(VfxType.BEAM, s.add(10, 60, 6), s, 0xFFFFFF, 3F, 10, 0);
        if (t == 120) {
            vfx(VfxType.PILLAR, s, s, 0xFFF4D0, 3F, 40, 40F);
            vfx(VfxType.RING, s, s, 0xFFFFFF, 30F, 25, 0);
            vfx(VfxType.FLARE, s.add(0, 1, 0), s, 0xFFFFFF, 12F, 18, 0);
            circle(s.add(0, 0.1, 0), new Vector3f(0, 1, 0), RuneCirclePreset.TRANSITION, 8F, 230);
            particles(Kind.SHARD, s.add(0, 1, 0), 60, 1.2, 0xFFE9A8, 0.25F, 40);
            CameraShakeHandler.shake(1.0F, 30);
            sound(ModSounds.JUDGEMENT_IMPACT.get(), 1.0F, 1.2F);
        }
        if (t > 125 && t < 170 && t % 2 == 0) particles(Kind.EMBER, s.add(0, 0.2, 0), 3, 2.0, 0xFFB050, 0.2F, 25);

        // --- Scene 2 : la naissance de la magie ---
        if (t == 178) circle(s.add(0, 3, 0), new Vector3f(1, 0, 0), RuneCirclePreset.NOVA, 2.5F, 160);
        if (t == 200) circle(s.add(0, 7, 0), new Vector3f(0, 1, 0), RuneCirclePreset.JUDGEMENT, 4F, 140);
        if (t == 230) circle(s.add(0, 3, 0), new Vector3f(0, 0, 1), RuneCirclePreset.BARRIER, 3.2F, 110);
        if (t == 260) circle(s.add(0, 3, 0), new Vector3f(0.7F, 0, 0.7F), RuneCirclePreset.TRAP, 3.8F, 80);
        if (t > 175 && t < 335) {
            double a = t * 0.35;
            particles(Kind.GLYPH, s.add(Math.cos(a) * 3.5, 1 + (t % 40) * 0.12, Math.sin(a) * 3.5), 1, 0.05, 0xFFD36B, 0.3F, 30);
            if (t % 3 == 0) particles(Kind.STAR, s.add(0, 3, 0), 2, 1.5, 0xC8A8FF, 0.25F, 14);
        }
        if (t == 250 || t == 300) sound(ModSounds.CRYSTAL_RESONANCE.get(), t == 250 ? 1.0F : 1.3F, 1.0F);
        if (t == 322) vfx(VfxType.FLARE, s.add(0, 3, 0), s, 0xFFFFFF, 6F, 18, 0);

        // --- Scene 3 : la forge du gardien ---
        if (t == 342) circle(s.add(0, 0.1, 0), new Vector3f(0, 1, 0), RuneCirclePreset.TRANSITION, 9F, 180);
        if (t == 360) {
            vfx(VfxType.PILLAR, s, s, 0xE6D0FF, 1.8F, 150, 30F);
            sound(ModSounds.VALERIUS_TRANSITION.get(), 1.0F, 1.0F);
        }
        if (t >= 345 && t < 515 && t % 6 == 0) {
            for (ArmorStand a : ANCIENTS) {
                Vec3 tip = a.position().add(0, 2.3, 0).add(Vec3.directionFromRotation(0, a.getYRot()).scale(0.6));
                vfx(VfxType.BEAM, tip, s.add(0, 3.5, 0), 0xC8A8FF, 0.35F, 7, 0);
                vfx(VfxType.FLARE, tip, tip, 0xE0D0FF, 0.7F, 7, 0);
            }
        }
        if (t >= 345 && t < 470 && t % 20 == 0) vfx(VfxType.IMPLOSION, s, s, 0x9B5CFF, 10F, 20, 0);
        if (t == 470) {
            sound(ModSounds.VALERIUS_ROAR.get(), 1.0F, 1.2F);
            vfx(VfxType.RING, s, s, 0xFFFFFF, 20F, 24, 0);
            vfx(VfxType.FLARE, s.add(0, 3.2, 0), s, 0xFFFFFF, 6F, 16, 0);
            CameraShakeHandler.shake(0.9F, 30);
            for (int i = 0; i < 4; i++) {
                double a = Math.PI / 2 * i + 0.4;
                Vec3 p = s.add(Math.cos(a) * 12, 0, Math.sin(a) * 12);
                vfx(VfxType.LIGHTNING, p.add(0, 28, 0), p, 0xFFFFFF, 0.6F, 12, 0);
            }
        }

        // --- Scene 4 : la chute ---
        if (t == 520 && valerius != null) {
            valerius.setCinematicState(false, 2);
            place(valerius, s.add(0, 3, 0), 180F);
            circle(s.add(0, 0.1, 0), new Vector3f(0, 1, 0), RuneCirclePreset.ECLIPSE, 12F, 180);
            circle(s.add(0, 12, 0), new Vector3f(0, 1, 0), RuneCirclePreset.ECLIPSE, 8F, 180);
        }
        if (t >= 520 && t < 700) {
            corruption = Mth.clamp((t - 520) / 80F, 0F, 1F);
            if (t % 7 == 0) {
                Vec3 to = s.add(RNG.nextGaussian() * 10, 0.1, RNG.nextGaussian() * 10);
                vfx(VfxType.LIGHTNING, s.add(0, 5, 0), to, 0xFF3050, 0.45F, 8, 0);
                vfx(VfxType.FLARE, to, to, 0xFF4060, 1.6F, 8, 0);
            }
            if (t % 2 == 0) particles(Kind.SMOKE, s.add(RNG.nextGaussian() * 8, 1 + RNG.nextFloat() * 6, RNG.nextGaussian() * 8),
                    2, 1.0, 0x2A0A20, 1.6F, 50);
            if (t % 15 == 0) vfx(VfxType.IMPLOSION, s, s, 0xFF3050, 14F, 15, 0);
        }
        if ((t == 545 || t == 615) && valerius != null) {
            valerius.triggerAnim("main", "enrage");
            sound(ModSounds.VALERIUS_ROAR.get(), 0.7F, 1.0F);
            CameraShakeHandler.shake(0.8F, 25);
        }
        if (t == 560 && ancientsAlive) {
            for (ArmorStand a : ANCIENTS) {
                vfx(VfxType.LIGHTNING, s.add(0, 5, 0), a.position().add(0, 1.2, 0), 0xFF3050, 0.7F, 12, 0);
                vfx(VfxType.FLARE, a.position().add(0, 1.2, 0), s, 0xFFFFFF, 3F, 14, 0);
                particles(Kind.SMOKE, a.position().add(0, 1, 0), 20, 0.5, 0x1A0A20, 1.4F, 50);
                particles(Kind.SHARD, a.position().add(0, 1.2, 0), 20, 0.4, 0xFFD36B, 0.2F, 30);
            }
            ancientsAlive = false;
            sound(ModSounds.THUNDER.get(), 0.8F, 1.3F);
            CameraShakeHandler.shake(0.9F, 25);
        }
        if (t == 650) sound(ModSounds.GRAVITY.get(), 0.8F, 1.2F);
        if (t == 690) {
            vfx(VfxType.FLARE, s.add(0, 6, 0), s, 0xFFFFFF, 20F, 20, 0);
            sound(ModSounds.JUDGEMENT_IMPACT.get(), 0.8F, 1.2F);
        }
    }

    private static void vfx(VfxType type, Vec3 a, Vec3 b, int color, float size, int duration, float param) {
        VfxManager.add(new SpawnVfxS2C(type, a, b, -1, -1, color, size, duration, param, RNG.nextInt()));
    }

    private static void circle(Vec3 c, Vector3f n, RuneCirclePreset p, float r, int d) {
        RuneCircleManager.add(new RuneCircleEmitter(c, n, p, r, d));
    }

    private static void particles(Kind kind, Vec3 pos, int count, double spread, int color, float scale, int life) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        for (int i = 0; i < count; i++) {
            mc.level.addParticle(AstralParticleOptions.of(kind, color, scale, life), true,
                    pos.x + RNG.nextGaussian() * spread, pos.y + RNG.nextGaussian() * spread, pos.z + RNG.nextGaussian() * spread,
                    RNG.nextGaussian() * 0.04, RNG.nextGaussian() * 0.04 + 0.02, RNG.nextGaussian() * 0.04);
        }
    }

    /** Son joue a la position de la camera de cinematique (l'auditeur suit la camera). */
    private static void sound(SoundEvent sound, float pitch, float volume) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Vec3 at = mc.gameRenderer.getMainCamera().getPosition();
        mc.level.playLocalSound(at.x, at.y, at.z, sound, net.minecraft.sounds.SoundSource.MASTER, volume, pitch, false);
    }

    // =====================================================================
    //  Rendu du theatre et des acteurs
    // =====================================================================
    public static void render(PoseStack pose, MultiBufferSource.BufferSource buffers, Camera camera, float pt, int t) {
        Minecraft mc = Minecraft.getInstance();
        if (t >= SCENE[4] || mc.level == null) return;
        Vec3 cam = camera.getPosition();
        float tt = t + pt;

        // Dome et sol
        pose.pushPose();
        pose.translate(stage.x - cam.x, stage.y - cam.y, stage.z - cam.z);
        float g = 1F - corruption * 0.65F, b = 1F - corruption * 0.5F;
        VertexConsumer sky = buffers.getBuffer(RenderType.entityCutoutNoCull(SKY));
        int lat = 16, lon = 32;
        for (int i = 0; i < lat; i++) {
            double p0 = Math.PI * i / lat, p1 = Math.PI * (i + 1) / lat;
            for (int j = 0; j < lon; j++) {
                double a0 = Math.PI * 2 * j / lon + tt * 0.0004, a1 = Math.PI * 2 * (j + 1) / lon + tt * 0.0004;
                VfxRender.quad(sky, pose.last(), sph(p0, a0), sph(p1, a0), sph(p1, a1), sph(p0, a1),
                        (float) j / lon, (float) i / lat, (float) (j + 1) / lon, (float) (i + 1) / lat, 1F, g, b);
            }
        }
        VfxRender.plane(buffers.getBuffer(RenderType.entityCutoutNoCull(FLOOR)), pose.last(), new Vector3f(0, 0.02F, 0),
                new Vector3f(1, 0, 0), new Vector3f(0, 0, 1), GROUND, 1F, g, b);
        pose.popPose();
        buffers.endBatch();

        // Cristaux de la scene 2
        if (t >= 172 && t < 345) {
            float grow = Mth.clamp((tt - 172) / 20F, 0F, 1F) * Mth.clamp((345 - tt) / 10F, 0F, 1F);
            renderItem(pose, buffers, mc, new ItemStack(ModItems.ASTRAL_SHARD.get()), stage.add(0, 3 + Mth.sin(tt * 0.08F) * 0.2, 0),
                    cam, tt * 3F, 3.2F * grow);
            for (int i = 0; i < 5; i++) {
                double a = tt * 0.03 + i * Math.PI * 2 / 5;
                renderItem(pose, buffers, mc, new ItemStack(ModItems.MANA_CRYSTAL.get()),
                        stage.add(Math.cos(a) * 2.8, 2.2 + Math.sin(tt * 0.1 + i) * 0.4, Math.sin(a) * 2.8), cam, -tt * 4F, 1.6F * grow);
            }
        }

        // Acteurs
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        if (ancientsAlive && t >= SCENE[2]) {
            for (ArmorStand a : ANCIENTS) {
                dispatcher.render(a, a.getX() - cam.x, a.getY() - cam.y, a.getZ() - cam.z, a.getYRot(), pt, pose, buffers, LightTexture.FULL_BRIGHT);
            }
        }
        if (valerius != null && t >= 380) {
            dispatcher.render(valerius, valerius.getX() - cam.x, valerius.getY() - cam.y, valerius.getZ() - cam.z,
                    valerius.getYRot(), pt, pose, buffers, LightTexture.FULL_BRIGHT);
        }
        buffers.endBatch();
    }

    private static Vector3f sph(double phi, double theta) {
        return new Vector3f((float) (Math.sin(phi) * Math.cos(theta) * DOME), (float) (Math.cos(phi) * DOME),
                (float) (Math.sin(phi) * Math.sin(theta) * DOME));
    }

    private static void renderItem(PoseStack pose, MultiBufferSource buffers, Minecraft mc, ItemStack stack, Vec3 at, Vec3 cam,
                                   float spin, float scale) {
        if (scale <= 0.01F) return;
        pose.pushPose();
        pose.translate(at.x - cam.x, at.y - cam.y, at.z - cam.z);
        pose.mulPose(Axis.YP.rotationDegrees(spin));
        pose.scale(scale, scale, scale);
        mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                pose, buffers, mc.level, 0);
        pose.popPose();
    }

    // =====================================================================
    //  Narration, fondus et titre
    // =====================================================================
    public static void overlay(GuiGraphics g, Font font, int w, int h, float t, int bars) {
        int[][] lines = {{20, 155}, {185, 325}, {355, 505}, {535, 680}};
        for (int i = 0; i < lines.length; i++) {
            if (t > lines[i][0] && t < lines[i][1]) {
                float a = Mth.clamp(Math.min((t - lines[i][0]) / 12F, (lines[i][1] - t) / 12F), 0F, 1F);
                g.pose().pushPose();
                g.pose().translate(w / 2F, h - bars - 26, 0);
                g.pose().scale(1.4F, 1.4F, 1F);
                g.drawCenteredString(font, Component.translatable("cinematic.aetheria.adventure.line" + (i + 1)), 0, 0,
                        alpha(a) | 0xE9DCFF);
                g.pose().popPose();
            }
        }
        if (t > 760 && t < 838) {
            float a = Mth.clamp(Math.min((t - 760) / 12F, (838 - t) / 12F), 0F, 1F);
            g.pose().pushPose();
            g.pose().translate(w / 2F, h * 0.34F, 0);
            g.pose().scale(4.5F, 4.5F, 1F);
            g.drawCenteredString(font, Component.translatable("cinematic.aetheria.adventure.title"), 0, 0, alpha(a) | 0xFFD36B);
            g.pose().popPose();
            g.pose().pushPose();
            g.pose().translate(w / 2F, h * 0.34F + 46, 0);
            g.pose().scale(1.8F, 1.8F, 1F);
            g.drawCenteredString(font, Component.translatable("cinematic.aetheria.adventure.subtitle"), 0, 0, alpha(a) | 0xD9B8FF);
            g.pose().popPose();
            if (t > 780) g.drawCenteredString(font, Component.translatable("cinematic.aetheria.adventure.objective"),
                    w / 2, h - bars - 24, alpha(a) | 0xFFD36B);
        }
        // Fondus au noir entre les scenes, flash blanc avant le retour au monde
        float black = Mth.clamp(1F - t / 15F, 0F, 1F);
        for (int cut : new int[]{SCENE[1], SCENE[2], SCENE[3]}) black = Math.max(black, Mth.clamp(1F - Math.abs(t - cut) / 9F, 0F, 1F));
        if (black > 0.01F) g.fill(0, 0, w, h, ((int) (black * 255) << 24));
        float white = t < 700 ? Mth.clamp((t - 682) / 18F, 0F, 1F) : Mth.clamp(1F - (t - 700) / 25F, 0F, 1F);
        if (white > 0.01F) g.fill(0, 0, w, h, ((int) (white * 255) << 24) | 0xFFFFFF);
    }

    private static int alpha(float a) {
        return Math.max(8, (int) (a * 255)) << 24;
    }

    private StoryCinematic() {}
}
