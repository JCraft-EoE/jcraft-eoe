package net.arna.jcraft.mixin;

import net.arna.jcraft.common.item.StandDiscItem;
import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net/minecraft/screen/GrindstoneScreenHandler$2", "net/minecraft/screen/GrindstoneScreenHandler$3"})
public class GrindstoneScreenHandlerAnon2and3Mixin extends Slot {
    public GrindstoneScreenHandlerAnon2and3Mixin(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void canInsertStandDiscs(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() == JObjectRegistry.STAND_DISC && !StandDiscItem.isEmptyDisc(stack))
            // This is executed before the item is inserted, so both slots must be empty when inserting a disc.
            // You cannot insert two discs simultaneously.
            cir.setReturnValue(inventory.getStack(0).isEmpty() && inventory.getStack(1).isEmpty());
    }
}
