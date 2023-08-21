package net.arna.jcraft.common.attack.moves.shared;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.moves.base.AbstractBarrageAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;

/**
 * A simple attack that performs at a set interval.
 */
public class BarrageAttack<A extends IAttacker<?, ?>> extends AbstractBarrageAttack<BarrageAttack<A>, StandEntity<?, ?>> {

    public BarrageAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun,
                         float hitboxSize, float knockback, float offset, int interval) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset, interval);
    }

    @Override
    protected @NonNull BarrageAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull BarrageAttack<A> copy() {
        return copyExtras(new BarrageAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getInterval()));
    }
}
