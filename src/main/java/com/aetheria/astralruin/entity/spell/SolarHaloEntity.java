package com.aetheria.astralruin.entity.spell;

import com.aetheria.astralruin.particle.AstralParticleOptions;
import com.aetheria.astralruin.particle.AstralParticleOptions.Kind;
import com.aetheria.astralruin.registry.ModEntities;
import com.aetheria.astralruin.util.Fx;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Halo Solaire : six soleils en orbite autour du lanceur, brulent ce qu'ils touchent. */
public class SolarHaloEntity extends Entity {
    public static final int ORBS = 6;
    public static final float ORBIT = 2.3F;
    private static final EntityDataAccessor<Integer> OWNER = SynchedEntityData.defineId(SolarHaloEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LIFE = SynchedEntityData.defineId(SolarHaloEntity.class, EntityDataSerializers.INT);
    private float damage = 3F;

    public SolarHaloEntity(EntityType<? extends SolarHaloEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static SolarHaloEntity create(ServerLevel level, LivingEntity owner, int life, float damage) {
        SolarHaloEntity e = new SolarHaloEntity(ModEntities.SOLAR_HALO.get(), level);
        e.setPos(owner.getX(), owner.getY(), owner.getZ());
        e.entityData.set(OWNER, owner.getId());
        e.entityData.set(LIFE, life);
        e.damage = damage;
        return e;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(OWNER, -1);
        entityData.define(LIFE, 200);
    }

    public @Nullable Entity owner() {
        return level().getEntity(entityData.get(OWNER));
    }

    public int maxLife() {
        return entityData.get(LIFE);
    }

    /** Angle de l'orbe i au temps t (identique client/serveur). */
    public static float orbAngle(int i, float time) {
        return time * 0.12F + i * (float) (Math.PI * 2) / ORBS;
    }

    @Override
    public void tick() {
        super.tick();
        Entity owner = owner();
        if (owner == null || !owner.isAlive() || tickCount > maxLife()) {
            if (!level().isClientSide()) discard();
            return;
        }
        setPos(owner.getX(), owner.getY(), owner.getZ());
        if (level().isClientSide()) {
            for (int i = 0; i < ORBS; i++) {
                float a = orbAngle(i, tickCount);
                level().addParticle(AstralParticleOptions.of(Kind.EMBER, 0xFFB040, 0.16F, 14),
                        getX() + Mth.cos(a) * ORBIT, getY() + 1.1, getZ() + Mth.sin(a) * ORBIT, 0, 0.02, 0);
            }
            return;
        }
        if (tickCount % 10 == 0) {
            ServerLevel level = (ServerLevel) level();
            for (int i = 0; i < ORBS; i++) {
                float a = orbAngle(i, tickCount);
                Vec3 orb = new Vec3(getX() + Mth.cos(a) * ORBIT, getY() + 1.1, getZ() + Mth.sin(a) * ORBIT);
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(orb, orb).inflate(1.2),
                        e -> e != owner && e.isAlive() && !e.isAlliedTo(owner))) {
                    e.invulnerableTime = 0;
                    e.hurt(damageSources().indirectMagic(this, owner), damage);
                    e.setSecondsOnFire(2);
                    Fx.flare(level, orb, 1.0F, Fx.ORANGE, 6);
                }
            }
        }
    }

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
