package com.aetheria.astralruin.spell;

import com.aetheria.astralruin.item.EclipseBladeItem;
import com.aetheria.astralruin.item.StaffItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Base de tous les sorts. Toute la logique de gameplay tourne cote serveur ;
 * le client ne fait que dessiner a partir des paquets d'effets.
 */
public abstract class AbstractSpell {
    public enum Focus { STAFF, STAFF_OR_BLADE }

    private final String id;
    protected final int manaCost;
    protected final int castTicks;
    protected final int cooldownTicks;
    protected final Focus focus;

    protected AbstractSpell(String id, int manaCost, int castTicks, int cooldownTicks, Focus focus) {
        this.id = id;
        this.manaCost = manaCost;
        this.castTicks = castTicks;
        this.cooldownTicks = cooldownTicks;
        this.focus = focus;
    }

    public String id() { return id; }
    public int manaCost() { return manaCost; }
    public int baseCastTicks() { return castTicks; }
    public int cooldownTicks() { return cooldownTicks; }

    public boolean acceptsFocus(ItemStack mainHand) {
        if (mainHand.getItem() instanceof StaffItem) return true;
        return focus == Focus.STAFF_OR_BLADE && mainHand.getItem() instanceof EclipseBladeItem;
    }

    /**
     * Verification avant paiement (cible presente, etc.).
     * @return une cle de traduction d'erreur, ou null si le sort peut etre lance
     */
    public @org.jetbrains.annotations.Nullable String precheck(ServerPlayer caster) {
        return null;
    }

    /** Appele une fois, quand l'incantation commence (mana deja payee). */
    public void onCastStart(ServerPlayer caster, CastContext ctx) {}

    /**
     * Appele chaque tick a partir de tick = 0.
     * @return true pour continuer, false pour terminer l'incantation.
     */
    public abstract boolean onCastTick(ServerPlayer caster, CastContext ctx, int tick);

    public void onCastEnd(ServerPlayer caster, CastContext ctx, boolean interrupted) {}
}
