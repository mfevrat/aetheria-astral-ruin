package com.aetheria.astralruin.event;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.entity.spell.EclipseSlashEntity;
import com.aetheria.astralruin.item.AstralArmorItem;
import com.aetheria.astralruin.item.EclipseBladeItem;
import com.aetheria.astralruin.item.EclipseRampartItem;
import com.aetheria.astralruin.item.ItemModifiers;
import com.aetheria.astralruin.mana.ManaCapability;
import com.aetheria.astralruin.mana.ManaEvents;
import com.aetheria.astralruin.registry.ModAttributes;
import com.aetheria.astralruin.registry.ModSounds;
import com.aetheria.astralruin.util.Fx;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Aetheria.MODID)
public final class CommonEvents {
    private static final AttributeModifier SET_BONUS = new AttributeModifier(ItemModifiers.SET_BONUS_CAST_SPEED,
            "Astral set bonus", 2.0 / 3.0, AttributeModifier.Operation.MULTIPLY_TOTAL);

    /** Lame d'Eclipse : un coup critique projette une lame de losanges (8 mana). */
    @SubscribeEvent
    public static void onCritical(CriticalHitEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !event.isVanillaCritical()) return;
        if (!(player.getMainHandItem().getItem() instanceof EclipseBladeItem blade)) return;
        if (!(player instanceof ServerPlayer sp)) return;
        ManaCapability.get(sp).ifPresent(data -> {
            if (data.getMana() < 8F) return;
            data.setMana(data.getMana() - 8F);
            data.recordSpent(8F);
            EclipseSlashEntity slash = new EclipseSlashEntity(sp.level(), sp, blade.slashDamage() * data.spellPower(sp),
                    blade.slashPierce(), Fx.PINK)
                    .withMaxLife(8);
            slash.shootFromRotation(sp, sp.getXRot(), sp.getYRot(), 0F, 1.5F, 0F);
            sp.level().addFreshEntity(slash);
            sp.level().playSound(null, sp.blockPosition(), ModSounds.CRYSTAL_RESONANCE.get(), SoundSource.PLAYERS, 0.6F, 1.9F);
        });
    }

    /** Faim d'Eclipse : chaque coup de Lame d'Eclipse rend de la mana. */
    @SubscribeEvent
    public static void onMeleeHit(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer sp) || source.getDirectEntity() != sp) return;
        if (!(sp.getMainHandItem().getItem() instanceof EclipseBladeItem)) return;
        ManaCapability.get(sp).ifPresent(data -> {
            float leech = data.manaLeech(sp);
            if (leech <= 0) return;
            data.setMana(Math.min(data.maxMana(sp), data.getMana() + leech));
            Fx.burst((ServerLevel) sp.level(), event.getEntity().position().add(0, event.getEntity().getBbHeight() * 0.6, 0),
                    0x7A5CFF, 6, 0.1F);
        });
    }

    /** Rempart d'Eclipse : 50 % des degats bloques renvoyes + onde de choc runique. */
    @SubscribeEvent
    public static void onShieldBlock(ShieldBlockEvent event) {
        LivingEntity blocker = event.getEntity();
        if (blocker.level().isClientSide() || !(blocker.getUseItem().getItem() instanceof EclipseRampartItem)) return;
        Entity attacker = event.getDamageSource().getEntity();
        float reflected = event.getBlockedDamage() * 0.5F;
        ServerLevel level = (ServerLevel) blocker.level();
        if (attacker instanceof LivingEntity living && attacker != blocker && reflected > 0) {
            living.hurt(blocker.damageSources().indirectMagic(blocker, blocker), reflected);
            Vec3 away = living.position().subtract(blocker.position()).multiply(1, 0, 1).normalize();
            living.knockback(0.6, -away.x, -away.z);
        }
        Vec3 front = blocker.position().add(blocker.getLookAngle().multiply(1, 0, 1).normalize().scale(0.8)).add(0, 1, 0);
        Fx.ring(level, blocker.position().add(0, 0.1, 0), 3.0, Fx.VIOLET, 28);
        Fx.burst(level, front, Fx.PINK, 14, 0.2F);
    }

    /** Bonus de set Astral : -40 % de temps d'incantation. */
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) return;
        if (event.getSlot().getType() != EquipmentSlot.Type.ARMOR) return;
        AttributeInstance castSpeed = player.getAttribute(ModAttributes.CAST_SPEED.get());
        if (castSpeed == null) return;
        boolean full = AstralArmorItem.hasFullSet(player);
        boolean has = castSpeed.getModifier(ItemModifiers.SET_BONUS_CAST_SPEED) != null;
        if (full && !has) castSpeed.addTransientModifier(SET_BONUS);
        else if (!full && has) castSpeed.removeModifier(ItemModifiers.SET_BONUS_CAST_SPEED);
    }

    /** Recompenses des quetes (advancements). */
    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        ResourceLocation id = event.getAdvancement().id();
        if (!Aetheria.MODID.equals(id.getNamespace())) return;
        ManaCapability.get(sp).ifPresent(data -> {
            switch (id.getPath()) {
                case "awakening" -> {
                    data.addBonusMaxMana(20);
                    data.addPoints(1);
                    data.setTitleKey("title.aetheria.awakened");
                }
                case "invoker" -> {
                    data.addBonusMaxMana(30);
                    data.addPoints(2);
                }
                case "awakened" -> {
                    data.addBonusMaxMana(50);
                    data.addPoints(2);
                    data.setTitleKey("title.aetheria.ascendant");
                }
                case "guardian_trial" -> {
                    data.addBonusMaxMana(50);
                    data.addPoints(3);
                    data.setTitleKey("title.aetheria.guardian");
                }
                default -> {
                    ManaEvents.sync(sp); // met a jour le Journal d'Aventure
                    return;
                }
            }
            sp.displayClientMessage(Component.translatable("message.aetheria.quest_reward")
                    .withStyle(ChatFormatting.GOLD), false);
            ManaEvents.sync(sp);
        });
    }

    /** Affiche le titre debloque devant le pseudo (chat, liste des joueurs). */
    @SubscribeEvent
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        if (event.getEntity().level().isClientSide()) return;
        ManaCapability.get(event.getEntity()).ifPresent(data -> {
            if (data.getTitleKey().isEmpty()) return;
            event.setDisplayname(Component.literal("[")
                    .append(Component.translatable(data.getTitleKey()))
                    .append("] ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(event.getDisplayname().copy().withStyle(ChatFormatting.RESET)));
        });
    }

    private CommonEvents() {}
}
