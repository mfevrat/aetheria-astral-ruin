package com.aetheria.astralruin.registry;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.item.*;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Aetheria.MODID);

    // Materiaux
    public static final RegistryObject<Item> ASTRAL_SHARD = ITEMS.register("astral_shard",
            () -> new AnimatedItem("astral_shard", new Item.Properties()));
    public static final RegistryObject<Item> ASTRAL_INGOT = ITEMS.register("astral_ingot",
            () -> new AnimatedItem("astral_ingot", new Item.Properties()));
    public static final RegistryObject<Item> STELE_FRAGMENT = ITEMS.register("stele_fragment",
            () -> new SteleFragmentItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> RUNE_HEART = ITEMS.register("rune_heart",
            () -> new RuneHeartItem(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> MANA_CRYSTAL = ITEMS.register("mana_crystal",
            () -> new ManaCrystalItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> ASTRAL_SIGIL = ITEMS.register("astral_sigil",
            () -> new AstralSigilItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    /** Objet technique : sert uniquement au rendu de la lame d'Eclipse projetee. */
    public static final RegistryObject<Item> ECLIPSE_SLASH = ITEMS.register("eclipse_slash",
            () -> new AnimatedItem("eclipse_slash", new Item.Properties()));

    /** Objet technique : modele 3D des lames spectrales de Valerius. */
    public static final RegistryObject<Item> SPECTRAL_BLADE = ITEMS.register("spectral_blade",
            () -> new AnimatedItem("spectral_blade", new Item.Properties()));

    // Equipements
    public static final RegistryObject<Item> ASTRAL_GRIMOIRE = ITEMS.register("astral_grimoire",
            () -> new GrimoireItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> ARCHMAGE_STAFF = ITEMS.register("archmage_staff",
            () -> new StaffItem(new Item.Properties().stacksTo(1).durability(1800).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ECLIPSE_BLADE = ITEMS.register("eclipse_blade",
            () -> new EclipseBladeItem(Tiers.NETHERITE, 4, -2.4F,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> ASTRAL_HELMET = ITEMS.register("astral_helmet",
            () -> new AstralArmorItem(ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ASTRAL_CHESTPLATE = ITEMS.register("astral_chestplate",
            () -> new AstralArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ASTRAL_LEGGINGS = ITEMS.register("astral_leggings",
            () -> new AstralArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ASTRAL_BOOTS = ITEMS.register("astral_boots",
            () -> new AstralArmorItem(ArmorItem.Type.BOOTS, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ECLIPSE_RAMPART = ITEMS.register("eclipse_rampart",
            () -> new EclipseRampartItem(new Item.Properties().durability(1200).rarity(Rarity.EPIC).fireResistant()));

    // Armes Eveillees (Coeur de Runes Grave)
    public static final RegistryObject<Item> AWAKENED_STAFF = ITEMS.register("awakened_archmage_staff",
            () -> new StaffItem(new Item.Properties().stacksTo(1).durability(3200).rarity(Rarity.EPIC).fireResistant(),
                    "awakened_archmage_staff", 0.35, 200, 0.85F));
    public static final RegistryObject<Item> AWAKENED_BLADE = ITEMS.register("awakened_eclipse_blade",
            () -> new EclipseBladeItem(Tiers.NETHERITE, 6, -2.3F, new Item.Properties().rarity(Rarity.EPIC).fireResistant(),
                    "awakened_eclipse_blade", 0.15, 10F, 4, 2F));

    // Blocs
    public static final RegistryObject<Item> ASTRAL_ALTAR = ITEMS.register("astral_altar",
            () -> new AltarBlockItem(ModBlocks.ASTRAL_ALTAR.get(), new Item.Properties().rarity(Rarity.EPIC)));

    // Oeuf de test
    public static final RegistryObject<Item> VALERIUS_SPAWN_EGG = ITEMS.register("valerius_spawn_egg",
            () -> new AnimatedSpawnEggItem(ModEntities.VALERIUS, 0x1E1830, 0xB98CFF, "valerius_spawn_egg", new Item.Properties()));

    private ModItems() {}
}
