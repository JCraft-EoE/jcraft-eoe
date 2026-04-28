package net.arna.jcraft.client.rendering.shader.texture.impl;

import lombok.Getter;
import net.arna.jcraft.client.rendering.shader.texture.api.ShaderTexture;

public class GLShaderTexture extends ShaderTexture {
    @Getter
    private final int handle;

    private GLShaderTexture(int handle)
    {
        this.handle = handle;
    }

    public static GLShaderTexture fromGlHandle(int glHandle)
    { return new GLShaderTexture(glHandle); }
}