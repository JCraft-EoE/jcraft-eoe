package net.arna.jcraft.client.rendering.handler;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JBiomeRegistry;
import net.arna.jcraft.client.rendering.api.PostEffect;
import net.arna.jcraft.client.rendering.api.callbacks.PostShaderRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

public class DevilsPalmShaderHandler implements PostShaderRenderCallback {

    public static final DevilsPalmShaderHandler INSTANCE = new DevilsPalmShaderHandler();
    public static final int SAMPLE_RADIUS = 2;
    private static final PostEffect EFFECT = new PostEffect(JCraft.id("shaders/post/devils_palm.json"));

    private float currentWeight = 0.0f;

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
        if (level != null && player != null) {
            final BlockPos playerPos = player.blockPosition();
            final int totalSamples = (SAMPLE_RADIUS * 2 + 1) * (SAMPLE_RADIUS * 2 + 1);
            int matchingBlocks = 0;
            // scan the surroundings
            for (int x = -SAMPLE_RADIUS; x <= SAMPLE_RADIUS; x++) {
                for (int z = -SAMPLE_RADIUS; z <= SAMPLE_RADIUS; z++) {
                    final BlockPos samplePos = playerPos.offset(x, 0, z);
                    if (level.getBiome(samplePos).is(JBiomeRegistry.DEVILS_PALM)) {
                        matchingBlocks++;
                    }
                }
            }

            // calculate the target density fraction (0.0 to 1.0)
            final float targetWeight = (float) matchingBlocks / totalSamples;
            // Linear interpolation to smoothly glide currentWeight towards targetWeight
            final float lerpSpeed = 0.05f;
            currentWeight = currentWeight + (targetWeight - currentWeight) * lerpSpeed;

            // only run the shader render if our tint is actually visible
            if (currentWeight > 0.001f) {
                final float timeFraction = (level.getDayTime() % 24000L) / 24000.0f;
                EFFECT.getUniform("GameTime").set(timeFraction);
                EFFECT.getUniform("BiomeWeight").set(currentWeight);
                EFFECT.render(tickDelta);
            } else {
                currentWeight = 0.0f; // fully reset when completely outside
            }
        }
    }

}
