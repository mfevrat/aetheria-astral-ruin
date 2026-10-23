package com.aetheria.astralruin.registry;

import com.aetheria.astralruin.Aetheria;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Aetheria.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.aetheria"))
            .icon(() -> ModItems.ARCHMAGE_STAFF.get().getDefaultInstance())
            .displayItems((params, out) -> {
                out.accept(ModItems.ASTRAL_SHARD.get());
                out.accept(ModItems.ASTRAL_INGOT.get());
                out.accept(ModItems.STELE_FRAGMENT.get());
                out.accept(ModItems.MANA_CRYSTAL.get());
                out.accept(ModItems.ASTRAL_SIGIL.get());
                out.accept(ModItems.RUNE_HEART.get());
                out.accept(ModItems.ASTRAL_GRIMOIRE.get());
                out.accept(ModItems.ARCHMAGE_STAFF.get());
                out.accept(ModItems.ECLIPSE_BLADE.get());
                out.accept(ModItems.AWAKENED_STAFF.get());
                out.accept(ModItems.AWAKENED_BLADE.get());
                out.accept(ModItems.ASTRAL_HELMET.get());
                out.accept(ModItems.ASTRAL_CHESTPLATE.get());
                out.accept(ModItems.ASTRAL_LEGGINGS.get());
                out.accept(ModItems.ASTRAL_BOOTS.get());
                out.accept(ModItems.ECLIPSE_RAMPART.get());
                out.accept(ModItems.ASTRAL_ALTAR.get());
                out.accept(ModItems.VALERIUS_SPAWN_EGG.get());
            })
            .build());

    private ModCreativeTabs() {}
}
