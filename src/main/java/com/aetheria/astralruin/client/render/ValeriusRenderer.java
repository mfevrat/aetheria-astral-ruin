package com.aetheria.astralruin.client.render;

import com.aetheria.astralruin.entity.boss.ValeriusEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class ValeriusRenderer extends GeoEntityRenderer<ValeriusEntity> {
    public ValeriusRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ValeriusModel());
        addRenderLayer(new AutoGlowingGeoLayer<>(this)); // textures/entity/valerius_glowmask.png
        this.shadowRadius = 1.3F;
    }

    /** Les ailes de lumiere n'existent qu'a partir de la phase 2. */
    @Override
    public void preRender(PoseStack poseStack, ValeriusEntity entity, BakedGeoModel model, MultiBufferSource bufferSource,
                          VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {
        boolean hideWings = entity.getPhase() < 2 || entity.isIntro();
        model.getBone("wings").ifPresent(b -> b.setHidden(hideWings));
        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay,
                red, green, blue, alpha);
    }

    @Override
    protected float getDeathMaxRotation(ValeriusEntity entity) {
        return 0F; // l'animation de mort remplace la chute laterale vanilla
    }
}
