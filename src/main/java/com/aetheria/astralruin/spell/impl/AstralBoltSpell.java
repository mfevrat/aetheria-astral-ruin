package com.aetheria.astralruin.spell.impl;

import com.aetheria.astralruin.entity.spell.EclipseSlashEntity;
import com.aetheria.astralruin.registry.ModSounds;
import com.aetheria.astralruin.spell.AbstractSpell;
import com.aetheria.astralruin.spell.CastContext;
import com.aetheria.astralruin.util.Fx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/** Sort de base : un eclat de losanges violets qui perce une cible. */
public class AstralBoltSpell extends AbstractSpell {
    public AstralBoltSpell() {
        super("astral_bolt", 10, 0, 15, Focus.STAFF_OR_BLADE);
    }

    @Override
    public boolean onCastTick(ServerPlayer caster, CastContext ctx, int tick) {
        ServerLevel level = caster.serverLevel();
        EclipseSlashEntity bolt = new EclipseSlashEntity(level, caster, 5F * ctx.power, 1, Fx.VIOLET);
        bolt.shootFromRotation(caster, caster.getXRot(), caster.getYRot(), 0F, 1.6F, 0.5F);
        level.addFreshEntity(bolt);
        level.playSound(null, caster.blockPosition(), ModSounds.CRYSTAL_RESONANCE.get(), SoundSource.PLAYERS, 0.6F, 1.7F);
        Vec3 muzzle = caster.getEyePosition().add(caster.getLookAngle());
        Fx.flare(level, muzzle, 0.9F, 0xC8A8FF, 6);
        Fx.particles(level, com.aetheria.astralruin.particle.AstralParticleOptions.Kind.STAR, muzzle, 5, 0.1, 0.05, 0xE0D0FF, 0.2F, 10);
        return false;
    }
}
