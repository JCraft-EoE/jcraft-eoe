package net.arna.jcraft.common.attack.moves.shared;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractEffectInflictingAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.List;

@Getter
public class EffectInflictingAttack<S extends StandEntity<?, ?>> extends AbstractEffectInflictingAttack<EffectInflictingAttack<S>, S> {
    public EffectInflictingAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                                  float hitboxSize, float knockback, float offset, @NonNull List<StatusEffectInstance> effects) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, effects);
    }

    @Override
    protected @NonNull EffectInflictingAttack<S> getThis() {
        return this;
    }

    @Override
    public @NonNull EffectInflictingAttack<S> copy() {
        return copyExtras(new EffectInflictingAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getEffects()));
    }
}
