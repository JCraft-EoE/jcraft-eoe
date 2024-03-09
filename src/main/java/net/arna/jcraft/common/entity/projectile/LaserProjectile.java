package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import static net.arna.jcraft.common.entity.stand.StandEntity.damageLogic;

public class LaserProjectile extends PersistentProjectileEntity implements IAnimatable {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public LaserProjectile(EntityType<? extends LaserProjectile> entityType, World world) {
        super(entityType, world);
        this.pickupType = PickupPermission.DISALLOWED;
    }

    public LaserProjectile(World world, LivingEntity owner) {
        super(JEntityTypeRegistry.LASER_PROJECTILE, owner, world);
        this.setNoGravity(true);
        this.setOwner(owner);
    }

    @Override
    protected void age() {
        discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (world.isClient()) {
            double x = getX(), y = getY(), z = getZ();
            Vec3d vel = getVelocity();

            if (age == 1) {
                for (int i = 0; i < 30; i++) {
                    world.addParticle(
                            ParticleTypes.FIREWORK,
                            x, y, z,
                            (vel.x + random.nextGaussian() * 0.8) * 0.2
                            , (vel.y + random.nextGaussian() * 0.8) * 0.2
                            , (vel.z + random.nextGaussian() * 0.8) * 0.2
                    );
                }
            } else {
                world.addParticle(
                        ParticleTypes.WITCH,
                        x, y, z,
                        vel.x / 2, vel.y / 2, vel.z / 2
                );
            }
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (world.isClient) return;
        Entity owner = getOwner();
        if (owner == null) return;
        Entity entity = entityHitResult.getEntity();
        if (owner.hasPassenger(entity) || entity == owner) return;

        JUtils.projectileDamageLogic(this, world, entity, getRotationVector(), 20, 1, false,
                5f, 0, HitPropertyComponent.HitAnimation.CRUSH, true, false);
        discard();
    }

    @Override
    public ItemStack asItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean hasNoGravity() {
        return false;
    }

    // Animations
    @Override
    public void registerControllers(AnimationData data) {
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
