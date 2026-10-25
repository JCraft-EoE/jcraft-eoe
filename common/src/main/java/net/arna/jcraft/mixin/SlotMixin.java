package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.common.item.Peacemaker;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Slot.class)
public abstract class SlotMixin {

    /**
     * Guns are two handed enough to want the offhand kept clear, so they cannot be put there.
     *
     * @return
     */
    @WrapMethod(method = "mayPlace")
    private boolean jcraft$noGunsInOffhand(final ItemStack stack, final Operation<Boolean> original) {
        final Slot self = (Slot)(Object)this;
        if (self.container instanceof Inventory
                && self.getContainerSlot() == Inventory.SLOT_OFFHAND
                && stack.getItem() instanceof Peacemaker) {
            return false;
        }
        return original.call(stack);
    }

}
