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

/** Runique : trois mines runiques en eventail devant le lanceur (actives 30 s). */
public class RuneMinesSpell extends AbstractSpell {
    public RuneMinesSpell() {
        super("rune_mines", 30, 0, 200, Focus.STAFF_OR_BLADE);
    }

    @Override
    public boolean onCastTick(ServerPlayer caster, CastContext ctx, int tick) {
        ServerLevel level = caster.serverLevel();
        float yaw = caster.getYRot();
        for (int i = -1; i <= 1; i++) {
            Vec3 dir = Vec3.directionFromRotation(0, yaw + i * 30F);
            Vec3 p = caster.position().add(dir.scale(4));
            var ground = level.clip(new net.minecraft.world.level.ClipContext(p.add(0, 2, 0), p.add(0, -6, 0),
                    net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, caster));
            Vec3 at = ground.getType() == net.minecraft.world.phys.HitResult.Type.MISS ? p : ground.getLocation();
            level.addFreshEntity(RuneStrikeEntity.create(level, at, RuneStrikeEntity.Kind.MINE, 600, 10F * ctx.power, 2.5F, caster));
            Fx.beam(level, caster.position().add(0, 1, 0), at.add(0, 0.2, 0), 0xB03CFF, 0.3F, 8);
        }
        return false;
    }
}
