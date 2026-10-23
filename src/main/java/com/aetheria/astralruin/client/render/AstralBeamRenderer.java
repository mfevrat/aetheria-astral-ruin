package com.aetheria.astralruin.client.render;

import com.aetheria.astralruin.entity.spell.AstralBeamEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

/** Pilier celeste : reutilise le rendu du faisceau de balise, avec un rayon anime. */
public class AstralBeamRenderer extends EntityRenderer<AstralBeamEntity> {
    public AstralBeamRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(AstralBeamEntity beam, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        float age = beam.tickCount + partialTick;
        float grow = Mth.clamp(age / 6F, 0F, 1F);
        float shrink = Mth.clamp((beam.getMaxLife() - age) / 10F, 0F, 1F);
        float r = beam.getRadius() * grow * shrink;
        if (r <= 0.01F) return;

        int c = beam.getColor();
        float[] color = {(c >> 16 & 0xFF) / 255F, (c >> 8 & 0xFF) / 255F, (c & 0xFF) / 255F};
        float pulse = 1F + 0.06F * Mth.sin(age * 0.6F);

        pose.pushPose();
        pose.translate(-0.5, 0, -0.5); // renderBeaconBeam recentre de +0.5 en interne
        BeaconRenderer.renderBeaconBeam(pose, buffers, BeaconRenderer.BEAM_LOCATION, partialTick, 1.0F,
                beam.level().getGameTime(), 0, Mth.ceil(beam.getBeamHeight()), color, r * 0.55F * pulse, r * pulse);
        pose.popPose();
    }

    @Override
    public boolean shouldRender(@NotNull AstralBeamEntity entity, @NotNull Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull AstralBeamEntity entity) {
        return BeaconRenderer.BEAM_LOCATION;
    }
}
