package net.arna.jcraft.common.attack.core;

import com.google.common.collect.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.util.CooldownType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

@NoArgsConstructor
public class MoveMapImpl<A extends IAttacker<? extends A, S>, S extends Enum<?>> implements MoveMap<A, S> {
    private final ListMultimap<MoveClass, MoveMap.Entry<A, S>> entries = MultimapBuilder.enumKeys(MoveClass.class).arrayListValues().build();
    private List<AbstractMove<?, ? super A>> allMoves;
    @Getter
    private boolean frozen = false;

    public MoveMapImpl(Collection<MoveMap.Entry<A, S>> entries) {
        entries.forEach(entry -> this.entries.put(entry.getMoveClass(), entry));
        freeze();
    }

    public EntryImpl<A, S> register(final @NonNull MoveClass type, final @NonNull AbstractMove<?, ? super A> move) {
        return register(type, move, null);
    }

    public EntryImpl<A, S> register(final @NonNull MoveClass type, final @NonNull AbstractMove<?, ? super A> move, final @Nullable S animState) {
        return register(type, move, type.getDefaultCooldownType(), animState);
    }

    public void registerImmediate(final @NonNull MoveClass type, final @NonNull AbstractMove<?, ? super A> move, final @Nullable S animState) {
        final EntryImpl<A, S> entry = register(type, move, animState);
        copyAnims(entry);
    }

    @SuppressWarnings("unchecked")
    private void copyAnims(MoveMap.Entry<A, S> entry) {
        final AbstractMove<?, ? super A> move = entry.getMove();
        if (move.getCrouchingVariant() != null) {
            MoveMap.Entry<A, S> cr = entry.withCrouchingVariant((S) move.getCrouchingVariant().getAnimation());
            copyAnims(cr);
        }
        if (move.getAerialVariant() != null) {
            MoveMap.Entry<A, S> ae = entry.withAerialVariant((S) move.getAerialVariant().getAnimation());
            copyAnims(ae);
        }
        if (move.getFollowup() != null) {
            MoveMap.Entry<A, S> fw = entry.withFollowup((S) move.getFollowup().getAnimation());
            copyAnims(fw);
        }
    }

    public EntryImpl<A, S> register(final @NonNull MoveClass type, final @NonNull AbstractMove<?, ? super A> move, final @Nullable CooldownType cooldownType, final @Nullable S animState) {
        checkFrozen();

        final AbstractMove<?, ? super A> copy = move.copy();
        //noinspection ConstantValue // That's the idea
        if (copy == null) {
            throw new IllegalStateException(move.getClass().getSimpleName() + "#copy() returned null.");
        }

        copy.onRegister(type);

        final EntryImpl<A, S> entry = new EntryImpl<A, S>(type, copy, cooldownType, animState);
        entries.put(type, entry);
        return entry;
    }

    /**
     * Used by {@link #copyFrom(MoveMap, boolean)} to copy entries.
     * @param entry The entry to register. Will be copied.
     */
    private void register(final MoveMap.Entry<A, S> entry) {
        if (entry == null) return;

        checkFrozen();
        entries.put(entry.getMoveClass(), entry.copy());
    }

    public void freeze() {
        checkFrozen();

        frozen = true;
        allMoves = toList();
    }

    public void copyFrom(final @NotNull MoveMap<A, S> other, final boolean force) {
        if (!force) checkFrozen();
        entries.clear();
        other.getEntries().values().forEach(this::register);
    }

    @NonNull
    public Collection<MoveMap.Entry<A, S>> getEntries(final @NotNull MoveClass type) {
        return Collections.unmodifiableList(entries.get(type));
    }

    /**
     * Throws an {@link IllegalStateException} if this MoveMap is frozen.
     */
    private void checkFrozen() {
        if (frozen) {
            throw new IllegalStateException("MoveMap is already frozen.");
        }
    }

    @NonNull
    @Override
    public Iterator<MoveMap.Entry<A, S>> iterator() {
        // Ensure we add all variants here too.
        return entries.values().stream()
                .flatMap(this::streamEntryAndChildren)
                .iterator();
    }

    /**
     * Builds a list of all moves in this map.
     *
     * @return A list of all moves in this map.
     */
    private List<AbstractMove<?, ? super A>> toList() {
        return ImmutableList.copyOf(this).stream()
                .map(MoveMap.Entry::getMove)
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * Returns a list of all moves in this map.
     * Includes variants and follow-ups, recursively.
     *
     * @return A list of all moves in this map.
     */
    public List<AbstractMove<?, ? super A>> asMovesList() {
        // If the map is frozen, we can return the cached list.
        return frozen && allMoves != null ? allMoves : toList();
    }

    private Stream<MoveMap.Entry<A, S>> streamEntryAndChildren(final MoveMap.Entry<A, S> entry) {
        Stream.Builder<MoveMap.Entry<A, S>> builder = Stream.builder();
        builder.add(entry);
        if (entry.getCrouchingVariant() != null) {
            streamEntryAndChildren(entry.getCrouchingVariant()).forEach(builder::add);
        }
        if (entry.getAerialVariant() != null) {
            streamEntryAndChildren(entry.getAerialVariant()).forEach(builder::add);
        }
        if (entry.getFollowup() != null) {
            streamEntryAndChildren(entry.getFollowup()).forEach(builder::add);
        }

        return builder.build();
    }

    @Override
    public Multimap<MoveClass, MoveMap.Entry<A, S>> getEntries() {
        return ImmutableListMultimap.copyOf(entries);
    }

    @Data
    public static class EntryImpl<A extends IAttacker<? extends A, S>, S extends Enum<?>> implements MoveMap.Entry<A, S> {
        private final MoveClass moveClass;
        private final AbstractMove<?, ? super A> move;
        private final CooldownType cooldownType;
        private final @Nullable S animState;
        private @Nullable MoveMap.Entry<A, S> crouchingVariant, aerialVariant, followup;

        public EntryImpl(final MoveClass moveClass, final AbstractMove<?, ? super A> move, final CooldownType cooldownType, final @Nullable S animState) {
            this.moveClass = moveClass;
            this.move = move;
            this.cooldownType = cooldownType;
            this.animState = animState;

            if (move.getCrouchingVariant() != null) {
                crouchingVariant = new EntryImpl<A, S>(moveClass, move.getCrouchingVariant(), cooldownType, animState);
            }

            if (move.getAerialVariant() != null) {
                aerialVariant = new EntryImpl<A, S>(moveClass, move.getAerialVariant(), cooldownType, animState);
            }

            if (move.getFollowup() != null) {
                followup = new EntryImpl<A, S>(moveClass, move.getFollowup(), cooldownType, animState);
            }
        }

        // Constructor for codec
        @SuppressWarnings("unchecked")
        public EntryImpl(@NonNull MoveClass moveClass, @NonNull AbstractMove<?, ?> move, @NonNull CooldownType cooldownType, @Nullable S animState,
                          @Nullable MoveMap.Entry<A, S> crouchingVariant, @Nullable MoveMap.Entry<A, S> aerialVariant, @Nullable MoveMap.Entry<A, S> followup) {
            this.moveClass = moveClass;
            this.move = (AbstractMove<?, ? super A>) move;
            this.move.withAnim(animState);
            this.cooldownType = cooldownType;
            this.animState = animState;
            this.crouchingVariant = crouchingVariant;
            this.aerialVariant = aerialVariant;
            this.followup = followup;

            if (this.crouchingVariant != null) {
                ((AbstractMove<?, A>) this.move).withCrouchingVariant(this.crouchingVariant.getMove().markCrouchingVariant());
            }

            if (this.aerialVariant != null) {
                ((AbstractMove<?, A>) this.move).withAerialVariant(this.aerialVariant.getMove().markAerialVariant());
            }

            if (this.followup != null) {
                ((AbstractMove<?, A>) this.move).withFollowup(this.followup.getMove().markFollowup());
            }

            this.move.onRegister(moveClass);
        }

        @Override
        public MoveMap.Entry<A, S> withCrouchingVariant(final CooldownType cooldownType, final S animState) {
            if (move.getCrouchingVariant() == null) {
                throw new IllegalArgumentException("The move of this entry has " +
                        "no crouching variant.");
            }
            crouchingVariant = new EntryImpl<A, S>(moveClass, move.getCrouchingVariant(), cooldownType, animState);
            return crouchingVariant;
        }

        @Override
        public MoveMap.Entry<A, S> withAerialVariant(final CooldownType cooldownType, final S animState) {
            if (move.getAerialVariant() == null) {
                throw new IllegalArgumentException("The move of this entry has " +
                        "no aerial variant.");
            }
            aerialVariant = new EntryImpl<A, S>(moveClass, move.getAerialVariant(), cooldownType, animState);
            return aerialVariant;
        }

        @Override
        public MoveMap.Entry<A, S> withFollowup(final CooldownType cooldownType, final S animState) {
            if (move.getFollowup() == null) {
                throw new IllegalArgumentException("The move of this entry has " +
                        "no follow-up.");
            }
            followup = new EntryImpl<A, S>(moveClass, move.getFollowup(), cooldownType, animState);
            return followup;
        }

        @Override
        public EntryImpl<A, S> copy() {
            return new EntryImpl<>(moveClass, move.copy(), cooldownType, animState,
                    crouchingVariant != null ? crouchingVariant.copy() : null,
                    aerialVariant != null ? aerialVariant.copy() : null,
                    followup != null ? followup.copy() : null);
        }

        @Override
        public String toString() {
            return "Type: " + moveClass + ", Move name: " + move.getName() + ", Move desc: " + move.getDescription();
        }
    }
}
