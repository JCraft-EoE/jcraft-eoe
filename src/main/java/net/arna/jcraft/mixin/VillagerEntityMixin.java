package net.arna.jcraft.mixin;

import net.arna.jcraft.common.item.BloodBottleItem;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntity.class)
public class VillagerEntityMixin {
    @Inject(at = @At("HEAD"), method = "interactMob", cancellable = true)
    private void interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof BloodBottleItem)
            cir.setReturnValue(ActionResult.PASS);
    }
}
