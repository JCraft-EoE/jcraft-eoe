package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class WinterStormEntity extends JAttackEntity {

    public static final int LIFETIME = 600;
    private static final int STORM_RADIUS = 64;

    public WinterStormEntity(Level world) {
        super(JEntityTypeRegistry.WINTER_STORM.get(), world);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            spawnStormParticles();
            return;
        }

        if (tickCount == 1) {
            freezeWater();
            if (level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.setWeatherParameters(0, 20 * 120, true, false);
            }
        }

        if (tickCount > LIFETIME || master == null) {
            discard();
            return;
        }

        if (tickCount % 20 == 0) {
            applySlowness();
        }

        if (tickCount % 40 == 0) {
            level().playSound(null, blockPosition(), SoundEvents.WEATHER_RAIN, net.minecraft.sounds.SoundSource.AMBIENT, 1.5f, 0.5f);
        }
    }

    private void spawnStormParticles() {
        if (tickCount % 2 != 0) return;
        for (int i = 0; i < 3; i++) {
            final double angle = random.nextDouble() * Math.PI * 2;
            final double dist = random.nextDouble() * STORM_RADIUS;
            final double ry = random.nextDouble() * 18;
            final double drift = (random.nextDouble() - 0.5) * 0.5;
            level().addParticle(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE,
                    getX() + Math.cos(angle) * dist, getY() + ry, getZ() + Math.sin(angle) * dist,
                    drift, -0.15, drift);
        }
        final double angle = random.nextDouble() * Math.PI * 2;
        final double dist = random.nextDouble() * STORM_RADIUS * 0.7;
        level().addParticle(net.minecraft.core.particles.ParticleTypes.CLOUD,
                getX() + Math.cos(angle) * dist,
                getY() + 6 + random.nextDouble() * 6,
                getZ() + Math.sin(angle) * dist,
                0, 0, 0);
    }

    private void applySlowness() {
        final AABB stormBox = AABB.ofSize(position(), STORM_RADIUS * 2, 32, STORM_RADIUS * 2);
        level().getEntitiesOfClass(LivingEntity.class, stormBox,
                EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != master)
        ).forEach(e -> e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, true, false)));
    }

    private void freezeWater() {
        final int r = 32;
        final BlockPos center = blockPosition();
        for (int x = -r; x <= r; x += 2) {
            for (int z = -r; z <= r; z += 2) {
                for (int y = -4; y <= 8; y++) {
                    final BlockPos pos = center.offset(x, y, z);
                    if (level().getFluidState(pos).getType() == Fluids.WATER
                            && level().getBlockState(pos).getBlock() == Blocks.WATER) {
                        level().setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
                    }
                }
            }
        }
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
        return SoundEvents.WEATHER_RAIN;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WEATHER_RAIN;
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
