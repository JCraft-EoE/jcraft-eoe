package net.arna.jcraft.common.block;

import com.mojang.datafixers.util.Either;
import net.arna.jcraft.registry.JBlockEntityTypeRegistry;
import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Unit;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CoffinBlock extends BedBlock {
    public CoffinBlock(AbstractBlock.Settings settings) {
        super(DyeColor.RED, settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.CONSUME;
        } else {
            if (!state.isOf(this))
                return ActionResult.CONSUME;

            Direction facing = state.get(FACING);

            if (!isBedWorking(world)) {
                world.removeBlock(pos, false);
                BlockPos blockPos = pos.offset(facing.getOpposite());
                if (world.getBlockState(blockPos).isOf(this))
                    world.removeBlock(blockPos, false);

                world.createExplosion(null, DamageSource.badRespawnPoint(), null, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 5.0F, true, Explosion.DestructionType.DESTROY);
            } else if (state.get(OCCUPIED)) {
                if (!wakeVillager(world, pos))
                    player.sendMessage(Text.translatable("block.minecraft.bed.occupied"), true);
            } else {
                Either<PlayerEntity.SleepFailureReason, Unit> sleep = player.trySleep(pos);
                if (sleep.right().isPresent()) {
                    Vec3d bedPos = player.getPos().add(0, -0.2, 0).add(
                            Vec3d.of(facing.getVector()).multiply(1.1)
                    );
                    player.requestTeleport(bedPos.x, bedPos.y, bedPos.z);
                }
            }

            return ActionResult.SUCCESS;
        }
    }

    private boolean wakeVillager(World world, BlockPos pos) {
        List<VillagerEntity> list = world.getEntitiesByClass(VillagerEntity.class, new Box(pos), LivingEntity::isSleeping);
        if (list.isEmpty()) return false;
        list.get(0).wakeUp();
        return true;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState()
                .with(FACING, context.getPlayerFacing())
                .with(OCCUPIED, false);
    }

    /*
     * Creates the block entity that we have playing our animations and rendering
     * the block
     */
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return JBlockEntityTypeRegistry.COFFIN_TILE.instantiate(pos, state);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case NORTH -> Block.createCuboidShape(0, 0, -16, 16, 1, 16);
            case SOUTH -> Block.createCuboidShape(0, 0, 0, 16, 1, 32);
            case WEST -> Block.createCuboidShape(-16, 0, 0, 16, 1, 16);
            default -> Block.createCuboidShape(0, 0, 0, 32, 1, 16);
        };
    }

    /*
     * Tests for air 1 block out from the facing pos to ensure it's air so the block
     * doesn't place into another block
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        for (BlockPos testPos : BlockPos.iterate(pos,
                pos.offset(state.get(FACING), 2))) {
            if (!testPos.equals(pos) && !world.getBlockState(testPos).isAir())
                return false;
        }
        return true;
    }

    // Simplified from Block#onBreak
    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        spawnBreakParticles(world, player, pos, state);
        world.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Emitter.of(player, state));
        if (!player.getAbilities().creativeMode)
            dropStack(world, pos, new ItemStack(JObjectRegistry.COFFIN_BLOCK));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
    }

    // Block#getStateForNeighborUpdate
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return state;
    }
}
