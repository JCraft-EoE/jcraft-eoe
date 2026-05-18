package net.arna.jcraft.client.rendering.shader.api;

import lombok.Getter;

import static org.lwjgl.opengl.GL33C.*;

@Getter
public enum ShaderType {
    VERTEX(GL_VERTEX_SHADER),
    FRAGMENT(GL_FRAGMENT_SHADER);

    private final int code;
    ShaderType(int code)
    {
        this.code = code;
    }
}
