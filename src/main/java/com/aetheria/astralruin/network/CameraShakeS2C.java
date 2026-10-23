package com.aetheria.astralruin.network;

import com.aetheria.astralruin.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

public record CameraShakeS2C(float power, int ticks) {

    public CameraShakeS2C(FriendlyByteBuf buf) {
        this(buf.readFloat(), buf.readVarInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(power);
        buf.writeVarInt(ticks);
    }

    public void handle(CustomPayloadEvent.Context ctx) {
        if (FMLEnvironment.dist == Dist.CLIENT) ClientPacketHandler.shake(this);
        ctx.setPacketHandled(true);
    }
}
