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

/** Eclipse : sept croissants tranchants en eventail. */
public class CrescentStormSpell extends AbstractSpell {
    public CrescentStormSpell() {
        super("crescent_storm", 35, 8, 120, Focus.STAFF_OR_BLADE);
    }

    @Override
    public boolean onCastTick(ServerPlayer caster, CastContext ctx, int tick) {
        if (tick < ctx.castTicks) return true;
        ServerLevel level = caster.serverLevel();
        for (int i = -3; i <= 3; i++) {
            EclipseSlashEntity slash = new EclipseSlashEntity(level, caster, 6F * ctx.power, 2, i % 2 == 0 ? Fx.PINK : Fx.VIOLET)
                    .withMaxLife(14);
            slash.shootFromRotation(caster, caster.getXRot(), caster.getYRot() + i * 11F, 0F, 1.5F, 0F);
            level.addFreshEntity(slash);
        }
        Vec3 front = caster.getEyePosition().add(caster.getLookAngle().scale(1.5)).add(0, -0.4, 0);
        Fx.slash(level, front, caster.getLookAngle(), 0F, 3.2F, Fx.PINK, 10);
        Fx.slash(level, front, caster.getLookAngle(), 25F, 2.6F, Fx.VIOLET, 12);
        Fx.slash(level, front, caster.getLookAngle(), -25F, 2.6F, 0xFFFFFF, 12);
        level.playSound(null, caster.blockPosition(), ModSounds.SLASH.get(), SoundSource.PLAYERS, 1.2F, 1.1F);
        return false;
    }
}
