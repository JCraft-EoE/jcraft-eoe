package net.arna.jcraft.api.attack.enums;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;

@Getter
public enum MoveInputType {
    LIGHT(MoveClass.LIGHT, true),
    HEAVY(MoveClass.HEAVY, false, MoveClass.TOSS, true),
    BARRAGE(MoveClass.BARRAGE),
    SPECIAL1(MoveClass.SPECIAL1),
    SPECIAL2(MoveClass.SPECIAL2),
    SPECIAL3(MoveClass.SPECIAL3),
    ULTIMATE(MoveClass.ULTIMATE),
    UTILITY(MoveClass.UTILITY),
    STAND_SUMMON(null),
    TOSS(MoveClass.TOSS);

    public static final int types = 10;
    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private static final Map<MoveClass, MoveInputType> fromMoveType = Arrays.stream(values())
            .filter(v -> v.getMoveClass() != null)
            .collect(ImmutableMap.toImmutableMap(MoveInputType::getMoveClass, v -> v));

    @Nullable
    private final MoveClass moveClass;
    private final boolean holdable;
    @Nullable
    private final MoveClass moveClassStandby;
    private final boolean holdableStandby;

    MoveInputType(final @Nullable MoveClass moveClass) {
        this(moveClass, false);
    }

    MoveInputType(final @Nullable MoveClass moveClass, final boolean holdable) {
        this(moveClass, holdable, null, false);
    }

    MoveInputType(final @Nullable MoveClass moveClass, final boolean holdable, final @Nullable MoveClass moveClassStandby, final boolean holdableStandby) {
        this.moveClass = moveClass;
        this.holdable = holdable;
        this.moveClassStandby = moveClassStandby;
        this.holdableStandby = holdable;
    }

    public MoveClass getMoveClass(boolean standby) {
        if (standby) {
            return moveClassStandby;
        }
        return moveClass;
    }

    public boolean isHoldable(boolean standby) {
        if (standby) {
            return holdableStandby;
        }
        return holdable;
    }

    public static @Nullable MoveInputType fromMoveClass(final MoveClass moveClass) {
        //return Objects.requireNonNull(getFromMoveType().get(moveType), "No MoveQueue has been associated with the given MoveType.");
        return getFromMoveType().get(moveClass);
    }
}
