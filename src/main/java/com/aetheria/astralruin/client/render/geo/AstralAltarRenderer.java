package com.aetheria.astralruin.client.render.geo;

import com.aetheria.astralruin.block.entity.AstralAltarBlockEntity;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class AstralAltarRenderer extends GeoBlockRenderer<AstralAltarBlockEntity> {
    public AstralAltarRenderer() {
        super(new AetheriaGeoModel<>("block", "astral_altar"));
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
