package com.aetheria.astralruin.registry;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.particle.AstralParticleOptions;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Aetheria.MODID);

    public static final RegistryObject<ParticleType<AstralParticleOptions>> ASTRAL_SPARK =
            PARTICLES.register("astral_spark", () ->
                    new ParticleType<AstralParticleOptions>(true, AstralParticleOptions.DESERIALIZER) {
                        @Override
                        public Codec<AstralParticleOptions> codec() {
                            return AstralParticleOptions.CODEC;
                        }
                    });

    private ModParticles() {}
}
