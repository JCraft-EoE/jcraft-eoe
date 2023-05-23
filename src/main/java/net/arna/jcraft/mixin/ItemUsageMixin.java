package net.arna.jcraft.mixin;

import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemUsage.class)
public class ItemUsageMixin {
    @Inject(cancellable = true, at = @At("HEAD"), method = "consumeHeldItem") // Inability to use items while stunned
    private static void jcraft$consumeHeldItem(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (player.hasStatusEffect(JStatusRegister.Dazed)) {
            cir.setReturnValue(TypedActionResult.fail(player.getStackInHand(hand)));
        }
    }
}
