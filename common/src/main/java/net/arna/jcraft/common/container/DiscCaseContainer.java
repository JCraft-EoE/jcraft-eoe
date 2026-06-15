package net.arna.jcraft.common.container;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.arna.jcraft.common.item.DiscCaseItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class DiscCaseContainer extends SimpleContainer {

    protected final ItemStack discCase;

    protected DiscCaseContainer(final int size, final @NonNull ItemStack discCase) {
        super(size);
        this.discCase = discCase;
    }

    @Override
    public boolean canAddItem(final @NonNull ItemStack stack) {
        return super.canAddItem(stack) && stack.is(JTagRegistry.DISCS);
    }

    @NonNull
    @Override
    public ItemStack addItem(final @NonNull ItemStack stack) {
        final ItemStack result = super.addItem(stack);
        updateDiscCaseStackContent();
        return result;
    }

    @Override
    public void setItem(final int slot, final @NonNull ItemStack stack) {
        super.setItem(slot, stack);
        updateDiscCaseStackContent();
    }

    protected void updateDiscCaseStackContent() {
        final CompoundTag tag = discCase.getOrCreateTag();
        tag.put("Content", createTag());
        discCase.setTag(tag);
    }

    @NonNull
    @Override
    public ListTag createTag() {
        final ListTag listTag = new ListTag();
        for (int i = 0; i < this.getContainerSize(); i++) {
            listTag.add(getItem(i).save(new CompoundTag()));
        }
        return listTag;
    }

    @Override
    public void fromTag(final @NonNull ListTag containerNbt) {
        clearContent();
        final int maxSize = Math.min(getContainerSize(), containerNbt.size());
        for (int i = 0; i < maxSize; i++) {
            super.setItem(i, ItemStack.of(containerNbt.getCompound(i))); // NOT this.addItem(stack)
        }
    }

    public static DiscCaseContainer of(final ItemStack discCase) {
        if (!(discCase.getItem() instanceof final DiscCaseItem discCaseItem)) {
            throw new IllegalArgumentException("Not a Disc Case item!");
        }
        final DiscCaseContainer container = new DiscCaseContainer(discCaseItem.getSize(), discCase);
        final CompoundTag tag = discCase.getOrCreateTag();
        ListTag content = tag.getList("Content", CompoundTag.TAG_COMPOUND);
        container.fromTag(content);
        return container;
    }

}
