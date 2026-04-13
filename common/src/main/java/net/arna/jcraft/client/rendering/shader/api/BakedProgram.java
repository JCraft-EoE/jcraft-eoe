package net.arna.jcraft.client.rendering.shader.api;

import org.jetbrains.annotations.Nullable;

/// Compiled graphics API agnostic shader program. Made using an implementation of {@link JShaderProvider} (i.e. {@link net.arna.jcraft.client.rendering.shader.impl.GLShaderProvider}) and two or more {@link UnbakedShader}s.
public abstract class BakedProgram {
    public abstract void bind();
    public abstract void unbind();

    public abstract void renderFullscreen();

    public abstract int handle();

    public abstract int getUniformLocation(String name);
    public abstract <T> @Nullable T getUniform(Class<T> type, String name);
    public abstract void setUniform(String name, Object type);
}