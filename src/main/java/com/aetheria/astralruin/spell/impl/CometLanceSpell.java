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

/** Celeste : comete perforante qui explose et enchaine des eclairs. */
public class CometLanceSpell extends AbstractSpell {
    public CometLanceSpell() {
        super("comet_lance", 40, 12, 160, Focus.STAFF_OR_BLADE);
    }

    @Override
    public boolean onCastTick(ServerPlayer caster, CastContext ctx, int tick) {
        ServerLevel level = caster.serverLevel();
        Vec3 hand = caster.getEyePosition().add(caster.getLookAngle().scale(1.2)).add(0, -0.3, 0);
        if (tick < ctx.castTicks) {
            if (tick % 3 == 0) Fx.particles(level, Kind.EMBER, hand, 4, 0.15, 0.03, 0xFFB050, 0.2F, 10);
            if (tick == 0) Fx.flare(level, hand, 0.8F, 0xFFC070, ctx.castTicks);
            return true;
        }
        CometEntity comet = new CometEntity(level, caster, 6F * ctx.power, 10F * ctx.power);
        comet.setPos(hand.x, hand.y, hand.z);
        comet.shootFromRotation(caster, caster.getXRot(), caster.getYRot(), 0F, 2.4F, 0F);
        level.addFreshEntity(comet);
        Fx.shockwave(level, hand.add(0, -1.2, 0), 2.5F, Fx.ORANGE, 8);
        Fx.slash(level, hand, caster.getLookAngle(), 90F, 1.4F, 0xFFC070, 6);
        level.playSound(null, caster.blockPosition(), ModSounds.NOVA_BURST.get(), SoundSource.PLAYERS, 1F, 1.5F);
        return false;
    }
}
