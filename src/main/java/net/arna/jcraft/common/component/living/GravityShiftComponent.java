package net.arna.jcraft.common.component.living;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;

public interface GravityShiftComponent extends Component, AutoSyncedComponent, CommonTickingComponent {
    void startRadial();
    void startDirectional();
    void swapRadialType();
    boolean isActive();
    void stop();
}
