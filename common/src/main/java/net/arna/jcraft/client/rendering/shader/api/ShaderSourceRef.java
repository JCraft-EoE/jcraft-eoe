package net.arna.jcraft.client.rendering.shader.api;

import net.minecraft.resources.ResourceLocation;

/// A reference to a shader source, not to be confused with {@link UnbakedShader}, which is the preprocessed version of this.
public record ShaderSourceRef(ResourceLocation path, ShaderType type) { }