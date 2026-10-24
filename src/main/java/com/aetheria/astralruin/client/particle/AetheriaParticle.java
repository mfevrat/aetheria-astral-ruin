package com.aetheria.astralruin.client.particle;

import com.aetheria.astralruin.particle.AstralParticleOptions;
import com.aetheria.astralruin.particle.AstralParticleOptions.Kind;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

/** Particule generique du mod : le comportement depend de la famille (Kind). */
public class AetheriaParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final Kind kind;
    private final float baseSize;
    private final float spin;

    protected AetheriaParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz,
                               AstralParticleOptions opt, SpriteSet sprites, Kind kind) {
        super(level, x, y, z);
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.rCol = opt.r();
        this.gCol = opt.g();
        this.bCol = opt.b();
        this.quadSize = this.baseSize = opt.scale();
        this.lifetime = Math.max(1, opt.lifetime());
        this.sprites = sprites;
        this.kind = kind;
        this.hasPhysics = false;
        this.gravity = 0F;
        this.friction = 0.9F;
        this.spin = (random.nextFloat() - 0.5F) * 0.3F;
        this.roll = random.nextFloat() * (float) (Math.PI * 2);
        this.oRoll = roll;
        switch (kind) {
            case SHARD -> {
                this.gravity = 0.6F;
                this.hasPhysics = true;
                this.friction = 0.96F;
                pickSprite(sprites);
            }
            case GLYPH -> {
                pickSprite(sprites);
                this.friction = 0.95F;
            }
            case EMBER -> this.friction = 0.92F;
            case SMOKE -> this.friction = 0.94F;
            default -> setSpriteFromAge(sprites);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (removed) return;
        float life = (float) age / lifetime;
        oRoll = roll;
        switch (kind) {
            case SPARK -> {
                setSpriteFromAge(sprites);
                alpha = Mth.clamp((lifetime - age) / (lifetime * 0.3F), 0F, 1F);
            }
            case STAR -> {
                setSpriteFromAge(sprites);
                roll += spin * 0.5F;
                quadSize = baseSize * (0.7F + 0.3F * Mth.sin(age * 0.8F)) * (1F - life * 0.5F);
                alpha = 1F - life * life;
            }
            case EMBER -> {
                setSpriteFromAge(sprites);
                yd += 0.004;
                quadSize = baseSize * (1F - life);
                alpha = 1F - life;
            }
            case SMOKE -> {
                setSpriteFromAge(sprites);
                yd += 0.002;
                roll += spin * 0.2F;
                quadSize = baseSize * (1F + life * 0.9F);
                alpha = 0.75F * (1F - life);
            }
            case SHARD -> {
                roll += spin * 2F;
                alpha = Mth.clamp((lifetime - age) / (lifetime * 0.25F), 0F, 1F);
            }
            case GLYPH -> {
                yd += 0.0015;
                roll += spin * 0.15F;
                alpha = Mth.clamp(Math.min(age / 5F, (lifetime - age) / 8F), 0F, 1F);
            }
        }
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Provider(SpriteSet sprites, Kind kind) implements ParticleProvider<AstralParticleOptions> {
        @Override
        public Particle createParticle(@NotNull AstralParticleOptions opt, @NotNull ClientLevel level,
                                       double x, double y, double z, double vx, double vy, double vz) {
            return new AetheriaParticle(level, x, y, z, vx, vy, vz, opt, sprites, kind);
        }
    }
}
