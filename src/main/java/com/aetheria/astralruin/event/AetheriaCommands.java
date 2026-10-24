package com.aetheria.astralruin.event;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.mana.ManaEvents;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** /aetheria intro : rejoue la cinematique d'introduction de l'aventure. */
@Mod.EventBusSubscriber(modid = Aetheria.MODID)
public final class AetheriaCommands {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("aetheria")
                .then(Commands.literal("intro").executes(ctx -> {
                    ManaEvents.playAdventureIntro(ctx.getSource().getPlayerOrException());
                    return 1;
                })));
    }

    private AetheriaCommands() {}
}
