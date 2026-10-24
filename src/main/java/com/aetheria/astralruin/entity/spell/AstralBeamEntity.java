package com.aetheria.astralruin.entity.spell;

import com.aetheria.astralruin.config.AetheriaConfig;
import com.aetheria.astralruin.particle.AstralParticleOptions;
import com.aetheria.astralruin.registry.ModEntities;
import com.aetheria.astralruin.registry.ModTags;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/**
 * Pilier de lumiere vertical. Sert au sort Astral Judgement (degats + desintegration)
 * et, avec damage = 0, aux rayons decoratifs de la transition de Valerius.
 */
public class AstralBeamEntity extends Entity {
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(AstralBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BEAM_HEIGHT = SynchedEntityData.defineId(AstralBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(AstralBeamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_LIFE = SynchedEntityData.defineId(AstralBeamEntity.class, EntityDataSerializers.INT);

    @Nullable private UUID ownerId;
    private float damage;
    private boolean breakBlocks;
    private boolean queueBuilt;
    private final Deque<BlockPos> breakQueue = new ArrayDeque<>();

    public AstralBeamEntity(EntityType<? extends AstralBeamEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static AstralBeamEntity create(ServerLevel level, Vec3 pos, @Nullable LivingEntity owner, float radius,
                                          float height, int life, float damage, int color, boolean breakBlocks) {
        AstralBeamEntity beam = new AstralBeamEntity(ModEntities.ASTRAL_BEAM.get(), level);
        beam.setPos(pos.x, pos.y, pos.z);
        beam.entityData.set(RADIUS, radius);
        beam.entityData.set(BEAM_HEIGHT, height);
        beam.entityData.set(COLOR, color);
        beam.entityData.set(MAX_LIFE, life);
        beam.ownerId = owner == null ? null : owner.getUUID();
        beam.damage = damage;
        beam.breakBlocks = breakBlocks;
        return beam;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(RADIUS, 2.5F);
        entityData.define(BEAM_HEIGHT, 25F);
        entityData.define(COLOR, 0xFFFFFF);
        entityData.define(MAX_LIFE, 100);
    }

    public float getRadius() { return entityData.get(RADIUS); }
    public float getBeamHeight() { return entityData.get(BEAM_HEIGHT); }
    public int getColor() { return entityData.get(COLOR); }
    public int getMaxLife() { return entityData.get(MAX_LIFE); }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            clientEffects();
            return;
        }
        if (tickCount >= getMaxLife()) {
            discard();
            return;
        }
        ServerLevel level = (ServerLevel) level();
        if (damage > 0 && tickCount % 10 == 1) hurtInside(level);
        if (breakBlocks && tickCount > 2) breakSome(level);
        // Eclairs qui frappent autour du pilier et pluie de glyphes (pilier offensif uniquement)
        if (damage > 0 && tickCount % 8 == 4) {
            double a = random.nextDouble() * Math.PI * 2, d = getRadius() * (1.5 + random.nextDouble() * 1.5);
            Vec3 strike = position().add(Math.cos(a) * d, 0, Math.sin(a) * d);
            com.aetheria.astralruin.util.Fx.lightning(level, position().add(0, getBeamHeight(), 0), strike, 0xFFF4D0, 0.4F, 8);
            com.aetheria.astralruin.util.Fx.impact(level, strike, 1.2F, 0xFFE9A8);
        }
        if (damage > 0 && tickCount % 4 == 0) {
            com.aetheria.astralruin.util.Fx.particles(level, com.aetheria.astralruin.particle.AstralParticleOptions.Kind.GLYPH,
                    position().add(0, getBeamHeight() * 0.6, 0), 3, getRadius() * 1.5, 0.0, 0xFFD36B, 0.3F, 40);
        }
    }

    private void hurtInside(ServerLevel level) {
        Entity owner = ownerId == null ? null : level.getEntity(ownerId);
        float r = getRadius();
        AABB box = new AABB(getX() - r, getY() - 1, getZ() - r, getX() + r, getY() + getBeamHeight(), getZ() + r);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (owner != null && (e == owner || e.isAlliedTo(owner))) continue;
            double dx = e.getX() - getX(), dz = e.getZ() - getZ();
            if (dx * dx + dz * dz > r * r) continue;
            e.invulnerableTime = 0;
            e.hurt(damageSources().indirectMagic(this, owner), damage);
        }
    }

    private void breakSome(ServerLevel level) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            breakBlocks = false;
            return;
        }
        if (!queueBuilt) {
            queueBuilt = true;
            BlockPos center = blockPosition();
            int r = Mth.ceil(getRadius());
            for (int dy = 2; dy >= -3; dy--) {
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (dx * dx + dz * dz <= getRadius() * getRadius()) breakQueue.add(center.offset(dx, dy, dz));
                    }
                }
            }
        }
        int limit = AetheriaConfig.JUDGEMENT_BLOCKS_PER_TICK.get();
        int destroyed = 0;
        while (destroyed < limit && !breakQueue.isEmpty()) {
            BlockPos pos = breakQueue.poll();
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;
            float hardness = state.getDestroySpeed(level, pos);
            if (hardness < 0 || hardness > 1.5F || state.is(ModTags.JUDGEMENT_IMMUNE) || state.hasBlockEntity()) continue;
            level.destroyBlock(pos, false);
            destroyed++;
        }
    }

    private void clientEffects() {
        float r = getRadius();
        float h = getBeamHeight();
        int color = getColor();
        for (int i = 0; i < 6; i++) {
            float a = random.nextFloat() * (float) (Math.PI * 2);
            float rr = r * Mth.sqrt(random.nextFloat());
            level().addParticle(AstralParticleOptions.rgb(color, 0.14F, 20), true,
                    getX() + Mth.cos(a) * rr, getY() + random.nextFloat() * h, getZ() + Mth.sin(a) * rr,
                    0, 0.12, 0);
        }
        if (damage == 0 && tickCount % 2 != 0) return;
        for (int i = 0; i < 8; i++) {
            float a = random.nextFloat() * (float) (Math.PI * 2);
            level().addParticle(AstralParticleOptions.rgb(0xFFD36B, 0.12F, 12), true,
                    getX() + Mth.cos(a) * r, getY() + 0.1, getZ() + Mth.sin(a) * r,
                    Mth.cos(a) * 0.15, 0.02, Mth.sin(a) * 0.15);
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 256 * 256;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isPushable() {
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
