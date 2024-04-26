package net.arna.jcraft.common.util;

import net.minecraft.entity.LivingEntity;

public interface IComboCounter {
    LivingEntity jcraft$getLastAttacked();

    void jcraft$setLastAttacked(LivingEntity l);

    int jcraft$getComboCount();

    /**
     * @return whether the victim was stunned at the start of the tick.
     */
    //boolean jcraft$wasStunned();

    void jcraft$setComboCount(int i);

    void jcraft$incrementComboCount();
}
