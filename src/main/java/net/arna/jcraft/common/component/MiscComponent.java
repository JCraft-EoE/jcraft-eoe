package net.arna.jcraft.common.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public interface MiscComponent extends Component, AutoSyncedComponent, CommonTickingComponent {
    boolean isThin();
    void setThin(boolean thin);

    Vec3d getDesiredVelocity();
    void updateRemoteInputs(int forward, int sideways, boolean jumping);

    UUID getSlavedTo();
    void setSlavedTo(UUID uuid);

    void startDamageTimer();
    boolean isOnDamageTimer();

    int getStuckKnifeCount();
    void stab();
}
