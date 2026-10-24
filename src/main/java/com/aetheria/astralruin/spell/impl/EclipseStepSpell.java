package com.aetheria.astralruin.spell.impl;

import com.aetheria.astralruin.particle.RuneCirclePreset;
import com.aetheria.astralruin.registry.ModSounds;
import com.aetheria.astralruin.spell.AbstractSpell;
import com.aetheria.astralruin.spell.CastContext;
import com.aetheria.astralruin.util.Fx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Branche Eclipse : bond de 8 blocs vers l'avant qui tranche tout sur son passage. */
public class EclipseStepSpell extends AbstractSpell {
    private static final double MAX_DISTANCE = 8.0;

    public EclipseStepSpell() {
        super("eclipse_step", 25, 0, 100, Focus.STAFF_OR_BLADE);
    }

    @Override
    public boolean onCastTick(ServerPlayer caster, CastContext ctx, int tick) {
        ServerLevel level = caster.serverLevel();
        Vec3 look = caster.getLookAngle();
        Vec3 dir = new Vec3(look.x, 0, look.z);
        if (dir.lengthSqr() < 1e-4) return false;
        dir = dir.normalize();

        // Avance par pas de 0,5 bloc tant que la hitbox du joueur tient sans collision
        Vec3 start = caster.position();
        Vec3 dest = start;
        for (double d = 0.5; d <= MAX_DISTANCE; d += 0.5) {
            Vec3 candidate = start.add(dir.scale(d));
            AABB box = caster.getBoundingBox().move(candidate.subtract(start));
            if (!level.noCollision(caster, box)) break;
            dest = candidate;
        }
        if (dest.distanceToSqr(start) < 1.0) return false;

        AABB sweep = new AABB(start, dest.add(0, caster.getBbHeight(), 0)).inflate(1.0);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, sweep,
                e -> e != caster && e.isAlive() && !e.isAlliedTo(caster))) {
            e.hurt(level.damageSources().indirectMagic(caster, caster), 6F * ctx.power);
        }

        Fx.groundCircle(level, start, RuneCirclePreset.NOVA, 1.2F, 14);
        Fx.line(level, start.add(0, 1, 0), dest.add(0, 1, 0), Fx.PINK, 0.22F, 0.35);
        for (double k = 0.2; k < 1.0; k += 0.27) {
            Vec3 p = start.lerp(dest, k).add(0, 1, 0);
            Fx.slash(level, p, dir, 70F + (float) k * 60F, 1.6F, Fx.PINK, 8);
            Fx.particles(level, com.aetheria.astralruin.particle.AstralParticleOptions.Kind.SMOKE, p, 4, 0.3, 0.01, 0x2A1048, 0.7F, 24);
        }
        caster.teleportTo(dest.x, dest.y, dest.z);
        caster.fallDistance = 0;
        Fx.groundCircle(level, dest, RuneCirclePreset.NOVA, 1.5F, 18);
        Fx.burst(level, dest.add(0, 1, 0), Fx.PINK, 30, 0.25F);
        level.playSound(null, dest.x, dest.y, dest.z, ModSounds.NOVA_BURST.get(), SoundSource.PLAYERS, 0.8F, 1.6F);
        return false;
    }
}
