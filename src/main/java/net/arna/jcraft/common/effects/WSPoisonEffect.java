package net.arna.jcraft.common.effects;

import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.mob.MobEntity;

public class WSPoisonEffect extends StatusEffect {

    public WSPoisonEffect() {
        super(StatusEffectCategory.HARMFUL, 0xccccdd);
    }

    // Should the status effect be applied and under what condition?
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    // Stun heavily reduces speed
    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity instanceof MobEntity mob) {
            mob.setVelocity(mob.getVelocity().multiply(0.2));

            mob.setTarget(null);
            mob.setAttacking(false);
        } else entity.setPose(EntityPose.SWIMMING);
    }
}
