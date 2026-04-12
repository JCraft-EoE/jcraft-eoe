package net.arna.jcraft.client.rendering.shader.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.arna.jcraft.client.rendering.shader.api.BakedProgram;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20C;

import java.util.Map;
import java.util.function.BiFunction;

import static org.lwjgl.opengl.GL33C.*;

public class GLBakedProgram extends BakedProgram {
    private final int handle;

    public GLBakedProgram(int handle)
    {
        this.handle = handle;
    }

    @Override
    public void bind() {
        glUseProgram(handle);
    }

    @Override
    public void unbind() {
        glUseProgram(0);
    }

    @Override
    public void renderFullscreen() {
        bind();

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
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        buffer.draw();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();

        VertexBuffer.unbind();
        buffer.close();

        unbind();
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

    @Override
    public @Nullable <T> T getUniform(Class<T> type, String name) {
        int uniformLoc = getUniformLocation(name);
        if (uniformLoc == -1) return null;

        BiFunction<Integer, Integer, ?> decoder = UNIFORM_DECODERS.get(type);
        if (decoder == null) return null;

        //noinspection unchecked
        return (T)decoder.apply(handle, uniformLoc);
    }
}