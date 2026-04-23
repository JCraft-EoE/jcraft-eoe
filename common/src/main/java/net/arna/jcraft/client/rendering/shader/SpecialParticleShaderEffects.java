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

public class SpecialParticleShaderEffects {
    // TODO: these two shaders should probably be merged into one class

    public static class InversionShaderEffect extends ShaderEffect implements DisplayResizeCallback, PostShaderRenderCallback {
        private ShaderSampler colorSampler, invertSampler;
        private RenderTarget invertTarget;

        public InversionShaderEffect()
        {
            super(
                    new ShaderEffect.LinkData(
                            JCraft.id("shaders/program/blit.vsh"),
                            JCraft.id("shaders/program/invert.fsh")
                    )
            );

            linkData.addSampler("DiffuseSampler");
            linkData.addSampler("InvertSampler");

            linkData.freeze();

            DisplayResizeCallback.EVENT.register(this);
            PostShaderRenderCallback.EVENT.register(this);
        }

        @Override
        public void link(BakedProgram program) {
            super.link(program);

            this.colorSampler = getSampler("DiffuseSampler");
            this.invertSampler = getSampler("InvertSampler");
        }

        @Override
        public void update(float tickProgress) { }

        public void prepare()
        {
            this.invertTarget.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());
            this.invertTarget.bindWrite(true);
        }

        @Override
        public void onResolutionChanged(int width, int height) {
            if (this.invertTarget != null)
            {
                this.invertTarget.resize(width, height, Minecraft.ON_OSX);
                return;
            }
            this.invertTarget = new TextureTarget(width, height, false, Minecraft.ON_OSX);
        }

        @Override
        public void renderEffect(float tickDelta) {
            this.program.pass(()->{
                this.colorSampler.bindTexture(GLShaderTexture.fromGlHandle(Minecraft.getInstance().getMainRenderTarget().getColorTextureId()));
                this.invertSampler.bindTexture(GLShaderTexture.fromGlHandle(this.invertTarget.getColorTextureId()));

                this.program.renderFullscreen();
            });

            this.invertTarget.clear(Minecraft.ON_OSX);
        }
    }

    public static class OverlapShaderEffect extends ShaderEffect implements DisplayResizeCallback, PostShaderRenderCallback {
        private ShaderSampler colorSampler, overlapSampler;
        private RenderTarget overlapTarget;

        public OverlapShaderEffect()
        {
            super(
                    new ShaderEffect.LinkData(
                            JCraft.id("shaders/program/blit.vsh"),
                            JCraft.id("shaders/program/overlap.fsh")
                    )
            );

            linkData.addSampler("DiffuseSampler");
            linkData.addSampler("OverlapSampler");

            linkData.freeze();

            DisplayResizeCallback.EVENT.register(this);
            PostShaderRenderCallback.EVENT.register(this);
        }

        @Override
        public void link(BakedProgram program) {
            super.link(program);

            this.colorSampler = getSampler("DiffuseSampler");
            this.overlapSampler = getSampler("OverlapSampler");
        }

        @Override
        public void update(float tickProgress) { }

        public void prepare()
        {
            this.overlapTarget.bindWrite(true);
        }

        @Override
        public void onResolutionChanged(int width, int height) {
            if (this.overlapTarget != null)
            {
                this.overlapTarget.resize(width, height, Minecraft.ON_OSX);
                return;
            }
            this.overlapTarget = new TextureTarget(width, height, false, Minecraft.ON_OSX);
        }

        @Override
        public void renderEffect(float tickDelta) {
            this.program.pass(()->{
                this.colorSampler.bindTexture(GLShaderTexture.fromGlHandle(Minecraft.getInstance().getMainRenderTarget().getColorTextureId()));
                this.overlapSampler.bindTexture(GLShaderTexture.fromGlHandle(this.overlapTarget.getColorTextureId()));

                this.program.renderFullscreen();
            });

            this.overlapTarget.clear(Minecraft.ON_OSX);
        }
    }
}
