package net.arna.jcraft.client.hud;

import lombok.Getter;
import lombok.Setter;
import net.arna.jcraft.JCraft;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class EpitathOverlay extends DrawableHelper {
    public static final long FRAME_TIME = 1000000000 / 60; // Time of one frame in nanoseconds.
    @Getter @Setter
    private static boolean enabled;
    private static int frame;
    private static long lastRender;
    private static State state = State.NONE;
    private static boolean shouldStop = false;
    private static int countdown;

    static {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (countdown == 0) stop();
            if (countdown < 0) return;
            countdown--;
        });
    }

    public static void preload() {
        TextureManager texMan = MinecraftClient.getInstance().getTextureManager();
        ExecutorService executor = Executors.newCachedThreadPool();
        for (State state : State.values()) state.preload(texMan, executor);
    }

    public static void start() {
        state = State.OPENING;
        frame = 0;
        countdown = 100; // Play animation for 5 seconds.
    }

    public static void stop() {
        if (state == State.NONE) return;
        shouldStop = true;
    }

    public static void render(Consumer<Identifier> renderFunction) {
        if (state == State.NONE) return;

        renderFunction.accept(state.getFrame(frame));

        State nextState = state.nextState(frame, shouldStop);
        if (nextState == state) {
            // If the state did not change, move to the next frame.
            if (Util.getMeasuringTimeNano() - lastRender >= FRAME_TIME) frame = state.nextFrame(frame);
        } else {
            // If the state changed, reset frame.
            state = nextState;
            frame = 0;
            shouldStop = false;
        }
        lastRender = Util.getMeasuringTimeNano();
    }

    public enum State {
        NONE(null),
        OPENING(getFrames("opening", 31)),
        LOOP(getFrames("loop", 60)),
        OUTRO(getFrames("outro", 22));

        private final @Nullable List<Identifier> frames;

        State(@Nullable List<Identifier> frames) {
            this.frames = frames;
        }

        /**
         * Loads all textures onto the GPU to prevent lag when the animation is first started.
         * @param textureManager The texture manager that should be used to load the textures
         * @param executor The executor to load the textures with.
         */
        private void preload(TextureManager textureManager, Executor executor) {
            if (frames == null) return;
            frames.forEach(frame -> textureManager.loadTextureAsync(frame, executor));
        }

        /**
         * Gets the frame at the given index.
         * @param index The index of the frame to get
         * @return the frame at the given index.
         */
        public Identifier getFrame(int index) {
            if (frames == null) throw new IllegalStateException("NONE state has no frames.");
            return frames.get(index);
        }

        /**
         * Acquires the index of the next frame based on the current frame.
         * Loops around to the first frame when the last one is reached.
         * @param frame The current frame
         * @return the next frame
         */
        public int nextFrame(int frame) {
            if (frames == null) throw new IllegalStateException("NONE state has no frames.");
            return (frame + 1) % frames.size();
        }

        /**
         * Acquires the next state.
         * If the end of this state has not yet been reached, this state is returned.
         * Otherwise, the next state is returned unless the current state is {@link State#LOOP LOOP}.
         * @param frame The current frame
         * @param force Whether to move to the next state regardless of what our current state is.
         *              I.e. whether to move to outro if we've reached the end of loop.
         * @return The next state
         */
        public State nextState(int frame, boolean force) {
            if (frames == null || frame != frames.size() - 1) return this;
            return !force && this == LOOP ? this : values()[(ordinal() + 1) % values().length];
        }

        private static List<Identifier> getFrames(String path, int count) {
            return IntStream.rangeClosed(1, count)
                    .mapToObj(i -> JCraft.id("textures/gui/epitath_overlay/" + path + "/frame" + i + ".png"))
                    .toList();
        }
    }
}
