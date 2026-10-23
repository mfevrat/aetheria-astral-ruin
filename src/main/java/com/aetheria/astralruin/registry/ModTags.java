package com.aetheria.astralruin.registry;

import com.aetheria.astralruin.Aetheria;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class ModTags {
    public static final TagKey<Block> JUDGEMENT_IMMUNE =
            TagKey.create(Registries.BLOCK, Aetheria.id("judgement_immune"));
    public static final TagKey<Structure> OBSERVATORIES =
            TagKey.create(Registries.STRUCTURE, Aetheria.id("observatories"));

    private ModTags() {}
}
