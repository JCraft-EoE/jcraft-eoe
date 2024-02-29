package net.arna.jcraft.common.util;

import net.arna.jcraft.common.attack.core.IAttacker;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;

public interface StandAnimationState<A extends IAttacker<A, ?> & IAnimatable> {

    void playAnimation(A attacker, AnimationBuilder builder);

    default void configureController(A attacker, AnimationController<A> controller) {
        // no-op by default
    }
}
