package com.aetheria.astralruin.item;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.registry.ModItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public enum AstralArmorMaterial implements ArmorMaterial {
    ASTRAL;

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        int base = switch (type) {
            case HELMET -> 11;
            case CHESTPLATE -> 16;
            case LEGGINGS -> 15;
            case BOOTS -> 13;
        };
        return base * 35;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 3;
            case CHESTPLATE -> 8;
            case LEGGINGS -> 6;
            case BOOTS -> 3;
        };
    }

    @Override
    public int getEnchantmentValue() { return 20; }

    @Override
    public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_NETHERITE; }

    @Override
    public Ingredient getRepairIngredient() { return Ingredient.of(ModItems.ASTRAL_INGOT.get()); }

    /** Textures : assets/aetheria/textures/models/armor/astral_layer_1.png et _layer_2.png */
    @Override
    public String getName() { return Aetheria.MODID + ":astral"; }

    @Override
    public float getToughness() { return 3.0F; }

    @Override
    public float getKnockbackResistance() { return 0.1F; }
}
