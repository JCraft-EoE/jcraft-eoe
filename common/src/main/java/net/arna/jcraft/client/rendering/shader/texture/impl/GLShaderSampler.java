package net.arna.jcraft.client.rendering.shader.texture.impl;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.shader.except.IllegalShaderUniforms;
import net.arna.jcraft.client.rendering.shader.impl.GLBakedProgram;
import net.arna.jcraft.client.rendering.shader.texture.api.ShaderSampler;

import java.util.Objects;

import static org.lwjgl.opengl.GL33C.*;

public class GLShaderSampler extends ShaderSampler<GLBakedProgram, GLShaderTexture> {
    private final int unit;

    public GLShaderSampler(GLBakedProgram program, String name) {
        super(program, name);

        Integer i = program.getUniform(Integer.class, name);
        this.unit = Objects.requireNonNullElse(i, -1);
        if (i == null) {
            JCraft.LOGGER.warn("Sampler '{}' does not exist/is unused in shader", name);
        }
    }

    @Override
    public void bindTexture(GLShaderTexture texture) {
        if (this.unit == -1) return;
        glActiveTexture(GL_TEXTURE0 + this.unit);
        glBindTexture(GL_TEXTURE_2D, texture.getHandle());
    }
}