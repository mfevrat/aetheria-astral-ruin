package com.aetheria.astralruin.registry;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.particle.AstralParticleOptions;
import com.aetheria.astralruin.particle.AstralParticleOptions.Kind;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Aetheria.MODID);

    public static final RegistryObject<ParticleType<AstralParticleOptions>> ASTRAL_SPARK = reg("astral_spark");
    public static final RegistryObject<ParticleType<AstralParticleOptions>> STAR_FLARE = reg("star_flare");
    public static final RegistryObject<ParticleType<AstralParticleOptions>> EMBER = reg("ember");
    public static final RegistryObject<ParticleType<AstralParticleOptions>> VOID_SMOKE = reg("void_smoke");
    public static final RegistryObject<ParticleType<AstralParticleOptions>> SHARD = reg("shard");
    public static final RegistryObject<ParticleType<AstralParticleOptions>> RUNE_GLYPH = reg("rune_glyph");

    private static RegistryObject<ParticleType<AstralParticleOptions>> reg(String name) {
        return PARTICLES.register(name, () -> new ParticleType<AstralParticleOptions>(true, AstralParticleOptions.DESERIALIZER) {
            private Codec<AstralParticleOptions> codec;

            @Override
            public Codec<AstralParticleOptions> codec() {
                if (codec == null) codec = AstralParticleOptions.codec(this);
                return codec;
            }
        });
    }

    public static ParticleType<AstralParticleOptions> get(Kind kind) {
        return switch (kind) {
            case SPARK -> ASTRAL_SPARK.get();
            case STAR -> STAR_FLARE.get();
            case EMBER -> EMBER.get();
            case SMOKE -> VOID_SMOKE.get();
            case SHARD -> SHARD.get();
            case GLYPH -> RUNE_GLYPH.get();
        };
    }

    private ModParticles() {}
}
