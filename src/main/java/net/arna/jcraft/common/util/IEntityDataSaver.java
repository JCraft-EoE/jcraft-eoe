package net.arna.jcraft.common.util;

import net.arna.jcraft.common.entity.StandEntity;
import net.minecraft.nbt.NbtCompound;

public interface IEntityDataSaver {
    NbtCompound getPersistentData();
    void setStand(StandEntity standEntity);
    StandEntity getStand();
}