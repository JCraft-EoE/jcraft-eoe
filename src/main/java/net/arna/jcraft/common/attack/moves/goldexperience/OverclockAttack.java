package net.arna.jcraft.common.attack.moves.goldexperience;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.GoldExperienceEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.Set;

public class OverclockAttack extends AbstractSimpleAttack<OverclockAttack, GoldExperienceEntity> {
    public OverclockAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun,
                           float hitBoxSize, float knockBack, float offset) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitBoxSize, knockBack, offset);
        hitSpark = JParticleType.HIT_SPARK_2;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(GoldExperienceEntity stand, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(stand, user, ctx);

        for (LivingEntity target : targets) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 5, 14, true, false));
            target.addStatusEffect(new StatusEffectInstance(JStatusRegistry.DAZED, 60, 1, true, false));
            target.addStatusEffect(new StatusEffectInstance(JStatusRegistry.OUTOFBODY, 60, 0, false, true));
        }

        return targets;
    }

    @Override
    protected OverclockAttack getThis() {
        return this;
    }

    @Override
    public OverclockAttack copy() {
        return new OverclockAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitBoxSize(), getKnockBack(), getOffset());
    }
}
