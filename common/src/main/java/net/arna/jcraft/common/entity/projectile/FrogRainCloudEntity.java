package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.common.entity.BluePoisonFrogEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class FrogRainCloudEntity extends JAttackEntity {

    public static final int LIFETIME = 500;

    public FrogRainCloudEntity(Level world) {
        super(JEntityTypeRegistry.FROG_RAIN_CLOUD.get(), world);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            spawnCloudParticles();
            return;
        }

        if (tickCount > LIFETIME || master == null) {
            discard();
            return;
        }

        if (tickCount % 20 == 0) {
            spawnFrog();
        }
    }

    private void spawnCloudParticles() {
        if (tickCount % 2 != 0) return;
        for (int i = 0; i < 3; i++) {
            final double angle = random.nextDouble() * Math.PI * 2;
            final double radius = 1.0 + random.nextDouble() * 4.0;
            level().addParticle(ParticleTypes.CLOUD,
                    getX() + Math.cos(angle) * radius,
                    getY() + random.nextGaussian() * 0.8,
                    getZ() + Math.sin(angle) * radius,
                    Math.cos(angle + Math.PI / 2) * 0.04, 0.01, Math.sin(angle + Math.PI / 2) * 0.04);
        }
        if (random.nextInt(5) == 0) {
            level().addParticle(ParticleTypes.DRIPPING_WATER,
                    getX() + (random.nextDouble() - 0.5) * 6,
                    getY() - random.nextDouble() * 2,
                    getZ() + (random.nextDouble() - 0.5) * 6,
                    0, -0.2, 0);
        }
    }

    private void spawnFrog() {
        final double angle = random.nextDouble() * Math.PI * 2;
        final double radius = random.nextDouble() * 5.0;
        final BluePoisonFrogEntity frog = new BluePoisonFrogEntity(JEntityTypeRegistry.BLUE_POISON_FROG.get(), level());
        frog.setMaster(master);
        frog.setPos(
                getX() + Math.cos(angle) * radius,
                getY() - 0.5,
                getZ() + Math.sin(angle) * radius);
        frog.setDeltaMovement(Math.cos(angle) * 0.15, -0.3, Math.sin(angle) * 0.15);
        level().addFreshEntity(frog);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return !source.is(DamageTypes.FELL_OUT_OF_WORLD) && !source.is(DamageTypes.GENERIC_KILL);
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void doPush(@NonNull Entity entity) {}

    @Override
    public void push(@NonNull Entity entity) {}

    @Override
    public boolean canCollideWith(@NonNull Entity other) {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean shouldShowName() {
        return false;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NonNull DamageSource source) {
        return SoundEvents.FROG_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.FROG_DEATH;
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        writeMasterNbt(tag);
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        readMasterNbt(tag);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.KNOCKBACK_RESISTANCE)
                .add(Attributes.MOVEMENT_SPEED)
                .add(Attributes.ARMOR)
                .add(Attributes.ARMOR_TOUGHNESS);
    }
}
