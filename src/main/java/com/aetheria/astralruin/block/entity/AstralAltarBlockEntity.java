package com.aetheria.astralruin.block.entity;

import com.aetheria.astralruin.item.geo.GeoItemSupport;
import com.aetheria.astralruin.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Porte le modele 3D anime de l'Autel Astral (anneaux et cristal en rotation). */
public class AstralAltarBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AstralAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASTRAL_ALTAR.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        GeoItemSupport.idle(this, controllers);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
