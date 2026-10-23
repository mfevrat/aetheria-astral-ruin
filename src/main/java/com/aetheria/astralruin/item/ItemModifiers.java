package com.aetheria.astralruin.item;

import java.util.UUID;

/** UUID fixes des modificateurs d'attributs du mod (ne jamais les changer apres publication). */
public final class ItemModifiers {
    public static final UUID STAFF_POWER = UUID.fromString("5b6a1f0e-8d0c-4a7e-9d43-0c1f7a2b9e01");
    public static final UUID STAFF_MANA = UUID.fromString("5b6a1f0e-8d0c-4a7e-9d43-0c1f7a2b9e02");
    public static final UUID BLADE_POWER = UUID.fromString("5b6a1f0e-8d0c-4a7e-9d43-0c1f7a2b9e03");
    public static final UUID SET_BONUS_CAST_SPEED = UUID.fromString("5b6a1f0e-8d0c-4a7e-9d43-0c1f7a2b9e04");
    public static final UUID[] ARMOR = {
            UUID.fromString("5b6a1f0e-8d0c-4a7e-9d43-0c1f7a2b9e10"), // bottes
            UUID.fromString("5b6a1f0e-8d0c-4a7e-9d43-0c1f7a2b9e11"), // jambieres
            UUID.fromString("5b6a1f0e-8d0c-4a7e-9d43-0c1f7a2b9e12"), // plastron
            UUID.fromString("5b6a1f0e-8d0c-4a7e-9d43-0c1f7a2b9e13")  // casque
    };

    private ItemModifiers() {}
}
