package com.aetheria.astralruin.client.gui;

import com.aetheria.astralruin.client.ClientManaData;
import com.aetheria.astralruin.mana.ManaData;
import com.aetheria.astralruin.network.ModNetwork;
import com.aetheria.astralruin.network.UnlockNodeC2S;
import com.aetheria.astralruin.skill.SkillTree;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/** Grimoire Astral : carte d'etoiles de l'arbre runique. Clic sur un noeud disponible pour le debloquer. */
public class GrimoireScreen extends Screen {
    private static final int NODE = 9;
    private final float[][] stars = new float[90][3];
    private double panX, panY;
    private boolean dragging;

    public GrimoireScreen() {
        super(Component.translatable("screen.aetheria.grimoire"));
        RandomSource r = RandomSource.create(1337L);
        for (float[] s : stars) {
            s[0] = r.nextFloat();
            s[1] = r.nextFloat();
            s[2] = r.nextFloat();
        }
    }

    private int cx() { return width / 2 + (int) panX; }
    private int cy() { return height / 2 + 10 + (int) panY; }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        ManaData data = ClientManaData.DATA;
        long time = minecraft != null && minecraft.level != null ? minecraft.level.getGameTime() : 0;

        // Ciel etoile
        for (float[] s : stars) {
            int x = (int) (s[0] * width), y = (int) (s[1] * height);
            int a = (int) (90 + 120 * (0.5 + 0.5 * Math.sin(time * 0.05 + s[2] * 20)));
            g.fill(x, y, x + 1, y + 1, (a << 24) | 0xE9D9FF);
        }

        // Constellations (liens de prerequis)
        for (SkillTree.Node n : SkillTree.nodes().values()) {
            for (String req : n.requires()) {
                SkillTree.Node p = SkillTree.get(req);
                if (p == null) continue;
                boolean lit = data.unlockedNodes().contains(n.id());
                dottedLine(g, cx() + p.x(), cy() + p.y(), cx() + n.x(), cy() + n.y(), lit ? 0xFFFFD36B : 0x806A5A8C);
            }
        }

        // Noeuds
        SkillTree.Node hovered = null;
        for (SkillTree.Node n : SkillTree.nodes().values()) {
            int x = cx() + n.x(), y = cy() + n.y();
            boolean unlocked = data.unlockedNodes().contains(n.id());
            boolean available = SkillTree.canUnlock(data.unlockedNodes(), data.getPoints(), n);
            int border = unlocked ? 0xFFFFD36B : available ? 0xFFB98CFF : 0xFF4A4060;
            int fill = unlocked ? 0xFF6A4A12 : available ? 0xFF2C1A4A : 0xFF141020;
            if (available) {
                int glow = (int) (60 + 60 * Math.sin(time * 0.2));
                g.fill(x - NODE - 2, y - NODE - 2, x + NODE + 2, y + NODE + 2, (glow << 24) | 0xB98CFF);
            }
            g.fill(x - NODE, y - NODE, x + NODE, y + NODE, border);
            g.fill(x - NODE + 1, y - NODE + 1, x + NODE - 1, y + NODE - 1, fill);
            String glyph = n.spell() != null ? "\u2726" : "\u25C6";
            g.drawCenteredString(font, glyph, x, y - 4, unlocked ? 0xFFFFE9A8 : 0xFFB0A0D0);
            if (Math.abs(mouseX - x) <= NODE && Math.abs(mouseY - y) <= NODE) hovered = n;
        }

        // En-tete
        g.drawCenteredString(font, title.copy().withStyle(ChatFormatting.GOLD), width / 2, 10, 0xFFFFFF);
        g.drawCenteredString(font, Component.translatable("screen.aetheria.points", data.getPoints()), width / 2, 22, 0xFFB98CFF);
        g.drawString(font, Component.translatable("screen.aetheria.help"), 6, height - 12, 0xFF8070A0);

        if (hovered != null) {
            List<Component> tip = new ArrayList<>();
            tip.add(Component.translatable(hovered.nameKey()).withStyle(ChatFormatting.GOLD));
            tip.add(Component.translatable(hovered.descKey()).withStyle(ChatFormatting.GRAY));
            if (data.unlockedNodes().contains(hovered.id())) {
                tip.add(Component.translatable("screen.aetheria.unlocked").withStyle(ChatFormatting.GREEN));
            } else {
                tip.add(Component.translatable("screen.aetheria.cost", hovered.cost()).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            g.renderComponentTooltip(font, tip, mouseX, mouseY);
        }
    }

    private static void dottedLine(GuiGraphics g, int x0, int y0, int x1, int y1, int color) {
        float len = Mth.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
        int steps = Math.max(1, (int) (len / 3));
        for (int i = 0; i <= steps; i++) {
            int x = Math.round(Mth.lerp((float) i / steps, x0, x1));
            int y = Math.round(Mth.lerp((float) i / steps, y0, y1));
            g.fill(x, y, x + 2, y + 2, color);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            ManaData data = ClientManaData.DATA;
            for (SkillTree.Node n : SkillTree.nodes().values()) {
                int x = cx() + n.x(), y = cy() + n.y();
                if (Math.abs(mouseX - x) <= NODE && Math.abs(mouseY - y) <= NODE) {
                    if (SkillTree.canUnlock(data.unlockedNodes(), data.getPoints(), n)) {
                        ModNetwork.sendToServer(new UnlockNodeC2S(n.id()));
                    }
                    return true;
                }
            }
            dragging = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging) {
            panX = Mth.clamp(panX + dx, -200, 200);
            panY = Mth.clamp(panY + dy, -150, 150);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
