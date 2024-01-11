package net.arna.jcraft.common.util;

import net.arna.jcraft.common.attack.core.MoveInputType;

import java.util.EnumSet;
import java.util.Set;

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
}
