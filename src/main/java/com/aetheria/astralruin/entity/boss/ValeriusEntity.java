package com.aetheria.astralruin.entity.boss;

import com.aetheria.astralruin.config.AetheriaConfig;
import com.aetheria.astralruin.entity.spell.AstralBeamEntity;
import com.aetheria.astralruin.entity.spell.RuneStrikeEntity;
import com.aetheria.astralruin.mana.ManaCapability;
import com.aetheria.astralruin.particle.AstralParticleOptions;
import com.aetheria.astralruin.particle.RuneCirclePreset;
import com.aetheria.astralruin.registry.ModEntities;
import com.aetheria.astralruin.registry.ModItems;
import com.aetheria.astralruin.registry.ModSounds;
import com.aetheria.astralruin.util.Fx;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Valerius, le Gardien des Runes.
 * Phase 1 : guerrier (combo d'epee, pieges runiques, charge).
 * Transition a 50 % : 5 s d'invulnerabilite dans un cocon de lumiere.
 * Phase 2 : forme liberee (meteores, faisceau continu, orbes de soutien).
 */
public class ValeriusEntity extends Monster implements GeoEntity {
    // --- Animations (noms identiques a valerius.animation.json) ---
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.valerius.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.valerius.walk");
    private static final RawAnimation HOVER = RawAnimation.begin().thenLoop("animation.valerius.hover");
    private static final RawAnimation CAST = RawAnimation.begin().thenPlay("animation.valerius.cast");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.valerius.attack");
    private static final RawAnimation TRANSITION = RawAnimation.begin().thenPlayAndHold("animation.valerius.phase_transition");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("animation.valerius.death");

    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(ValeriusEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> TRANSITIONING = SynchedEntityData.defineId(ValeriusEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int DEATH_TICKS = 60;

    private enum Attack {
        SWORD_COMBO, RUNE_TRAP, ECLIPSE_CHARGE, METEORS, ENERGY_BEAM;

        String key() { return name().toLowerCase(java.util.Locale.ROOT); }
    }
    private enum State { IDLE, TELEGRAPH, EXECUTE, RECOVER }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossBar = new ServerBossEvent(Component.translatable("entity.aetheria.valerius"),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);

    @Nullable private BlockPos anchor;
    private State state = State.IDLE;
    @Nullable private Attack attack;
    private int stateTicks;
    private int globalCooldown = 40;
    private final Map<Attack, Integer> cooldowns = new EnumMap<>(Attack.class);
    private int transitionTicks;
    private int orbWave;
    private int orbDeadline;
    private Vec3 chargeDir = Vec3.ZERO;
    private Vec3 beamDir = Vec3.ZERO;
    private final List<Integer> chargeHits = new ArrayList<>();

    public ValeriusEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 500;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 600)
                .add(Attributes.ARMOR, 12)
                .add(Attributes.ARMOR_TOUGHNESS, 4)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
                .add(Attributes.FOLLOW_RANGE, 48)
                .add(Attributes.ATTACK_DAMAGE, 14)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5);
    }

    /** Appele par l'autel apres invocation. */
    public void setAnchor(BlockPos pos) {
        this.anchor = pos.immutable();
    }

    /** Applique valerius.json (PV, armure, vitesse, degats) et le multiplicateur de config. */
    public void applyDefinition() {
        BossDefinition def = BossDefinition.valerius();
        setBase(Attributes.MAX_HEALTH, def.maxHealth * AetheriaConfig.BOSS_HEALTH_MULTIPLIER.get());
        setBase(Attributes.ARMOR, def.armor);
        setBase(Attributes.ARMOR_TOUGHNESS, def.armorToughness);
        setBase(Attributes.MOVEMENT_SPEED, def.movementSpeed);
        setBase(Attributes.ATTACK_DAMAGE, def.attack("sword_combo").damage());
        setHealth(getMaxHealth());
    }

    private void setBase(net.minecraft.world.entity.ai.attributes.Attribute attribute, double value) {
        var inst = getAttribute(attribute);
        if (inst != null) inst.setBaseValue(value);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        applyDefinition();
        return super.finalizeSpawn(level, difficulty, reason, data, tag);
    }

    private static BossDefinition.AttackDef def(Attack a) {
        return BossDefinition.valerius().attack(a.key());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(PHASE, 1);
        entityData.define(TRANSITIONING, false);
    }

    public int getPhase() { return entityData.get(PHASE); }
    public boolean isTransitioning() { return entityData.get(TRANSITIONING); }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 24F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    // =====================================================================
    //  IA : machine a etats IDLE -> TELEGRAPH -> EXECUTE -> RECOVER
    // =====================================================================
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        ServerLevel level = (ServerLevel) level();
        bossBar.setProgress(getHealth() / getMaxHealth());
        if (anchor == null) anchor = blockPosition();

        if (getPhase() == 1 && getHealth() < getMaxHealth() * BossDefinition.valerius().phase2Threshold) startTransition(level);
        if (isTransitioning()) {
            tickTransition(level);
            return;
        }

        cooldowns.replaceAll((k, v) -> Math.max(0, v - 1));
        if (globalCooldown > 0) globalCooldown--;

        if (getPhase() == 2) {
            hover();
            tickOrbs(level);
        }

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) {
            if (state != State.IDLE) setState(State.IDLE);
            return;
        }
        getLookControl().setLookAt(target, 30F, 30F);
        stateTicks++;

        switch (state) {
            case IDLE -> {
                if (getPhase() == 1 && distanceTo(target) > 3.2F && stateTicks % 10 == 1) getNavigation().moveTo(target, 1.0);
                if (globalCooldown == 0) chooseAttack(target);
            }
            case TELEGRAPH -> {
                getNavigation().stop();
                if (stateTicks >= telegraphTicks(attack)) {
                    setState(State.EXECUTE);
                    beginExecute(level, target);
                }
            }
            case EXECUTE -> {
                if (!tickExecute(level, target)) setState(State.RECOVER);
            }
            case RECOVER -> {
                if (stateTicks >= 20) {
                    setState(State.IDLE);
                    globalCooldown = getPhase() == 1 ? 20 : 15;
                }
            }
        }
    }

    private void setState(State s) {
        state = s;
        stateTicks = 0;
    }

    private void chooseAttack(LivingEntity target) {
        double d = distanceTo(target);
        List<Attack> pool = new ArrayList<>();
        Attack[] options = getPhase() == 1
                ? new Attack[]{Attack.SWORD_COMBO, Attack.RUNE_TRAP, Attack.ECLIPSE_CHARGE}
                : new Attack[]{Attack.METEORS, Attack.ENERGY_BEAM};
        for (Attack a : options) {
            BossDefinition.AttackDef ad = def(a);
            if (ready(a) && d >= ad.minRange() && d <= ad.maxRange()) addWeighted(pool, a, ad.weight());
        }
        if (pool.isEmpty()) return;
        attack = pool.get(random.nextInt(pool.size()));
        setState(State.TELEGRAPH);
        if (attack == Attack.SWORD_COMBO || attack == Attack.ECLIPSE_CHARGE) triggerAnim("main", "attack");
        else triggerAnim("main", "cast");
        level().playSound(null, blockPosition(), ModSounds.CAST_CHARGE.get(), SoundSource.HOSTILE, 1.5F, 0.7F);
    }

    private static void addWeighted(List<Attack> pool, Attack a, int weight) {
        for (int i = 0; i < weight; i++) pool.add(a);
    }

    private boolean ready(Attack a) {
        return cooldowns.getOrDefault(a, 0) == 0;
    }

    private static int telegraphTicks(@Nullable Attack a) {
        return a == null ? 0 : def(a).telegraph();
    }

    private void beginExecute(ServerLevel level, LivingEntity target) {
        if (attack == null) return;
        BossDefinition.AttackDef ad = def(attack);
        cooldowns.put(attack, ad.cooldown());
        switch (attack) {
            case SWORD_COMBO -> {}
            case RUNE_TRAP -> {
                Vec3 predicted = target.position().add(target.getDeltaMovement().multiply(20, 0, 20));
                for (int i = 0; i < ad.count(); i++) {
                    Vec3 p = i == 0 ? target.position() : i == 1 ? predicted
                            : target.position().add(random.nextGaussian() * 3, 0, random.nextGaussian() * 3);
                    spawnStrike(level, p, RuneStrikeEntity.Kind.TRAP, ad.duration(), ad.damage(), ad.radius());
                }
            }
            case ECLIPSE_CHARGE -> {
                Vec3 to = target.position().subtract(position());
                chargeDir = new Vec3(to.x, 0, to.z).normalize();
                chargeHits.clear();
            }
            case METEORS -> {
                spawnStrike(level, target.position(), RuneStrikeEntity.Kind.METEOR, ad.duration(), ad.damage(), ad.radius());
                for (int i = 1; i < ad.count(); i++) {
                    Vec3 p = target.position().add((random.nextDouble() - 0.5) * 14, 0, (random.nextDouble() - 0.5) * 14);
                    spawnStrike(level, p, RuneStrikeEntity.Kind.METEOR, ad.duration() + i * 2, ad.damage(), ad.radius());
                }
            }
            case ENERGY_BEAM -> {
                beamDir = target.getEyePosition().subtract(getEyePosition()).normalize();
            }
        }
    }

    /** @return true tant que l'attaque continue. */
    private boolean tickExecute(ServerLevel level, LivingEntity target) {
        if (attack == null) return false;
        int t = stateTicks;
        BossDefinition.AttackDef ad = def(attack);
        switch (attack) {
            case SWORD_COMBO -> {
                if (t == 1 || t == 9 || t == 17) {
                    swing(InteractionHand.MAIN_HAND);
                    if (t > 1) triggerAnim("main", "attack");
                    if (distanceTo(target) < 4.8F) {
                        doHurtTarget(target);
                        if (t == 17) target.knockback(1.2, getX() - target.getX(), getZ() - target.getZ());
                    }
                    Fx.burst(level, position().add(getLookAngle().scale(2)).add(0, 1.5, 0), Fx.VIOLET, 14, 0.25F);
                }
                return t < ad.duration();
            }
            case RUNE_TRAP, METEORS -> {
                return t < 15;
            }
            case ECLIPSE_CHARGE -> {
                setDeltaMovement(chargeDir.x * 1.1, getDeltaMovement().y, chargeDir.z * 1.1);
                for (Player p : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(0.8))) {
                    if (chargeHits.contains(p.getId())) continue;
                    chargeHits.add(p.getId());
                    p.hurt(damageSources().mobAttack(this), ad.damage());
                    p.knockback(1.5, -chargeDir.x, -chargeDir.z);
                }
                level.sendParticles(AstralParticleOptions.rgb(Fx.VIOLET, 0.2F, 12), getX(), getY() + 1, getZ(),
                        4, 0.4, 0.8, 0.4, 0.02);
                if (horizontalCollision && t > 2) {
                    // S'ecrase contre un pilier : etourdi 1 s (RECOVER allonge)
                    Fx.shake(level, position(), 0.4F, 10, 16);
                    level.playSound(null, blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1F, 0.6F);
                    setState(State.RECOVER);
                    stateTicks = -20;
                    return true;
                }
                return t < ad.duration();
            }
            case ENERGY_BEAM -> {
                tickEnergyBeam(level, target, t, ad);
                return t < ad.duration();
            }
        }
        return false;
    }

    private void tickEnergyBeam(ServerLevel level, LivingEntity target, int t, BossDefinition.AttackDef ad) {
        Vec3 eye = getEyePosition();
        Vec3 desired = target.getEyePosition().subtract(eye).normalize();
        double dot = Mth.clamp(beamDir.dot(desired), -1, 1);
        double angle = Math.acos(dot);
        double maxStep = Math.toRadians(ad.turnSpeed());
        double f = angle <= maxStep || angle < 1e-6 ? 1.0 : maxStep / angle;
        beamDir = beamDir.add(desired.subtract(beamDir).scale(f)).normalize();

        Vec3 end = eye.add(beamDir.scale(32));
        HitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        end = hit.getLocation();

        Fx.line(level, eye, end, Fx.PINK, 0.22F, 0.7);
        if (t % 5 == 0) {
            for (Player p : level.getEntitiesOfClass(Player.class, new AABB(eye, end).inflate(1))) {
                if (p.getBoundingBox().inflate(0.4).clip(eye, end).isPresent()) {
                    p.invulnerableTime = 0;
                    p.hurt(damageSources().indirectMagic(this, this), ad.damage());
                }
            }
        }
        if (t % 20 == 0) level.playSound(null, blockPosition(), ModSounds.CRYSTAL_RESONANCE.get(), SoundSource.HOSTILE, 1F, 0.5F);
    }

    private void spawnStrike(ServerLevel level, Vec3 pos, RuneStrikeEntity.Kind kind, int delay, float damage, float radius) {
        HitResult ground = level.clip(new ClipContext(pos.add(0, 3, 0), pos.add(0, -12, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        Vec3 p = ground.getType() == HitResult.Type.MISS ? pos : ground.getLocation();
        level.addFreshEntity(RuneStrikeEntity.create(level, p, kind, delay, damage, radius, this));
    }

    // =====================================================================
    //  Transition de phase
    // =====================================================================
    private void startTransition(ServerLevel level) {
        entityData.set(PHASE, 2);
        entityData.set(TRANSITIONING, true);
        int duration = BossDefinition.valerius().transitionTicks;
        transitionTicks = duration;
        setState(State.IDLE);
        getNavigation().stop();
        setNoGravity(true);
        if (anchor != null) teleportTo(anchor.getX() + 0.5, anchor.getY() + 1.5, anchor.getZ() + 0.5);
        setDeltaMovement(Vec3.ZERO);

        Vec3 feet = position();
        Fx.groundCircle(level, feet, RuneCirclePreset.TRANSITION, 10F, duration);
        Fx.circle(level, feet.add(0, 6, 0), new Vector3f(0, 1, 0), RuneCirclePreset.JUDGEMENT, 5F, duration);
        for (int i = 0; i < 6; i++) {
            float a = (float) (Math.PI * 2) * i / 6;
            Vec3 p = feet.add(Mth.cos(a) * 4, 0, Mth.sin(a) * 4);
            level.addFreshEntity(AstralBeamEntity.create(level, p, this, 0.35F, 30F, duration, 0F, 0xB98CFF, false));
        }
        level.playSound(null, blockPosition(), ModSounds.VALERIUS_TRANSITION.get(), SoundSource.HOSTILE, 4F, 1F);
        Fx.shake(level, feet, 0.5F, 40, 48);
        broadcast(level, Component.translatable("message.aetheria.valerius.phase2").withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    private void tickTransition(ServerLevel level) {
        setDeltaMovement(0, transitionTicks > BossDefinition.valerius().transitionTicks - 30 ? 0.04 : 0, 0);
        if (transitionTicks % 4 == 0) {
            level.sendParticles(AstralParticleOptions.rgb(0xB98CFF, 0.25F, 20), getX(), getY() + 1.6, getZ(),
                    10, 0.8, 1.2, 0.8, 0.05);
        }
        if (--transitionTicks > 0) return;

        entityData.set(TRANSITIONING, false);
        double push = BossDefinition.valerius().transitionPushRadius;
        for (Player p : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(push))) {
            Vec3 away = p.position().subtract(position()).multiply(1, 0, 1).normalize();
            p.knockback(2.0, -away.x, -away.z);
            p.hurtMarked = true;
        }
        Fx.groundCircle(level, position(), RuneCirclePreset.SHOCKWAVE, 10F, 12);
        Fx.burst(level, position().add(0, 1.5, 0), Fx.WHITE, 120, 0.8F);
        Fx.shake(level, position(), 1.0F, 30, 48);
        level.playSound(null, blockPosition(), ModSounds.JUDGEMENT_IMPACT.get(), SoundSource.HOSTILE, 4F, 0.8F);
        globalCooldown = 20;
    }

    private void hover() {
        double targetY = (anchor != null ? anchor.getY() : getY()) + 1.5;
        double dy = (targetY - getY()) * 0.1;
        LivingEntity target = getTarget();
        double dx = 0, dz = 0;
        if (target != null && distanceTo(target) > 6) {
            Vec3 to = target.position().subtract(position()).multiply(1, 0, 1).normalize();
            dx = to.x * 0.06;
            dz = to.z * 0.06;
        }
        setDeltaMovement(dx, dy + Mth.sin(tickCount * 0.08F) * 0.01, dz);
    }

    // =====================================================================
    //  Orbes de soutien et Jugement Absolu
    // =====================================================================
    private void tickOrbs(ServerLevel level) {
        BossDefinition def = BossDefinition.valerius();
        float ratio = getHealth() / getMaxHealth();
        List<ValeriusOrbEntity> orbs = level.getEntitiesOfClass(ValeriusOrbEntity.class, getBoundingBox().inflate(48));
        if (orbWave < def.orbThresholds.size() && ratio < def.orbThresholds.get(orbWave) && orbs.isEmpty()) {
            orbWave++;
            spawnOrbs(level);
            return;
        }
        if (orbs.isEmpty()) {
            orbDeadline = 0;
            return;
        }
        if (tickCount % 20 == 0) heal(def.orbHealPerSecond * orbs.size() / 4F);
        if (tickCount % 10 == 0) {
            for (ValeriusOrbEntity orb : orbs) Fx.line(level, orb.position().add(0, 0.45, 0), position().add(0, 1.8, 0), 0xC08CFF, 0.1F, 1.2);
        }
        if (orbDeadline > 0 && --orbDeadline == 0) absoluteJudgement(level, orbs);
    }

    private void spawnOrbs(ServerLevel level) {
        BlockPos c = anchor != null ? anchor : blockPosition();
        int[][] corners = {{9, 9}, {-9, 9}, {9, -9}, {-9, -9}};
        for (int[] o : corners) {
            ValeriusOrbEntity orb = new ValeriusOrbEntity(ModEntities.VALERIUS_ORB.get(), level);
            orb.setPos(c.getX() + 0.5 + o[0], c.getY() + 3, c.getZ() + 0.5 + o[1]);
            var hp = orb.getAttribute(Attributes.MAX_HEALTH);
            if (hp != null) hp.setBaseValue(BossDefinition.valerius().orbHealth);
            orb.setHealth(orb.getMaxHealth());
            level.addFreshEntity(orb);
            Fx.burst(level, orb.position(), 0xC08CFF, 30, 0.3F);
        }
        orbDeadline = BossDefinition.valerius().orbDeadline;
        broadcast(level, Component.translatable("message.aetheria.valerius.orbs").withStyle(ChatFormatting.GOLD));
    }

    private void absoluteJudgement(ServerLevel level, List<ValeriusOrbEntity> orbs) {
        Vec3 eye = getEyePosition();
        Fx.groundCircle(level, position(), RuneCirclePreset.SHOCKWAVE, 30F, 20);
        Fx.shake(level, position(), 1.0F, 40, 64);
        level.playSound(null, blockPosition(), ModSounds.JUDGEMENT_IMPACT.get(), SoundSource.HOSTILE, 6F, 0.5F);
        for (Player p : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(48))) {
            if (p.isCreative() || p.isSpectator()) continue;
            HitResult los = level.clip(new ClipContext(eye, p.getEyePosition(), ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, this));
            boolean covered = los.getType() == HitResult.Type.BLOCK;
            if (!covered) {
                p.invulnerableTime = 0;
                p.hurt(damageSources().indirectMagic(this, this), 999F);
            }
        }
        orbs.forEach(Entity::discard);
    }

    // =====================================================================
    //  Degats, mort, loot
    // =====================================================================
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isTransitioning() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel level) {
            level.getEntitiesOfClass(ValeriusOrbEntity.class, getBoundingBox().inflate(48)).forEach(Entity::discard);
            for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(64))) {
                ManaCapability.get(p).ifPresent(data -> {
                    boolean first = !data.hasReceivedRampart();
                    if (first || random.nextFloat() < 0.25F) {
                        data.markRampartReceived();
                        p.getInventory().placeItemBackInInventory(new ItemStack(ModItems.ECLIPSE_RAMPART.get()));
                    }
                });
            }
            broadcast(level, Component.translatable("message.aetheria.valerius.defeated").withStyle(ChatFormatting.GOLD));
            Fx.circle(level, position().add(0, 0.1, 0), new Vector3f(0, 1, 0), RuneCirclePreset.TRANSITION, 8F, DEATH_TICKS);
        }
    }

    @Override
    protected void tickDeath() {
        ++deathTime;
        if (level().isClientSide()) {
            for (int i = 0; i < 4; i++) {
                level().addParticle(AstralParticleOptions.rgb(i % 2 == 0 ? 0xFFD36B : 0x9B5CFF, 0.2F, 30),
                        getRandomX(1.0), getRandomY(), getRandomZ(1.0), 0, 0.08, 0);
            }
        }
        if (deathTime >= DEATH_TICKS && !level().isClientSide() && !isRemoved()) {
            level().broadcastEntityEvent(this, (byte) 60);
            remove(RemovalReason.KILLED);
        }
    }

    private void broadcast(ServerLevel level, Component msg) {
        for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(64))) {
            p.sendSystemMessage(msg);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        bossBar.setName(getDisplayName());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Phase", getPhase());
        tag.putInt("OrbWave", orbWave);
        if (anchor != null) tag.put("Anchor", NbtUtils.writeBlockPos(anchor));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(PHASE, Math.max(1, tag.getInt("Phase")));
        orbWave = tag.getInt("OrbWave");
        if (tag.contains("Anchor")) anchor = NbtUtils.readBlockPos(tag.getCompound("Anchor"));
        if (getPhase() == 2) setNoGravity(true);
        if (hasCustomName()) bossBar.setName(getDisplayName());
    }

    // =====================================================================
    //  GeckoLib
    // =====================================================================
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, anim -> {
            if (isDeadOrDying()) return anim.setAndContinue(DEATH);
            if (isTransitioning()) return anim.setAndContinue(TRANSITION);
            if (getPhase() == 2) return anim.setAndContinue(HOVER);
            if (anim.isMoving()) return anim.setAndContinue(WALK);
            return anim.setAndContinue(IDLE);
        }).triggerableAnim("cast", CAST).triggerableAnim("attack", ATTACK));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
