package net.arna.jcraft.api.attack.moves;

import lombok.NonNull;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.UUID;

public interface BlockMarkerMove {

    boolean isResolving();

    void setResolving(final boolean resolving);

    /**
     * Maybe saves a block to be restored.
     * @param pos the position of the block
     * @param state the (old) block state of that position
     * @param level the level of the block
     * @return <code>true</code> if the block has been saved, <code>false</code> otherwise.
     */
    boolean addBlock(final @NonNull BlockPos pos, final @NonNull BlockState state, final @NonNull Level level);

    boolean removeBlock(@NonNull BlockPos pos, @NonNull Level level);

    boolean isInRange(final @NonNull BlockPos pos, final @NonNull Level level);

    boolean isRecording();

    UUID getUuid();

    List<Boolean> getIteration();
}
