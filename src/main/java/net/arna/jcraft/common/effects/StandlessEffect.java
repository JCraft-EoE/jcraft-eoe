package net.arna.jcraft.common.effects;

import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class StandlessEffect extends StatusEffect {
    public StandlessEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x000000);
    }

    // Should the status effect be applied and under what condition?
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        StandEntity stand = ((IEntityDataSaver)entity).getStand();
        if (stand != null)
            stand.desummon();
    }
}
