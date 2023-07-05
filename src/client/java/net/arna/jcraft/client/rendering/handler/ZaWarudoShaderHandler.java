package net.arna.jcraft.client.rendering.handler;

import ladysnake.satin.api.event.PostWorldRenderCallbackV2;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.experimental.ReadableDepthFramebuffer;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.util.GlMatrices;
import net.arna.jcraft.JCraft;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ZaWarudoShaderHandler extends StandShaderHandler {
    public static ZaWarudoShaderHandler INSTANCE = new ZaWarudoShaderHandler();
    public final Identifier SHADER_ID = JCraft.id("shaders/post/za_warudo.json");

    public float prevRadius, radius = 0f;
    public long effectLength = 0;

    public @Nullable LivingEntity shaderSourceEntity = null;

    private final ManagedShaderEffect SHADER = ShaderEffectManager.getInstance().manage(SHADER_ID, this::setup);

    private void setup(ManagedShaderEffect managedShaderEffect) {
        MinecraftClient mc = MinecraftClient.getInstance();
        SHADER.setSamplerUniform("DepthSampler", ((ReadableDepthFramebuffer) mc.getFramebuffer()).getStillDepthMap());
        SHADER.setUniformValue("ViewPort", 0, 0, mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());
    }

    @Override
    public void onWorldRendered(@NotNull MatrixStack matrices, @NotNull Camera camera, float tickDelta, long nanoTime) {
        if (renderingEffect) {
            SHADER.setUniformValue("InverseTransformMatrix", GlMatrices.getInverseTransformMatrix(projectionMatrix));
            Vec3d cameraPos = camera.getPos();
            SHADER.setUniformValue("CameraPosition", (float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);
            if (shaderSourceEntity != null) {
                SHADER.setUniformValue(
                        "Center",
                        lerp(shaderSourceEntity.getX(), shaderSourceEntity.prevX, tickDelta),
                        lerp(shaderSourceEntity.getY() + shaderSourceEntity.getHeight() / 2, shaderSourceEntity.prevY + shaderSourceEntity.getHeight() / 2, tickDelta),
                        lerp(shaderSourceEntity.getZ(), shaderSourceEntity.prevZ, tickDelta)
                );
            }
            SHADER.setUniformValue("Radius", Math.max(0f, lerp(radius, prevRadius, tickDelta)));
            SHADER.render(tickDelta);
        }
    }

    @Override
    public void onEndTick(MinecraftClient client) {
       if (shouldRender) {
            if (!renderingEffect) {
                SHADER.setUniformValue("OuterSat", 1f);
                ticks = 0;
                radius = 0f;
                renderingEffect = true;
            }
            ticks++;
            prevRadius = radius;
            float expansionRate = 4f;
            int inversion = 100 / (int) expansionRate;
            if (ticks < inversion) {
                radius += expansionRate;
            } else if (ticks == inversion) {
                SHADER.setUniformValue("OuterSat", 0.3f);
            } else if (ticks < 2 * inversion) {
                radius -= 2 * expansionRate;
            }
            if (hasFinishedAnimation()) {
                renderingEffect = false;
                shouldRender = false;
            }
        } else {
            renderingEffect = false;
        }
    }

    private float lerp(double n, double prevN, float tickDelta) {
        return (float) MathHelper.lerp(tickDelta, prevN, n);
    }

    private boolean hasFinishedAnimation() {
        return ticks > effectLength;
    }

    public void init() {
        PostWorldRenderCallbackV2.EVENT.register(this);
        ClientTickEvents.END_CLIENT_TICK.register(this);
        ShaderEffectRenderCallback.EVENT.register(this);
    }

    @Override
    public void renderShaderEffects(float tickDelta) {
        if (this.renderingEffect) {
            SHADER.render(tickDelta);
        }
    }
}
