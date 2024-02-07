package net.arna.jcraft.common.component.living;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.util.math.Vec3d;

public interface HitPropertyComponent extends Component, AutoSyncedComponent, CommonTickingComponent {
    // Hit Animation
    long endHitAnimTime();
    Vec3d getRandomRotation();
    HitAnimation getHitAnimation();
    void setHitAnimation(HitAnimation hitAnimation, int duration);

    enum HitAnimation {
        HIGH,
        MID,
        LOW,
        CRUSH,
        LAUNCH,
        ROLL
    }
}
