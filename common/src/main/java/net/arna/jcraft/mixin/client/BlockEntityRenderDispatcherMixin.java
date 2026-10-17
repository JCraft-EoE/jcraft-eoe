package net.arna.jcraft.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.arna.jcraft.client.util.JClientUtils;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {

    @ModifyArg(
            method = "setupAndRender",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"
            )
    )
    private static float modifyTickDelta(float partialTick, @Local(argsOnly = true) BlockEntity blockEntity) {
        return JClientUtils.getTicksIfInTSRange(blockEntity.getBlockPos()) > 0 ? 0 : partialTick;
    }

}
