package com.aetheria.astralruin.entity.boss;

import com.aetheria.astralruin.config.AetheriaConfig;
import com.aetheria.astralruin.entity.spell.AstralBeamEntity;
import com.aetheria.astralruin.entity.spell.GravityWellEntity;
import com.aetheria.astralruin.entity.spell.RuneStrikeEntity;
import com.aetheria.astralruin.entity.spell.SpectralBladeEntity;
import com.aetheria.astralruin.mana.ManaCapability;
import com.aetheria.astralruin.network.CinematicS2C;
import com.aetheria.astralruin.network.ModNetwork;
import com.aetheria.astralruin.particle.AstralParticleOptions;
import com.aetheria.astralruin.particle.AstralParticleOptions.Kind;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
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

import java.util.*;

/**
 * Valerius, le Gardien des Runes.
 * Apparition cinematique, phase 1 (guerrier : 6 attaques), transition cinematique a 50 %,
 * phase 2 (forme liberee : 6 attaques + orbes), rage a 15 % (arene fermee, attaques accelerees), mort cinematique.
 */
public class ValeriusEntity extends Monster implements GeoEntity {
    private static RawAnimation loop(String n) { return RawAnimation.begin().thenLoop("animation.valerius." + n); }
    private static RawAnimation once(String n) { return RawAnimation.begin().thenPlay("animation.valerius." + n); }
    private static RawAnimation hold(String n) { return RawAnimation.begin().thenPlayAndHold("animation.valerius." + n); }

    private static final RawAnimation IDLE = loop("idle"), WALK = loop("walk"), HOVER = loop("hover");
    private static final RawAnimation INTRO_ANIM = hold("intro"), TRANSITION = hold("phase_transition"), DEATH = hold("death");

    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(ValeriusEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> INTRO = SynchedEntityData.defineId(ValeriusEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TRANSITIONING = SynchedEntityData.defineId(ValeriusEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ENRAGED = SynchedEntityData.defineId(ValeriusEntity.class, EntityDataSerializers.BOOLEAN);

    public static final int DEATH_TICKS = 110;
    private static final int GOLD = 0xFFD36B, VIOLET = 0x9B5CFF, PINK = 0xE07CFF, WHITE = 0xFFF6E0;

    private enum Attack {
        SWORD_COMBO, GROUND_SLAM, RUNE_TRAP, ECLIPSE_CHARGE, RUNE_BLADES, WHIRLWIND,
        METEORS, ENERGY_BEAM, STARFALL_CROSS, GRAVITY_WELL, JUDGEMENT_RAIN;

        String key() { return name().toLowerCase(Locale.ROOT); }
    }

    private static final Attack[] PHASE_1 = {Attack.SWORD_COMBO, Attack.GROUND_SLAM, Attack.RUNE_TRAP,
            Attack.ECLIPSE_CHARGE, Attack.RUNE_BLADES, Attack.WHIRLWIND};
    private static final Attack[] PHASE_2 = {Attack.METEORS, Attack.ENERGY_BEAM, Attack.STARFALL_CROSS,
            Attack.GRAVITY_WELL, Attack.JUDGEMENT_RAIN, Attack.RUNE_BLADES};

    private enum State { IDLE, TELEGRAPH, EXECUTE, RECOVER }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossBar = new ServerBossEvent(Component.translatable("entity.aetheria.valerius"),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);

    @Nullable private BlockPos anchor;
    private State state = State.IDLE;
    @Nullable private Attack attack;
    private int stateTicks;
    private int globalCooldown = 30;
    private final Map<Attack, Integer> cooldowns = new EnumMap<>(Attack.class);
    private int introTicks;
    private int transitionTicks;
    private int orbWave;
    private int orbDeadline;
    private int crossRotation;
    private Vec3 chargeDir = Vec3.ZERO;
    private Vec3 beamDir = Vec3.ZERO;
    private Vec3 slamCenter = Vec3.ZERO;
    private boolean slamLanded;
    private final Set<Integer> hitOnce = new HashSet<>();

    public ValeriusEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 500;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 600).add(Attributes.ARMOR, 12).add(Attributes.ARMOR_TOUGHNESS, 4)
                .add(Attributes.MOVEMENT_SPEED, 0.28).add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
                .add(Attributes.FOLLOW_RANGE, 48).add(Attributes.ATTACK_DAMAGE, 14).add(Attributes.ATTACK_KNOCKBACK, 0.5);
    }

    // =====================================================================
    //  Initialisation
    // =====================================================================
    public void setAnchor(BlockPos pos) { this.anchor = pos.immutable(); }

    public void applyDefinition() {
        BossDefinition def = BossDefinition.valerius();
        setBase(Attributes.MAX_HEALTH, def.maxHealth * AetheriaConfig.BOSS_HEALTH_MULTIPLIER.get());
        setBase(Attributes.ARMOR, def.armor);
        setBase(Attributes.ARMOR_TOUGHNESS, def.armorToughness);
        setBase(Attributes.MOVEMENT_SPEED, def.movementSpeed);
        setBase(Attributes.ATTACK_DAMAGE, def.attack("sword_combo").damage());
        setHealth(getMaxHealth());
    }

    private void setBase(Attribute attribute, double value) {
        var inst = getAttribute(attribute);
        if (inst != null) inst.setBaseValue(value);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        applyDefinition();
        introTicks = BossDefinition.valerius().introTicks;
        entityData.set(INTRO, true);
        return super.finalizeSpawn(level, difficulty, reason, data, tag);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(PHASE, 1);
        entityData.define(INTRO, false);
        entityData.define(TRANSITIONING, false);
        entityData.define(ENRAGED, false);
    }

    public int getPhase() { return entityData.get(PHASE); }

    /** Cinematique d'histoire (client) : force l'etat d'animation d'une instance de decor. */
    public void setCinematicState(boolean intro, int phase) {
        entityData.set(INTRO, intro);
        entityData.set(PHASE, phase);
    }
    public boolean isIntro() { return entityData.get(INTRO); }
    public boolean isTransitioning() { return entityData.get(TRANSITIONING); }
    public boolean isEnraged() { return entityData.get(ENRAGED); }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    private static BossDefinition.AttackDef def(Attack a) { return BossDefinition.valerius().attack(a.key()); }

    // =====================================================================
    //  Boucle serveur
    // =====================================================================
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        ServerLevel level = (ServerLevel) level();
        bossBar.setProgress(getHealth() / getMaxHealth());
        if (anchor == null) anchor = blockPosition();
        BossDefinition def = BossDefinition.valerius();

        if (isIntro()) { tickIntro(level, def); return; }
        if (getPhase() == 1 && getHealth() < getMaxHealth() * def.phase2Threshold) startTransition(level);
        if (isTransitioning()) { tickTransition(level); return; }
        if (getPhase() == 2 && !isEnraged() && getHealth() < getMaxHealth() * def.enrageThreshold) enrage(level);

        cooldowns.replaceAll((k, v) -> Math.max(0, v - 1));
        if (globalCooldown > 0) globalCooldown--;
        if (getPhase() == 2) {
            hover();
            tickOrbs(level);
        }
        if (isEnraged()) tickArena(level, def);

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
                if (stateTicks >= (attack == null ? 0 : def(attack).telegraph())) {
                    setState(State.EXECUTE);
                    beginExecute(level, target);
                }
            }
            case EXECUTE -> {
                if (!tickExecute(level, target)) setState(State.RECOVER);
            }
            case RECOVER -> {
                if (stateTicks >= (isEnraged() ? 10 : 18)) {
                    setState(State.IDLE);
                    globalCooldown = isEnraged() ? 8 : getPhase() == 1 ? 18 : 14;
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
        for (Attack a : getPhase() == 1 ? PHASE_1 : PHASE_2) {
            BossDefinition.AttackDef ad = def(a);
            if (cooldowns.getOrDefault(a, 0) == 0 && d >= ad.minRange() && d <= ad.maxRange()) {
                for (int i = 0; i < ad.weight(); i++) pool.add(a);
            }
        }
        if (pool.isEmpty()) return;
        attack = pool.get(random.nextInt(pool.size()));
        hitOnce.clear();
        setState(State.TELEGRAPH);
        String anim = switch (attack) {
            case SWORD_COMBO, ECLIPSE_CHARGE -> "attack";
            case GROUND_SLAM -> "slam";
            case WHIRLWIND -> "spin";
            case RUNE_BLADES, GRAVITY_WELL -> "summon";
            case ENERGY_BEAM -> "channel";
            default -> "cast";
        };
        triggerAnim("main", anim);
        level().playSound(null, blockPosition(), ModSounds.CAST_CHARGE.get(), SoundSource.HOSTILE, 1.5F, 0.7F);
        Fx.particles((ServerLevel) level(), Kind.GLYPH, position().add(0, 2.5, 0), 8, 1.2, 0.05, GOLD, 0.3F, 25);
    }

    private void setCooldown(Attack a) {
        float mult = isEnraged() ? BossDefinition.valerius().enrageCooldownMultiplier : 1F;
        cooldowns.put(a, Math.round(def(a).cooldown() * mult));
    }

    // =====================================================================
    //  Attaques
    // =====================================================================
    private void beginExecute(ServerLevel level, LivingEntity target) {
        if (attack == null) return;
        BossDefinition.AttackDef ad = def(attack);
        setCooldown(attack);
        switch (attack) {
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
                Fx.slash(level, position().add(0, 1.8, 0).add(chargeDir.scale(1.5)), chargeDir, 90F, 3F, PINK, 10);
            }
            case GROUND_SLAM -> {
                slamLanded = false;
                Vec3 to = target.position().subtract(position());
                Vec3 flat = new Vec3(to.x, 0, to.z);
                double dist = Math.min(flat.length(), 10);
                Vec3 dir = flat.lengthSqr() > 1e-4 ? flat.normalize() : Vec3.ZERO;
                setDeltaMovement(dir.x * dist * 0.11, 0.95, dir.z * dist * 0.11);
                hasImpulse = true;
                Fx.particles(level, Kind.SMOKE, position(), 12, 0.8, 0.05, 0x3A2A50, 1.0F, 30);
            }
            case RUNE_BLADES -> {
                float yaw = (float) Math.toRadians(getYRot());
                for (int i = 0; i < ad.count(); i++) {
                    float off = (i - (ad.count() - 1) / 2F) * 0.55F;
                    Vec3 p = position().add(Math.sin(-yaw + off + Math.PI) * 2.2, 4.4 + Math.abs(off) * 0.8,
                            Math.cos(-yaw + off + Math.PI) * 2.2);
                    level.addFreshEntity(new SpectralBladeEntity(level, this, p, target, 20 + i * 6, ad.damage()));
                    Fx.flare(level, p, 1.2F, 0xC8A8FF, 14);
                }
                level.playSound(null, blockPosition(), ModSounds.CRYSTAL_RESONANCE.get(), SoundSource.HOSTILE, 2F, 1.4F);
            }
            case METEORS -> {
                spawnStrike(level, target.position(), RuneStrikeEntity.Kind.METEOR, ad.duration(), ad.damage(), ad.radius());
                for (int i = 1; i < ad.count(); i++) {
                    Vec3 p = target.position().add((random.nextDouble() - 0.5) * 14, 0, (random.nextDouble() - 0.5) * 14);
                    spawnStrike(level, p, RuneStrikeEntity.Kind.METEOR, ad.duration() + i * 3, ad.damage(), ad.radius());
                }
                Fx.flare(level, position().add(0, 6, 0), 3F, 0xFF9A3C, 20);
            }
            case ENERGY_BEAM -> beamDir = target.getEyePosition().subtract(getEyePosition()).normalize();
            case STARFALL_CROSS -> {
                crossRotation = (crossRotation + 45) % 90;
                for (int arm = 0; arm < 4; arm++) {
                    double a = Math.toRadians(arm * 90 + crossRotation);
                    for (int j = 0; j < ad.count(); j++) {
                        Vec3 p = position().add(Math.cos(a) * (2.5 + j * 1.8), 0, Math.sin(a) * (2.5 + j * 1.8));
                        spawnStrike(level, p, RuneStrikeEntity.Kind.PILLAR, 15 + j * 2, ad.damage(), ad.radius());
                    }
                }
            }
            case GRAVITY_WELL -> {
                Vec3 center = anchor != null ? Vec3.atBottomCenterOf(anchor) : position();
                level.addFreshEntity(GravityWellEntity.create(level, center, this, ad.radius(), ad.duration(), ad.damage()));
            }
            default -> {}
        }
    }

    /** @return true tant que l'attaque continue. */
    private boolean tickExecute(ServerLevel level, LivingEntity target) {
        if (attack == null) return false;
        int t = stateTicks;
        BossDefinition.AttackDef ad = def(attack);
        switch (attack) {
            case SWORD_COMBO -> {
                if (t == 2 || t == 10 || t == 18) {
                    swing(InteractionHand.MAIN_HAND);
                    if (t == 10) triggerAnim("main", "attack2");
                    else if (t == 18) triggerAnim("main", "attack");
                    Vec3 look = getLookAngle();
                    Vec3 front = position().add(0, 1.8, 0).add(look.scale(2));
                    Fx.slash(level, front, look, t == 2 ? 20F : t == 10 ? -20F : 90F, 3.2F, t == 18 ? WHITE : PINK, 10);
                    level.playSound(null, blockPosition(), ModSounds.SLASH.get(), SoundSource.HOSTILE, 1.4F, t == 18 ? 0.7F : 1F);
                    if (distanceTo(target) < 4.8F) {
                        doHurtTarget(target);
                        if (t == 18) target.knockback(1.2, getX() - target.getX(), getZ() - target.getZ());
                    }
                    if (t == 18) Fx.shockwave(level, position().add(look.scale(3)), 4F, PINK, 12);
                }
                return t < ad.duration();
            }
            case GROUND_SLAM -> {
                if (!slamLanded && t > 6 && onGround()) {
                    slamLanded = true;
                    slamCenter = position();
                    stateTicks = 1000; // compteur de l'onde (repere distinct de la phase de saut)
                    Fx.shockwave(level, slamCenter, ad.radius(), PINK, 20);
                    Fx.shockwave(level, slamCenter, ad.radius() * 0.6F, WHITE, 14);
                    Fx.impact(level, slamCenter, 3F, VIOLET);
                    for (int i = 0; i < 6; i++) {
                        double a = Math.PI * 2 * i / 6;
                        Fx.lightning(level, slamCenter.add(0, 0.2, 0), slamCenter.add(Math.cos(a) * 5, 0.2, Math.sin(a) * 5), PINK, 0.3F, 10);
                    }
                    Fx.shake(level, slamCenter, 0.9F, 20, 40);
                    level.playSound(null, blockPosition(), ModSounds.VALERIUS_SLAM.get(), SoundSource.HOSTILE, 3F, 1F);
                }
                if (slamLanded) {
                    // L'onde s'etend comme le visuel (ease-out cubique sur 20 ticks) : sauter permet de l'esquiver
                    float k = Mth.clamp((stateTicks - 1000) / 20F, 0F, 1F);
                    float r = ad.radius() * (1F - (1F - k) * (1F - k) * (1F - k));
                    for (Player p : level.getEntitiesOfClass(Player.class, new AABB(slamCenter, slamCenter).inflate(ad.radius() + 1))) {
                        double dist = p.position().distanceTo(slamCenter);
                        if (Math.abs(dist - r) < 1.1 && p.onGround() && hitOnce.add(p.getId())) {
                            p.hurt(damageSources().mobAttack(this), ad.damage());
                            p.setDeltaMovement(p.getDeltaMovement().add(0, 0.7, 0));
                            p.hurtMarked = true;
                        }
                    }
                    return stateTicks - 1000 < 22;
                }
                return t < 60;
            }
            case RUNE_TRAP, METEORS, STARFALL_CROSS, GRAVITY_WELL -> {
                return t < 15;
            }
            case ECLIPSE_CHARGE -> {
                setDeltaMovement(chargeDir.x * 1.15, getDeltaMovement().y, chargeDir.z * 1.15);
                for (Player p : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(0.8))) {
                    if (!hitOnce.add(p.getId())) continue;
                    p.hurt(damageSources().mobAttack(this), ad.damage());
                    p.knockback(1.5, -chargeDir.x, -chargeDir.z);
                    Fx.slash(level, p.position().add(0, 1, 0), chargeDir, 60F, 2.2F, WHITE, 8);
                }
                Fx.particles(level, Kind.SMOKE, position().add(0, 1.5, 0), 3, 0.4, 0.01, 0x2A1048, 1.2F, 20);
                Fx.particles(level, Kind.SPARK, position().add(0, 1.5, 0), 4, 0.5, 0.02, PINK, 0.2F, 12);
                if (horizontalCollision && t > 2) {
                    Fx.shake(level, position(), 0.5F, 12, 16);
                    Fx.impact(level, position(), 2F, PINK);
                    level.playSound(null, blockPosition(), ModSounds.VALERIUS_SLAM.get(), SoundSource.HOSTILE, 1.5F, 1.4F);
                    setState(State.RECOVER);
                    stateTicks = -20; // etourdi 1 s
                    return true;
                }
                return t < ad.duration();
            }
            case RUNE_BLADES -> {
                return t < 25;
            }
            case WHIRLWIND -> {
                if (t % 5 == 0) {
                    for (int i = 0; i < 3; i++) {
                        double a = Math.toRadians(t * 25 + i * 120);
                        Vec3 dir = new Vec3(Math.cos(a), 0, Math.sin(a));
                        Fx.slash(level, position().add(0, 1.4, 0).add(dir.scale(2)), dir, 0F, 3F, i == 0 ? WHITE : PINK, 6);
                    }
                    for (Player p : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(ad.radius()))) {
                        Vec3 to = position().subtract(p.position());
                        if (to.length() > ad.radius()) continue;
                        p.setDeltaMovement(p.getDeltaMovement().add(to.normalize().scale(0.25)));
                        p.hurtMarked = true;
                        p.invulnerableTime = 0;
                        p.hurt(damageSources().mobAttack(this), ad.damage());
                    }
                }
                if (t % 10 == 0) level.playSound(null, blockPosition(), ModSounds.SLASH.get(), SoundSource.HOSTILE, 1.2F, 0.8F);
                return t < ad.duration();
            }
            case ENERGY_BEAM -> {
                tickEnergyBeam(level, target, t, ad);
                return t < ad.duration();
            }
            case JUDGEMENT_RAIN -> {
                if (t % 25 == 1 && t / 25 < ad.count()) {
                    for (Player p : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(40))) {
                        if (p.isCreative() || p.isSpectator()) continue;
                        spawnStrike(level, p.position(), RuneStrikeEntity.Kind.PILLAR, 25, ad.damage(), ad.radius());
                    }
                    Fx.flare(level, position().add(0, 5, 0), 2.5F, WHITE, 12);
                }
                return t < ad.duration();
            }
        }
        return false;
    }

    private void tickEnergyBeam(ServerLevel level, LivingEntity target, int t, BossDefinition.AttackDef ad) {
        Vec3 eye = getEyePosition();
        Vec3 desired = target.getEyePosition().subtract(eye).normalize();
        double angle = Math.acos(Mth.clamp(beamDir.dot(desired), -1, 1));
        double maxStep = Math.toRadians(isEnraged() ? ad.turnSpeed() * 1.5 : ad.turnSpeed());
        double f = angle <= maxStep || angle < 1e-6 ? 1.0 : maxStep / angle;
        beamDir = beamDir.add(desired.subtract(beamDir).scale(f)).normalize();
        Vec3 end = eye.add(beamDir.scale(32));
        HitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        end = hit.getLocation();
        if (t % 4 == 0) {
            Fx.beam(level, eye, end, PINK, 1.1F, 6);
            Fx.flare(level, end, 1.6F, PINK, 6);
            Fx.flare(level, eye.add(beamDir), 1.2F, WHITE, 6);
        }
        Fx.particles(level, Kind.EMBER, end, 3, 0.3, 0.08, PINK, 0.2F, 14);
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
    //  Apparition, transition, rage
    // =====================================================================
    private void tickIntro(ServerLevel level, BossDefinition def) {
        getNavigation().stop();
        setDeltaMovement(0, getDeltaMovement().y, 0);
        int t = def.introTicks - introTicks;
        Vec3 c = position();
        // le paquet attend quelques ticks que le client ait recu l'entite
        if (t == 3) sendCinematic(level, CinematicS2C.Type.INTRO);
        if (t == 0) {
            Fx.pillar(level, c, 32F, 2.2F, 0xE6D0FF, 70);
            Fx.groundCircle(level, c, RuneCirclePreset.TRANSITION, 9F, def.introTicks);
            level.playSound(null, blockPosition(), ModSounds.VALERIUS_TRANSITION.get(), SoundSource.HOSTILE, 4F, 1.2F);
        }
        if (t > 10 && t < 90 && t % 20 == 0) Fx.implosion(level, c, 10F, VIOLET, 20);
        if (t > 10 && t < 90 && t % 3 == 0) Fx.particles(level, Kind.GLYPH, c.add(0, 2, 0), 4, 3.0, 0.02, GOLD, 0.35F, 30);
        if (t == 95) {
            level.playSound(null, blockPosition(), ModSounds.VALERIUS_ROAR.get(), SoundSource.HOSTILE, 5F, 1F);
            Fx.shockwave(level, c, 18F, WHITE, 24);
            Fx.flare(level, c.add(0, 3, 0), 6F, WHITE, 16);
            Fx.shake(level, c, 0.9F, 30, 48);
            for (int i = 0; i < 4; i++) {
                double a = Math.PI / 2 * i + 0.4;
                Vec3 p = c.add(Math.cos(a) * 7, 0, Math.sin(a) * 7);
                Fx.lightning(level, p.add(0, 26, 0), p, WHITE, 0.6F, 12);
                Fx.impact(level, p, 1.5F, GOLD);
            }
        }
        if (--introTicks <= 0) {
            entityData.set(INTRO, false);
            globalCooldown = 20;
            broadcast(level, Component.translatable("message.aetheria.valerius.awaken").withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

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
        sendCinematic(level, CinematicS2C.Type.PHASE_TWO);
        Vec3 feet = position();
        Fx.groundCircle(level, feet, RuneCirclePreset.TRANSITION, 10F, duration);
        Fx.circle(level, feet.add(0, 7, 0), new Vector3f(0, 1, 0), RuneCirclePreset.JUDGEMENT, 5F, duration);
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
        int duration = BossDefinition.valerius().transitionTicks;
        int t = duration - transitionTicks;
        setDeltaMovement(0, t < 30 ? 0.04 : 0, 0);
        if (t % 4 == 0) Fx.particles(level, Kind.SMOKE, position().add(0, 1.6, 0), 6, 1.0, 0.03, 0x4A2080, 1.1F, 30);
        if (t % 15 == 0) Fx.implosion(level, position(), 9F, PINK, 15);
        if (t % 12 == 6) {
            double a = random.nextDouble() * Math.PI * 2;
            Vec3 p = position().add(Math.cos(a) * 9, -1.5, Math.sin(a) * 9);
            Fx.lightning(level, position().add(0, 2, 0), p, PINK, 0.4F, 8);
            Fx.impact(level, p, 1.2F, PINK);
        }
        if (--transitionTicks > 0) return;
        entityData.set(TRANSITIONING, false);
        double push = BossDefinition.valerius().transitionPushRadius;
        for (Player p : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(push))) {
            Vec3 away = p.position().subtract(position()).multiply(1, 0, 1);
            if (away.lengthSqr() > 1e-4) p.knockback(2.0, -away.normalize().x, -away.normalize().z);
            p.hurtMarked = true;
        }
        Fx.shockwave(level, position().add(0, -1.5, 0), 20F, WHITE, 26);
        Fx.flare(level, position().add(0, 1.5, 0), 8F, WHITE, 18);
        Fx.particles(level, Kind.SHARD, position().add(0, 1.5, 0), 60, 1.0, 0.8, PINK, 0.2F, 40);
        Fx.shake(level, position(), 1.0F, 30, 48);
        level.playSound(null, blockPosition(), ModSounds.VALERIUS_ROAR.get(), SoundSource.HOSTILE, 5F, 0.9F);
        globalCooldown = 20;
    }

    private void enrage(ServerLevel level) {
        entityData.set(ENRAGED, true);
        triggerAnim("main", "enrage");
        level.playSound(null, blockPosition(), ModSounds.VALERIUS_ROAR.get(), SoundSource.HOSTILE, 6F, 0.7F);
        Fx.shake(level, position(), 1.0F, 40, 64);
        Fx.flare(level, position().add(0, 2, 0), 9F, 0xFF4060, 20);
        Fx.shockwave(level, position().add(0, -1.5, 0), 26F, 0xFF4060, 30);
        cooldowns.replaceAll((k, v) -> v / 2);
        broadcast(level, Component.translatable("message.aetheria.valerius.enrage").withStyle(ChatFormatting.RED));
    }

    /** Rage : l'arene se referme, rester hors du cercle brule. */
    private void tickArena(ServerLevel level, BossDefinition def) {
        if (anchor == null) return;
        Vec3 c = Vec3.atBottomCenterOf(anchor);
        if (tickCount % 100 == 0) Fx.groundCircle(level, c, RuneCirclePreset.ECLIPSE, def.arenaRadius, 102);
        if (tickCount % 20 != 0) return;
        for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, new AABB(c, c).inflate(64))) {
            if (p.isCreative() || p.isSpectator()) continue;
            Vec3 to = c.subtract(p.position()).multiply(1, 0, 1);
            if (to.length() <= def.arenaRadius + 1) continue;
            p.hurt(damageSources().indirectMagic(this, this), 3F);
            p.setDeltaMovement(p.getDeltaMovement().add(to.normalize().scale(0.6)));
            p.hurtMarked = true;
            Fx.particles(level, Kind.EMBER, p.position().add(0, 1, 0), 10, 0.4, 0.05, 0xFF4060, 0.25F, 16);
        }
    }

    private void hover() {
        double targetY = (anchor != null ? anchor.getY() : getY()) + 1.5;
        double dy = (targetY - getY()) * 0.1;
        LivingEntity target = getTarget();
        double dx = 0, dz = 0;
        if (target != null && distanceTo(target) > 6) {
            Vec3 to = target.position().subtract(position()).multiply(1, 0, 1).normalize();
            double speed = isEnraged() ? 0.09 : 0.06;
            dx = to.x * speed;
            dz = to.z * speed;
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
        if (tickCount % 8 == 0) {
            for (ValeriusOrbEntity orb : orbs) Fx.beam(level, orb.position().add(0, 0.45, 0), position().add(0, 1.8, 0), 0xC08CFF, 0.35F, 9);
        }
        if (orbDeadline > 0 && orbDeadline % 100 == 0) {
            broadcast(level, Component.translatable("message.aetheria.valerius.countdown", orbDeadline / 20).withStyle(ChatFormatting.GOLD));
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
            Fx.pillar(level, orb.position().add(0, -3, 0), 12F, 0.6F, 0xC08CFF, 20);
            Fx.flare(level, orb.position(), 2F, 0xC08CFF, 14);
        }
        orbDeadline = BossDefinition.valerius().orbDeadline;
        broadcast(level, Component.translatable("message.aetheria.valerius.orbs").withStyle(ChatFormatting.GOLD));
    }

    private void absoluteJudgement(ServerLevel level, List<ValeriusOrbEntity> orbs) {
        Vec3 eye = getEyePosition();
        Fx.groundCircle(level, position(), RuneCirclePreset.SHOCKWAVE, 30F, 20);
        Fx.shockwave(level, position(), 40F, WHITE, 30);
        Fx.flare(level, eye, 12F, WHITE, 24);
        Fx.shake(level, position(), 1.0F, 40, 64);
        level.playSound(null, blockPosition(), ModSounds.JUDGEMENT_IMPACT.get(), SoundSource.HOSTILE, 6F, 0.5F);
        for (Player p : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(48))) {
            if (p.isCreative() || p.isSpectator()) continue;
            HitResult los = level.clip(new ClipContext(eye, p.getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (los.getType() != HitResult.Type.BLOCK) {
                Fx.beam(level, eye, p.getEyePosition(), WHITE, 1.6F, 12);
                p.invulnerableTime = 0;
                p.hurt(damageSources().indirectMagic(this, this), 999F);
            }
        }
        orbs.forEach(Entity::discard);
    }

    // =====================================================================
    //  Degats, mort cinematique, loot
    // =====================================================================
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if ((isIntro() || isTransitioning()) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        boolean hurt = super.hurt(source, amount);
        if (hurt && level() instanceof ServerLevel level && amount > 4) {
            Fx.particles(level, Kind.SHARD, position().add(0, 2, 0), 6, 0.5, 0.2, PINK, 0.14F, 20);
        }
        return hurt;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel level) {
            sendCinematic(level, CinematicS2C.Type.DEATH);
            level.getEntitiesOfClass(ValeriusOrbEntity.class, getBoundingBox().inflate(48)).forEach(Entity::discard);
            setNoGravity(true);
            for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(64))) {
                ManaCapability.get(p).ifPresent(data -> {
                    boolean first = !data.hasReceivedRampart();
                    if (first || random.nextFloat() < 0.25F) {
                        data.markRampartReceived();
                        p.getInventory().placeItemBackInInventory(new ItemStack(ModItems.ECLIPSE_RAMPART.get()));
                    }
                });
            }
            Fx.circle(level, position().add(0, 0.1, 0), new Vector3f(0, 1, 0), RuneCirclePreset.TRANSITION, 8F, DEATH_TICKS);
        }
    }

    @Override
    protected void tickDeath() {
        ++deathTime;
        setDeltaMovement(0, deathTime > 40 && deathTime < 80 ? 0.03 : 0, 0);
        if (level() instanceof ServerLevel level) {
            Vec3 c = position().add(0, 2, 0);
            if (deathTime % 6 == 0 && deathTime < 95) {
                Vec3 p = c.add(random.nextGaussian() * 0.8, random.nextGaussian() * 1.0, random.nextGaussian() * 0.8);
                Fx.flare(level, p, 1.4F, deathTime % 12 == 0 ? GOLD : PINK, 10);
                Fx.lightning(level, c, p.add(p.subtract(c).scale(3)), WHITE, 0.25F, 6);
                level.playSound(null, blockPosition(), ModSounds.RUNE_EXPLODE.get(), SoundSource.HOSTILE, 1F, 1.5F);
            }
            if (deathTime == 60) Fx.implosion(level, position(), 14F, WHITE, 30);
            if (deathTime == 95) {
                Fx.pillar(level, position(), 48F, 3F, WHITE, 40);
                Fx.shockwave(level, position(), 30F, GOLD, 30);
                Fx.flare(level, c, 12F, WHITE, 24);
                Fx.particles(level, Kind.SHARD, c, 80, 1.2, 0.9, GOLD, 0.25F, 50);
                Fx.particles(level, Kind.GLYPH, c, 40, 2.0, 0.2, GOLD, 0.35F, 60);
                Fx.shake(level, position(), 1.0F, 30, 64);
                level.playSound(null, blockPosition(), ModSounds.JUDGEMENT_IMPACT.get(), SoundSource.HOSTILE, 6F, 0.8F);
                broadcast(level, Component.translatable("message.aetheria.valerius.defeated").withStyle(ChatFormatting.GOLD));
            }
        }
        if (deathTime >= DEATH_TICKS && !level().isClientSide() && !isRemoved()) {
            level().broadcastEntityEvent(this, (byte) 60);
            remove(RemovalReason.KILLED);
        }
    }

    private void sendCinematic(ServerLevel level, CinematicS2C.Type type) {
        for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(64))) {
            ModNetwork.sendToPlayer(p, new CinematicS2C(type, getId()));
        }
    }

    private void broadcast(ServerLevel level, Component msg) {
        for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(64))) p.sendSystemMessage(msg);
    }

    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override protected boolean shouldDespawnInPeaceful() { return false; }
    @Override public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }

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
        tag.putBoolean("Enraged", isEnraged());
        if (anchor != null) tag.put("Anchor", NbtUtils.writeBlockPos(anchor));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(PHASE, Math.max(1, tag.getInt("Phase")));
        entityData.set(ENRAGED, tag.getBoolean("Enraged"));
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
        AnimationController<ValeriusEntity> main = new AnimationController<>(this, "main", 4, anim -> {
            if (isDeadOrDying()) return anim.setAndContinue(DEATH);
            if (isIntro()) return anim.setAndContinue(INTRO_ANIM);
            if (isTransitioning()) return anim.setAndContinue(TRANSITION);
            if (getPhase() == 2) return anim.setAndContinue(HOVER);
            if (anim.isMoving()) return anim.setAndContinue(WALK);
            return anim.setAndContinue(IDLE);
        });
        for (String n : new String[]{"attack", "attack2", "slam", "spin", "cast", "channel", "summon", "enrage"}) {
            main.triggerableAnim(n, once(n));
        }
        controllers.add(main);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
