package net.arna.jcraft.common.attack.moves.shared;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.moves.base.AbstractGrabAttack;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.util.StandAnimationState;
import software.bernie.geckolib.animatable.GeoEntity;

public class GrabAttack<A extends IAttacker<A, S> & GeoEntity, S extends Enum<S> & StandAnimationState<A>>
        extends AbstractGrabAttack<GrabAttack<A, S>, A, S> {

    public GrabAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun,
                      float hitboxSize, float knockback, float offset, AbstractMove<?, ? super A> hitMove, S hitState, int grabDuration, double grabOffset) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset, hitMove, hitState, grabDuration, grabOffset);
    }

    public GrabAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun,
                      float hitboxSize, float knockback, float offset, AbstractMove<?, ? super A> hitMove, S hitState) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset, hitMove, hitState);
    }

    @Override
    protected @NonNull GrabAttack<A, S> getThis() {
        return this;
    }

    @Override
    public @NonNull GrabAttack<A, S> copy() {
        return copyExtras(new GrabAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getHitMove(), getHitState(), getGrabDuration(), getGrabOffset()));
    }
}
