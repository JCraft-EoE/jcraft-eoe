package net.arna.jcraft.common.entity.projectile;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JStatRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class LaserProjectile extends AbstractArrow {
    private int lifetime = 60;
    private final IntOpenHashSet hit = new IntOpenHashSet(8);
    @Setter
    private boolean unblockable = false;

    public LaserProjectile(Level world) {
        super(JEntityTypeRegistry.LASER_PROJECTILE.get(), world);
        this.pickup = Pickup.DISALLOWED;
    }

    public LaserProjectile(Level world, LivingEntity owner) {
        super(JEntityTypeRegistry.LASER_PROJECTILE.get(), owner, world);
        if (owner instanceof final Player player && !player.level().isClientSide()) {
            player.awardStat(JStatRegistry.VAMPIRE_LASER.get());
        }
    }

    @Override
    protected void tickDespawn() {
        discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            final double x = getX(), y = getY(), z = getZ();
            final Vec3 vel = getDeltaMovement();

            if (tickCount == 1) {
                for (int i = 0; i < 20; i++) {
                    level().addParticle(
                            ParticleTypes.FIREWORK,
                            x, y, z
                            , (vel.x + random.nextGaussian() * 0.5) * 0.2
                            , (vel.y + random.nextGaussian() * 0.5) * 0.2
                            , (vel.z + random.nextGaussian() * 0.5) * 0.2
                    );
                }
                for (int i = 0; i < 10; i++) {
                    final Vec3 frontVel = vel.scale(random.nextDouble());
                    level().addParticle(
                            ParticleTypes.FIREWORK,
                            x, y, z
                            , frontVel.x
                            , frontVel.y
                            , frontVel.z
                    );
                }
            } else {
                level().addParticle(
                        ParticleTypes.WITCH,
                        x, y, z,
                        vel.x / 2, vel.y / 2, vel.z / 2
                );
            }
        } else if (--lifetime < 1) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(@NonNull EntityHitResult entityHitResult) {
        if (level().isClientSide) {
            return;
        }
        Entity owner = getOwner();
        if (owner == null) {
            return;
        }
        Entity entity = entityHitResult.getEntity();
        if (owner.hasPassenger(entity) || entity == owner || hit.contains(entity.getId())) {
            return;
        }

        JUtils.projectileDamageLogic(this, level(), entity, getDeltaMovement(), 20, 1, false,
                5f, 0, CommonHitPropertyComponent.HitAnimation.CRUSH, false, unblockable);
        hit.add(entity.getId());
    }

    @Override
    protected float getWaterInertia() {
        // Not actually drag, just a multiplier
        return 1.0F;
    }

    @Override
    public @NonNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

}
