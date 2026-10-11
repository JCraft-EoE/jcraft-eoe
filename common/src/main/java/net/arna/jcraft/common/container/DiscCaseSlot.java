package net.arna.jcraft.common.container;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DiscCaseSlot extends Slot {

    public DiscCaseSlot(final Container container, final int slot, final int x, final int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(final @NonNull ItemStack stack) {
        return stack.is(JTagRegistry.DISCS);
    }

    @Override
    public int getMaxStackSize(final @NonNull ItemStack stack) {
        return 1;
    }

    // Shift clicking fills slots through this overload instead, so it has to agree, or a stack of blank discs would land in a single slot.
    @Override
    public int getMaxStackSize() {
        return 1;
    }

}
