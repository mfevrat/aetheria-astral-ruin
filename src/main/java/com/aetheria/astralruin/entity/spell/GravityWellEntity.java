package com.aetheria.astralruin.entity.spell;

import com.aetheria.astralruin.particle.AstralParticleOptions;
import com.aetheria.astralruin.particle.AstralParticleOptions.Kind;
import com.aetheria.astralruin.particle.RuneCirclePreset;
import com.aetheria.astralruin.registry.ModEntities;
import com.aetheria.astralruin.registry.ModSounds;
import com.aetheria.astralruin.util.Fx;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Puits gravitationnel (sort Sceau de Gravite et attaque de Valerius) : aspire puis explose. */
public class GravityWellEntity extends Entity {
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(GravityWellEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFE = SynchedEntityData.defineId(GravityWellEntity.class, EntityDataSerializers.INT);
    @Nullable private UUID ownerId;
    private float damage = 14F;

    public GravityWellEntity(EntityType<? extends GravityWellEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static GravityWellEntity create(ServerLevel level, Vec3 pos, @Nullable LivingEntity owner, float radius,
                                           int life, float damage) {
        GravityWellEntity e = new GravityWellEntity(ModEntities.GRAVITY_WELL.get(), level);
        e.setPos(pos.x, pos.y + 1.5, pos.z);
        e.entityData.set(RADIUS, radius);
        e.entityData.set(LIFE, life);
        e.ownerId = owner == null ? null : owner.getUUID();
        e.damage = damage;
        Fx.groundCircle(level, pos, RuneCirclePreset.ECLIPSE, radius, life);
        level.playSound(null, pos.x, pos.y, pos.z, ModSounds.GRAVITY.get(), SoundSource.HOSTILE, 2F, 1F);
        return e;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(RADIUS, 8F);
        entityData.define(LIFE, 50);
    }

    public float radius() { return entityData.get(RADIUS); }
    public int maxLife() { return entityData.get(LIFE); }

    @Override
    public void tick() {
        super.tick();
        float radius = radius();
        if (level().isClientSide()) {
            for (int i = 0; i < 6; i++) {
                float a = random.nextFloat() * (float) (Math.PI * 2);
                double d = radius * (0.5 + random.nextDouble() * 0.5);
                double px = getX() + Mth.cos(a) * d, pz = getZ() + Mth.sin(a) * d, py = getY() - 1.2 + random.nextDouble() * 2.5;
                level().addParticle(AstralParticleOptions.of(i % 3 == 0 ? Kind.SMOKE : Kind.SPARK, i % 3 == 0 ? 0x3A1A60 : 0xC08CFF,
                        i % 3 == 0 ? 0.6F : 0.12F, 18), px, py, pz, (getX() - px) * 0.08, (getY() - py) * 0.08, (getZ() - pz) * 0.08);
            }
            return;
        }
        ServerLevel level = (ServerLevel) level();
        Entity owner = ownerId == null ? null : level.getEntity(ownerId);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius), LivingEntity::isAlive)) {
            if (owner != null && (e == owner || e.isAlliedTo(owner))) continue;
            Vec3 to = position().subtract(e.position());
            double dist = to.length();
            if (dist > radius || dist < 0.3) continue;
            double pull = 0.12 * (1.0 - dist / radius) + 0.04;
            e.setDeltaMovement(e.getDeltaMovement().add(to.normalize().scale(pull)));
            e.hurtMarked = true;
            if (tickCount % 10 == 0) {
                e.invulnerableTime = 0;
                e.hurt(damageSources().indirectMagic(this, owner), 1.5F);
            }
        }
        if (tickCount % 10 == 0) Fx.implosion(level, position().add(0, -1.4, 0), radius, 0x9B5CFF, 12);
        if (tickCount >= maxLife()) {
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius * 0.7), LivingEntity::isAlive)) {
                if (owner != null && (e == owner || e.isAlliedTo(owner))) continue;
                e.invulnerableTime = 0;
                e.hurt(damageSources().indirectMagic(this, owner), damage);
                Vec3 away = e.position().subtract(position()).multiply(1, 0, 1);
                if (away.lengthSqr() > 1e-4) e.knockback(1.6, -away.normalize().x, -away.normalize().z);
            }
            Fx.flare(level, position(), radius * 0.9F, 0xE6D0FF, 16);
            Fx.shockwave(level, position().add(0, -1.4, 0), radius * 1.6F, 0xB98CFF, 18);
            Fx.particles(level, Kind.SHARD, position(), 40, 0.5, 0.6, 0xC08CFF, 0.2F, 30);
            Fx.shake(level, position(), 0.7F, 20, 32);
            level.playSound(null, getX(), getY(), getZ(), ModSounds.JUDGEMENT_IMPACT.get(), SoundSource.HOSTILE, 2.5F, 1.3F);
            discard();
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) { return d < 160 * 160; }

    @Override
    public boolean shouldBeSaved() { return false; }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
