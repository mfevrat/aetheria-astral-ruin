package com.aetheria.astralruin.entity.boss;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.util.*;

/**
 * Parametres de combat de Valerius, lus depuis data/aetheria/bosses/valerius.json.
 * Toute cle absente reprend la valeur par defaut : un JSON partiel reste valide.
 */
public final class BossDefinition {
    public record AttackDef(int weight, float minRange, float maxRange, int cooldown, int telegraph,
                            float damage, float radius, int count, int duration, float turnSpeed) {
        static AttackDef fromJson(JsonObject o, AttackDef d) {
            float min = d.minRange, max = d.maxRange;
            if (o.has("range")) {
                var arr = GsonHelper.getAsJsonArray(o, "range");
                min = arr.get(0).getAsFloat();
                max = arr.get(1).getAsFloat();
            }
            return new AttackDef(GsonHelper.getAsInt(o, "weight", d.weight), min, max,
                    GsonHelper.getAsInt(o, "cooldown", d.cooldown), GsonHelper.getAsInt(o, "telegraph", d.telegraph),
                    GsonHelper.getAsFloat(o, "damage", d.damage), GsonHelper.getAsFloat(o, "radius", d.radius),
                    GsonHelper.getAsInt(o, "count", d.count), GsonHelper.getAsInt(o, "duration", d.duration),
                    GsonHelper.getAsFloat(o, "turn_speed", d.turnSpeed));
        }
    }

    public float maxHealth = 600, armor = 12, armorToughness = 4, movementSpeed = 0.28F;
    public float phase2Threshold = 0.5F;
    public int transitionTicks = 100;
    public float transitionPushRadius = 8;
    public List<Float> orbThresholds = List.of(0.4F, 0.2F);
    public float orbHealth = 40, orbHealPerSecond = 2;
    public int orbDeadline = 600;
    public final Map<String, AttackDef> attacks = new LinkedHashMap<>();

    public static final BossDefinition DEFAULT = new BossDefinition();
    private static volatile BossDefinition valerius = DEFAULT;

    private BossDefinition() {
        //                              poids  min  max  cd   tele  degats rayon nb dur  virage
        attacks.put("sword_combo", new AttackDef(5, 0, 4.5F, 80, 10, 14, 0, 3, 20, 0));
        attacks.put("rune_trap", new AttackDef(3, 3, 20, 160, 10, 12, 2.5F, 3, 30, 0));
        attacks.put("eclipse_charge", new AttackDef(2, 6, 16, 240, 15, 16, 0, 1, 12, 0));
        attacks.put("meteors", new AttackDef(4, 0, 30, 200, 10, 10, 2, 8, 40, 0));
        attacks.put("energy_beam", new AttackDef(3, 3, 30, 300, 25, 4, 0, 1, 80, 2));
    }

    public static BossDefinition valerius() { return valerius; }

    public static void setValerius(BossDefinition def) { valerius = def; }

    public AttackDef attack(String id) {
        AttackDef a = attacks.get(id);
        return a != null ? a : DEFAULT.attacks.get(id);
    }

    public static BossDefinition fromJson(JsonObject root) {
        BossDefinition d = new BossDefinition();
        if (root.has("attributes")) {
            JsonObject a = GsonHelper.getAsJsonObject(root, "attributes");
            d.maxHealth = GsonHelper.getAsFloat(a, "max_health", d.maxHealth);
            d.armor = GsonHelper.getAsFloat(a, "armor", d.armor);
            d.armorToughness = GsonHelper.getAsFloat(a, "armor_toughness", d.armorToughness);
            d.movementSpeed = GsonHelper.getAsFloat(a, "movement_speed", d.movementSpeed);
        }
        d.phase2Threshold = GsonHelper.getAsFloat(root, "phase_2_threshold", d.phase2Threshold);
        if (root.has("transition")) {
            JsonObject t = GsonHelper.getAsJsonObject(root, "transition");
            d.transitionTicks = GsonHelper.getAsInt(t, "duration", d.transitionTicks);
            d.transitionPushRadius = GsonHelper.getAsFloat(t, "push_radius", d.transitionPushRadius);
        }
        if (root.has("orbs")) {
            JsonObject o = GsonHelper.getAsJsonObject(root, "orbs");
            if (o.has("thresholds")) {
                List<Float> th = new ArrayList<>();
                for (JsonElement e : GsonHelper.getAsJsonArray(o, "thresholds")) th.add(e.getAsFloat());
                th.sort(Comparator.reverseOrder());
                d.orbThresholds = List.copyOf(th);
            }
            d.orbHealth = GsonHelper.getAsFloat(o, "health", d.orbHealth);
            d.orbHealPerSecond = GsonHelper.getAsFloat(o, "heal_per_second", d.orbHealPerSecond);
            d.orbDeadline = GsonHelper.getAsInt(o, "deadline", d.orbDeadline);
        }
        if (root.has("attacks")) {
            JsonObject atk = GsonHelper.getAsJsonObject(root, "attacks");
            for (String key : atk.keySet()) {
                AttackDef base = d.attacks.get(key);
                if (base == null) continue; // attaque inconnue : ignoree
                d.attacks.put(key, AttackDef.fromJson(GsonHelper.getAsJsonObject(atk, key), base));
            }
        }
        return d;
    }
}
