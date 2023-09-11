package net.arna.jcraft.common.attack.moves.madeinheaven;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.MadeInHeavenEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class FuryChopAttack extends AbstractSimpleAttack<FuryChopAttack, MadeInHeavenEntity> {
    public FuryChopAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                          float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        hitSpark = JParticleType.HIT_SPARK_2;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(MadeInHeavenEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        // Punish user with mining fatigue on miss, reward with haste on hit.
        user.addStatusEffect(new StatusEffectInstance(targets.isEmpty() ? StatusEffects.MINING_FATIGUE : StatusEffects.HASTE,
                160, 0));

        return targets;
    }

    @Override
    protected void processTarget(MadeInHeavenEntity attacker, LivingEntity target, Vec3d kbVec, DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);

        target.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 160, 0));
    }

    @Override
    protected @NonNull FuryChopAttack getThis() {
        return this;
    }

    @Override
    public @NonNull FuryChopAttack copy() {
        return copyExtras(new FuryChopAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
