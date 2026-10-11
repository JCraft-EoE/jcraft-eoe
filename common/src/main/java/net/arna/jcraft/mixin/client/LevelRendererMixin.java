package net.arna.jcraft.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.arna.jcraft.client.rendering.api.callbacks.PostWorldRenderCallback;
import net.arna.jcraft.client.rendering.skybox.SkyBoxManager;
import net.arna.jcraft.client.util.BlockBreakerClient;
import net.arna.jcraft.client.util.JClientUtils;
import net.arna.jcraft.mixin_logic.StillDepthHolder;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SortedSet;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Unique
    private Entity jcraft$entity;
    private @Unique float jcraft$tsStartPartialTick, jcraft$tsStartRainLevel;

    @Inject(
            method = "renderLevel",
            slice = @Slice(from = @At(value = "FIELD:LAST", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/renderer/LevelRenderer;transparencyChain:Lnet/minecraft/client/renderer/PostChain;")),
            at = {
                    // Only one of these is run, depending on the user's settings.
                    @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PostChain;process(F)V"),
                    @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;depthMask(Z)V", ordinal = 1, shift = At.Shift.AFTER)
            }
    )
    private void jcraft$hookPostWorldRender(PoseStack matrices, float tickDelta, long nanoTime, boolean renderBlockOutline,
                                            Camera camera, GameRenderer renderer, LightTexture lmTexManager, Matrix4f matrix4f, CallbackInfo ci) {
        ((StillDepthHolder) Minecraft.getInstance().getMainRenderTarget()).jcraft$freezeDepth();
        PostWorldRenderCallback.EVENT.invoker().onWorldRendered(matrices, camera, tickDelta, nanoTime);
    }

    @ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V"), index = 1)
    private float jcraft$freezePrecipitationInTimestop(float partialTick) {
        if (!JClientUtils.isInTSRange(Minecraft.getInstance().cameraEntity)) {
            jcraft$tsStartPartialTick = partialTick;
            return partialTick;
        }

        return jcraft$tsStartPartialTick;
    }

    @ModifyExpressionValue(method = "renderSnowAndRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F"))
    private float jcraft$restoreRainLevelInTS(float original) {
        if (!JClientUtils.isInTSRange(Minecraft.getInstance().cameraEntity)) {
            jcraft$tsStartRainLevel = original;
            return original;
        }

        return jcraft$tsStartRainLevel;
    }

    @WrapMethod(method = "tick")
    private void jcraft$cancelTickInTS(final Operation<Void> original) {
        if (!JClientUtils.isInTSRange(Minecraft.getInstance().cameraEntity)) {
            original.call();
        }
    }

    @WrapMethod(method = "renderSky")
    private void jcraft$renderSky(final PoseStack poseStack, final Matrix4f projectionMatrix, final float partialTick, final Camera camera, final boolean isFoggy, final Runnable skyFogSetup, final Operation<Void> original) {
        SkyBoxManager skyboxManager = SkyBoxManager.getInstance();
        if (skyboxManager.isEnabled() && skyboxManager.getCurrentSkybox() != null) {
            skyFogSetup.run();
            skyboxManager.renderSkyBox(poseStack, projectionMatrix, partialTick, camera, isFoggy);
            return;
        }
        original.call(poseStack, projectionMatrix, partialTick, camera, isFoggy, skyFogSetup);
    }

    @ModifyArg(method = "renderEntity",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    )
    private Entity jcraft$deltaTick(Entity entity) {
        this.jcraft$entity = entity;
        return entity;
    }

    @ModifyArg(method = "renderEntity",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"),
            index = 5
    )
    private float jcraft$deltaTick(float yaw) {
        if (JComponentPlatformUtils.getTimeStopData(jcraft$entity).isPresent()) {
            if (JComponentPlatformUtils.getTimeStopData(jcraft$entity).get().getTicks() > 0) {
                return 0;
            } // Args 0 = ent, 5 = deltatick
        }

        return yaw;
    }

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;long2ObjectEntrySet()Lit/unimi/dsi/fastutil/objects/ObjectSet;"))
    private ObjectSet<Long2ObjectMap.Entry<SortedSet<BlockDestructionProgress>>> jcraft$injectBlockBreakerBreakage(ObjectSet<Long2ObjectMap.Entry<SortedSet<BlockDestructionProgress>>> original) {
        if (BlockBreakerClient.isEmpty()) return original;

        ObjectOpenHashSet<Long2ObjectMap.Entry<SortedSet<BlockDestructionProgress>>> set = new ObjectOpenHashSet<>(original);
        set.addAll(BlockBreakerClient.getBreakStates());
        return set;
    }

}
