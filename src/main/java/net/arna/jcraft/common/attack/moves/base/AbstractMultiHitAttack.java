package net.arna.jcraft.common.attack.moves.base;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.ints.IntSortedSets;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple attack that performs at set points.
 * These points are the time in ticks from when the attack is initiated.
 * @param <T>
 * @param <A>
 */
@Getter
public abstract class AbstractMultiHitAttack<T extends AbstractMultiHitAttack<T, A>, A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<T, A> {
    private IntSortedSet hitMoments;

    protected AbstractMultiHitAttack(int cooldown, int duration, float moveDistance, float damage, int stun,
                                     float hitboxSize, float knockback, float offset, @NonNull IntCollection hitMoments) {
        super(cooldown, hitMoments.intStream().min().orElse(0), duration, moveDistance, damage, stun, hitboxSize, knockback, offset);

        withHitMoments(hitMoments);
    }

    public T withHitMoments(IntCollection hitMoments) {
        // Ensure hitMoments is sorted
        IntSortedSet intermediary = new IntLinkedOpenHashSet();
        hitMoments.intStream()
                .sorted()
                .forEachOrdered(intermediary::add);
        this.hitMoments = IntSortedSets.unmodifiable(intermediary);
        return getThis();
    }

    @Override
    protected boolean shouldPerform(A attacker) {
        return attacker.hasUser() && hitMoments.contains(getDuration() - attacker.getMoveStun());
    }

    @Override
    public int getBlow(A attacker) {
        int tick = getDuration() - attacker.getMoveStun();
        AtomicInteger blow = new AtomicInteger(-1);
        hitMoments.forEach(i -> {
            if (tick >= i) blow.getAndIncrement();
        });

        return blow.get();
    }
}
