package net.arna.jcraft.client.rendering.shader;

import net.arna.jcraft.client.rendering.shader.api.BakedProgram;
import net.arna.jcraft.client.rendering.shader.api.ShaderEffect;
import net.arna.jcraft.client.rendering.shader.api.uniform.UniformWriter;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2f;

public class BasicShaderEffect extends ShaderEffect {
    private UniformWriter shaderUniforms;

    public BasicShaderEffect(ResourceLocation vertexPath, ResourceLocation fragmentPath) {
        super(
                new LinkData(
                        vertexPath,
                        fragmentPath
                )
        );

        linkData.addUniformBuffer("ShaderUniforms");

        linkData.freeze();
    }

    @Override
    public void link(BakedProgram program) {
        super.link(program);

        this.shaderUniforms = getUniformWriter("ShaderUniforms");
    }

    private float time = 0.0f;

    @Override
    public void update(float tickProgress) {
        time += tickProgress;
        Minecraft minecraft = Minecraft.getInstance();

        shaderUniforms.reset();

        shaderUniforms.push(new Vector2f(minecraft.getMainRenderTarget().width, minecraft.getMainRenderTarget().height));
        shaderUniforms.push(time);

        shaderUniforms.write();
    }
}