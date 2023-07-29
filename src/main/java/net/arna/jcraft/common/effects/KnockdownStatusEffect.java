package net.arna.jcraft.common.effects;

import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;

// KNOCKDOWN prevents attacking, and sets you into a horizontal pose
public class KnockdownStatusEffect extends StatusEffect {

    public KnockdownStatusEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x444444);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration <= 5;
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);
        if (entity.hasVehicle())
            entity.stopRiding();
        entity.setPose(entity instanceof PlayerEntity ? EntityPose.SWIMMING : EntityPose.SLEEPING);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onRemoved(entity, attributes, amplifier);
        entity.setPose(EntityPose.STANDING);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {// 5 tick (0.25s) stun immunity window after knockdown
        entity.removeStatusEffect(JStatusRegistry.DAZED);
    }
}
