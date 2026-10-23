package com.aetheria.astralruin.spell.impl;

import com.aetheria.astralruin.entity.spell.ManaCrystalEntity;
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
import org.joml.Vector3f;

/**
 * 3 cercles verticaux devant le lanceur, puis onde de choc en cone
 * et pluie de cristaux de mana explosifs.
 */
public class RuneworkNovaSpell extends AbstractSpell {
    private static final double RANGE = 8.0;
    private static final double CONE_COS = Math.cos(Math.toRadians(30));

    public RuneworkNovaSpell() {
        super("runework_nova", 45, 20, 240, Focus.STAFF_OR_BLADE);
    }

    @Override
    public void onCastStart(ServerPlayer caster, CastContext ctx) {
        ServerLevel level = caster.serverLevel();
        Vector3f normal = new Vector3f((float) ctx.look.x, (float) ctx.look.y, (float) ctx.look.z);
        float[] radii = {1.2F, 1.6F, 2.0F};
        for (int i = 0; i < 3; i++) {
            Vec3 c = ctx.origin.add(ctx.look.scale(1.5 * (i + 1)));
            Fx.circle(level, c, normal, RuneCirclePreset.NOVA, radii[i], ctx.castTicks + 6);
        }
    }

    @Override
    public boolean onCastTick(ServerPlayer caster, CastContext ctx, int tick) {
        if (tick < ctx.castTicks) return true;
        release(caster, ctx);
        return false;
    }

    private void release(ServerPlayer caster, CastContext ctx) {
        ServerLevel level = caster.serverLevel();
        Vec3 look = caster.getLookAngle();
        Vec3 origin = caster.position();

        // Onde de choc en cone
        AABB area = caster.getBoundingBox().inflate(RANGE);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != caster && e.isAlive())) {
            if (e.isAlliedTo(caster)) continue;
            Vec3 to = e.position().subtract(origin);
            double dist = to.length();
            if (dist > RANGE || dist < 1e-3) continue;
            if (to.normalize().dot(look) < CONE_COS) continue;
            e.hurt(level.damageSources().indirectMagic(caster, caster), 8F * ctx.power);
            e.push(look.x * 1.8, 0.4, look.z * 1.8);
            e.hurtMarked = true;
        }

        Vec3 front = caster.getEyePosition().add(look);
        Fx.circle(level, front, new Vector3f((float) look.x, (float) look.y, (float) look.z),
                RuneCirclePreset.SHOCKWAVE, 6F, 10);
        Fx.burst(level, front, Fx.PINK, 40, 0.35F);
        Fx.shake(level, origin, 0.35F, 12, 24);
        level.playSound(null, caster.blockPosition(), ModSounds.NOVA_BURST.get(), SoundSource.PLAYERS, 1.2F, 1.0F);

        // Pluie de cristaux de mana
        for (int i = 0; i < 8; i++) {
            ManaCrystalEntity crystal = new ManaCrystalEntity(level, caster, 5F * ctx.power);
            float yaw = caster.getYRot() + (caster.getRandom().nextFloat() - 0.5F) * 50F;
            float pitch = caster.getXRot() - 25F - caster.getRandom().nextFloat() * 15F;
            crystal.shootFromRotation(caster, pitch, yaw, 0F, 0.8F + caster.getRandom().nextFloat() * 0.4F, 2F);
            level.addFreshEntity(crystal);
        }
    }
}
