package com.aetheria.astralruin.client.render;

import com.aetheria.astralruin.entity.boss.ValeriusOrbEntity;
import com.aetheria.astralruin.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/** Orbe de soutien : un grand cristal de mana qui tourne et flotte. */
public class ValeriusOrbRenderer extends EntityRenderer<ValeriusOrbEntity> {
    private final ItemRenderer itemRenderer;
    private ItemStack stack = ItemStack.EMPTY;

    public ValeriusOrbRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.itemRenderer = ctx.getItemRenderer();
        this.shadowRadius = 0.3F;
    }

    @Override
    public void render(ValeriusOrbEntity orb, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        if (stack.isEmpty()) stack = new ItemStack(ModItems.MANA_CRYSTAL.get());
        float t = orb.tickCount + partialTick;
        pose.pushPose();
        pose.translate(0, 0.45 + Mth.sin(t * 0.1F) * 0.12, 0);
        pose.mulPose(Axis.YP.rotationDegrees(t * 4F));
        float hurt = orb.hurtTime > 0 ? 1.15F : 1F;
        pose.scale(1.8F * hurt, 1.8F * hurt, 1.8F * hurt);
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, pose, buffers, orb.level(), orb.getId());
        pose.popPose();
        super.render(orb, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ValeriusOrbEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
