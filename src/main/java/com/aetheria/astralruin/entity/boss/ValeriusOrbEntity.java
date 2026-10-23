package com.aetheria.astralruin.entity.boss;

import com.aetheria.astralruin.particle.AstralParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/** Orbe de soutien : soigne Valerius tant qu'il vit, declenche le Jugement Absolu s'il survit 30 s. */
public class ValeriusOrbEntity extends Mob {
    public ValeriusOrbEntity(EntityType<? extends ValeriusOrbEntity> type, Level level) {
        super(type, level);
        setNoAi(true);
        setNoGravity(true);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 40).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            float a = tickCount * 0.25F;
            for (int i = 0; i < 2; i++) {
                float b = a + i * (float) Math.PI;
                level().addParticle(AstralParticleOptions.rgb(0xC08CFF, 0.12F, 12),
                        getX() + Mth.cos(b) * 0.8, getY() + 0.45 + Mth.sin(a * 0.5F) * 0.3, getZ() + Mth.sin(b) * 0.8,
                        0, 0.01, 0);
            }
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }
}
