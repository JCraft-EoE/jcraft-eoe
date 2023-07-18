package net.arna.jcraft.client.rendering.handler;

import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.managed.ManagedFramebuffer;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.hud.JCraftAbilityHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

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
        Window window = MinecraftClient.getInstance().getWindow();

        RenderSystem.backupProjectionMatrix();
        Matrix4f matrix4f = Matrix4f.projectionMatrix(0.0f, window.getScaledWidth(), 0.0f,
                window.getScaledHeight(), 1000.0f, 3000.0f);
        RenderSystem.setProjectionMatrix(matrix4f);

        MatrixStack mvStack = RenderSystem.getModelViewStack();
        mvStack.push();
        mvStack.loadIdentity();
        mvStack.translate(0.0, 0.0, -2000.0);
        RenderSystem.applyModelViewMatrix();


        // Render HUD
        inputBuffer.clear();
        inputBuffer.beginWrite(false);
        JCraftAbilityHud.render(new MatrixStack(), false);

        overlayBuffer.clear();
        overlayBuffer.beginWrite(false);
        JCraftAbilityHud.render(new MatrixStack(), true);


        // Restore
        RenderSystem.restoreProjectionMatrix();
        mvStack.pop();

        // Do blending and masking
        SHADER.render(tickDelta);
    }

    public void init() {
        ShaderEffectRenderCallback.EVENT.register(this);
    }
}
