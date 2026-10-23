package com.aetheria.astralruin.registry;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.entity.boss.ValeriusEntity;
import com.aetheria.astralruin.entity.boss.ValeriusOrbEntity;
import com.aetheria.astralruin.entity.spell.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Aetheria.MODID);

    public static final RegistryObject<EntityType<ValeriusEntity>> VALERIUS = ENTITIES.register("valerius",
            () -> EntityType.Builder.<ValeriusEntity>of(ValeriusEntity::new, MobCategory.MONSTER)
                    .sized(1.4F, 3.3F).fireImmune().clientTrackingRange(10).build("valerius"));

    public static final RegistryObject<EntityType<ValeriusOrbEntity>> VALERIUS_ORB = ENTITIES.register("valerius_orb",
            () -> EntityType.Builder.<ValeriusOrbEntity>of(ValeriusOrbEntity::new, MobCategory.MISC)
                    .sized(0.9F, 0.9F).fireImmune().clientTrackingRange(10).build("valerius_orb"));

    public static final RegistryObject<EntityType<AstralBeamEntity>> ASTRAL_BEAM = ENTITIES.register("astral_beam",
            () -> EntityType.Builder.<AstralBeamEntity>of(AstralBeamEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).fireImmune().noSummon().clientTrackingRange(10).updateInterval(20)
                    .build("astral_beam"));

    public static final RegistryObject<EntityType<ManaCrystalEntity>> MANA_CRYSTAL = ENTITIES.register("mana_crystal",
            () -> EntityType.Builder.<ManaCrystalEntity>of(ManaCrystalEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F).clientTrackingRange(6).updateInterval(10).build("mana_crystal"));

    public static final RegistryObject<EntityType<EclipseSlashEntity>> ECLIPSE_SLASH = ENTITIES.register("eclipse_slash",
            () -> EntityType.Builder.<EclipseSlashEntity>of(EclipseSlashEntity::new, MobCategory.MISC)
                    .sized(0.8F, 0.4F).clientTrackingRange(6).updateInterval(5).build("eclipse_slash"));

    public static final RegistryObject<EntityType<RuneStrikeEntity>> RUNE_STRIKE = ENTITIES.register("rune_strike",
            () -> EntityType.Builder.<RuneStrikeEntity>of(RuneStrikeEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.1F).fireImmune().noSummon().clientTrackingRange(8).updateInterval(20)
                    .build("rune_strike"));

    private ModEntities() {}
}
