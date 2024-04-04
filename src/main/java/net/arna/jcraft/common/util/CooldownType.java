package net.arna.jcraft.common.util;

import lombok.Getter;
import net.arna.jcraft.JCraft;

@Getter
public enum CooldownType {
    // Stand Cooldowns
    STAND_LIGHT,
    STAND_HEAVY,
    STAND_BARRAGE(true),
    STAND_SP1,
    STAND_SP2,
    STAND_SP3,
    STAND_ULTIMATE(Category.STAND, true, true),

    // Spec Cooldowns
    HEAVY(Category.SPEC),
    BARRAGE(Category.SPEC, true),
    SPECIAL1(Category.SPEC),
    SPECIAL2(Category.SPEC),
    SPECIAL3(Category.SPEC),
    ULTIMATE(Category.SPEC, true, true),

    // Universal Cooldowns
    UTILITY(Category.UNIVERSAL),
    COMBO_BREAKER(Category.UNIVERSAL, 1200, true, true),  // 60s
    COOLDOWN_CANCEL(Category.UNIVERSAL, 900, true, true), // 45s
    DASH(Category.UNIVERSAL, JCraft.dashCooldown, true, true);

    private final Category category;
    private final int duration;
    private final boolean nonResettable;
    private final boolean overrideNoCooldowns;

    CooldownType() {
        this(-1);
    }

    CooldownType(int duration) {
        this(duration, false);
    }

    CooldownType(Category category) {
        this(category, -1, false, false);
    }

    CooldownType(boolean nonResettable) {
        this(-1, nonResettable);
    }

    CooldownType(Category category, boolean nonResettable) {
        this(category, -1, nonResettable, false);
    }

    CooldownType(int duration, boolean nonResettable) {
        this(Category.STAND, duration, nonResettable, false);
    }

    CooldownType(Category category, boolean nonResettable, boolean overrideNoCooldowns) {
        this(category, -1, nonResettable, overrideNoCooldowns);
    }

    CooldownType(Category category, int duration, boolean nonResettable, boolean overrideNoCooldowns) {
        this.category = category;
        this.duration = duration;
        this.nonResettable = nonResettable;
        this.overrideNoCooldowns = overrideNoCooldowns;
    }

    public enum Category {
        STAND, SPEC, UNIVERSAL
    }
}
