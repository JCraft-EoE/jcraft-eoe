package net.arna.jcraft.common.attack.moves.base;

import it.unimi.dsi.fastutil.ints.IntCollection;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
public abstract class AbstractEffectInflictingMultiHitAttack<T extends AbstractEffectInflictingMultiHitAttack<T, A>, A extends IAttacker<?, ?>>
        extends AbstractMultiHitAttack<T, A> {
    private final List<StatusEffectInstance> effects = new ArrayList<>();

    protected AbstractEffectInflictingMultiHitAttack(int cooldown, int duration, float moveDistance, float damage, int stun,
                                                     float hitboxSize, float knockback, float offset,
                                                     @NonNull IntCollection hitMoments, @NonNull List<StatusEffectInstance> effects) {
        super(cooldown, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitMoments);
        this.effects.addAll(effects);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);
        AbstractEffectInflictingAttack.inflictEffects(targets, effects, getBlockableType().isNonBlockableEffects());

        return targets;
    }
}
