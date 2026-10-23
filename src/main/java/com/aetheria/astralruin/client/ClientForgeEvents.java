package com.aetheria.astralruin.client;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.client.camera.CameraShakeHandler;
import com.aetheria.astralruin.client.particle.RuneCircleManager;
import com.aetheria.astralruin.client.sound.BossMusicManager;
import com.aetheria.astralruin.network.CastSpellC2S;
import com.aetheria.astralruin.network.CycleSpellC2S;
import com.aetheria.astralruin.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Aetheria.MODID, value = Dist.CLIENT)
public final class ClientForgeEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        CameraShakeHandler.tick();
        RuneCircleManager.tick();
        BossMusicManager.tick();
        if (mc.player == null || mc.level == null) return;

        if (mc.screen == null) {
            while (KeyBindings.CAST.consumeClick()) ModNetwork.sendToServer(new CastSpellC2S());
            while (KeyBindings.CYCLE.consumeClick()) ModNetwork.sendToServer(new CycleSpellC2S(mc.player.isShiftKeyDown() ? -1 : 1));
            while (KeyBindings.GRIMOIRE.consumeClick()) ClientHooks.openGrimoire();
        }
    }

    /** Decalques des cercles runiques, dessines apres les particules en melange additif. */
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        RuneCircleManager.render(event.getPoseStack(), buffers, event.getCamera().getPosition(), event.getPartialTick());
        buffers.endBatch();
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        CameraShakeHandler.apply(event);
    }

    private ClientForgeEvents() {}
}
