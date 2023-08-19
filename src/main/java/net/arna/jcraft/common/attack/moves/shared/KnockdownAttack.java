package net.arna.jcraft.common.attack.moves.shared;

import lombok.Getter;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Getter
public class KnockdownAttack<S extends StandEntity<?, ?>> extends AbstractSimpleAttack<KnockdownAttack<S>, S> {
    private final int knockdownDuration;

    public KnockdownAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun,
                           float hitBoxSize, float knockBack, float offset, int knockdownDuration) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitBoxSize, knockBack, offset);
        this.knockdownDuration = knockdownDuration;
    }

    @Override
    public @NotNull Set<LivingEntity> perform(S stand, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(stand, user, ctx);
        for (LivingEntity target : targets)
            if (!JUtils.isBlocking(target))
                target.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, knockdownDuration, 0));

        return targets;
    }

    @Override
    protected KnockdownAttack<S> getThis() {
        return this;
    }

    @Override
    public KnockdownAttack<S> copy() {
        return new KnockdownAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitBoxSize(), getKnockBack(), getOffset(), knockdownDuration);
    }
}
