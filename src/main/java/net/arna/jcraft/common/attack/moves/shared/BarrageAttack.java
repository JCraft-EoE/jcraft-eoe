package net.arna.jcraft.common.attack.moves.shared;

import net.arna.jcraft.common.attack.moves.base.AbstractBarrageAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;

/**
 * A simple attack that performs at a set interval.
 */
public class BarrageAttack<S extends StandEntity<?, ?>> extends AbstractBarrageAttack<BarrageAttack<S>, StandEntity<?, ?>> {

    public BarrageAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun,
                         float hitBoxSize, float knockBack, float offset, int interval) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitBoxSize, knockBack, offset, interval);
    }

    @Override
    protected BarrageAttack<S> getThis() {
        return this;
    }

    @Override
    public BarrageAttack<S> copy() {
        return new BarrageAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitBoxSize(), getKnockBack(), getOffset(), getInterval());
    }
}
