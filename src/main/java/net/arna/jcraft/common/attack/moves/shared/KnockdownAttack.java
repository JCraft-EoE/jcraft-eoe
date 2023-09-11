package net.arna.jcraft.common.attack.moves.shared;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.moves.base.AbstractEffectInflictingAttack;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.List;

@Getter
public class KnockdownAttack<A extends IAttacker<? extends A, ?>> extends AbstractEffectInflictingAttack<KnockdownAttack<A>, A> {
    private final int knockdownDuration;

    public KnockdownAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                           float hitboxSize, float knockback, float offset, int knockdownDuration) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset,
                List.of(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, knockdownDuration, 0)));
        this.knockdownDuration = knockdownDuration;
    }

    @Override
    protected @NonNull KnockdownAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull KnockdownAttack<A> copy() {
        return copyExtras(new KnockdownAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getKnockdownDuration()));
    }
}
