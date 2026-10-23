package com.aetheria.astralruin.mana;

import com.aetheria.astralruin.Aetheria;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class ManaCapability {
    public static final Capability<ManaData> MANA = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation KEY = Aetheria.id("mana");

    public static Optional<ManaData> get(Player player) {
        return player.getCapability(MANA).resolve();
    }

    public static class Provider implements ICapabilitySerializable<CompoundTag> {
        private final ManaData data = new ManaData();
        private final LazyOptional<ManaData> optional = LazyOptional.of(() -> data);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return MANA.orEmpty(cap, optional);
        }

        @Override
        public CompoundTag serializeNBT() {
            return data.save();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            data.load(nbt);
        }

        public void invalidate() {
            optional.invalidate();
        }
    }

    private ManaCapability() {}
}
