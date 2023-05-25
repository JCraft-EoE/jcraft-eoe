package net.arna.jcraft.client.rendering.handler;

import ladysnake.satin.api.event.PostWorldRenderCallbackV2;
import ladysnake.satin.api.managed.ManagedCoreShader;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.arna.jcraft.JCraft;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class CrimsonShaderHandler extends StandShaderHandler {
    public static CrimsonShaderHandler INSTANCE = new CrimsonShaderHandler();
    public static final Identifier SHADER_ID = JCraft.id("space");

    public static final ManagedCoreShader SPACE = ShaderEffectManager.getInstance().manageCoreShader(SHADER_ID, VertexFormats.POSITION_TEXTURE);


    @Override
    public void onWorldRendered(MatrixStack matrices, Camera camera, float tickDelta, long nanoTime) {

    }

    @Override
    public void onEndTick(MinecraftClient client) {

    }

    public void init() {
        PostWorldRenderCallbackV2.EVENT.register(this);
        ClientTickEvents.END_CLIENT_TICK.register(this);
    }
}
