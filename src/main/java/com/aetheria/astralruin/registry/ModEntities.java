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

    public static final RegistryObject<EntityType<CometEntity>> COMET = ENTITIES.register("comet",
            () -> EntityType.Builder.<CometEntity>of(CometEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(2).build("comet"));

    public static final RegistryObject<EntityType<SolarHaloEntity>> SOLAR_HALO = ENTITIES.register("solar_halo",
            () -> EntityType.Builder.<SolarHaloEntity>of(SolarHaloEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).fireImmune().noSummon().clientTrackingRange(8).updateInterval(5).build("solar_halo"));

    public static final RegistryObject<EntityType<GlyphPrisonEntity>> GLYPH_PRISON = ENTITIES.register("glyph_prison",
            () -> EntityType.Builder.<GlyphPrisonEntity>of(GlyphPrisonEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).fireImmune().noSummon().clientTrackingRange(8).updateInterval(10).build("glyph_prison"));

    public static final RegistryObject<EntityType<GravityWellEntity>> GRAVITY_WELL = ENTITIES.register("gravity_well",
            () -> EntityType.Builder.<GravityWellEntity>of(GravityWellEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F).fireImmune().noSummon().clientTrackingRange(10).updateInterval(20).build("gravity_well"));

    public static final RegistryObject<EntityType<SpectralBladeEntity>> SPECTRAL_BLADE = ENTITIES.register("spectral_blade",
            () -> EntityType.Builder.<SpectralBladeEntity>of(SpectralBladeEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(2).build("spectral_blade"));

    /** Camera des cinematiques : jamais ajoutee au monde, creee cote client uniquement. */
    public static final RegistryObject<EntityType<com.aetheria.astralruin.entity.CinematicCameraEntity>> CINEMATIC_CAMERA =
            ENTITIES.register("cinematic_camera", () -> EntityType.Builder.<com.aetheria.astralruin.entity.CinematicCameraEntity>of(
                    com.aetheria.astralruin.entity.CinematicCameraEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F).noSummon().clientTrackingRange(0).build("cinematic_camera"));

    private ModEntities() {}
}
