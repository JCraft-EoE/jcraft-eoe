package net.arna.jcraft.common.attack.moves.goldexperience;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.GoldExperienceEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

public class BerryBushAttack extends AbstractSimpleAttack<BerryBushAttack, GoldExperienceEntity> {
    private static final BlockState berryBush = Blocks.SWEET_BERRY_BUSH.getDefaultState().with(SweetBerryBushBlock.AGE, 1);

    public BerryBushAttack(int cooldown, int windup, int duration, float attackDistance, float damage, float hitBoxSize,
                           float knockBack, float offset) {
        super(cooldown, windup, duration, attackDistance, damage, hitBoxSize, knockBack, offset);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(GoldExperienceEntity stand, LivingEntity user, MoveContext ctx) {
        World world = stand.world;
        BlockPos blockPos = stand.getBlockPos();
        if (world.getBlockState(blockPos).isAir() && world.getBlockState(blockPos.down()).isOpaque())
            world.setBlockState(blockPos, berryBush);

        return super.perform(stand, user, ctx);
    }

    @Override
    protected BerryBushAttack getThis() {
        return this;
    }
}
