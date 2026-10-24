package com.aetheria.astralruin.client.render;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.client.vfx.VfxRender;
import com.aetheria.astralruin.entity.spell.SolarHaloEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

/** Six soleils en orbite, positionnes sur la position interpolee du lanceur (mouvement fluide). */
public class SolarHaloRenderer extends EntityRenderer<SolarHaloEntity> {
    private static final ResourceLocation FLARE = Aetheria.id("textures/vfx/flare.png");
    private static final ResourceLocation RING = Aetheria.id("textures/vfx/shockwave.png");

    public SolarHaloRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(@NotNull SolarHaloEntity e, float yaw, float pt, PoseStack pose, MultiBufferSource buffers, int light) {
        Entity owner = e.owner();
        Vec3 offset = owner == null ? Vec3.ZERO : owner.getPosition(pt).subtract(e.getPosition(pt));
        float t = e.tickCount + pt;
        float fade = Mth.clamp(Math.min(t / 8F, (e.maxLife() - t) / 10F), 0F, 1F);
        pose.pushPose();
        pose.translate(offset.x, offset.y + 1.1, offset.z);
        VfxRender.plane(buffers.getBuffer(RenderType.eyes(RING)), pose.last(), new Vector3f(), new Vector3f(1, 0, 0),
                new Vector3f(0, 0, 1), SolarHaloEntity.ORBIT * 1.15F, 0.6F * fade, 0.35F * fade, 0.1F * fade);
        for (int i = 0; i < SolarHaloEntity.ORBS; i++) {
            float a = SolarHaloEntity.orbAngle(i, t);
            pose.pushPose();
            pose.translate(Mth.cos(a) * SolarHaloEntity.ORBIT, Mth.sin(t * 0.2F + i) * 0.15F, Mth.sin(a) * SolarHaloEntity.ORBIT);
            pose.mulPose(entityRenderDispatcher.cameraOrientation());
            float s = (0.55F + 0.08F * Mth.sin(t * 0.7F + i)) * fade;
            VfxRender.plane(buffers.getBuffer(RenderType.eyes(FLARE)), pose.last(), new Vector3f(), new Vector3f(1, 0, 0),
                    new Vector3f(0, 1, 0), s, 1F, 0.75F, 0.35F);
            pose.popPose();
        }
        pose.popPose();
    }

    @Override
    public boolean shouldRender(@NotNull SolarHaloEntity e, @NotNull Frustum f, double x, double y, double z) {
        return true;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull SolarHaloEntity e) {
        return FLARE;
    }
}
