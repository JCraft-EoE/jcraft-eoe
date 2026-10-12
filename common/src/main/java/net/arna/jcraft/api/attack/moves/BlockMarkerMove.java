package net.arna.jcraft.api.attack.moves;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
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

    default boolean isRecording() {
        return getRemainingRecordingTime() > 0;
    }

    int getMaxRecordingTime();

    int getRemainingRecordingTime();

    default void addRecordingTimes(final @NonNull LivingEntity living) {
        JComponentPlatformUtils.getMiscData(living).addRecordingTimes(
                new RecordingTimes(getMaxRecordingTime(), getRemainingRecordingTime(), getUuid())
        );
    }

    default void removeRecordingTimes(final @NonNull LivingEntity living, boolean shouldSync) {
        JComponentPlatformUtils.getMiscData(living).removeRecordingTimes(getUuid(), shouldSync);
    }

    UUID getUuid();

    List<Boolean> getIteration();

    @Getter
    final class RecordingTimes {

        private final int max;
        private int remaining;
        private final UUID uuid;

        public RecordingTimes(int max, int remaining, UUID uuid) {
            this.max = max;
            this.remaining = remaining;
            this.uuid = uuid;
        }

        public void decreaseRemaining() {
            remaining--;
        }

    }

}
