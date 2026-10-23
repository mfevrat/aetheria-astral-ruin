package com.aetheria.astralruin.network;

import com.aetheria.astralruin.client.ClientPacketHandler;
import com.aetheria.astralruin.particle.RuneCirclePreset;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.joml.Vector3f;

public record SpawnRuneCircleS2C(Vec3 center, Vector3f normal, RuneCirclePreset preset, float radius, int duration) {

    public SpawnRuneCircleS2C(FriendlyByteBuf buf) {
        this(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                buf.readVector3f(), buf.readEnum(RuneCirclePreset.class), buf.readFloat(), buf.readVarInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(center.x);
        buf.writeDouble(center.y);
        buf.writeDouble(center.z);
        buf.writeVector3f(normal);
        buf.writeEnum(preset);
        buf.writeFloat(radius);
        buf.writeVarInt(duration);
    }

    public void handle(CustomPayloadEvent.Context ctx) {
        if (FMLEnvironment.dist == Dist.CLIENT) ClientPacketHandler.spawnCircle(this);
        ctx.setPacketHandled(true);
    }
}
