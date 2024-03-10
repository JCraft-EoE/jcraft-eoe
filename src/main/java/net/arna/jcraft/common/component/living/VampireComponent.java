package net.arna.jcraft.common.component.living;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;

public interface VampireComponent extends Component, AutoSyncedComponent, CommonTickingComponent {
    float getBlood();
    void setBlood(float blood);
    boolean isVampire();
    void setVampire(boolean b);
}
