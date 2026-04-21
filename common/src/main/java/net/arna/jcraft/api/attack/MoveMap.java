package net.arna.jcraft.api.attack;

import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.JRegistries;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.attack.core.MoveMapImpl;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JCodecUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MoveMap<A extends IAttacker<? extends A, S>, S extends Enum<?>> extends Iterable<MoveMap.Entry<A, S>> {

    /**
     * Creates a codec for a move map with the given state enum.
     * @param stateEnum The state enum class to use for the move map.
     * @return A codec for a move map with the given state enum.
     * @param <A> The type of attacker that this move map is for, which extends {@link IAttacker}.
     * @param <S> The type of animation state enum that this move map uses, which extends {@link Enum}.
     */
    static <A extends IAttacker<? extends A, S>, S extends Enum<? extends S>> Codec<MoveMap<A, S>> codecFor(Class<S> stateEnum) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Entry.<A, S>codecFor(stateEnum).listOf().fieldOf("moves").forGetter(m -> List.copyOf(m.getEntries().values()))
        ).apply(instance, MoveMapImpl::new));
    }

    /**
     * Registers a move with the given type and move.
     * The cooldown type will be set to the default cooldown type for the given move class,
     * and the animation state will be set to null.
     * @param moveClass The class of move to register.
     * @param move The move to register.
     * @return The entry for the registered move.
     */
    default Entry<A, S> register(final @NonNull MoveClass moveClass, final @NonNull AbstractMove<?, ? super A> move) {
        return register(moveClass, move, null);
    }

    /**
     * Registers a move with the given type, move, and animation state.
     * The cooldown type will be set to the default cooldown type for the given move class.
     * @param type The class of move to register.
     * @param move The move to register.
     * @param animState The animation state for this move.
     * @return The entry for the registered move.
     */
    default MoveMap.Entry<A, S> register(final @NonNull MoveClass type, final @NonNull AbstractMove<?, ? super A> move,
                                         final @Nullable S animState) {
        return register(type, move, type.getDefaultCooldownType(), animState);
    }

    /**
     * Registers a move with the given type, move, cooldown type, and animation state.
     * @param type The class of move to register.
     * @param move The move to register.
     * @param cooldownType The cooldown type for this move.
     * @param animState The animation state for this move.
     * @return The entry for the registered move.
     */
    MoveMap.Entry<A, S> register(final @NonNull MoveClass type, final @NonNull AbstractMove<?, ? super A> move,
                                 final @Nullable CooldownType cooldownType, final @Nullable S animState);

    /**
     * Registers a move and its immediate followup and variants.
     * Sub-moves must have an assigned animation state.
     */
    void registerImmediate(final @NonNull MoveClass type, final @NonNull AbstractMove<?, ? super A> move, final @Nullable S animState);

    /**
     * Freezes this move map, preventing any further modifications.
     * Once frozen, this move map can only be used to retrieve moves and cannot be modified.
     */
    void freeze();

    /**
     * Checks if this move map is frozen.
     * A frozen move map cannot be modified, but can still be used to retrieve moves.
     * @return True if this move map is frozen, false otherwise.
     */
    boolean isFrozen();

    /**
     * Clears this move map and copies all entries from the other move map.
     * @param other The move map to copy from.
     * @see #copyFrom(MoveMap, boolean)
     */
    default void copyFrom(final @NonNull MoveMap<A, S> other) {
        copyFrom(other, false);
    }

    /**
     * Clears this move map and copies all entries from the other move map.
     * @param other The move map to copy from.
     * @param force Whether to ignore the frozen state of this move map and copy entries regardless.
     */
    void copyFrom(final @NonNull MoveMap<A, S> other, final boolean force);

    /**
     * Gets all entries in this move map.
     * @return A multimap of move classes to entries.
     */
    Multimap<MoveClass, Entry<A, S>> getEntries();

    /**
     * Gets all entries for the given move class.
     *
     * @param moveClass The move class to get entries for.
     * @return A collection of entries for the given move class.
     */
    Collection<Entry<A, S>> getEntries(final @NonNull MoveClass moveClass);

    /**
     * Finds the entry for the given move.
     * This will return the first entry that matches the move's original move.
     *
     * @param move The move to find the entry for.
     * @return The entry for the given move, or null if none are found.
     */
    @Nullable
    default MoveMap.Entry<A, S> getEntry(final AbstractMove<?, ? super A> move) {
        return Streams.stream(this)
                .filter(entry -> entry.getMove().getOriginalMove() == move)
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds a move by its type.
     * @param moveType The type of move to find.
     * @return The first move of the given type, or an empty optional if none are found.
     * @param <M> The type of move to find.
     */
    default <M extends AbstractMove<?, ?>> Optional<M> findMoveByType(final Class<M> moveType) {
        return asMovesList().stream()
                .filter(moveType::isInstance)
                .map(moveType::cast)
                .findFirst();
    }

    /**
     * Initiates a follow-up move for the given attacker and move.
     * @param attacker The attacker to initiate the follow-up for.
     * @param move The move to initiate the follow-up for.
     * @param setMoveStun Whether to set the move stun for the follow-up move.
     * @param chargeTime How long the current move was charged for.
     */
    default void initiateFollowup(final A attacker, AbstractMove<?, ? super A> move, boolean setMoveStun, int chargeTime) {
        MoveMap.Entry<A, S> entry = getEntry(move.getOriginalMove());
        if (entry == null || entry.getFollowup() == null) {
            return;
        }
        entry = entry.getFollowup();

        // Check conditions
        if (!entry.getMove().conditionsMet(attacker)) {
            return;
        }

        move = entry.getMove();
        AbstractMove<?, ? super A> nextMove = move.isCopyOnUse() ? move.copy() : move;
        nextMove.setChargeTime(chargeTime);
        attacker.setMove(nextMove, entry.getAnimState());
        if (setMoveStun) {
            attacker.setMoveStun(move.getDuration());
        }
    }

    /**
     * Ticks all moves in this map for the given attacker.
     * This will call {@link AbstractMove#tick(IAttacker)} on each move.
     *
     * @param attacker The attacker to tick the moves for.
     */
    default void tickMoves(final A attacker) {
        asMovesList().forEach(move -> move.tick(attacker));
    }

    /**
     * Returns a list of all moves in this map.
     * This will return all moves, including variants and follow-ups.
     *
     * @return A list of all moves in this map.
     */
    List<AbstractMove<?, ? super A>> asMovesList();

    /**
     * Finds first move of the given type that matches all conditions for the given attacker.
     *
     * @param type     The type of move to find
     * @param attacker The attacker to test the move against
     * @return The first valid entry for the given type, or null if none are found.
     */
    @Nullable
    default MoveMap.Entry<A, S> getFirstValidEntry(final MoveClass type, final A attacker, final boolean crouching, final boolean aerial) {
        return getEntries(type).stream()
                .map(entry -> {
                    if (crouching && entry.getCrouchingVariant() != null) {
                        entry = entry.getCrouchingVariant();
                        if (aerial && entry.getAerialVariant() != null) {
                            return entry.getAerialVariant();
                        }

                        return entry;
                    } else if (aerial && entry.getAerialVariant() != null) {
                        entry = entry.getAerialVariant();
                        if (crouching && entry.getCrouchingVariant() != null) {
                            return entry.getCrouchingVariant();
                        }

                        return entry;
                    } else {
                        return entry;
                    }
                })
                .filter(entry -> entry.getMove().conditionsMet(attacker))
                .findFirst()
                .orElse(null);
    }

    /**
     * An entry in a move map. Contains a move, its associated properties and its variants.
     * @param <A> The type of attacker that this entry is for, which extends {@link IAttacker}.
     * @param <S> The type of animation state enum that this entry uses, which extends {@link Enum}.
     */
    interface Entry<A extends IAttacker<? extends A, S>, S extends Enum<?>> {
        /**
         * Creates a codec for an entry with the given state enum.
         * @param stateEnum The class of the animation state enum
         * @return A codec for an entry with the given state enum
         * @param <S> The type of the animation state enum
         */
        static <A extends IAttacker<? extends A, S>, S extends Enum<? extends S>> Codec<Entry<A, S>> codecFor(Class<S> stateEnum) {
            Codec<S> stateCodec = JCodecUtils.createEnumCodec(stateEnum);
            return JCodecUtils.recursive("MoveMap.Entry", self ->
                    RecordCodecBuilder.create(instance -> instance.group(
                            MoveClass.CODEC.fieldOf("class").forGetter(Entry::getMoveClass),
                            JRegistries.MOVE_CODEC.fieldOf("move").forGetter(Entry::getMove),
                            CooldownType.CODEC.fieldOf("cooldown_type").forGetter(Entry::getCooldownType),
                            stateCodec.optionalFieldOf("anim_state").forGetter(e -> Optional.ofNullable(e.getAnimState())),
                            self.optionalFieldOf("crouching_variant").forGetter(e -> Optional.ofNullable(e.getCrouchingVariant())),
                            self.optionalFieldOf("aerial_variant").forGetter(e -> Optional.ofNullable(e.getAerialVariant())),
                            self.optionalFieldOf("followup").forGetter(e -> Optional.ofNullable(e.getFollowup()))
                    ).apply(instance, (moveClass, move, cooldownType,
                                       animState, crouchingVariant, aerialVariant, followup) ->
                            new MoveMapImpl.EntryImpl<>(moveClass, move, cooldownType, animState.orElse(null),
                                    crouchingVariant.orElse(null), aerialVariant.orElse(null), followup.orElse(null)))));
        }

        MoveClass getMoveClass();
        AbstractMove<?, ? super A> getMove();
        CooldownType getCooldownType();
        S getAnimState();

        @Nullable Entry<A, S> getCrouchingVariant();
        @Nullable Entry<A, S> getAerialVariant();
        @Nullable Entry<A, S> getFollowup();

        /**
         * Overrides the default crouching variant of this entry.
         * If this method is not called, but this entry's move does have a
         * crouching variant, the crouching variant will use the same cooldown type
         * and animation state as this entry.
         * Use this if you wish to use a different state for the crouching variant.
         *
         * @param animState The animation state to use for the crouching variant of this move
         * @return The crouching variant entry
         * @see #withCrouchingVariant(CooldownType, Enum)
         */
        default Entry<A, S> withCrouchingVariant(final S animState) {
            return withCrouchingVariant(getCooldownType(), animState);
        }

        /**
         * Overrides the default crouching variant of this entry.
         * If this method is not called, but this entry's move does have a
         * crouching variant, the crouching variant will use the same cooldown type
         * and animation state as this entry.
         * Use this if you wish to use a different state and cooldown type for the crouching variant.
         *
         * @param cooldownType The cooldown type to use for the crouching variant of this move
         * @param animState    The animation state to use for the crouching variant of this move
         * @return The crouching variant entry
         * @see #withCrouchingVariant(Enum)
         */
        Entry<A, S> withCrouchingVariant(final CooldownType cooldownType, final S animState);

        /**
         * Overrides the default aerial variant of this entry.
         * If this method is not called, but this entry's move does have an
         * aerial variant, the aerial variant will use the same cooldown type
         * and animation state as this entry.
         * Use this if you wish to use a different state for the aerial variant.
         *
         * @param animState The animation state to use for the aerial variant of this move
         * @return The aerial variant entry
         * @see #withAerialVariant(CooldownType, Enum)
         */
        default Entry<A, S> withAerialVariant(final S animState) {
            return withAerialVariant(getCooldownType(), animState);
        }

        /**
         * Overrides the default aerial variant of this entry.
         * If this method is not called, but this entry's move does have an
         * aerial variant, the aerial variant will use the same cooldown type
         * and animation state as this entry.
         * Use this if you wish to use a different state for the aerial variant.
         *
         * @param cooldownType The cooldown type to use for the aerial variant of this move
         * @param animState    The animation state to use for the aerial variant of this move
         * @return The aerial variant entry
         * @see #withAerialVariant(Enum)
         */
        Entry<A, S> withAerialVariant(final CooldownType cooldownType, final S animState);

        /**
         * Overrides the default follow-up of this entry.
         * If this method is not called, but this entry's move does have a
         * follow-up, the follow-up will use the same cooldown type and
         * animation state as this entry.
         * Use this if you wish to use a different state for the follow-up.
         *
         * @param animState The animation state to use for the crouching variant of this move
         * @return The followup entry
         * @see #withFollowup(CooldownType, Enum)
         */
        default Entry<A, S> withFollowup(final S animState) {
            return withFollowup(getCooldownType(), animState);
        }

        /**
         * Overrides the default follow-up of this entry.
         * If this method is not called, but this entry's move does have a
         * follow-up, the follow-up will use the same cooldown type and
         * animation state as this entry.
         * Use this if you wish to use a different cooldown type and state for the follow-up.
         *
         * @param cooldownType The cooldown type to use for the follow-up of this move
         * @param animState    The animation state to use for the follow-up of this move
         * @return The followup entry
         * @see #withFollowup(CooldownType, Enum)
         */
        Entry<A, S> withFollowup(final CooldownType cooldownType, final S animState);

        /**
         * Creates a copy of this entry.
         * @return A copy of this entry with the same move, cooldown type, and animation state.
         */
        Entry<A, S> copy();
    }
}
