package com.aetheria.astralruin.spell;

import net.minecraft.world.phys.Vec3;

/** Donnees d'une incantation en cours. */
public class CastContext {
    public Vec3 origin = Vec3.ZERO;
    public Vec3 look = new Vec3(0, 0, 1);
    public Vec3 target = Vec3.ZERO;
    /** Temps d'incantation effectif (apres cast_speed). */
    public int castTicks;
    public float power = 1F;
}
