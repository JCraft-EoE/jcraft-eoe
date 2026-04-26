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

import static net.arna.jcraft.api.Attacks.damageLogic;

public class FiredIcicleProjectile extends AbstractArrow {
    public static final BlockParticleOption ICE_PARTICLE = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState());

    private static final EntityDataAccessor<Float> SCALE =
            SynchedEntityData.defineId(FiredIcicleProjectile.class, EntityDataSerializers.FLOAT);

    private LivingEntity livingOwner;
    private boolean lockVelocity = false;
    private int ticksAlive = 0;

    public FiredIcicleProjectile(Level world) {
        super(JEntityTypeRegistry.FIRED_ICICLE.get(), world);
    }

    public FiredIcicleProjectile(Level world, @NonNull LivingEntity owner) {
        super(JEntityTypeRegistry.FIRED_ICICLE.get(), owner, world);
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
    }

    public void setScale(float scale) { entityData.set(SCALE, scale); }
    public float getScale() { return entityData.get(SCALE); }

    public void fire(@NonNull Vec3 velocity) {
        setDeltaMovement(velocity);
        lockVelocity = true;
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

        if (ticksAlive == 0) {
            yRotO = getYRot();
            xRotO = getXRot();
        }
        ticksAlive++;

        if (level().isClientSide) {
            if (ticksAlive % 2 == 0) {
                final Vec3 vel = getDeltaMovement();
                for (int i = 0; i < 3; i++) {
                    level().addParticle(random.nextBoolean() ? ICE_PARTICLE : ParticleTypes.SNOWFLAKE,
                            getX() + random.nextGaussian() * 0.2,
                            getY() + random.nextGaussian() * 0.2,
                            getZ() + random.nextGaussian() * 0.2,
                            -vel.x * 0.15 + random.nextGaussian() * 0.05,
                            -vel.y * 0.15 + random.nextGaussian() * 0.05,
                            -vel.z * 0.15 + random.nextGaussian() * 0.05);
                }
            }
            return;
        }

        if (ticksAlive > 40 || level().getBlockState(blockPosition()).canOcclude()) {
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1, 0.8f);
            discard();
            return;
        }

        final Vec3 direction = getDeltaMovement().normalize();
        final float scale = getScale();
        final Vec3 pos = position();

        boolean hit = false;
        for (LivingEntity living : JUtils.generateHitbox(level(), pos.add(direction), 1.75 * scale, e -> true)) {
            if (cantAttack(living)) continue;
            hit = !JUtils.isBlocking(living);
            damageLogic(level(), JUtils.getUserIfStand(living), direction.scale(0.75 * scale),
                    15, 3, false, 3f * scale, true,
                    4, level().damageSources().mobAttack(livingOwner), livingOwner,
                    CommonHitPropertyComponent.HitAnimation.CRUSH, false, false);
        }
        if (hit) {
            JCraft.createParticle((ServerLevel) level(),
                    pos.x + direction.x * 1.5 + random.nextGaussian() * 0.25 * scale,
                    pos.y + direction.y * 1.5 + random.nextGaussian() * 0.25 * scale,
                    pos.z + direction.z * 1.5 + random.nextGaussian() * 0.25 * scale,
                    JParticleType.HIT_SPARK_2);
        }
    }

    @Override
    public void onClientRemoval() {
        super.onClientRemoval();
        final double x = getX(), y = getY(), z = getZ();
        final Vec3 velocity = getDeltaMovement().normalize();
        for (int i = 0; i < 24; i++) {
            level().addParticle(random.nextBoolean() ? ICE_PARTICLE : ParticleTypes.SNOWFLAKE, x, y, z,
                    (velocity.x + random.nextGaussian()) * 0.5,
                    (velocity.y + random.nextGaussian()) * 0.5,
                    (velocity.z + random.nextGaussian()) * 0.5);
        }
    }

    @Override
    public @NonNull ItemStack getPickupItem() { return ItemStack.EMPTY; }

    @Override
    protected void onHit(@NonNull HitResult hitResult) {}

    @Override
    protected float getWaterInertia() { return 1.0f; }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putShort("life", (short) ticksAlive);
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ticksAlive = tag.getShort("life");
    }

    private boolean cantAttack(LivingEntity living) {
        if (living == livingOwner) return true;
        return livingOwner != null && JComponentPlatformUtils.getStandComponent(livingOwner).getStand() == living;
    }

    public static final AzCommand FIRE = AzCommand.create(JCraft.BASE_CONTROLLER, "animation.large_icicle.spawn", AzPlayBehaviors.HOLD_ON_LAST_FRAME);
}
