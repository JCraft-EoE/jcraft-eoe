package net.arna.jcraft.common.gravity.api;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.entity.CommonGravityComponent;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.gravity.RotationAnimation;
import net.arna.jcraft.common.gravity.util.*;
import net.arna.jcraft.common.gravity.util.packet.DefaultGravityPacket;
import net.arna.jcraft.common.gravity.util.packet.InvertGravityPacket;
import net.arna.jcraft.common.gravity.util.packet.OverwriteGravityPacket;
import net.arna.jcraft.common.gravity.util.packet.UpdateGravityPacket;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class GravityChangerAPI {

    /**
     * Returns the applied gravity direction for the given entity
     * This is the direction that directly affects everything this mod changes
     * If the player is riding a vehicle this will be the applied gravity direction of the vehicle
     * Otherwise it will be the main gravity direction of the player itself
     */
    public static Direction getGravityDirection(Entity entity) {
        if (EntityTags.canChangeGravity(entity) && (entity instanceof Player || !JServerConfig.GRAVITY_ONLY_AFFECTS_PLAYERS.getValue())) {
            return JComponentPlatformUtils.getGravity(entity).map(CommonGravityComponent::getGravityDirection).orElse(Direction.DOWN);
        }
        return Direction.DOWN;
    }

    public static List<Gravity> getGravityList(Entity entity) {
        if (EntityTags.canChangeGravity(entity)) {
            return JComponentPlatformUtils.getGravity(entity).map(CommonGravityComponent::getGravity).orElse(new ArrayList<>());
        }
        return new ArrayList<>();
    }

    public static Direction getPrevGravityDirection(Entity entity) {
        if (EntityTags.canChangeGravity(entity)) {
            return JComponentPlatformUtils.getGravity(entity).map(CommonGravityComponent::getPrevGravityDirection).orElse(Direction.DOWN);
        }
        return Direction.DOWN;
    }

    /**
     * Returns the main gravity direction for the given player
     * This may not be the applied gravity direction for the player, see GravityChangerAPI#getAppliedGravityDirection
     */
    public static Direction getDefaultGravityDirection(Entity entity) {
        if (EntityTags.canChangeGravity(entity)) {
            return JComponentPlatformUtils.getGravity(entity).map(CommonGravityComponent::getDefaultGravityDirection).orElse(Direction.DOWN);
        }
        return Direction.DOWN;
    }

    public static Direction getActualGravityDirection(Entity entity) {
        if (EntityTags.canChangeGravity(entity)) {
            return JComponentPlatformUtils.getGravity(entity).map(CommonGravityComponent::getActualGravityDirection).orElse(Direction.DOWN);
        }
        return Direction.DOWN;
    }

    public static boolean getIsInverted(Entity entity) {
        if (EntityTags.canChangeGravity(entity)) {
            return JComponentPlatformUtils.getGravity(entity).map(CommonGravityComponent::getInvertGravity).orElse(false);
        }
        return false;
    }

    public static Optional<RotationAnimation> getGravityAnimation(Entity entity) {
        if (EntityTags.canChangeGravity(entity)) {
            return JComponentPlatformUtils.getGravity(entity).map(CommonGravityComponent::getGravityAnimation);
        }
        return Optional.empty();
    }

    /**
     * Sets the main gravity direction for the given player
     * If the player is a ServerPlayerEntity and gravity direction changed also syncs the direction to the clients
     * If the player is either a ServerPlayerEntity or a ClientPlayerEntity also slightly adjusts player position
     * This may not immediately change the applied gravity direction for the player, see GravityChangerAPI#getAppliedGravityDirection
     */
    public static void addGravity(Entity entity, Gravity gravity) {
        if (onWrongSide(entity) || !EntityTags.canChangeGravity(entity)) {
            return;
        }
        JComponentPlatformUtils.getGravity(entity).ifPresent(gc -> {
            gc.addGravity(gravity, false);
            GravityChannel.UPDATE_GRAVITY.sendToClient(entity, new UpdateGravityPacket(gravity, false), NetworkUtil.PacketMode.EVERYONE);
        });
    }

    /**
     * Update gravity should always be automatically called when you call any api function
     * that could result in a gravityDirection change.
     */
    public static void updateGravity(Entity entity) {
        updateGravity(entity, new RotationParameters());
    }

    public static void updateGravity(Entity entity, RotationParameters rotationParameters) {
        if (EntityTags.canChangeGravity(entity)) {
            JComponentPlatformUtils.getGravity(entity).ifPresent(gc -> gc.updateGravity(rotationParameters, false));
        }
    }

    public static void setGravity(Entity entity, List<Gravity> gravity) {
        if (onWrongSide(entity) || !EntityTags.canChangeGravity(entity)) {
            return;
        }
        JComponentPlatformUtils.getGravity(entity).ifPresent(gc -> {
            gc.setGravity(gravity, false);
            GravityChannel.OVERWRITE_GRAVITY.sendToClient(entity, new OverwriteGravityPacket(gravity, false), NetworkUtil.PacketMode.EVERYONE);
        });
    }

    public static void setIsInverted(Entity entity, boolean isInverted) {
        setIsInverted(entity, isInverted, new RotationParameters());
    }

    public static void setIsInverted(Entity entity, boolean isInverted, RotationParameters rotationParameters) {
        if (onWrongSide(entity) || !EntityTags.canChangeGravity(entity)) {
            return;
        }
        JComponentPlatformUtils.getGravity(entity).ifPresent(gc -> {
            gc.invertGravity(isInverted, rotationParameters, false);
            GravityChannel.INVERT_GRAVITY.sendToClient(entity, new InvertGravityPacket(isInverted, rotationParameters, false), NetworkUtil.PacketMode.EVERYONE);
        });
    }

    public static void clearGravity(Entity entity) {
        clearGravity(entity, new RotationParameters());
    }

    public static void clearGravity(Entity entity, RotationParameters rotationParameters) {
        if (onWrongSide(entity) || !EntityTags.canChangeGravity(entity)) {
            return;
        }
        JComponentPlatformUtils.getGravity(entity).ifPresent(gc -> {
            gc.clearGravity(rotationParameters, false);
            GravityChannel.OVERWRITE_GRAVITY.sendToClient(entity, new OverwriteGravityPacket(new ArrayList<>(), false), NetworkUtil.PacketMode.EVERYONE);
        });
    }

    @Deprecated
    public static void setDefaultGravityDirection(Entity entity, Direction gravityDirection, int animationDurationMs) {
        setDefaultGravityDirection(entity, gravityDirection, new RotationParameters().rotationTime(animationDurationMs));
    }

    public static void setDefaultGravityDirection(Entity entity, Direction gravityDirection) {
        setDefaultGravityDirection(entity, gravityDirection, new RotationParameters());
    }

    public static void setDefaultGravityDirection(Entity entity, Direction gravityDirection, RotationParameters rotationParameters) {
        if (onWrongSide(entity) || !EntityTags.canChangeGravity(entity)) {
            return;
        }
        JComponentPlatformUtils.getGravity(entity).ifPresent(gc -> {
            gc.setDefaultGravityDirection(gravityDirection, rotationParameters, false);
            GravityChannel.DEFAULT_GRAVITY.sendToClient(entity, new DefaultGravityPacket(gravityDirection, rotationParameters, false), NetworkUtil.PacketMode.EVERYONE);
        });
    }

    /**
     * For internal use only, direct calls on GravityComponent will cause de-sync between client and server.
     */
    @Nullable
    public static CommonGravityComponent getGravityComponent(Entity entity) {
        return JComponentPlatformUtils.getGravity(entity).orElse(null);
    }

    /**
     * Returns the world relative velocity for the given player
     * Using minecraft's methods to get the velocity of a the player will return player relative velocity
     */
    public static Vec3 getWorldVelocity(Entity playerEntity) {
        return RotationUtil.vecPlayerToWorld(playerEntity.getDeltaMovement(), getGravityDirection(playerEntity));
    }

    /**
     * Sets the world relative velocity for the given player
     * Using minecraft's methods to set the velocity of an entity will set player relative velocity
     */
    public static void setWorldVelocity(Entity entity, Vec3 worldVelocity) {
        entity.setDeltaMovement(RotationUtil.vecWorldToPlayer(worldVelocity, getGravityDirection(entity)));
    }

    public static void setWorldVelocity(Entity entity, Vector3f worldVelocity) {
        entity.setDeltaMovement(RotationUtil.vecWorldToPlayer(new Vec3(worldVelocity), getGravityDirection(entity)));
    }

    /**
     * Adds to the world relative velocity for the given player
     * Using minecraft's methods to add to the velocity of an entity will set player relative velocity
     */
    public static void addWorldVelocity(Entity entity, double x, double y, double z) {
        Vec3 corrected = RotationUtil.vecWorldToPlayer(new Vec3(x, y, z), getGravityDirection(entity));
        entity.push(corrected.x, corrected.y, corrected.z);
    }

    public static void addWorldVelocity(Entity entity, Vec3 worldVelocity) {
        Vec3 corrected = RotationUtil.vecWorldToPlayer(worldVelocity, getGravityDirection(entity));
        entity.push(corrected.x, corrected.y, corrected.z);
    }

    /**
     * Returns eye position offset from feet position for the given entity
     */
    public static Vec3 getEyeOffset(Entity entity) {
        return RotationUtil.vecPlayerToWorld(0, (double) entity.getEyeHeight(), 0, getGravityDirection(entity));
    }

    private static boolean onWrongSide(Entity entity) {
        if (entity.level().isClientSide) {
            JCraft.LOGGER.error("GravityChangerAPI function cannot be called from the client, use dedicated client class. ", new Exception());
            return true;
        }
        return false;
    }
}
