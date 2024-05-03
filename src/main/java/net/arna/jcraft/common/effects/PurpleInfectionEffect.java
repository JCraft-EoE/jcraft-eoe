package net.arna.jcraft.common.effects;

import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.stand.StandType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class PurpleInfectionEffect extends StatusEffect {
    public PurpleInfectionEffect() {
        super(StatusEffectCategory.HARMFUL, 0xA34AB5);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int i = 0b101000 >> amplifier;
        if (i > 0) {
            return duration % i == 0;
        } else {
            return true;
        }
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        StandType standType = JComponents.getStandData(entity).getType();
        float damage = 0.6666f; // 1/3rd of a heart
        if (standType == StandType.PURPLE_HAZE_DISTORTION)
            damage /= 3.0f;
        entity.damage(JDamageSources.phpoison(entity.getWorld()), damage);
    }
}
