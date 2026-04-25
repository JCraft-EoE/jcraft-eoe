package net.arna.jcraft.client.rendering.shader.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.shader.api.BakedProgram;
import net.arna.jcraft.client.rendering.shader.texture.api.ShaderSampler;
import net.arna.jcraft.client.rendering.shader.texture.impl.GLShaderSampler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20C;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static org.lwjgl.opengl.GL33C.*;

public class GLBakedProgram extends BakedProgram {
    private final int handle;

    public GLBakedProgram(String name, int handle)
    {
        super(name);
        this.handle = handle;
    }

    @Override
    protected void bindProgram() {
        glUseProgram(handle);
    }

    @Override
    protected void unbindProgram() {
        glUseProgram(0);
    }

    @Override
    public void renderFullscreen() {
        requireBound("rendering fullscreen");

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();

        // TODO: this buffer should be baked and be final. Creating new VBO/VAOs each frame is expensive!

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        builder.vertex(-1.f, -1.f, 0.f).uv(0.f, 0.f).endVertex();
        builder.vertex(1.f, -1.f, 0.f).uv(1.f, 0.f).endVertex();
        builder.vertex(1.f, 1.f, 0.f).uv(1.f, 1.f).endVertex();
        builder.vertex(-1.f, 1.f, 0.f).uv(0.f, 1.f).endVertex();

        BufferBuilder.RenderedBuffer rendered = builder.end();

        VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        buffer.bind();
        buffer.upload(rendered);

        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();

        buffer.draw();
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        // NOTE: this is not modified because we do not use the Minecraft shader instance
        ShaderInstance previousShader = RenderSystem.getShader();
        if (previousShader != null)
        {
            // restore the previous render state
            previousShader.apply();
        }

        VertexBuffer.unbind();
        buffer.close();
    }

    @Override
    public int handle() {
        return handle;
    }

    @Override
    public int getUniformLocation(String name) {
        return glGetUniformLocation(handle, name);
    }

    private static final Map<Class<?>, BiFunction<Integer, Integer, ?>> UNIFORM_DECODERS = Map.of(
            Float.class, GL20C::glGetUniformf,
            Integer.class, GL20C::glGetUniformi
    );

    private static final Map<Class<?>, BiConsumer<Integer, Object>> UNIFORM_ENCODERS = Map.of(
            Float.class, (location, value) -> glUniform1f(location, (float) value),
            Integer.class, (location, value) -> glUniform1i(location, (int) value),
            Matrix4f.class, (location, value) -> glUniformMatrix4fv(location, false, ((Matrix4f)value).get(BufferUtils.createFloatBuffer(16))),
            Vector2f.class, (location, value) -> {Vector2f v = (Vector2f)value; glUniform2f(location, v.x, v.y);},
            Vector3f.class, (location, value) -> {Vector3f v = (Vector3f)value; glUniform3f(location, v.x, v.y, v.z);},
            Vector4f.class, (location, value) -> {Vector4f v = (Vector4f)value; glUniform4f(location, v.x, v.y, v.z, v.w);}
    );

    @Override
    public @Nullable <T> T getUniform(Class<T> type, String name) {
        int uniformLoc = getUniformLocation(name);
        if (uniformLoc == -1) return null;

        BiFunction<Integer, Integer, ?> decoder = UNIFORM_DECODERS.get(type);
        if (decoder == null) return null;

        //noinspection unchecked
        return (T)decoder.apply(handle, uniformLoc);
    }

    private final HashMap<String, Integer> UNIFORM_LOCATION_CACHE = new HashMap<>();

    @Override
    public void setUniform(String name, Object value) {
        requireBound("setting uniforms");

        int location = UNIFORM_LOCATION_CACHE.getOrDefault(name, glGetUniformLocation(handle, name));
        if (location == -1)
            return;

        UNIFORM_LOCATION_CACHE.putIfAbsent(name, location);

        BiConsumer<Integer, Object> encoder = UNIFORM_ENCODERS.get(value.getClass());
        if (encoder == null) return;

        encoder.accept(location, value);
    }

    @Override
    public @Nullable ShaderSampler initializeSampler(String name, int index) {
        GLShaderSampler sampler = new GLShaderSampler(this, name, index);
        return sampler.valid() ? sampler : null;
    }
}