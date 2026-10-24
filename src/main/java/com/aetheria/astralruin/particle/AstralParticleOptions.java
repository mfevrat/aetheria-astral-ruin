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

/**
 * Options communes a toutes les particules du mod : type (texture + comportement), couleur, taille, duree.
 * Un meme jeu d'options sert aux 6 types, chacun avec sa propre texture animee.
 */
public record AstralParticleOptions(ParticleType<AstralParticleOptions> type, float r, float g, float b,
                                    float scale, int lifetime) implements ParticleOptions {

    /** Familles de particules : chacune a ses sprites et son comportement (voir AetheriaParticle). */
    public enum Kind { SPARK, STAR, EMBER, SMOKE, SHARD, GLYPH }

    public static Codec<AstralParticleOptions> codec(ParticleType<AstralParticleOptions> type) {
        return RecordCodecBuilder.create(i -> i.group(
                Codec.FLOAT.fieldOf("r").forGetter(AstralParticleOptions::r),
                Codec.FLOAT.fieldOf("g").forGetter(AstralParticleOptions::g),
                Codec.FLOAT.fieldOf("b").forGetter(AstralParticleOptions::b),
                Codec.FLOAT.fieldOf("scale").forGetter(AstralParticleOptions::scale),
                Codec.INT.fieldOf("lifetime").forGetter(AstralParticleOptions::lifetime)
        ).apply(i, (r, g, b, s, l) -> new AstralParticleOptions(type, r, g, b, s, l)));
    }

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
            return new AstralParticleOptions(type, r, g, b, s, life);
        }

        @Override
        public AstralParticleOptions fromNetwork(ParticleType<AstralParticleOptions> type, FriendlyByteBuf buf) {
            return new AstralParticleOptions(type, buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readVarInt());
        }
    };

    /** Etincelle losange (type historique). */
    public static AstralParticleOptions rgb(int rgb, float scale, int lifetime) {
        return of(Kind.SPARK, rgb, scale, lifetime);
    }

    public static AstralParticleOptions of(Kind kind, int rgb, float scale, int lifetime) {
        return new AstralParticleOptions(ModParticles.get(kind), (rgb >> 16 & 0xFF) / 255F, (rgb >> 8 & 0xFF) / 255F,
                (rgb & 0xFF) / 255F, scale, lifetime);
    }

    @Override
    public ParticleType<?> getType() {
        return type;
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
