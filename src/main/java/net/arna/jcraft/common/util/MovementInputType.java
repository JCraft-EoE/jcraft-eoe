package net.arna.jcraft.common.util;

import lombok.Getter;

@Getter
public enum MovementInputType {
    FORWARD(1, 0),
    BACKWARD(-1, 0),
    LEFT(0, 1),
    RIGHT(0, -1),
    JUMP,
    DASH;

    private final int forward, side;

    MovementInputType() {
        this(0, 0);
    }

    MovementInputType(int forward, int side) {
        this.forward = forward;
        this.side = side;
    }
}
