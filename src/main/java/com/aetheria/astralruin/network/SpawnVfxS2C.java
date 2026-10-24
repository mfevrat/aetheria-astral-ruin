package com.aetheria.astralruin.network;

import com.aetheria.astralruin.client.ClientPacketHandler;
import com.aetheria.astralruin.vfx.VfxType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

public record SpawnVfxS2C(VfxType type, Vec3 a, Vec3 b, int entityA, int entityB, int color, float size,
                          int duration, float param, int seed) {

    public SpawnVfxS2C(FriendlyByteBuf buf) {
        this(buf.readEnum(VfxType.class), new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readVarInt(), buf.readVarInt(),
                buf.readInt(), buf.readFloat(), buf.readVarInt(), buf.readFloat(), buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(type);
        buf.writeDouble(a.x);
        buf.writeDouble(a.y);
        buf.writeDouble(a.z);
        buf.writeDouble(b.x);
        buf.writeDouble(b.y);
        buf.writeDouble(b.z);
        buf.writeVarInt(entityA);
        buf.writeVarInt(entityB);
        buf.writeInt(color);
        buf.writeFloat(size);
        buf.writeVarInt(duration);
        buf.writeFloat(param);
        buf.writeInt(seed);
    }

    public void handle(CustomPayloadEvent.Context ctx) {
        if (FMLEnvironment.dist == Dist.CLIENT) ClientPacketHandler.spawnVfx(this);
        ctx.setPacketHandled(true);
    }
}
