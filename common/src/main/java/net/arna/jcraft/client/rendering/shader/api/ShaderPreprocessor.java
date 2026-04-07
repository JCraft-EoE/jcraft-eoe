package net.arna.jcraft.client.rendering.shader.api;

import net.arna.jcraft.client.rendering.shader.except.InvalidShaderSource;
import net.minecraft.server.packs.resources.ResourceManager;

public class ShaderPreprocessor {
    private final ResourceManager resourceManager;

    public ShaderPreprocessor(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public UnbakedShader process(UnbakedShader source) throws InvalidShaderSource
    {
        return source; // TODO: some QOL preprocessor directives like #import <>
    }
}