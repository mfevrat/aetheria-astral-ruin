package com.aetheria.astralruin.event;

import com.aetheria.astralruin.Aetheria;
import com.aetheria.astralruin.entity.boss.ValeriusEntity;
import com.aetheria.astralruin.entity.boss.ValeriusOrbEntity;
import com.aetheria.astralruin.mana.ManaData;
import com.aetheria.astralruin.registry.ModAttributes;
import com.aetheria.astralruin.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Aetheria.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModBusEvents {

    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(ManaData.class);
    }

    @SubscribeEvent
    public static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.VALERIUS.get(), ValeriusEntity.createAttributes().build());
        event.put(ModEntities.VALERIUS_ORB.get(), ValeriusOrbEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void addPlayerAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, ModAttributes.MAX_MANA.get());
        event.add(EntityType.PLAYER, ModAttributes.MANA_REGEN.get());
        event.add(EntityType.PLAYER, ModAttributes.SPELL_POWER.get());
        event.add(EntityType.PLAYER, ModAttributes.CAST_SPEED.get());
        event.add(EntityType.PLAYER, ModAttributes.BACKLASH_RESISTANCE.get());
    }

    private ModBusEvents() {}
}
