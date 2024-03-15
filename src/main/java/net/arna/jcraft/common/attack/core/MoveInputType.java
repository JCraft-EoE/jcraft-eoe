package net.arna.jcraft.common.attack.core;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

@Getter
public enum MoveInputType {
    LIGHT(MoveType.LIGHT, true),
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
    private static final Map<MoveType, MoveInputType> fromMoveType = Arrays.stream(values())
            .filter(v -> v.getMoveType() != null)
            .collect(ImmutableMap.toImmutableMap(MoveInputType::getMoveType, v -> v));

    @Nullable
    private final MoveType moveType;
    private final boolean holdable;

    MoveInputType(@Nullable MoveType moveType) {
        this(moveType, false);
    }

    MoveInputType(@Nullable MoveType moveType, boolean holdable) {
        this.moveType = moveType;
        this.holdable = holdable;
    }

    public static @Nullable MoveInputType fromMoveType(MoveType moveType) {
        //return Objects.requireNonNull(getFromMoveType().get(moveType), "No MoveQueue has been associated with the given MoveType.");
        return getFromMoveType().get(moveType);
    }
}
