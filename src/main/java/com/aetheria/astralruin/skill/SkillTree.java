package com.aetheria.astralruin.skill;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Arbre runique. Les noeuds sont charges depuis data/<namespace>/skill_nodes/*.json
 * (voir SkillNodeLoader) puis synchronises vers les clients. Les valeurs Java ci-dessous
 * ne servent que de repli si aucun JSON n'est present.
 */
public final class SkillTree {
    public enum Bonus {
        NONE, MAX_MANA, MANA_REGEN, SPELL_POWER, BACKLASH_RESISTANCE, MANA_LEECH;

        public static Bonus parse(String s) {
            try {
                return valueOf(s.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return NONE;
            }
        }
    }

    /** x/y : position sur la carte du grimoire, en pixels depuis le centre. */
    public record Node(String id, int x, int y, int cost, List<String> requires, Bonus bonus, float amount,
                       @Nullable String spell) {
        public String nameKey() { return "skill.aetheria." + id; }
        public String descKey() { return "skill.aetheria." + id + ".desc"; }

        public static Node fromJson(String id, JsonObject o) {
            List<String> req = new ArrayList<>();
            if (o.has("requires")) {
                JsonArray arr = GsonHelper.getAsJsonArray(o, "requires");
                arr.forEach(e -> req.add(e.getAsString()));
            }
            String spell = o.has("spell") && !o.get("spell").isJsonNull() ? GsonHelper.getAsString(o, "spell") : null;
            return new Node(id, GsonHelper.getAsInt(o, "x", 0), GsonHelper.getAsInt(o, "y", 0),
                    GsonHelper.getAsInt(o, "cost", 1), List.copyOf(req),
                    Bonus.parse(GsonHelper.getAsString(o, "bonus", "none")),
                    GsonHelper.getAsFloat(o, "amount", 0F), spell);
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeUtf(id);
            buf.writeVarInt(x);
            buf.writeVarInt(y);
            buf.writeVarInt(cost);
            buf.writeCollection(requires, FriendlyByteBuf::writeUtf);
            buf.writeEnum(bonus);
            buf.writeFloat(amount);
            buf.writeUtf(spell == null ? "" : spell);
        }

        public static Node read(FriendlyByteBuf buf) {
            String id = buf.readUtf();
            int x = buf.readVarInt(), y = buf.readVarInt(), cost = buf.readVarInt();
            List<String> req = buf.readList(FriendlyByteBuf::readUtf);
            Bonus bonus = buf.readEnum(Bonus.class);
            float amount = buf.readFloat();
            String spell = buf.readUtf();
            return new Node(id, x, y, cost, List.copyOf(req), bonus, amount, spell.isEmpty() ? null : spell);
        }
    }

    public static final String ROOT = "astral_bolt";
    private static final Map<String, Node> DEFAULTS = buildDefaults();
    private static volatile Map<String, Node> nodes = DEFAULTS;

    private static Map<String, Node> buildDefaults() {
        Map<String, Node> m = new LinkedHashMap<>();
        for (Node n : List.of(
                new Node("astral_bolt", 0, 40, 0, List.of(), Bonus.NONE, 0, "astral_bolt"),
                new Node("astral_reservoir", 0, 95, 1, List.of("astral_bolt"), Bonus.MAX_MANA, 50, null),
                new Node("astral_flow", -70, 115, 2, List.of("astral_reservoir"), Bonus.MANA_REGEN, 1.0F, null),
                new Node("stellar_reservoir", 70, 115, 2, List.of("astral_reservoir"), Bonus.MAX_MANA, 100, null),
                new Node("runework_nova", -80, -10, 1, List.of("astral_bolt"), Bonus.NONE, 0, "runework_nova"),
                new Node("rune_ward", -150, -40, 1, List.of("runework_nova"), Bonus.BACKLASH_RESISTANCE, 0.25F, null),
                new Node("celestial_focus", 80, -10, 1, List.of("astral_bolt"), Bonus.SPELL_POWER, 0.10F, null),
                new Node("celestial_focus_2", 150, -40, 2, List.of("celestial_focus"), Bonus.SPELL_POWER, 0.15F, null),
                new Node("astral_judgement", 0, -80, 3, List.of("runework_nova", "celestial_focus"), Bonus.NONE, 0, "astral_judgement"),
                new Node("rune_barrier", -220, -10, 2, List.of("rune_ward"), Bonus.NONE, 0, "rune_barrier"),
                new Node("eclipse_hunger", -150, 55, 1, List.of("astral_bolt"), Bonus.MANA_LEECH, 3, null),
                new Node("eclipse_step", -220, 90, 2, List.of("eclipse_hunger"), Bonus.NONE, 0, "eclipse_step"))) {
            m.put(n.id(), n);
        }
        return Collections.unmodifiableMap(m);
    }

    public static Map<String, Node> nodes() { return nodes; }

    public static @Nullable Node get(String id) { return nodes.get(id); }

    /** Remplace l'arbre courant (rechargement de datapack ou synchro reseau). */
    public static void replace(Map<String, Node> newNodes) {
        Map<String, Node> m = new LinkedHashMap<>(newNodes);
        m.putIfAbsent(ROOT, DEFAULTS.get(ROOT));
        nodes = Collections.unmodifiableMap(m);
    }

    public static float total(Set<String> unlocked, Bonus bonus) {
        float sum = 0;
        Map<String, Node> current = nodes;
        for (String id : unlocked) {
            Node n = current.get(id);
            if (n != null && n.bonus() == bonus) sum += n.amount();
        }
        return sum;
    }

    public static boolean canUnlock(Set<String> unlocked, int points, @Nullable Node n) {
        if (n == null || unlocked.contains(n.id()) || points < n.cost()) return false;
        return unlocked.containsAll(n.requires());
    }

    private SkillTree() {}
}
