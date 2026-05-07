package net.arna.jcraft.client.rendering.shader;

import com.mojang.blaze3d.vertex.PoseStack;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.gui.hud.EpitaphOverlay;
import net.arna.jcraft.client.rendering.api.callbacks.PostWorldRenderCallback;
import net.arna.jcraft.client.rendering.shader.api.BakedProgram;
import net.arna.jcraft.client.rendering.shader.api.ShaderEffect;
import net.arna.jcraft.client.rendering.shader.texture.api.ShaderSampler;
import net.arna.jcraft.client.rendering.shader.texture.impl.GLShaderTexture;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;

public class EpitaphVignetteShaderEffect extends ShaderEffect implements PostWorldRenderCallback {
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

        PostWorldRenderCallback.EVENT.register(this);
    }

    @Override
    public void link(BakedProgram program) {
        super.link(program);

        this.colorSampler = getSampler("DiffuseSampler");
    }


    @Override
    public void update(float tickProgress) {

    }

    private void renderVignette(float intensity, float extend)
    {
        this.program.pass(()->{
            this.program.setUniform("Intensity", intensity);
            this.program.setUniform("Extend", extend);

            GLShaderTexture colorTexture = GLShaderTexture.fromGlHandle(Minecraft.getInstance().getMainRenderTarget().getColorTextureId());
            this.colorSampler.bindTexture(colorTexture);

            this.program.renderFullscreen();
        });
    }

    @Override
    public void onWorldRendered(PoseStack matrices, Camera camera, float tickDelta, long nanoTime) {
        if (EpitaphOverlay.shouldRenderVignette() && JShaderRegistry.EPITAPH_VIGNETTE != null)
        {
            renderVignette(
                    EpitaphOverlay.getVignetteIntensity(),
                    EpitaphOverlay.getVignetteExtend()
            );
        }
    }
}