package net.arna.jcraft.mixin.client;

import net.arna.jcraft.client.rendering.skybox.SkyBoxManager;
import net.arna.jcraft.common.util.ITimeStop;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @ModifyArgs(method = "renderEntity(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void jcraft$deltaTick(Args args) {
        Entity entity = args.get(0);
        if (((ITimeStop) entity).getTimeStopTicks() > 0) {
            args.set(5, 0.0F);
        } // Args 0 = ent, 5 = deltatick
    }

    @Inject(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true)
    private void jcraft$renderSky(MatrixStack matrices, Matrix4f matrix4f, float tickDelta, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
        SkyBoxManager skyboxManager = SkyBoxManager.getInstance();
        if (skyboxManager.isEnabled() && skyboxManager.getCurrentSkybox() != null) {
            runnable.run();
            skyboxManager.renderSkyBox((WorldRendererAccess) this, matrices, matrix4f, tickDelta, camera, bl);
            ci.cancel();
        }
    }
}
