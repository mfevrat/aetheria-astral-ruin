package com.aetheria.astralruin.spell.impl;

import com.aetheria.astralruin.config.AetheriaConfig;
import com.aetheria.astralruin.entity.spell.AstralBeamEntity;
import com.aetheria.astralruin.particle.RuneCirclePreset;
import com.aetheria.astralruin.registry.ModSounds;
import com.aetheria.astralruin.spell.AbstractSpell;
import com.aetheria.astralruin.spell.CastContext;
import com.aetheria.astralruin.util.Fx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Sort ultime : cercle tridimensionnel de 15 blocs dans le ciel,
 * puis pilier de lumiere de 5 blocs de large pendant 5 secondes.
 */
public class AstralJudgementSpell extends AbstractSpell {
    private static final int BEAM_TICKS = 100;
    private static final float RADIUS = 2.5F;
    private static final float HEIGHT = 25F;

    public AstralJudgementSpell() {
        super("astral_judgement", 120, 60, 90 * 20, Focus.STAFF);
    }

    @Override
    public void onCastStart(ServerPlayer caster, CastContext ctx) {
        ServerLevel level = caster.serverLevel();
        ctx.target = findGround(caster);

        // Le lanceur s'eleve legerement pendant l'incantation
        caster.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 15, 0, false, false));
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, ctx.castTicks + 40, 0, false, false));

        Fx.circle(level, ctx.target.add(0, HEIGHT, 0), new Vector3f(0, 1, 0),
                RuneCirclePreset.JUDGEMENT, 7.5F, ctx.castTicks + BEAM_TICKS);
        Fx.groundCircle(level, ctx.target, RuneCirclePreset.TELEGRAPH, RADIUS, ctx.castTicks + 10);
        Fx.circle(level, caster.position().add(0, 0.1, 0), new Vector3f(0, 1, 0),
                RuneCirclePreset.NOVA, 1.6F, ctx.castTicks);
    }

    @Override
    public boolean onCastTick(ServerPlayer caster, CastContext ctx, int tick) {
        ServerLevel level = caster.serverLevel();
        if (tick < ctx.castTicks) {
            if (tick % 10 == 0) Fx.line(level, ctx.target.add(0, HEIGHT, 0), ctx.target, Fx.GOLD, 0.12F, 2.0);
            return true;
        }
        boolean breakBlocks = AetheriaConfig.JUDGEMENT_BREAKS_BLOCKS.get();
        AstralBeamEntity beam = AstralBeamEntity.create(level, ctx.target, caster, RADIUS, HEIGHT,
                BEAM_TICKS, 6F * ctx.power, 0xFFF6DC, breakBlocks);
        level.addFreshEntity(beam);

        Fx.groundCircle(level, ctx.target, RuneCirclePreset.SHOCKWAVE, 9F, 12);
        Fx.shockwave(level, ctx.target, 14F, 0xFFF4D0, 20);
        Fx.flare(level, ctx.target.add(0, 1, 0), 7F, 0xFFFFFF, 14);
        for (int i = 0; i < 6; i++) {
            double a = Math.PI * 2 * i / 6;
            Fx.lightning(level, ctx.target.add(0, HEIGHT, 0), ctx.target.add(Math.cos(a) * 6, 0, Math.sin(a) * 6), 0xFFE9A8, 0.45F, 10);
        }
        Fx.burst(level, ctx.target.add(0, 0.5, 0), Fx.WHITE, 80, 0.6F);
        Fx.shake(level, ctx.target, 0.8F, 25, 48);
        level.playSound(null, ctx.target.x, ctx.target.y, ctx.target.z, ModSounds.JUDGEMENT_IMPACT.get(),
                SoundSource.PLAYERS, 3.0F, 1.0F);
        return false;
    }

    /** Point vise, ramene au sol s'il est dans le vide. */
    private static Vec3 findGround(ServerPlayer caster) {
        HitResult hit = caster.pick(48, 0F, false);
        Vec3 point = hit.getLocation();
        BlockHitResult down = caster.level().clip(new ClipContext(point.add(0, 0.5, 0), point.add(0, -40, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        return down.getType() == HitResult.Type.MISS ? point : down.getLocation();
    }
}
