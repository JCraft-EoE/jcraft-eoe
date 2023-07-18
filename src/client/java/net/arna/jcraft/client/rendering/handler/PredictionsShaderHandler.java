package net.arna.jcraft.client.rendering.handler;

import ladysnake.satin.api.event.PostWorldRenderCallbackV2;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.managed.ManagedFramebuffer;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import lombok.Getter;
import net.arna.jcraft.JCraft;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;

public class PredictionsShaderHandler extends StandShaderHandler {
    public static final PredictionsShaderHandler INSTANCE = new PredictionsShaderHandler();
    private static final ManagedShaderEffect SHADER = ShaderEffectManager.getInstance().manage(JCraft.id("shaders/post/predictions.json"), PredictionsShaderHandler::setup);
    @Getter
    private static ManagedFramebuffer predictionsBuffer;

    private PredictionsShaderHandler() {
        if (INSTANCE != null) throw new IllegalStateException("An instance already exists.");
    }

    private static void setup(ManagedShaderEffect shader) {
        predictionsBuffer = shader.getTarget("predictions");
    }

    @Override
    public void onWorldRendered(@NotNull MatrixStack matrices, @NotNull Camera camera, float tickDelta, long nanoTime) {
    }

    @Override
    public void renderShaderEffects(float tickDelta) {
        SHADER.render(tickDelta);
    }

    @Override
    public void onEndTick(MinecraftClient client) {}

    public void init() {
        PostWorldRenderCallbackV2.EVENT.register(this);
        ClientTickEvents.END_CLIENT_TICK.register(this);
        ShaderEffectRenderCallback.EVENT.register(this);
    }
}
