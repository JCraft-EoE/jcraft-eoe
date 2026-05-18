package net.arna.jcraft.client.rendering.shader.texture.api;

import net.arna.jcraft.client.rendering.shader.api.BakedProgram;

public abstract class ShaderSampler {
    public ShaderSampler(BakedProgram program, String name)
    { /* stub */ }

    public abstract void bindTexture(ShaderTexture texture);
}