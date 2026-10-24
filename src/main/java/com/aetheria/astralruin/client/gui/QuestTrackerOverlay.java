package com.aetheria.astralruin.client.gui;

import com.aetheria.astralruin.client.ClientManaData;
import com.aetheria.astralruin.config.AetheriaConfig;
import com.aetheria.astralruin.quest.AdventureQuests;
import com.aetheria.astralruin.quest.AdventureQuests.Quest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.List;

/** Suivi de quete en haut a gauche : etape en cours et son indice principal. */
public final class QuestTrackerOverlay implements IGuiOverlay {
    public static final QuestTrackerOverlay INSTANCE = new QuestTrackerOverlay();

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float pt, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.screen != null
                || !AetheriaConfig.QUEST_TRACKER.get()) return;
        List<Quest> qs = AdventureQuests.QUESTS;
        int idx = AdventureJournalScreen.currentIndex();
        Quest q = qs.get(idx);
        if (ClientManaData.DATA.isQuestDone(q.id())) return; // aventure terminee
        int x = 6, y = 6, width = 170;
        List<FormattedCharSequence> obj = mc.font.split(Component.translatable(q.descKey()), width - 4);
        int lines = Math.min(3, obj.size());
        g.fill(x - 3, y - 3, x + width + 3, y + 24 + lines * 10, 0x90100A1E);
        g.fill(x - 3, y - 3, x - 2, y + 24 + lines * 10, 0xFFFFD36B);
        g.renderItem(new ItemStack(q.icon().get()), x, y);
        g.drawString(mc.font, Component.translatable("screen.aetheria.tracker", idx + 1, qs.size()), x + 20, y, 0xFF8A7AB0, false);
        g.drawString(mc.font, Component.translatable(q.titleKey()), x + 20, y + 9, 0xFFFFD36B, false);
        for (int i = 0; i < lines; i++) g.drawString(mc.font, obj.get(i), x, y + 22 + i * 10, 0xFFE0D4F4, false);
    }

    private QuestTrackerOverlay() {}
}
