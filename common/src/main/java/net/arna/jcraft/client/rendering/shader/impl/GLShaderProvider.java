package net.arna.jcraft.client.rendering.shader.impl;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.shader.api.JShaderProvider;
import net.arna.jcraft.client.rendering.shader.api.UnbakedShader;

import static org.lwjgl.opengl.GL33C.*;

/// The OpenGL Shader Provider.
public class GLShaderProvider implements JShaderProvider<GLBakedProgram> {
    @Override
    public GLBakedProgram compile(String name, UnbakedShader... sources)
    {
        int handle = glCreateProgram();
        int[] compiled = new int[sources.length];

        for (int i = 0; i < sources.length; i++) {
            UnbakedShader shader = sources[i];
            int shaderHandle = glCreateShader(shader.type().getCode());
            glShaderSource(shaderHandle, shader.source());
            glCompileShader(shaderHandle);
            if (glGetShaderi(shaderHandle, GL_COMPILE_STATUS) == GL_FALSE)
            {
                JCraft.LOGGER.error("Failed to compile {} shader '{}':\n\t{}", shader.type().name(), name, glGetShaderInfoLog(shaderHandle));
                return null;
            }
            compiled[i] = shaderHandle;

            glAttachShader(handle, shaderHandle);
        }

        glLinkProgram(handle);
        if (glGetProgrami(handle, GL_LINK_STATUS) == GL_FALSE)
        {
            JCraft.LOGGER.error("Failed to link shader program '{}':\n\t{}", name, glGetProgramInfoLog(handle));
            return null;
        }

        for (int shader : compiled)
        { glDeleteShader(shader); }

        return new GLBakedProgram(name, handle);
    }
}