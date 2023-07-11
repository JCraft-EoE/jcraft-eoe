package net.arna.jcraft.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.HUDAnimation;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.Window;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EpitaphOverlay extends DrawableHelper {
    public static final long FRAME_TIME = 1000000000 / 60; // Time of one frame in nanoseconds.
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
        countdown = -1;
    }

    public static void render() {
        if (state == State.NONE || state.getAnimation() == null) return;

        Window window = MinecraftClient.getInstance().getWindow();
        HUDAnimation.Frame frameData = state.getFrame(frame);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, state.getAnimation().getAtlas());

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        bufferBuilder
                .vertex(0.0, window.getScaledHeight(), -90.0)
                .texture(frameData.getUvMin().x, frameData.getUvMax().y)
                .next();
        bufferBuilder
                .vertex(window.getScaledWidth(), window.getScaledHeight(), -90.0)
                .texture(frameData.getUvMax().x, frameData.getUvMax().y)
                .next();
        bufferBuilder
                .vertex(window.getScaledWidth(), 0.0, -90.0)
                .texture(frameData.getUvMax().x, frameData.getUvMin().y)
                .next();
        bufferBuilder
                .vertex(0.0, 0.0, -90.0)
                .texture(frameData.getUvMin().x, frameData.getUvMin().y)
                .next();

        tessellator.draw();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();

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
        OPENING("opening"),
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
