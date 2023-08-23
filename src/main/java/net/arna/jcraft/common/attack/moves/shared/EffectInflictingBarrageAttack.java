package net.arna.jcraft.common.attack.moves.shared;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractEffectInflictingBarrageAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.List;

@Getter
public class EffectInflictingBarrageAttack<S extends StandEntity<?, ?>> extends AbstractEffectInflictingBarrageAttack<EffectInflictingBarrageAttack<S>, S> {
    public EffectInflictingBarrageAttack(int cooldown, int windup, int duration, float attackDistance, float damage,
                                         int stun, float hitboxSize, float knockback, float offset, int interval,
                                         @NonNull List<StatusEffectInstance> effects) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset, interval, effects);
    }

    @Override
    protected @NonNull EffectInflictingBarrageAttack<S> getThis() {
        return this;
    }

    @Override
    public @NonNull EffectInflictingBarrageAttack<S> copy() {
        return copyExtras(new EffectInflictingBarrageAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), getInterval(), getEffects()));
    }
}
