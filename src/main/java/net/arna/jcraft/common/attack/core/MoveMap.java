package net.arna.jcraft.common.attack.core;

import lombok.Data;
import lombok.Getter;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
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

    public Entry<E, S> register(MoveType type, AbstractMove<?, ? super E> move) {
        return register(type, move, null);
    }

    public Entry<E, S> register(MoveType type, AbstractMove<?, ? super E> move, S animState) {
        return register(type, move, type.getDefaultCooldownType(), animState);
    }

    public Entry<E, S> register(MoveType type, AbstractMove<?, ? super E> move, CooldownType cooldownType, S animState) {
        checkFrozen();

        Entry<E, S> entry = new Entry<>(move, cooldownType, animState);
        moves.put(type, entry);
        return entry;
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

    @Data
    public static class Entry<E extends StandEntity<E, S>, S extends Enum<S> & StandAnimationState<E>> {
        private final AbstractMove<?, ? super E> move;
        private final CooldownType cooldownType;
        private final @Nullable S animState;
        private @Nullable Entry<E, S> crouchingVariant, followUp;

        private Entry(AbstractMove<?, ? super E> move, CooldownType cooldownType, @Nullable S animState) {
            this.move = move;
            this.cooldownType = cooldownType;
            this.animState = animState;

            if (move.getCrouchingVariant() != null)
                crouchingVariant = new Entry<>(move.getCrouchingVariant(), cooldownType, animState);
        }

        /**
         * Overrides the default crouching variant of this entry.
         * If this method is not called, but this entry's move does have a
         * crouching variant, the crouching variant will use the same cooldown type
         * and animation state as this entry.
         * Use this if you wish to use a different state for the crouching variant.
         * @param animState The animation state to use for the crouching variant of this move
         * @see #withCrouchingVariant(CooldownType, Enum)
         * @return This entry
         */
        public Entry<E, S> withCrouchingVariant(S animState) {
            return withCrouchingVariant(cooldownType, animState);
        }

        /**
         * Overrides the default crouching variant of this entry.
         * If this method is not called, but this entry's move does have a
         * crouching variant, the crouching variant will use the same cooldown type
         * and animation state as this entry.
         * Use this if you wish to use a different state and cooldown type for the crouching variant.
         * @param cooldownType The cooldown type to use for the crouching variant of this move
         * @param animState The animation state to use for the crouching variant of this move
         * @see #withCrouchingVariant(Enum)
         * @return This entry
         */
        public Entry<E, S> withCrouchingVariant(CooldownType cooldownType, S animState) {
            if (move.getCrouchingVariant() == null) throw new IllegalArgumentException("The move of this entry has " +
                    "no crouching variant.");
            crouchingVariant = new Entry<>(move.getCrouchingVariant(), cooldownType, animState);
            return this;
        }

        /**
         * Overrides the default follow-up of this entry.
         * If this method is not called, but this entry's move does have a
         * follow-up, the follow-up will use the same cooldown type and
         * animation state as this entry.
         * Use this if you wish to use a different state for the follow-up.
         * @param animState The animation state to use for the crouching variant of this move
         * @see #withFollowUp(CooldownType, Enum)
         * @return This entry
         */
        public Entry<E, S> withFollowUp(S animState) {
            return withFollowUp(cooldownType, animState);
        }

        /**
         * Overrides the default follow-up of this entry.
         * If this method is not called, but this entry's move does have a
         * follow-up, the follow-up will use the same cooldown type and
         * animation state as this entry.
         * Use this if you wish to use a different cooldown type and state for the follow-up.
         * @param cooldownType The cooldown type to use for the follow-up of this move
         * @param animState The animation state to use for the follow-up of this move
         * @see #withFollowUp(CooldownType, Enum)
         * @return This entry
         */
        public Entry<E, S> withFollowUp(CooldownType cooldownType, S animState) {
            if (move.getFollowUp() == null) throw new IllegalArgumentException("The move of this entry has " +
                    "no follow-up.");
            crouchingVariant = new Entry<>(move.getFollowUp(), cooldownType, animState);
            return this;
        }

        public void registerContextEntries(MoveContext ctx) {
            registerContextEntries(move, ctx);
        }

        private void registerContextEntries(AbstractMove<?, ?> move, MoveContext ctx) {
            move.registerContextEntries(ctx);
            if (move.getCrouchingVariant() != null) registerContextEntries(move.getCrouchingVariant(), ctx);
            if (move.getFollowUp() != null) registerContextEntries(move.getFollowUp(), ctx);
        }
    }
}
