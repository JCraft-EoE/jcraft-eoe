package net.arna.jcraft.common.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Keratin Growth — applied by Herbal Tea.
 * Marker effect that signals 1.5x nail regen speed to Tusk stand entities.
 */
public class KeratinGrowthEffect extends MobEffect {
    public KeratinGrowthEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFD700);
    }
}
