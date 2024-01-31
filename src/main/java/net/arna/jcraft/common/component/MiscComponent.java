package net.arna.jcraft.common.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public interface MiscComponent extends Component, AutoSyncedComponent, CommonTickingComponent {
    enum HitAnimation {
        LIGHT_MID,
        LIGHT_LOW
    }

    // General
    Vec3d getDesiredVelocity();
    void updateRemoteInputs(int forward, int sideways, boolean jumping);

    void startDamageTimer();
    boolean isOnDamageTimer();

    // TheWorldOverHeavenEntity
    UUID getSlavedTo();
    void setSlavedTo(UUID uuid);

    // StuckKnivesFeatureRenderer
    int getStuckKnifeCount();
    void stab();

    // WeightlessStatusEffect
    int getHoverTime();
    void setHoverTime(int hoverTime);
    boolean getPrevNoGrav();
    void setPrevNoGrav(boolean noGrav);

    // Armored Hits
    int getArmoredHitTicks();
    void displayArmoredHit();

    // AnubisSpec
    float getAttackSpeedMult();
    void setAttackSpeedMult(float speedMult);

    // Hit Animation
    long endHitAnimTime();
    HitAnimation getHitAnimation();
    void setHitAnimation(HitAnimation hitAnimation, int duration);
}
