package net.arna.jcraft.common.component.impl.entity;

import lombok.NonNull;
import net.arna.jcraft.api.component.entity.CommonGravityComponent;
import net.arna.jcraft.common.gravity.RotationAnimation;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.api.RotationParameters;
import net.arna.jcraft.common.gravity.util.EntityTags;
import net.arna.jcraft.common.gravity.util.Gravity;
import net.arna.jcraft.common.gravity.util.GravityChannel;
import net.arna.jcraft.common.gravity.util.NetworkUtil;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public abstract class CommonGravityComponentImpl implements CommonGravityComponent {
    private Direction gravityDirection = Direction.DOWN;
    private Direction defaultGravityDirection = Direction.DOWN;
    private Direction prevGravityDirection = Direction.DOWN;
    private boolean isInverted = false;
    private final RotationAnimation animation = new RotationAnimation();
    private boolean needsInitialSync = false;
    private List<Gravity> gravityList = new ArrayList<>();
    private final Entity entity;

    protected CommonGravityComponentImpl(final Entity entity) {
        this.entity = entity;
    }

    public void onGravityChanged(final Direction oldGravity, final Direction newGravity, final RotationParameters rotationParameters, final boolean initialGravity) {
        entity.fallDistance = 0;
        entity.setPos(entity.position()); // Causes bounding box recalculation

        if (initialGravity) {
            return;
        }

        if (!(entity instanceof ServerPlayer)) {
            //A relativeRotationCentre of zero will result in zero translation
            Vec3 relativeRotationCentre = getCentreOfRotation(oldGravity, newGravity, rotationParameters);
            Vec3 translation = RotationUtil.vecPlayerToWorld(relativeRotationCentre, oldGravity).subtract(RotationUtil.vecPlayerToWorld(relativeRotationCentre, newGravity));
            Direction relativeDirection = RotationUtil.dirWorldToPlayer(newGravity, oldGravity);
            Vec3 smidge = new Vec3(
                    relativeDirection == Direction.EAST ? -1.0E-6D : 0.0D,
                    relativeDirection == Direction.UP ? -1.0E-6D : 0.0D,
                    relativeDirection == Direction.SOUTH ? -1.0E-6D : 0.0D
            );
            smidge = RotationUtil.vecPlayerToWorld(smidge, oldGravity);
            entity.setPos(entity.position().add(translation).add(smidge));
            if (shouldChangeVelocity() && !rotationParameters.alternateCenter()) {
                //Adjust entity position to avoid suffocation and collision
                adjustEntityPosition(oldGravity);
            }
        }

        if (!shouldChangeVelocity()) {
            return;
        }
        final Vec3 realWorldVelocity = getRealWorldVelocity(entity, prevGravityDirection);
        if (rotationParameters.rotateVelocity()) {
            //Rotate velocity with gravity, this will cause things to appear to take a sharp turn
            Vector3f worldSpaceVec = realWorldVelocity.toVector3f();
            worldSpaceVec.rotate(RotationUtil.getRotationBetween(prevGravityDirection, gravityDirection));
            entity.setDeltaMovement(RotationUtil.vecWorldToPlayer(new Vec3(worldSpaceVec), gravityDirection));
        } else {
            //Velocity will be conserved relative to the world, will result in more natural motion
            entity.setDeltaMovement(RotationUtil.vecWorldToPlayer(realWorldVelocity, gravityDirection));
        }
    }

    // getVelocity() does not return the actual velocity. It returns the velocity plus acceleration.
    // Even if the entity is standing still, getVelocity() will still give a downwards vector.
    // The real velocity is this tick position subtract last tick position
    private Vec3 getRealWorldVelocity(final Entity entity, final Direction prevGravityDirection) {
        if (entity.isControlledByLocalInstance()) {
            return new Vec3(
                    entity.getX() - entity.xo,
                    entity.getY() - entity.yo,
                    entity.getZ() - entity.zo
            );
        }

        return RotationUtil.vecPlayerToWorld(entity.getDeltaMovement(), prevGravityDirection);
    }

    private boolean shouldChangeVelocity() {
        if (entity instanceof FishingHook) {
            return true;
        }
        if (entity instanceof FireworkRocketEntity) {
            return true;
        }
        return !(entity instanceof Projectile);
    }

    @NonNull
    private Vec3 getCentreOfRotation(final Direction oldGravity, final Direction newGravity, final RotationParameters rotationParameters) {
        Vec3 relativeRotationCentre = Vec3.ZERO;
        if (entity instanceof EndCrystal) {
            //In the middle of the block below
            relativeRotationCentre = new Vec3(0, -0.5, 0);
        } else if (rotationParameters.alternateCenter()) {
            EntityDimensions dimensions = entity.getDimensions(entity.getPose());
            if (newGravity.getOpposite() == oldGravity) {
                //In the center of the hitbox
                relativeRotationCentre = new Vec3(0, dimensions.height / 2, 0);
            } else {
                //Around the ankles
                relativeRotationCentre = new Vec3(0, dimensions.width / 2, 0);
            }
        }
        return relativeRotationCentre;
    }

    // Adjust position to avoid suffocation in blocks when changing gravity
    private void adjustEntityPosition(final Direction oldGravity) {
        if (entity instanceof AreaEffectCloud || entity instanceof AbstractArrow || entity instanceof EndCrystal) {
            return;
        }

        AABB entityBoundingBox = entity.getBoundingBox();

        // for example, if gravity changed from down to north, move up
        // if gravity changed from down to up, also move up
        Direction movingDirection = oldGravity.getOpposite();

        Iterable<VoxelShape> collisions = entity.level().getCollisions(entity, entityBoundingBox);
        AABB totalCollisionBox = null;
        for (VoxelShape collision : collisions) {
            if (!collision.isEmpty()) {
                AABB boundingBox = collision.bounds();
                if (totalCollisionBox == null) {
                    totalCollisionBox = boundingBox;
                } else {
                    totalCollisionBox = totalCollisionBox.minmax(boundingBox);
                }
            }
        }

        if (totalCollisionBox != null) {
            entity.setPos(entity.position().add(getPositionAdjustmentOffset(
                    entityBoundingBox, totalCollisionBox, movingDirection
            )));
        }
    }

    private static Vec3 getPositionAdjustmentOffset(
            final AABB entityBoundingBox, final AABB nearbyCollisionUnion, final Direction movingDirection
    ) {
        final Direction.Axis axis = movingDirection.getAxis();
        double offset = 0;
        if (movingDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            double pushing = nearbyCollisionUnion.max(axis);
            double pushed = entityBoundingBox.min(axis);
            if (pushing > pushed) {
                offset = pushing - pushed;
            }
        } else {
            double pushing = nearbyCollisionUnion.min(axis);
            double pushed = entityBoundingBox.max(axis);
            if (pushing < pushed) {
                offset = pushed - pushing;
            }
        }

        return new Vec3(movingDirection.step()).scale(offset);
    }

    @Override
    public Direction getGravityDirection() {
        if (canChangeGravity()) {
            return gravityDirection;
        }
        return Direction.DOWN;
    }

    private boolean canChangeGravity() {
        return EntityTags.canChangeGravity(entity);
    }

    @Override
    public Direction getPrevGravityDirection() {
        if (canChangeGravity()) {
            return prevGravityDirection;
        }
        return Direction.DOWN;
    }

    @Override
    public Direction getDefaultGravityDirection() {
        if (canChangeGravity()) {
            return defaultGravityDirection;
        }
        return Direction.DOWN;
    }

    @Override
    public void updateGravity(final RotationParameters rotationParameters, final boolean initialGravity) {
        if (canChangeGravity()) {
            Direction newGravity = getActualGravityDirection();
            Direction oldGravity = gravityDirection;
            if (oldGravity != newGravity) {
                long timeMs = entity.level().getGameTime() * 50;
                if (entity.level().isClientSide) {
                    animation.applyRotationAnimation(
                            newGravity, oldGravity,
                            initialGravity ? 0 : rotationParameters.rotationTime(),
                            entity, timeMs, rotationParameters.rotateView()
                    );
                }
                prevGravityDirection = oldGravity;
                gravityDirection = newGravity;
                onGravityChanged(oldGravity, newGravity, rotationParameters, initialGravity);
            }
        }
    }

    @Override
    public Direction getActualGravityDirection() {
        Direction newGravity = getDefaultGravityDirection();
        Gravity highestPriority = getHighestPriority();
        if (highestPriority != null) {
            newGravity = highestPriority.direction();
        }
        if (isInverted) {
            newGravity = newGravity.getOpposite();
        }
        return newGravity;
    }

    @Nullable
    private Gravity getHighestPriority() {
        return !gravityList.isEmpty() ? Collections.max(gravityList, Comparator.comparingInt(Gravity::priority)) : null;
    }

    @Override
    public void setDefaultGravityDirection(final Direction gravityDirection, final RotationParameters rotationParameters, final boolean initialGravity) {
        if (canChangeGravity()) {
            defaultGravityDirection = gravityDirection;
            updateGravity(rotationParameters, initialGravity);
        }
    }

    @Override
    public void addGravity(final Gravity gravity, final boolean initialGravity) {
        if (canChangeGravity()) {
            gravityList.removeIf(g -> Objects.equals(g.source(), gravity.source()));
            if (gravity.direction() != null) {
                gravityList.add(new Gravity(gravity));
            }
            updateGravity(gravity.rotationParameters(), initialGravity);
        }
    }

    @Override
    public List<Gravity> getGravity() {
        return gravityList;
    }

    @Override
    public void setGravity(final List<Gravity> _gravityList, final boolean initialGravity) {
        final Gravity highestBefore = getHighestPriority();
        gravityList = new ArrayList<>(_gravityList);
        final Gravity highestAfter = getHighestPriority();

        if (highestBefore == highestAfter) {
            return;
        }
        if (highestBefore == null) {
            updateGravity(highestAfter.rotationParameters(), initialGravity);
        } else if (highestAfter == null) {
            updateGravity(highestBefore.rotationParameters(), initialGravity);
        } else if (highestBefore.priority() > highestAfter.priority()) {
            updateGravity(highestBefore.rotationParameters(), initialGravity);
        } else {
            updateGravity(highestAfter.rotationParameters(), initialGravity);
        }
    }

    @Override
    public void invertGravity(final boolean _isInverted, final RotationParameters rotationParameters, final boolean initialGravity) {
        isInverted = _isInverted;
        updateGravity(rotationParameters, initialGravity);
    }

    @Override
    public boolean getInvertGravity() {
        return this.isInverted;
    }

    @Override
    public void clearGravity(final RotationParameters rotationParameters, final boolean initialGravity) {
        gravityList.clear();
        updateGravity(rotationParameters, initialGravity);
    }

    @Override
    public RotationAnimation getGravityAnimation() {
        return animation;
    }


    public void readFromNbt(final CompoundTag nbt) {
        //Store old values
        Direction oldDefaultGravity = defaultGravityDirection;
        List<Gravity> oldList = gravityList;
        boolean oldIsInverted = isInverted;
        //Load values from nbt
        if (nbt.contains("ListSize", Tag.TAG_INT)) {
            int listSize = nbt.getInt("ListSize");
            List<Gravity> newGravityList = new ArrayList<>();
            if (listSize != 0) {
                for (int index = 0; index < listSize; index++) {
                    Gravity newGravity = new Gravity(
                            Direction.from3DDataValue(nbt.getInt("GravityDirection " + index)),
                            nbt.getInt("GravityPriority " + index),
                            nbt.getInt("GravityDuration " + index),
                            nbt.getString("GravitySource " + index)
                    );
                    newGravityList.add(newGravity);
                }
            }
            gravityList = newGravityList;
        }
        prevGravityDirection = Direction.from3DDataValue(nbt.getInt("PrevGravityDirection"));
        defaultGravityDirection = Direction.from3DDataValue(nbt.getInt("DefaultGravityDirection"));
        isInverted = nbt.getBoolean("IsGravityInverted");
        //Update
        RotationParameters rp = new RotationParameters(false, false, false, 0);
        updateGravity(rp, true);
        //Check if an initial sync is required (actual sync happens in tick() because the network handler isn't initialised here yet)
        if (oldDefaultGravity != defaultGravityDirection) {
            needsInitialSync = true;
        }
        if (oldList.isEmpty() != gravityList.isEmpty()) {
            needsInitialSync = true;
        }
        if (oldIsInverted != isInverted) {
            needsInitialSync = true;
        }
    }

    public void writeToNbt(final @NonNull CompoundTag nbt) {
        int index = 0;
        for (Gravity temp : getGravity()) {
            if (temp.direction() != null && temp.source() != null) {
                nbt.putInt("GravityDirection " + index, temp.direction().get3DDataValue());
                nbt.putInt("GravityPriority " + index, temp.priority());
                nbt.putInt("GravityDuration " + index, temp.duration());
                nbt.putString("GravitySource " + index, temp.source());
                index++;
            }
        }
        nbt.putInt("ListSize", index);
        nbt.putInt("PrevGravityDirection", this.getPrevGravityDirection().get3DDataValue());
        nbt.putInt("DefaultGravityDirection", this.getDefaultGravityDirection().get3DDataValue());
        nbt.putBoolean("IsGravityInverted", this.getInvertGravity());
    }

    public void tick() {
        final Entity vehicle = entity.getVehicle();
        if (vehicle != null) {
            addGravity(new Gravity(GravityChangerAPI.getGravityDirection(vehicle), 99999999, 2, "vehicle"), true);
        }
        final List<Gravity> gravityList = getGravity();
        final Gravity highestBefore = getHighestPriority();
        if (gravityList.removeIf(g -> g.duration() == 0)) {
            if (highestBefore != null) {
                updateGravity(highestBefore.rotationParameters(), false);
            } else {
                // Added this here because mobs would not reset gravity after it ran out.
                updateGravity(new RotationParameters(false, false, false, 0), true);
            }
        }
        for (Gravity temp : gravityList) {
            if (temp.duration() > 0) {
                temp.decrementDuration();
            }
        }
        if (!entity.level().isClientSide && needsInitialSync) {
            needsInitialSync = false;
            RotationParameters rotationParameters = new RotationParameters(false, false, false, 0);
            GravityChannel.sendFullStatePacket(entity, NetworkUtil.PacketMode.EVERYONE, rotationParameters, true);
        }
    }
}
