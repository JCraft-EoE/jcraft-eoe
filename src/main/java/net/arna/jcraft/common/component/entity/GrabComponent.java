package net.arna.jcraft.common.component.entity;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.entity.Entity;

public interface GrabComponent extends Component, AutoSyncedComponent, CommonTickingComponent {
    void startGrab(Entity e, int duration, double distance, double verticalOffset);
    void startGrab(Entity e, int duration, double distance);
    void endGrab();
    int getDuration();
    Entity getAttacker();
    Entity getGrabbed();
}
