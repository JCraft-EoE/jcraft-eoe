package net.arna.jcraft.common.attack.moves.shared;

import net.arna.jcraft.common.attack.moves.base.AbstractGrabAttack;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.StandAnimationState;

public class GrabAttack<S extends StandEntity<S, A>, A extends Enum<A> & StandAnimationState<S>> extends AbstractGrabAttack<GrabAttack<S, A>, S, A> {

    public GrabAttack(int cooldown, int windup, int duration, float attackDistance, float damage, float hitBoxSize,
                      float knockBack, float offset, AbstractMove<?, S> hitMove, A hitState) {
        super(cooldown, windup, duration, attackDistance, damage, , hitBoxSize, knockBack, offset, hitMove, hitState);
    }

    @Override
    protected GrabAttack<S, A> getThis() {
        return this;
    }
}
