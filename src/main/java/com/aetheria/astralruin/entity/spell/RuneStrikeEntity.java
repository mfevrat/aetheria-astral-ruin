package com.aetheria.astralruin.entity.spell;

import com.aetheria.astralruin.entity.boss.ValeriusOrbEntity;
import com.aetheria.astralruin.particle.AstralParticleOptions;
import com.aetheria.astralruin.particle.RuneCirclePreset;
import com.aetheria.astralruin.registry.ModEntities;
import com.aetheria.astralruin.registry.ModSounds;
import com.aetheria.astralruin.util.Fx;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Zone telegraphiee qui explose apres un delai : pieges runiques (phase 1) et meteores (phase 2). */
public class RuneStrikeEntity extends Entity {
    public enum Kind { TRAP, METEOR }

    private Kind kind = Kind.TRAP;
    private int delay = 30;
    private float damage = 10F;
    private float radius = 2.5F;
    @Nullable private UUID ownerId;

    public RuneStrikeEntity(EntityType<? extends RuneStrikeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static RuneStrikeEntity create(ServerLevel level, Vec3 pos, Kind kind, int delay, float damage,
                                          float radius, @Nullable Entity owner) {
        RuneStrikeEntity e = new RuneStrikeEntity(ModEntities.RUNE_STRIKE.get(), level);
        e.setPos(pos.x, pos.y, pos.z);
        e.kind = kind;
        e.delay = delay;
        e.damage = damage;
        e.radius = radius;
        e.ownerId = owner == null ? null : owner.getUUID();
        return e;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        ServerLevel level = (ServerLevel) level();

        if (tickCount == 1) {
            Fx.groundCircle(level, position(), kind == Kind.TRAP ? RuneCirclePreset.TRAP : RuneCirclePreset.METEOR,
                    radius, delay + 2);
        }
        if (kind == Kind.METEOR && tickCount > delay - 12) {
            double h = (delay - tickCount) * 2.5;
            level.sendParticles(AstralParticleOptions.rgb(0xFFB347, 0.4F, 6),
                    getX(), getY() + h, getZ(), 6, 0.3, 0.3, 0.3, 0.02);
        }
        if (tickCount >= delay) {
            explode(level);
            discard();
        }
    }

    private void explode(ServerLevel level) {
        Entity owner = ownerId == null ? null : level.getEntity(ownerId);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius, 2, radius),
                LivingEntity::isAlive)) {
            if (e == owner || e instanceof ValeriusOrbEntity) continue;
            double dx = e.getX() - getX(), dz = e.getZ() - getZ();
            if (dx * dx + dz * dz > radius * radius) continue;
            e.hurt(damageSources().indirectMagic(this, owner), damage);
            if (kind == Kind.METEOR) e.setSecondsOnFire(3);
        }
        int color = kind == Kind.TRAP ? 0xB03CFF : 0xFFB347;
        Fx.burst(level, position().add(0, 0.3, 0), color, 30, 0.3F);
        Fx.ring(level, position().add(0, 0.1, 0), radius, color, 24);
        Fx.shake(level, position(), 0.25F, 8, 12);
        level.playSound(null, getX(), getY(), getZ(), ModSounds.RUNE_EXPLODE.get(), SoundSource.HOSTILE, 1.2F,
                kind == Kind.TRAP ? 1.0F : 0.7F);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
