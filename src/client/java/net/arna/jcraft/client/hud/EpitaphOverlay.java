package net.arna.jcraft.client.hud;

import lombok.Getter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.HUDAnimation;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EpitaphOverlay extends DrawableHelper {
    public static final long FRAME_TIME = 1000000000 / 60; // Time of one frame in nanoseconds.
    private static final float VIGNETTE_INTENSITY = 5f;
    private static final float VIGNETTE_EXTEND = 0.5f;
    private static int frame;
    private static long lastRender;
    @Getter
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
        state = State.INTRO;
        frame = 0;
        countdown = 100; // Play animation for 5 seconds.
    }

    public static void stop() {
        if (state == State.NONE) return;
        shouldStop = true;
        countdown = -1;
    }

    public static void render() {
        if (!shouldRender() || state.getAnimation() == null) return;

        state.getFrame(frame).render();
        if (Util.getMeasuringTimeNano() - lastRender < FRAME_TIME || MinecraftClient.getInstance().isPaused()) return;

        State nextState = state.nextState(frame, shouldStop);
        if (nextState == state) {
            // If the state did not change, move to the next frame.
            int dFrame = (int) ((Util.getMeasuringTimeNano() - lastRender) / FRAME_TIME);
            for (int i = 0; i < dFrame; i++) {
                int prevFrame = frame;
                frame = state.nextFrame(frame);

                // If we skipped enough frames to get to the end of the state, move to the next state
                // unless the current state is loop.
                if (frame > prevFrame || state == State.LOOP && !shouldStop) continue;
                nextState = state.nextState(frame, shouldStop);
                if (nextState != state) break; // Stop if the state changed.
            }
        }

        if (nextState != state) {
            // If the state changed, reset frame.
            state = nextState;
            frame = 0;
            shouldStop = false;
        }
        lastRender = Util.getMeasuringTimeNano();
    }

    public static boolean shouldRender() {
        return state != State.NONE && MinecraftClient.getInstance().options.getPerspective() == Perspective.FIRST_PERSON;
    }

    public static boolean shouldRenderVignette() {
        return shouldRender() && (state != State.INTRO || frame > 10);
    }

    public static float getVignetteIntensity() {
        return state == State.INTRO ? MathHelper.lerp(getIntroProgress(), 0f, VIGNETTE_INTENSITY) :
                state == State.OUTRO ? MathHelper.lerp(getOutroProgress(), VIGNETTE_INTENSITY, 150f) :
                VIGNETTE_INTENSITY;
    }

    public static float getVignetteExtend() {
        return state == State.INTRO ? MathHelper.lerp(getIntroProgress(), 0f, VIGNETTE_EXTEND) :
                state == State.OUTRO ? MathHelper.lerp(getOutroProgress(), VIGNETTE_EXTEND, 0f) :
                        VIGNETTE_EXTEND;
    }

    private static float getIntroProgress() {
        return (frame - 11) / 9f;
    }

    private static float getOutroProgress() {
        return frame / 9f;
    }

    public enum State {
        NONE(null),
        INTRO("intro"),
        LOOP("loop"),
        OUTRO("outro");

        @Getter
        private final @Nullable HUDAnimation animation;

        State(@Nullable String path) {
            final String prefix = "textures/gui/epitaph_overlay/";
            animation = path == null ? null : HUDAnimation.create(JCraft.id(prefix + path + "/atlas.png"),
                    JCraft.id(prefix + path + "/atlas.json"));
        }

        /**
         * Loads all textures onto the GPU to prevent lag when the animation is first started.
         * @param textureManager The texture manager that should be used to load the textures
         * @param executor The executor to load the textures with.
         */
        private void preload(TextureManager textureManager, Executor executor) {
            if (animation == null) return;
            animation.preload(textureManager, executor);
        }

        /**
         * Gets the frame at the given index.
         * @param index The index of the frame to get
         * @return the frame at the given index.
         */
        public HUDAnimation.Frame getFrame(int index) {
            if (animation == null) throw new IllegalStateException("NONE state has no animation.");
            return animation.getFrame(index);
        }

        /**
         * Acquires the index of the next frame based on the current frame.
         * Loops around to the first frame when the last one is reached.
         * @param frame The current frame
         * @return the next frame
         */
        public int nextFrame(int frame) {
            if (animation == null) throw new IllegalStateException("NONE state has no animation.");
            return (frame + 1) % animation.getFrameCount();
        }

        /**
         * Acquires the next state.
         * If the end of this state has not yet been reached, this state is returned.
         * Otherwise, the next state is returned unless the current state is {@link State#LOOP LOOP}.
         * @param frame The current frame
         * @param forceOutro Whether to move to the outro state regardless of what our current state is.
         * @return The next state
         */
        public State nextState(int frame, boolean forceOutro) {
            if (animation == null || frame != animation.getFrameCount() - 1) return this;
            return forceOutro ? OUTRO : this == LOOP ? this : values()[(ordinal() + 1) % values().length];
        }
    }
}
