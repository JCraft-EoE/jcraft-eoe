package net.arna.jcraft.common.attack.moves.shared;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.moves.base.AbstractEffectInflictingBarrageAttack;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.List;

@Getter
public class EffectInflictingBarrageAttack<A extends IAttacker<? extends A, ?>> extends AbstractEffectInflictingBarrageAttack<EffectInflictingBarrageAttack<A>, A> {
    public EffectInflictingBarrageAttack(int cooldown, int windup, int duration, float attackDistance, float damage,
                                         int stun, float hitboxSize, float knockback, float offset, int interval,
                                         @NonNull List<StatusEffectInstance> effects) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset, interval, effects);
    }

    @Override
    protected @NonNull EffectInflictingBarrageAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull EffectInflictingBarrageAttack<A> copy() {
        return copyExtras(new EffectInflictingBarrageAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), getInterval(), getEffects()));
    }
}
