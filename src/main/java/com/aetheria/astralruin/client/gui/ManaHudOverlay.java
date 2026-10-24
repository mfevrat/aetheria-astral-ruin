package com.aetheria.astralruin.client.gui;

import com.aetheria.astralruin.client.ClientManaData;
import com.aetheria.astralruin.mana.ManaData;
import com.aetheria.astralruin.spell.SpellRegistry;
import com.aetheria.astralruin.spell.AbstractSpell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/** Barre de mana a gauche de la hotbar + sort selectionne et sa recharge. */
public final class ManaHudOverlay implements IGuiOverlay {
    public static final ManaHudOverlay INSTANCE = new ManaHudOverlay();
    private static final int WIDTH = 82;

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.player.isSpectator() || mc.level == null) return;
        ManaData data = ClientManaData.DATA;
        float max = Math.max(1F, data.getSyncedMaxMana());
        float mana = data.getMana();

        int x = screenWidth / 2 - 91 - WIDTH - 8;
        int y = screenHeight - 14;
        if (x < 4) {
            x = 4;
            y = screenHeight - 60;
        }

        // Cadre et fond
        g.fill(x - 1, y - 1, x + WIDTH + 1, y + 7, 0xC0100A1E);
        int filled = (int) (WIDTH * Math.max(0F, Math.min(1F, mana / max)));
        g.fillGradient(x, y, x + filled, y + 6, 0xFFB98CFF, 0xFF6A3BD6);
        if (mana < 0) {
            int debt = (int) (WIDTH * Math.min(1F, -mana / max));
            g.fill(x, y, x + debt, y + 6, 0xFFB0203A);
        }
        for (int i = 1; i < 4; i++) g.fill(x + WIDTH * i / 4, y, x + WIDTH * i / 4 + 1, y + 6, 0x60FFFFFF);

        String text = (int) mana + " / " + (int) max;
        g.drawString(mc.font, text, x + WIDTH / 2 - mc.font.width(text) / 2, y - 10, 0xFFE9D9FF, true);

        // Sort selectionne
        String spellId = data.selectedSpellId();
        AbstractSpell spell = SpellRegistry.get(spellId);
        if (spell != null) {
            Component name = Component.translatable("spell.aetheria." + spellId);
            int cd = data.cooldownRemaining(spellId, mc.level.getGameTime());
            int color = cd > 0 ? 0xFF8A8A8A : 0xFFFFD36B;
            g.blit(com.aetheria.astralruin.Aetheria.id("textures/gui/spells/" + spellId + ".png"), x - 20, y - 14, 18, 18,
                    0F, 0F, 32, 32, 32, 32);
            if (cd > 0) g.fill(x - 20, y - 14, x - 2, y + 4, 0x90000000);
            g.drawString(mc.font, name, x, y - 21, color, true);
            if (cd > 0) {
                String s = String.format("%.1fs", cd / 20F);
                g.drawString(mc.font, s, x + WIDTH - mc.font.width(s), y - 21, 0xFFAAAAAA, true);
            }
        }
    }

    private ManaHudOverlay() {}
}
