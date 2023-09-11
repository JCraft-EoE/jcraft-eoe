package net.arna.jcraft.common.attack.moves.goldexperience.requiem;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.GEREntity;
import net.arna.jcraft.common.util.JParticleType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

public class OverheadKickAttack extends AbstractSimpleAttack<OverheadKickAttack, GEREntity> {
    public OverheadKickAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                              float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        hitSpark = JParticleType.HIT_SPARK_2;
    }

    @Override
    protected void processTarget(GEREntity attacker, LivingEntity target, Vec3d kbVec, DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);

        target.addVelocity(0, -1, 0);
        target.velocityModified = true;
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 5, 19, false, false));
    }

    @Override
    protected @NonNull OverheadKickAttack getThis() {
        return this;
    }

    @Override
    public @NonNull OverheadKickAttack copy() {
        return copyExtras(new OverheadKickAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
