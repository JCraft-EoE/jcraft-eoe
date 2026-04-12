package net.arna.jcraft.api.component.living;

import net.arna.jcraft.api.component.JComponent;
import net.arna.jcraft.common.spec.VampireSpec;

public interface CommonVampireComponent extends JComponent {

    float getBlood();

    void setBlood(final float blood);

    default void resetBlood() {
        setBlood(VampireSpec.MAX_BLOOD);
    }

    boolean isVampire();

    void setVampire(final boolean b);
}
