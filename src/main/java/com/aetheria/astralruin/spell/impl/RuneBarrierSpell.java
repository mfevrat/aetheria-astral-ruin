package com.aetheria.astralruin.spell.impl;

import com.aetheria.astralruin.particle.RuneCirclePreset;
import com.aetheria.astralruin.registry.ModSounds;
import com.aetheria.astralruin.spell.AbstractSpell;
import com.aetheria.astralruin.spell.CastContext;
import com.aetheria.astralruin.util.Fx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Branche Runique : trois cercles verticaux enveloppent le lanceur, puis un bouclier d'absorption. */
public class RuneBarrierSpell extends AbstractSpell {
    private static final double PUSH_RADIUS = 4.0;

    public RuneBarrierSpell() {
        super("rune_barrier", 35, 10, 400, Focus.STAFF_OR_BLADE);
    }

    @Override
    public void onCastStart(ServerPlayer caster, CastContext ctx) {
        ServerLevel level = caster.serverLevel();
        Fx.implosion(level, caster.position(), 6F, Fx.VIOLET, ctx.castTicks + 4);
        Vec3 chest = caster.position().add(0, 1.0, 0);
        Vector3f[] normals = {new Vector3f(1, 0, 0), new Vector3f(0, 0, 1), new Vector3f(0.7F, 0, 0.7F)};
        for (Vector3f n : normals) Fx.circle(level, chest, n, RuneCirclePreset.BARRIER, 1.7F, ctx.castTicks + 40);
    }

    @Override
    public boolean onCastTick(ServerPlayer caster, CastContext ctx, int tick) {
        if (tick < ctx.castTicks) return true;
        ServerLevel level = caster.serverLevel();
        int amplifier = Mth.clamp(Mth.floor(ctx.power * 2F) - 1, 0, 4);
        caster.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, amplifier));
        caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0));

        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(PUSH_RADIUS),
                e -> e != caster && e.isAlive() && e instanceof Enemy)) {
            Vec3 away = e.position().subtract(caster.position()).multiply(1, 0, 1);
            if (away.lengthSqr() < 1e-4) continue;
            away = away.normalize();
            e.knockback(1.2, -away.x, -away.z);
        }
        Fx.groundCircle(level, caster.position(), RuneCirclePreset.SHOCKWAVE, (float) PUSH_RADIUS + 1, 10);
        Fx.burst(level, caster.position().add(0, 1, 0), Fx.VIOLET, 40, 0.3F);
        Fx.flare(level, caster.position().add(0, 1, 0), 2.4F, 0xC8A8FF, 10);
        Fx.particles(level, com.aetheria.astralruin.particle.AstralParticleOptions.Kind.GLYPH, caster.position().add(0, 1, 0), 24, 1.2, 0.05, 0xFFD36B, 0.25F, 30);
        level.playSound(null, caster.blockPosition(), ModSounds.CRYSTAL_RESONANCE.get(), SoundSource.PLAYERS, 1.2F, 0.8F);
        return false;
    }
}
