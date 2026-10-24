package com.aetheria.astralruin.client.gui;

import com.aetheria.astralruin.client.ClientManaData;
import com.aetheria.astralruin.quest.AdventureQuests;
import com.aetheria.astralruin.quest.AdventureQuests.Quest;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Journal d'Aventure (touche J) : etapes de l'aventure, indices et recompenses. */
public class AdventureJournalScreen extends Screen {
    private static final int W = 360, H = 220, LIST_W = 130, ROW = 30;
    private int selected = -1;

    public AdventureJournalScreen() {
        super(Component.translatable("screen.aetheria.journal"));
    }

    @Override
    protected void init() {
        if (selected < 0) selected = currentIndex();
    }

    /** Premiere etape non terminee (ou la derniere si tout est fini). */
    public static int currentIndex() {
        List<Quest> qs = AdventureQuests.QUESTS;
        for (int i = 0; i < qs.size(); i++) if (!ClientManaData.DATA.isQuestDone(qs.get(i).id())) return i;
        return qs.size() - 1;
    }

    private int left() { return (width - W) / 2; }
    private int top() { return (height - H) / 2; }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        int x0 = left(), y0 = top();
        // Cadre facon livre
        g.fill(x0 - 4, y0 - 4, x0 + W + 4, y0 + H + 4, 0xFF8A6A2A);
        g.fill(x0 - 2, y0 - 2, x0 + W + 2, y0 + H + 2, 0xFF1A1428);
        g.fill(x0 + LIST_W, y0 + 6, x0 + LIST_W + 1, y0 + H - 6, 0xFF6A5A8C);
        g.drawCenteredString(font, title.copy().withStyle(ChatFormatting.GOLD), x0 + W / 2, y0 - 16, 0xFFFFFF);

        List<Quest> qs = AdventureQuests.QUESTS;
        int current = currentIndex();
        for (int i = 0; i < qs.size(); i++) {
            Quest q = qs.get(i);
            int ry = y0 + 6 + i * ROW;
            boolean done = ClientManaData.DATA.isQuestDone(q.id());
            boolean locked = !done && i > current;
            if (i == selected) g.fill(x0 + 3, ry - 2, x0 + LIST_W - 3, ry + ROW - 4, 0x60B98CFF);
            else if (mx >= x0 && mx < x0 + LIST_W && my >= ry - 2 && my < ry + ROW - 4) g.fill(x0 + 3, ry - 2, x0 + LIST_W - 3, ry + ROW - 4, 0x30FFFFFF);
            g.renderItem(new ItemStack(q.icon().get()), x0 + 6, ry + 2);
            if (locked) g.fill(x0 + 6, ry + 2, x0 + 22, ry + 18, 0xB0000000);
            int color = done ? 0xFF7CE07C : locked ? 0xFF6A6080 : 0xFFFFD36B;
            String mark = done ? "\u2714 " : locked ? "\u2716 " : "\u25B6 ";
            List<FormattedCharSequence> name = font.split(Component.literal(mark).append(Component.translatable(q.titleKey())), LIST_W - 32);
            for (int l = 0; l < Math.min(2, name.size()); l++) g.drawString(font, name.get(l), x0 + 26, ry + 2 + l * 10, color, false);
        }

        // Page de droite
        if (selected < 0 || selected >= qs.size()) return;
        Quest q = qs.get(selected);
        boolean done = ClientManaData.DATA.isQuestDone(q.id());
        boolean locked = !done && selected > current;
        int px = x0 + LIST_W + 10, pw = W - LIST_W - 18, py = y0 + 8;
        g.drawString(font, Component.translatable("screen.aetheria.journal.step", selected + 1, qs.size()), px, py, 0xFF8A7AB0, false);
        py += 12;
        py = wrap(g, Component.translatable(q.titleKey()).withStyle(ChatFormatting.BOLD), px, py, pw, 0xFFFFD36B) + 4;
        if (locked) {
            wrap(g, Component.translatable("screen.aetheria.journal.locked"), px, py, pw, 0xFF8A7AB0);
            return;
        }
        py = wrap(g, Component.translatable(q.descKey()), px, py, pw, 0xFFE9DCFF) + 6;
        g.drawString(font, Component.translatable("screen.aetheria.journal.hint").withStyle(ChatFormatting.LIGHT_PURPLE), px, py, 0xFFFFFF, false);
        py = wrap(g, Component.translatable(q.hintKey()), px, py + 11, pw, 0xFFC8B8E8) + 6;
        g.drawString(font, Component.translatable("screen.aetheria.journal.reward").withStyle(ChatFormatting.GOLD), px, py, 0xFFFFFF, false);
        py = wrap(g, Component.translatable(q.rewardKey()), px, py + 11, pw, 0xFFE0C890) + 8;
        g.drawString(font, Component.translatable(done ? "screen.aetheria.journal.done" : "screen.aetheria.journal.current"),
                px, Math.min(py, y0 + H - 14), done ? 0xFF7CE07C : 0xFFFFD36B, false);
    }

    private int wrap(GuiGraphics g, Component text, int x, int y, int width, int color) {
        for (FormattedCharSequence line : font.split(text, width)) {
            g.drawString(font, line, x, y, color, false);
            y += 10;
        }
        return y;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int x0 = left(), y0 = top();
        if (button == 0 && mx >= x0 && mx < x0 + LIST_W) {
            int i = (int) ((my - y0 - 4) / ROW);
            if (i >= 0 && i < AdventureQuests.QUESTS.size()) {
                selected = i;
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (com.aetheria.astralruin.client.KeyBindings.JOURNAL.matches(key, scan)) {
            onClose();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
