package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.common.item.StandDiscItem;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneScreenHandlerMixin {

    @Shadow
    @Final
    Container repairSlots;

    @ModifyVariable(method = "createResult", at = @At("STORE"), ordinal = 2)
    private boolean jcraft$allowStandDiscs(boolean value) {
        final ItemStack stack1 = repairSlots.getItem(0);
        final ItemStack stack2 = repairSlots.getItem(1);

        final ItemStack stack = stack1.isEmpty() ? stack2 : stack1;
        if (stack.getItem() != JItemRegistry.STAND_DISC.get()) {
            return value;
        }

        return StandDiscItem.isEmptyDisc(stack); // true means not allowed
    }

    @WrapMethod(method = "removeNonCurses")
    private ItemStack grindStandDisc(final ItemStack stack, final int damage, final int count, final Operation<ItemStack> original) {
        if (stack.getItem() == JItemRegistry.STAND_DISC.get()) {
            final ItemStack copy = original.call(stack, damage, count);
            if (StandDiscItem.isEmptyDisc(copy)) {
                return ItemStack.EMPTY;
            }
            CompoundTag nbt = copy.getTag();
            if (nbt == null) {
                return copy; // Should be impossible
            }
            nbt.remove("StandID");
            nbt.remove("Skin");
            return copy;
        }
        return original.call(stack, damage, count);
    }

}
