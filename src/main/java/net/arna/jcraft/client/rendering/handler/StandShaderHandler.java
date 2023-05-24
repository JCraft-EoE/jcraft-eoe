package net.arna.jcraft.client.rendering.handler;

import ladysnake.satin.api.event.PostWorldRenderCallbackV2;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.math.Matrix4f;

public abstract class StandShaderHandler implements PostWorldRenderCallbackV2, ClientTickEvents.EndTick {
    public int ticks, tickDelay = 0;
    public boolean shouldRender, renderingEffect = false;

    public final Matrix4f projectionMatrix = new Matrix4f();

}
