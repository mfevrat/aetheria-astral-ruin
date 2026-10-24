package com.aetheria.astralruin.entity.spell;

import com.aetheria.astralruin.entity.boss.ValeriusOrbEntity;
import com.aetheria.astralruin.particle.AstralParticleOptions;
import com.aetheria.astralruin.particle.AstralParticleOptions.Kind;
import com.aetheria.astralruin.registry.ModEntities;
import com.aetheria.astralruin.registry.ModItems;
import com.aetheria.astralruin.registry.ModSounds;
import com.aetheria.astralruin.util.Fx;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Lame spectrale de Valerius : flotte en visant sa cible, puis fond sur elle. */
public class SpectralBladeEntity extends ThrowableItemProjectile {
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> TARGET =
            net.minecraft.network.syncher.SynchedEntityData.defineId(SpectralBladeEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.INT);
    private int hoverTicks = 20;
    private float damage = 8F;
    private boolean launched;

    public SpectralBladeEntity(EntityType<? extends SpectralBladeEntity> type, Level level) {
        super(type, level);
    }

    public SpectralBladeEntity(Level level, LivingEntity owner, Vec3 pos, @Nullable LivingEntity target, int hoverTicks, float damage) {
        super(ModEntities.SPECTRAL_BLADE.get(), owner, level);
        setPos(pos.x, pos.y, pos.z);
        this.hoverTicks = hoverTicks;
        this.entityData.set(TARGET, target == null ? -1 : target.getId());
        this.damage = damage;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TARGET, -1);
    }

    /** Cible visee pendant le vol stationnaire (lue par le renderer). */
    public @Nullable Entity aimTarget() {
        return level().getEntity(entityData.get(TARGET));
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SPECTRAL_BLADE.get();
    }

    @Override
    protected float getGravity() {
        return 0F;
    }

    @Override
    public void tick() {
        Entity target = aimTarget();
        if (!level().isClientSide() && !launched) {
            setDeltaMovement(Vec3.ZERO);
            if (tickCount >= hoverTicks) {
                launched = true;
                Vec3 aim = target != null ? target.getEyePosition().subtract(position()) : Vec3.directionFromRotation(getXRot(), getYRot());
                shoot(aim.x, aim.y, aim.z, 1.9F, 0F);
                level().playSound(null, blockPosition(), ModSounds.SLASH.get(), SoundSource.HOSTILE, 0.8F, 1.6F);
            }
        }
        super.tick();
        // la mise a jour vanilla de rotation suit la vitesse (nulle en vol stationnaire) : on re-vise apres
        if (!launched && target != null) faceTowards(target.getEyePosition());
        if (level().isClientSide()) {
            level().addParticle(AstralParticleOptions.of(Kind.STAR, 0xC8A8FF, 0.18F, 8), getX(), getY(), getZ(), 0, 0, 0);
        } else if (tickCount > hoverTicks + 60) {
            discard();
        }
    }

    private void faceTowards(Vec3 point) {
        Vec3 d = point.subtract(position());
        double h = Math.sqrt(d.x * d.x + d.z * d.z);
        setYRot((float) (Mth.atan2(d.x, d.z) * (180F / Math.PI)));
        setXRot((float) (Mth.atan2(d.y, h) * (180F / Math.PI)));
        yRotO = getYRot();
        xRotO = getXRot();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide() || !launched) return;
        Entity e = result.getEntity();
        Entity owner = getOwner();
        if (e == owner || e instanceof ValeriusOrbEntity || e instanceof SpectralBladeEntity) return;
        e.hurt(damageSources().indirectMagic(this, owner), damage);
        Fx.slash((ServerLevel) level(), e.position().add(0, e.getBbHeight() * 0.5, 0), getDeltaMovement(),
                random.nextFloat() * 180F, 1.6F, 0xC8A8FF, 8);
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level().isClientSide() || !launched) return;
        Fx.particles((ServerLevel) level(), Kind.SHARD, result.getLocation(), 14, 0.2, 0.3, 0xC8A8FF, 0.16F, 26);
        discard();
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
        launched = true;
    }
}
