package com.aetheria.astralruin.client.render;

import com.aetheria.astralruin.client.vfx.VfxRender;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

/** Sprite lumineux face camera (additif) avec pulsation : comete, eclats d'energie. */
public class GlowSpriteRenderer<T extends Entity> extends EntityRenderer<T> {
    private final ResourceLocation texture;
    private final float size;
    private final int color;

    public GlowSpriteRenderer(EntityRendererProvider.Context ctx, ResourceLocation texture, float size, int color) {
        super(ctx);
        this.texture = texture;
        this.size = size;
        this.color = color;
    }

    @Override
    public void render(@NotNull T entity, float yaw, float pt, PoseStack pose, MultiBufferSource buffers, int light) {
        float t = entity.tickCount + pt;
        float s = size * (0.9F + 0.12F * Mth.sin(t * 0.9F));
        pose.pushPose();
        pose.translate(0, entity.getBbHeight() * 0.5, 0);
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(t * 6F));
        float r = (color >> 16 & 0xFF) / 255F, g = (color >> 8 & 0xFF) / 255F, b = (color & 0xFF) / 255F;
        VfxRender.plane(buffers.getBuffer(RenderType.eyes(texture)), pose.last(), new Vector3f(),
                new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), s, r, g, b);
        pose.popPose();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return texture;
    }
}
