package com.aetheria.astralruin.mana;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.network.ManaSyncS2C;
import com.aetheria.astralruin.network.ModNetwork;
import com.aetheria.astralruin.spell.CastingManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Aetheria.MODID)
public final class ManaEvents {

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            ManaCapability.Provider provider = new ManaCapability.Provider();
            event.addCapability(ManaCapability.KEY, provider);
            event.addListener(provider::invalidate);
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        original.reviveCaps();
        ManaCapability.get(original).ifPresent(old ->
                ManaCapability.get(event.getEntity()).ifPresent(now ->
                        now.copyFrom(old, event.isWasDeath(), event.getEntity())));
        original.invalidateCaps();
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) sync(sp);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        CastingManager.clear(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) sync(sp);
    }

    @SubscribeEvent
    public static void changeDim(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) sync(sp);
    }

    @SubscribeEvent
    public static void serverStopped(ServerStoppedEvent event) {
        CastingManager.clearAll();
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer sp)) return;
        CastingManager.tick(sp);
        ManaCapability.get(sp).ifPresent(data -> {
            float max = data.maxMana(sp);
            if (data.getRegenBlockedTicks() > 0) {
                data.setRegenBlockedTicks(data.getRegenBlockedTicks() - 1);
            } else if (data.getMana() < max) {
                boolean outOfCombat = sp.tickCount - sp.getLastHurtByMobTimestamp() > 100
                        && sp.tickCount - sp.getLastHurtMobTimestamp() > 100;
                float perTick = data.regenPerSecond(sp) / 20F * (outOfCombat ? 2F : 1F);
                data.setMana(Math.min(max, data.getMana() + perTick));
            }
            if (data.getMana() > max) data.setMana(max);
            if (Math.abs(data.getSyncedMaxMana() - max) > 0.01F) data.markDirty();
            if (data.isDirty() && sp.tickCount % 5 == 0) sync(sp);
        });
    }

    public static void sync(ServerPlayer player) {
        ManaCapability.get(player).ifPresent(data -> {
            var tag = data.toSyncTag(player);
            data.load(tag); // met a jour syncedMax cote serveur aussi
            data.clearDirty();
            ModNetwork.sendToPlayer(player, new ManaSyncS2C(tag));
        });
    }

    private ManaEvents() {}
}
