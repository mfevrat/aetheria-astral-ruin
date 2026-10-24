package com.aetheria.astralruin.client.render;

import com.aetheria.astralruin.entity.spell.SpectralBladeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Lame spectrale : le modele 3D anime de l'objet, pointe orientee vers la cible ou la trajectoire. */
public class SpectralBladeRenderer extends EntityRenderer<SpectralBladeEntity> {
    private final ItemRenderer items;

    public SpectralBladeRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.items = ctx.getItemRenderer();
    }

    @Override
    public void render(@NotNull SpectralBladeEntity e, float yaw, float pt, PoseStack pose, MultiBufferSource buffers, int light) {
        Vec3 dir = e.getDeltaMovement();
        if (dir.lengthSqr() < 1e-3) {
            Entity target = e.aimTarget();
            dir = target != null ? target.getEyePosition(pt).subtract(e.getPosition(pt)) : new Vec3(0, -1, 0);
        }
        if (dir.lengthSqr() < 1e-6) dir = new Vec3(0, -1, 0);
        dir = dir.normalize();
        pose.pushPose();
        pose.translate(0, e.getBbHeight() * 0.5, 0);
        pose.mulPose(new Quaternionf().rotationTo(new Vector3f(0, 1, 0), new Vector3f((float) dir.x, (float) dir.y, (float) dir.z)));
        pose.scale(1.6F, 1.6F, 1.6F);
        items.renderStatic(e.getItem(), ItemDisplayContext.NONE, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                pose, buffers, e.level(), e.getId());
        pose.popPose();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull SpectralBladeEntity e) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
