package net.arna.jcraft.common.effects;

import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;

public class KnockdownStatusEffect extends StatusEffect {

    public KnockdownStatusEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x444444);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    // Knockdown prevents attacking, and sets you into a horizontal pose
    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        StatusEffectInstance self = entity.getStatusEffect(JStatusRegister.Knockdown);
        if (self.getDuration() > 6) { // 5 tick (0.25s) stun immunity window after knockdown
            entity.setPose(entity instanceof PlayerEntity ? EntityPose.SWIMMING : EntityPose.SLEEPING);
        } else {
            entity.setPose(EntityPose.STANDING);
            entity.removeStatusEffect(JStatusRegister.Dazed);
        }
    }
}