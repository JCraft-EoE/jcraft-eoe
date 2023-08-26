package net.arna.jcraft.common.attack.moves.shared;

import it.unimi.dsi.fastutil.ints.IntCollection;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.moves.base.AbstractEffectInflictingMultiHitAttack;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.List;

@Getter
public class KnockdownMultiHitAttack<A extends IAttacker<? extends A, ?>> extends AbstractEffectInflictingMultiHitAttack<KnockdownMultiHitAttack<A>, A> {
    private final int knockdownDuration;

    public KnockdownMultiHitAttack(int cooldown, int duration, float attackDistance, float damage, int stun, float hitboxSize,
                                   float knockback, float offset, @NonNull IntCollection hitMoments, int knockdownDuration) {
        super(cooldown, duration, attackDistance, damage, stun, hitboxSize, knockback, offset, hitMoments,
                List.of(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, knockdownDuration, 0, true, false)));
        this.knockdownDuration = knockdownDuration;
    }

    @Override
    protected @NonNull KnockdownMultiHitAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull KnockdownMultiHitAttack<A> copy() {
        return copyExtras(new KnockdownMultiHitAttack<>(getCooldown(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getHitMoments(), knockdownDuration));
    }
}
