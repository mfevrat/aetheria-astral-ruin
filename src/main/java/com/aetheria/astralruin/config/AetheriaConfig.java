package com.aetheria.astralruin.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class AetheriaConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final ForgeConfigSpec.BooleanValue JUDGEMENT_BREAKS_BLOCKS;
    public static final ForgeConfigSpec.IntValue JUDGEMENT_BLOCKS_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue BOSS_HEALTH_MULTIPLIER;

    public static final ForgeConfigSpec.DoubleValue SHAKE_SCALE;
    public static final ForgeConfigSpec.DoubleValue PARTICLE_DENSITY;
    public static final ForgeConfigSpec.BooleanValue SHOW_AURA;
    public static final ForgeConfigSpec.BooleanValue RENDER_DECALS;
    public static final ForgeConfigSpec.BooleanValue BOSS_MUSIC;

    static {
        ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
        common.push("spells");
        JUDGEMENT_BREAKS_BLOCKS = common
                .comment("Astral Judgement desintegre les blocs fragiles (respecte aussi la gamerule mobGriefing).")
                .define("judgementBreaksBlocks", true);
        JUDGEMENT_BLOCKS_PER_TICK = common
                .comment("Nombre maximum de blocs detruits par tick par le rayon.")
                .defineInRange("judgementBlocksPerTick", 12, 0, 64);
        common.pop();
        common.push("boss");
        BOSS_HEALTH_MULTIPLIER = common
                .comment("Multiplicateur de PV de Valerius (utile en multijoueur).")
                .defineInRange("valeriusHealthMultiplier", 1.0, 0.1, 20.0);
        common.pop();
        COMMON_SPEC = common.build();

        ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
        client.push("visuals");
        SHAKE_SCALE = client.comment("Intensite des secousses camera (0 = desactive).")
                .defineInRange("cameraShakeScale", 1.0, 0.0, 2.0);
        PARTICLE_DENSITY = client.comment("Densite des particules des cercles runiques.")
                .defineInRange("circleParticleDensity", 1.0, 0.1, 2.0);
        SHOW_AURA = client.comment("Afficher l'aura runique du set Astral.")
                .define("showAstralAura", true);
        RENDER_DECALS = client.comment("Dessiner les cercles runiques en decalques textures (fluide, moins de particules).")
                .define("renderCircleDecals", true);
        BOSS_MUSIC = client.comment("Jouer la musique de combat pres de Valerius.")
                .define("bossMusic", true);
        client.pop();
        CLIENT_SPEC = client.build();
    }

    private AetheriaConfig() {}
}
