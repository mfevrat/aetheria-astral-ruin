package com.aetheria.astralruin.network;

import com.aetheria.astralruin.mana.ManaCapability;
import com.aetheria.astralruin.mana.ManaEvents;
import com.aetheria.astralruin.skill.SkillTree;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class UnlockNodeC2S {
    private final String nodeId;

    public UnlockNodeC2S(String nodeId) {
        this.nodeId = nodeId;
    }

    public UnlockNodeC2S(FriendlyByteBuf buf) {
        this(buf.readUtf(64));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(nodeId, 64);
    }

    public void handle(CustomPayloadEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            ManaCapability.get(player).ifPresent(data -> {
                SkillTree.Node node = SkillTree.get(nodeId);
                if (node != null && data.tryUnlock(nodeId)) {
                    player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE,
                            SoundSource.PLAYERS, 1.0F, 1.2F);
                    player.displayClientMessage(Component.translatable("message.aetheria.unlocked",
                            Component.translatable(node.nameKey())).withStyle(ChatFormatting.GOLD), true);
                    ManaEvents.sync(player);
                }
            });
        }
        ctx.setPacketHandled(true);
    }
}
