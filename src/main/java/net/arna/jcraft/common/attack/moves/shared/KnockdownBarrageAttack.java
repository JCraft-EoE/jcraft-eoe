package net.arna.jcraft.common.attack.moves.shared;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.moves.base.AbstractEffectInflictingBarrageAttack;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.List;

@Getter
public class KnockdownBarrageAttack<A extends IAttacker<? extends A, ?>> extends AbstractEffectInflictingBarrageAttack<KnockdownBarrageAttack<A>, A> {
    private final int knockdownDuration;

    public KnockdownBarrageAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                                  float hitboxSize, float knockback, float offset, int interval, int knockdownDuration) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, interval,
                List.of(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, knockdownDuration, 0, true, false)));
        this.knockdownDuration = knockdownDuration;
    }

    @Override
    protected @NonNull KnockdownBarrageAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull KnockdownBarrageAttack<A> copy() {
        return copyExtras(new KnockdownBarrageAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), getInterval(), getKnockdownDuration()));
    }
}
