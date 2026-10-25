package net.arna.jcraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.arna.jcraft.client.renderer.item.SpecDiscItemRenderer;
import net.arna.jcraft.client.renderer.item.StandDiscItemRenderer;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityWithoutLevelRenderer.class)
public abstract class BlockEntityWithoutLevelRendererMixin {

    @Inject(method = "renderByItem", at = @At("HEAD"), cancellable = true)
    private void overrideRenderForStandDiscs(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                                             MultiBufferSource buffer, int packedLight, int packedOverlay, CallbackInfo ci) {
        if (stack.is(JItemRegistry.STAND_DISC.get())) {
            StandDiscItemRenderer.render(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
            ci.cancel();
        }
        else if (stack.is(JItemRegistry.SPEC_DISC.get())) {
            SpecDiscItemRenderer.render(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
            ci.cancel();
        }
    }
}
