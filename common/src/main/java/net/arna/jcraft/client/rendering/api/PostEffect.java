package net.arna.jcraft.client.rendering.api;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.api.callbacks.DisplayResizeCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class PostEffect implements DisplayResizeCallback {
    private static final Set<PostEffect> INSTANCES = Collections.newSetFromMap(new WeakHashMap<>());
    @Getter
    private final ResourceLocation location;
    private final Consumer<PostEffect> initCallback;
    @Getter
    private PostChain postChain;

    public PostEffect(final ResourceLocation location) {
        this(location, e -> {});
    }

    public PostEffect(final ResourceLocation location, final Consumer<PostEffect> initCallback) {
        INSTANCES.add(this);
        this.location = location;
        this.initCallback = initCallback;

        DisplayResizeCallback.EVENT.register(this);
    }

    public static void initAll() {
        INSTANCES.forEach(effect -> {
            try {
                effect.initialize();
            } catch (IOException e) {
                JCraft.LOGGER.error("Failed to initialize post effect {}", effect.getLocation(), e);
            }
        });
    }

    public void initialize() throws IOException {
        release(false); // Release the old shader if it exists

        Minecraft mc = Minecraft.getInstance();
        postChain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), location);
        resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
    }

    public void resize(final int width, final int height) {
        if (!isInitialized()) return;

        postChain.resize(width, height);
        initCallback.accept(this);
    }

    public Uniform getUniform(final String name) {
        return postChain.passes.stream()
                .map(pass -> pass.getEffect().getUniform(name))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Uniform not found: " + name));
    }

    public RenderTarget getRenderTarget(final String name) {
        //noinspection OptionalOfNullableMisuse // it's not non-null???
        return Optional.ofNullable(postChain.getTempTarget(name))
                .orElseThrow(() -> new IllegalArgumentException("Render target not found: " + name));
    }

    public void setSampler(final String name, final int textureId) {
        postChain.passes.forEach(pass -> pass.getEffect().setSampler(name, () -> textureId));
    }

    public void release() {
        release(true);
    }

    private void release(boolean remove) {
        if (!this.isInitialized()) return;

        postChain.close();
        postChain = null;

        if (remove) INSTANCES.remove(this);
        DisplayResizeCallback.EVENT.unregister(this);
    }

    public void render(float tickDelta) {
        if (postChain == null) return;

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.resetTextureMatrix();
        postChain.process(tickDelta);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true); // restore render target
        RenderSystem.disableBlend();
        RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); // restore blending
        RenderSystem.enableDepthTest();
    }

    public boolean isInitialized() {
        return postChain != null;
    }

    @Override
    public void onResolutionChanged(final int newWidth, final int newHeight) {
        resize(newWidth, newHeight);
    }
}
