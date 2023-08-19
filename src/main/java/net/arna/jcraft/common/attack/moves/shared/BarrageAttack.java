package net.arna.jcraft.common.attack.moves.shared;

import net.arna.jcraft.common.attack.moves.base.AbstractBarrageAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;

/**
 * A simple attack that performs at a set interval.
 */
public class BarrageAttack<S extends StandEntity<?, ?>> extends AbstractBarrageAttack<BarrageAttack<S>, StandEntity<?, ?>> {

    public BarrageAttack(int cooldown, int windup, int duration, float attackDistance, float damage,
                         float hitBoxSize, float knockBack, float offset, int interval) {
        super(cooldown, windup, duration, attackDistance, damage, hitBoxSize, knockBack, offset, interval);
    }

    @Override
    protected BarrageAttack<S> getThis() {
        return this;
    }
}
