package net.arna.jcraft.common.attack.moves.shared;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.base.AbstractMove;
import net.arna.jcraft.common.attack.core.base.AbstractSimpleAttack;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class GrabAttack<S extends StandEntity<S, A>, A extends Enum<A> & StandAnimationState<S>> extends AbstractSimpleAttack<GrabAttack<S, A>, S> {
    private final AbstractMove<?, S> hitMove;
    private final A hitState;

    public GrabAttack(int cooldown, int windup, int moveStunTicks, float attackDistance, float damage, float hitBoxSize,
                      float knockBack, float offset, AbstractMove<?, S> hitMove, A hitState) {
        super(cooldown, windup, moveStunTicks, attackDistance, damage, hitBoxSize, knockBack, offset);
        grab = true;
        this.hitMove = hitMove;
        this.hitState = hitState;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(S stand, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(stand, user, ctx);
        if (targets.isEmpty()) return targets;

        stand.setAttack(hitMove, hitState);
        return targets;
    }

    @Override
    protected GrabAttack<S, A> getThis() {
        return this;
    }
}
