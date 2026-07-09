package net.arna.jcraft.common.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class SpeclessEffect extends MobEffect {
    public SpeclessEffect() {
        super(MobEffectCategory.NEUTRAL, 0x000000);
    }

    @Override
    public boolean isDurationEffectTick(final int duration, final int amplifier) {
        return true;
    }
}
