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

/** Celeste : six soleils orbitent 10 s autour du lanceur et brulent ce qu'ils touchent. */
public class SolarHaloSpell extends AbstractSpell {
    public SolarHaloSpell() {
        super("solar_halo", 45, 0, 400, Focus.STAFF_OR_BLADE);
    }

    @Override
    public boolean onCastTick(ServerPlayer caster, CastContext ctx, int tick) {
        ServerLevel level = caster.serverLevel();
        level.addFreshEntity(SolarHaloEntity.create(level, caster, 200, 3F * ctx.power));
        Fx.shockwave(level, caster.position(), 4F, Fx.ORANGE, 12);
        Fx.flare(level, caster.position().add(0, 1.1, 0), 2.5F, 0xFFD080, 10);
        level.playSound(null, caster.blockPosition(), ModSounds.CRYSTAL_RESONANCE.get(), SoundSource.PLAYERS, 1F, 1.8F);
        return false;
    }
}
