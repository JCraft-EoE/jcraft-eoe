package net.arna.jcraft.common.attack.moves.shared;

import net.arna.jcraft.common.attack.core.base.AbstractBarrageAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;

/**
 * A simple attack that performs at a set interval.
 * @param <S>
 */
public class BarrageAttack<S extends StandEntity<?, ?>> extends AbstractBarrageAttack<BarrageAttack<S>, S> {

    public BarrageAttack(int cooldown, int windup, int moveStunTicks, float attackDistance, float damage,
                         float hitBoxSize, float knockBack, float offset, int interval) {
        super(cooldown, windup, moveStunTicks, attackDistance, damage, hitBoxSize, knockBack, offset, interval);
    }

    @Override
    protected BarrageAttack<S> getThis() {
        return this;
    }
}
