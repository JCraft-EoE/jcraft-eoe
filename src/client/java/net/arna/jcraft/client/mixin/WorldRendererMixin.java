package net.arna.jcraft.client.mixin;

import net.arna.jcraft.client.rendering.skybox.SkyBoxManager;
import net.arna.jcraft.common.util.ITimeStop;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.Uniform;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.ArrayList;
import java.util.List;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Unique private List<BlockPos> blockPosList = new ArrayList<>();

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
            skyboxManager.renderSkyBox(matrices, matrix4f, tickDelta, camera, bl);
            ci.cancel();
        }
    }

    /*
    @WrapWithCondition(method = "renderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/GlUniform;set(FFF)V"))
    private boolean jcraft$chunkRender(GlUniform uniform, float cameraX, float cameraY, float cameraZ, RenderLayer layer, MatrixStack stack, double d){
        return false;
    }

    @WrapWithCondition(method = "renderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/GlUniform;upload()V"))
    private boolean jcraft$chunkRender2(GlUniform uniform, RenderLayer layer, MatrixStack stack, double d){
        return false;
    }

    @WrapWithCondition(method = "renderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/VertexBuffer;drawElements()V"))
    private boolean jcraft$chunkRender4(VertexBuffer buffer){
        return false;
    }

    @SuppressWarnings("all")
    @Inject(method = "renderLayer", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/client/gl/GlUniform;upload()V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void jcraft$preRenderChunk(RenderLayer renderLayer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f positionMatrix, CallbackInfo ci,
                                       //Capture
                                       boolean bl,
                                       ObjectListIterator objectListIterator,
                                       Shader shader,
                                       GlUniform glUniform,
                                       WorldRenderer.ChunkInfo chunkInfo2,
                                       ChunkBuilder.BuiltChunk builtChunk,
                                       VertexBuffer vertexBuffer,
                                       BlockPos blockPos) {


        ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
        if(clientPlayerEntity != null){
            var dx = (float)((double)blockPos.getX() - cameraX);
            var dy = (float)((double)blockPos.getY() - cameraY);
            var dz = (float)((double)blockPos.getZ() - cameraZ);


            int distance = (int)Math.sqrt(Math.pow((Math.abs(dx)), 2) + Math.pow((Math.abs(dy)), 2));
            System.out.println(distance * 0.01);
            matrices.translate(0, - distance * 0.01, 0);
        }



    }
    */
}
