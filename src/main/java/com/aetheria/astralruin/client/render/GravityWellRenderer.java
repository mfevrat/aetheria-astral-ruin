package com.aetheria.astralruin.client.render;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.client.vfx.VfxRender;
import com.aetheria.astralruin.entity.spell.GravityWellEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

/** Trou noir : disque sombre translucide qui tourne, couronne additive et anneau d'accretion. */
public class GravityWellRenderer extends EntityRenderer<GravityWellEntity> {
    private static final ResourceLocation CORE = Aetheria.id("textures/vfx/void_core.png");
    private static final ResourceLocation FLARE = Aetheria.id("textures/vfx/flare.png");
    private static final ResourceLocation RING = Aetheria.id("textures/vfx/shockwave.png");

    public GravityWellRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(@NotNull GravityWellEntity e, float yaw, float pt, PoseStack pose, MultiBufferSource buffers, int light) {
        float t = e.tickCount + pt;
        float grow = Mth.clamp(t / 10F, 0F, 1F);
        float collapse = Mth.clamp((e.maxLife() - t) / 6F, 0.15F, 1F);
        float size = (1.4F + e.radius() * 0.12F) * grow * collapse;
        // anneau d'accretion horizontal
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(t * 8F));
        VfxRender.plane(buffers.getBuffer(RenderType.eyes(RING)), pose.last(), new Vector3f(),
                new Vector3f(1, 0, 0), new Vector3f(0, 0, 1), size * 2.2F, 0.55F, 0.3F, 0.9F);
        pose.popPose();
        // disque et couronne face camera
        pose.pushPose();
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.mulPose(Axis.ZP.rotationDegrees(-t * 10F));
        VfxRender.plane(buffers.getBuffer(RenderType.eyes(FLARE)), pose.last(), new Vector3f(),
                new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), size * 1.9F, 0.6F, 0.35F, 1F);
        VfxRender.plane(buffers.getBuffer(RenderType.entityTranslucent(CORE)), pose.last(), new Vector3f(0, 0, 0.01F),
                new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), size, 1F, 1F, 1F);
        pose.popPose();
    }

    @Override
    public boolean shouldRender(@NotNull GravityWellEntity e, @NotNull Frustum f, double x, double y, double z) {
        return true;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull GravityWellEntity e) {
        return CORE;
    }
}
