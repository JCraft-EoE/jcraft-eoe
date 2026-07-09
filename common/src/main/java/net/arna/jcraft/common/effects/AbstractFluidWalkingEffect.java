package net.arna.jcraft.common.effects;

import lombok.Getter;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.level.material.Fluid;

public abstract class AbstractFluidWalkingEffect extends MobEffect {

    protected AbstractFluidWalkingEffect(final int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }

    public abstract boolean supports(final Fluid fluid);

}
