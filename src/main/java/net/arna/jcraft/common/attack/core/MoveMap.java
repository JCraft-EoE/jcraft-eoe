package net.arna.jcraft.common.attack.core;

import lombok.Getter;
import net.arna.jcraft.common.attack.core.base.AbstractMove;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.StandAnimationState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class MoveMap<E extends StandEntity<E, S>, S extends Enum<S> & StandAnimationState<E>> implements Iterable<MoveMap.Entry<E, S>> {
    private final Map<MoveType, Entry<E, S>> moves = new EnumMap<>(MoveType.class);
    @Getter
    private boolean frozen = false;

    public MoveMap<E, S> register(MoveType type, AbstractMove<?, ? super E> move) {
        return register(type, move, null);
    }

    public MoveMap<E, S> register(MoveType type, AbstractMove<?, ? super E> move, S animState) {
        return register(type, move, type.getDefaultCooldownType(), animState);
    }

    public MoveMap<E, S> register(MoveType type, AbstractMove<?, ? super E> move, CooldownType cooldownType, S animState) {
        checkFrozen();

        moves.put(type, new Entry<>(move, cooldownType, animState));
        return this;
    }

    public void freeze() {
        checkFrozen();

        frozen = true;
    }

    public Entry<E, S> getMove(MoveType type) {
        return Optional.ofNullable(moves.get(type)).orElseThrow(() -> new IllegalArgumentException("MoveMap has no " +
                "move of type " + type));
    }

    private void checkFrozen() {
        if (frozen) throw new IllegalStateException("MoveMap is already frozen.");
    }

    @NotNull
    @Override
    public Iterator<MoveMap.Entry<E, S>> iterator() {
        return moves.values().iterator();
    }

    public record Entry<E extends StandEntity<E, S>, S extends Enum<S> & StandAnimationState<E>>(
            AbstractMove<?, ? super E> attack, CooldownType cooldownType, @Nullable S animState) {}
}
