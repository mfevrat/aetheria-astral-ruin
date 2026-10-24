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

/** Eclipse : des chaines du neant attrapent la cible et la ramenent au lanceur. */
public class VoidChainsSpell extends AbstractSpell {
    public VoidChainsSpell() {
        super("void_chains", 30, 0, 160, Focus.STAFF_OR_BLADE);
    }

    @Override
    public @Nullable String precheck(ServerPlayer caster) {
        return SpellTargeting.entity(caster, 20) == null ? "message.aetheria.no_target" : null;
    }

    @Override
    public boolean onCastTick(ServerPlayer caster, CastContext ctx, int tick) {
        LivingEntity target = SpellTargeting.entity(caster, 20);
        if (target == null) return false;
        ServerLevel level = caster.serverLevel();
        Vec3 pull = caster.position().subtract(target.position());
        double dist = pull.length();
        if (dist > 1.5) {
            Vec3 v = pull.normalize().scale(Math.min(2.2, 0.35 + dist * 0.12));
            target.setDeltaMovement(v.x, 0.35 + v.y * 0.3, v.z);
            target.hurtMarked = true;
        }
        target.hurt(level.damageSources().indirectMagic(caster, caster), 6F * ctx.power);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
        Fx.tether(level, caster, target, 0xB98CFF, 0.5F, 16);
        Fx.particles(level, Kind.SMOKE, target.position().add(0, 1, 0), 10, 0.4, 0.02, 0x2A1048, 0.8F, 30);
        level.playSound(null, caster.blockPosition(), ModSounds.CHAINS.get(), SoundSource.PLAYERS, 1.2F, 0.9F);
        return false;
    }
}
