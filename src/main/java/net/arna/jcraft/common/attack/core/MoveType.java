package net.arna.jcraft.common.attack.core;

import lombok.Getter;
import net.arna.jcraft.common.util.CooldownType;

@Getter
public enum MoveType {
    LIGHT(CooldownType.STAND_LIGHT),
    HEAVY(CooldownType.STAND_HEAVY),
    BARRAGE(CooldownType.STAND_BARRAGE),
    SPECIAL1(CooldownType.STAND_SP1),
    SPECIAL2(CooldownType.STAND_SP2),
    SPECIAL3(CooldownType.STAND_SP3),
    ULT(CooldownType.ULT),
    UTIL(CooldownType.UTIL);

    private final CooldownType defaultCooldownType;

    MoveType(CooldownType defaultCooldownType) {
        this.defaultCooldownType = defaultCooldownType;
    }
}
