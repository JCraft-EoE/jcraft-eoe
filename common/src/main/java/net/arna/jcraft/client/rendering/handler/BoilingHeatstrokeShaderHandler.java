package net.arna.jcraft.client.rendering.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.client.rendering.api.PostEffect;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;

public class BoilingHeatstrokeShaderHandler extends StandShaderHandler {
    public static final BoilingHeatstrokeShaderHandler INSTANCE = new BoilingHeatstrokeShaderHandler();
    private static final PostEffect EFFECT = new PostEffect(JCraft.id("shaders/post/boiling_heatstroke.json"));

    private float intensity = 0f;
    private float time = 0f;

    private BoilingHeatstrokeShaderHandler() {
        if (INSTANCE != null) throw new IllegalStateException("An instance already exists.");
    }

    @Override
    public void tick(Minecraft client) {
        if (client.player == null) return;

        if (client.player.hasEffect(JStatusRegistry.BOILING.get())) {
            intensity = 1.0f;
        } else {
            intensity -= 0.025f;
            intensity = Math.max(0f, intensity);
        }

        shouldRender = intensity > 0f;
    }

    @Override
    public void onWorldRendered(final @NonNull PoseStack matrices, final @NonNull Camera camera,
                                final float tickDelta, final long nanoTime) {
        if (!EFFECT.isInitialized()) return;
        time += 0.02f;
        EFFECT.getUniform("HeatTime").set(time);
        EFFECT.getUniform("Intensity").set(intensity);
    }

    @Override
    public void renderEffect(final float tickDelta) {
        if (shouldRender) EFFECT.render(tickDelta);
    }
}
