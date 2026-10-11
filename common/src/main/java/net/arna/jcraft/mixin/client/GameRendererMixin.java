package net.arna.jcraft.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.arna.jcraft.client.aim.GunAimHandler;
import net.arna.jcraft.client.rendering.api.PostEffect;
import net.arna.jcraft.client.rendering.api.PostProcessHandler;
import net.arna.jcraft.client.rendering.api.callbacks.PostShaderRenderCallback;
import net.arna.jcraft.common.entity.stand.CreamEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

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

    @WrapMethod(method = "method_18144")
    private static boolean preventUserHittingCreamWhenInBall(final Entity entity, final Operation<Boolean> original) {
        if (entity instanceof CreamEntity cream && cream.isHalfBall() && JUtils.getStand(Minecraft.getInstance().player) == cream) {
            return false;
        }
        return original.call(entity);
    }

    // Right after the world has been rendered, just before the post-effect and GUI rendering.
    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V", shift = AFTER)
    )
    private void hookShaderRender(final float partialTicks, final long nanoTime, final boolean renderLevel, final CallbackInfo info) {
        PostShaderRenderCallback.EVENT.invoker().renderEffect(partialTicks);
    }

    @Inject(method = "reloadShaders", at = @At("RETURN"))
    private void loadShaders(ResourceProvider resourceProvider, CallbackInfo ci) {
        PostEffect.initAll();
    }

    @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
    private double jcraft$aimZoom(final double original, final Camera activeRenderInfo, final float partialTicks, final boolean useFOVSetting) {
        if (!useFOVSetting) {
            return original;
        }
        float progress = GunAimHandler.progress(partialTicks);
        if (progress <= 0f) {
            return original;
        }
        return original / Mth.lerp(progress, 1f, GunAimHandler.ZOOM);
    }

}
