package net.arna.jcraft.client.rendering.shader.api;

/// Compiled graphics API agnostic shader program. Made using an implementation of {@link JShaderProvider} (i.e. {@link net.arna.jcraft.client.rendering.shader.impl.GLShaderProvider}) and two or more {@link UnbakedShader}s.
public abstract class BakedProgram {
    public abstract void bind();
    public abstract void unbind();

    public abstract void renderFullscreen();

    public abstract int handle();
}