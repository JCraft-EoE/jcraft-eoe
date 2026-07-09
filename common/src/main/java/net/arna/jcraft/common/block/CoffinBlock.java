package net.arna.jcraft.common.block;

import com.mojang.datafixers.util.Either;
import lombok.NonNull;
import net.arna.jcraft.api.registry.JBlockEntityTypeRegistry;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.common.block.tile.CoffinTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @see net.arna.jcraft.common.events.JServerEvents#allowSleep(Player, BlockPos)
 * @see net.arna.jcraft.common.events.JServerEvents#stopSleeping(Entity, BlockPos)
 * @see net.arna.jcraft.common.events.JServerEvents#modifySleepingDirection(Entity, BlockPos, Direction)
 */
public final class CoffinBlock extends BedBlock {

    private static final VoxelShape NORTH_SHAPE, SOUTH_SHAPE, WEST_SHAPE, EAST_SHAPE;
    public CoffinBlock(final Properties settings) {
        super(DyeColor.RED, settings);
    }

    private static List<ItemStack> DROP_LIST;
    public static void init() {
        DROP_LIST = List.of(new ItemStack(JItemRegistry.COFFIN_BLOCK.get()));
    }

    @NonNull
    @Override
    public InteractionResult use(@NonNull BlockState state, final Level world, @NonNull BlockPos pos,
                                 @NonNull final Player player, @NonNull final InteractionHand hand, @NonNull final BlockHitResult hit) {
        if (world.isClientSide) {
            return InteractionResult.CONSUME;
        }
        if (state.getValue(PART) != BedPart.HEAD) {
            pos = pos.relative(state.getValue(FACING));
            state = world.getBlockState(pos);
            if (!state.is(this)) {
                return InteractionResult.CONSUME;
            }
        }

        final Direction facing = state.getValue(FACING);

        if (!canSetSpawn(world)) {
            world.removeBlock(pos, false);
            BlockPos blockPos = pos.relative(facing.getOpposite());
            if (world.getBlockState(blockPos).is(this)) {
                world.removeBlock(blockPos, false);
            }

            Vec3 vec3d = pos.getCenter();
            world.explode(null, world.damageSources().badRespawnPointExplosion(vec3d), null,
                    (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, 5.0F, true, Level.ExplosionInteraction.BLOCK);
        } else if (state.getValue(OCCUPIED)) {
            if (!kickVillagerOutOfBed(world, pos)) {
                player.displayClientMessage(Component.translatable("block.minecraft.bed.occupied"), true);
            }
        } else {
            Either<Player.BedSleepingProblem, Unit> sleep = player.startSleepInBed(pos);
            sleep.ifRight(unit -> {
                Vec3 bedPos = player.position().add(0, -0.2, 0)
                        // .add( Vec3.atLowerCornerOf(facing.getNormal()).scale(1.1) )
                        ;
                player.teleportTo(bedPos.x, bedPos.y, bedPos.z);
            });

            sleep.ifLeft(problem -> {
                if (problem.getMessage() != null) {
                    player.displayClientMessage(problem.getMessage(), true);
                }
            });
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState getStateForPlacement(@NonNull final BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null) state.setValue(OCCUPIED, false);
        return state;
    }

    /*
     * Creates the block entity that we have playing our animations and rendering
     * the block
     */
    @Override
    public @NonNull BlockEntity newBlockEntity(@NonNull final BlockPos pos, @NonNull final BlockState state) {
        return JBlockEntityTypeRegistry.COFFIN_TILE.get().create(pos, state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NonNull List<ItemStack> getDrops(@NonNull final BlockState state, @NonNull final LootParams.Builder params) {
        return state.getValue(PART) == BedPart.FOOT ? DROP_LIST : Collections.emptyList();
    }

    @Override
    public @NonNull VoxelShape getShape(@NonNull final BlockState state, @NonNull final BlockGetter world,
                                        @NonNull final BlockPos pos, @NonNull final CollisionContext context) {
        return BedPart.FOOT == state.getValue(BedBlock.PART) ?
            switch (state.getValue(FACING)) {
                case NORTH -> NORTH_SHAPE;
                case SOUTH -> SOUTH_SHAPE;
                case WEST -> WEST_SHAPE;
                case EAST -> EAST_SHAPE;
                default -> Shapes.block(); // shouldn't happen
            } :
            switch (state.getValue(FACING)) {
                case NORTH -> SOUTH_SHAPE;
                case SOUTH -> NORTH_SHAPE;
                case WEST -> EAST_SHAPE;
                case EAST -> WEST_SHAPE;
                default -> Shapes.block(); // shouldn't happen
            };
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(final @NonNull Level level, final @NonNull BlockState state, final @NonNull BlockEntityType<T> type) {
        return level.isClientSide() && type == JBlockEntityTypeRegistry.COFFIN_TILE.get() ? (l, p, s, e) ->  CoffinTileEntity.tickClient(l, p, s, (CoffinTileEntity) e) : null;
    }

    static {
        SOUTH_SHAPE = Shapes.or(
                // base
                Block.box(-1d, 0d, 0d, 17d, 1d, 16d),
                // left planks
                Block.box(-1d, 0d, 0d, 0d, 5d, 16d), // outer
                Block.box(0d, 0d, 1d, 1d, 6d, 16d), // middle
                Block.box(1d, 0d, 2d, 2d, 5d, 16d), // inner
                // right planks
                Block.box(16d, 0d, 0d, 17d, 5d, 16d), // outer
                Block.box(15d, 0d, 1d, 16d, 6d, 16d), // middle
                Block.box(14d, 0d, 2d, 15d, 5d, 16d), // inner
                // front planks
                Block.box(0d, 1d, 0d, 16d, 6d, 1d), // outer
                Block.box(1d, 1d, 1d, 15d, 5d, 2d) // inner
        );
        EAST_SHAPE = Shapes.or(
                // base
                Block.box(0d, 0d, -1d, 16d, 1d, 17d),
                // front planks
                Block.box(0d, 0d, -1d, 16d, 5d, 0d), // outer
                Block.box(1d, 0d, 0d, 16d, 6d, 1d), // middle
                Block.box(2d, 0d, 1d, 16d, 5d, 2d), // inner
                // behind planks
                Block.box(0d, 0d, 16d, 16d, 5d, 17d), // outer
                Block.box(1d, 0d, 15d, 16d, 6d, 16d), // middle
                Block.box(2d, 0d, 14d, 16d, 5d, 15d), // inner
                // left planks
                Block.box(0d, 1d, 0d, 1d, 6d, 16d), // outer
                Block.box(1d, 1d, 1d, 2d, 5d, 15d) // inner
        );
        NORTH_SHAPE = Shapes.or(
                // base
                Block.box(-1d, 0d, 0d, 17d, 1d, 16d),
                // left planks
                Block.box(-1d, 0d, 0d, 0d, 5d, 16d), // outer
                Block.box(0d, 0d, 0d, 1d, 6d, 15d), // middle
                Block.box(1d, 0d, 0d, 2d, 5d, 14d), // inner
                // right planks
                Block.box(16d, 0d, 0d, 17d, 5d, 16d), // outer
                Block.box(15d, 0d, 0d, 16d, 6d, 15d), // middle
                Block.box(14d, 0d, 0d, 15d, 5d, 14d), // inner
                // behind planks
                Block.box(0d, 1d, 15d, 16d, 6d, 16d), // outer
                Block.box(1d, 1d, 14d, 15d, 5d, 15d) // inner
        );
        WEST_SHAPE = Shapes.or(
                // base
                Block.box(0d, 0d, -1d, 16d, 1d, 17d),
                // front planks
                Block.box(0d, 0d, -1d, 16d, 5d, 0d), // outer
                Block.box(0d, 0d, 0d, 15d, 6d, 1d), // middle
                Block.box(0d, 0d, 1d, 14d, 5d, 2d), // inner
                // behind planks
                Block.box(0d, 0d, 16d, 16d, 5d, 17d), // outer
                Block.box(0d, 0d, 15d, 14d, 6d, 16d), // middle
                Block.box(0d, 0d, 14d, 14d, 5d, 15d), // inner
                // right planks
                Block.box(14d, 1d, 0d, 15d, 6d, 16d), // outer
                Block.box(15d, 1d, 1d, 16d, 5d, 15d) // inner
        );
    }

}
