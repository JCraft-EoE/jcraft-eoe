package net.arna.jcraft.common.attack.core;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.util.CooldownType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class MoveMap<A extends IAttacker<A, S>, S> implements Iterable<MoveMap.Entry<A, S>> {
    private final ListMultimap<MoveType, Entry<A, S>> moves = MultimapBuilder.enumKeys(MoveType.class).arrayListValues().build();
    private final List<Entry<A, S>> extraMoves = new ArrayList<>();
    @Getter
    private boolean frozen = false;

    public Entry<A, S> register(@NonNull MoveType type, @NonNull AbstractMove<?, ? super A> move) {
        return register(type, move, null);
    }

    public Entry<A, S> register(@NonNull MoveType type, @NonNull AbstractMove<?, ? super A> move, @Nullable S animState) {
        return register(type, move, type.getDefaultCooldownType(), animState);
    }

    public Entry<A, S> register(@NonNull MoveType type, @NonNull AbstractMove<?, ? super A> move, @Nullable CooldownType cooldownType, @Nullable S animState) {
        checkFrozen();

        AbstractMove<?, ? super A> copy = move.copy();
        //noinspection ConstantValue // That's the idea
        if (copy == null) throw new IllegalStateException(move.getClass().getSimpleName() + "#copy() returned null.");

        copy.onRegister(type);

        Entry<A, S> entry = new Entry<A, S>(null, type, copy, cooldownType, animState);
        moves.put(type, entry);
        return entry;
    }

    /**
     * For any move that does not get referenced directly by any other move and is not invoked by a move-type,
     * but should still be included.
     * @param move The move to register
     * @return The entry for the given move
     */
    public Entry<A, S> registerExtra(@NonNull AbstractMove<?, ? super A> move) {
        Entry<A, S> entry = new Entry<A, S>(null, null, move, null, null);
        extraMoves.add(entry);
        return entry;
    }

    public void freeze() {
        checkFrozen();

        frozen = true;
    }

    @NonNull
    public List<Entry<A, S>> getEntries(MoveType type) {
        return Collections.unmodifiableList(moves.get(type));
    }

    @Nullable
    public Entry<A, S> getFirstValidEntry(MoveType type, A attacker) {
        return getEntries(type).stream()
                .filter(entry -> entry.getMove().getConditions().stream().allMatch(c -> c.test(attacker)))
                .findFirst()
                .orElse(null);
    }

    private void checkFrozen() {
        if (frozen) throw new IllegalStateException("MoveMap is already frozen.");
    }

    @NotNull
    @Override
    public Iterator<MoveMap.Entry<A, S>> iterator() {
        // Ensure we add all variants here too.
        return Stream.concat(moves.values().stream(), extraMoves.stream())
                .flatMap(this::streamEntryAndChildren)
                .iterator();
    }

    private Stream<Entry<A, S>> streamEntryAndChildren(MoveMap.Entry<A, S> entry) {
        Stream.Builder<Entry<A, S>> builder = Stream.builder();
        builder.add(entry);
        if (entry.getCrouchingVariant() != null)
            streamEntryAndChildren(entry.getCrouchingVariant()).forEach(builder::add);
        if (entry.getAerialVariant() != null)
            streamEntryAndChildren(entry.getAerialVariant()).forEach(builder::add);
        if (entry.getFollowUp() != null)
            streamEntryAndChildren(entry.getFollowUp()).forEach(builder::add);

        return builder.build();
    }

    @Data
    public static class Entry<A extends IAttacker<A, S>, S> {
        private final Entry<A, S> parent;
        private final MoveType type;
        private final AbstractMove<?, ? super A> move;
        private final CooldownType cooldownType;
        private final @Nullable S animState;
        private @Nullable Entry<A, S> crouchingVariant, aerialVariant, followUp;

        private Entry(Entry<A, S> parent, MoveType type, AbstractMove<?, ? super A> move, CooldownType cooldownType, @Nullable S animState) {
            this.parent = parent;
            this.type = type;
            this.move = move;
            this.cooldownType = cooldownType;
            this.animState = animState;

            if (move.getCrouchingVariant() != null)
                crouchingVariant = new Entry<>(this, type, move.getCrouchingVariant(), cooldownType, animState);

            if (move.getAerialVariant() != null)
                aerialVariant = new Entry<>(this, type, move.getAerialVariant(), cooldownType, animState);

            if (move.getFollowup() != null)
                followUp = new Entry<>(this, type, move.getFollowup(), cooldownType, animState);
        }

        /**
         * Overrides the default crouching variant of this entry.
         * If this method is not called, but this entry's move does have a
         * crouching variant, the crouching variant will use the same cooldown type
         * and animation state as this entry.
         * Use this if you wish to use a different state for the crouching variant.
         * @param animState The animation state to use for the crouching variant of this move
         * @see #withCrouchingVariant(CooldownType, Object)
         * @return The crouching variant entry
         */
        public Entry<A, S> withCrouchingVariant(S animState) {
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
         * @see #withCrouchingVariant(Object)
         * @return The crouching variant entry
         */
        public Entry<A, S> withCrouchingVariant(CooldownType cooldownType, S animState) {
            if (move.getCrouchingVariant() == null) throw new IllegalArgumentException("The move of this entry has " +
                    "no crouching variant.");
            crouchingVariant = new Entry<>(this, type, move.getCrouchingVariant(), cooldownType, animState);
            return crouchingVariant;
        }

        /**
         * Overrides the default aerial variant of this entry.
         * If this method is not called, but this entry's move does have an
         * aerial variant, the aerial variant will use the same cooldown type
         * and animation state as this entry.
         * Use this if you wish to use a different state for the aerial variant.
         * @param animState The animation state to use for the aerial variant of this move
         * @see #withAerialVariant(CooldownType, Object)
         * @return The aerial variant entry
         */
        public Entry<A, S> withAerialVariant(S animState) {
            return withAerialVariant(cooldownType, animState);
        }

        /**
         * Overrides the default aerial variant of this entry.
         * If this method is not called, but this entry's move does have an
         * aerial variant, the aerial variant will use the same cooldown type
         * and animation state as this entry.
         * Use this if you wish to use a different state for the aerial variant.
         * @param cooldownType The cooldown type to use for the aerial variant of this move
         * @param animState The animation state to use for the aerial variant of this move
         * @see #withAerialVariant(Object)
         * @return The aerial variant entry
         */
        public Entry<A, S> withAerialVariant(CooldownType cooldownType, S animState) {
            if (move.getAerialVariant() == null) throw new IllegalArgumentException("The move of this entry has " +
                    "no aerial variant.");
            aerialVariant = new Entry<>(this, type, move.getAerialVariant(), cooldownType, animState);
            return aerialVariant;
        }

        /**
         * Overrides the default follow-up of this entry.
         * If this method is not called, but this entry's move does have a
         * follow-up, the follow-up will use the same cooldown type and
         * animation state as this entry.
         * Use this if you wish to use a different state for the follow-up.
         * @param animState The animation state to use for the crouching variant of this move
         * @see #withFollowUp(CooldownType, Object)
         * @return The followup entry
         */
        public Entry<A, S> withFollowUp(S animState) {
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
         * @see #withFollowUp(CooldownType, Object)
         * @return The followup entry
         */
        public Entry<A, S> withFollowUp(CooldownType cooldownType, S animState) {
            if (move.getFollowup() == null) throw new IllegalArgumentException("The move of this entry has " +
                    "no follow-up.");
            followUp = new Entry<>(this, type, move.getFollowup(), cooldownType, animState);
            return followUp;
        }
    }
}
