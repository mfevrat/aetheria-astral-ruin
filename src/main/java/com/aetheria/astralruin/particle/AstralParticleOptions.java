package com.aetheria.astralruin.particle;

import com.aetheria.astralruin.registry.ModParticles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Locale;

/** Etincelle astrale : couleur RGB, taille et duree de vie en ticks. */
public record AstralParticleOptions(float r, float g, float b, float scale, int lifetime) implements ParticleOptions {

    public static final Codec<AstralParticleOptions> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.fieldOf("r").forGetter(AstralParticleOptions::r),
            Codec.FLOAT.fieldOf("g").forGetter(AstralParticleOptions::g),
            Codec.FLOAT.fieldOf("b").forGetter(AstralParticleOptions::b),
            Codec.FLOAT.fieldOf("scale").forGetter(AstralParticleOptions::scale),
            Codec.INT.fieldOf("lifetime").forGetter(AstralParticleOptions::lifetime)
    ).apply(i, AstralParticleOptions::new));

    @SuppressWarnings("deprecation")
    public static final ParticleOptions.Deserializer<AstralParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public AstralParticleOptions fromCommand(ParticleType<AstralParticleOptions> type, StringReader reader)
                throws CommandSyntaxException {
            reader.expect(' ');
            float r = reader.readFloat();
            reader.expect(' ');
            float g = reader.readFloat();
            reader.expect(' ');
            float b = reader.readFloat();
            reader.expect(' ');
            float s = reader.readFloat();
            reader.expect(' ');
            int life = reader.readInt();
            return new AstralParticleOptions(r, g, b, s, life);
        }

        @Override
        public AstralParticleOptions fromNetwork(ParticleType<AstralParticleOptions> type, FriendlyByteBuf buf) {
            return new AstralParticleOptions(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readVarInt());
        }
    };

    public static AstralParticleOptions rgb(int rgb, float scale, int lifetime) {
        return new AstralParticleOptions((rgb >> 16 & 0xFF) / 255F, (rgb >> 8 & 0xFF) / 255F, (rgb & 0xFF) / 255F, scale, lifetime);
    }

    @Override
    public ParticleType<?> getType() {
        return ModParticles.ASTRAL_SPARK.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeFloat(r);
        buf.writeFloat(g);
        buf.writeFloat(b);
        buf.writeFloat(scale);
        buf.writeVarInt(lifetime);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %d",
                BuiltInRegistries.PARTICLE_TYPE.getKey(getType()), r, g, b, scale, lifetime);
    }
}
