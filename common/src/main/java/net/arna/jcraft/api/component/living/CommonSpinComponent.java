package net.arna.jcraft.api.component.living;

import net.arna.jcraft.api.component.JComponent;
import net.minecraft.world.InteractionHand;

public interface CommonSpinComponent extends JComponent {

    InteractionHand getItemHand();

    void setItemHand(final InteractionHand itemHand);

}
