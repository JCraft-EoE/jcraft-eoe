package net.arna.jcraft.common.attack.moves.vampire;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.base.AbstractSpecGrabAttack;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.util.SpecAnimationState;

public class BloodSuckAttack<A extends JSpec<A, S>, S extends Enum<S> & SpecAnimationState<A>> extends AbstractSpecGrabAttack<BloodSuckAttack<A, S>, A, S> {

    public BloodSuckAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun,
                      float hitboxSize, float knockback, float offset, AbstractMove<?, ? super A> hitMove, S hitState, int grabDuration, double grabOffset) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset, hitMove, hitState, grabDuration, grabOffset);
    }

    public BloodSuckAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun,
                      float hitboxSize, float knockback, float offset, AbstractMove<?, ? super A> hitMove, S hitState) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset, hitMove, hitState);
    }

    @Override
    protected @NonNull BloodSuckAttack<A, S> getThis() {
        return this;
    }

    @Override
    public @NonNull BloodSuckAttack<A, S> copy() {
        return copyExtras(new BloodSuckAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getHitMove(), getHitState(), getGrabDuration(), getGrabOffset()));
    }
}
