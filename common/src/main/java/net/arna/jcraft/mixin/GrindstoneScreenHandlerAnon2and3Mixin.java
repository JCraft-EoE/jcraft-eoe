package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.common.item.StandDiscItem;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = {"net/minecraft/world/inventory/GrindstoneMenu$2", "net/minecraft/world/inventory/GrindstoneMenu$3"})
public abstract class GrindstoneScreenHandlerAnon2and3Mixin extends Slot {

    public GrindstoneScreenHandlerAnon2and3Mixin(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @WrapMethod(method = "mayPlace")
    private boolean jcraft$canInsertStandDiscs(final ItemStack stack, final Operation<Boolean> original) {
        if (stack.getItem() == JItemRegistry.STAND_DISC.get() && !StandDiscItem.isEmptyDisc(stack)) {
            // This is executed before the item is inserted, so both slots must be empty when inserting a disc.
            // You cannot insert two discs simultaneously.
            return container.getItem(0).isEmpty() && container.getItem(1).isEmpty();
        }
        return original.call(stack);
    }

}
