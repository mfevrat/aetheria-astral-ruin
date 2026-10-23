package com.aetheria.astralruin.client.particle;

import com.aetheria.astralruin.particle.AstralParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

/** Losange incandescent, pleine luminosite, fondu sur les 30 % finaux de sa vie. */
public class AstralSparkParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected AstralSparkParticle(ClientLevel level, double x, double y, double z,
                                  double vx, double vy, double vz, AstralParticleOptions opt, SpriteSet sprites) {
        super(level, x, y, z);
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.rCol = opt.r();
        this.gCol = opt.g();
        this.bCol = opt.b();
        this.quadSize = opt.scale();
        this.lifetime = Math.max(1, opt.lifetime());
        this.gravity = 0F;
        this.friction = 0.9F;
        this.hasPhysics = false;
        this.sprites = sprites;
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!removed) setSpriteFromAge(sprites);
        this.alpha = Mth.clamp((lifetime - age) / (lifetime * 0.3F), 0F, 1F);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<AstralParticleOptions> {
        @Override
        public Particle createParticle(@NotNull AstralParticleOptions opt, @NotNull ClientLevel level,
                                       double x, double y, double z, double vx, double vy, double vz) {
            return new AstralSparkParticle(level, x, y, z, vx, vy, vz, opt, sprites);
        }
    }
}
