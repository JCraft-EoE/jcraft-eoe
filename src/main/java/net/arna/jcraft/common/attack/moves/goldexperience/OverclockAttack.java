package net.arna.jcraft.common.attack.moves.goldexperience;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.StunType;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.GoldExperienceEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class OverclockAttack extends AbstractSimpleAttack<OverclockAttack, GoldExperienceEntity> {
    public OverclockAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun,
                           float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset);
        withStunType(StunType.LAUNCH);
        hitSpark = JParticleType.HIT_SPARK_3;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(GoldExperienceEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        for (LivingEntity target : targets) {
            target.addStatusEffect(new StatusEffectInstance(JStatusRegistry.DAZED, 60, 3, true, false));
            target.addStatusEffect(new StatusEffectInstance(JStatusRegistry.OUTOFBODY, 60, 0, false, true));

            Vec3d upDir = new Vec3d(GravityChangerAPI.getGravityDirection(user).getUnitVector());
            JUtils.setVelocity(target, upDir.multiply(-0.8));
        }

        return targets;
    }

    @Override
    protected @NonNull OverclockAttack getThis() {
        return this;
    }

    @Override
    public @NonNull OverclockAttack copy() {
        return copyExtras(new OverclockAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset()));
    }
}
