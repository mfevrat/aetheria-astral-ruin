package com.aetheria.astralruin.registry;

import com.aetheria.astralruin.Aetheria;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Aetheria.MODID);

    public static final RegistryObject<Attribute> MAX_MANA = reg("max_mana", 100, 0, 2000);
    public static final RegistryObject<Attribute> MANA_REGEN = reg("mana_regen", 1.5, 0, 50);
    public static final RegistryObject<Attribute> SPELL_POWER = reg("spell_power", 1.0, 0, 10);
    public static final RegistryObject<Attribute> CAST_SPEED = reg("cast_speed", 1.0, 0.1, 5);
    public static final RegistryObject<Attribute> BACKLASH_RESISTANCE = reg("backlash_resistance", 0, 0, 1);

    private static RegistryObject<Attribute> reg(String name, double def, double min, double max) {
        return ATTRIBUTES.register(name,
                () -> new RangedAttribute("attribute." + Aetheria.MODID + "." + name, def, min, max).setSyncable(true));
    }

    private ModAttributes() {}
}
