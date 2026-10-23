package com.aetheria.astralruin.network;

import com.aetheria.astralruin.mana.ManaCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class CycleSpellC2S {
    private final int direction;

    public CycleSpellC2S(int direction) {
        this.direction = direction >= 0 ? 1 : -1;
    }

    public CycleSpellC2S(FriendlyByteBuf buf) {
        this(buf.readByte());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(direction);
    }

    public void handle(CustomPayloadEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            ManaCapability.get(player).ifPresent(data -> {
                data.cycleSpell(direction);
                player.displayClientMessage(Component.translatable("message.aetheria.selected",
                        Component.translatable("spell.aetheria." + data.selectedSpellId())), true);
            });
        }
        ctx.setPacketHandled(true);
    }
}
