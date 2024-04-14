package net.arna.jcraft.common.entity;

import net.arna.jcraft.common.entity.stand.AbstractPurpleHazeEntity;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.arna.jcraft.registry.JParticleTypeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class PurpleHazeCloudEntity extends Entity {
    public static int MAX_AGE = 100;
    private static final TrackedData<Float> RADIUS;

    static {
        RADIUS = DataTracker.registerData(PurpleHazeCloudEntity.class, TrackedDataHandlerRegistry.FLOAT);
    }

    public PurpleHazeCloudEntity(World world, float radius) {
        this(world);
        setRadius(radius);
    }

    public float getRadius() {
        return dataTracker.get(RADIUS);
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
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        age = nbt.getInt("Age");
        setRadius(nbt.getFloat("Radius"));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Age", age);
        nbt.putFloat("Radius", getRadius());
    }

    @Override
    public void tick() {
        super.tick();

        float radius = getRadius();

        if (world.isClient()) {
            for (int i = 0; i < radius; i++) {
                world.addParticle(
                        JParticleTypeRegistry.PURPLE_HAZE_CLOUD, false,
                        getX() + random.nextGaussian() * radius / 2,
                        getY() + random.nextGaussian() * radius / 2,
                        getZ() + random.nextGaussian() * radius / 2,
                        0, 0, 0
                );

                world.addParticle(
                        JParticleTypeRegistry.PURPLE_HAZE_PARTICLE, false,
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

            world.getOtherEntities(this, getBoundingBox(),
                    EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(EntityPredicates.VALID_ENTITY)).forEach(
                    entity -> {
                        if (entity instanceof LivingEntity living)
                            AbstractPurpleHazeEntity.infect(living, 4);
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

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}
