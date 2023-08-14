package net.arna.jcraft.client.rendering.handler;

import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.managed.ManagedFramebuffer;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.gui.hud.JCraftAbilityHud;
import net.arna.jcraft.client.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

public class UIShaderHandler implements ShaderEffectRenderCallback {
    public static final UIShaderHandler INSTANCE = new UIShaderHandler();
    private static final ManagedShaderEffect SHADER = ShaderEffectManager.getInstance().manage(JCraft.id("shaders/post/ui.json"), UIShaderHandler::setup);
    private static ManagedFramebuffer inputBuffer, overlayBuffer;

    private UIShaderHandler() {
        if (INSTANCE != null) throw new IllegalStateException("An instance already exists.");
    }

    private static void setup(ManagedShaderEffect shader) {
        inputBuffer = shader.getTarget("input");
        overlayBuffer = shader.getTarget("overlay");
    }

    @Override
    public void renderShaderEffects(float tickDelta) {
        if (MinecraftClient.getInstance().options.hudHidden) return;

        // Do necessary initialisation to render HUD stuff at this stage.
        // HUD stuff should generally be rendered somewhere in InGameHud,
        // but we do it here, so we can use different frame-buffers.
        RenderUtils.startOverlayRender();

        // Render HUD
        inputBuffer.clear();
        inputBuffer.beginWrite(false);
        JCraftAbilityHud.render(new MatrixStack(), false);

        overlayBuffer.clear();
        overlayBuffer.beginWrite(false);
        JCraftAbilityHud.render(new MatrixStack(), true);

        // Restore
        RenderUtils.endOverlayRender();

        // Do blending and masking
        SHADER.render(tickDelta);
    }

    public void init() {
        ShaderEffectRenderCallback.EVENT.register(this);
    }
}
