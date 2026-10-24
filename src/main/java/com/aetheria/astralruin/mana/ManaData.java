package com.aetheria.astralruin.mana;

import com.aetheria.astralruin.registry.ModAttributes;
import com.aetheria.astralruin.skill.SkillTree;
import com.aetheria.astralruin.spell.SpellRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/** Toutes les donnees magiques d'un joueur : mana, arbre runique, recharges, titres. */
public class ManaData {
    private float mana = 100;
    private int selectedSpell;
    private final Set<String> unlocked = new LinkedHashSet<>(List.of(SkillTree.ROOT));
    private int points;
    private float spentAccumulator;
    private int regenBlockedTicks;
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Set<String> castSpells = new HashSet<>();
    private String titleKey = "";
    private float bonusMaxMana;
    private boolean rampartReceived;
    private boolean introSeen;
    /** Valeur synchronisee pour le client (le client ne calcule pas les attributs des autres sources). */
    private float syncedMaxMana = 100;
    private boolean dirty = true;

    // ---------- Valeurs derivees ----------
    public float maxMana(Player p) {
        return (float) p.getAttributeValue(ModAttributes.MAX_MANA.get()) + bonusMaxMana
                + SkillTree.total(unlocked, SkillTree.Bonus.MAX_MANA);
    }

    public float regenPerSecond(Player p) {
        return (float) p.getAttributeValue(ModAttributes.MANA_REGEN.get())
                + SkillTree.total(unlocked, SkillTree.Bonus.MANA_REGEN);
    }

    public float spellPower(Player p) {
        return (float) p.getAttributeValue(ModAttributes.SPELL_POWER.get())
                * (1F + SkillTree.total(unlocked, SkillTree.Bonus.SPELL_POWER));
    }

    /** Mana rendue par coup de Lame d'Eclipse (noeud Faim d'Eclipse + lame eveillee). */
    public float manaLeech(Player p) {
        float bonus = p.getMainHandItem().getItem() instanceof com.aetheria.astralruin.item.EclipseBladeItem blade ? blade.manaLeech() : 0F;
        return SkillTree.total(unlocked, SkillTree.Bonus.MANA_LEECH) + bonus;
    }

    public float backlashResistance(Player p) {
        return Mth.clamp((float) p.getAttributeValue(ModAttributes.BACKLASH_RESISTANCE.get())
                + SkillTree.total(unlocked, SkillTree.Bonus.BACKLASH_RESISTANCE), 0F, 1F);
    }

    // ---------- Mana ----------
    public float getMana() { return mana; }

    public void setMana(float value) {
        if (value != mana) {
            mana = value;
            dirty = true;
        }
    }

    /** Ajoute la mana depensee au compteur d'Eclats Astraux (1 point par 500 mana). */
    public void recordSpent(float amount) {
        spentAccumulator += amount;
        while (spentAccumulator >= 500F) {
            spentAccumulator -= 500F;
            points++;
        }
        dirty = true;
    }

    public int getRegenBlockedTicks() { return regenBlockedTicks; }
    public void setRegenBlockedTicks(int t) { regenBlockedTicks = t; }

    // ---------- Sorts ----------
    public List<String> unlockedSpells() {
        List<String> out = new ArrayList<>();
        for (String spellId : SpellRegistry.ORDER) {
            for (String nodeId : unlocked) {
                SkillTree.Node n = SkillTree.get(nodeId);
                if (n != null && spellId.equals(n.spell())) {
                    out.add(spellId);
                    break;
                }
            }
        }
        return out;
    }

    public boolean isSpellUnlocked(String spellId) { return unlockedSpells().contains(spellId); }

    public String selectedSpellId() {
        List<String> list = unlockedSpells();
        if (list.isEmpty()) return SkillTree.ROOT;
        return list.get(Math.floorMod(selectedSpell, list.size()));
    }

    public void cycleSpell(int dir) {
        int size = Math.max(1, unlockedSpells().size());
        selectedSpell = Math.floorMod(selectedSpell + dir, size);
        dirty = true;
    }

    public long cooldownReadyAt(String spellId) { return cooldowns.getOrDefault(spellId, 0L); }

    public int cooldownRemaining(String spellId, long gameTime) {
        return (int) Math.max(0, cooldownReadyAt(spellId) - gameTime);
    }

    public void setCooldown(String spellId, long readyAt) {
        cooldowns.put(spellId, readyAt);
        dirty = true;
    }

    /** @return le nombre de sorts distincts lances depuis toujours. */
    public int recordCast(String spellId) {
        castSpells.add(spellId);
        return castSpells.size();
    }

    // ---------- Arbre runique ----------
    public Set<String> unlockedNodes() { return Collections.unmodifiableSet(unlocked); }

    public boolean tryUnlock(String nodeId) {
        SkillTree.Node n = SkillTree.get(nodeId);
        if (!SkillTree.canUnlock(unlocked, points, n)) return false;
        points -= n.cost();
        unlocked.add(nodeId);
        dirty = true;
        return true;
    }

    public int getPoints() { return points; }

    public void addPoints(int amount) {
        points += amount;
        dirty = true;
    }

    // ---------- Progression ----------
    public void addBonusMaxMana(float amount) {
        bonusMaxMana += amount;
        dirty = true;
    }

    public String getTitleKey() { return titleKey; }

    public void setTitleKey(String key) {
        titleKey = key;
        dirty = true;
    }

    public boolean hasSeenIntro() { return introSeen; }
    public void markIntroSeen() { introSeen = true; }

    public boolean hasReceivedRampart() { return rampartReceived; }
    public void markRampartReceived() { rampartReceived = true; }

    // ---------- Synchro ----------
    public boolean isDirty() { return dirty; }
    public void markDirty() { dirty = true; }
    public void clearDirty() { dirty = false; }

    public float getSyncedMaxMana() { return syncedMaxMana; }

    // ---------- NBT ----------
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("mana", mana);
        tag.putInt("selected", selectedSpell);
        ListTag nodes = new ListTag();
        unlocked.forEach(id -> nodes.add(StringTag.valueOf(id)));
        tag.put("unlocked", nodes);
        tag.putInt("points", points);
        tag.putFloat("spent", spentAccumulator);
        CompoundTag cds = new CompoundTag();
        cooldowns.forEach(cds::putLong);
        tag.put("cooldowns", cds);
        ListTag cast = new ListTag();
        castSpells.forEach(id -> cast.add(StringTag.valueOf(id)));
        tag.put("cast", cast);
        tag.putString("title", titleKey);
        tag.putFloat("bonusMax", bonusMaxMana);
        tag.putBoolean("rampart", rampartReceived);
        tag.putBoolean("introSeen", introSeen);
        return tag;
    }

    public void load(CompoundTag tag) {
        mana = tag.getFloat("mana");
        selectedSpell = tag.getInt("selected");
        unlocked.clear();
        unlocked.add(SkillTree.ROOT);
        tag.getList("unlocked", Tag.TAG_STRING).forEach(t -> unlocked.add(t.getAsString()));
        points = tag.getInt("points");
        spentAccumulator = tag.getFloat("spent");
        cooldowns.clear();
        CompoundTag cds = tag.getCompound("cooldowns");
        for (String key : cds.getAllKeys()) cooldowns.put(key, cds.getLong(key));
        castSpells.clear();
        tag.getList("cast", Tag.TAG_STRING).forEach(t -> castSpells.add(t.getAsString()));
        titleKey = tag.getString("title");
        bonusMaxMana = tag.getFloat("bonusMax");
        rampartReceived = tag.getBoolean("rampart");
        introSeen = tag.getBoolean("introSeen");
        if (tag.contains("syncedMax")) syncedMaxMana = tag.getFloat("syncedMax");
        dirty = true;
    }

    public CompoundTag toSyncTag(Player p) {
        CompoundTag tag = save();
        tag.putFloat("syncedMax", maxMana(p));
        return tag;
    }

    /** Copie apres mort ou retour de l'End. A la mort, la mana repart a 50 %. */
    public void copyFrom(ManaData old, boolean death, Player newPlayer) {
        load(old.save());
        if (death) {
            regenBlockedTicks = 0;
            mana = Math.max(0, maxMana(newPlayer) * 0.5F);
        }
    }
}
