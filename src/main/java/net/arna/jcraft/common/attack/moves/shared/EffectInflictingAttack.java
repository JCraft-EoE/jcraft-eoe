package net.arna.jcraft.common.attack.moves.shared;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.moves.base.AbstractEffectInflictingAttack;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.List;

@Getter
public class EffectInflictingAttack<A extends IAttacker<? extends A, ?>> extends AbstractEffectInflictingAttack<EffectInflictingAttack<A>, A> {
    public EffectInflictingAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                                  float hitboxSize, float knockback, float offset, @NonNull List<StatusEffectInstance> effects) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, effects);
    }

    @Override
    protected @NonNull EffectInflictingAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull EffectInflictingAttack<A> copy() {
        return copyExtras(new EffectInflictingAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getEffects()));
    }
}
