package net.arna.jcraft.client.rendering.shader;

import net.arna.jcraft.client.rendering.shader.api.ShaderEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class BasicShaderEffect extends ShaderEffect {
    public BasicShaderEffect(ResourceLocation vertexPath, ResourceLocation fragmentPath) {
        super(LinkData.vertexFragment(
                vertexPath,
                fragmentPath
        ));
    }

    private float time = 0.0f;

    @Override
    public void update(float tickProgress) {
        time += tickProgress;
        Minecraft minecraft = Minecraft.getInstance();

        uniformWriter.reset();

        uniformWriter.pushVec2(
                minecraft.getMainRenderTarget().width,
                minecraft.getMainRenderTarget().height
        );

        uniformWriter.pushFloat(time);

        uniformWriter.write();
    }
}