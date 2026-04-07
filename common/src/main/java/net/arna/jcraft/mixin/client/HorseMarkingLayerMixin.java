package net.arna.jcraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HorseMarkingLayer;
import net.minecraft.world.entity.animal.horse.Horse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * @see HorseRendererMixin
 */
@Mixin(HorseMarkingLayer.class)
public class HorseMarkingLayerMixin {

    @Unique
    private static final List<String> CUSTOM_HORSE_NAMES =List.of("El Condor Pasa", "Pinkie Pie", "Silver Bullet", "Slow Dancer", "Valkyrie");

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/horse/Horse;FFFFFF)V", at = @At("HEAD"), cancellable = true)
    public void jcraft$render(final PoseStack poseStack, final MultiBufferSource buffer, final int packedLight, final Horse livingEntity, final float limbSwing, final float limbSwingAmount, final float partialTicks, final float ageInTicks, final float netHeadYaw, final float headPitch, final CallbackInfo ci) {
        if (livingEntity.hasCustomName()) {
            if (CUSTOM_HORSE_NAMES.contains(livingEntity.getCustomName().getString())) {
                ci.cancel();
            }
        }
    }

}
