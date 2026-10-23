package com.aetheria.astralruin.entity.spell;

import com.aetheria.astralruin.particle.AstralParticleOptions;
import com.aetheria.astralruin.registry.ModEntities;
import com.aetheria.astralruin.registry.ModItems;
import com.aetheria.astralruin.registry.ModSounds;
import com.aetheria.astralruin.util.Fx;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/** Cristal de mana lance par Runework Nova : explose a l'impact sans casser de blocs. */
public class ManaCrystalEntity extends ThrowableItemProjectile {
    private static final double RADIUS = 2.0;
    private float damage = 5F;

    public ManaCrystalEntity(EntityType<? extends ManaCrystalEntity> type, Level level) {
        super(type, level);
    }

    public ManaCrystalEntity(Level level, LivingEntity owner, float damage) {
        super(ModEntities.MANA_CRYSTAL.get(), owner, level);
        this.damage = damage;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.MANA_CRYSTAL.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            level().addParticle(AstralParticleOptions.rgb(0xB98CFF, 0.10F, 10), getX(), getY() + 0.1, getZ(), 0, 0, 0);
        } else if (tickCount > 100) {
            discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) {
            explode((ServerLevel) level());
            discard();
        }
    }

    private void explode(ServerLevel level) {
        Entity owner = getOwner();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(RADIUS), LivingEntity::isAlive)) {
            if (owner != null && (e == owner || e.isAlliedTo(owner))) continue;
            if (e.distanceToSqr(this) > RADIUS * RADIUS * 1.5) continue;
            e.hurt(damageSources().indirectMagic(this, owner), damage);
        }
        Fx.burst(level, position(), 0xD9B8FF, 18, 0.25F);
        Fx.ring(level, position().add(0, 0.1, 0), RADIUS, Fx.VIOLET, 16);
        level.playSound(null, getX(), getY(), getZ(), ModSounds.RUNE_EXPLODE.get(), SoundSource.PLAYERS, 0.7F, 1.4F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("damage", damage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        damage = tag.getFloat("damage");
    }
}
