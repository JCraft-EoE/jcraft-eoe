package net.arna.jcraft.common.attack.moves.shared;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractChargeAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.StandAnimationState;

public class ChargeAttack<S extends StandEntity<S, A>, A extends Enum<A> & StandAnimationState<S>>
        extends AbstractChargeAttack<ChargeAttack<S, A>, S, A> {
    public ChargeAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                        float hitboxSize, float knockback, float offset, A hitAnimState) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitAnimState);
    }

    @Override
    protected @NonNull ChargeAttack<S, A> getThis() {
        return this;
    }

    @Override
    public @NonNull ChargeAttack<S, A> copy() {
        return copyExtras(new ChargeAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getHitAnimState()));
    }
}
