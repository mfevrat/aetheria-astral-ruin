package com.aetheria.astralruin.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Point de vue des cinematiques : cree cote client, jamais ajoute au monde ni sauvegarde. */
public class CinematicCameraEntity extends Entity {
    public CinematicCameraEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /** Place la camera et memorise l'ancienne position pour une interpolation fluide entre deux ticks. */
    public void moveTo(double x, double y, double z, float yaw, float pitch, boolean snap) {
        if (snap) {
            setPos(x, y, z);
            setYRot(yaw);
            setXRot(pitch);
            setOldPosAndRot();
        } else {
            setOldPosAndRot();
            setPos(x, y, z);
            setYRot(yaw);
            setXRot(pitch);
        }
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
