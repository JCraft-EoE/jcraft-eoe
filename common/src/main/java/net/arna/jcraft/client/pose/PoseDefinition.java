package net.arna.jcraft.client.pose;

import org.jetbrains.annotations.Nullable;

/**
 * A pose loaded from player_animation/pose/*.json.
 * windupAnimId plays once, then idleAnimId loops until cancelled.
 */
public record PoseDefinition(
        String windupAnimId,
        int windupTicks,       // animation_length * 20, rounded up
        @Nullable String idleAnimId,
        String displayName
) {
    public boolean hasIdle() {
        return idleAnimId != null;
    }
}
