package net.arna.jcraft.client.rendering.shader.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.arna.jcraft.client.rendering.shader.api.BakedProgram;
import net.minecraft.client.Minecraft;

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
        buffer.draw();
        RenderSystem.enableDepthTest();

        VertexBuffer.unbind();
        buffer.close();

        unbind();
    }

    @Override
    public int handle() {
        return handle;
    }
}