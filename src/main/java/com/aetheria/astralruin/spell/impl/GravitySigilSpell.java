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

/** Runique (ultime) : trou noir qui aspire les ennemis pendant 2,5 s puis explose. */
public class GravitySigilSpell extends AbstractSpell {
    public GravitySigilSpell() {
        super("gravity_sigil", 70, 20, 600, Focus.STAFF);
    }

    @Override
    public void onCastStart(ServerPlayer caster, CastContext ctx) {
        ctx.target = SpellTargeting.ground(caster, 24);
        Fx.groundCircle(caster.serverLevel(), ctx.target, RuneCirclePreset.TELEGRAPH, 3F, ctx.castTicks);
    }

    @Override
    public boolean onCastTick(ServerPlayer caster, CastContext ctx, int tick) {
        if (tick < ctx.castTicks) return true;
        ServerLevel level = caster.serverLevel();
        level.addFreshEntity(GravityWellEntity.create(level, ctx.target, caster, 8F, 50, 14F * ctx.power));
        Fx.shake(level, ctx.target, 0.3F, 50, 24);
        return false;
    }
}
