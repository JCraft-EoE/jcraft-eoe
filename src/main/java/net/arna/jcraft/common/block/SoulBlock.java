package net.arna.jcraft.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SoulBlock extends Block {
    public SoulBlock(Settings settings) { super(settings); }
    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.isGlowing() && entity instanceof LivingEntity livingEntity)
            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 80, 0));
        super.onSteppedOn(world, pos, state, entity);
    }
}