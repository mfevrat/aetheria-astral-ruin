package com.aetheria.astralruin.network;

import com.aetheria.astralruin.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Demarre une cinematique camera centree sur une entite (Valerius). */
public record CinematicS2C(Type type, int entityId) {
    public enum Type { INTRO, PHASE_TWO, DEATH }

    public CinematicS2C(FriendlyByteBuf buf) {
        this(buf.readEnum(Type.class), buf.readVarInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(type);
        buf.writeVarInt(entityId);
    }

    public void handle(CustomPayloadEvent.Context ctx) {
        if (FMLEnvironment.dist == Dist.CLIENT) ClientPacketHandler.cinematic(this);
        ctx.setPacketHandled(true);
    }
}
