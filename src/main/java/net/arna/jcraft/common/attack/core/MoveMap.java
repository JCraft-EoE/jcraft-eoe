package net.arna.jcraft.common.attack.core;

import com.google.common.collect.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.util.CooldownType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class MoveMap<A extends IAttacker<A, S>, S> implements Iterable<MoveMap.Entry<A, S>> {
    private final ListMultimap<MoveType, Entry<A, S>> moves = MultimapBuilder.enumKeys(MoveType.class).arrayListValues().build();
    private final List<Entry<A, S>> extraMoves = new ArrayList<>();
    @Getter
    private Map<AbstractMove<?, ? super A>, AbstractMove<?, ? super A>> copyMap = Map.of();
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final List<AbstractMove<?, ? super A>> allMoves = toList();
    @Getter
    private boolean frozen = false;

    public Entry<A, S> register(@NonNull MoveType type, @NonNull AbstractMove<?, ? super A> move) {
        return register(type, move, null);
    }

    public Entry<A, S> register(@NonNull MoveType type, @NonNull AbstractMove<?, ? super A> move, @Nullable S animState) {
        return register(type, move, type.getDefaultCooldownType(), animState);
    }

    /**
     * Registers a move and all its variants.
     * Sub-moves must have an assigned animation state
     */
    public void registerRecursive(@NonNull MoveType type, @NonNull AbstractMove<?, ? super A> move, @Nullable S animState) {
        Entry<A, S> entry = register(type, move, animState);

        AbstractMove<?, ? super A> c = move.getCrouchingVariant();
        if (c != null) {
            entry.withCrouchingVariant((S) c.getAnimation());
        }

        AbstractMove<?, ? super A> a = move.getAerialVariant();
        if (a != null) {
            entry.withAerialVariant((S) a.getAnimation());
        }

        AbstractMove<?, ? super A> f = move.getFollowup();
        if (f != null) {
            registerRecursive(type, f, (S) f.getAnimation());
            entry.withFollowUp((S) f.getAnimation());
        }
    }

    /**
     * Registers a move and its immediate followup and variants.
     * Sub-moves must have an assigned animation state.
     */
    public void registerImmediate(@NonNull MoveType type, @NonNull AbstractMove<?, ? super A> move, @Nullable S animState) {
        Entry<A, S> entry = register(type, move, animState);
        if (move.getCrouchingVariant() != null)
            entry.withCrouchingVariant((S) move.getCrouchingVariant().getAnimation());
        if (move.getAerialVariant() != null)
            entry.withAerialVariant((S) move.getAerialVariant().getAnimation());
        if (move.getFollowup() != null)
            entry.withFollowUp((S) move.getFollowup().getAnimation());
    }

    public Entry<A, S> register(@NonNull MoveType type, @NonNull AbstractMove<?, ? super A> move, @Nullable CooldownType cooldownType, @Nullable S animState) {
        checkFrozen();

        JCraft.LOGGER.info("Registering move " + move.getName().getString() + ", of type: " + type);

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
        buildCopyMap();
    }

    @NonNull
    public List<Entry<A, S>> getEntries(MoveType type) {
        return Collections.unmodifiableList(moves.get(type));
    }

    /**
     * Finds first move of the given type that matches all conditions for the given attacker.
     * @param type The type of move to find
     * @param attacker The attacker to test the move against
     * @return The first valid entry for the given type, or null if none are found.
     */
    @Nullable
    public Entry<A, S> getFirstValidEntry(MoveType type, A attacker) {
        return getEntries(type).stream()
                .filter(entry -> entry.getMove().getConditions().stream().allMatch(c -> c.test(attacker)))
                .findFirst()
                .orElse(null);
    }

    /**
     * Throws an {@link IllegalStateException} if this MoveMap is frozen.
     */
    private void checkFrozen() {
        if (frozen) throw new IllegalStateException("MoveMap is already frozen.");
    }

    private void buildCopyMap() {
        copyMap = Streams.stream(this)
                .collect(Collector.of(ImmutableMap::<AbstractMove<?, ? super A>, AbstractMove<?, ? super A>>builder,
                        (b, entry) -> b.put(entry.getMove().getOriginalMove(), entry.getMove()),
                        (b1, b2) -> b1.putAll(b2.build()), ImmutableMap.Builder::build));
    }

    // Upon registering, the move registered is copied before being put in this map.
    // At any point you want the registered version for a move, use this method.
    @Contract("!null -> !null; null -> null")
    public AbstractMove<?, ? super A> getRegisteredMoveFor(AbstractMove<?, ? super A> move) {
        return copyMap.getOrDefault(move, move);
    }

    @NotNull
    @Override
    public Iterator<MoveMap.Entry<A, S>> iterator() {
        // Ensure we add all variants here too.
        return Stream.concat(moves.values().stream(), extraMoves.stream())
                .flatMap(this::streamEntryAndChildren)
                .iterator();
    }

    /**
     * Builds a list of all moves in this map.
     * @return A list of all moves in this map.
     */
    private List<AbstractMove<?, ? super A>> toList() {
        return ImmutableList.copyOf(this).stream()
                .map(Entry::getMove)
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * Returns a list of all moves in this map.
     * Includes variants and follow-ups, recursively.
     * @return A list of all moves in this map.
     */
    public List<AbstractMove<?, ? super A>> asMovesList() {
        // If the map is frozen, we can return the cached list.
        return frozen ? getAllMoves() : toList();
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
