package net.arna.jcraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.arna.jcraft.client.rendering.api.PostEffect;
import net.arna.jcraft.client.rendering.api.PostProcessHandler;
import net.arna.jcraft.client.rendering.api.callbacks.PostShaderRenderCallback;
import net.arna.jcraft.client.rendering.shader.JShaderRegistry;
import net.arna.jcraft.common.entity.stand.CreamEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    @Final
    private Camera mainCamera;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V", shift = At.Shift.AFTER))
    private void jcraft$renderWorldLast(float tickDelta, long limitTime, PoseStack matrix, CallbackInfo ci) {
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        matrix.pushPose();
        matrix.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());
        PostProcessHandler.renderLast(matrix);
        matrix.popPose();
    }

    @Inject(method = "resize", at = @At(value = "HEAD"))
    public void jcraft$injectionResizeListener(int width, int height, CallbackInfo ci) {
        PostProcessHandler.resize(width, height);
    }

    @Inject(method = "method_18144", at = @At("HEAD"), cancellable = true)
    private static void preventUserHittingCreamWhenInBall(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof CreamEntity cream && cream.isHalfBall() && JUtils.getStand(Minecraft.getInstance().player) == cream) {
            cir.setReturnValue(false);
        }
    }

    // Right after the world has been rendered, just before the post-effect and GUI rendering.
    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V", shift = AFTER)
    )
    private void hookShaderRender(float tickDelta, long nanoTime, boolean renderLevel, CallbackInfo info) {
        PostShaderRenderCallback.EVENT.invoker().renderEffect(tickDelta);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V", shift = AFTER))
    private void renderShaders(float tickDelta, long finishTimeNano, PoseStack poseStack, CallbackInfo ci)
    {
        // NOTE: The main depth buffer has been cleared by now
        // This is right before it renders the hand, meaning all terrain, entities, etc... have been rendered.
    }

    @Inject(method = "reloadShaders", at = @At("RETURN"))
    private void loadShaders(ResourceProvider resourceProvider, CallbackInfo ci) {
        PostEffect.initAll();
    }
}
