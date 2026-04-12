package net.arna.jcraft.client.rendering.shader.texture.api;

import net.arna.jcraft.client.rendering.shader.api.BakedProgram;

public abstract class ShaderSampler<P extends BakedProgram, T extends ShaderTexture> {
    public ShaderSampler(P program, String name)
    { /* stub */ }

    public abstract void bindTexture(T texture);
}