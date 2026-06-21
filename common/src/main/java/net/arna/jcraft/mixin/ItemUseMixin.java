package net.arna.jcraft.mixin;

import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Item.class, BowItem.class, CrossbowItem.class, TridentItem.class})
public class ItemUseMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(Level level, Player player, InteractionHand usedHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (player.hasEffect(JStatusRegistry.DAZED.get())) {
            cir.setReturnValue(InteractionResultHolder.fail(player.getItemInHand(usedHand)));
        }
    }

    @Inject(cancellable = true, at = @At("HEAD"), method = "releaseUsing") // Inability to use items while stunned
    private void jcraft$onStoppedUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (user.hasEffect(JStatusRegistry.DAZED.get())) {
            ci.cancel();
        }
    }
}
