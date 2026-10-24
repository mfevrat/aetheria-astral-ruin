package com.aetheria.astralruin.entity.spell;

import com.aetheria.astralruin.particle.AstralParticleOptions;
import com.aetheria.astralruin.particle.AstralParticleOptions.Kind;
import com.aetheria.astralruin.registry.ModEntities;
import com.aetheria.astralruin.registry.ModItems;
import com.aetheria.astralruin.registry.ModSounds;
import com.aetheria.astralruin.util.Fx;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** Lance Comete : traverse jusqu'a 3 ennemis, explose au contact du decor et enchaine des eclairs. */
public class CometEntity extends ThrowableItemProjectile {
    private float pierceDamage = 6F;
    private float blastDamage = 10F;
    private int pierced;
    private final IntSet hit = new IntOpenHashSet();

    public CometEntity(EntityType<? extends CometEntity> type, Level level) {
        super(type, level);
    }

    public CometEntity(Level level, LivingEntity owner, float pierceDamage, float blastDamage) {
        super(ModEntities.COMET.get(), owner, level);
        this.pierceDamage = pierceDamage;
        this.blastDamage = blastDamage;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.ASTRAL_SHARD.get();
    }

    @Override
    protected float getGravity() {
        return 0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            Vec3 v = getDeltaMovement();
            for (int i = 0; i < 4; i++) {
                double k = random.nextDouble();
                level().addParticle(AstralParticleOptions.of(Kind.EMBER, i % 2 == 0 ? 0xFFB050 : 0xFFF0C0, 0.22F, 16),
                        getX() - v.x * k, getY() - v.y * k, getZ() - v.z * k,
                        (random.nextDouble() - 0.5) * 0.05, (random.nextDouble() - 0.5) * 0.05, (random.nextDouble() - 0.5) * 0.05);
            }
            level().addParticle(AstralParticleOptions.of(Kind.STAR, 0xFFFFFF, 0.35F, 6), getX(), getY(), getZ(), 0, 0, 0);
        } else if (tickCount > 80) {
            explode((ServerLevel) level());
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide() || !(result.getEntity() instanceof LivingEntity target) || !hit.add(target.getId())) return;
        Entity owner = getOwner();
        if (owner != null && (target == owner || target.isAlliedTo(owner))) return;
        target.hurt(damageSources().indirectMagic(this, owner), pierceDamage);
        target.setSecondsOnFire(3);
        Fx.flare((ServerLevel) level(), target.position().add(0, target.getBbHeight() * 0.5, 0), 1.2F, Fx.ORANGE, 8);
        if (++pierced >= 3) explode((ServerLevel) level());
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide()) explode((ServerLevel) level());
    }

    private void explode(ServerLevel level) {
        if (isRemoved()) return;
        Entity owner = getOwner();
        Vec3 c = position();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3), LivingEntity::isAlive)) {
            if (owner != null && (e == owner || e.isAlliedTo(owner))) continue;
            e.hurt(damageSources().indirectMagic(this, owner), blastDamage);
        }
        // Eclairs en chaine vers 3 ennemis proches
        int chained = 0;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(6), LivingEntity::isAlive)) {
            if (chained >= 3 || (owner != null && (e == owner || e.isAlliedTo(owner)))) continue;
            Fx.lightning(level, c, e.position().add(0, e.getBbHeight() * 0.6, 0), 0xFFE8B0, 0.35F, 10);
            e.hurt(damageSources().indirectMagic(this, owner), blastDamage * 0.4F);
            chained++;
        }
        Fx.impact(level, c, 3F, Fx.ORANGE);
        Fx.flare(level, c, 4F, 0xFFF0C0, 14);
        Fx.shake(level, c, 0.4F, 12, 24);
        level.playSound(null, c.x, c.y, c.z, ModSounds.THUNDER.get(), SoundSource.PLAYERS, 1.4F, 1.2F);
        discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("pierce", pierceDamage);
        tag.putFloat("blast", blastDamage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        pierceDamage = tag.getFloat("pierce");
        blastDamage = tag.getFloat("blast");
    }
}
