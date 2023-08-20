package net.arna.jcraft.common.attack.core.old;

import lombok.Getter;
import net.arna.jcraft.common.attack.core.MoveType;
import org.jetbrains.annotations.Nullable;

@Getter
public enum AttackQueue {
    LIGHT(MoveType.LIGHT),
    HEAVY(MoveType.HEAVY),
    BARRAGE(MoveType.BARRAGE),
    SPECIAL1(MoveType.SPECIAL1),
    ULTIMATE(MoveType.ULT),
    SPECIAL2(MoveType.SPECIAL2),
    SPECIAL3(MoveType.SPECIAL3),
    MIDDLE_MOUSE(MoveType.UTIL),
    STAND_SUMMON(null);

    @Nullable
    private final MoveType moveType;

    AttackQueue(@Nullable MoveType moveType) {
        this.moveType = moveType;
    }
}
