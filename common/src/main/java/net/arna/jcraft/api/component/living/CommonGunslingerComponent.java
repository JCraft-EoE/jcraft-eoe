package net.arna.jcraft.api.component.living;

import lombok.NonNull;
import net.arna.jcraft.api.component.JComponent;
import net.minecraft.world.item.ItemStack;

public interface CommonGunslingerComponent extends JComponent {

    @NonNull
    ItemStack getHolsteredItem();

    void setHolsteredItem(final @NonNull ItemStack stack);

    default boolean hasHolsteredItem() {
        return !getHolsteredItem().isEmpty();
    }

    boolean isFocusActive();

    void setFocusActive(final boolean active);

    float getFocus();

    void setFocus(final float focus);

}
