package net.arna.jcraft.client.rendering.handler;

import ladysnake.satin.api.event.PostWorldRenderCallbackV2;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.joml.Matrix4f;

public abstract class StandShaderHandler implements PostWorldRenderCallbackV2, ClientTickEvents.EndTick, ShaderEffectRenderCallback {
    public int ticks = 0;
    public boolean shouldRender, renderingEffect = false;

    public final Matrix4f projectionMatrix = new Matrix4f();

}
