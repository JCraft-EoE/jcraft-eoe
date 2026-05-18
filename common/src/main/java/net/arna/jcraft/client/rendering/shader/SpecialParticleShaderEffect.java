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
import org.lwjgl.opengl.GL33C;

public class SpecialParticleShaderEffect extends ShaderEffect implements DisplayResizeCallback, PostShaderRenderCallback {
    private ShaderSampler colorSampler, effectSampler;
    private RenderTarget effectTarget;
    private final boolean usesDepth;

    public SpecialParticleShaderEffect(ResourceLocation fragmentPath, boolean usesDepth)
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

        this.usesDepth = false;

        DisplayResizeCallback.EVENT.register(this);
        PostShaderRenderCallback.EVENT.register(this);
    }

    @Override
    public void link(BakedProgram program) {
        super.link(program);

        this.colorSampler = getSampler("DiffuseSampler");
        this.effectSampler = getSampler("EffectSampler");

        Minecraft minecraft = Minecraft.getInstance();

        this.effectTarget = new TextureTarget(minecraft.getMainRenderTarget().width, minecraft.getMainRenderTarget().height, usesDepth, Minecraft.ON_OSX);
    }

    @Override
    public void update(float tickProgress) { }

    int oldFramebuffer = 0;

    public void prepare()
    {
        oldFramebuffer = GL33C.glGetInteger(GL33C.GL_DRAW_FRAMEBUFFER_BINDING);
        if (this.effectTarget == null || this.effectTarget.getColorTextureId() == 0) return;

//        if (usesDepth) this.effectTarget.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());
        this.effectTarget.bindWrite(true);
    }

    @Override
    public void onResolutionChanged(int width, int height) {
        if (this.effectTarget != null)
        {
            this.effectTarget.resize(width, height, Minecraft.ON_OSX);
            return;
        }
        this.effectTarget = new TextureTarget(width, height, usesDepth, Minecraft.ON_OSX);
    }

    @Override
    public void renderEffect(float tickDelta) {
        if (this.effectTarget == null || this.effectTarget.getColorTextureId() == 0) return;

        this.program.pass(()->{
            this.colorSampler.bindTexture(GLShaderTexture.fromGlHandle(Minecraft.getInstance().getMainRenderTarget().getColorTextureId()));
            this.effectSampler.bindTexture(GLShaderTexture.fromGlHandle(this.effectTarget.getColorTextureId()));

            this.program.renderFullscreen();
        });

        this.effectTarget.clear(Minecraft.ON_OSX);
        this.effectTarget.unbindWrite();

        GL33C.glBindFramebuffer(GL33C.GL_DRAW_FRAMEBUFFER, oldFramebuffer);
    }
}
