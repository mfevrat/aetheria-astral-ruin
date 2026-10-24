package com.aetheria.astralruin.client.render;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.client.vfx.VfxRender;
import com.aetheria.astralruin.entity.spell.AstralBeamEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

/** Pilier celeste : trois cylindres textures qui defilent en sens contraires, coeur blanc, onde au sol et eclat. */
public class AstralBeamRenderer extends EntityRenderer<AstralBeamEntity> {
    private static final ResourceLocation PILLAR = Aetheria.id("textures/vfx/pillar.png");
    private static final ResourceLocation BEAM = Aetheria.id("textures/vfx/beam.png");
    private static final ResourceLocation RING = Aetheria.id("textures/vfx/shockwave.png");
    private static final ResourceLocation FLARE = Aetheria.id("textures/vfx/flare.png");

    public AstralBeamRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(AstralBeamEntity beam, float yaw, float pt, PoseStack pose, MultiBufferSource buffers, int light) {
        float age = beam.tickCount + pt;
        float grow = Mth.clamp(age / 6F, 0F, 1F);
        float shrink = Mth.clamp((beam.getMaxLife() - age) / 10F, 0F, 1F);
        float r = beam.getRadius() * grow * shrink;
        if (r <= 0.01F) return;
        float h = beam.getBeamHeight();
        int c = beam.getColor();
        float cr = (c >> 16 & 0xFF) / 255F, cg = (c >> 8 & 0xFF) / 255F, cb = (c & 0xFF) / 255F;
        float pulse = 1F + 0.06F * Mth.sin(age * 0.6F);
        Vector3f base = new Vector3f();

        VertexConsumer pillar = buffers.getBuffer(RenderType.eyes(PILLAR));
        VfxRender.cylinder(pillar, pose.last(), base, r * pulse, h, 20, -age * 0.04F, h / 8F, cr * 0.8F, cg * 0.8F, cb * 0.8F);
        VertexConsumer beamTex = buffers.getBuffer(RenderType.eyes(BEAM));
        VfxRender.cylinder(beamTex, pose.last(), base, r * 0.7F, h, 16, age * 0.07F, h / 5F, cr, cg, cb);
        VfxRender.cylinder(pillar, pose.last(), base, r * 0.3F, h, 12, -age * 0.12F, h / 4F, 1F, 1F, 1F);

        float ring = r * (2.6F + 0.4F * Mth.sin(age * 0.25F));
        VfxRender.plane(buffers.getBuffer(RenderType.eyes(RING)), pose.last(), new Vector3f(0, 0.06F, 0),
                new Vector3f(1, 0, 0), new Vector3f(0, 0, 1), ring, cr, cg, cb);
        pose.pushPose();
        pose.translate(0, 0.3, 0);
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        VfxRender.plane(buffers.getBuffer(RenderType.eyes(FLARE)), pose.last(), new Vector3f(), new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0), r * 2.2F * pulse, 1F, 1F, 1F);
        pose.popPose();
    }

    @Override
    public boolean shouldRender(@NotNull AstralBeamEntity entity, @NotNull Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull AstralBeamEntity entity) {
        return PILLAR;
    }
}
