package net.arna.jcraft.common.util;

import net.arna.jcraft.common.attack.core.IAttacker;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

public interface StandAnimationState<A extends IAttacker<A, ?>> {

    void playAnimation(A attacker, AnimationBuilder builder);
}
