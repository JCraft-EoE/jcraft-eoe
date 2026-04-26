package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class CloudPuffEntity extends JAttackEntity {

    private int lifetime = 30;
    private float drift = 0.04f;
    private boolean followFeet = false;

    public CloudPuffEntity(Level world) {
        super(JEntityTypeRegistry.CLOUD_PUFF.get(), world);
    }

    public static CloudPuffEntity footCloud(Level world, LivingEntity master) {
        final CloudPuffEntity e = new CloudPuffEntity(world);
        e.setMaster(master);
        e.followFeet = true;
        e.lifetime = 18;
        e.drift = 0f;
        return e;
    }

    public static CloudPuffEntity idlePuff(Level world, LivingEntity master) {
        final CloudPuffEntity e = new CloudPuffEntity(world);
        e.setMaster(master);
        e.followFeet = false;
        e.lifetime = 50;
        e.drift = 0.05f;
        return e;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            spawnParticles();
            return;
        }

        if (tickCount > lifetime || master == null) {
            discard();
            return;
        }

        if (followFeet) {
            setPos(master.getX(), master.getY() + 0.1, master.getZ());
        } else {
            setPos(getX(), getY() + drift, getZ());
        }
    }

    private void spawnParticles() {
        final float alpha = 1f - (float) tickCount / lifetime;
        final int count = followFeet ? 4 : 2;
        for (int i = 0; i < count; i++) {
            level().addParticle(ParticleTypes.CLOUD,
                    getX() + random.nextGaussian() * (followFeet ? 0.8 : 0.4),
                    getY() + random.nextDouble() * 0.3,
                    getZ() + random.nextGaussian() * (followFeet ? 0.8 : 0.4),
                    0, 0.01 * alpha, 0);
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return !source.is(DamageTypes.FELL_OUT_OF_WORLD) && !source.is(DamageTypes.GENERIC_KILL);
    }

    @Override public boolean isNoGravity() { return true; }
    @Override protected void doPush(@NonNull Entity entity) {}
    @Override public void push(@NonNull Entity entity) {}
    @Override public boolean canCollideWith(@NonNull Entity other) { return false; }
    @Override public boolean shouldShowName() { return false; }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        writeMasterNbt(tag);
        tag.putInt("Lifetime", lifetime);
        tag.putFloat("Drift", drift);
        tag.putBoolean("FollowFeet", followFeet);
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        readMasterNbt(tag);
        lifetime = tag.getInt("Lifetime");
        drift = tag.getFloat("Drift");
        followFeet = tag.getBoolean("FollowFeet");
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1)
                .add(Attributes.KNOCKBACK_RESISTANCE)
                .add(Attributes.MOVEMENT_SPEED)
                .add(Attributes.ARMOR)
                .add(Attributes.ARMOR_TOUGHNESS);
    }
}
