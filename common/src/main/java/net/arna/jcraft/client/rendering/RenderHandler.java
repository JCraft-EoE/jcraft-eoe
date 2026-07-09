package net.arna.jcraft.client.rendering;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.platform.Platform;
import net.arna.jcraft.client.rendering.shader.ShaderUniformHandler;
import net.arna.jcraft.client.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

import java.util.HashMap;

public class RenderHandler {
    public static HashMap<RenderType, BufferBuilder> BUFFERS = new HashMap<>();
    public static boolean LARGER_BUFFER_SOURCES = Platform.isModLoaded("sodium");

    public static HashMap<RenderType, ShaderUniformHandler> UNIFORM_HANDLERS = new HashMap<>();
    public static MultiBufferSource.BufferSource DELAYED_RENDER;

    public static Matrix4f MATRIX4F;

    public static float FOG_NEAR;
    public static float FOG_FAR;
    public static FogShape FOG_SHAPE;
    public static float FOG_RED, FOG_GREEN, FOG_BLUE;

    public static void init() {
        int size = LARGER_BUFFER_SOURCES ? 262144 : 256;
        DELAYED_RENDER = MultiBufferSource.immediateWithBuffers(BUFFERS, new BufferBuilder(size));
    }

    public static void cacheFogData(final float near, final float far, final FogShape shape) {
        FOG_NEAR = near;
        FOG_FAR = far;
        FOG_SHAPE = shape;
    }

    public static void cacheFogData(final float r, final float g, final float b) {
        FOG_RED = r;
        FOG_GREEN = g;
        FOG_BLUE = b;
    }

    public static void beginBufferedRendering(final PoseStack matrixStack) {
        matrixStack.pushPose();
        LightTexture lightTexture = Minecraft.getInstance().gameRenderer.lightTexture();
        lightTexture.turnOnLightLayer();
        RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE2);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        float fogRed = RenderSystem.getShaderFogColor()[0];
        float fogGreen = RenderSystem.getShaderFogColor()[1];
        float fogBlue = RenderSystem.getShaderFogColor()[2];
        float shaderFogStart = RenderSystem.getShaderFogStart();
        float shaderFogEnd = RenderSystem.getShaderFogEnd();
        FogShape shaderFogShape = RenderSystem.getShaderFogShape();

        RenderSystem.setShaderFogStart(FOG_NEAR);
        RenderSystem.setShaderFogEnd(FOG_FAR);
        RenderSystem.setShaderFogShape(FOG_SHAPE);
        RenderSystem.setShaderFogColor(FOG_RED, FOG_GREEN, FOG_BLUE);

        FOG_RED = fogRed;
        FOG_GREEN = fogGreen;
        FOG_BLUE = fogBlue;

        FOG_NEAR = shaderFogStart;
        FOG_FAR = shaderFogEnd;
        FOG_SHAPE = shaderFogShape;
    }

    public static void renderBufferedBatches(final PoseStack matrixStack) {
        draw(DELAYED_RENDER, BUFFERS);
    }

    public static void endBufferedRendering(final PoseStack poseStack) {
        final LightTexture lightTexture = Minecraft.getInstance().gameRenderer.lightTexture();
        RenderSystem.setShaderFogStart(FOG_NEAR);
        RenderSystem.setShaderFogEnd(FOG_FAR);
        RenderSystem.setShaderFogShape(FOG_SHAPE);
        RenderSystem.setShaderFogColor(FOG_RED, FOG_GREEN, FOG_BLUE);

        poseStack.popPose();
        lightTexture.turnOffLightLayer();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(true);
    }

    public static void draw(final MultiBufferSource.BufferSource source, final HashMap<RenderType, BufferBuilder> buffers) {
        for (RenderType type : buffers.keySet()) {
            final ShaderInstance instance = RenderUtils.getShader(type);
            if (UNIFORM_HANDLERS.containsKey(type)) {
                ShaderUniformHandler handler = UNIFORM_HANDLERS.get(type);
                handler.updateShaderData(instance);
            }
            source.endBatch(type);
            if (instance instanceof IJShader jShader) {
                jShader.setUniformDefaults();
            }
        }
        source.endBatch();
    }

    public static void addRenderLayer(RenderType type) {
        int size = LARGER_BUFFER_SOURCES ? 262144 : type.bufferSize();
        HashMap<RenderType, BufferBuilder> buffers = BUFFERS;
        buffers.put(type, new BufferBuilder(size));
    }
}
