package net.arna.jcraft.common.attack.core.old;

import lombok.Getter;
import net.arna.jcraft.common.attack.core.MoveType;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

@Getter
public enum MoveQueue {
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

    MoveQueue(@Nullable MoveType moveType) {
        this.moveType = moveType;
        FromMoveTypeHolder.fromMoveType.put(moveType, this);
    }

    public static MoveQueue fromMoveType(MoveType moveType) {
        return Objects.requireNonNull(FromMoveTypeHolder.fromMoveType.get(moveType), "No MoveQueue has been " +
                "associated with the given MoveType.");
    }

    private static class FromMoveTypeHolder {
        private static final Map<MoveType, MoveQueue> fromMoveType = new EnumMap<>(MoveType.class);
    }
}
