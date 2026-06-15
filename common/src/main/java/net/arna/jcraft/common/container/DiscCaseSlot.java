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

}
