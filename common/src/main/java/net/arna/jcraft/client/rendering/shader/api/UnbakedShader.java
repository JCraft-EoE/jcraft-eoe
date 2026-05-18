package net.arna.jcraft.client.rendering.shader.api;

/// Represents a preprocessed shader to be linked into a {@link BakedProgram}. Source should be loaded through the {@link ShaderSourceProvider} with a {@link ShaderSourceRef} and preprocessed with {@link ShaderPreprocessor}.
public record UnbakedShader(String source, ShaderType type) { }