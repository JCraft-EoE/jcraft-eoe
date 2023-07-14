package net.arna.jcraft.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.util.ITimeStop;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class ScreenFreezeRenderer extends DrawableHelper implements ClientTickEvents.EndTick, HudRenderCallback {
    public NativeImage screenImage;
    private boolean shouldScreenGrab = true;


    public static NativeImage getScreenImage(Framebuffer framebuffer) {
        int i = framebuffer.textureWidth;
        int j = framebuffer.textureHeight;
        NativeImage nativeImage = new NativeImage(i, j, false);
        RenderSystem.bindTexture(framebuffer.getColorAttachment());
        nativeImage.loadFromTextureImage(0, true);
        nativeImage.mirrorVertically();
        return nativeImage;
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        if (true) {
            return;
        }

        var player = client.player;
        if (screenImage == null) {
            screenImage = getScreenImage(client.getFramebuffer());
        }


        if (player instanceof ITimeStop iTimeStop) {
            int i = iTimeStop.getTimeStopTicks();
            if (i > 0 && shouldScreenGrab) {
                screenImage = getScreenImage(client.getFramebuffer());
                shouldScreenGrab = false;
            }

            if (screenImage != null) {

            }
        }
    }

    @Override
    public void onHudRender(MatrixStack matrixStack, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (screenImage != null && client.player != null) {
            matrixStack.push();
            NativeImageBackedTexture backedTexture = new NativeImageBackedTexture(screenImage);
            Identifier id = JCraft.id("screen/screenfreeze/" + client.player.getName());
            MinecraftClient.getInstance().getTextureManager().registerTexture(id, backedTexture);

            RenderSystem.setShaderTexture(0, id);
            drawTexture(matrixStack, 0,0, 0 ,0 ,screenImage.getWidth(), screenImage.getHeight(), screenImage.getWidth(), screenImage.getHeight());
            matrixStack.pop();
        }
    }
}
