package net.arna.jcraft.client.rendering.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.api.PostEffect;
import net.arna.jcraft.common.spec.RangerSpec;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class RangerFocusShaderHandler extends StandShaderHandler {
    public static final RangerFocusShaderHandler INSTANCE = new RangerFocusShaderHandler();
    private static final PostEffect EFFECT = new PostEffect(JCraft.id("shaders/post/ranger_focus.json"));
    private static final float FADE_PER_TICK = 0.1f;

    private float fade = 0f;

    private RangerFocusShaderHandler() {
        if (INSTANCE != null) throw new IllegalStateException("An instance already exists.");
    }

    @Override
    public void onWorldRendered(final @NonNull PoseStack matrices, final @NonNull Camera camera, final float tickDelta, final long nanoTime) {
        EFFECT.getUniform("Fade").set(fade);
    }

    @Override
    public void renderEffect(final float tickDelta) {
        if (fade > 0f)
            EFFECT.render(tickDelta);
    }

    @Override
    public void tick(Minecraft client) {
        final boolean focusing = client.player != null &&
                JUtils.getSpec(client.player) instanceof RangerSpec &&
                JComponentPlatformUtils.getGunslinger(client.player).isFocusActive();
        fade = Mth.clamp(fade + (focusing ? FADE_PER_TICK : -FADE_PER_TICK), 0f, 1f);
    }
}
