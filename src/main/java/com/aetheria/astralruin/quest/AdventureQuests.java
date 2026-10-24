package com.aetheria.astralruin.quest;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.registry.ModItems;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Etapes de l'aventure, dans l'ordre. Chaque etape est validee par un advancement du mod,
 * ce qui la rend independante de tout mod de quetes externe.
 */
public final class AdventureQuests {
    public record Quest(String id, ResourceLocation advancement, Supplier<Item> icon) {
        public String titleKey() { return "quest.aetheria." + id + ".title"; }
        public String descKey() { return "quest.aetheria." + id + ".desc"; }
        public String hintKey() { return "quest.aetheria." + id + ".hint"; }
        public String rewardKey() { return "quest.aetheria." + id + ".reward"; }
    }

    public static final List<Quest> QUESTS = List.of(
            new Quest("awakening", Aetheria.id("awakening"), ModItems.STELE_FRAGMENT::get),
            new Quest("staff", Aetheria.id("staff"), ModItems.ARCHMAGE_STAFF::get),
            new Quest("invoker", Aetheria.id("invoker"), ModItems.ASTRAL_GRIMOIRE::get),
            new Quest("sigil", Aetheria.id("sigil"), ModItems.ASTRAL_SIGIL::get),
            new Quest("guardian_trial", Aetheria.id("guardian_trial"), ModItems.RUNE_HEART::get),
            new Quest("awakened", Aetheria.id("awakened"), ModItems.AWAKENED_STAFF::get));

    /** Identifiants des etapes terminees par ce joueur (cote serveur). */
    public static List<String> completed(ServerPlayer player) {
        List<String> done = new ArrayList<>();
        for (Quest q : QUESTS) {
            AdvancementHolder adv = player.server.getAdvancements().get(q.advancement());
            if (adv != null && player.getAdvancements().getOrStartProgress(adv).isDone()) done.add(q.id());
        }
        return done;
    }

    private AdventureQuests() {}
}
