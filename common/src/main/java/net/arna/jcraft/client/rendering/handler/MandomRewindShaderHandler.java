package net.arna.jcraft.client.rendering.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.api.PostEffect;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.joml.Vector3f;

public class MandomRewindShaderHandler extends StandShaderHandler {
    public static final MandomRewindShaderHandler INSTANCE = new MandomRewindShaderHandler();
    private static final PostEffect EFFECT = new PostEffect(JCraft.id("shaders/post/mandom_rewind.json"));

    public int duration = 23;
    public Vector3f shaderColor = new Vector3f(1.0f, 0.2f, 0.6f);

    private MandomRewindShaderHandler() {
        if (INSTANCE != null) throw new IllegalStateException("An instance already exists.");
    }

    @Override
    public void onWorldRendered(final @NonNull PoseStack matrices, final @NonNull Camera camera, final float tickDelta, final long nanoTime) {
        if (renderingEffect) {
            // Include tickDelta so progress (and therefore intensity) interpolates smoothly
            // between ticks rather than stepping. Clamped so cos never goes negative.
            float progress = Math.min(1.0f, ((float) ticks + tickDelta) / (float) duration);
            float intensity = (float) Math.cos(progress * (Math.PI / 2.0));
            EFFECT.getUniform("Intensity").set(intensity);
            EFFECT.getUniform("Time").set((float) ticks * 0.05f + tickDelta * 0.05f);
            EFFECT.getUniform("Color").set(shaderColor.x(), shaderColor.y(), shaderColor.z());
        }
    }

    @Override
    public void renderEffect(final float tickDelta) {
        if (renderingEffect) {
            EFFECT.render(tickDelta);
        }
    }

    @Override
    public void tick(Minecraft client) {
        if (!shouldRender) {
            renderingEffect = false;
            return;
        }

        if (!renderingEffect) {
            ticks = 0;
            renderingEffect = true;
        }

        ticks++;

        if (ticks > duration) {
            renderingEffect = false;
            shouldRender = false;
        }
    }
}