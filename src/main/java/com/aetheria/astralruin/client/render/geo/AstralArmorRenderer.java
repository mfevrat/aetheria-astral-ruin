package com.aetheria.astralruin.client.render.geo;

import com.aetheria.astralruin.item.AstralArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** Armure Astrale portee : un seul modele, GeckoLib affiche les os du bon emplacement. */
public class AstralArmorRenderer extends GeoArmorRenderer<AstralArmorItem> {
    public AstralArmorRenderer() {
        super(new AetheriaGeoModel<>("armor", "astral_armor"));
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
