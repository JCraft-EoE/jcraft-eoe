package net.arna.jcraft.common.component.living;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public interface MiscComponent extends Component, AutoSyncedComponent, CommonTickingComponent {
    // General
    Vec3d getDesiredVelocity();
    void updateRemoteInputs(int forward, int sideways, boolean jumping);

    void startDamageTimer();
    boolean isOnDamageTimer();

    // TheWorldOverHeavenEntity
    UUID getSlavedTo();
    void setSlavedTo(UUID uuid);
    LivingEntity getMaster();

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
}
