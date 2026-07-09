package net.arna.jcraft.platform.forge;


import net.arna.jcraft.api.component.entity.CommonGrabComponent;
import net.arna.jcraft.api.component.living.*;
import net.arna.jcraft.api.component.player.CommonPhComponent;
import net.arna.jcraft.api.component.player.CommonSpecComponent;
import net.arna.jcraft.api.component.world.CommonShockwaveHandlerComponent;
import net.arna.jcraft.api.component.world.CommonTexasHoldEmComponent;
import net.arna.jcraft.forge.capability.impl.entity.GrabCapability;
import net.arna.jcraft.forge.capability.impl.entity.GravityCapability;
import net.arna.jcraft.forge.capability.impl.entity.TimeStopCapability;
import net.arna.jcraft.forge.capability.impl.living.*;
import net.arna.jcraft.forge.capability.impl.player.PhCapability;
import net.arna.jcraft.forge.capability.impl.living.SpecCapability;
import net.arna.jcraft.forge.capability.impl.world.ShockwaveHandlerCapability;
import net.arna.jcraft.forge.capability.impl.world.TexasHoldEmCapability;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class JComponentPlatformUtilsImpl {
    public static CommonStandComponent getStandComponent(LivingEntity entity) {
        return StandCapability.getCapability(entity);
    }


    public static CommonSpecComponent getSpecData(LivingEntity livingEntity) {
        return SpecCapability.getCapability(livingEntity);
    }


    public static CommonPhComponent getPhData(Player player) {
        return PhCapability.getCapability(player);
    }


    public static CommonCooldownsComponent getCooldowns(LivingEntity entity) {
        return CooldownsCapability.getCapability(entity);
    }


    public static Optional<TimeStopCapability> getTimeStopData(Entity entity) {
        return TimeStopCapability.getCapabilityOptional(entity);
    }


    public static CommonMiscComponent getMiscData(LivingEntity entity) {
        return MiscCapability.getCapability(entity);
    }


    public static CommonBombTrackerComponent getBombTracker(LivingEntity entity) {
        return BombTrackerCapability.getCapability(entity);
    }


    public static CommonGrabComponent getGrab(LivingEntity entity) {
        return GrabCapability.getCapability(entity);
    }


    public static CommonHitPropertyComponent getHitProperties(LivingEntity livingEntity) {
        return HitPropertyCapability.getCapability(livingEntity);
    }

    public static Optional<GravityCapability> getGravity(Entity entity) {
        return GravityCapability.getCapabilityOptional(entity);
    }
    public static CommonGravityShiftComponent getGravityShift(LivingEntity entity) {
        return GravityShiftCapability.getCapability(entity);
    }

    public static CommonShockwaveHandlerComponent getShockwaveHandler(Level world) {
        return ShockwaveHandlerCapability.getCapability(world);
    }

    public static CommonTexasHoldEmComponent getTexasHoldEmHandler(Level world) {
        return TexasHoldEmCapability.getCapability(world);
    }

    public static CommonHamonComponent getHamon(LivingEntity living) {
        return HamonCapability.getCapability(living);
    }

    public static CommonVampireComponent getVampirism(LivingEntity living) {
        return VampireCapability.getCapability(living);
    }
}
