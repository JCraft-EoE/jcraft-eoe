package net.arna.jcraft.common.container;

import lombok.NonNull;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MaybeNoTakeSlot extends Slot {

    protected final ItemStack blackListed;

    public MaybeNoTakeSlot(final ItemStack blackListed, final Container container, final int slot, final int x, final int y) {
        super(container, slot, x, y);
        this.blackListed = blackListed;
    }

    @Override
    public boolean mayPickup(final @NonNull Player player) {
        return getItem() != blackListed;
    }

}
