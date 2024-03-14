package net.arna.jcraft.common.attack.moves.base;

import lombok.Getter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.IAttacker;

@Getter
public abstract class AbstractHoldableMove<T extends AbstractHoldableMove<T, A, S>, A extends IAttacker<A, S>, S>
        extends AbstractMove<T, A> {
    private final AbstractMove<?, ? super A> followupMove;
    private final S followupState;
    private final int minimumCharge, maximumCharge;

    protected AbstractHoldableMove(int cooldown, int windup, int duration, float attackDistance,
                                   AbstractMove<?, ? super A> followupMove, S followupState, int minimumCharge, int maximumCharge) {
        super(cooldown, windup, duration, attackDistance);

        this.followupMove = followupMove;
        this.followupState = followupState;
        this.minimumCharge = minimumCharge;
        this.maximumCharge = maximumCharge;

        withHoldable();
    }

    public void onRelease() {
        JCraft.LOGGER.info("released");
    }
}
