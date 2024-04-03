package net.arna.jcraft.client.rendering.shader;


import net.minecraft.client.gl.ShaderProgram;

public interface ShaderUniformHandler {
    void updateShaderData(ShaderProgram instance);
}