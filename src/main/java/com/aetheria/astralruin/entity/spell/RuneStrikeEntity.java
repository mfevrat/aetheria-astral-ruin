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

/**
 * Zone telegraphiee qui frappe apres un delai :
 * TRAP (piege runique), METEOR (boule de feu), STAR (etoile filante), PILLAR (colonne de lumiere),
 * MINE (mine runique armee qui explose au passage d'un ennemi).
 */
public class RuneStrikeEntity extends Entity {
    public enum Kind { TRAP, METEOR, STAR, PILLAR, MINE }

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

    /** Pour MINE, delay = duree de vie de la mine (elle s'arme apres 20 ticks). */
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
        Entity owner = ownerId == null ? null : level.getEntity(ownerId);

        if (tickCount == 1) {
            RuneCirclePreset preset = switch (kind) {
                case TRAP, MINE -> RuneCirclePreset.TRAP;
                case METEOR -> RuneCirclePreset.METEOR;
                case STAR, PILLAR -> RuneCirclePreset.TELEGRAPH;
            };
            Fx.groundCircle(level, position(), preset, kind == Kind.MINE ? 1.2F : radius, delay + 2);
            if (kind == Kind.MINE) level.playSound(null, blockPosition(), ModSounds.MINE_ARM.get(), SoundSource.PLAYERS, 0.8F, 1.4F);
        }

        switch (kind) {
            case METEOR -> {
                int fall = 16;
                if (tickCount == delay - fall) level.playSound(null, getX(), getY(), getZ(), ModSounds.METEOR_FALL.get(), SoundSource.HOSTILE, 1.5F, 1F);
                if (tickCount > delay - fall) {
                    double h = (delay - tickCount) * 2.2;
                    Vec3 p = position().add(h * 0.35, h, h * 0.2);
                    Fx.particles(level, AstralParticleOptions.Kind.EMBER, p, 8, 0.4, 0.04, 0xFF9A3C, 0.35F, 16);
                    Fx.particles(level, AstralParticleOptions.Kind.SMOKE, p, 3, 0.3, 0.02, 0x40202A, 0.9F, 30);
                    Fx.particles(level, AstralParticleOptions.Kind.STAR, p, 1, 0.1, 0, 0xFFF0C0, 0.9F, 4);
                }
            }
            case STAR -> {
                if (tickCount > delay - 10) {
                    double h = (delay - tickCount) * 3.0;
                    Fx.particles(level, AstralParticleOptions.Kind.STAR, position().add(-h * 0.3, h, 0), 2, 0.05, 0, 0xFFF4D0, 0.5F, 6);
                    Fx.particles(level, AstralParticleOptions.Kind.EMBER, position().add(-h * 0.3, h, 0), 3, 0.1, 0.02, 0xFFD36B, 0.18F, 10);
                }
            }
            case PILLAR -> {
                if (tickCount == delay - 4) Fx.lightning(level, position().add(0, 24, 0), position(), 0xFFF4D0, 0.5F, 8);
            }
            case MINE -> {
                if (tickCount % 10 == 0) Fx.particles(level, AstralParticleOptions.Kind.GLYPH, position().add(0, 0.3, 0), 1, 0.3, 0.01, 0xB03CFF, 0.2F, 20);
                if (tickCount > 20) {
                    boolean triggered = !level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(1.6, 1.5, 1.6),
                            e -> e.isAlive() && e != owner && (owner == null || !e.isAlliedTo(owner))).isEmpty();
                    if (triggered) {
                        explode(level, owner);
                        discard();
                        return;
                    }
                }
            }
            default -> {}
        }

        if (tickCount >= delay) {
            if (kind != Kind.MINE) explode(level, owner);
            discard();
        }
    }

    private void explode(ServerLevel level, @Nullable Entity owner) {
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius, 2, radius),
                LivingEntity::isAlive)) {
            if (e == owner || e instanceof ValeriusOrbEntity || (owner != null && e.isAlliedTo(owner))) continue;
            double dx = e.getX() - getX(), dz = e.getZ() - getZ();
            if (dx * dx + dz * dz > radius * radius) continue;
            e.invulnerableTime = 0;
            e.hurt(damageSources().indirectMagic(this, owner), damage);
            if (kind == Kind.METEOR) e.setSecondsOnFire(3);
            if (kind == Kind.MINE) e.setDeltaMovement(e.getDeltaMovement().add(0, 0.6, 0));
        }
        int color = switch (kind) {
            case TRAP, MINE -> 0xB03CFF;
            case METEOR -> 0xFF8A30;
            case STAR -> 0xFFE9A8;
            case PILLAR -> 0xFFF4D0;
        };
        Fx.impact(level, position(), radius, color);
        if (kind == Kind.PILLAR) Fx.pillar(level, position(), 18F, radius * 0.6F, color, 16);
        if (kind == Kind.METEOR) {
            Fx.particles(level, AstralParticleOptions.Kind.SMOKE, position().add(0, 0.5, 0), 12, radius * 0.4, 0.05, 0x3A2030, 1.2F, 40);
            Fx.shake(level, position(), 0.4F, 10, 18);
        } else {
            Fx.shake(level, position(), 0.2F, 8, 12);
        }
        level.playSound(null, getX(), getY(), getZ(), kind == Kind.PILLAR || kind == Kind.STAR ? ModSounds.THUNDER.get()
                : ModSounds.RUNE_EXPLODE.get(), SoundSource.HOSTILE, 1.2F, kind == Kind.STAR ? 1.6F : kind == Kind.METEOR ? 0.7F : 1F);
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
