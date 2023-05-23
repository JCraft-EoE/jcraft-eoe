package net.arna.jcraft.common.util;

import net.minecraft.entity.LivingEntity;

public interface IComboCounter {
    LivingEntity getLastAttacked();

    void setLastAttacked(LivingEntity l);

    int getComboCount();

    void setComboCount(int i);

    void incrementComboCount();
}
