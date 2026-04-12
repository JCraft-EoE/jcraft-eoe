package net.arna.jcraft.client.rendering.shader;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.shader.api.BakedProgram;
import net.arna.jcraft.client.rendering.shader.api.ShaderEffect;
import net.arna.jcraft.client.rendering.shader.impl.GLBakedProgram;
import net.arna.jcraft.client.rendering.shader.texture.impl.GLShaderSampler;
import net.arna.jcraft.client.rendering.shader.texture.impl.GLShaderTexture;
import net.arna.jcraft.mixin_logic.StillDepthHolder;
import net.minecraft.client.Minecraft;

public class TimestopShaderEffect extends ShaderEffect {
    private GLShaderSampler depthSampler;

    public TimestopShaderEffect() {
        super(LinkData.vertexFragment(
                JCraft.id("shaders/program/blit.vsh"),
                JCraft.id("shaders/program/timestop.fsh")
        ));
    }

    @Override
    public void link(BakedProgram program) {
        super.link(program);

        this.depthSampler = new GLShaderSampler((GLBakedProgram) this.program, "DepthTexture");
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

        GLShaderTexture depthTexture = GLShaderTexture.fromGlHandle(((StillDepthHolder) Minecraft.getInstance().getMainRenderTarget()).jcraft$getDepthTexture());
        this.depthSampler.bindTexture(depthTexture);
    }
}