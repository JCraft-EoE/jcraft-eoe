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
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(final @NonNull Player player) {
        return true;
    }

    public int getSlotCount() {
        return slots;
    }

}
