package net.arna.jcraft.common.effects;

import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class BleedingEffect extends StatusEffect {
    public BleedingEffect() {
        super(StatusEffectCategory.HARMFUL, 0x6F1616);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int i = 40 >> amplifier;
        if (i > 0)
            return duration % i == 0;
        else
            return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        entity.damage(JDamageSources.bleeding(), 1);
    }
}
