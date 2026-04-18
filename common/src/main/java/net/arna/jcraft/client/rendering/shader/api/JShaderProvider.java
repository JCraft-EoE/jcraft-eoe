package net.arna.jcraft.client.rendering.shader.api;

/// Provides the compilation interface for {@link UnbakedShader}. Implementations should implement per-API specific compilation.
public interface JShaderProvider<T extends BakedProgram> {
    T compile(String name, UnbakedShader... sources);
}