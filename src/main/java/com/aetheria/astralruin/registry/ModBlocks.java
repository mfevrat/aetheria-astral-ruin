package com.aetheria.astralruin.registry;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.block.AstralAltarBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Aetheria.MODID);

    public static final RegistryObject<Block> ASTRAL_ALTAR = BLOCKS.register("astral_altar",
            () -> new AstralAltarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .sound(SoundType.AMETHYST)
                    .noOcclusion()
                    .lightLevel(state -> 12)));

    private ModBlocks() {}
}
