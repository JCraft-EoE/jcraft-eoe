package net.arna.jcraft.common.attack.moves.shared;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.*;

@Getter
public class EffectInflictingAttack<S extends StandEntity<?, ?>> extends AbstractSimpleAttack<EffectInflictingAttack<S>, S> {
    private final ArrayList<StatusEffectInstance> inflictedEffects = new ArrayList<>();

    public EffectInflictingAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun,
                                  float hitboxSize, float knockback, float offset, List<StatusEffectInstance> effects) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset);
        inflictedEffects.addAll(effects);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(S attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);
        if (getBlockableType().isNonBlockableEffects())
            for (LivingEntity target : targets)
                inflictedEffects.forEach(target::addStatusEffect);
        else
            for (LivingEntity target : targets)
                if (!JUtils.isBlocking(target))
                    inflictedEffects.forEach(target::addStatusEffect);

        return targets;
    }

    @Override
    protected @NonNull EffectInflictingAttack<S> getThis() {
        return this;
    }

    @Override
    public @NonNull EffectInflictingAttack<S> copy() {
        return new EffectInflictingAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getInflictedEffects());
    }
}
