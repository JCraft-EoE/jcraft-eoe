package net.arna.jcraft.client.rendering.handler;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JBiomeRegistry;
import net.arna.jcraft.client.rendering.api.PostEffect;
import net.arna.jcraft.client.rendering.api.callbacks.PostShaderRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

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
        final ClientLevel level = Minecraft.getInstance().level;
        final LocalPlayer player = Minecraft.getInstance().player;
        if (level != null && player != null && level.getBiome(player.blockPosition()).is(JBiomeRegistry.DEVILS_PALM)) {
            float timeFraction = (level.getDayTime() % 24000L) / 24000.0f;
            EFFECT.getUniform("GameTime").set(timeFraction);
            EFFECT.render(tickDelta);
        }
    }

}
