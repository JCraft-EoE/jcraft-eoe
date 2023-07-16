package net.arna.jcraft.client.renderer.effects;

import lombok.experimental.UtilityClass;
import net.arna.jcraft.common.network.s2c.TimeAccelStatePacket;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.util.Util;
import net.minecraft.world.GameRules;

@UtilityClass
public class TimeAccelerationEffectRenderer {
    
    public static void init() {
        WorldRenderEvents.START.register(TimeAccelerationEffectRenderer::render);
    }
    
    private static void render(WorldRenderContext ctx) {
        if (!ctx.world().getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) return;

        double acceleration = TimeAccelStatePacket.getAcceleration(ctx.world());

        long currentTime = Util.getMeasuringTimeMs();
        if (acceleration == 0) {
            TimeAccelStatePacket.lastUpdate = currentTime;
            return;
        }

        double multiplier = (currentTime - TimeAccelStatePacket.lastUpdate) / 1000d;
        ctx.world().setTimeOfDay((long) (ctx.world().getTimeOfDay() + acceleration * multiplier));

        TimeAccelStatePacket.lastUpdate = currentTime;
    }
}
