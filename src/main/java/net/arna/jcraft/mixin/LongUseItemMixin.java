package net.arna.jcraft.mixin;

import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Item.class, BowItem.class, CrossbowItem.class, TridentItem.class})
public class LongUseItemMixin {
    @Inject(cancellable = true, at = @At("HEAD"), method = "onStoppedUsing") // Inability to use items while stunned
    private void jcraft$onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (user.hasStatusEffect(JStatusRegistry.DAZED))
            ci.cancel();
    }
}
