package net.arna.jcraft.client.rendering.shader.texture.impl;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.shader.except.IllegalRenderCommandException;
import net.arna.jcraft.client.rendering.shader.impl.GLBakedProgram;
import net.arna.jcraft.client.rendering.shader.texture.api.ShaderSampler;
import net.arna.jcraft.client.rendering.shader.texture.api.ShaderTexture;

import static org.lwjgl.opengl.GL33C.*;

public class GLShaderSampler extends ShaderSampler {
    private final int unit;

    public GLShaderSampler(GLBakedProgram program, String name, int unit) {
        super(program, name);

        int location = program.getUniformLocation(name);
        if (location == -1) {
            JCraft.LOGGER.warn("Sampler '{}' does not exist/is unused in shader", name);
            this.unit = -1;
        } else {
            this.unit = unit;
            JCraft.LOGGER.info("Bound '{}' (@ {}) to '{}'", name, location, unit);
            program.bind();
            glUniform1i(location, unit);
            program.unbind();
        }
    }

    @Override
    public void bindTexture(ShaderTexture texture) {
        if (this.unit == -1) return;

        if (!(texture instanceof GLShaderTexture glTexture))
            throw new IllegalRenderCommandException("Cannot bind shader texture of type '" + texture.getClass().getSimpleName() + "' to GLShaderSampler.");

        glActiveTexture(GL_TEXTURE0 + this.unit);
        glBindTexture(GL_TEXTURE_2D, glTexture.getHandle());
    }

    public boolean valid()
    {
        return this.unit != -1;
    }
}