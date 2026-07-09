package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import static net.arna.jcraft.api.Attacks.damageLogic;

public class IceBranchProjectile extends AbstractArrow {
    private static final int MAX_CHAIN_LENGTH = 10;
    private final int chainIndex;

    private LivingEntity livingOwner;

    private boolean grown = false;
    private boolean lockRotation = false;
    private boolean lockVelocity = true;

    public IceBranchProjectile(Level level) {
        super(JEntityTypeRegistry.ICE_BRANCH.get(), level);
        chainIndex = 0;
    }
    public IceBranchProjectile(Level level, LivingEntity owner, int chainIndex) {
        super(JEntityTypeRegistry.ICE_BRANCH.get(), level);
        setOwner(owner);
        livingOwner = owner;
        setNoGravity(true);
        setNoPhysics(true);
        this.chainIndex = chainIndex;
        this.pickup = Pickup.DISALLOWED;
    }

    @Override
    public void setXRot(float xRot) {
        if (lockRotation) return;
        super.setXRot(xRot);
    }

    @Override
    public void setYRot(float yRot) {
        if (lockRotation) return;
        super.setYRot(yRot);
    }

    @Override
    public void setDeltaMovement(@NonNull Vec3 deltaMovement) {
        if (lockVelocity) return;
        super.setDeltaMovement(deltaMovement);
    }

    private final Comparator<Entity> distanceComparator = (entity1, entity2) -> {
        double distance1 = this.distanceToSqr(entity1);
        double distance2 = this.distanceToSqr(entity2);
        return Double.compare(distance1, distance2);
    };

    public static final double LENGTH = 1;
    public static final int DEATH_TICK = 100;
    @Override
    public void tick() {
        lockRotation = true;
        super.tick();
        lockRotation = false;

        if (level().isClientSide()) {
            if (tickCount == 1) {
                final Vec3 rotVec = calculateViewVector(getXRot(), -getYRot());
                for (int i = 0; i < 6; i++) {
                    level().addParticle(random.nextBoolean() ? LargeIcicleProjectile.ICE_PARTICLE : ParticleTypes.SNOWFLAKE,
                            getX(), getY(), getZ(),
                            (random.nextGaussian() - rotVec.x) * 0.1,
                            (random.nextGaussian() - rotVec.y) * 0.1,
                            (random.nextGaussian() - rotVec.z) * 0.1
                    );
                }
            } else if (tickCount == DEATH_TICK) {
                for (int i = 0; i < 6; i++) {
                    level().addParticle(LargeIcicleProjectile.ICE_PARTICLE,
                            getX(), getY(), getZ(),
                            random.nextGaussian(),
                            random.nextGaussian(),
                            random.nextGaussian()
                    );
                }
            } else if (random.nextFloat() < 0.1f) {
                level().addParticle(random.nextBoolean() ? ParticleTypes.SPIT : ParticleTypes.SNOWFLAKE,
                        getX() + random.nextGaussian() * 0.25,
                        getY() + random.nextGaussian() * 0.25,
                        getZ() + random.nextGaussian() * 0.25,
                        0, 0, 0
                );
            }
            return;
        }
        if (livingOwner == null) {
            discard();
            return;
        }
        if (tickCount == 1) {
            final Vec3 rotVec = calculateViewVector(getXRot(), -getYRot()); // No-clipping projectiles have inverted yaw when rendering
            final Vec3 pos = position();
            final Level level = level();
            final Set<LivingEntity> hurt = JUtils.generateHitbox(level, pos.add(rotVec.scale(-0.25)), 1.25, e -> true);
            boolean hit = false;
            for (LivingEntity living : hurt) {
                if (!canAttack(living)) continue;
                hit = !JUtils.isBlocking(living);

                LivingEntity target = JUtils.getUserIfStand(living);

                damageLogic(level, target, Vec3.ZERO,
                        30 - 10 * chainIndex / MAX_CHAIN_LENGTH, 0, false, 4f, true,
                        10, level.damageSources().mobAttack(livingOwner), livingOwner,
                        CommonHitPropertyComponent.HitAnimation.MID);
            }
            if (hit) {
                final Vec3 frontPos = pos.add(rotVec.scale(-0.5));
                playSound(SoundEvents.PLAYER_HURT_FREEZE, 1, 1);

                final ServerLevel serverWorld = (ServerLevel) level;
                JCraft.createParticle(serverWorld,
                        frontPos.x + random.nextGaussian() * 0.25,
                        frontPos.y + random.nextGaussian() * 0.25,
                        frontPos.z + random.nextGaussian() * 0.25,
                        JParticleType.HIT_SPARK_1);

                JUtils.around(serverWorld, frontPos, 128).forEach(
                        serverPlayer -> (serverWorld).sendParticles(serverPlayer,
                                LargeIcicleProjectile.ICE_PARTICLE, false, frontPos.x - 0.25, frontPos.y - 0.25, frontPos.z - 0.25,
                                10, 0.5, 0.5, 0.5, 0.1)
                );

                grown = true; // Stop growth
            }
        } else if (chainIndex < MAX_CHAIN_LENGTH && !grown && tickCount == 10) {
            final ServerLevel serverWorld = (ServerLevel) level();
            final Vec3 rotVec = calculateViewVector(getXRot(), -getYRot()); // No-clipping projectiles have inverted yaw when rendering
            final Vec3 initialPos = position().add(rotVec.scale(-LENGTH));

            final Optional<LivingEntity> target = serverWorld.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(32.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR)
                    .stream()
                    .filter(livingEntity -> livingEntity != livingOwner && !livingEntity.isPassengerOfSameVehicle(livingOwner))
                    .min(distanceComparator);

            boolean shouldSplit = chainIndex % 4 == 2;
            float inaccuracy = shouldSplit ? 25.0f : 0.0f;
            for (int i = 0; i < (shouldSplit ? 2 : 1); i++) {
                IceBranchProjectile next = new IceBranchProjectile(level(), livingOwner, chainIndex + 1);

                if (target.isPresent()) {
                    final LivingEntity nearestTarget = target.get();

                    float currentXRot = getXRot();
                    float currentYRot = getYRot();

                    Vec3 toTargetVec = nearestTarget.position()
                            .subtract(position());
                    double dx = toTargetVec.x;
                    double dy = toTargetVec.y;
                    double dz = toTargetVec.z;

                    double flatDist = Math.sqrt(dx * dx + dz * dz);
                    float targetXRot = (float) (Mth.atan2(dy, flatDist) * (180F / Math.PI));
                    float targetYRot = (float) -(Mth.atan2(dz, dx) * (180F / Math.PI)) - 90.0F;

                    float clampedXRot = Mth.approachDegrees(currentXRot, targetXRot, 25.0F) + (float) random.nextGaussian() * inaccuracy;
                    float clampedYRot = -Mth.approachDegrees(currentYRot, targetYRot, 25.0F) + (float) random.nextGaussian() * inaccuracy;

                    Vec3 toTarget = calculateViewVector(clampedXRot, clampedYRot).normalize();

                    next.setXRot(clampedXRot);
                    next.setYRot(-clampedYRot);

                    next.setPos(initialPos.add(toTarget.scale(-LENGTH)));
                } else {
                    float xRot = getXRot() + random.nextFloat() * 60.0F - 30.0F;
                    float yRot = getYRot() + random.nextFloat() * 60.0F - 30.0F;

                    Vec3 newRotVec = calculateViewVector(xRot, yRot).normalize();

                    next.setXRot(xRot);
                    next.setYRot(-yRot);

                    next.setPos(initialPos.add(newRotVec.scale(-LENGTH)));
                }

                next.xRotO = next.getXRot();
                next.yRotO = next.getYRot();
                level().addFreshEntity(next);
            }
            grown = true;
        } else if (tickCount == DEATH_TICK - 10) {
            lockVelocity = false;
            setNoGravity(false);
            setNoPhysics(false);
        } else if (tickCount == DEATH_TICK) {
            discard();
        }
    }

    private boolean canAttack(LivingEntity living) {
        if (living == livingOwner)
            return false;
        if (livingOwner != null && JComponentPlatformUtils.getStandComponent(livingOwner).getStand() == living)
            return false;
        return true;
    }

    @Override
    protected void onHit(@NonNull HitResult result) { }

    @Override
    protected @NonNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    public static final AzCommand SPAWN = AzCommand.create(JCraft.BASE_CONTROLLER, "animation.ice_branch.spawn", AzPlayBehaviors.HOLD_ON_LAST_FRAME);

    // Animations
    /*
    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private static final RawAnimation FIRE = RawAnimation.begin().thenPlayAndHold("animation.ice_branch.spawn");
    private PlayState predicate(AnimationState<IceBranchProjectile> state) {
        return state.setAndContinue(FIRE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }*/
}
