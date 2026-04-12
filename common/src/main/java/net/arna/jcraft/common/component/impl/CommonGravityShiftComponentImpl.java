package net.arna.jcraft.common.component.impl;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.moves.cmoon.GravityShiftMove;
import net.arna.jcraft.api.component.living.CommonGravityShiftComponent;
import net.arna.jcraft.common.entity.projectile.BlockProjectile;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.Gravity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static net.arna.jcraft.common.entity.stand.CMoonEntity.GRAVITY_CHANGE_DURATION;

public abstract class CommonGravityShiftComponentImpl implements CommonGravityShiftComponent {
    private enum ShiftType {
        NONE,
        DIRECTIONAL,
        RADIAL_REPULSE,
        RADIAL_ATTRACT;

        public static @NonNull ShiftType fromId(final int id) {
            switch (id) {
                default -> {
                    return NONE;
                }
                case (1) -> {
                    return DIRECTIONAL;
                }
                case (2) -> {
                    return RADIAL_REPULSE;
                }
                case (3) -> {
                    return RADIAL_ATTRACT;
                }
            }
        }
    }

    public static final String GRAVITY_SOURCE = JCraft.MOD_ID + "$" + GravityShiftMove.class.getSimpleName();
    protected static final int RANGE_SQR = 10000;

    private final LivingEntity user;
    private final RandomSource random;
    private final List<Entity> shiftedEntities = new ArrayList<>();
    protected int time = 0;
    private Vec3 particleDirection = Vec3.ZERO; // Only for ShiftType.DIRECTIONAL
    private ShiftType type = ShiftType.NONE;

    public CommonGravityShiftComponentImpl(final LivingEntity user) {
        this.user = user;
        this.random = RandomSource.create();
    }

    public void tick() {
        if (time <= 0) {
            return;
        }
        time--;

        final Level world = user.level();
        final Vec3 pos = user.position();

        if (world.isClientSide) {
            for (int h = 0; h < 256; ++h) {
                Vec3 vel = Vec3.ZERO;
                double x = pos.x + random.triangle(0, 100);
                double y = pos.y + random.triangle(0, 10);
                double z = pos.z + random.triangle(0, 100);
                switch (type) {
                    case DIRECTIONAL -> vel = particleDirection;
                    case RADIAL_ATTRACT -> vel = new Vec3(x, y, z).subtract(pos);
                    case RADIAL_REPULSE -> vel = pos.subtract(x, y, z);
                }
                world.addParticle(
                        ParticleTypes.REVERSE_PORTAL,
                        x, y, z,
                        vel.x, vel.y, vel.z);
            }
        } else {
            if (type == ShiftType.DIRECTIONAL) {
                if (time < 1 && !shiftedEntities.isEmpty()) {
                    shiftedEntities.clear();
                } else {
                    for (Entity entity : shiftedEntities) {
                        if (entity.distanceToSqr(user) > RANGE_SQR) {
                            GravityChangerAPI.setGravity(entity, GravityChangerAPI.getGravityList(entity).stream()
                                    .filter(g -> !GRAVITY_SOURCE.equals(g.source()))
                                    .toList());
                        }
                        entity.resetFallDistance(); // No fall damage
                    }
                }
            } else {
                if (user.hasEffect(JStatusRegistry.DAZED.get())) {
                    return;
                }

                final List<Entity> toCatch = world.getEntitiesOfClass(Entity.class, user.getBoundingBox().inflate(64), EntitySelector.NO_CREATIVE_OR_SPECTATOR);

                for (final Entity entity : toCatch) {
                    if (entity.isPassengerOfSameVehicle(user)) {
                        continue;
                    }
                    if (entity instanceof BlockProjectile block && block.getMaster() == user) {
                        continue;
                    }

                    if (type == ShiftType.RADIAL_ATTRACT) {
                        entity.setDeltaMovement(
                                entity.getDeltaMovement().add(entity.position().subtract(pos).normalize().scale(0.1))
                        );
                    } else {
                        entity.setDeltaMovement(
                                entity.getDeltaMovement().add(pos.subtract(entity.position()).normalize().scale(0.1))
                        );
                    }

                    if (entity instanceof ServerPlayer serverPlayerEntity) {
                        serverPlayerEntity.connection.send(new ClientboundSetEntityMotionPacket(serverPlayerEntity));
                    }
                    entity.hurtMarked = true;
                }
            }
        }
    }

    @Override
    public void startRadial() {
        time = 200;
        type = ShiftType.RADIAL_ATTRACT;

        sync(user);
    }

    @Override
    public void startDirectional(final int range) {
        time = 600;
        type = ShiftType.DIRECTIONAL;

        Direction lookDir = JUtils.getLookDirection(user);
        List<Entity> toCatch = user.level().getEntitiesOfClass(Entity.class, user.getBoundingBox().inflate(Math.max(1, range)),
                EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> !e.isPassengerOfSameVehicle(user)));

        Gravity gravity = new Gravity(lookDir, 3, GRAVITY_CHANGE_DURATION, GRAVITY_SOURCE);
        shiftedEntities.clear();

        for (Entity entity : toCatch) {
            shiftedEntities.add(entity);
            GravityChangerAPI.addGravity(entity, gravity);
        }

        particleDirection = new Vec3(lookDir.step());
        sync(user);
    }

    @Override
    public boolean isActive() {
        return time > 0;
    }

    @Override
    public void swapRadialType() {
        if (type == ShiftType.DIRECTIONAL) {
            return;
        }
        if (type == ShiftType.RADIAL_ATTRACT) {
            type = ShiftType.RADIAL_REPULSE;
        } else {
            type = ShiftType.RADIAL_ATTRACT;
        }

        sync(user);
    }

    @Override
    public void stop() {
        time = 0;
        type = null;
        sync(user);
    }

    public void sync(final Entity entity) {

    }

    public boolean shouldSyncWith(final ServerPlayer player) {
        if (player.distanceToSqr(user) > RANGE_SQR) {
            return false;
        }
        return true;
    }

    private static Vec3 vecFromArray(final int[] arr) {
        return new Vec3(arr[0], arr[1], arr[2]);
    }

    public void readFromNbt(final CompoundTag tag) {
        this.time = tag.getInt("Time");
        this.type = ShiftType.fromId(tag.getInt("Type"));
        this.particleDirection = vecFromArray(tag.getIntArray("Direction"));
    }

    public void writeToNbt(final CompoundTag tag) {
        tag.putInt("Time", time);
        tag.putInt("Type", type.ordinal());

        tag.putIntArray("Direction", new int[]{(int) particleDirection.x, (int) particleDirection.y, (int) particleDirection.z});
        // Directional gravity shift partially breaks if the server resets.
        // At the moment, I don't care.
    }
}
