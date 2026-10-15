package net.arna.jcraft.client.rendering.handler;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.api.PostEffect;
import net.arna.jcraft.client.rendering.api.callbacks.PostShaderRenderCallback;

public class DevilsPalmShaderHandler implements PostShaderRenderCallback {

    public static final DevilsPalmShaderHandler INSTANCE = new DevilsPalmShaderHandler();
    private static final PostEffect EFFECT = new PostEffect(JCraft.id("shaders/post/devils_palm.json"));

    private DevilsPalmShaderHandler() {
        // single instance
    }

    public void init() {
        PostShaderRenderCallback.EVENT.register(this);
    }

    @Override
    public void renderEffect(final float tickDelta) {
        EFFECT.render(tickDelta);
    }

}
