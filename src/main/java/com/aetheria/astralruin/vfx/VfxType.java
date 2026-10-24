package com.aetheria.astralruin.vfx;

/** Effets visuels rendus cote client (textures additives dans textures/vfx/). */
public enum VfxType {
    /** Arc de lame : a = centre, b = direction, param = roulis (degres), size = rayon. */
    SLASH,
    /** Eclair a fourches de a vers b, size = largeur. */
    LIGHTNING,
    /** Onde de choc horizontale qui s'etend jusqu'a size. */
    RING,
    /** Anneau qui se contracte de size vers 0 (aspiration). */
    IMPLOSION,
    /** Faisceau d'energie de a vers b, size = largeur. */
    BEAM,
    /** Pilier vertical depuis a, param = hauteur, size = rayon. */
    PILLAR,
    /** Eclat lumineux en a, size = taille. */
    FLARE,
    /** Chaine lumineuse entre deux entites (entityA, entityB), size = largeur. */
    TETHER
}
