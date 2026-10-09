package net.arna.jcraft.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.arna.jcraft.api.component.entity.CommonGrabComponent;
import net.arna.jcraft.api.component.entity.CommonGravityComponent;
import net.arna.jcraft.api.component.entity.CommonTimeStopComponent;
import net.arna.jcraft.api.component.living.*;
import net.arna.jcraft.api.component.player.CommonPhComponent;
import net.arna.jcraft.api.component.player.CommonSpecComponent;
import net.arna.jcraft.api.component.world.CommonShockwaveHandlerComponent;
import net.arna.jcraft.api.component.world.CommonTexasHoldEmComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import java.util.Optional;

/**
 * What's Sterner Cooking???
 * <p>
 * So some things are only available on either forge or fabric, so we need a way to use api specific methods sometimes.
 * Both our forge and fabric project have a {@link net/arna/platform/$/JComponentPlatformUtilsImpl} which handles the platform.
 */
public class JComponentPlatformUtils {

    @ExpectPlatform
    public static CommonStandComponent getStandComponent(final LivingEntity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static CommonSpecComponent getSpecData(final LivingEntity livingEntity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static CommonPhComponent getPhData(final Player player) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static CommonCooldownsComponent getCooldowns(final LivingEntity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Optional<CommonTimeStopComponent> getTimeStopData(final Entity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static CommonMiscComponent getMiscData(final LivingEntity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static CommonBombTrackerComponent getBombTracker(final LivingEntity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static CommonGrabComponent getGrab(final LivingEntity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static CommonHitPropertyComponent getHitProperties(final LivingEntity livingEntity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Optional<CommonGravityComponent> getGravity(final Entity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static CommonGravityShiftComponent getGravityShift(final LivingEntity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static CommonShockwaveHandlerComponent getShockwaveHandler(final Level world) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static CommonTexasHoldEmComponent getTexasHoldEmComponent(final Level world) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static CommonHamonComponent getHamon(final LivingEntity living) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static CommonVampireComponent getVampirism(final LivingEntity living) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static CommonGunslingerComponent getGunslinger(final LivingEntity living) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static CommonSpinComponent getSpin(final LivingEntity living) {
        throw new AssertionError();
    }

}
