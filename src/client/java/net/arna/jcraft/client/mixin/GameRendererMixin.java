package net.arna.jcraft.client.mixin;

import com.mojang.datafixers.util.Pair;
import net.arna.jcraft.client.registry.JShaderRegistry;
import net.arna.jcraft.client.rendering.PostProcessHandler;
import net.arna.jcraft.common.entity.stand.CreamEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V", shift = At.Shift.AFTER))
    private void jcraft$renderWorldLast(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        matrix.push();
        matrix.translate(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ());
        PostProcessHandler.renderLast(matrix);
        matrix.pop();
    }

    @Inject(method = "loadPrograms", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void jcraft$registerShaders(ResourceFactory manager, CallbackInfo ci, List<Pair<ShaderProgram, Consumer<ShaderProgram>>> list, List<Pair<ShaderProgram, Consumer<ShaderProgram>>> list2) throws IOException {
        JShaderRegistry.init(manager);
        list2.addAll(JShaderRegistry.shaderList);
    }

    @Inject(method = "onResized", at = @At(value = "HEAD"))
    public void jcraft$injectionResizeListener(int width, int height, CallbackInfo ci) {
        PostProcessHandler.resize(width, height);
    }

    @Inject(method = "method_18144", at = @At("HEAD"), cancellable = true)
    private static void preventUserHittingCreamWhenInBall(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof CreamEntity cream && cream.isHalfBall() && JUtils.getStand(MinecraftClient.getInstance().player) == cream)
            cir.setReturnValue(false);
    }
}
