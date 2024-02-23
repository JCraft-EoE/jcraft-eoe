package net.arna.jcraft.common.attack.moves.cream;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractEffectInflictingAttack;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.stand.CreamEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class DestroyAttack extends AbstractEffectInflictingAttack<DestroyAttack, CreamEntity> {
    public DestroyAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                         float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset,
                List.of(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 35, 0, true, false)));
        hitSpark = JParticleType.HIT_SPARK_3;
    }

    @Override
    protected void processTarget(CreamEntity attacker, LivingEntity target, Vec3d kbVec, DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);

        StandEntity.trueDamage(8, JDamageSources.stand(attacker), target);
    }

    @Override
    protected @NonNull DestroyAttack getThis() {
        return this;
    }

    @Override
    public @NonNull DestroyAttack copy() {
        return copyExtras(new DestroyAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
