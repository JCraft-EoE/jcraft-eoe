package net.arna.jcraft.client.rendering.shader;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.api.callbacks.DisplayResizeCallback;
import net.arna.jcraft.client.rendering.api.callbacks.PostShaderRenderCallback;
import net.arna.jcraft.client.rendering.shader.api.BakedProgram;
import net.arna.jcraft.client.rendering.shader.api.ShaderEffect;
import net.arna.jcraft.client.rendering.shader.texture.api.ShaderSampler;
import net.arna.jcraft.client.rendering.shader.texture.impl.GLShaderTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class SpecialParticleShaderEffect extends ShaderEffect implements DisplayResizeCallback, PostShaderRenderCallback {
    private ShaderSampler colorSampler, effectSampler;
    private RenderTarget effectTarget;

    public SpecialParticleShaderEffect(ResourceLocation fragmentPath)
    {
        super(
                new ShaderEffect.LinkData(
                        JCraft.id("shaders/program/blit.vsh"),
                        fragmentPath
                )
        );

        linkData.addSampler("DiffuseSampler");
        linkData.addSampler("EffectSampler");

        linkData.freeze();

        DisplayResizeCallback.EVENT.register(this);
        PostShaderRenderCallback.EVENT.register(this);
    }

    @Override
    public void link(BakedProgram program) {
        super.link(program);

        this.colorSampler = getSampler("DiffuseSampler");
        this.effectSampler = getSampler("EffectSampler");
    }

    @Override
    public void update(float tickProgress) { }

    public void prepare(boolean copyDepth)
    {
        if (copyDepth) this.effectTarget.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());
        this.effectTarget.bindWrite(true);
    }

    @Override
    public void onResolutionChanged(int width, int height) {
        if (this.effectTarget != null)
        {
            this.effectTarget.resize(width, height, Minecraft.ON_OSX);
            return;
        }
        this.effectTarget = new TextureTarget(width, height, false, Minecraft.ON_OSX);
    }

    @Override
    public void renderEffect(float tickDelta) {
        this.program.pass(()->{
            this.colorSampler.bindTexture(GLShaderTexture.fromGlHandle(Minecraft.getInstance().getMainRenderTarget().getColorTextureId()));
            this.effectSampler.bindTexture(GLShaderTexture.fromGlHandle(this.effectTarget.getColorTextureId()));

            this.program.renderFullscreen();
        });

        this.effectTarget.clear(Minecraft.ON_OSX);
    }
}
