package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class WeatherTornadoEntity extends JAttackEntity {

    public static final int LIFETIME = 400;
    private static final float BASE_PULL_RADIUS = 12f;
    private static final float MAX_PULL_RADIUS  = 20f;
    private static final float PULL_STRENGTH    = 0.15f;
    private static final int WANDER_INTERVAL    = 45;

    private boolean electrified;
    private double driftX;
    private double driftZ;

    public WeatherTornadoEntity(Level world) {
        super(JEntityTypeRegistry.WEATHER_TORNADO.get(), world);
    }

    public void setElectrified(boolean electrified) {
        this.electrified = electrified;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            spawnTornadoParticles();
            return;
        }

        if (tickCount > LIFETIME || master == null) {
            discard();
            return;
        }

        if (tickCount % WANDER_INTERVAL == 1) {
            final double angle = random.nextDouble() * Math.PI * 2;
            driftX = Math.cos(angle) * 0.35;
            driftZ = Math.sin(angle) * 0.35;
        }

        setPos(getX() + driftX, getY(), getZ() + driftZ);

        final float lifeRatio = (float) tickCount / LIFETIME;
        final float pullRadius = BASE_PULL_RADIUS + lifeRatio * (MAX_PULL_RADIUS - BASE_PULL_RADIUS);

        if (tickCount % 4 == 0) {
            pullAndLaunchEntities(pullRadius);
        }

        if (electrified && tickCount % 10 == 0) {
            shockTouchingEntities();
        }

        if (lifeRatio > 0.3f && electrified && tickCount % 25 == 0) {
            spawnSurroundingLightning();
        }
    }

    private void pullAndLaunchEntities(float pullRadius) {
        final Vec3 center = position().add(0, 1.5, 0);
        final AABB pullBox = AABB.ofSize(center, pullRadius * 2, pullRadius, pullRadius * 2);

        for (final LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, pullBox,
                EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != master))) {

            final Vec3 toCenter = center.subtract(entity.position());
            final double dist = toCenter.horizontalDistance();
            if (dist > pullRadius || dist < 0.5) continue;

            final double strength = PULL_STRENGTH * (1.0 - dist / pullRadius);

            if (dist < 3.0) {
                entity.setDeltaMovement(entity.getDeltaMovement()
                        .add(0, 0.55 * strength * 8, 0));
                entity.hurt(level().damageSources().inWall(), 1.5f);
            } else {
                final Vec3 tangent = new Vec3(-toCenter.z, 0, toCenter.x).normalize();
                entity.setDeltaMovement(entity.getDeltaMovement()
                        .add(toCenter.normalize().scale(strength))
                        .add(tangent.scale(strength * 0.6)));
            }
            entity.hurtMarked = true;
        }

        for (final Entity entity : level().getEntities(this, pullBox,
                e -> e instanceof net.minecraft.world.entity.projectile.Projectile)) {
            final Vec3 toCenter = center.subtract(entity.position());
            final double dist = toCenter.length();
            if (dist > pullRadius || dist < 0.5) continue;
            entity.setDeltaMovement(entity.getDeltaMovement()
                    .add(toCenter.normalize().scale(PULL_STRENGTH * 1.5)));
        }
    }

    private void shockTouchingEntities() {
        final AABB touchBox = getBoundingBox().inflate(2.5);
        for (final LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, touchBox,
                EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != master))) {
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 2, true, true));
            entity.hurt(level().damageSources().lightningBolt(), 1.0f);
        }
    }

    private void spawnSurroundingLightning() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        final double angle = random.nextDouble() * Math.PI * 2;
        final double r = 4 + random.nextDouble() * 4;
        final LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
        bolt.setVisualOnly(true);
        bolt.setPos(getX() + Math.cos(angle) * r, getY(), getZ() + Math.sin(angle) * r);
        serverLevel.addFreshEntity(bolt);
    }

    private void spawnTornadoParticles() {
        final float lifeRatio = Math.min((float) tickCount / LIFETIME, 1.0f);
        final float maxRadius = 0.8f + lifeRatio * 3.0f;
        final int layers = 2 + (int) (lifeRatio * 2);

        for (int layer = 0; layer < layers; layer++) {
            final float layerHeight = layer * 0.5f;
            final float layerRadius = maxRadius * ((float) layer / layers);
            final int particlesAtLayer = 2 + layer;
            for (int j = 0; j < particlesAtLayer; j++) {
                final float angle = tickCount * 0.25f + layer * 0.6f + j * (Mth.TWO_PI / particlesAtLayer);
                final float px = (float) getX() + Mth.cos(angle) * layerRadius;
                final float pz = (float) getZ() + Mth.sin(angle) * layerRadius;
                level().addParticle(ParticleTypes.CLOUD,
                        px, getY() + layerHeight, pz,
                        Mth.cos(angle + Mth.HALF_PI) * 0.2, 0.08,
                        Mth.sin(angle + Mth.HALF_PI) * 0.2);
            }
        }

        if (tickCount % 2 == 0) {
            final int debrisCount = 1 + (int) (lifeRatio * 2);
            for (int i = 0; i < debrisCount; i++) {
                final float debrisAngle = tickCount * 0.18f + i * (Mth.TWO_PI / debrisCount);
                final float debrisR = maxRadius * (0.4f + random.nextFloat() * 0.6f);
                level().addParticle(ParticleTypes.POOF,
                        (float) getX() + Mth.cos(debrisAngle) * debrisR, getY() + 0.1f,
                        (float) getZ() + Mth.sin(debrisAngle) * debrisR,
                        Mth.cos(debrisAngle + Mth.HALF_PI) * 0.12, 0.05, Mth.sin(debrisAngle + Mth.HALF_PI) * 0.12);
            }
        }

        if (electrified && random.nextInt(5) == 0) {
            final float angle = tickCount * 0.3f + random.nextFloat() * Mth.TWO_PI;
            final float r = maxRadius * 0.5f;
            level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    getX() + Mth.cos(angle) * r, getY() + random.nextFloat() * 2.0f, getZ() + Mth.sin(angle) * r,
                    0, 0.06, 0);
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
        tag.putBoolean("Electrified", electrified);
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        readMasterNbt(tag);
        electrified = tag.getBoolean("Electrified");
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
