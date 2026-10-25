package net.arna.jcraft.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.arna.jcraft.client.util.JClientUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HumanoidArmorLayer.class)
public abstract class ArmorFeatureRendererMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {

    @WrapMethod(method = "renderArmorPiece")
    private void jcraft$renderArmor(final PoseStack poseStack, final MultiBufferSource buffer, final T livingEntity, final EquipmentSlot slot, final int packedLight, final A model, final Operation<Void> original) {
        if (!JClientUtils.shouldNotRender(livingEntity)) {
            original.call(poseStack, buffer, livingEntity, slot, packedLight, model);
        }
    }

}
