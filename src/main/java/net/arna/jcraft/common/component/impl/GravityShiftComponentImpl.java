package net.arna.jcraft.common.component.impl;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.moves.cmoon.GravityShiftMove;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.GravityShiftComponent;
import net.arna.jcraft.common.entity.projectile.BlockProjectile;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.Gravity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.arna.jcraft.common.entity.stand.CMoonEntity.GRAVITY_CHANGE_DURATION;

public class GravityShiftComponentImpl implements GravityShiftComponent {
    private enum ShiftType {
        NONE,
        DIRECTIONAL,
        RADIAL_REPULSE,
        RADIAL_ATTRACT;

        public static @NotNull ShiftType fromId(int id) {
            switch (id) {
                default -> { return NONE; }
                case (1) -> { return DIRECTIONAL; }
                case (2) -> { return RADIAL_REPULSE; }
                case (3) -> { return RADIAL_ATTRACT; }
            }
        }
    }

    public static final String GRAVITY_SOURCE = JCraft.MOD_ID + "$" + GravityShiftMove.class.getSimpleName();
    private static final int RANGE_SQR = 10000;

    private final LivingEntity user;
    private final Random random;
    private final List<Entity> shiftedEntities = new ArrayList<>();
    private int time = 0;
    private Vec3d particleDirection = Vec3d.ZERO; // Only for ShiftType.DIRECTIONAL
    private ShiftType type = ShiftType.NONE;

    public GravityShiftComponentImpl(LivingEntity user) {
        this.user = user;
        this.random = Random.create();
    }

    @Override
    public void tick() {
        if (time <= 0) return;
        time--;

        World world = user.getWorld();
        Vec3d pos = user.getPos();

        if (world.isClient) {
            for (int h = 0; h < 256; ++h) {
                Vec3d vel = Vec3d.ZERO;
                double x = pos.x + random.nextTriangular(0, 100);
                double y = pos.y + random.nextTriangular(0, 10);
                double z = pos.z + random.nextTriangular(0, 100);
                switch (type) {
                    case DIRECTIONAL -> vel = particleDirection;
                    case RADIAL_ATTRACT -> vel = new Vec3d(x, y, z).subtract(pos);
                    case RADIAL_REPULSE -> vel = pos.subtract(x, y, z);
                }
                world.addParticle(
                        ParticleTypes.REVERSE_PORTAL,
                        x, y, z,
                        vel.x, vel.y, vel.z);
            }
        } else {
            if (type == ShiftType.DIRECTIONAL) {
                if (time < 1 && !shiftedEntities.isEmpty())
                    shiftedEntities.clear();
                else for (Entity entity : shiftedEntities) {
                    if (entity.squaredDistanceTo(user) > RANGE_SQR)
                        GravityChangerAPI.setGravity(entity, GravityChangerAPI.getGravityList(entity).stream()
                                .filter(g -> !GRAVITY_SOURCE.equals(g.source()))
                                .toList());
                    entity.onLanding(); // No fall damage
                }
            } else {
                if (user.hasStatusEffect(JStatusRegistry.DAZED)) return;

                List<Entity> toCatch = world.getEntitiesByClass(Entity.class, user.getBoundingBox().expand(64), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                for (Entity entity : toCatch) {
                    if (entity.isConnectedThroughVehicle(user)) continue;
                    if (entity instanceof BlockProjectile block && block.getMaster() == user) continue;

                    if (type == ShiftType.RADIAL_ATTRACT) {
                        entity.setVelocity(
                                entity.getVelocity().add(entity.getPos().subtract(pos).normalize().multiply(0.1))
                        );
                    } else {
                        entity.setVelocity(
                                entity.getVelocity().add(pos.subtract(entity.getPos()).normalize().multiply(0.1))
                        );
                    }

                    if (entity instanceof ServerPlayerEntity serverPlayerEntity)
                        serverPlayerEntity.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayerEntity));
                    entity.velocityModified = true;
                }
            }
        }
    }

    @Override
    public void startRadial() {
        time = 200;
        type = ShiftType.RADIAL_ATTRACT;

        sync();
    }

    @Override
    public void startDirectional() {
        time = 600;
        type = ShiftType.DIRECTIONAL;

        Direction lookDir = JUtils.getLookDirection(user);
        List<net.minecraft.entity.Entity> toCatch = user.getWorld().getEntitiesByClass(net.minecraft.entity.Entity.class, user.getBoundingBox().expand(16),
                EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e -> !e.isConnectedThroughVehicle(user)));

        Gravity gravity = new Gravity(lookDir, 3, GRAVITY_CHANGE_DURATION, GRAVITY_SOURCE);
        shiftedEntities.clear();

        for (Entity entity : toCatch) {
            shiftedEntities.add(entity);
            GravityChangerAPI.addGravity(entity, gravity);
        }

        particleDirection = new Vec3d(lookDir.getUnitVector());
        sync();
    }

    @Override
    public boolean isActive() {
        return time > 0;
    }

    @Override
    public void swapRadialType() {
        if (type == ShiftType.DIRECTIONAL) return;
        if (type == ShiftType.RADIAL_ATTRACT)
            type = ShiftType.RADIAL_REPULSE;
        else
            type = ShiftType.RADIAL_ATTRACT;

        sync();
    }

    @Override
    public void stop() {
        time = 0;
        type = null;
        sync();
    }

    private void sync() {
        JComponents.GRAVITY_SHIFT.sync(user);
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        if (player.squaredDistanceTo(user) > RANGE_SQR) return false;
        return GravityShiftComponent.super.shouldSyncWith(player);
    }

    private static Vec3d vecFromArray(int[] arr) {
        return new Vec3d(arr[0], arr[1], arr[2]);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.time = tag.getInt("Time");
        this.type = ShiftType.fromId(tag.getInt("Type"));
        this.particleDirection = vecFromArray(tag.getIntArray("Direction"));
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("Time", time);
        tag.putInt("Type", type.ordinal());

        tag.putIntArray("Direction", new int[]{(int) particleDirection.x, (int) particleDirection.y, (int) particleDirection.z} );
        // Directional gravity shift partially breaks if the server resets.
        // At the moment, I don't care.
    }
}
