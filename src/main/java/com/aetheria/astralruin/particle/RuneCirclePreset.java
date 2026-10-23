package com.aetheria.astralruin.particle;

import java.util.List;

/**
 * Definition geometrique des cercles runiques, partagee client/serveur.
 * Le serveur n'envoie que l'index du preset, le client reconstruit la geometrie.
 */
public enum RuneCirclePreset {
    //                         type            rayon  n   k   spin     wobble  couleur
    JUDGEMENT(false, List.of(
            new Ring(Ring.Kind.CIRCLE, 1.00F, 0, 0, 0.010F, 0.000F, 0xFFFFFF),
            new Ring(Ring.Kind.STAR, 0.95F, 8, 3, -0.015F, 0.000F, 0xFFD36B),
            new Ring(Ring.Kind.RUNES, 0.80F, 16, 0, 0.025F, 0.000F, 0xFFD36B),
            new Ring(Ring.Kind.CIRCLE, 0.55F, 0, 0, -0.030F, 0.020F, 0x9B5CFF),
            new Ring(Ring.Kind.STAR, 0.50F, 6, 2, 0.040F, 0.020F, 0x9B5CFF))),
    NOVA(false, List.of(
            new Ring(Ring.Kind.CIRCLE, 1.00F, 0, 0, 0.08F, 0.0F, 0x9B5CFF),
            new Ring(Ring.Kind.STAR, 0.90F, 6, 2, -0.12F, 0.0F, 0xD9B8FF),
            new Ring(Ring.Kind.RUNES, 0.65F, 6, 0, 0.15F, 0.0F, 0xFFD36B))),
    AURA(false, List.of(
            new Ring(Ring.Kind.CIRCLE, 1.00F, 0, 0, 0.05F, 0.0F, 0xFFD36B),
            new Ring(Ring.Kind.RUNES, 0.75F, 8, 0, -0.05F, 0.0F, 0x9B5CFF))),
    TRAP(false, List.of(
            new Ring(Ring.Kind.CIRCLE, 1.00F, 0, 0, 0.06F, 0.0F, 0xB03CFF),
            new Ring(Ring.Kind.STAR, 0.95F, 5, 2, 0.06F, 0.0F, 0xB03CFF))),
    METEOR(false, List.of(
            new Ring(Ring.Kind.CIRCLE, 1.00F, 0, 0, 0.05F, 0.0F, 0xFFB347),
            new Ring(Ring.Kind.STAR, 0.95F, 4, 1, -0.08F, 0.0F, 0xFFD36B))),
    TELEGRAPH(false, List.of(
            new Ring(Ring.Kind.CIRCLE, 1.00F, 0, 0, 0.04F, 0.0F, 0xFFE9A8),
            new Ring(Ring.Kind.RUNES, 0.80F, 8, 0, -0.06F, 0.0F, 0xFFD36B))),
    TRANSITION(false, List.of(
            new Ring(Ring.Kind.CIRCLE, 1.00F, 0, 0, 0.02F, 0.0F, 0x9B5CFF),
            new Ring(Ring.Kind.STAR, 0.92F, 7, 3, -0.03F, 0.0F, 0xC08CFF),
            new Ring(Ring.Kind.RUNES, 0.75F, 12, 0, 0.04F, 0.015F, 0xFFD36B),
            new Ring(Ring.Kind.CIRCLE, 0.45F, 0, 0, -0.06F, 0.030F, 0xFFFFFF))),
    BARRIER(false, List.of(
            new Ring(Ring.Kind.CIRCLE, 1.00F, 0, 0, 0.10F, 0.0F, 0x9B5CFF),
            new Ring(Ring.Kind.RUNES, 0.82F, 8, 0, -0.12F, 0.0F, 0xFFD36B))),
    SHOCKWAVE(true, List.of(
            new Ring(Ring.Kind.CIRCLE, 1.00F, 0, 0, 0.0F, 0.0F, 0xFFFFFF),
            new Ring(Ring.Kind.CIRCLE, 0.85F, 0, 0, 0.0F, 0.0F, 0xFFD36B)));

    /** true : le cercle s'etend lineairement de 0 au rayon pendant toute sa duree (onde de choc). */
    public final boolean expanding;
    public final List<Ring> rings;

    RuneCirclePreset(boolean expanding, List<Ring> rings) {
        this.expanding = expanding;
        this.rings = rings;
    }

    /**
     * @param spin   rotation propre autour de la normale, en rad/tick
     * @param wobble vitesse de bascule sur les axes locaux X et Z (0 = anneau a plat)
     */
    public record Ring(Kind kind, float radiusFactor, int sides, int step, float spin, float wobble, int color) {
        public enum Kind { CIRCLE, STAR, RUNES }
    }
}
