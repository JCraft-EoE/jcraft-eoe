package net.arna.jcraft.mixin.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.mixin_logic.StillDepthHolder;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT24;

@Mixin(RenderTarget.class)
public abstract class RenderTargetMixin implements StillDepthHolder {

    @Shadow @Final public boolean useDepth;
    @Shadow public int width, height;
    private @Unique int jcraft$depthTexture; // already unique, but suppressing warning

    @Shadow public abstract void bindWrite(boolean setViewport);

    @Inject(method = "createBuffers", at = @At("RETURN"))
    private void createDepthTexture(int width, int height, boolean clearError, CallbackInfo ci) {
        if (!useDepth) return;

        jcraft$depthTexture = GL11.glGenTextures();
        RenderSystem.bindTexture(jcraft$depthTexture);
        RenderSystem.texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        RenderSystem.texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        RenderSystem.texParameter(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        GlStateManager._texImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, width, height,
                0, GL_DEPTH_COMPONENT, GL_UNSIGNED_BYTE, null);
    }

    @Inject(method = "destroyBuffers", at = @At("RETURN"))
    private void deleteDepthTexture(CallbackInfo ci) {
        if (jcraft$depthTexture == 0) return;

        TextureUtil.releaseTextureId(jcraft$depthTexture);
        jcraft$depthTexture = 0;
    }

    @Override
    public int jcraft$getDepthTexture() {
        return jcraft$depthTexture;
    }

    @Override
    public void jcraft$freezeDepth() {
        if (!useDepth) return;

        // Bind this framebuffer for writing.
        // bindRead only binds the texture which we do not want as we're binding our own.
        // bindWrite binds for both reading and writing, so it suits our needs.
        bindWrite(false);
        RenderSystem.bindTexture(jcraft$depthTexture); // bind target texture
        // Copy the depth buffer to the texture
        glCopyTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
    }

}
