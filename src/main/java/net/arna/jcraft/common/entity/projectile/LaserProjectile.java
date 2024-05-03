package net.arna.jcraft.common.entity.projectile;

import lombok.Setter;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class LaserProjectile extends PersistentProjectileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int lifetime = 60;
    private final List<Entity> hit = new ArrayList<>();
    @Setter
    private boolean unblockable = false;

    public LaserProjectile(EntityType<? extends LaserProjectile> entityType, World world) {
        super(entityType, world);
        this.pickupType = PickupPermission.DISALLOWED;
    }

    public LaserProjectile(World world, LivingEntity owner) {
        super(JEntityTypeRegistry.LASER_PROJECTILE, owner, world);
        this.setOwner(owner);
    }

    @Override
    protected void age() {
        discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient()) {
            double x = getX(), y = getY(), z = getZ();
            Vec3d vel = getVelocity();

            if (age == 1) {
                for (int i = 0; i < 20; i++) {
                    getWorld().addParticle(
                            ParticleTypes.FIREWORK,
                            x, y, z
                            , (vel.x + random.nextGaussian() * 0.5) * 0.2
                            , (vel.y + random.nextGaussian() * 0.5) * 0.2
                            , (vel.z + random.nextGaussian() * 0.5) * 0.2
                    );
                }
                for (int i = 0; i < 10; i++) {
                    Vec3d frontVel = vel.multiply(random.nextDouble());
                    getWorld().addParticle(
                            ParticleTypes.FIREWORK,
                            x, y, z
                            , frontVel.x
                            , frontVel.y
                            , frontVel.z
                    );
                }
            } else {
                getWorld().addParticle(
                        ParticleTypes.WITCH,
                        x, y, z,
                        vel.x / 2, vel.y / 2, vel.z / 2
                );
            }
        } else if (--lifetime < 1)
            discard();
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (getWorld().isClient) return;
        Entity owner = getOwner();
        if (owner == null) return;
        Entity entity = entityHitResult.getEntity();
        if (owner.hasPassenger(entity) || entity == owner || hit.contains(entity)) return;

        JUtils.projectileDamageLogic(this, getWorld(), entity, getVelocity(), 20, 1, false,
                5f, 0, HitPropertyComponent.HitAnimation.CRUSH, unblockable, false);
        hit.add(entity);
    }

    @Override
    protected float getDragInWater() {
        // Not actually drag, just a multiplier
        return 1.0F;
    }

    @Override
    public ItemStack asItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    // Animations

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
