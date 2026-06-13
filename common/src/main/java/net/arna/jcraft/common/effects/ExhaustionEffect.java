package net.arna.jcraft.common.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ExhaustionEffect extends MobEffect {

    public static final int MAX_LEVEL = 4;

    public ExhaustionEffect() {
        super(MobEffectCategory.HARMFUL, 0xDC143C);
    }

    @Override
    public boolean isDurationEffectTick(final int duration, final int amplifier) {
        return false;
    }

}
