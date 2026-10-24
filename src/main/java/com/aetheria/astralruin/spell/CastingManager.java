package com.aetheria.astralruin.spell;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.mana.ManaCapability;
import com.aetheria.astralruin.mana.ManaData;
import com.aetheria.astralruin.mana.ManaEvents;
import com.aetheria.astralruin.registry.ModAttributes;
import com.aetheria.astralruin.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Gere les incantations en cours, cote serveur uniquement. */
public final class CastingManager {
    private static final Map<UUID, ActiveCast> ACTIVE = new HashMap<>();

    private static final class ActiveCast {
        final AbstractSpell spell;
        final CastContext ctx;
        int tick;

        ActiveCast(AbstractSpell spell, CastContext ctx) {
            this.spell = spell;
            this.ctx = ctx;
        }
    }

    public static boolean isCasting(ServerPlayer player) {
        return ACTIVE.containsKey(player.getUUID());
    }

    public static void tryCastSelected(ServerPlayer player) {
        ManaCapability.get(player).ifPresent(data -> tryCast(player, data, data.selectedSpellId()));
    }

    public static void tryCast(ServerPlayer player, ManaData data, String spellId) {
        if (player.isSpectator() || !player.isAlive()) return;
        AbstractSpell spell = SpellRegistry.get(spellId);
        if (spell == null) return;

        if (isCasting(player)) {
            fail(player, "message.aetheria.already_casting");
            return;
        }
        if (!data.isSpellUnlocked(spellId)) {
            fail(player, "message.aetheria.locked");
            return;
        }
        if (!spell.acceptsFocus(player.getMainHandItem())) {
            fail(player, spell.focus == AbstractSpell.Focus.STAFF
                    ? "message.aetheria.need_staff" : "message.aetheria.need_focus");
            return;
        }
        long now = player.level().getGameTime();
        int remaining = data.cooldownRemaining(spellId, now);
        if (remaining > 0 && !player.isCreative()) {
            player.displayClientMessage(Component.translatable("message.aetheria.cooldown",
                    String.format("%.1f", remaining / 20F)).withStyle(ChatFormatting.GRAY), true);
            return;
        }

        String problem = spell.precheck(player);
        if (problem != null) {
            fail(player, problem);
            return;
        }

        float max = data.maxMana(player);
        float costMult = player.getMainHandItem().getItem() instanceof com.aetheria.astralruin.item.StaffItem staff
                ? staff.costMultiplier() : 1F;
        float cost = player.isCreative() ? 0 : spell.manaCost() * costMult;
        float after = data.getMana() - cost;
        if (after < -0.2F * max) {
            fail(player, "message.aetheria.no_mana");
            return;
        }

        // Paiement + contrecoup eventuel
        data.setMana(after);
        data.recordSpent(cost);
        if (after < 0) applyBacklash(player, data, -after);
        data.setCooldown(spellId, now + spell.cooldownTicks());

        // Progression : 3 sorts distincts => advancement "La Voie de l'Invocateur"
        if (data.recordCast(spellId) >= 3) {
            AdvancementHolder adv = player.server.getAdvancements().get(Aetheria.id("invoker"));
            if (adv != null) player.getAdvancements().award(adv, "cast_three");
        }

        CastContext ctx = new CastContext();
        ctx.origin = player.getEyePosition();
        ctx.look = player.getLookAngle();
        ctx.power = data.spellPower(player);
        double speed = Math.max(0.1, player.getAttributeValue(ModAttributes.CAST_SPEED.get()));
        ctx.castTicks = Mth.ceil(spell.baseCastTicks() / speed);

        if (ctx.castTicks > 0) {
            player.level().playSound(null, player.blockPosition(), ModSounds.CAST_CHARGE.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        spell.onCastStart(player, ctx);
        ActiveCast cast = new ActiveCast(spell, ctx);
        if (spell.onCastTick(player, ctx, 0)) {
            ACTIVE.put(player.getUUID(), cast);
        } else {
            spell.onCastEnd(player, ctx, false);
        }
        ManaEvents.sync(player);
    }

    private static void applyBacklash(ServerPlayer player, ManaData data, float debt) {
        float damage = debt * 0.25F * (1F - data.backlashResistance(player));
        if (damage > 0) player.hurt(player.damageSources().magic(), damage);
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
        data.setRegenBlockedTicks(60);
        player.displayClientMessage(Component.translatable("message.aetheria.backlash")
                .withStyle(ChatFormatting.DARK_PURPLE), true);
    }

    /** Appele chaque tick serveur par joueur (voir ManaEvents). */
    public static void tick(ServerPlayer player) {
        ActiveCast cast = ACTIVE.get(player.getUUID());
        if (cast == null) return;
        if (!player.isAlive()) {
            ACTIVE.remove(player.getUUID());
            cast.spell.onCastEnd(player, cast.ctx, true);
            return;
        }
        cast.tick++;
        if (!cast.spell.onCastTick(player, cast.ctx, cast.tick)) {
            ACTIVE.remove(player.getUUID());
            cast.spell.onCastEnd(player, cast.ctx, false);
        }
    }

    public static void clear(UUID id) {
        ACTIVE.remove(id);
    }

    public static void clearAll() {
        ACTIVE.clear();
    }

    private static void fail(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.RED), true);
    }

    private CastingManager() {}
}
