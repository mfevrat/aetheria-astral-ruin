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

/** Runique : enferme la cible visee dans une cage de glyphes 3 s, puis l'ecrase. */
public class GlyphPrisonSpell extends AbstractSpell {
    public GlyphPrisonSpell() {
        super("glyph_prison", 40, 8, 240, Focus.STAFF_OR_BLADE);
    }

    @Override
    public @Nullable String precheck(ServerPlayer caster) {
        return SpellTargeting.entity(caster, 24) == null ? "message.aetheria.no_target" : null;
    }

    @Override
    public boolean onCastTick(ServerPlayer caster, CastContext ctx, int tick) {
        if (tick < ctx.castTicks) return true;
        LivingEntity target = SpellTargeting.entity(caster, 24);
        if (target == null) return false;
        ServerLevel level = caster.serverLevel();
        level.addFreshEntity(GlyphPrisonEntity.create(level, caster, target, 8F * ctx.power));
        Fx.beam(level, caster.getEyePosition().add(0, -0.3, 0), target.position().add(0, target.getBbHeight() * 0.5, 0), 0xB98CFF, 0.4F, 10);
        return false;
    }
}
