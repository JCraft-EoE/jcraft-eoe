package net.arna.jcraft.common.attack.moves.base;

import lombok.Getter;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.MoveInputType;

@Getter
public abstract class AbstractHoldableMove<T extends AbstractHoldableMove<T, A, S>, A extends IAttacker<A, S>, S>
        extends AbstractMove<T, A> {
    private final AbstractMove<?, ? super A> followupMove;
    protected boolean setMoveStun = false;
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

    public T shouldSetMoveStun() {
        setMoveStun = true;
        return getThis();
    }

    @Override
    public void tick(A attacker) {
        super.tick(attacker);
        int moveStun = attacker.getMoveStun();
        if (moveStun == 1 || (!attacker.isHolding() && moveStun <= getDuration() - minimumCharge))
            followUp(attacker);
    }

    @Override
    public void onUserMoveInput(A attacker, MoveInputType type, boolean pressed, boolean moveInitiated) {
        if (!pressed && moveInitiated && type.getMoveType() == getMoveType()) onRelease(attacker);
    }

    public void onRelease(A attacker) {
        if (attacker.getMoveStun() <= getDuration() - minimumCharge)
            followUp(attacker);
    }

    private void followUp(A attacker) {
        attacker.setMove(followupMove, followupState);
        if (setMoveStun)
            attacker.setMoveStun(followupMove.getDuration());
    }
}
