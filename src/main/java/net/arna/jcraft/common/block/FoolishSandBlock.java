package net.arna.jcraft.common.block;

import net.arna.jcraft.JCraft;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;

public class FoolishSandBlock extends FallingBlock {
    public static final int MAX_AGE = 250;
    public static final IntProperty AGE;

    public FoolishSandBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(AGE, 0));
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (this.increaseAge(state, world, pos)) {
            super.scheduledTick(state, world, pos, random);
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            Direction[] var6 = Direction.values();
            for (Direction direction : var6) {
                mutable.set(pos, direction);
                BlockState blockState = world.getBlockState(mutable);
                if (blockState.isOf(this) && !this.increaseAge(blockState, world, mutable))
                    world.createAndScheduleBlockTick(mutable, this, 20);
            }
        } else {
            world.createAndScheduleBlockTick(pos, this, 20, TickPriority.NORMAL);
        }
    }

    private boolean increaseAge(BlockState state, World world, BlockPos pos) {
        int i = state.get(AGE);
        if (i < MAX_AGE) {
            world.setBlockState(pos, state.with(AGE, i + 1), 2);
            return false;
        }

        world.removeBlock(pos, false);
        return true;
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    static {
        AGE = IntProperty.of("age", 0, MAX_AGE);
    }
}
