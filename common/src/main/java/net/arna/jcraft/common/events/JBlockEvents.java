package net.arna.jcraft.common.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import lombok.NonNull;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface JBlockEvents<T> {

    void add(T instance);

    /**
     * Invoked directly before a block is being set, unless the block is not being changed or it happens during chunk generation.
     */
    Event<BeforeSet> BEFORE_SET = EventFactory.createEventResult();

    interface BeforeSet {
        EventResult setBlock(final @NonNull BlockPos blockPos, final @NonNull BlockState oldBlockState, final @NonNull BlockState newBlockState, final @NonNull Level level);
    }

    Event<BeforeBlockLoot> BEFORE_BLOCK_LOOT = EventFactory.createEventResult();

    interface BeforeBlockLoot {
        EventResult processBlockLoot(final @NonNull List<ItemStack> loot, final @NonNull BlockState state, final @NonNull ServerLevel level, final @NonNull BlockPos pos, final @Nullable BlockEntity blockEntity);
    }
}
