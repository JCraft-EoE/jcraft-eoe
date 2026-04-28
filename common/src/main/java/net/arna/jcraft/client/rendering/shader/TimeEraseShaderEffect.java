package net.arna.jcraft.client.rendering.shader;

import com.mojang.blaze3d.vertex.PoseStack;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.api.callbacks.PostWorldRenderCallback;
import net.arna.jcraft.client.rendering.shader.api.BakedProgram;
import net.arna.jcraft.client.rendering.shader.api.ShaderEffect;
import net.arna.jcraft.client.rendering.shader.texture.api.ShaderSampler;
import net.arna.jcraft.client.rendering.shader.texture.impl.GLShaderTexture;
import net.arna.jcraft.mixin_logic.StillDepthHolder;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.joml.Vector2f;

public class TimeEraseShaderEffect extends ShaderEffect implements PostWorldRenderCallback {
    public boolean enabled = false;
    private float time = 0.f;

    private ShaderSampler depthSampler;
    private ShaderSampler colorSampler;

    public TimeEraseShaderEffect()
    {
        super(
                new LinkData(
                        JCraft.id("shaders/program/blit.vsh"),
                        JCraft.id("shaders/program/time_erase.fsh")
                )
        );

        linkData.addSampler("DiffuseSampler");
        linkData.addSampler("DepthSampler");

        linkData.freeze();

        PostWorldRenderCallback.EVENT.register(this);
    }

    @Override
    public void link(BakedProgram program) {
        super.link(program);

        this.colorSampler = getSampler("DiffuseSampler");
        this.depthSampler = getSampler("DepthSampler");
    }

    @Override
    public void onWorldRendered(PoseStack matrices, Camera camera, float tickDelta, long nanoTime) {
        update(tickDelta);

        if (!enabled) return;
        Minecraft minecraft = Minecraft.getInstance();

        this.program.pass(()->{
            GLShaderTexture colorTexture = GLShaderTexture.fromGlHandle(minecraft.getMainRenderTarget().getColorTextureId());
            GLShaderTexture depthTexture = GLShaderTexture.fromGlHandle(((StillDepthHolder)minecraft.getMainRenderTarget()).jcraft$getDepthTexture());

            this.colorSampler.bindTexture(colorTexture);
            this.depthSampler.bindTexture(depthTexture);

            this.program.setUniform("Time", time);
            this.program.setUniform("Viewport", new Vector2f(minecraft.getMainRenderTarget().width, minecraft.getMainRenderTarget().height));
            this.program.setUniform("InverseTransformMatrix", TimestopShaderEffect.getInvTransformMat());

            this.program.renderFullscreen();
        });
    }

    @Override
    public void update(float tickProgress) { time += (tickProgress/3.f); }
}