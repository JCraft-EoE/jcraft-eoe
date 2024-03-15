package net.arna.jcraft.common.attack.moves.shared;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractHoldableMove;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.entity.LivingEntity;
import software.bernie.geckolib3.core.IAnimatable;

import java.util.Set;

public class HoldableMove <A extends IAttacker<A, S> & IAnimatable, S extends Enum<S> & StandAnimationState<A>> extends AbstractHoldableMove<HoldableMove<A, S>, A, S> {
    public HoldableMove(int cooldown, int windup, int duration, float attackDistance, AbstractMove<?, ? super A> followupMove, S followupState, int minimumCharge) {
        super(cooldown, windup, duration, attackDistance, followupMove, followupState, minimumCharge);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        return Set.of();
    }

    @Override
    protected @NonNull HoldableMove<A, S> getThis() {
        return this;
    }

    @Override
    public @NonNull HoldableMove<A, S> copy() {
        return copyExtras(new HoldableMove<>(getCooldown(), getWindup(), getDuration(), getArmor(), getFollowupMove(), getFollowupState(), getMinimumCharge()));
    }
}
