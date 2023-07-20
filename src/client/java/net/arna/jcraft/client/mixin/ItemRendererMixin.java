package net.arna.jcraft.client.mixin;

import net.arna.jcraft.common.item.DebugWand;
import net.arna.jcraft.common.item.MockItem;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Shadow
    @Final
    private ItemModels models;

    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private void jcraft$getHeldItemModel(ItemStack stack, World world, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        Item item = stack.getItem();
        if (item instanceof DebugWand) {
            BakedModel bakedModel = models.getModelManager().getModel(new ModelIdentifier("minecraft", "trident_in_hand", "inventory")); // this is the model type (not the texture), its insane that copy-pasting this works first try
            ClientWorld clientWorld = world instanceof ClientWorld ? (ClientWorld) world : null;
            BakedModel bakedModel2 = bakedModel.getOverrides().apply(bakedModel, stack, clientWorld, entity, seed);
            cir.setReturnValue(bakedModel2 == null ? this.models.getModelManager().getMissingModel() : bakedModel2);
        }
    }

    @ModifyVariable(method = "getModel", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private ItemStack mockModelInGetModel(ItemStack stack) {
        return MockItem.isMockItem(stack) ? MockItem.getMockedStack(stack) : stack;
    }

    @ModifyVariable(method = "renderGuiItemIcon", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private ItemStack mockModelInRenderGuiIcon(ItemStack stack) {
        return MockItem.isMockItem(stack) ? MockItem.getMockedStack(stack) : stack;
    }

    @ModifyVariable(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private ItemStack mockModelInRenderItem(ItemStack stack) {
        return MockItem.isMockItem(stack) ? MockItem.getMockedStack(stack) : stack;
    }
}
