package net.arna.jcraft.client.rendering.shader.impl;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.shader.api.JShaderProvider;
import net.arna.jcraft.client.rendering.shader.api.UnbakedShader;

import static org.lwjgl.opengl.GL33C.*;

/// The OpenGL Shader Provider.
public class GLShaderProvider implements JShaderProvider<GLBakedProgram> {
    @Override
    public GLBakedProgram compile(UnbakedShader... sources)
    {
        int handle = glCreateProgram();
        int[] compiled = new int[sources.length];

        for (int i = 0; i < sources.length; i++) {
            UnbakedShader shader = sources[i];
            int shaderHandle = glCreateShader(shader.type().getCode());
            glShaderSource(shaderHandle, shader.source());
            glCompileShader(shaderHandle);
            compiled[i] = shaderHandle;

            glAttachShader(handle, shaderHandle);
        }

        glLinkProgram(handle);
        if (glGetProgrami(handle, GL_LINK_STATUS) == GL_FALSE)
        {
            JCraft.LOGGER.error("Failed to link shader program:\n\t{}", glGetProgramInfoLog(handle));
            return null;
        }

        for (int shader : compiled)
        { glDeleteShader(shader); }

        return new GLBakedProgram(handle);
    }
}