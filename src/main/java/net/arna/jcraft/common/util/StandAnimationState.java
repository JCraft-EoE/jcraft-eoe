package net.arna.jcraft.common.util;

import net.arna.jcraft.common.attack.core.IAttacker;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;

public interface StandAnimationState<A extends IAttacker<A, ?> & GeoEntity> {

    void playAnimation(A attacker, AnimationState state);

    default void configureController(A attacker, AnimationController<A> controller) {
        // no-op by default
    }
}
