package com.aetheria.astralruin.client.render.geo;

import net.minecraft.world.item.Item;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** Rendu 3D anime d'un objet du mod, avec couche emissive (glowmask). */
public class AetheriaItemRenderer<T extends Item & GeoAnimatable> extends GeoItemRenderer<T> {
    public AetheriaItemRenderer(String name) {
        super(new AetheriaGeoModel<>("item", name));
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
