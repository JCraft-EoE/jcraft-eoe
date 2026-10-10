package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import net.arna.jcraft.common.attack.moves.kingcrimson.TimeEraseMove;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.arna.jcraft.common.item.MockItem;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @WrapMethod(method = "is(Lnet/minecraft/world/item/Item;)Z")
    private boolean jcraft$mockItem(final Item item, final Operation<Boolean> original) {
        ItemStack thiz = (ItemStack)(Object)this;
        if (thiz.getItem() instanceof MockItem) {
            return MockItem.getMockedStack(thiz).is(item);
        }
        return original.call(item);
    }

    @WrapMethod(method = "matches")
    private static boolean jcraft$mockItemEqualsCheck(final ItemStack stack, final ItemStack other, final Operation<Boolean> original) {
        if (stack.getItem() instanceof MockItem || other.getItem() instanceof  MockItem) {
            ItemStack stack1 = stack.getItem() instanceof MockItem ? MockItem.getMockedStack(stack) : stack;
            ItemStack stack2 = other.getItem() instanceof MockItem ? MockItem.getMockedStack(other) : other;
            return ItemStack.matches(stack1, stack2);
        }
        return original.call(stack, other);
    }

    @Definition(id = "stack", local = @Local(type = ItemStack.class, ordinal = 0, argsOnly = true))
    @Definition(id = "is", method = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    @Definition(id = "other", local = @Local(type = ItemStack.class, ordinal = 1, argsOnly = true))
    @Definition(id = "getItem", method = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;")
    @Expression("stack.is(other.getItem())")
    @ModifyExpressionValue(method = "isSameItemSameTags(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean jcraft$mockItemStackEqualsCheck(final boolean original, final ItemStack stack, final ItemStack other) {
        return original || (stack.getItem() instanceof MockItem && other.getItem() instanceof MockItem);
    }

    @Inject(method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", at = @At("HEAD"))
    private void jcraft$endTEOnUseOn(final UseOnContext context, final CallbackInfoReturnable<InteractionResult> cir) {
        final Player player = context.getPlayer();
        if (JUtils.inTimeErase(player) && JUtils.getStand(player) instanceof KingCrimsonEntity kc) {
            final TimeEraseMove te = kc.getTimeEraseMove();
            if (te != null) {
                te.cancelTE(kc);
            }
        }
    }

    @Inject(method = "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;", at = @At("HEAD"))
    private void jcraft$endTEOnUse(final Level level, final Player player, final InteractionHand usedHand, final CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (JUtils.inTimeErase(player) && JUtils.getStand(player) instanceof KingCrimsonEntity kc) {
            final TimeEraseMove te = kc.getTimeEraseMove();
            if (te != null) {
                te.cancelTE(kc);
            }
        }
    }

}
