package com.aetheria.astralruin.client;

import com.aetheria.astralruin.Aetheria;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.Set;

/** Erreurs d'effets cote client : journalisees et affichees une fois dans le chat au lieu de faire planter le jeu. */
public final class ClientErrors {
    private static final Set<String> SHOWN = new HashSet<>();

    public static void report(String what, Throwable t) {
        Aetheria.LOGGER.error("Erreur client Aetheria ({})", what, t);
        if (!SHOWN.add(what + t.getClass().getName())) return;
        StackTraceElement where = null;
        for (StackTraceElement el : t.getStackTrace()) {
            if (el.getClassName().startsWith("com.aetheria")) { where = el; break; }
        }
        String loc = where == null ? "?" : where.getFileName() + ":" + where.getLineNumber();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal("[Aetheria] Erreur " + what + " : " + t.getClass().getSimpleName()
                    + " (" + t.getMessage() + ") @ " + loc).withStyle(ChatFormatting.RED), false);
        }
    }

    private ClientErrors() {}
}
