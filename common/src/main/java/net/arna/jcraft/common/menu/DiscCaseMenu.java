package net.arna.jcraft.common.menu;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JMenuRegistry;
import net.arna.jcraft.common.container.MaybeNoTakeSlot;
import net.arna.jcraft.common.item.DiscCaseItem;
import net.arna.jcraft.common.container.DiscCaseContainer;
import net.arna.jcraft.common.container.DiscCaseSlot;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DiscCaseMenu extends AbstractContainerMenu {

    private final int slots;

    public DiscCaseMenu(final int id, final Player player) {
        super(JMenuRegistry.DISC_CASE_MENU_TYPE.get(), id);
        ItemStack discCase = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(discCase.getItem() instanceof DiscCaseItem)) {
            discCase = player.getItemInHand(InteractionHand.OFF_HAND);
        }
        if (!(discCase.getItem() instanceof final DiscCaseItem discCaseItem)) {
            slots = 0;
            return;
        }
        slots = discCaseItem.getSize();
        final Container container = DiscCaseContainer.of(discCase);
        // case inventory
        for (int j = 0; j < 2; j++) {
            for (int k = 0; k < slots / 2; k++) {
                this.addSlot(new DiscCaseSlot(container, k + j * (slots / 2), 8 + 9 * (9 - slots / 2) + k * 18, 27 + j * 18));
            }
        }
        // player inventory
        for (int j = 0; j < 3; j++) {
            for (int k = 0; k < 9; k++) {
                this.addSlot(new Slot(player.getInventory(), k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
            }
        }
        // player hotbar
        for (int j = 0; j < 9; j++) {
            this.addSlot(new MaybeNoTakeSlot(discCase, player.getInventory(), j, 8 + j * 18, 142));
        }
    }

    @NonNull
    @Override
    public ItemStack quickMoveStack(final @NonNull Player player, final int index) {
        final Slot slot = getSlot(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        final ItemStack stack = slot.getItem();
        final ItemStack original = stack.copy();
        // The case slots come first, followed by the player's inventory and hotbar.
        final boolean moved = index < slots
                ? moveItemStackTo(stack, slots, super.slots.size(), true)
                : moveIntoCase(stack);
        if (!moved) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        // Vanilla repeats a shift click for as long as this hands the item back, so moving into the case hands back nothing, letting in one disc per click.
        return index < slots ? original : ItemStack.EMPTY;
    }


    // Puts a single disc into the first free case slot
    private boolean moveIntoCase(final ItemStack stack) {
        for (int i = 0; i < slots; i++) {
            final Slot caseSlot = getSlot(i);
            if (!caseSlot.hasItem() && caseSlot.mayPlace(stack)) {
                caseSlot.setByPlayer(stack.split(1));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean stillValid(final @NonNull Player player) {
        return true;
    }

    public int getSlotCount() {
        return slots;
    }

}
