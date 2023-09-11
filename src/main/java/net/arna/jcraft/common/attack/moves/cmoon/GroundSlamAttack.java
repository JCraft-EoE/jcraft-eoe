package net.arna.jcraft.common.attack.moves.cmoon;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.CMoonEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.Vec3d;

public class GroundSlamAttack extends AbstractSimpleAttack<GroundSlamAttack, CMoonEntity> {
    public GroundSlamAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                            float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    protected void processTarget(CMoonEntity attacker, LivingEntity target, Vec3d kbVec, DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);

        LivingEntity user = attacker.getUserOrThrow();
        GravityChangerAPI.setWorldVelocity(target, GravityChangerAPI.getGravityDirection(user).getUnitVector());
        target.velocityModified = true;
        if (user.isSneaking())
            target.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 30, 0, true, false));
    }

    @Override
    protected @NonNull GroundSlamAttack getThis() {
        return this;
    }

    @Override
    public @NonNull GroundSlamAttack copy() {
        return copyExtras(new GroundSlamAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
