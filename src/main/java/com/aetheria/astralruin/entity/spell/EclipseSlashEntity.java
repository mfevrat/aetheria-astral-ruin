package com.aetheria.astralruin.entity.spell;

import com.aetheria.astralruin.particle.AstralParticleOptions;
import com.aetheria.astralruin.registry.ModEntities;
import com.aetheria.astralruin.registry.ModItems;
import com.aetheria.astralruin.util.Fx;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/** Lame de losanges sans gravite : coup critique de la Lame d'Eclipse et sort Astral Bolt. */
public class EclipseSlashEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(EclipseSlashEntity.class, EntityDataSerializers.INT);

    private float damage = 6F;
    private int maxPierce = 2;
    private int maxLife = 20;
    private int pierced;
    private final IntSet alreadyHit = new IntOpenHashSet();

    public EclipseSlashEntity(EntityType<? extends EclipseSlashEntity> type, Level level) {
        super(type, level);
    }

    public EclipseSlashEntity(Level level, LivingEntity owner, float damage, int maxPierce, int color) {
        super(ModEntities.ECLIPSE_SLASH.get(), owner, level);
        this.damage = damage;
        this.maxPierce = maxPierce;
        this.entityData.set(COLOR, color);
    }

    public EclipseSlashEntity withMaxLife(int ticks) {
        this.maxLife = ticks;
        return this;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(COLOR, Fx.VIOLET);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.ECLIPSE_SLASH.get();
    }

    @Override
    protected float getGravity() {
        return 0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            int color = entityData.get(COLOR);
            for (int i = 0; i < 3; i++) {
                level().addParticle(AstralParticleOptions.rgb(color, 0.16F, 10),
                        getX() + (random.nextDouble() - 0.5) * 0.6, getY() + (random.nextDouble() - 0.5) * 0.3,
                        getZ() + (random.nextDouble() - 0.5) * 0.6, 0, 0, 0);
            }
        } else if (tickCount > maxLife) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide()) return;
        Entity target = result.getEntity();
        if (!(target instanceof LivingEntity living) || !alreadyHit.add(target.getId())) return;
        Entity owner = getOwner();
        if (owner != null && (target == owner || target.isAlliedTo(owner))) return;
        living.hurt(damageSources().indirectMagic(this, owner), damage);
        Fx.burst((ServerLevel) level(), target.position().add(0, target.getBbHeight() * 0.5, 0),
                entityData.get(COLOR), 12, 0.2F);
        if (++pierced >= maxPierce) discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide()) {
            Fx.burst((ServerLevel) level(), result.getLocation(), entityData.get(COLOR), 10, 0.15F);
            discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("damage", damage);
        tag.putInt("maxPierce", maxPierce);
        tag.putInt("maxLife", maxLife);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        damage = tag.getFloat("damage");
        maxPierce = tag.getInt("maxPierce");
        maxLife = tag.getInt("maxLife");
    }
}
