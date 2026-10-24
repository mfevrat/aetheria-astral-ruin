package com.aetheria.astralruin.spell;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;

/** Visee commune aux sorts : entite sous le reticule ou point au sol. */
public final class SpellTargeting {

    public static @Nullable LivingEntity entity(ServerPlayer caster, double range) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        Vec3 end = eye.add(look.scale(range));
        BlockHitResult block = caster.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        if (block.getType() != HitResult.Type.MISS) end = block.getLocation();
        AABB box = caster.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(caster, eye, end, box,
                e -> e instanceof LivingEntity && e.isAlive() && e != caster && !e.isAlliedTo(caster) && !e.isSpectator(),
                range * range);
        if (hit == null) return null;
        Entity e = hit.getEntity();
        return e instanceof LivingEntity living ? living : null;
    }

    /** Point vise, ramene au sol s'il est dans le vide. */
    public static Vec3 ground(ServerPlayer caster, double range) {
        HitResult hit = caster.pick(range, 0F, false);
        Vec3 point = hit.getLocation();
        BlockHitResult down = caster.level().clip(new ClipContext(point.add(0, 0.5, 0), point.add(0, -40, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        return down.getType() == HitResult.Type.MISS ? point : down.getLocation();
    }

    private SpellTargeting() {}
}
