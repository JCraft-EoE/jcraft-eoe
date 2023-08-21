package net.arna.jcraft.common.attack.moves.base;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
public abstract class AbstractEffectInflictingAttack<T extends AbstractEffectInflictingAttack<T, A>, A extends IAttacker<?, ?>>
        extends AbstractSimpleAttack<T, A> {
    private final List<StatusEffectInstance> effects = new ArrayList<>();

    protected AbstractEffectInflictingAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                                             float hitboxSize, float knockback, float offset, @NonNull List<StatusEffectInstance> effects) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        this.effects.addAll(effects);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);
        inflictEffects(targets, effects, getBlockableType().isNonBlockableEffects());

        return targets;
    }

    static void inflictEffects(Set<LivingEntity> targets, List<StatusEffectInstance> effects, boolean nonBlockableEffects) {
        // Copy the effects
        effects = effects.stream()
                .map(StatusEffectInstance::new)
                .toList();

        if (nonBlockableEffects)
            for (LivingEntity target : targets)
                effects.forEach(target::addStatusEffect);
        else for (LivingEntity target : targets)
            if (!JUtils.isBlocking(target))
                effects.forEach(target::addStatusEffect);
    }
}
