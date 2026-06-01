package net.arna.jcraft.common.util;

import net.arna.jcraft.api.attack.IAttacker;

public interface StandAnimationState<A extends IAttacker<A, ?>> {

    void playAnimation(A attacker);

    /**
     * Whether this state may be active while the stand is not performing a move.
     * I.e., whether it can be considered an 'idle state.'
     * @return Whether this state may be active while the stand is idle.
     */
    default boolean mayLinger() {
        return false;
    }
}
