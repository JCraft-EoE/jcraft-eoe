package net.arna.jcraft.mixin;

import net.arna.jcraft.common.attack.moves.kingcrimson.TimeEraseMove;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.arna.jcraft.common.item.MockItem;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "is(Lnet/minecraft/world/item/Item;)Z", at = @At("HEAD"), cancellable = true)
    private void jcraft$mockItem(Item item, CallbackInfoReturnable<Boolean> cir) {
        ItemStack thiz = (ItemStack) (Object) this;
        if (thiz.getItem() instanceof MockItem) {
            cir.setReturnValue(MockItem.getMockedStack(thiz).is(item));
        }
    }

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    private static void jcraft$mockItemEqualsCheck(ItemStack left, ItemStack right, CallbackInfoReturnable<Boolean> cir) {
        if (!(left.getItem() instanceof MockItem) && !(right.getItem() instanceof  MockItem)) {
            return;
        }

        ItemStack stack1 = left.getItem() instanceof MockItem ? MockItem.getMockedStack(left) : left;
        ItemStack stack2 = right.getItem() instanceof MockItem ? MockItem.getMockedStack(right) : right;

        cir.setReturnValue(ItemStack.matches(stack1, stack2));
    }

    @Inject(method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", at = @At("HEAD"))
    private void jcraft$endTEOnUse(final UseOnContext context, final CallbackInfoReturnable<InteractionResult> cir) {
        final Player player = context.getPlayer();
        if (player != null && JUtils.inTimeErase(player) && JUtils.getStand(player) instanceof KingCrimsonEntity kc) {
            final TimeEraseMove te = kc.getMove(TimeEraseMove.class);
            if (te != null) {
                te.cancelTE(kc);
            }
        }
    }
}
