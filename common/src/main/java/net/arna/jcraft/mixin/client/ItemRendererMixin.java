package net.arna.jcraft.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.common.item.DebugWand;
import net.arna.jcraft.common.item.MockItem;
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
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow
    @Final
    private ItemModelShaper itemModelShaper;

    @WrapMethod(method = "getModel")
    private BakedModel jcraft$getHeldItemModel(final ItemStack stack, final Level level, final LivingEntity entity, final int seed, final Operation<BakedModel> original) {
        Item item = stack.getItem();
        if (item instanceof DebugWand) {
            BakedModel bakedModel = itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft", "trident_in_hand", "inventory")); // this is the model type (not the texture), its insane that copy-pasting this works first try
            ClientLevel clientWorld = level instanceof ClientLevel ? (ClientLevel) level : null;
            BakedModel bakedModel2 = bakedModel.getOverrides().resolve(bakedModel, stack, clientWorld, entity, seed);
            return bakedModel2 == null ? this.itemModelShaper.getModelManager().getMissingModel() : bakedModel2;
        }
        return original.call(stack, level, entity, seed);
    }

    @ModifyVariable(method = "getModel", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private ItemStack jcraft$mockModelInGetModel(final ItemStack stack) {
        return stack.getItem() instanceof MockItem ? MockItem.getMockedStack(stack) : stack;
    }

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private ItemStack jcraft$mockModelInRenderGuiIcon(ItemStack stack) {
        return stack.getItem() instanceof MockItem ? MockItem.getMockedStack(stack) : stack;
    }

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private ItemStack jcraft$mockModelInRenderItem(ItemStack stack) {
        return stack.getItem() instanceof MockItem ? MockItem.getMockedStack(stack) : stack;
    }

}
