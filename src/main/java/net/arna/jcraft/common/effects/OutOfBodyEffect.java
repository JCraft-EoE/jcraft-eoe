package net.arna.jcraft.common.effects;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class OutOfBodyEffect extends StatusEffect {
    public OutOfBodyEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x5B6EE1);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false;
    }
}
