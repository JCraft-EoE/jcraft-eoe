package net.arna.jcraft.client.rendering;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.Window;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

/**
 * Parses an animation packed using <a href="https://free-tex-packer.com/">Free Tex Packer</a>
 * into an atlas and a set of framesData.
 * <br/>
 * Settings used in Free Tex Packer:
 * <ul>
 * <li><b>Remove file ext</b>: enabled</li>
 * <li><b>Format</b>: JSON (array)</li>
 * <li><b>Allow rotation</b>: disabled</li>
 * <li><b>Allow trim</b>: disabled</li>
 * <li><b>Packer</b>: optimal packer (optional, can also be any other value)</li>
 * </ul>
 * Rest of the settings left as-is.<br/>
 * The filenames should be in the format 'frame&lt;i&gt;' where &lt;i&gt; is the index of the frame starting with 1.
 */
@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HUDAnimation {
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("frame(\\d*)");
    private final Identifier atlas;
    private final List<Frame> frames;
    @Getter(lazy = true)
    private final int frameCount = frames.size();

    public static HUDAnimation create(Identifier atlas, Identifier atlasData) {
        Optional<Resource> dataRes = MinecraftClient.getInstance().getResourceManager().getResource(atlasData);
        if (dataRes.isEmpty()) throw new IllegalArgumentException("Atlas data not found.");

        JsonObject data;
        try (BufferedReader reader = dataRes.get().getReader()) {
            data = new Gson().fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read atlas data.");
        }

        JsonArray framesData = data.getAsJsonArray("frames");
        JsonObject sizeData = data.getAsJsonObject("meta").getAsJsonObject("size");

        int atlasWidth = sizeData.get("w").getAsInt();
        int atlasHeight = sizeData.get("h").getAsInt();
        List<Frame> frames = StreamSupport.stream(framesData.spliterator(), false)
                .peek(HUDAnimation::validateFrame)
                .map(frame -> Frame.parse(frame.getAsJsonObject(), atlas, atlasWidth, atlasHeight))
                .sorted(Comparator.comparingInt(Frame::getIndex))
                .toList();

        return new HUDAnimation(atlas, frames);
    }

    private static void validateFrame(JsonElement frame) {
        if (!frame.isJsonObject()) throw new IllegalArgumentException("Frame in atlas data is not an object");

        JsonObject frameObj = frame.getAsJsonObject();
        if (!FILE_NAME_PATTERN.asMatchPredicate().test(frameObj.get("filename").getAsString()))
            throw new IllegalArgumentException("Frame in atlas data has invalid filename (must be of format 'frame<i>' where <i> is a 1-based index.");

        if (frameObj.get("rotated").getAsBoolean()) throw new IllegalArgumentException("Frames may not be rotated");
        if (frameObj.get("trimmed").getAsBoolean()) throw new IllegalArgumentException("Frames may not be trimmed");
    }

    /**
     * Preloads the texture atlas with the given texture manager and executor.
     * @param textureManager The texture manager that will load the atlas.
     * @param executor The executor used to load the atlas.
     */
    public void preload(TextureManager textureManager, Executor executor) {
        textureManager.loadTextureAsync(getAtlas(), executor);
    }

    public Frame getFrame(int i) {
        return frames.get(i);
    }

    /**
     * Creates a new frame-counter that fits this animation.
     * @param framerate The framerate of this animation. Probably 60 fps.
     * @param shouldLoop Whether the frame-counter should wrap around
     *                   when it has reached the end of the animation.
     * @return A new frame-counter for this animation.
     * @see FrameCounter
     */
    public FrameCounter createFrameCounter(float framerate, boolean shouldLoop) {
        return new FrameCounter(framerate, getFrameCount(), shouldLoop);
    }

    @Data
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Frame {
        private final Identifier atlas;
        private final int index;
        private final int width, height;
        private final int xOffset, yOffset;
        private final Vec2f uvMin, uvMax;

        private static Frame parse(JsonObject frame, Identifier atlas, int atlasWidth, int atlasHeight) {
            JsonObject frameData = frame.getAsJsonObject("frame");

            int index = Integer.parseInt(frame.get("filename").getAsString().substring(5));
            int width = frameData.get("w").getAsInt();
            int height = frameData.get("h").getAsInt();
            int xOffset = frameData.get("x").getAsInt();
            int yOffset = frameData.get("y").getAsInt();
            float uMin = (float) xOffset / atlasWidth;
            float vMin = (float) yOffset / atlasHeight;
            float uMax = (float) (xOffset + width) / atlasWidth;
            float vMax = (float) (yOffset + height) / atlasHeight;

            return new Frame(atlas, index, width, height, xOffset, yOffset, new Vec2f(uMin, vMin), new Vec2f(uMax, vMax));
        }

        public void render() {
            Window window = MinecraftClient.getInstance().getWindow();

            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShaderTexture(0, getAtlas());

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

            bufferBuilder
                    .vertex(0.0, window.getScaledHeight(), -90.0)
                    .texture(getUvMin().x, getUvMax().y)
                    .next();
            bufferBuilder
                    .vertex(window.getScaledWidth(), window.getScaledHeight(), -90.0)
                    .texture(getUvMax().x, getUvMax().y)
                    .next();
            bufferBuilder
                    .vertex(window.getScaledWidth(), 0.0, -90.0)
                    .texture(getUvMax().x, getUvMin().y)
                    .next();
            bufferBuilder
                    .vertex(0.0, 0.0, -90.0)
                    .texture(getUvMin().x, getUvMin().y)
                    .next();

            tessellator.draw();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    }
}
