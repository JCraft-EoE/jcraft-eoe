package net.arna.jcraft.common.attack.moves.shared;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.Set;

@Getter
public class KnockdownAttack<S extends StandEntity<?, ?>> extends AbstractSimpleAttack<KnockdownAttack<S>, S> {
    private final int knockdownDuration;

    public KnockdownAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun,
                           float hitboxSize, float knockback, float offset, int knockdownDuration) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset);
        this.knockdownDuration = knockdownDuration;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(S attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);
        for (LivingEntity target : targets)
            if (!JUtils.isBlocking(target))
                target.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, knockdownDuration, 0));

        return targets;
    }

    @Override
    protected @NonNull KnockdownAttack<S> getThis() {
        return this;
    }

    @Override
    public @NonNull KnockdownAttack<S> copy() {
        return new KnockdownAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), knockdownDuration);
    }
}
