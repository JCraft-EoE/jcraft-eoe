package net.arna.jcraft.common.entity;

import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.GoalSelector;

public record Behavior(Brain<?> brain, GoalSelector goalSelector, GoalSelector targetSelector) {
    // nothing else needed for now
}
