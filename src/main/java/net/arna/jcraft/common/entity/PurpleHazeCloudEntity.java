package net.arna.jcraft.common.entity;

import net.arna.jcraft.common.entity.stand.AbstractPurpleHazeEntity;
import net.arna.jcraft.common.entity.stand.AbstractPurpleHazeEntity.PoisonType;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.arna.jcraft.registry.JParticleTypeRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.Stack;

public class PurpleHazeCloudEntity extends Entity {
    public static int MAX_AGE = 100;
    private static final TrackedData<Float> RADIUS;
    private static final TrackedData<Integer> POISON_TYPE;

    static {
        RADIUS = DataTracker.registerData(PurpleHazeCloudEntity.class, TrackedDataHandlerRegistry.FLOAT);
        POISON_TYPE = DataTracker.registerData(PurpleHazeCloudEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    public PurpleHazeCloudEntity(World world, float radius, PoisonType poisonType) {
        this(world);
        setRadius(radius);
        if (poisonType != null)
            dataTracker.set(POISON_TYPE, poisonType.ordinal());
    }

    public float getRadius() {
        return dataTracker.get(RADIUS);
    }

    public PoisonType getPoisonType() {
        return PoisonType.values()[dataTracker.get(POISON_TYPE)];
    }

    public void setRadius(float radius) {
        dataTracker.set(RADIUS, radius);
    }

    public PurpleHazeCloudEntity(World world) {
        super(JEntityTypeRegistry.PURPLE_HAZE_COUD, world);
    }

    @Override
    protected void initDataTracker() {
        dataTracker.startTracking(RADIUS, 1.0f);
        dataTracker.startTracking(POISON_TYPE, 0);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        age = nbt.getInt("Age");
        setRadius(nbt.getFloat("Radius"));
        dataTracker.set(POISON_TYPE, nbt.getInt("PoisonType"));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Age", age);
        nbt.putFloat("Radius", getRadius());
        nbt.putInt("PoisonType", dataTracker.get(POISON_TYPE));
    }

    @Override
    public void tick() {
        super.tick();

        float radius = getRadius();
        PoisonType poisonType = getPoisonType();

        if (getWorld().isClient()) {
            for (int i = 0; i < radius; i++) {
                getWorld().addParticle(
                        JParticleTypeRegistry.PURPLE_HAZE_CLOUD, false,
                        getX() + random.nextGaussian() * radius / 2,
                        getY() + random.nextGaussian() * radius / 2,
                        getZ() + random.nextGaussian() * radius / 2,
                        0, 0, 0
                );

                getWorld().addParticle(
                        switch (poisonType) {
                            case HARMING -> JParticleTypeRegistry.PURPLE_HAZE_PARTICLE;
                            case NULLIFYING -> ParticleTypes.POOF;
                            case DEBILITATING -> ParticleTypes.SQUID_INK;
                        },
                        false,
                        getX() + random.nextGaussian() * radius / 2,
                        getY() + random.nextGaussian() * radius / 2,
                        getZ() + random.nextGaussian() * radius / 2,
                        0, 0, 0
                );
            }
        } else {
            // -0.5 radius per second
            setRadius(radius - 0.025f);

            if (getRadius() <= 0 || age >= MAX_AGE) {
                discard();
                return;
            }

            getWorld().getOtherEntities(this, getBoundingBox(),
                    EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(EntityPredicates.VALID_ENTITY)).forEach(
                    entity -> {
                        if (entity instanceof LivingEntity living) {
                            switch (poisonType) {
                                case HARMING -> AbstractPurpleHazeEntity.infect(living, 4);
                                case DEBILITATING -> {
                                    AbstractPurpleHazeEntity.infect(living, 3, StatusEffects.BLINDNESS);
                                    AbstractPurpleHazeEntity.infect(living, 3, StatusEffects.SLOWNESS);
                                    AbstractPurpleHazeEntity.infect(living, 3, StatusEffects.WEAKNESS);
                                }
                                case NULLIFYING -> {
                                    Stack<StatusEffect> toRemove = new Stack<>();

                                    living.getStatusEffects().forEach(
                                            statusEffectInstance -> {
                                                StatusEffect effectType = statusEffectInstance.getEffectType();
                                                if (effectType != JStatusRegistry.DAZED && effectType != JStatusRegistry.KNOCKDOWN)
                                                    toRemove.add(effectType);
                                            }
                                    );

                                    toRemove.forEach(living::removeStatusEffect);
                                }
                            }
                        }
                    }
            );
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (RADIUS.equals(data)) {
            this.calculateDimensions();
        }

        super.onTrackedDataSet(data);
    }

    @Override
    protected Box calculateBoundingBox() {
        float radius = getRadius();
        double x = getX(), y = getY(), z = getZ();
        return new Box(
                x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius
        );
    }
}