package net.arna.jcraft.client.rendering.handler;

import net.arna.jcraft.JCraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class CrimsonShaderHandler extends StandShaderHandler {
    public static CrimsonShaderHandler INSTANCE = new CrimsonShaderHandler();
    public final Identifier SHADER_ID = JCraft.id("shaders/post/crimson.json");

    @Override
    public void onWorldRendered(MatrixStack matrices, Camera camera, float tickDelta, long nanoTime) {

    }

    @Override
    public void onEndTick(MinecraftClient client) {

    }
}
