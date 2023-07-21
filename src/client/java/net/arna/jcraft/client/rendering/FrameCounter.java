package net.arna.jcraft.client.rendering;

import lombok.Getter;
import net.minecraft.util.Util;

/**
 * Keeps track of the current frame of an animation based on the difference
 * in time between when the animation started and when a frame is requested.
 * Also supports pausing the animation.
 */
public class FrameCounter {
    /**
     * The amount of nanoseconds a single frame should last.
     */
    @Getter
    private final long singleFrameTime;
    /**
     * The amount of frames the animation this frame-counter is for has.
     */
    @Getter
    private final int frameCount;
    /**
     * Whether this frame-counter should wrap around when it reaches the end
     * of the animation. If {@code false}, returns -1 once the end is surpassed.
     */
    @Getter
    private final boolean shouldLoop;
//    @Getter
    private long start, offset;
    @Getter
    private boolean paused;

    public FrameCounter(float framerate, int frameCount, boolean shouldLoop) {
        singleFrameTime = (long) (1_000_000_000L / framerate);
        this.frameCount = frameCount;
        this.shouldLoop = shouldLoop;
    }

    /**
     * Updates the start time and resets other variables.
     */
    public void start() {
        start = Util.getMeasuringTimeNano();
        offset = 0;
        paused = false;
    }

    /**
     * Updates the offset and pauses the counting.
     */
    public void pause() {
        if (paused) return;
        offset += Util.getMeasuringTimeNano() - start;
        paused = true;
    }

    /**
     * Updates the start time and unpauses the counting.
     */
    public void unpause() {
        if (!paused) return;
        start = Util.getMeasuringTimeNano();
        paused = false;
    }

    /**
     * Gets the frame the counter is currently at.
     * If the frame is greater than the frame-count and the
     * animation does not loop, returns {@code -1} instead.
     * @return Either the current frame or {@code -1}
     */
    public int getCurrentFrame() {
        long d = (paused ? 0 :  Util.getMeasuringTimeNano() - start) + offset;
        int frame = (int) (d / singleFrameTime);

        return !shouldLoop && frame >= frameCount ? -1 : frame % frameCount;
    }
}
