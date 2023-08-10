package net.arna.jcraft.common.util;

import lombok.Getter;

@Getter
public enum CooldownType {
    // Stand Cooldowns
    STAND_LIGHT,
    STAND_HEAVY,
    STAND_BARRAGE(true),
    STAND_SP1,
    STAND_SP2,
    STAND_SP3,
    STAND_ULT(true),

    // Spec Cooldowns
    HEAVY,
    BARRAGE(true),
    SP1,
    SP2,
    SP3,
    ULT(true),

    // Universal Cooldowns
    UTIL,
    COMBO_BREAKER(1200, true),  // 60s
    COOLDOWN_CANCEL(900, true), // 45s
    DASH(true);

    private final int duration;
    private final boolean nonResettable;

    CooldownType() {
        this(-1);
    }

    CooldownType(int duration) {
        this(duration, false);
    }

    CooldownType(boolean nonResettable) {
        this(-1, nonResettable);
    }

    CooldownType(int duration, boolean nonResettable) {
        this.duration = duration;
        this.nonResettable = nonResettable;
    }
}
