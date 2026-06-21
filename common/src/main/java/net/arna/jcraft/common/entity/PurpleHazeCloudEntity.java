package net.arna.jcraft.common.entity;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.common.entity.stand.AbstractPurpleHazeEntity;
import net.arna.jcraft.common.entity.stand.AbstractPurpleHazeEntity.PoisonType;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import java.util.Stack;

public class PurpleHazeCloudEntity extends Entity {
    public static int MAX_AGE = 100;
    private static final EntityDataAccessor<Float> RADIUS;
    private static final EntityDataAccessor<Integer> POISON_TYPE;

    @Getter @Setter
    @Nullable private LivingEntity owner;

    static {
        RADIUS = SynchedEntityData.defineId(PurpleHazeCloudEntity.class, EntityDataSerializers.FLOAT);
        POISON_TYPE = SynchedEntityData.defineId(PurpleHazeCloudEntity.class, EntityDataSerializers.INT);
    }

    public PurpleHazeCloudEntity(Level world, float radius, PoisonType poisonType) {
        this(world);
        setRadius(radius);
        if (poisonType != null) {
            entityData.set(POISON_TYPE, poisonType.ordinal());
        }
    }

    public float getRadius() {
        return entityData.get(RADIUS);
    }

    public PoisonType getPoisonType() {
        return PoisonType.values()[entityData.get(POISON_TYPE)];
    }

    public void setRadius(float radius) {
        entityData.set(RADIUS, radius);
    }

    public PurpleHazeCloudEntity(Level world) {
        super(JEntityTypeRegistry.PURPLE_HAZE_CLOUD.get(), world);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(RADIUS, 1.0f);
        entityData.define(POISON_TYPE, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        tickCount = nbt.getInt("Age");
        setRadius(nbt.getFloat("Radius"));
        entityData.set(POISON_TYPE, nbt.getInt("PoisonType"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("Age", tickCount);
        nbt.putFloat("Radius", getRadius());
        nbt.putInt("PoisonType", entityData.get(POISON_TYPE));
    }

    @Override
    public void tick() {
        super.tick();

        final float radius = getRadius();
        final PoisonType poisonType = getPoisonType();

        if (level().isClientSide()) {
            double x = getX(), y = getY(), z = getZ();
            for (int i = 0; i < radius; i++) {
                level().addParticle(
                        JParticleTypeRegistry.PURPLE_HAZE_CLOUD.get(), false,
                        x + random.nextGaussian() * radius / 2,
                        y + random.nextGaussian() * radius / 2,
                        z + random.nextGaussian() * radius / 2,
                        0, 0, 0
                );

                level().addParticle(
                        switch (poisonType) {
                            case HARMING -> JParticleTypeRegistry.PURPLE_HAZE_PARTICLE.get();
                            case NULLIFYING -> ParticleTypes.POOF;
                            case DEBILITATING -> ParticleTypes.SQUID_INK;
                        },
                        false,
                        x + random.nextGaussian() * radius / 2,
                        y + random.nextGaussian() * radius / 2,
                        z + random.nextGaussian() * radius / 2,
                        0, 0, 0
                );
            }
        } else {
            // -0.5 radius per second
            setRadius(radius - 0.025f);

            if (getRadius() <= 0 || tickCount >= MAX_AGE) {
                discard();
                return;
            }

            level().getEntities(this, getBoundingBox(),
                    EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(EntitySelector.ENTITY_STILL_ALIVE)).forEach(
                    entity -> {
                        if (entity instanceof LivingEntity living) {
                            switch (poisonType) {
                                case HARMING -> AbstractPurpleHazeEntity.infect(living, 4, owner);
                                case DEBILITATING -> {
                                    AbstractPurpleHazeEntity.infect(living, 3, MobEffects.BLINDNESS);
                                    AbstractPurpleHazeEntity.infect(living, 3, MobEffects.MOVEMENT_SLOWDOWN);
                                    AbstractPurpleHazeEntity.infect(living, 3, MobEffects.WEAKNESS);
                                }
                                case NULLIFYING -> {
                                    final Stack<MobEffect> toRemove = new Stack<>();

                                    living.getActiveEffects().forEach(
                                            statusEffectInstance -> {
                                                final MobEffect effectType = statusEffectInstance.getEffect();
                                                if (effectType != JStatusRegistry.DAZED.get() && effectType != JStatusRegistry.KNOCKDOWN.get()) {
                                                    toRemove.add(effectType);
                                                }
                                            }
                                    );

                                    toRemove.forEach(living::removeEffect);
                                }
                            }
                        }
                    }
            );
        }
    }

    @Override
    public void onSyncedDataUpdated(@NonNull EntityDataAccessor<?> data) {
        if (RADIUS.equals(data)) {
            this.refreshDimensions();
        }

        super.onSyncedDataUpdated(data);
    }

    @Override
    protected @NonNull AABB makeBoundingBox() {
        final float radius = getRadius();
        final double x = getX(), y = getY(), z = getZ();
        return new AABB(
                x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius
        );
    }
}
