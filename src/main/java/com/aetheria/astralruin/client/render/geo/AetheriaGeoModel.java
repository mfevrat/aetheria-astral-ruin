package com.aetheria.astralruin.client.render.geo;

import com.aetheria.astralruin.Aetheria;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

/**
 * Modele GeckoLib aux chemins du mod :
 * geo/<dossier>/<nom>.geo.json, animations/<dossier>/<nom>.animation.json, textures/geo/<dossier>/<nom>.png
 * (+ <nom>_glowmask.png pour les parties lumineuses).
 */
public class AetheriaGeoModel<T extends GeoAnimatable> extends GeoModel<T> {
    private final ResourceLocation model;
    private final ResourceLocation texture;
    private final ResourceLocation animation;

    public AetheriaGeoModel(String folder, String name) {
        this.model = Aetheria.id("geo/" + folder + "/" + name + ".geo.json");
        this.texture = Aetheria.id("textures/geo/" + folder + "/" + name + ".png");
        this.animation = Aetheria.id("animations/" + folder + "/" + name + ".animation.json");
    }

    @Override
    public ResourceLocation getModelResource(T animatable) { return model; }

    @Override
    public ResourceLocation getTextureResource(T animatable) { return texture; }

    @Override
    public ResourceLocation getAnimationResource(T animatable) { return animation; }
}
