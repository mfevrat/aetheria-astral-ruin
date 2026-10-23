package com.aetheria.astralruin.registry;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.block.entity.AstralAltarBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Aetheria.MODID);

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<AstralAltarBlockEntity>> ASTRAL_ALTAR =
            BLOCK_ENTITIES.register("astral_altar", () ->
                    BlockEntityType.Builder.of(AstralAltarBlockEntity::new, ModBlocks.ASTRAL_ALTAR.get()).build(null));

    private ModBlockEntities() {}
}
