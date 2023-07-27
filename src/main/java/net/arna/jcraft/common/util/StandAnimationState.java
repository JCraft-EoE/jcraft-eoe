package net.arna.jcraft.common.util;

import net.arna.jcraft.common.entity.StandEntity;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

public interface StandAnimationState<E extends StandEntity<E, ?>> {

    void playAnimation(E stand, AnimationBuilder builder);
}
