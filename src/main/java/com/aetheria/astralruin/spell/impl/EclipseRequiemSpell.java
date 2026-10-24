package com.aetheria.astralruin.spell.impl;

import com.aetheria.astralruin.entity.spell.*;
import com.aetheria.astralruin.particle.AstralParticleOptions.Kind;
import com.aetheria.astralruin.particle.RuneCirclePreset;
import com.aetheria.astralruin.registry.ModSounds;
import com.aetheria.astralruin.spell.AbstractSpell;
import com.aetheria.astralruin.spell.CastContext;
import com.aetheria.astralruin.spell.SpellTargeting;
import com.aetheria.astralruin.util.Fx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/** Eclipse (ultime) : dome d'eclipse de 3 s, lames qui tranchent tout ennemi a 7 blocs, puis explosion. */
public class EclipseRequiemSpell extends AbstractSpell {
    private static final double RADIUS = 7.0;

    public EclipseRequiemSpell() {
        super("eclipse_requiem", 130, 60, 1800, Focus.STAFF_OR_BLADE);
    }

    @Override
    public void onCastStart(ServerPlayer caster, CastContext ctx) {
        ServerLevel level = caster.serverLevel();
        ctx.target = caster.position();
        Fx.groundCircle(level, ctx.target, RuneCirclePreset.ECLIPSE, (float) RADIUS, ctx.castTicks + 10);
        Fx.circle(level, ctx.target.add(0, 6, 0), new Vector3f(0, 1, 0), RuneCirclePreset.ECLIPSE, 5F, ctx.castTicks + 10);
        Fx.implosion(level, ctx.target, (float) RADIUS * 2, Fx.VIOLET, 20);
        caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, ctx.castTicks + 20, 1, false, false));
        level.playSound(null, caster.blockPosition(), ModSounds.GRAVITY.get(), SoundSource.PLAYERS, 1.5F, 0.6F);
    }

    @Override
    public boolean onCastTick(ServerPlayer caster, CastContext ctx, int tick) {
        ServerLevel level = caster.serverLevel();
        if (tick % 3 == 0) {
            Fx.particles(level, Kind.SMOKE, ctx.target.add(0, 1.5, 0), 6, RADIUS * 0.4, 0.02, 0x1A0830, 1.4F, 40);
        }
        if (tick < ctx.castTicks) {
            if (tick % 6 == 0) {
                var enemies = level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(RADIUS),
                        e -> e != caster && e.isAlive() && !e.isAlliedTo(caster));
                if (!enemies.isEmpty()) {
                    LivingEntity e = enemies.get(caster.getRandom().nextInt(enemies.size()));
                    e.invulnerableTime = 0;
                    e.hurt(level.damageSources().indirectMagic(caster, caster), 5F * ctx.power);
                    Vec3 mid = e.position().add(0, e.getBbHeight() * 0.5, 0);
                    Fx.slash(level, mid, mid.subtract(caster.position()), caster.getRandom().nextFloat() * 180F, 2.2F,
                            tick % 12 == 0 ? Fx.PINK : 0xFFFFFF, 8);
                    level.playSound(null, e.blockPosition(), ModSounds.SLASH.get(), SoundSource.PLAYERS, 0.8F, 1.3F);
                }
            }
            return true;
        }
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(RADIUS),
                e -> e != caster && e.isAlive() && !e.isAlliedTo(caster))) {
            e.invulnerableTime = 0;
            e.hurt(level.damageSources().indirectMagic(caster, caster), 15F * ctx.power);
        }
        Fx.flare(level, ctx.target.add(0, 2, 0), 9F, 0xFFFFFF, 18);
        Fx.shockwave(level, ctx.target, (float) RADIUS * 2.2F, Fx.PINK, 22);
        Fx.pillar(level, ctx.target, 30F, 2.2F, 0xE07CFF, 24);
        Fx.particles(level, Kind.SHARD, ctx.target.add(0, 1.5, 0), 60, 1.0, 0.7, 0xE07CFF, 0.22F, 40);
        Fx.shake(level, ctx.target, 0.9F, 30, 40);
        level.playSound(null, caster.blockPosition(), ModSounds.JUDGEMENT_IMPACT.get(), SoundSource.PLAYERS, 3F, 0.8F);
        return false;
    }
}
