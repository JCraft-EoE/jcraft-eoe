package net.arna.jcraft.client.renderer.effects;

import ladysnake.satin.api.managed.ManagedFramebuffer;
import net.arna.jcraft.client.JCraftClient;
import net.arna.jcraft.client.rendering.handler.PredictionsShaderHandler;
import net.arna.jcraft.common.entity.KingCrimsonEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;

import java.util.*;

public class TimeErasePredictionEffectRenderer {
    private static int ticksLeft = 0;
    private static final Map<Entity, Vec3d> predictions = new WeakHashMap<>();

    public static void init() {
        WorldRenderEvents.AFTER_ENTITIES.register(TimeErasePredictionEffectRenderer::render);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (ticksLeft < 0) {
                predictions.clear();
                return;
            }
            ticksLeft--;

            synchronized (predictions) {
                updatePredictions();
            }
        });
    }

    public static void startEffect(int length) {
        if (length <= 0) throw new IllegalArgumentException("Length must be at least 1.");
        ticksLeft = length;

        MinecraftClient client = MinecraftClient.getInstance();
        for (Entity entity : KingCrimsonEntity.getEntitiesToCatch(client.world, JCraftClient.getStandEntity(), client.player))
            predictions.put(entity, entity.getPos());
    }

    public static void stopEffect() {
        ticksLeft = -1;
        predictions.clear();
    }

    @SuppressWarnings("deprecation") // Minecraft does this too.
    private static void render(WorldRenderContext ctx) {
        if (ticksLeft < 0) {
            if (ticksLeft == -1) {
                PredictionsShaderHandler.getPredictionsBuffer().clear();
                ticksLeft--;
            }
            return;
        }

        // Ensure these are drawn and empty
        VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) Objects.requireNonNull(ctx.consumers());
        consumers.draw(TexturedRenderLayers.getEntitySolid());
        consumers.draw(TexturedRenderLayers.getEntityCutout());

        // Acquire the predictions
        Set<Map.Entry<Entity, Vec3d>> predictionsSet;
        synchronized (predictions) {
            predictionsSet = new HashSet<>(predictions.entrySet());
        }

        // Init frame-buffer
        ManagedFramebuffer predictionsBuffer = PredictionsShaderHandler.getPredictionsBuffer();
        predictionsBuffer.clear();
        predictionsBuffer.beginWrite(true);

        // Render entities
        EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        Vec3d camPos = ctx.camera().getPos();

        for (Map.Entry<Entity, Vec3d> prediction : predictionsSet) {
            Entity entity = prediction.getKey();
            if (entity == null || !entity.isAlive()) continue;

            Vec3d pos = prediction.getValue().subtract(camPos);
            BlockPos bPos = new BlockPos(prediction.getValue());

            int blockLight = Math.max(entity.isOnFire() ? 15 : entity.world.getLightLevel(LightType.BLOCK, bPos), 7);
            int skyLight = Math.max(entity.world.getLightLevel(LightType.SKY, bPos), 7);
            entityRenderDispatcher.render(entity, pos.x, pos.y - 0.1, pos.z, entity.getYaw(), ctx.tickDelta(), ctx.matrixStack(),
                    consumers, LightmapTextureManager.pack(blockLight, skyLight));
        }

        // Draw entities to predictions buffer
        consumers.drawCurrentLayer();
        consumers.draw(RenderLayer.getEntitySolid(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
        consumers.draw(RenderLayer.getEntityCutout(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
        consumers.draw(RenderLayer.getEntityCutoutNoCull(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
        consumers.draw(RenderLayer.getEntitySmoothCutout(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
        consumers.draw(TexturedRenderLayers.getEntitySolid());
        consumers.draw(TexturedRenderLayers.getEntityCutout());

        // Restore framebuffer
        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
    }

    private static void updatePredictions() {
        Set<Map.Entry<Entity, Vec3d>> predictionsSet;
        synchronized (predictions) {
            predictionsSet = new HashSet<>(predictions.entrySet());
        }

        KingCrimsonEntity.updatePredictions(predictionsSet, ticksLeft);
    }
}
