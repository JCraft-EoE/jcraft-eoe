package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import net.arna.jcraft.api.AttackData;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.events.JServerEvents;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import static net.arna.jcraft.api.Attacks.damageLogic;

public class BulletProjectile extends AbstractArrow {
    private int stunTicks;
    private float damage;
    private float mass; // Used for penetration calculation
    private boolean cancelMoves = false;

    private static final EntityDataAccessor<Float> CALIBER; //mm

    static {
        CALIBER = SynchedEntityData.defineId(BulletProjectile.class, EntityDataSerializers.FLOAT);
    }

    public void setCaliber(float cal) {
        entityData.set(CALIBER, cal);
    }

    public float getCaliber() {
        return entityData.get(CALIBER);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(CALIBER, 9f);
        super.defineSynchedData();
    }

    public BulletProjectile(Level world) {
        super(JEntityTypeRegistry.BULLET.get(), world);
    }

    public BulletProjectile(Level world, LivingEntity owner, float caliber, float length, int stunTicks, float damage) {
        super(JEntityTypeRegistry.BULLET.get(), owner, world);

        setCaliber(caliber);
        this.stunTicks = stunTicks;
        this.damage = damage;
        this.mass = (length * caliber * caliber * Mth.PI) * 0.000000013f; // Volume of a cylinder (mm^3) * Density of lead (kg/mm^3)

        setSoundEvent(JSoundRegistry.BULLET_RICOCHET.get());
    }

    @Override
    protected void onHit(HitResult hitResult) {
        final HitResult.Type type = hitResult.getType();

        if (type == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult) hitResult);
            level().gameEvent(GameEvent.PROJECTILE_LAND, hitResult.getLocation(), GameEvent.Context.of(this, null));
        } else if (type == HitResult.Type.BLOCK) {
            final BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            final BlockPos blockPos = blockHitResult.getBlockPos();
            final BlockState blockState = level().getBlockState(blockPos);
            if (blockState.isAir()) {
                return;
            }

            // Calculate penetrative value and decide if it should land
            final Vec3i intNormal = blockHitResult.getDirection().getNormal();
            final Vec3 normal = new Vec3(intNormal.getX(), intNormal.getY(), intNormal.getZ());
            final Vec3 impactVec = getDeltaMovement();

            // a*b = |a|*|b|*cos(φ) , a*b = a.dotProduct(b)
            final double impactAngleRad = Math.acos(normal.dot(impactVec.normalize())) - Math.PI / 2.0;
            final double impactAngleDeg = Math.toDegrees(impactAngleRad);

            // Ek = mv^2/2
            final double kineticEnergy = mass * impactVec.lengthSqr() / 2;
            double hardness = blockState.getBlock().defaultDestroyTime();
            if (hardness < 0) { // Unbreakable block
                hardness = Double.MAX_VALUE;
            }

            final double penAngle = 45.0 + hardness * 5; // This is bs but so is minecraft physics

            final boolean lowEnergy = kineticEnergy < 0.001;
            if (impactAngleDeg > penAngle || lowEnergy) { // If penetrated or ran out of energy
                final boolean through = hardness <= 1.0; // Go straight through?
                if (lowEnergy || !through) { // Lodged inside block
                    this.onHitBlock(blockHitResult);
                    this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockPos, GameEvent.Context.of(this, blockState));

                    // Add block hit particle effect (when bullet stops/lodges)
                    if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                JParticleTypeRegistry.HITSPARK_1.get(), // Use hitspark_1 for block hits
                                getX(), getY(), getZ(),
                                1, // particle count
                                0, 0, 0, // spread
                                0 // speed
                        );
                    }

                    discard();
                } else if (!level().isClientSide()) {
                    JUtils.serverPlaySound(JSoundRegistry.BULLET_PENETRATE.get(), (ServerLevel) level(), position(), 32);

                    // Add penetration particle effect
                    if (level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                JParticleTypeRegistry.STUN_PIERCE.get(), // jcraft:stun_pierce for penetration
                                getX(), getY(), getZ(),
                                1, // particle count
                                0, 0, 0, // spread
                                0 // speed
                        );
                    }
                }
            } else { // Ricochet
                setDeltaMovement(impactVec.add(normal).scale(0.5 / hardness));
                if (!level().isClientSide()) {
                    JUtils.serverPlaySound(JSoundRegistry.BULLET_RICOCHET.get(), (ServerLevel) level(), position(), 32);

                    // Add ricochet particle effect
                    if (level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                JParticleTypeRegistry.BLOCKSPARK.get(), // jcraft:blockspark for ricochet
                                getX(), getY(), getZ(),
                                1, // particle count
                                0, 0, 0, // spread
                                0 // speed
                        );
                    }
                }
            }
        }
    }

    @Override
    public void setDeltaMovement(@NonNull Vec3 velocity) {
        super.setDeltaMovement(velocity);
        hurtMarked = true;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        final Entity entity = entityHitResult.getEntity();
        if (entity instanceof LivingEntity living) {
            if (!level().isClientSide()) {
                final Entity owner = getOwner();
                final LivingEntity target = JUtils.getUserIfStand(living);
                DamageSource thrown = level().damageSources().thrown(this, owner);

                AttackData attackData = new AttackData( getDeltaMovement().normalize(),
                        stunTicks, 1, false, damage, true, (int) (4 + damage),
                        thrown, owner, CommonHitPropertyComponent.HitAnimation.MID,
                        null, false, false, cancelMoves
                );

                damageLogic(level(), target, attackData);

                if (entity instanceof LivingEntity livingEntity) {
                    JServerEvents.maybeLaunch(livingEntity, thrown, (ServerLevel) level(), livingEntity.getEffect(JStatusRegistry.DAZED.get()), owner );
                }
                JUtils.serverPlaySound(JSoundRegistry.BULLET_PENETRATE.get(), (ServerLevel) level(), position(), 32);

                // Add entity hit particle effect
                if (level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            JParticleTypeRegistry.HITSPARK_2.get(),
                            getX(), getY(), getZ(),
                            1, // particle count
                            0, 0, 0, // spread
                            0 // speed
                    );
                }

                discard();
            }
        } else {
            super.onHitEntity(entityHitResult);
        }
    }

    @Override
    public void tick() {
        super.tick();

        // Only create particle trail if bullet is moving and not stuck in ground
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel && !this.inGround) {
            // Get current velocity
            Vec3 velocity = this.getDeltaMovement();
            double speed = velocity.length();

            // Only spawn particles if bullet is moving fast enough
            if (speed > 0.1) {
                // Get current position
                double currentX = this.getX();
                double currentY = this.getY();
                double currentZ = this.getZ();

                // Calculate previous position based on velocity
                double prevX = currentX - velocity.x;
                double prevY = currentY - velocity.y;
                double prevZ = currentZ - velocity.z;

                // Calculate distance for particle density
                double distance = Math.sqrt(
                        Math.pow(currentX - prevX, 2) +
                                Math.pow(currentY - prevY, 2) +
                                Math.pow(currentZ - prevZ, 2)
                );

                // Spawn particles along the path
                int particleCount = Math.max(1, (int)(distance * 4)); // 4 particles per block for denser trail

                for (int i = 0; i < particleCount; i++) {
                    double t = (double) i / particleCount;
                    double x = prevX + (currentX - prevX) * t;
                    double y = prevY + (currentY - prevY) * t;
                    double z = prevZ + (currentZ - prevZ) * t;

                    // Use end rod particles for the trail
                    serverLevel.sendParticles(
                            ParticleTypes.END_ROD,
                            x, y, z,
                            1, // particle count
                            0.02, 0.02, 0.02, // tiny spread for slight variation
                            0.0 // no extra velocity
                    );
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("StunTicks", stunTicks);
        nbt.putFloat("Mass", mass);
        nbt.putFloat("Damage", damage);
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        stunTicks = nbt.getInt("StunTicks");
        mass = nbt.getFloat("Mass");
        damage = nbt.getFloat("Damage");
    }

    @Override
    protected @NonNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

}