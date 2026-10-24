package com.aetheria.astralruin.registry;

import com.aetheria.astralruin.Aetheria;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Aetheria.MODID);

    public static final RegistryObject<SoundEvent> CAST_CHARGE = reg("spell.cast_charge");
    public static final RegistryObject<SoundEvent> CRYSTAL_RESONANCE = reg("spell.crystal_resonance");
    public static final RegistryObject<SoundEvent> JUDGEMENT_IMPACT = reg("spell.judgement_impact");
    public static final RegistryObject<SoundEvent> NOVA_BURST = reg("spell.nova_burst");
    public static final RegistryObject<SoundEvent> RUNE_EXPLODE = reg("spell.rune_explode");
    public static final RegistryObject<SoundEvent> VALERIUS_TRANSITION = reg("entity.valerius.transition");
    public static final RegistryObject<SoundEvent> VALERIUS_THEME = reg("music.valerius");
    public static final RegistryObject<SoundEvent> SLASH = reg("spell.slash");
    public static final RegistryObject<SoundEvent> THUNDER = reg("spell.thunder");
    public static final RegistryObject<SoundEvent> CHAINS = reg("spell.chains");
    public static final RegistryObject<SoundEvent> GRAVITY = reg("spell.gravity");
    public static final RegistryObject<SoundEvent> METEOR_FALL = reg("spell.meteor_fall");
    public static final RegistryObject<SoundEvent> MINE_ARM = reg("spell.mine_arm");
    public static final RegistryObject<SoundEvent> VALERIUS_ROAR = reg("entity.valerius.roar");
    public static final RegistryObject<SoundEvent> VALERIUS_SLAM = reg("entity.valerius.slam");

    private static RegistryObject<SoundEvent> reg(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Aetheria.id(name)));
    }

    private ModSounds() {}
}
