package net.arna.jcraft.common.block;

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
import net.minecraft.world.World;
import net.minecraft.world.tick.TickPriority;

public class FoolishSandBlock extends FallingBlock {
    public static final int MAX_AGE = 250;
    public static final IntProperty AGE;

    public FoolishSandBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(AGE, 0));
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!increaseAge(state, world, pos)) {
            world.scheduleBlockTick(pos, this, 20, TickPriority.NORMAL);
            return;
        }

        super.scheduledTick(state, world, pos, random);
        BlockPos.Mutable blockPos = new BlockPos.Mutable();
        for (Direction direction : Direction.values()) {
            blockPos.set(pos, direction);
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.isOf(this) && !increaseAge(blockState, world, blockPos))
                world.scheduleBlockTick(blockPos, this, 20);
        }
    }

    @Override
    public int getColor(BlockState state, BlockView world, BlockPos pos) {
        return 16777216;
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
