package net.arna.jcraft.client.rendering.handler;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.api.PostEffect;
import net.arna.jcraft.client.rendering.api.callbacks.PostShaderRenderCallback;

public class SpecialParticleShaderHandler implements PostShaderRenderCallback {
    public static final SpecialParticleShaderHandler INSTANCE = new SpecialParticleShaderHandler();
    private static final PostEffect INVERSION_SHADER = new PostEffect(JCraft.id("shaders/post/inversion.json"),
            effect -> toInvertBuffer = effect.getRenderTarget("to_invert"));
    private static final PostEffect OVERLAP_SHADER = new PostEffect(JCraft.id("shaders/post/overlap.json"),
            effect -> overlapBuffer = effect.getRenderTarget("overlap"));
    @Getter
    private static RenderTarget toInvertBuffer, overlapBuffer;

    private SpecialParticleShaderHandler() {}

    @Override
    public void renderEffect(final float tickDelta) {
        INVERSION_SHADER.render(tickDelta);
        toInvertBuffer.clear(true); // Clear for the next round.
        OVERLAP_SHADER.render(tickDelta);
        overlapBuffer.clear(true); // Clear for the next round.
    }

    public void init() {
        PostShaderRenderCallback.EVENT.register(this);
    }
}
