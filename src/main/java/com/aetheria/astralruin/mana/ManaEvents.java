package com.aetheria.astralruin.mana;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.config.AetheriaConfig;
import com.aetheria.astralruin.network.ManaSyncS2C;
import com.aetheria.astralruin.network.ModNetwork;
import com.aetheria.astralruin.spell.CastingManager;
import net.minecraft.nbt.CompoundTag;
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
        Player now = event.getEntity();
        original.reviveCaps();
        var oldData = ManaCapability.get(original);
        var newData = ManaCapability.get(now);
        if (newData.isPresent()) {
            if (oldData.isPresent()) {
                newData.get().copyFrom(oldData.get(), event.isWasDeath(), now);
            } else {
                // Secours : la sauvegarde persistante a deja ete recopiee par Forge sur le nouveau joueur
                CompoundTag backup = ManaCapability.readBackup(now);
                if (backup != null) {
                    ManaData restored = new ManaData();
                    restored.load(backup);
                    newData.get().copyFrom(restored, event.isWasDeath(), now);
                    Aetheria.LOGGER.warn("Progression magique de {} restauree depuis la sauvegarde de secours", now.getName().getString());
                }
            }
            ManaCapability.backup(now, newData.get());
        }
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
            // Cinematique d'aventure : une seule fois par joueur, 3 s apres sa premiere arrivee
            if (!data.hasSeenIntro() && sp.tickCount >= 60 && AetheriaConfig.ADVENTURE_INTRO.get()) {
                data.markIntroSeen();
                data.markDirty();
                playAdventureIntro(sp);
            }
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

    public static final int ADVENTURE_INTRO_TICKS = 840;

    /** Lance la cinematique d'introduction (joueur protege pendant sa duree). */
    public static void playAdventureIntro(ServerPlayer player) {
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, ADVENTURE_INTRO_TICKS + 40, 4, false, false));
        ModNetwork.sendToPlayer(player, new com.aetheria.astralruin.network.CinematicS2C(
                com.aetheria.astralruin.network.CinematicS2C.Type.ADVENTURE, player.getId()));
    }

    public static void sync(ServerPlayer player) {
        ManaCapability.get(player).ifPresent(data -> {
            ManaCapability.backup(player, data);
            var tag = data.toSyncTag(player);
            var quests = new net.minecraft.nbt.ListTag();
            for (String id : com.aetheria.astralruin.quest.AdventureQuests.completed(player)) {
                quests.add(net.minecraft.nbt.StringTag.valueOf(id));
            }
            tag.put("questsDone", quests);
            data.load(tag); // met a jour syncedMax cote serveur aussi
            data.clearDirty();
            ModNetwork.sendToPlayer(player, new ManaSyncS2C(tag));
        });
    }

    private ManaEvents() {}
}
