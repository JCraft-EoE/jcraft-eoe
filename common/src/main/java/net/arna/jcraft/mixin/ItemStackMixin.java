package net.arna.jcraft.mixin;

import net.arna.jcraft.common.item.MockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "is(Lnet/minecraft/world/item/Item;)Z", at = @At("HEAD"), cancellable = true)
    private void mockItem(Item item, CallbackInfoReturnable<Boolean> cir) {
        ItemStack thiz = (ItemStack) (Object) this;
        if (thiz.getItem() instanceof MockItem) {
            cir.setReturnValue(MockItem.getMockedStack(thiz).is(item));
        }
    }

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    private static void mockItemEqualsCheck(ItemStack left, ItemStack right, CallbackInfoReturnable<Boolean> cir) {
        if (!(left.getItem() instanceof MockItem) && !(right.getItem() instanceof  MockItem)) {
            return;
        }

        ItemStack stack1 = left.getItem() instanceof MockItem ? MockItem.getMockedStack(left) : left;
        ItemStack stack2 = right.getItem() instanceof MockItem ? MockItem.getMockedStack(right) : right;

        cir.setReturnValue(ItemStack.matches(stack1, stack2));
    }
}
