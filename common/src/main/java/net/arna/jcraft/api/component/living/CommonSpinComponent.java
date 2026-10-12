package net.arna.jcraft.api.component.living;

import net.arna.jcraft.api.component.JComponent;

public interface CommonSpinComponent extends JComponent {

    Byte getSlot();

    void setSlot(final Byte slot);

    void cancelSpinning();

    void endSpinning();

}
