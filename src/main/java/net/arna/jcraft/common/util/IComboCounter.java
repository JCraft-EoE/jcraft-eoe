package net.arna.jcraft.common.util;

import net.minecraft.entity.LivingEntity;

public interface IComboCounter {
    LivingEntity jcraft$getLastAttacked();

    void jcraft$setLastAttacked(LivingEntity l);

    int jcraft$getComboCount();

    /**
     * @return whether the victim was stunned at the start of the tick.
     * This is important because one cannot act during the tick stun is finishing, meaning combos and such should keep counting.
     */
    boolean jcraft$wasStunned();

    void jcraft$setComboCount(int i);

    void jcraft$incrementComboCount();
}
