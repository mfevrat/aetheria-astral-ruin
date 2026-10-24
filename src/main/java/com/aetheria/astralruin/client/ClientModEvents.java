package com.aetheria.astralruin.client;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.client.gui.ManaHudOverlay;
import com.aetheria.astralruin.client.particle.AetheriaParticle;
import com.aetheria.astralruin.particle.AstralParticleOptions.Kind;
import com.aetheria.astralruin.client.render.AstralBeamRenderer;
import com.aetheria.astralruin.client.render.ValeriusOrbRenderer;
import com.aetheria.astralruin.client.render.ValeriusRenderer;
import com.aetheria.astralruin.registry.ModEntities;
import com.aetheria.astralruin.registry.ModParticles;
import com.aetheria.astralruin.client.render.geo.AstralAltarRenderer;
import com.aetheria.astralruin.registry.ModBlockEntities;
import com.aetheria.astralruin.registry.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Aetheria.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.VALERIUS.get(), ValeriusRenderer::new);
        event.registerEntityRenderer(ModEntities.VALERIUS_ORB.get(), ValeriusOrbRenderer::new);
        event.registerEntityRenderer(ModEntities.ASTRAL_BEAM.get(), AstralBeamRenderer::new);
        event.registerEntityRenderer(ModEntities.MANA_CRYSTAL.get(), ctx -> new ThrownItemRenderer<>(ctx, 1.0F, true));
        event.registerEntityRenderer(ModEntities.ECLIPSE_SLASH.get(), ctx -> new ThrownItemRenderer<>(ctx, 2.2F, true));
        event.registerEntityRenderer(ModEntities.RUNE_STRIKE.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.COMET.get(), ctx -> new com.aetheria.astralruin.client.render.GlowSpriteRenderer<>(
                ctx, com.aetheria.astralruin.Aetheria.id("textures/vfx/flare.png"), 1.3F, 0xFFC070));
        event.registerEntityRenderer(ModEntities.SOLAR_HALO.get(), com.aetheria.astralruin.client.render.SolarHaloRenderer::new);
        event.registerEntityRenderer(ModEntities.GLYPH_PRISON.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.GRAVITY_WELL.get(), com.aetheria.astralruin.client.render.GravityWellRenderer::new);
        event.registerEntityRenderer(ModEntities.SPECTRAL_BLADE.get(), com.aetheria.astralruin.client.render.SpectralBladeRenderer::new);
        event.registerEntityRenderer(ModEntities.CINEMATIC_CAMERA.get(), NoopRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASTRAL_ALTAR.get(), ctx -> new AstralAltarRenderer());
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.ASTRAL_SPARK.get(), s -> new AetheriaParticle.Provider(s, Kind.SPARK));
        event.registerSpriteSet(ModParticles.STAR_FLARE.get(), s -> new AetheriaParticle.Provider(s, Kind.STAR));
        event.registerSpriteSet(ModParticles.EMBER.get(), s -> new AetheriaParticle.Provider(s, Kind.EMBER));
        event.registerSpriteSet(ModParticles.VOID_SMOKE.get(), s -> new AetheriaParticle.Provider(s, Kind.SMOKE));
        event.registerSpriteSet(ModParticles.SHARD.get(), s -> new AetheriaParticle.Provider(s, Kind.SHARD));
        event.registerSpriteSet(ModParticles.RUNE_GLYPH.get(), s -> new AetheriaParticle.Provider(s, Kind.GLYPH));
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.CAST);
        event.register(KeyBindings.CYCLE);
        event.register(KeyBindings.GRIMOIRE);
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("mana_hud", ManaHudOverlay.INSTANCE);
        event.registerAboveAll("cinematic", com.aetheria.astralruin.client.cinematic.CinematicManager.OVERLAY);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        // Pose "bouclier leve" pour le modele du Rempart
        event.enqueueWork(() -> ItemProperties.register(ModItems.ECLIPSE_RAMPART.get(), new ResourceLocation("blocking"),
                (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1F : 0F));
    }

    private ClientModEvents() {}
}
