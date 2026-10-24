package com.aetheria.astralruin.client.cinematic;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.config.AetheriaConfig;
import com.aetheria.astralruin.entity.CinematicCameraEntity;
import com.aetheria.astralruin.network.CinematicS2C;
import com.aetheria.astralruin.registry.ModEntities;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Cinematiques de Valerius : la camera suit une trajectoire (Catmull-Rom) definie autour du boss,
 * avec bandes noires, titres, HUD masque et deplacements bloques. Touche Sauter = passer.
 */
@Mod.EventBusSubscriber(modid = Aetheria.MODID, value = Dist.CLIENT)
public final class CinematicManager {
    private record Key(int time, Vec3 pos, Vec3 look) {}

    @Nullable private static CinematicS2C.Type type;
    private static final List<Key> KEYS = new ArrayList<>();
    private static int tick;
    private static int length;
    private static int bossId;
    @Nullable private static CinematicCameraEntity camera;
    @Nullable private static CameraType previousCameraType;
    @Nullable private static CinematicS2C.Type pendingType;
    private static int pendingId;
    private static int pendingRetries;

    public static boolean active() {
        return type != null;
    }

    public static void start(CinematicS2C.Type t, int entityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !AetheriaConfig.CINEMATICS.get()) return;
        Entity boss = mc.level.getEntity(entityId);
        if (boss == null) {
            // l'entite n'est peut-etre pas encore arrivee : on reessaie pendant 10 ticks
            if (pendingType == null) {
                pendingType = t;
                pendingId = entityId;
                pendingRetries = 10;
            }
            return;
        }
        pendingType = null;
        if (active()) stop();
        Vec3 b = boss.position();
        Vec3 eye = mc.player.getEyePosition();
        Vec3 lookEnd = eye.add(mc.player.getLookAngle().scale(10));
        Vec3 d = new Vec3(eye.x - b.x, 0, eye.z - b.z);
        d = d.lengthSqr() < 1e-3 ? Vec3.directionFromRotation(0, boss.getYRot()).multiply(1, 0, 1).normalize() : d.normalize();
        Vec3 s = new Vec3(-d.z, 0, d.x);
        KEYS.clear();
        Vec3 up2 = b.add(0, 2.2, 0);
        switch (t) {
            case INTRO -> {
                key(0, eye, up2);
                key(25, b.add(d.scale(11)).add(0, 4, 0), b.add(0, 1.5, 0));
                key(60, b.add(d.scale(5)).add(s.scale(4)).add(0, 0.8, 0), b.add(0, 2.6, 0));
                key(95, b.add(d.scale(4.5)).subtract(s.scale(2.5)).add(0, 3.8, 0), b.add(0, 2.9, 0));
                key(122, b.add(d.scale(13)).add(0, 6, 0), up2);
                key(140, eye, lookEnd);
            }
            case PHASE_TWO -> {
                key(0, eye, up2);
                for (int i = 1; i <= 6; i++) {
                    double a = Math.toRadians(i * 55);
                    Vec3 dir = d.scale(Math.cos(a)).add(s.scale(Math.sin(a)));
                    key(i * 16, b.add(dir.scale(9)).add(0, 2.5 + i * 0.7, 0), b.add(0, 3 + i * 0.2, 0));
                }
                key(120, eye, lookEnd);
            }
            case ADVENTURE -> StoryCinematic.start(mc);
            case DEATH -> {
                key(0, eye, up2);
                key(20, b.add(d.scale(7)).add(0, 2.5, 0), b.add(0, 2.5, 0));
                key(45, b.add(d.scale(5)).add(s.scale(5)).add(0, 3, 0), b.add(0, 3, 0));
                key(70, b.add(s.scale(7)).subtract(d.scale(2)).add(0, 3.5, 0), b.add(0, 3.5, 0));
                key(96, b.add(d.scale(3)).add(0, 15, 0), b.add(0, 2, 0));
                key(110, eye, lookEnd);
            }
        }
        type = t;
        tick = 0;
        length = t == CinematicS2C.Type.ADVENTURE ? StoryCinematic.LENGTH : KEYS.get(KEYS.size() - 1).time();
        bossId = entityId;
        camera = new CinematicCameraEntity(ModEntities.CINEMATIC_CAMERA.get(), mc.level);
        apply(0, true);
        previousCameraType = mc.options.getCameraType();
        mc.options.setCameraType(CameraType.FIRST_PERSON);
        mc.setCameraEntity(camera);
        if (t == CinematicS2C.Type.ADVENTURE) {
            mc.getMusicManager().stopPlaying();
            music = net.minecraft.client.resources.sounds.SimpleSoundInstance.forMusic(
                    com.aetheria.astralruin.registry.ModSounds.ADVENTURE_THEME.get());
            mc.getSoundManager().play(music);
        }
    }

    @Nullable private static net.minecraft.client.resources.sounds.SoundInstance music;

    private static void key(int time, Vec3 pos, Vec3 look) {
        KEYS.add(new Key(time, pos, look));
    }

    public static void tick() {
        if (!active() && pendingType != null) {
            if (--pendingRetries < 0) pendingType = null;
            else start(pendingType, pendingId);
        }
        if (!active()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !mc.player.isAlive()) {
            stop();
            return;
        }
        if (mc.isPaused()) return;
        tick++;
        if (tick > length || (tick > 20 && mc.options.keyJump.isDown())) {
            stop();
            return;
        }
        apply(tick, false);
        if (type == CinematicS2C.Type.ADVENTURE) StoryCinematic.tick(tick);
    }

    /** Rendu du theatre de la cinematique d'histoire (appele par RenderLevelStageEvent). */
    public static void renderStage(com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.MultiBufferSource.BufferSource buffers,
                                   net.minecraft.client.Camera cam, float pt) {
        if (type == CinematicS2C.Type.ADVENTURE) StoryCinematic.render(pose, buffers, cam, pt, tick);
    }

    private static void apply(int t, boolean snap) {
        if (camera == null) return;
        if (type == CinematicS2C.Type.ADVENTURE) {
            StoryCinematic.Shot shot = StoryCinematic.shot(t);
            Vec3 dir = shot.look().subtract(shot.pos());
            double hz = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
            camera.moveTo(shot.pos().x, shot.pos().y, shot.pos().z,
                    (float) (Mth.atan2(-dir.x, dir.z) * (180.0 / Math.PI)), (float) (-Mth.atan2(dir.y, hz) * (180.0 / Math.PI)),
                    snap || shot.cut());
            return;
        }
        if (KEYS.isEmpty()) return;
        int i = 0;
        while (i < KEYS.size() - 2 && KEYS.get(i + 1).time() <= t) i++;
        Key k1 = KEYS.get(i), k2 = KEYS.get(Math.min(i + 1, KEYS.size() - 1));
        Key k0 = KEYS.get(Math.max(i - 1, 0)), k3 = KEYS.get(Math.min(i + 2, KEYS.size() - 1));
        float span = Math.max(1, k2.time() - k1.time());
        float u = Mth.clamp((t - k1.time()) / span, 0F, 1F);
        u = u * u * (3F - 2F * u); // lissage
        Vec3 pos = catmull(k0.pos(), k1.pos(), k2.pos(), k3.pos(), u);
        Vec3 look = catmull(k0.look(), k1.look(), k2.look(), k3.look(), u);
        Vec3 dir = look.subtract(pos);
        double h = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        float yaw = (float) (Mth.atan2(-dir.x, dir.z) * (180.0 / Math.PI));
        float pitch = (float) (-Mth.atan2(dir.y, h) * (180.0 / Math.PI));
        camera.moveTo(pos.x, pos.y, pos.z, yaw, pitch, snap);
    }

    private static Vec3 catmull(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, double t) {
        double t2 = t * t, t3 = t2 * t;
        return p0.scale(-0.5 * t3 + t2 - 0.5 * t)
                .add(p1.scale(1.5 * t3 - 2.5 * t2 + 1.0))
                .add(p2.scale(-1.5 * t3 + 2.0 * t2 + 0.5 * t))
                .add(p3.scale(0.5 * t3 - 0.5 * t2));
    }

    public static void stop() {
        Minecraft mc = Minecraft.getInstance();
        if (music != null) {
            mc.getSoundManager().stop(music);
            music = null;
        }
        if (mc.player != null) mc.setCameraEntity(mc.player);
        if (previousCameraType != null) mc.options.setCameraType(previousCameraType);
        previousCameraType = null;
        StoryCinematic.stop();
        camera = null;
        type = null;
        KEYS.clear();
    }

    // =====================================================================
    //  Bandes noires et titres
    // =====================================================================
    public static final IGuiOverlay OVERLAY = (ForgeGui gui, GuiGraphics g, float pt, int w, int h) -> {
        if (!active()) return;
        float t = tick + pt;
        float bars = Mth.clamp(Math.min(t / 15F, (length - t) / 15F), 0F, 1F);
        int bh = (int) (h * 0.11F * bars);
        g.fill(0, 0, w, bh, 0xFF000000);
        g.fill(0, h - bh, w, h, 0xFF000000);
        Minecraft mc = Minecraft.getInstance();
        Component title = null, subtitle = null;
        float from = 0, to = 0;
        switch (type) {
            case INTRO -> { title = Component.translatable("cinematic.aetheria.intro.title"); subtitle = Component.translatable("cinematic.aetheria.intro.subtitle"); from = 58; to = 128; }
            case PHASE_TWO -> { title = Component.translatable("cinematic.aetheria.phase2.title"); subtitle = Component.translatable("cinematic.aetheria.phase2.subtitle"); from = 70; to = 116; }
            case DEATH -> { title = Component.translatable("cinematic.aetheria.death.title"); subtitle = Component.translatable("cinematic.aetheria.death.subtitle"); from = 70; to = 108; }
        }
        if (type == CinematicS2C.Type.ADVENTURE) StoryCinematic.overlay(g, mc.font, w, h, t, bh);
        if (title != null && t > from && t < to) {
            float a = Mth.clamp(Math.min((t - from) / 10F, (to - t) / 10F), 0F, 1F);
            int alpha = Math.max(8, (int) (a * 255)) << 24;
            g.pose().pushPose();
            g.pose().translate(w / 2F, h * 0.36F, 0);
            g.pose().scale(4F, 4F, 1F);
            g.drawCenteredString(mc.font, title, 0, 0, alpha | 0xFFD36B);
            g.pose().popPose();
            g.pose().pushPose();
            g.pose().translate(w / 2F, h * 0.36F + 42, 0);
            g.pose().scale(1.6F, 1.6F, 1F);
            g.drawCenteredString(mc.font, subtitle, 0, 0, alpha | 0xD9B8FF);
            g.pose().popPose();
        }
        if (tick > 20 && bh > 6) {
            g.drawString(mc.font, Component.translatable("cinematic.aetheria.skip"), w - 120, h - bh + 2, 0x80FFFFFF, false);
        }
    };

    // =====================================================================
    //  Blocage du joueur et du HUD pendant la cinematique
    // =====================================================================
    @SubscribeEvent
    public static void onMovement(MovementInputUpdateEvent event) {
        if (!active()) return;
        var in = event.getInput();
        in.forwardImpulse = 0;
        in.leftImpulse = 0;
        in.up = in.down = in.left = in.right = false;
        in.jumping = false;
        in.shiftKeyDown = false;
    }

    @SubscribeEvent
    public static void onInteract(InputEvent.InteractionKeyMappingTriggered event) {
        if (active()) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (active()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onOverlay(RenderGuiOverlayEvent.Pre event) {
        if (active() && !event.getOverlay().id().equals(Aetheria.id("cinematic"))) event.setCanceled(true);
    }

    private CinematicManager() {}
}
