package net.arna.jcraft.common.attack.core;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

@Getter
public enum MoveQueue {
    LIGHT(MoveType.LIGHT),
    HEAVY(MoveType.HEAVY),
    BARRAGE(MoveType.BARRAGE),
    SPECIAL1(MoveType.SPECIAL1),
    SPECIAL2(MoveType.SPECIAL2),
    SPECIAL3(MoveType.SPECIAL3),
    ULTIMATE(MoveType.ULTIMATE),
    UTILITY(MoveType.UTILITY),
    STAND_SUMMON(null);

    public static final int types = 9;
    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private static final Map<MoveType, MoveQueue> fromMoveType = Arrays.stream(values())
            .filter(v -> v.getMoveType() != null)
            .collect(ImmutableMap.toImmutableMap(MoveQueue::getMoveType, v -> v));

    @Nullable
    private final MoveType moveType;

    MoveQueue(@Nullable MoveType moveType) {
        this.moveType = moveType;
    }

    public static MoveQueue fromMoveType(MoveType moveType) {
        return Objects.requireNonNull(getFromMoveType().get(moveType), "No MoveQueue has been " +
                "associated with the given MoveType.");
    }
}
