package net.arna.jcraft.common.attack.moves.shared;

import it.unimi.dsi.fastutil.ints.IntCollection;
import net.arna.jcraft.common.attack.core.base.AbstractMultiHitAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;

/**
 * The simplest implementation of {@link AbstractMultiHitAttack}.
 * Only special feature is that it fires the same hit-box at set points.
 * @param <S>
 */
public class SimpleMultiHitAttack<S extends StandEntity<?, ?>> extends AbstractMultiHitAttack<SimpleMultiHitAttack<S>, S> {
    public SimpleMultiHitAttack(int cooldown, int moveStunTicks, float damage, float hitBoxSize, float knockBack, float range, float offset, IntCollection hitMoments) {
        super(cooldown, moveStunTicks, range, damage, hitBoxSize, knockBack, offset, hitMoments);
    }

    /**
     * For light attacks
     * @param moveStunTicks The duration after which a new attack can be initiated in ticks.
     * @param damage The damage this attack deals.
     * @param attackDistance The distance at which the hit-box is placed.
     * @param offset The amount the hit-box is offset by.
     * @param hitMoments The ticks at which this attack is performed.
     */
    public static <S extends StandEntity<?, ?>> SimpleMultiHitAttack<S> lightAttack(int moveStunTicks, float damage,
                                                                                    float attackDistance, float offset,
                                                                                    IntCollection hitMoments) {
        return new SimpleMultiHitAttack<>(30, moveStunTicks, damage, 1.5f, 0.75f,
                attackDistance, offset, hitMoments);
    }

    @Override
    protected SimpleMultiHitAttack<S> getThis() {
        return this;
    }
}
