package net.arna.jcraft.client.rendering.shader.texture.impl;

import net.arna.jcraft.client.rendering.shader.except.IllegalShaderUniforms;
import net.arna.jcraft.client.rendering.shader.impl.GLBakedProgram;
import net.arna.jcraft.client.rendering.shader.texture.api.ShaderSampler;

import static org.lwjgl.opengl.GL33C.*;

public class GLShaderSampler extends ShaderSampler<GLBakedProgram, GLShaderTexture> {
    private final int unit;

    public GLShaderSampler(GLBakedProgram program, String name) {
        super(program, name);

        Integer i = program.getUniform(Integer.class, name);
        if (i == null) throw new IllegalShaderUniforms("Sampler '" + name + "' does not exist in shader");
        this.unit = i;
    }

    @Override
    public void bindTexture(GLShaderTexture texture) {
        glActiveTexture(GL_TEXTURE0 + this.unit);
        glBindTexture(GL_TEXTURE_2D, texture.getHandle());
    }
}