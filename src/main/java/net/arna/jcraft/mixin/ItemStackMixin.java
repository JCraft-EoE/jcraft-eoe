package net.arna.jcraft.mixin;

import net.arna.jcraft.common.item.MockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "isOf", at = @At("HEAD"), cancellable = true)
    private void mockItem(Item item, CallbackInfoReturnable<Boolean> cir) {
        ItemStack thiz = (ItemStack) (Object) this;
        if (MockItem.isMockItem(thiz)) cir.setReturnValue(MockItem.getMockedStack(thiz).isOf(item));
    }

    @Inject(method = "isEqual", at = @At("HEAD"), cancellable = true)
    private void mockItemEqualsCheck(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        ItemStack thiz = (ItemStack) (Object) this;
        if (!MockItem.isMockItem(thiz) && !MockItem.isMockItem(stack)) return;

        ItemStack stack1 = MockItem.isMockItem(thiz) ? MockItem.getMockedStack(thiz) : thiz;
        ItemStack stack2 = MockItem.isMockItem(stack) ? MockItem.getMockedStack(stack) : stack;

        cir.setReturnValue(ItemStack.areEqual(stack1, stack2));
    }
}
