package net.arna.jcraft.api.component.entity;

import net.arna.jcraft.api.component.JComponent;
import net.minecraft.world.phys.Vec3;

public interface CommonTimeStopComponent extends JComponent {

    int getTicks();

    //TODO: specialized sync packet
    void setTicks(final int ticks);

    void addTotalVelocity(final Vec3 vel);

    /**
     * @return <code>true</code> if further tick handling shall be interrupted
     */
    boolean tick();

}
