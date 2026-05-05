package net.arna.jcraft.common.effects;

import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;

public class FlammableEffect extends MobEffect {
    public FlammableEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF6600);
    }

    @Override
    public boolean isDurationEffectTick(final int duration, final int amplifier) {
        return duration % 5 == 0;
    }

    @Override
    public void applyEffectTick(final LivingEntity entity, final int amplifier) {
        if (!entity.isOnFire() || entity.level().isClientSide()) return;

        // Fire damage
        entity.hurt(entity.damageSources().onFire(), 1.0f);

        // Fire trail
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        if (level.getBlockState(pos).isAir()) {
            level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
        }
    }

    public static boolean isFlammable(final LivingEntity entity) {
        return entity.hasEffect(JStatusRegistry.FLAMMABLE.get());
    }
}