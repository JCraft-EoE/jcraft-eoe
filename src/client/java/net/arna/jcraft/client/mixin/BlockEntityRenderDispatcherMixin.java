package net.arna.jcraft.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.arna.jcraft.client.util.JClientUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {

    @ModifyVariable(method = "render(Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
            at = @At("HEAD"), argsOnly = true)
    private static float overrideTickDeltaIfInTimeStop(float tickDelta, @Local BlockEntity blockEntity) {
        return JClientUtils.getTicksIfInTSRange(blockEntity.getPos()) > 0 ? 0 : tickDelta;
    }
}
