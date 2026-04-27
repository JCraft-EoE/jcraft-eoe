package net.arna.jcraft.api.splatter;

import java.util.Random;

public enum SplatterRotation {
    NONE,
    CW_90,
    R_180,
    CCW_90;

    public static SplatterRotation getRandom() {
        return values()[new Random().nextInt(values().length)];
    }

    public float[] rotateUv(float u, float v) {
        return switch (this) {
            case CW_90  -> new float[]{v,     1 - u};
            case R_180  -> new float[]{1 - u, 1 - v};
            case CCW_90 -> new float[]{1 - v, u    };
            default     -> new float[]{u,     v    };
        };
    }
}
