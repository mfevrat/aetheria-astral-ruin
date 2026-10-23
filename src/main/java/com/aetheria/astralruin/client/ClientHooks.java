package com.aetheria.astralruin.client;

import com.aetheria.astralruin.client.gui.GrimoireScreen;
import net.minecraft.client.Minecraft;

public final class ClientHooks {
    public static void openGrimoire() {
        Minecraft.getInstance().setScreen(new GrimoireScreen());
    }

    private ClientHooks() {}
}
