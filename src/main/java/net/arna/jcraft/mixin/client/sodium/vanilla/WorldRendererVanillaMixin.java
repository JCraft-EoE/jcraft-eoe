package net.arna.jcraft.mixin.client.sodium.vanilla;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.arna.jcraft.client.rendering.handler.CrimsonShaderHandler;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldRenderer.class)
public class WorldRendererVanillaMixin {
/*
    @WrapWithCondition(method = "renderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/GlUniform;set(FFF)V"))
    private boolean jcraft$chunkRender(GlUniform uniform, float cameraX, float cameraY, float cameraZ, RenderLayer layer, MatrixStack stack, double d){
        return CrimsonShaderHandler.INSTANCE.renderingEffect;
    }

    @WrapWithCondition(method = "renderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/GlUniform;upload()V"))
    private boolean jcraft$chunkRender2(GlUniform uniform, RenderLayer layer, MatrixStack stack, double d){
        return CrimsonShaderHandler.INSTANCE.renderingEffect;
    }

    @WrapWithCondition(method = "renderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/VertexBuffer;drawElements()V"))
    private boolean jcraft$chunkRender4(VertexBuffer buffer){
        return CrimsonShaderHandler.INSTANCE.renderingEffect;
    }

 */
}
