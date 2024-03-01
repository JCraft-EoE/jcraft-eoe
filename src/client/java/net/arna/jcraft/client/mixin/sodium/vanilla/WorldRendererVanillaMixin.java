package net.arna.jcraft.client.mixin.sodium.vanilla;

import net.arna.jcraft.client.rendering.handler.CrimsonShaderHandler;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public class WorldRendererVanillaMixin {
    @Redirect(method = "renderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/GlUniform;set(FFF)V"))
    private void jcraft$chunkRender(GlUniform uniform, float x, float y, float z, RenderLayer layer, MatrixStack stack){
        if (!CrimsonShaderHandler.INSTANCE.renderingEffect)
            uniform.set(x, y, z);
    }

    @Redirect(method = "renderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/GlUniform;upload()V"))
    private void jcraft$chunkRender2(GlUniform uniform, RenderLayer layer, MatrixStack stack, double d){
        if (!CrimsonShaderHandler.INSTANCE.renderingEffect)
            uniform.upload();
    }

    @Redirect(method = "renderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/VertexBuffer;drawElements()V"))
    private void jcraft$chunkRender4(VertexBuffer buffer){
        if (!CrimsonShaderHandler.INSTANCE.renderingEffect)
            buffer.drawElements();
    }
}
