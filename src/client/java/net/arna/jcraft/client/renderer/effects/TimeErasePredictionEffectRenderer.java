package net.arna.jcraft.client.renderer.effects;

import net.arna.jcraft.client.JCraftClient;
import net.arna.jcraft.common.entity.KingCrimsonEntity;
import net.arna.jcraft.common.entity.StandEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.*;

public class TimeErasePredictionEffectRenderer {
    private static int ticksLeft = 0;
    private static final Map<Entity, Vec3d> predictions = new WeakHashMap<>();
    
    public static void init() {
        WorldRenderEvents.BEFORE_ENTITIES.register(TimeErasePredictionEffectRenderer::render);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (ticksLeft < 0) return;
            ticksLeft--;
            
            synchronized (predictions) {
                updatePredictions();
            }
        });
    }
    
    public static void startEffect(int length) {
        if (length <= 0) throw new IllegalArgumentException("Length must be at least 1.");
        ticksLeft = length;

        World world = MinecraftClient.getInstance().world;
        StandEntity stand = JCraftClient.getStandEntity();
        if (world == null || stand == null) return;
        
        List<Entity> toCatch = world.getEntitiesByClass(Entity.class, stand.getBoundingBox().expand(64),
                EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e -> e != stand && e != MinecraftClient.getInstance().player));
        for (Entity entity : toCatch)
            predictions.put(entity, entity.getPos());
    }
    
    public static void stopEffect() {
        ticksLeft = -1;
    }
    
    private static void render(WorldRenderContext ctx) {
        if (ticksLeft < 0) return;
        
        EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        Vec3d camPos = ctx.camera().getPos();
        Set<Map.Entry<Entity, Vec3d>> predictionsSet;
        synchronized (predictions) {
            predictionsSet = new HashSet<>(predictions.entrySet());
        }

        for (Map.Entry<Entity, Vec3d> prediction : predictionsSet) {
            Entity entity = prediction.getKey();
            if (entity == null) return;
            
            Vec3d pos = prediction.getValue().subtract(camPos);
            BlockPos bPos = new BlockPos(prediction.getValue());

            int blockLight = entity.isOnFire() ? 15 : entity.world.getLightLevel(LightType.BLOCK, bPos);
            int skyLight = entity.world.getLightLevel(LightType.SKY, bPos);
            entityRenderDispatcher.render(entity, pos.x, pos.y, pos.z, entity.getYaw(), ctx.tickDelta(), ctx.matrixStack(), 
                    ctx.consumers(), LightmapTextureManager.pack(blockLight, skyLight));
        }
    }
    
    private static void updatePredictions() {
        Set<Map.Entry<Entity, Vec3d>> predictionsSet;
        synchronized (predictions) {
            predictionsSet = new HashSet<>(predictions.entrySet());
        }

        KingCrimsonEntity.updatePredictions(predictionsSet, ticksLeft);
    }
}
