package net.arna.jcraft.api.component.living;

import net.arna.jcraft.api.component.JComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public interface CommonMiscComponent extends JComponent {
    // General
    Vec3 getDesiredVelocity();

    void updateRemoteInputs(final int forward, final int sideways, final boolean jumping);

    void startDamageTimer();

    boolean isOnDamageTimer();

    // TheWorldOverHeavenEntity
    UUID getSlavedTo();

    void setSlavedTo(final UUID uuid);

    LivingEntity getMaster();

    // StuckKnivesFeatureRenderer
    int getStuckKnifeCount();

    void stab();

    // WeightlessStatusEffect
    int getHoverTime();

    void setHoverTime(final int hoverTime);

    boolean getPrevNoGrav();

    void setPrevNoGrav(final boolean noGrav);

    // Armored Hits
    int getArmoredHitTicks();

    void displayArmoredHit();

    // AnubisSpec
    float getAttackSpeedMult();

    void setAttackSpeedMult(final float speedMult);

    // MetallicaEntity
    float getMetallicaIron();
    void setMetallicaIron(final float iron);

    // AerosmithEntity
    float getAerosmithOverheat();
    void setAerosmithOverheat(final float overheat);
}
