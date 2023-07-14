package net.arna.jcraft.common.util;

import net.arna.jcraft.common.entity.StandEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;

public interface IEntityDataSaver {
    Vec3d getDesiredVelocity();
    void updateRemoteInputs(int f, int s, boolean j);
    NbtCompound getPersistentData();
    void setStand(StandEntity standEntity);
    StandEntity getStand();
}