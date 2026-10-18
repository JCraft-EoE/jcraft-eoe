package net.arna.jcraft.api.component.living;

import net.arna.jcraft.api.component.JComponent;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.HitResult;

public interface CommonSpinComponent extends JComponent {

    Byte getSlot();

    void setSlot(final Byte slot);

    void cancelSpinning();

    void endSpinning();

    void observeNature(final HitResult observation, final LevelReader level);

    int natureObservationAmount();

    void resetNatureObservations();

    void goldenRatioC(final int amount);

    void goldenRatioB(final int amount);

    void goldenRatioA(final int amount);
}
