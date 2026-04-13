package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.attack.moves.speedking.HeatWavesAttack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class FireSparkProjectile extends ThrowableProjectile {
    private static final EntityDataAccessor<Boolean> FLASHBANG_MODE = SynchedEntityData.defineId(FireSparkProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BOUNCING_MODE = SynchedEntityData.defineId(FireSparkProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HEAT_WAVE_MODE = SynchedEntityData.defineId(FireSparkProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> BASE_DAMAGE = SynchedEntityData.defineId(FireSparkProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> BOUNCE_COUNT = SynchedEntityData.defineId(FireSparkProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_BOUNCES = SynchedEntityData.defineId(FireSparkProjectile.class, EntityDataSerializers.INT);

    private boolean isStationary = false;
    private int stationaryTimer = 0;
    private static final int STATIONARY_LIFETIME = 200; // 10 seconds

    public FireSparkProjectile(Level level) {
        super(JEntityTypeRegistry.FIRE_SPARK_PROJECTILE.get(), level);
    }

    public FireSparkProjectile(EntityType<FireSparkProjectile> type, Level level) {
        super(type, level);
    }

    public FireSparkProjectile(Level level, LivingEntity shooter) {
        this(JEntityTypeRegistry.FIRE_SPARK_PROJECTILE.get(), level);
        setOwner(shooter);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(FLASHBANG_MODE, false);
        this.entityData.define(BOUNCING_MODE, false);
        this.entityData.define(HEAT_WAVE_MODE, false);
        this.entityData.define(BASE_DAMAGE, 2.0f);
        this.entityData.define(BOUNCE_COUNT, 0);
        this.entityData.define(MAX_BOUNCES, 3);
    }

    public void setFlashbangMode(boolean flashbang) {
        this.entityData.set(FLASHBANG_MODE, flashbang);
    }

    public boolean isFlashbangMode() {
        return this.entityData.get(FLASHBANG_MODE);
    }

    public void setBouncingMode(boolean bouncing) {
        this.entityData.set(BOUNCING_MODE, bouncing);
    }

    public boolean isBouncingMode() {
        return this.entityData.get(BOUNCING_MODE);
    }

    public void setHeatWaveMode(boolean heatWave) {
        this.entityData.set(HEAT_WAVE_MODE, heatWave);
    }

    public boolean isHeatWaveMode() {
        return this.entityData.get(HEAT_WAVE_MODE);
    }

    public void setBaseDamage(float damage) {
        this.entityData.set(BASE_DAMAGE, damage);
    }

    public float getBaseDamage() {
        return this.entityData.get(BASE_DAMAGE);
    }

    public void setBounceCount(int count) {
        this.entityData.set(BOUNCE_COUNT, count);
    }

    public int getBounceCount() {
        return this.entityData.get(BOUNCE_COUNT);
    }

    public void setMaxBounces(int max) {
        this.entityData.set(MAX_BOUNCES, max);
    }

    public int getMaxBounces() {
        return this.entityData.get(MAX_BOUNCES);
    }

    public float getCurrentDamage() {
        if (isBouncingMode()) {
            // Increase damage by 25% per bounce
            return getBaseDamage() * (1.0f + (getBounceCount() * 0.25f));
        }
        return getBaseDamage();
    }

    @Override
    public void tick() {
        super.tick();

        if (isStationary && isBouncingMode()) {
            stationaryTimer++;
            if (stationaryTimer >= STATIONARY_LIFETIME) {
                discard();
                return;
            }

            // Check for entities stepping on the stationary spark
            for (Entity entity : level().getEntitiesOfClass(Entity.class, getBoundingBox().inflate(1.0))) {
                if (entity instanceof LivingEntity livingEntity && entity != getOwner()) {
                    hitEntity(livingEntity);
                    discard();
                    return;
                }
            }
        }

        // Add fire particles for visibility
        if (level().isClientSide) {
            level().addParticle(net.minecraft.core.particles.ParticleTypes.FLAME, getX(), getY(), getZ(), 0, 0, 0);
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (isStationary) return;

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hitResult;
            if (entityHit.getEntity() instanceof LivingEntity livingEntity && entityHit.getEntity() != getOwner()) {
                hitEntity(livingEntity);
                if (!isBouncingMode() && !isHeatWaveMode()) {
                    discard();
                }
            }
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;

            if (isHeatWaveMode()) {
                createHeatWave(blockHit.getBlockPos());
                HeatWavesAttack.cleanupProjectile(this);
                discard();
            } else if (isBouncingMode()) {
                handleBounce(blockHit);
            } else {
                handleBlockHit(blockHit);
                discard();
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (isHeatWaveMode()) {
            HeatWavesAttack.cleanupProjectile(this);
        }
        super.remove(reason);
    }

    private void hitEntity(LivingEntity target) {
        // Deal damage
        target.hurt(target.damageSources().thrown(this, getOwner()), getCurrentDamage());

        if (isFlashbangMode()) {
            // Flashbang effects: blindness only
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, true)); // 5 seconds
            target.addEffect(new MobEffectInstance(JStatusRegistry.BOILING.get(), 100, 0, false, true)); // 5 seconds
        } else if (getBaseDamage() <= 2.0f) {
            // Fire Sparks Heavy: lower damage = boiling only, no stun
            target.addEffect(new MobEffectInstance(JStatusRegistry.BOILING.get(), 100, 0, false, true)); // 5 seconds
        } else if (isBouncingMode()) {
            // Fire Sparks effects: Boiling, blindness, slowness
            target.addEffect(new MobEffectInstance(JStatusRegistry.BOILING.get(), 200, 0, false, true)); // 10 seconds
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 10, false, true)); // 3 seconds
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, true)); // 5 seconds
        } else {
            // Regular fire spark: just set on fire
            target.setSecondsOnFire(5);
        }

        // Destroy nearby plants and water for all modes
        destroyPlantsAndWater(target.blockPosition());
    }

    private void createHeatWave(BlockPos impactPos) {
        int radius = 2;
        Vec3 center = Vec3.atCenterOf(impactPos);

        // TNT-style explosion — handles particles and sound automatically, no block damage
        if (!level().isClientSide()) {
            level().explode(this, center.x, center.y, center.z, 0.5f, false, Level.ExplosionInteraction.NONE);
        }

        // Apply boiling to entities in the area
        List<LivingEntity> entitiesInArea = level().getEntitiesOfClass(LivingEntity.class,
                new net.minecraft.world.phys.AABB(center.add(-radius, -2, -radius), center.add(radius, 2, radius)),
                entity -> entity != getOwner());

        for (LivingEntity entity : entitiesInArea) {
            entity.addEffect(new MobEffectInstance(JStatusRegistry.BOILING.get(), 200, 0, false, true));
            entity.hurt(entity.damageSources().explosion(null, getOwner()), 2.0f);
        }

        // Destroy plants and water in the area
        for (int x = -radius; x <= radius; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = impactPos.offset(x, y, z);
                    destroyPlantsAndWater(pos);
                }
            }
        }
    }

    public void explodeAsHeatWave() {
        createHeatWave(blockPosition());
    }

    private void handleBounce(BlockHitResult blockHit) {
        if (getBounceCount() >= getMaxBounces()) {
            // Create fire when stopping after max bounces
            BlockPos firePos = blockHit.getBlockPos().relative(blockHit.getDirection());
            if (level().getBlockState(firePos).isAir()) {
                level().setBlockAndUpdate(firePos, Blocks.FIRE.defaultBlockState());
            }
            destroyPlantsAndWater(blockHit.getBlockPos());
            discard(); // Remove projectile instead of becoming stationary
            return;
        }

        // Calculate bounce direction
        Vec3 velocity = getDeltaMovement();
        Vec3 normal = Vec3.atLowerCornerOf(blockHit.getDirection().getNormal());

        // Reflect velocity off the surface
        Vec3 newVelocity = velocity.subtract(normal.scale(2 * velocity.dot(normal)));

        // Reduce speed slightly with each bounce
        newVelocity = newVelocity.scale(0.8);

        setDeltaMovement(newVelocity);
        setBounceCount(getBounceCount() + 1);

        // Destroy blocks at impact point
        destroyPlantsAndWater(blockHit.getBlockPos());
    }

    private void handleBlockHit(BlockHitResult blockHit) {
        if (isFlashbangMode()) {
            // Create flashbang explosion effect - no damage, just blindness/nausea in area
            Vec3 explosionPos = blockHit.getLocation();
            int radius = 4; // 4 block radius for flashbang effect

            // Find all living entities in the area
            List<LivingEntity> entitiesInArea = level().getEntitiesOfClass(LivingEntity.class,
                    new net.minecraft.world.phys.AABB(explosionPos.add(-radius, -radius, -radius), explosionPos.add(radius, radius, radius)),
                    entity -> entity != getOwner());

            for (LivingEntity entity : entitiesInArea) {
                // Apply flashbang effects: blindness and nausea
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, true)); // 5 seconds
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0, false, true)); // 4 seconds nausea
            }

            // Create explosion visual/sound effect but no damage
            if (!level().isClientSide) {
                level().explode(this, explosionPos.x, explosionPos.y, explosionPos.z, 0.5f, false,
                        Level.ExplosionInteraction.NONE);
            }
        } else {
            // Regular fire spark: create fire
            BlockPos firePos = blockHit.getBlockPos().relative(blockHit.getDirection());
            if (level().getBlockState(firePos).isAir()) {
                level().setBlockAndUpdate(firePos, Blocks.FIRE.defaultBlockState());
            }
        }

        destroyPlantsAndWater(blockHit.getBlockPos());
    }

    private void destroyPlantsAndWater(BlockPos center) {
        int radius = 1;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level().getBlockState(pos);

                    if (state.is(BlockTags.FLOWERS) ||
                            state.is(BlockTags.CROPS) ||
                            state.is(BlockTags.SAPLINGS) ||
                            state.is(Blocks.GRASS) ||
                            state.is(Blocks.TALL_GRASS) ||
                            state.is(Blocks.FERN) ||
                            state.is(Blocks.LARGE_FERN) ||
                            state.is(Blocks.DEAD_BUSH) ||
                            state.is(Blocks.SEAGRASS) ||
                            state.is(Blocks.TALL_SEAGRASS) ||
                            state.is(Blocks.VINE) ||
                            state.is(Blocks.LILY_PAD) ||
                            state.is(Blocks.KELP) ||
                            state.is(Blocks.KELP_PLANT)) {
                        level().destroyBlock(pos, false);
                    }

                    // Destroy water source blocks
                    if (state.is(Blocks.WATER)) {
                        level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
}