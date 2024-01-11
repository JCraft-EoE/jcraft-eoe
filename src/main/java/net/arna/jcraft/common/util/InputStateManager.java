package net.arna.jcraft.common.util;

import net.arna.jcraft.common.attack.core.MoveInputType;

import java.util.EnumSet;
import java.util.Set;

/**
 * Very simple datastructure that stores the state of the player's inputs.
 * Look at PlayerInputPacket for usage.
 */
public class InputStateManager {
    public Set<MoveInputType> heldInputs = EnumSet.noneOf(MoveInputType.class);
    public boolean forward, backward, left, right;
    public boolean dashing, jumping;

    public int calcForward() {
        int forward = 0;
        if (this.forward) forward++;
        if (backward) forward--;
        return forward;
    }

    public int calcSide() {
        int side = 0;
        if (left) side++;
        if (right) side--;
        return side;
    }

    public void copyFrom(InputStateManager other) {
        this.heldInputs = EnumSet.copyOf(other.heldInputs);
        this.forward = other.forward;
        this.backward = other.backward;
        this.left = other.left;
        this.right = other.right;
        this.dashing = other.dashing;
        this.jumping = other.jumping;
    }
}
