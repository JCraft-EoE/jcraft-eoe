package net.arna.jcraft.client.rendering.shader;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.shader.api.BakedProgram;
import net.arna.jcraft.client.rendering.shader.api.ShaderEffect;
import net.arna.jcraft.client.rendering.shader.texture.api.ShaderSampler;
import net.arna.jcraft.client.rendering.shader.texture.impl.GLShaderTexture;
import net.minecraft.client.Minecraft;

public class EpitaphVignetteShaderEffect extends ShaderEffect {
    private ShaderSampler colorSampler;

    public EpitaphVignetteShaderEffect() {
        super(
                new ShaderEffect.LinkData(
                        JCraft.id("shaders/program/blit.vsh"),
                        JCraft.id("shaders/program/epitaph_vignette.fsh")
                )
        );

        linkData.addSampler("DiffuseSampler");

        linkData.freeze();
    }

    @Override
    public void link(BakedProgram program) {
        super.link(program);

        this.colorSampler = getSampler("DiffuseSampler");
    }


    @Override
    public void update(float tickProgress) {

    }

    public void renderVignette(float intensity, float extend)
    {
        this.program.pass(()->{
            this.program.setUniform("Intensity", intensity);
            this.program.setUniform("Extend", extend);

            GLShaderTexture colorTexture = GLShaderTexture.fromGlHandle(Minecraft.getInstance().getMainRenderTarget().getColorTextureId());
            this.colorSampler.bindTexture(colorTexture);

            this.program.renderFullscreen();
        });
    }
}