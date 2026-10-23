package com.aetheria.astralruin.client.render;

import com.aetheria.astralruin.entity.boss.ValeriusEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class ValeriusRenderer extends GeoEntityRenderer<ValeriusEntity> {
    public ValeriusRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ValeriusModel());
        addRenderLayer(new AutoGlowingGeoLayer<>(this)); // textures/entity/valerius_glowmask.png
        this.shadowRadius = 1.2F;
    }

    @Override
    protected float getDeathMaxRotation(ValeriusEntity entity) {
        return 0F; // l'animation de mort remplace la chute laterale vanilla
    }
}
