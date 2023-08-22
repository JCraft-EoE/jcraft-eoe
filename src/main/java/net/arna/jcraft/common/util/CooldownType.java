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
    HEAVY(Category.SPEC),
    BARRAGE(Category.SPEC, true),
    SP1(Category.SPEC),
    SP2(Category.SPEC),
    SP3(Category.SPEC),
    ULTIMATE(Category.SPEC, true),

    // Universal Cooldowns
    UTILITY(Category.UNIVERSAL),
    COMBO_BREAKER(Category.UNIVERSAL, 1200, true),  // 60s
    COOLDOWN_CANCEL(Category.UNIVERSAL, 900, true), // 45s
    DASH(Category.UNIVERSAL, true);

    private final Category category;
    private final int duration;
    private final boolean nonResettable;

    CooldownType() {
        this(-1);
    }

    CooldownType(int duration) {
        this(duration, false);
    }

    CooldownType(Category category) {
        this(category, -1, false);
    }

    CooldownType(boolean nonResettable) {
        this(-1, nonResettable);
    }

    CooldownType(Category category, boolean nonResettable) {
        this(category, -1, nonResettable);
    }

    CooldownType(int duration, boolean nonResettable) {
        this(Category.STAND, duration, nonResettable);
    }

    CooldownType(Category category, int duration, boolean nonResettable) {
        this.category = category;
        this.duration = duration;
        this.nonResettable = nonResettable;
    }

    public enum Category {
        STAND, SPEC, UNIVERSAL
    }
}
