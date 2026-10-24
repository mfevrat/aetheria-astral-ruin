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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.UUID;

/** Prison de Glyphes : immobilise la cible 3 s dans une cage de runes, puis l'ecrase. */
public class GlyphPrisonEntity extends Entity {
    private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(GlyphPrisonEntity.class, EntityDataSerializers.INT);
    private static final int DURATION = 60;
    @Nullable private UUID ownerId;
    private float damage = 8F;

    public GlyphPrisonEntity(EntityType<? extends GlyphPrisonEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static GlyphPrisonEntity create(ServerLevel level, LivingEntity owner, LivingEntity target, float damage) {
        GlyphPrisonEntity e = new GlyphPrisonEntity(ModEntities.GLYPH_PRISON.get(), level);
        e.setPos(target.getX(), target.getY(), target.getZ());
        e.entityData.set(TARGET, target.getId());
        e.ownerId = owner.getUUID();
        e.damage = damage;
        Vec3 mid = target.position().add(0, target.getBbHeight() * 0.5, 0);
        float r = Math.max(1.2F, target.getBbWidth() + 0.6F);
        for (Vector3f n : new Vector3f[]{new Vector3f(1, 0, 0), new Vector3f(0, 0, 1), new Vector3f(0.7F, 0, -0.7F)}) {
            Fx.circle(level, mid, n, RuneCirclePreset.BARRIER, r, DURATION);
        }
        Fx.groundCircle(level, target.position(), RuneCirclePreset.TRAP, r + 0.6F, DURATION);
        Fx.implosion(level, target.position(), r * 3, Fx.VIOLET, 12);
        level.playSound(null, target.blockPosition(), ModSounds.CHAINS.get(), SoundSource.PLAYERS, 1F, 1.3F);
        return e;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(TARGET, -1);
    }

    @Override
    public void tick() {
        super.tick();
        Entity t = level().getEntity(entityData.get(TARGET));
        if (!(t instanceof LivingEntity target) || !target.isAlive()) {
            if (!level().isClientSide()) discard();
            return;
        }
        if (level().isClientSide()) {
            float a = tickCount * 0.3F;
            for (int i = 0; i < 3; i++) {
                float b = a + i * 2.094F;
                level().addParticle(AstralParticleOptions.of(Kind.GLYPH, i == 0 ? 0xFFD36B : 0xB98CFF, 0.2F, 20),
                        target.getX() + Mth.cos(b) * 1.1, target.getY() + (tickCount % 20) / 10.0, target.getZ() + Mth.sin(b) * 1.1, 0, 0.01, 0);
            }
            return;
        }
        // Immobilisation
        Vec3 v = target.getDeltaMovement();
        target.setDeltaMovement(0, Math.min(0, v.y), 0);
        target.hurtMarked = true;
        if (target instanceof Mob mob) mob.getNavigation().stop();
        Entity owner = ownerId == null ? null : ((ServerLevel) level()).getEntity(ownerId);
        if (tickCount % 20 == 0) {
            target.invulnerableTime = 0;
            target.hurt(damageSources().indirectMagic(this, owner), damage * 0.25F);
        }
        if (tickCount >= DURATION) {
            ServerLevel level = (ServerLevel) level();
            target.invulnerableTime = 0;
            target.hurt(damageSources().indirectMagic(this, owner), damage);
            Fx.impact(level, target.position(), 2F, Fx.VIOLET);
            Fx.pillar(level, target.position(), 6F, 0.8F, 0xC08CFF, 14);
            level.playSound(null, target.blockPosition(), ModSounds.RUNE_EXPLODE.get(), SoundSource.PLAYERS, 1.2F, 0.8F);
            discard();
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
