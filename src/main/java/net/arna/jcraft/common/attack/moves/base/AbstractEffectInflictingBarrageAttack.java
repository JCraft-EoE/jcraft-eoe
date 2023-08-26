package net.arna.jcraft.common.attack.moves.base;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class AbstractEffectInflictingBarrageAttack<T extends AbstractEffectInflictingBarrageAttack<T, A>, A extends IAttacker<? extends A, ?>>
        extends AbstractBarrageAttack<T, A> {
    private final List<StatusEffectInstance> effects = new ArrayList<>();

    protected AbstractEffectInflictingBarrageAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                                                    float hitboxSize, float knockback, float offset, int interval,
                                                    @NonNull List<StatusEffectInstance> effects) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, interval);
        this.effects.addAll(effects);
    }

    @Override
    protected void processTarget(A attacker, LivingEntity target, Vec3d kbVec, DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);

        AbstractEffectInflictingAttack.inflictEffects(target, effects, getBlockableType().isNonBlockableEffects());
    }
}
