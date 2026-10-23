package com.aetheria.astralruin.event;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.entity.boss.BossDefinitionLoader;
import com.aetheria.astralruin.mana.ManaEvents;
import com.aetheria.astralruin.network.ModNetwork;
import com.aetheria.astralruin.network.SkillTreeSyncS2C;
import com.aetheria.astralruin.skill.SkillNodeLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Datapacks : chargement des noeuds runiques et des boss, puis synchro des noeuds vers les clients. */
@Mod.EventBusSubscriber(modid = Aetheria.MODID)
public final class DataEvents {

    @SubscribeEvent
    public static void onReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SkillNodeLoader());
        event.addListener(new BossDefinitionLoader());
    }

    /** Appele a la connexion d'un joueur (getPlayer() != null) et apres /reload (getPlayer() == null). */
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        SkillTreeSyncS2C msg = SkillTreeSyncS2C.current();
        if (event.getPlayer() != null) {
            send(event.getPlayer(), msg);
        } else {
            event.getPlayerList().getPlayers().forEach(p -> send(p, msg));
        }
    }

    private static void send(ServerPlayer player, SkillTreeSyncS2C msg) {
        ModNetwork.sendToPlayer(player, msg);
        ManaEvents.sync(player); // les bonus de l'arbre peuvent avoir change
    }

    private DataEvents() {}
}
