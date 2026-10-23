package com.aetheria.astralruin.network;

import com.aetheria.astralruin.spell.CastingManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

/** Demande de lancement du sort selectionne. Aucune donnee : le serveur decide de tout. */
public class CastSpellC2S {
    public CastSpellC2S() {}

    public CastSpellC2S(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    public void handle(CustomPayloadEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player != null) CastingManager.tryCastSelected(player);
        ctx.setPacketHandled(true);
    }
}
