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

    // ---------- Sauvegarde de secours dans les donnees persistantes (copiees par Forge apres la mort) ----------
    private static final String PERSISTED = "PlayerPersisted";
    private static final String BACKUP_KEY = "aetheria_mana";

    public static void backup(Player player, ManaData data) {
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.getCompound(PERSISTED);
        persisted.put(BACKUP_KEY, data.save());
        root.put(PERSISTED, persisted);
    }

    /** @return la sauvegarde de secours, ou null s'il n'y en a pas */
    public static @Nullable CompoundTag readBackup(Player player) {
        CompoundTag persisted = player.getPersistentData().getCompound(PERSISTED);
        return persisted.contains(BACKUP_KEY) ? persisted.getCompound(BACKUP_KEY) : null;
    }

    public static class Provider implements ICapabilitySerializable<CompoundTag> {
        private final ManaData data = new ManaData();
        private LazyOptional<ManaData> optional = LazyOptional.of(() -> data);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            // Forge invalide les capabilities a la mort puis appelle reviveCaps() pour la copie (PlayerEvent.Clone),
            // mais une LazyOptional invalidee ne redevient jamais valide : on la recree si besoin.
            if (!optional.isPresent()) optional = LazyOptional.of(() -> data);
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
