package net.arna.jcraft.client.rendering.handler;

import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.managed.ManagedFramebuffer;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import lombok.Getter;
import net.arna.jcraft.JCraft;

public class InversionShaderHandler implements ShaderEffectRenderCallback {
    public static final InversionShaderHandler INSTANCE = new InversionShaderHandler();
    private static final ManagedShaderEffect SHADER = ShaderEffectManager.getInstance().manage(JCraft.id("shaders/post/inversion.json"), InversionShaderHandler::setup);
    @Getter
    private static ManagedFramebuffer toInvertBuffer;

    private InversionShaderHandler() {}

    private static void setup(ManagedShaderEffect managedShaderEffect) {
        toInvertBuffer = SHADER.getTarget("to_invert");
    }

    @Override
    public void renderShaderEffects(float tickDelta) {
        SHADER.render(tickDelta);
        toInvertBuffer.clear(); // Clear for the next round.
    }

    public void init() {
        ShaderEffectRenderCallback.EVENT.register(this);
    }
}
