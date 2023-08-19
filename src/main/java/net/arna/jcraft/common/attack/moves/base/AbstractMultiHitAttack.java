package net.arna.jcraft.common.attack.moves.base;

import it.unimi.dsi.fastutil.ints.*;
import lombok.NonNull;
import net.arna.jcraft.common.entity.stand.StandEntity;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple attack that performs at set points.
 * These points are the time in ticks from when the attack is initiated.
 * @param <T>
 * @param <S>
 */
public abstract class AbstractMultiHitAttack<T extends AbstractMultiHitAttack<T, S>, S extends StandEntity<?, ?>> extends AbstractSimpleAttack<T, S> {
    private final IntSortedSet hitMoments;

    protected AbstractMultiHitAttack(int cooldown, int duration, float attackDistance, float damage, int stun,
                                     float hitBoxSize, float knockBack, float offset, @NonNull IntCollection hitMoments) {
        super(cooldown, hitMoments.intStream().min().orElse(0), duration, attackDistance, damage, stun, hitBoxSize, knockBack, offset);

        // Ensure hitMoments is sorted
        IntSortedSet intermediary = new IntLinkedOpenHashSet();
        hitMoments.intStream()
                .sorted()
                .forEachOrdered(intermediary::add);
        this.hitMoments = IntSortedSets.unmodifiable(intermediary);
    }

    @Override
    protected boolean shouldPerform(S stand) {
        return stand.hasUser() && hitMoments.contains(getDuration() - stand.getMoveStun());
    }

    @Override
    public int getBlow(S stand) {
        int tick = getDuration() - stand.getMoveStun();
        AtomicInteger blow = new AtomicInteger(-1);
        hitMoments.forEach(i -> {
            if (i >= tick) blow.getAndIncrement();
        });

        return super.getBlow(stand);
    }
}
