package net.arna.jcraft.client.gui.hud;

import lombok.Getter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.FrameCounter;
import net.arna.jcraft.client.rendering.HUDAnimation;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EpitaphOverlay {
    public static final long FRAME_TIME = 1000000000 / 60; // Time of one frame in nanoseconds.
    private static final float VIGNETTE_INTENSITY = 5f;
    private static final float VIGNETTE_EXTEND = 0.5f;
    private static FrameCounter frameCounter;
    private static int lastFrame;
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
        frameCounter = state.getFrameCounter();
        Objects.requireNonNull(frameCounter).start();
        countdown = 100; // Play animation for 5 seconds.
    }

    public static void stop() {
        if (state == State.NONE) return;
        shouldStop = true;
        countdown = -1;
    }

    public static void render() {
        if (!shouldRender() || frameCounter == null || state.getAnimation() == null) return;
        if (MinecraftClient.getInstance().isPaused()) frameCounter.pause();
        else frameCounter.unpause();

        int currentFrame = frameCounter.getCurrentFrame();
        if (currentFrame < 0 || shouldStop && currentFrame < lastFrame) {
            // Move to next state.
            state = state.nextState(shouldStop);
            frameCounter = state.getFrameCounter();
            if (frameCounter != null) frameCounter.start();
            currentFrame = 0;
            shouldStop = false;
        }

        if (state == State.NONE) return;
        state.getFrame(lastFrame = currentFrame).render();
    }

    public static boolean shouldRender() {
        return state != State.NONE && MinecraftClient.getInstance().options.getPerspective() == Perspective.FIRST_PERSON;
    }

    public static boolean shouldRenderVignette() {
        return shouldRender() && (state != State.INTRO || lastFrame > 10);
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
        return (lastFrame - 11) / 9f;
    }

    private static float getOutroProgress() {
        return lastFrame / 9f;
    }

    public enum State {
        NONE(null, false),
        INTRO("intro", false),
        LOOP("loop", true),
        OUTRO("outro", false);

        @Getter
        private final @Nullable HUDAnimation animation;
        @Getter
        private final @Nullable FrameCounter frameCounter;

        State(@Nullable String path, boolean looping) {
            final String prefix = "textures/gui/epitaph_overlay/";
            animation = path == null ? null : HUDAnimation.create(JCraft.id(prefix + path + "/atlas.png"),
                    JCraft.id(prefix + path + "/atlas.json"));
            frameCounter = animation == null ? null : animation.createFrameCounter(60, looping);
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
         * Acquires the next state.
         * If the end of this state has not yet been reached, this state is returned.
         * Otherwise, the next state is returned unless the current state is {@link State#LOOP LOOP}.
         * @param forceOutro Whether to move to the outro state regardless of what our current state is.
         * @return The next state
         */
        public State nextState(boolean forceOutro) {
            if (animation == null) return this;
            return forceOutro ? this == OUTRO ? NONE : OUTRO : this == LOOP ? this : values()[(ordinal() + 1) % values().length];
        }
    }
}
