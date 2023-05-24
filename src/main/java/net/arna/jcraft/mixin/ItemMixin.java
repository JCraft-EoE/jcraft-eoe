package net.arna.jcraft.mixin;

import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Item.class, EnderPearlItem.class, PotionItem.class, ThrowablePotionItem.class})
public class ItemMixin {
    @Inject(cancellable = true, at = @At("HEAD"), method = "use") // Inability to use items while stunned
    private void jcraft$use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (user.hasStatusEffect(JStatusRegister.DAZED)) {
            cir.setReturnValue(TypedActionResult.fail(user.getStackInHand(hand)));
        }
    }
}
