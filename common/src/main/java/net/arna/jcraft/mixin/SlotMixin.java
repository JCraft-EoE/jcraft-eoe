package net.arna.jcraft.mixin;

import net.arna.jcraft.common.item.Peacemaker;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
    /** Guns are two handed enough to want the offhand kept clear, so they cannot be put there. */
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void jcraft$noGunsInOffhand(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        final Slot self = (Slot) (Object) this;
        if (self.container instanceof Inventory
                && self.getContainerSlot() == Inventory.SLOT_OFFHAND
                && stack.getItem() instanceof Peacemaker) {
            cir.setReturnValue(false);
        }
    }
}
