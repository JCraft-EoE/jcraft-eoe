package net.arna.jcraft.common.component.impl.living;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.component.living.CommonGunslingerComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class CommonGunslingerComponentImpl implements CommonGunslingerComponent {

    @Getter
    private @NonNull ItemStack holsteredItem = ItemStack.EMPTY;

    @Override
    public void setHolsteredItem(final @NonNull ItemStack stack) {
        holsteredItem = stack;
    }

    @Override
    public void readFromNbt(@NonNull CompoundTag tag) {
        holsteredItem = ItemStack.of(tag.getCompound("HolsteredItem"));
    }

    @Override
    public void writeToNbt(@NonNull CompoundTag tag) {
        if (!holsteredItem.isEmpty()) {
            tag.put("HolsteredItem", holsteredItem.save(new CompoundTag()));
        }
    }

}
