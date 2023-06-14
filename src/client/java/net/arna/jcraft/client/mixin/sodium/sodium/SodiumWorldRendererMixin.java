package net.arna.jcraft.client.mixin.sodium.sodium;

import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.arna.jcraft.client.rendering.handler.CrimsonShaderHandler;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SodiumWorldRenderer.class)
public class SodiumWorldRendererMixin {

    @Inject(method = "drawChunkLayer", at = @At("HEAD"), cancellable = true)
    private void jcraft$killSodium(RenderLayer renderLayer, MatrixStack matrixStack, double x, double y, double z, CallbackInfo ci){
        if(CrimsonShaderHandler.INSTANCE.renderingEffect){
            ci.cancel();
        }
    }
}
