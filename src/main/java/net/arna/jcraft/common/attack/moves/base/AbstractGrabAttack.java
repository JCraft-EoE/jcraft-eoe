package net.arna.jcraft.common.attack.moves.base;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

@Getter
public abstract class AbstractGrabAttack<T extends AbstractGrabAttack<T, S, A>, S extends StandEntity<S, A>, A extends Enum<A> & StandAnimationState<S>>
        extends AbstractSimpleAttack<T, S> {
    private final AbstractMove<?, S> hitMove;
    private final A hitState;

    public AbstractGrabAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun, float hitboxSize,
                              float knockback, float offset, AbstractMove<?, S> hitMove, A hitState) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset);
        grab = true;
        this.hitMove = hitMove;
        this.hitState = hitState;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(S attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);
        if (targets.isEmpty()) return targets;

        attacker.setMove(hitMove, hitState);
        return targets;
    }
}
