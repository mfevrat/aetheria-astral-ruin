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

/** Celeste : pluie de 14 etoiles filantes sur la zone visee. */
public class StarfallSpell extends AbstractSpell {
    private static final float AREA = 5F;

    public StarfallSpell() {
        super("starfall", 50, 15, 300, Focus.STAFF_OR_BLADE);
    }

    @Override
    public void onCastStart(ServerPlayer caster, CastContext ctx) {
        ServerLevel level = caster.serverLevel();
        ctx.target = SpellTargeting.ground(caster, 32);
        Fx.groundCircle(level, ctx.target, RuneCirclePreset.TELEGRAPH, AREA, ctx.castTicks + 45);
        Fx.circle(level, ctx.target.add(0, 18, 0), new Vector3f(0, 1, 0), RuneCirclePreset.JUDGEMENT, 4F, ctx.castTicks + 45);
    }

    @Override
    public boolean onCastTick(ServerPlayer caster, CastContext ctx, int tick) {
        if (tick < ctx.castTicks) return true;
        ServerLevel level = caster.serverLevel();
        for (int i = 0; i < 14; i++) {
            double a = caster.getRandom().nextDouble() * Math.PI * 2, d = Math.sqrt(caster.getRandom().nextDouble()) * AREA;
            Vec3 p = ctx.target.add(Math.cos(a) * d, 0, Math.sin(a) * d);
            level.addFreshEntity(RuneStrikeEntity.create(level, p, RuneStrikeEntity.Kind.STAR, 8 + i * 3,
                    5F * ctx.power, 1.6F, caster));
        }
        Fx.flare(level, ctx.target.add(0, 18, 0), 5F, 0xFFF4D0, 40);
        level.playSound(null, caster.blockPosition(), ModSounds.CRYSTAL_RESONANCE.get(), SoundSource.PLAYERS, 1.2F, 1.4F);
        return false;
    }
}
