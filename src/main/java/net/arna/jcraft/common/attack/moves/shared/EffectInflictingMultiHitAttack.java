package net.arna.jcraft.common.attack.moves.shared;

import it.unimi.dsi.fastutil.ints.IntCollection;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractEffectInflictingMultiHitAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.List;

@Getter
public class EffectInflictingMultiHitAttack<S extends StandEntity<?, ?>> extends AbstractEffectInflictingMultiHitAttack<EffectInflictingMultiHitAttack<S>, S> {
    public EffectInflictingMultiHitAttack(int cooldown, int duration, float attackDistance, float damage, int stun,
                                          float hitboxSize, float knockback, float offset,
                                          @NonNull IntCollection hitMoments, @NonNull List<StatusEffectInstance> effects) {
        super(cooldown, duration, attackDistance, damage, stun, hitboxSize, knockback, offset, hitMoments, effects);
    }

    @Override
    protected @NonNull EffectInflictingMultiHitAttack<S> getThis() {
        return this;
    }

    @Override
    public @NonNull EffectInflictingMultiHitAttack<S> copy() {
        return copyExtras(new EffectInflictingMultiHitAttack<>(getCooldown(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getHitMoments(), getEffects()));
    }
}
