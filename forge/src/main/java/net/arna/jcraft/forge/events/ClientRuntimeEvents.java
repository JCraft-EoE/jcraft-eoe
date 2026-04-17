package net.arna.jcraft.forge.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.arna.jcraft.client.events.JClientEvents;
import net.arna.jcraft.client.renderer.effects.*;
import net.arna.jcraft.forge.mixin.client.LevelRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientRuntimeEvents {
    @SubscribeEvent
    public static void renderTickStart(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                TimeAccelerationEffectRenderer.render(level);
            }
        } else {
            JClientEvents.onLast(new PoseStack(), Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());
        }
    }

    @SubscribeEvent
    public static void renderLevel(RenderLevelStageEvent event) {
        RenderBuffers renderBuffers = ((LevelRendererAccessor) event.getLevelRenderer()).getRenderBuffers();
        ClientLevel level = Minecraft.getInstance().level;
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            JClientEvents.afterTranslucent(event.getPoseStack(), event.getCamera().getPosition(), event.getLevelRenderer());
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            AttackHitboxEffectRenderer.render(event.getPoseStack(), event.getCamera().getPosition(), event.getLevelRenderer(), renderBuffers.bufferSource());
            ShockwaveEffectRenderer.render(event.getPoseStack(), event.getCamera().getPosition(), level, renderBuffers.bufferSource());
            SplatterEffectRenderer.render(event.getPoseStack(), event.getCamera().getPosition(), level, event.getPartialTick(), renderBuffers.bufferSource());
            TimeErasePredictionEffectRenderer.render(event.getPoseStack(), event.getCamera().getPosition(), level, event.getPartialTick(), renderBuffers.bufferSource());
//            EpitaphOverlay.render();
        }
    }
}
