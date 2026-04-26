package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class StormCloudEntity extends JAttackEntity {

    public static final int LIFETIME = 500;
    private static final int WANDER_RADIUS = 8;

    private Vec3 origin;

    public StormCloudEntity(Level world) {
        super(JEntityTypeRegistry.STORM_CLOUD.get(), world);
    }

    @Override
    public void tick() {
        super.tick();

        if (origin == null) {
            origin = position();
        }

        if (level().isClientSide) {
            spawnCloudParticles();
            return;
        }

        if (tickCount > LIFETIME || master == null) {
            discard();
            return;
        }

        if (tickCount % 10 == 0) {
            wander();
        }

        if (tickCount % 30 == 0) {
            strikeLightning();
        }
    }

    private void spawnCloudParticles() {
        if (tickCount % 2 != 0) return;
        for (int i = 0; i < 2; i++) {
            final double angle = random.nextDouble() * Math.PI * 2;
            final double radius = 0.5 + random.nextDouble() * 3.0;
            level().addParticle(ParticleTypes.CLOUD,
                    getX() + Math.cos(angle) * radius,
                    getY() + random.nextGaussian() * 0.5,
                    getZ() + Math.sin(angle) * radius,
                    0, -0.01, 0);
        }
        if (random.nextInt(8) == 0) {
            level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    getX() + (random.nextDouble() - 0.5) * 3,
                    getY() - random.nextDouble() * 0.6,
                    getZ() + (random.nextDouble() - 0.5) * 3,
                    0, -0.05, 0);
        }
    }

    private void wander() {
        final double dx = (random.nextDouble() - 0.5) * 2.0;
        final double dz = (random.nextDouble() - 0.5) * 2.0;
        final Vec3 next = position().add(dx, 0, dz);

        if (Math.abs(next.x - origin.x) <= WANDER_RADIUS && Math.abs(next.z - origin.z) <= WANDER_RADIUS) {
            setPos(next.x, getY(), next.z);
        }
    }

    private void strikeLightning() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        final Vec3 pos = position().add(0, -1, 0);
        final LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
        bolt.setPos(pos);
        serverLevel.addFreshEntity(bolt);

        final AABB strikeBox = AABB.ofSize(pos, 3, 4, 3);
        serverLevel.getEntitiesOfClass(LivingEntity.class, strikeBox,
                EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != master)
        ).forEach(e -> e.hurt(serverLevel.damageSources().lightningBolt(), 4f));
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
        return SoundEvents.LIGHTNING_BOLT_THUNDER;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.LIGHTNING_BOLT_THUNDER;
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
