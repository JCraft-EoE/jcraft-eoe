package net.arna.jcraft.client.rendering.shader;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.shader.api.ShaderEffect;

public class TimestopShaderEffect extends ShaderEffect {
    public TimestopShaderEffect() {
        super(LinkData.vertexFragment(
                JCraft.id("shaders/program/blit.vsh"),
                JCraft.id("shaders/program/timestop.fsh")
        ));
    }

    @Override
    public void update(float tickProgress) {
        uniformWriter.reset();

        uniformWriter.write();
    }
}