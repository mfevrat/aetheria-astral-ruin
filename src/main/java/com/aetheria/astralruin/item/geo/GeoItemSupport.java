package com.aetheria.astralruin.item.geo;

import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

/**
 * Tous les modeles 3D du mod partagent une animation en boucle "animation.aetheria.idle",
 * definie dans le fichier d'animation propre a chaque objet / bloc / armure.
 */
public final class GeoItemSupport {
    public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.aetheria.idle");

    public static <T extends GeoAnimatable> void idle(T animatable, AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(animatable, "idle", 0, state -> state.setAndContinue(IDLE)));
    }

    private GeoItemSupport() {}
}
