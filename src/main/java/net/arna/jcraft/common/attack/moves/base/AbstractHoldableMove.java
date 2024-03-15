package net.arna.jcraft.common.attack.moves.base;

import lombok.Getter;
import net.arna.jcraft.common.attack.core.IAttacker;

@Getter
public abstract class AbstractHoldableMove<T extends AbstractHoldableMove<T, A, S>, A extends IAttacker<A, S>, S>
        extends AbstractMove<T, A> {
    private final AbstractMove<?, ? super A> followupMove;
    private final S followupState;
    private final int minimumCharge;
    // Maximum charge is the end of the move

    protected AbstractHoldableMove(int cooldown, int windup, int duration, float attackDistance,
                                   AbstractMove<?, ? super A> followupMove, S followupState, int minimumCharge) {
        super(cooldown, windup, duration, attackDistance);

        this.followupMove = followupMove;
        this.followupState = followupState;
        this.minimumCharge = minimumCharge;

        withHoldable();
    }

    @Override
    public void tick(A attacker) {
        super.tick(attacker);
        //todo: add a marker for "already released", so holdable moves that arent held dont last till max
        //        finish sc spin
        if (attacker.getMoveStun() == 1)
            attacker.setMove(followupMove, followupState);
    }

    public <A extends IAttacker<? extends A, S>, S> void onRelease(IAttacker<A,S> attacker) {
        if (attacker.getMoveStun() <= getDuration() - minimumCharge)
            attacker.setMove((AbstractMove<?, ? super A>) followupMove, (S) followupState);
    }
}
