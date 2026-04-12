package net.arna.jcraft.mixin;

import net.arna.jcraft.api.registry.JDimensionRegistry;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderEyeItem.class)
public class EnderEyeItemMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void jcraft$noAuEndPortal(final UseOnContext context, final CallbackInfoReturnable<InteractionResult> cir) {
        if (context == null) {
            return;
        }
        if (context.getLevel().dimension() == JDimensionRegistry.AU_DIMENSION_KEY) {
            cir.setReturnValue(InteractionResult.PASS);
            cir.cancel();
        }
    }

}
