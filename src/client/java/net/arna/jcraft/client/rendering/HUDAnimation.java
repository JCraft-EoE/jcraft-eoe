package net.arna.jcraft.client.rendering;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.TextureManager;
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
                .map(frame -> Frame.parse(frame.getAsJsonObject(), atlasWidth, atlasHeight))
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

    @Data
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Frame {
        private final int index;
        private final int width, height;
        private final int xOffset, yOffset;
        private final Vec2f uvMin, uvMax;

        private static Frame parse(JsonObject frame, int atlasWidth, int atlasHeight) {
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

            return new Frame(index, width, height, xOffset, yOffset, new Vec2f(uMin, vMin), new Vec2f(uMax, vMax));
        }
    }
}
