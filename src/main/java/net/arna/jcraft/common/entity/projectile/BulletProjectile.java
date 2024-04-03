package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BulletProjectile extends PersistentProjectileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int stunTicks = 0;
    private int damage = 0;
    private float mass = 1f; // Used for penetration calculation

    private static final TrackedData<Float> CALIBER; //mm

    static {
        CALIBER = DataTracker.registerData(BulletProjectile.class, TrackedDataHandlerRegistry.FLOAT);
    }

    public void setCaliber(float cal) {
        dataTracker.set(CALIBER, cal);
    }

    public float getCaliber() {
        return dataTracker.get(CALIBER);
    }

    @Override
    protected void initDataTracker() {
        dataTracker.startTracking(CALIBER, 9f);
        super.initDataTracker();
    }

    public BulletProjectile(EntityType<? extends BulletProjectile> entityType, World world) {
        super(entityType, world);
    }

    public BulletProjectile(World world, LivingEntity owner, float caliber, float length, int stunTicks, int damage) {
        super(JEntityTypeRegistry.BULLET, owner, world);

        setCaliber(caliber);
        this.stunTicks = stunTicks;
        this.damage = damage;

        this.mass = (length * caliber * caliber * MathHelper.PI) * 0.000000013f; // Volume of a cylinder (mm^3) * Density of lead (kg/mm^3)

        setSound(JSoundRegistry.BULLET_RICOCHET);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        HitResult.Type type = hitResult.getType();

        if (type == HitResult.Type.ENTITY) {
            this.onEntityHit((EntityHitResult) hitResult);
            getWorld().emitGameEvent(GameEvent.PROJECTILE_LAND, hitResult.getPos(), GameEvent.Emitter.of(this, null));
        } else if (type == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            BlockPos blockPos = blockHitResult.getBlockPos();
            BlockState blockState = getWorld().getBlockState(blockPos);
            if (blockState.isAir()) return;

            // Calculate penetrative value and decide if it should land
            Vec3i intNormal = blockHitResult.getSide().getVector();
            Vec3d normal = new Vec3d(intNormal.getX(), intNormal.getY(), intNormal.getZ());
            Vec3d impactVec = getVelocity();

            // a*b = |a|*|b|*cos(φ) , a*b = a.dotProduct(b)
            double impactAngleRad = Math.acos(normal.dotProduct(impactVec.normalize())) - Math.PI / 2.0;
            double impactAngleDeg = Math.toDegrees(impactAngleRad);
            //JCraft.LOGGER.info("Impact Angle: " + impactAngle + "");

            // Ek = mv^2/2
            double kineticEnergy = mass * impactVec.lengthSquared() / 2;
            double hardness = blockState.getBlock().getHardness();
            if (hardness < 0)
                hardness = 32767;

            double penAngle = 45.0 + hardness * 5; // This is bs but so is minecraft physics

            //if (getOwner() instanceof PlayerEntity player) player.sendMessage(Text.of("Impact Angle: " + impactAngleDeg + "\n Hardness: " + hardness + "\n Penetration Angle: " + penAngle + "\n Kinetic Energy: " + kineticEnergy));

            boolean lowEnergy = kineticEnergy < 0.001;
            if (impactAngleDeg > penAngle || lowEnergy) { // If penetrated or ran out of energy
                boolean through = hardness <= 1.0; // Go straight through?
                if (lowEnergy || !through) { // Lodged inside block
                    this.onBlockHit(blockHitResult);
                    this.getWorld().emitGameEvent(GameEvent.PROJECTILE_LAND, blockPos, GameEvent.Emitter.of(this, blockState));
                    discard();
                } else if (!getWorld().isClient) {
                    JUtils.serverPlaySound(JSoundRegistry.BULLET_PENETRATE, (ServerWorld) getWorld(), getPos(), 32);
                }
            } else { // Ricochet
                setVelocity(impactVec.add(normal).multiply(0.5 / hardness));
                if (!getWorld().isClient) JUtils.serverPlaySound(JSoundRegistry.BULLET_RICOCHET, (ServerWorld) getWorld(), getPos(), 32);
            }
        }
    }

    @Override
    public void setVelocity(Vec3d velocity) {
        super.setVelocity(velocity);
        velocityModified = true;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        if (entity instanceof LivingEntity living) {
            if (!getWorld().isClient) {
                Entity owner = getOwner();
                LivingEntity target = JUtils.getUserIfStand(living);
                StandEntity.damageLogic(getWorld(), target, getVelocity().normalize(), stunTicks, 1,
                        false, damage, true, 4 + damage, getWorld().getDamageSources().thrown(this, owner), owner, HitPropertyComponent.HitAnimation.MID);
                JUtils.serverPlaySound(JSoundRegistry.BULLET_PENETRATE, (ServerWorld) getWorld(), getPos(), 32);
                discard();
            }
        } else
            super.onEntityHit(entityHitResult);
    }

    @Override
    public void tick() {
        super.tick();

        /*
        if (inGroundTime > 0) {
            if (getWorld().isClient() && inGroundTime == 2) {
                Vec3d rotVec = getRotationVector();
                BlockState blockState = world.getBlockState(getBlockPos().add(rotVec.x / 2, rotVec.y / 2, rotVec.z / 2));
                JCraft.LOGGER.info("Emitting impact effect with blockState " + blockState);
                Vec3d pos = getPos();
                for (int i = 0; i < 16; i++)
                    world.addParticle(
                            new BlockStateParticleEffect(ParticleTypes.BLOCK, blockState),
                            pos.x, pos.y, pos.z,
                            -rotVec.x * 5 + random.nextDouble() - 0.5,
                            -rotVec.y * 5 + random.nextDouble() - 0.5,
                            -rotVec.z * 5 + random.nextDouble() - 0.5
                    );
            } else if (inGroundTime > 10) {
                discard();
            }

            // DEBUG
            JCraft.LOGGER.info("CLIENT: Bullet position is " + getPos());
            if (inGround) {
                world.addParticle(
                        ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                        getX(), getY(), getZ(),
                        0, 0, 0
                );
            } else {
                double l = 3.0;
                for (double i = 0; i < l; i++) {
                    world.addParticle(
                            ParticleTypes.DRAGON_BREATH,
                            MathHelper.lerp(i / l, getX(), prevX), MathHelper.lerp(i / l, getY(), prevY), MathHelper.lerp(i / l, getZ(), prevZ),
                            0, 0, 0
                    );
                }
            }
        }
         */
    }

    @Override
    protected ItemStack asItemStack() {
        //return BulletItem.ofCaliber(getCaliber());
        return ItemStack.EMPTY;
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
