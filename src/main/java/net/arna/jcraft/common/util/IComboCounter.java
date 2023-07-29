package net.arna.jcraft.common.util;

import net.minecraft.entity.LivingEntity;

public interface IComboCounter {
    LivingEntity getLastAttacked();

    void setLastAttacked(LivingEntity l);

    int jcraft$getComboCount();

    void jcraft$setComboCount(int i);

    void jcraft$incrementComboCount();
}
