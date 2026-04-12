package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

import static net.arna.jcraft.api.Attacks.damageLogic;

public class LargeIcicleProjectile extends AbstractArrow {
    public static final BlockParticleOption ICE_PARTICLE = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState());
    private int ticksInAir;
    private LivingEntity livingOwner;
    private boolean projectile = false;
    private boolean instant = false;
    private boolean lockVelocity = false;
    public void lock() {
        lockVelocity = true;
    }

    private static final EntityDataAccessor<Float> SCALE;
    private static final EntityDataAccessor<Boolean> IS_INSTANT;
    static {
        SCALE = SynchedEntityData.defineId(LargeIcicleProjectile.class, EntityDataSerializers.FLOAT);
        IS_INSTANT = SynchedEntityData.defineId(LargeIcicleProjectile.class, EntityDataSerializers.BOOLEAN);
    }

    public LargeIcicleProjectile(Level world) {
        super(JEntityTypeRegistry.LARGE_ICICLE.get(), world);
    }

    public LargeIcicleProjectile(Level world, @NonNull LivingEntity owner) {
        super(JEntityTypeRegistry.LARGE_ICICLE.get(), owner, world);
        // setNoGravity(true);
        setNoPhysics(true);
        setOwner(owner);
        this.pickup = Pickup.DISALLOWED;
        livingOwner = owner;
        setSoundEvent(SoundEvents.GLASS_BREAK);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SCALE, 1.0f);
        entityData.define(IS_INSTANT, false);
    }

    public void setScale(float scale) {
        entityData.set(SCALE, scale);
    }
    public float getScale() {
        return entityData.get(SCALE);
    }

    public void setInstant(boolean instant) {
        this.instant = instant;
        entityData.set(IS_INSTANT, instant);
    }

    public boolean isInstant() {
        return entityData.get(IS_INSTANT);
    }

    public void markProjectile() {
        this.projectile = true;
    }

    @Override
    public void setDeltaMovement(@NonNull Vec3 deltaMovement) {
        if (lockVelocity) return;
        super.setDeltaMovement(deltaMovement);
    }

    @Override
    public void tick() {
        super.tick();
        if (livingOwner == null) {
            if (getOwner() instanceof LivingEntity living) {
                livingOwner = living;
            } else {
                discard();
                return;
            }
        }

        ticksInAir++;

        if (level().isClientSide) {
            if (ticksInAir == 1) {
                final double x = getX();
                final double y = getY();
                final double z = getZ();
                final Vec3 velocity = getDeltaMovement().normalize();

                for (int i = 0; i < 24; i++) {
                    level().addParticle(random.nextBoolean() ? ICE_PARTICLE : ParticleTypes.SNOWFLAKE, x, y, z,
                            (velocity.x + random.nextGaussian()) * 0.25,
                            (velocity.y + random.nextGaussian()) * 0.25,
                            (velocity.z + random.nextGaussian()) * 0.25
                    );
                }
            }
            return;
        }

        if (projectile) {
            final Vec3 velocity = getDeltaMovement();
            final BlockPos blockPos = blockPosition();
            final BlockPos forward = blockPosition().offset((int)velocity.x, (int)velocity.y, (int)velocity.z);
            if (ticksInAir > 30 || level().getBlockState(blockPos).canOcclude() || level().getBlockState(forward).canOcclude()) {
                detonate();
            } else {
                final Vec3 gravity = Vec3.atLowerCornerOf(GravityChangerAPI.getGravityDirection(this).getNormal());
                this.lockVelocity = false;
                setDeltaMovement(velocity.scale(0.99));
                push(gravity.x * 9.81 / 400, gravity.y * 9.81 / 400, gravity.z * 9.81 / 400);
                this.lockVelocity = true;

                final Vec3 pos = position();
                final Vec3 direction = velocity.normalize();
                Set<LivingEntity> hurt = JUtils.generateHitbox(level(), pos.add(direction), 1.75, e -> true);
                boolean hit = false;
                for (LivingEntity living : hurt) {
                    if (cantAttack(living)) continue;
                    hit = !JUtils.isBlocking(living);

                    LivingEntity target = JUtils.getUserIfStand(living);

                    final Vec3 kbVec = direction.scale(0.75);

                    int stun = 15;

                    damageLogic(level(), target, kbVec,
                            stun, 3, false, 3f, true,
                            4, level().damageSources().mobAttack(livingOwner), livingOwner,
                            CommonHitPropertyComponent.HitAnimation.CRUSH, false, false);
                }
                if (hit) {
                    final Vec3 frontPos = pos.add(direction);
                    JCraft.createParticle((ServerLevel) level(),
                            frontPos.x + random.nextGaussian() * 0.25,
                            frontPos.y + random.nextGaussian() * 0.25,
                            frontPos.z + random.nextGaussian() * 0.25,
                            JParticleType.HIT_SPARK_2);
                }
            }
        } else if (instant) {
            if (ticksInAir == 1) {
                attack();
            } else if (ticksInAir > 10) {
                discard();
            }
        } else {
            if (ticksInAir < 10) {
                setDeltaMovement(getDeltaMovement().scale(0.9));
            } else if (ticksInAir == 10) {
                attack();
            } else if (ticksInAir > 50) {
                discard();
            }
        }
    }

    private void attack() {
        float scale = entityData.get(SCALE);
        boolean perfect = scale == 1 && instant; // Fully charged instant largecicle

        final Vec3 pos = position();
        final Vec3 direction = getDeltaMovement().normalize();
        Set<LivingEntity> hurt = JUtils.generateHitbox(level(), pos.add(direction.scale(1.25 * scale)), 1.75 * scale, e -> true);
        hurt.addAll(JUtils.generateHitbox(level(), pos.add(direction.scale(2.25 * scale)), 1.25 * scale, e -> true));
        boolean hit = false;
        for (LivingEntity living : hurt) {
            if (cantAttack(living)) continue;
            hit = !JUtils.isBlocking(living);

            LivingEntity target = JUtils.getUserIfStand(living);

            final Vec3 kbVec = direction.scale(0.75 * scale + (perfect ? 1 : 0));

            int stun = (int) (15 * scale);
            if (instant) stun += 9;

            damageLogic(level(), target, kbVec,
                    stun, 3, false, 7f * scale + (perfect ? 3f : 0), true,
                    (int) (13.0f * scale), level().damageSources().mobAttack(livingOwner), livingOwner,
                    CommonHitPropertyComponent.HitAnimation.CRUSH, false, perfect);
        }
        if (hit) {
            final Vec3 frontPos = pos.add(direction.scale(2.5));
            JCraft.createParticle((ServerLevel) level(),
                    frontPos.x + random.nextGaussian() * 0.25 * scale,
                    frontPos.y + random.nextGaussian() * 0.25 * scale,
                    frontPos.z + random.nextGaussian() * 0.25 * scale,
                    JParticleType.HIT_SPARK_2);
            if (perfect) {
                JComponentPlatformUtils.getShockwaveHandler(level()).addShockwave(frontPos, direction, 2.0f);
            }
        }
        playSound(SoundEvents.TRIDENT_THROW, 1, 1);
    }

    public void detonate() {
        final Vec3 direction = getDeltaMovement().normalize();
        double x = getX();
        double y = getY();
        double z = getZ();
        final float pitch = -getXRot();
        final float yaw = -getYRot() + 180;
        for (int set = 0; set < 3; set++) {
            for (int i = 0; i < 3; i++) {
                IcicleProjectile icicle = new IcicleProjectile(level(), livingOwner);
                float yawOffset = 10f * (i - 1);
                float pitchOffset = (i == 1) ? 10f : -10f;
                if (set % 2 == 0) pitchOffset = -pitchOffset;
                icicle.moveTo(x, y, z, yaw, pitch);
                JUtils.shoot(
                        icicle,
                        null,
                        (pitch + pitchOffset),
                        (yaw + yawOffset),
                        0,
                        0.5F + 0.5F / (set + 1), 0.0F
                );
                level().addFreshEntity(icicle);
            }
            x += direction.x;
            y += direction.y;
            z += direction.z;
        }

        kill();
        level().playSound(null, x, y, z, SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1, 0.8f);
    }

    @Override
    public void onClientRemoval() {
        super.onClientRemoval();
        if (entityData.get(IS_INSTANT)) return;
        final double x = getX();
        final double y = getY();
        final double z = getZ();
        final Vec3 velocity = getDeltaMovement().normalize();

        for (int i = 0; i < 32; i++) {
            level().addParticle(random.nextBoolean() ? ICE_PARTICLE : ParticleTypes.SNOWFLAKE, x, y, z,
                    (velocity.x + random.nextGaussian()) * 0.5,
                    (velocity.y + random.nextGaussian()) * 0.5,
                    (velocity.z + random.nextGaussian()) * 0.5
            );
        }
    }

    @Override
    public @NonNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    private boolean cantAttack(LivingEntity living) {
        if (living == livingOwner)
            return true;
        return livingOwner != null && JComponentPlatformUtils.getStandComponent(livingOwner).getStand() == living;
    }

    @Override
    protected void onHit(@NonNull HitResult hitResult) {
        if (level().isClientSide() || !projectile) return;
        detonate();
    }

    @Override
    protected float getWaterInertia() { // Not actually drag, just a multiplier
        return 1.0F;
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putShort("life", (short) this.ticksInAir);
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.ticksInAir = tag.getShort("life");
    }

    public static final AzCommand FIRE = AzCommand.create(JCraft.BASE_CONTROLLER, "animation.large_icicle.spawn", AzPlayBehaviors.HOLD_ON_LAST_FRAME);
    public static final AzCommand FIRE_INSTANT = AzCommand.create(JCraft.BASE_CONTROLLER, "animation.large_icicle.spawn_instant", AzPlayBehaviors.HOLD_ON_LAST_FRAME);

    // Animations
    /*
    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private static final RawAnimation FIRE = RawAnimation.begin().thenPlayAndHold("animation.large_icicle.spawn");
    private static final RawAnimation FIRE_INSTANT = RawAnimation.begin().thenPlayAndHold("animation.large_icicle.spawn_instant");
    private PlayState predicate(AnimationState<LargeIcicleProjectile> state) {
        return state.setAndContinue(entityData.get(IS_INSTANT) ? FIRE_INSTANT : FIRE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }*/
}
