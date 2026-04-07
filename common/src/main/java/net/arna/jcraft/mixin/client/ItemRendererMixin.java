package net.arna.jcraft.mixin.client;

import net.arna.jcraft.common.item.DebugWand;
import net.arna.jcraft.common.item.MockItem;
import net.arna.jcraft.common.item.RewindMockItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
    private ItemModelShaper itemModelShaper;

    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private void jcraft$getHeldItemModel(ItemStack stack, Level world, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        Item item = stack.getItem();
        if (item instanceof DebugWand) {
            BakedModel bakedModel = itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft", "trident_in_hand", "inventory")); // this is the model type (not the texture), its insane that copy-pasting this works first try
            ClientLevel clientWorld = world instanceof ClientLevel ? (ClientLevel) world : null;
            BakedModel bakedModel2 = bakedModel.getOverrides().resolve(bakedModel, stack, clientWorld, entity, seed);
            cir.setReturnValue(bakedModel2 == null ? this.itemModelShaper.getModelManager().getMissingModel() : bakedModel2);
        }
    }

    @ModifyVariable(method = "getModel", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private ItemStack mockModelInGetModel(ItemStack stack) {
        return stack.getItem() instanceof MockItem ? MockItem.getMockedStack(stack) : stack;
    }

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private ItemStack mockModelInRenderGuiIcon(ItemStack stack) {
        return stack.getItem() instanceof MockItem ? MockItem.getMockedStack(stack) : stack;
    }

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private ItemStack mockModelInRenderItem(ItemStack stack) {
        return stack.getItem() instanceof MockItem ? MockItem.getMockedStack(stack) : stack;
    }
}
