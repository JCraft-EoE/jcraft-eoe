package net.arna.jcraft.platform.fabric;

import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import net.arna.jcraft.api.component.entity.CommonGrabComponent;
import net.arna.jcraft.api.component.entity.CommonGravityComponent;
import net.arna.jcraft.api.component.living.*;
import net.arna.jcraft.api.component.player.CommonPhComponent;
import net.arna.jcraft.api.component.player.CommonSpecComponent;
import net.arna.jcraft.api.component.world.CommonShockwaveHandlerComponent;
import net.arna.jcraft.api.component.world.CommonTexasHoldEmComponent;
import net.arna.jcraft.fabric.common.component.JComponents;
import net.arna.jcraft.fabric.common.component.entity.TimeStopComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class JComponentPlatformUtilsImpl {

    public static CommonStandComponent getStandComponent(LivingEntity entity) {
        return JComponents.STAND.get(entity);
    }


    public static CommonSpecComponent getSpecData(LivingEntity livingEntity) {
        return JComponents.SPEC.get(livingEntity);
    }


    public static CommonPhComponent getPhData(Player player) {
        return JComponents.PH.get(player);
    }


    public static CommonCooldownsComponent getCooldowns(LivingEntity entity) {
        return JComponents.COOLDOWNS.get(entity);
    }


    public static Optional<TimeStopComponent> getTimeStopData(Entity entity) {
        return JComponents.TIME_STOP.maybeGet(entity);
    }


    public static CommonMiscComponent getMiscData(LivingEntity entity) {
        return JComponents.MISC.get(entity);
    }


    public static CommonBombTrackerComponent getBombTracker(LivingEntity entity) {
        return JComponents.BOMB_TRACKER.get(entity);
    }


    public static CommonGrabComponent getGrab(LivingEntity entity) {
        return JComponents.GRAB.get(entity);
    }


    public static CommonHitPropertyComponent getHitProperties(LivingEntity livingEntity) {
        return JComponents.HIT_PROPERTY.get(livingEntity);
    }

    public static Optional<CommonGravityComponent> getGravity(Entity entity) {
        if (entity instanceof ComponentProvider p) {
            var cc = p.getComponentContainer();
            if (cc != null) {
                return Optional.of(JComponents.GRAVITY_MODIFIER.get(entity));
            }
        }

        return Optional.empty();
    }

    public static CommonGravityShiftComponent getGravityShift(LivingEntity livingEntity) {
        return JComponents.GRAVITY_SHIFT.get(livingEntity);
    }

    public static CommonShockwaveHandlerComponent getShockwaveHandler(Level world) {
        return JComponents.SHOCKWAVE_HANDLER.get(world);
    }

    public static CommonTexasHoldEmComponent getTexasHoldEm(Level world) {
        return JComponents.TEXAS_HOLD_EM.get(world);
    }

    public static CommonHamonComponent getHamon(LivingEntity living) {
        return JComponents.HAMON.get(living);
    }

    public static CommonVampireComponent getVampirism(LivingEntity living) {
        return JComponents.VAMPIRE.get(living);
    }
}
