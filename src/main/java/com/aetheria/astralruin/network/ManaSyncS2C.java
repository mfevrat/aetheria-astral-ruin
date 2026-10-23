package com.aetheria.astralruin.network;

import com.aetheria.astralruin.client.ClientPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class ManaSyncS2C {
    private final CompoundTag tag;

    public ManaSyncS2C(CompoundTag tag) {
        this.tag = tag;
    }

    public ManaSyncS2C(FriendlyByteBuf buf) {
        this(buf.readNbt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(tag);
    }

    public CompoundTag tag() { return tag; }

    public void handle(CustomPayloadEvent.Context ctx) {
        if (FMLEnvironment.dist == Dist.CLIENT && tag != null) ClientPacketHandler.syncMana(this);
        ctx.setPacketHandled(true);
    }
}
