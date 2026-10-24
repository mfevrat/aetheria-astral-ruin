package com.aetheria.astralruin.network;

import com.aetheria.astralruin.Aetheria;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public final class ModNetwork {
    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(Aetheria.id("main"))
            .networkProtocolVersion(3)
            .simpleChannel();

    private static boolean registered;

    public static void register() {
        if (registered) return;
        registered = true;

        // Client -> serveur
        CHANNEL.messageBuilder(CastSpellC2S.class, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CastSpellC2S::encode).decoder(CastSpellC2S::new)
                .consumerMainThread(CastSpellC2S::handle).add();
        CHANNEL.messageBuilder(CycleSpellC2S.class, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CycleSpellC2S::encode).decoder(CycleSpellC2S::new)
                .consumerMainThread(CycleSpellC2S::handle).add();
        CHANNEL.messageBuilder(UnlockNodeC2S.class, NetworkDirection.PLAY_TO_SERVER)
                .encoder(UnlockNodeC2S::encode).decoder(UnlockNodeC2S::new)
                .consumerMainThread(UnlockNodeC2S::handle).add();

        // Serveur -> client
        CHANNEL.messageBuilder(ManaSyncS2C.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ManaSyncS2C::encode).decoder(ManaSyncS2C::new)
                .consumerMainThread(ManaSyncS2C::handle).add();
        CHANNEL.messageBuilder(SpawnRuneCircleS2C.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SpawnRuneCircleS2C::encode).decoder(SpawnRuneCircleS2C::new)
                .consumerMainThread(SpawnRuneCircleS2C::handle).add();
        CHANNEL.messageBuilder(SkillTreeSyncS2C.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SkillTreeSyncS2C::encode).decoder(SkillTreeSyncS2C::new)
                .consumerMainThread(SkillTreeSyncS2C::handle).add();
        CHANNEL.messageBuilder(SpawnVfxS2C.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SpawnVfxS2C::encode).decoder(SpawnVfxS2C::new)
                .consumerMainThread(SpawnVfxS2C::handle).add();
        CHANNEL.messageBuilder(CinematicS2C.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CinematicS2C::encode).decoder(CinematicS2C::new)
                .consumerMainThread(CinematicS2C::handle).add();
        CHANNEL.messageBuilder(CameraShakeS2C.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CameraShakeS2C::encode).decoder(CameraShakeS2C::new)
                .consumerMainThread(CameraShakeS2C::handle).add();
    }

    public static void sendToServer(Object msg) {
        CHANNEL.send(msg, PacketDistributor.SERVER.noArg());
    }

    public static void sendToPlayer(ServerPlayer player, Object msg) {
        CHANNEL.send(msg, PacketDistributor.PLAYER.with(player));
    }

    public static void sendNear(ServerLevel level, Vec3 pos, double radius, Object msg) {
        CHANNEL.send(msg, PacketDistributor.NEAR.with(
                new PacketDistributor.TargetPoint(pos.x, pos.y, pos.z, radius * radius, level.dimension())));
    }

    private ModNetwork() {}
}
