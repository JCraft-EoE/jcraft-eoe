package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.attack.moves.speedking.HeatTrapManager;
import net.arna.jcraft.common.attack.moves.speedking.HeatWavesAttack;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
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
import org.jetbrains.annotations.NotNull;
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.common.attack.moves.speedking.OverheatAttack;

public class FireSparkProjectile extends ThrowableProjectile {
    private static final EntityDataAccessor<Boolean> HEAT_TRAP_MODE = SynchedEntityData.defineId(FireSparkProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BOUNCING_MODE = SynchedEntityData.defineId(FireSparkProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HEAT_WAVE_MODE = SynchedEntityData.defineId(FireSparkProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> BASE_DAMAGE = SynchedEntityData.defineId(FireSparkProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> BOUNCE_COUNT = SynchedEntityData.defineId(FireSparkProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_BOUNCES = SynchedEntityData.defineId(FireSparkProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HEAT_TRAP_BLINDNESS = SynchedEntityData.defineId(FireSparkProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HEAT_TRAP_CONFUSION = SynchedEntityData.defineId(FireSparkProjectile.class, EntityDataSerializers.INT);

    private final boolean isStationary = false;
    private int stationaryTimer = 0;
    private static final int STATIONARY_LIFETIME = 200;

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
        this.entityData.define(HEAT_TRAP_MODE, false);
        this.entityData.define(BOUNCING_MODE, false);
        this.entityData.define(HEAT_WAVE_MODE, false);
        this.entityData.define(BASE_DAMAGE, 2.0f);
        this.entityData.define(BOUNCE_COUNT, 0);
        this.entityData.define(MAX_BOUNCES, 3);
        this.entityData.define(HEAT_TRAP_BLINDNESS, 80);
        this.entityData.define(HEAT_TRAP_CONFUSION, 60);
    }

    public void setHeatTrapMode(boolean heatTrap) { this.entityData.set(HEAT_TRAP_MODE, heatTrap); }
    public boolean isHeatTrapMode() { return this.entityData.get(HEAT_TRAP_MODE); }

    public void setBouncingMode(boolean bouncing) { this.entityData.set(BOUNCING_MODE, bouncing); }
    public boolean isBouncingMode() { return this.entityData.get(BOUNCING_MODE); }

    public void setHeatWaveMode(boolean heatWave) { this.entityData.set(HEAT_WAVE_MODE, heatWave); }
    public boolean isHeatWaveMode() { return this.entityData.get(HEAT_WAVE_MODE); }

    public void setBaseDamage(float damage) { this.entityData.set(BASE_DAMAGE, damage); }
    public float getBaseDamage() { return this.entityData.get(BASE_DAMAGE); }

    public void setBounceCount(int count) { this.entityData.set(BOUNCE_COUNT, count); }
    public int getBounceCount() { return this.entityData.get(BOUNCE_COUNT); }

    public void setMaxBounces(int max) { this.entityData.set(MAX_BOUNCES, max); }
    public int getMaxBounces() { return this.entityData.get(MAX_BOUNCES); }

    public float getCurrentDamage() {
        return isBouncingMode() ? getBaseDamage() * (1.0f + getBounceCount() * 0.25f) : getBaseDamage();
    }

    @Override
    public void tick() {
        super.tick();

        if (isStationary && isBouncingMode()) {
            if (++stationaryTimer >= STATIONARY_LIFETIME) {
                discard();
                return;
            }
            for (Entity entity : level().getEntitiesOfClass(Entity.class, getBoundingBox().inflate(1.0))) {
                if (entity instanceof LivingEntity livingEntity && entity != getOwner()) {
                    hitEntity(livingEntity);
                    discard();
                    return;
                }
            }
        }

        if (level().isClientSide) {
            level().addParticle(net.minecraft.core.particles.ParticleTypes.FLAME, getX(), getY(), getZ(), 0, 0, 0);
        }
    }

    @Override
    protected void onHit(@NotNull HitResult hitResult) {
        if (isStationary) return;

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hitResult;
            if (entityHit.getEntity() instanceof LivingEntity livingEntity && entityHit.getEntity() != getOwner()) {
                hitEntity(livingEntity);
                if (!isHeatWaveMode()) discard();
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
    public void remove(@NotNull RemovalReason reason) {
        if (isHeatWaveMode()) HeatWavesAttack.cleanupProjectile(this);
        super.remove(reason);
    }

    private void hitEntity(LivingEntity target) {
        if (level().isClientSide) return;
        LivingEntity realTarget = JUtils.getUserIfStand(target);

        JUtils.projectileDamageLogic(this, level(), realTarget, Vec3.ZERO,
                (int)(getCurrentDamage() * 2), 1, false,
                getCurrentDamage(), 0, CommonHitPropertyComponent.HitAnimation.MID);

        if (isHeatTrapMode()) {
            if (getOwner() instanceof LivingEntity owner) {
                HeatTrapManager.addHeat(realTarget, owner);
                HeatTrapManager.addHeat(realTarget, owner);
            }
            discard();
            return;
        } else if (isHeatWaveMode()) {
            realTarget.addEffect(new MobEffectInstance(JStatusRegistry.BOILING.get(), 50, 0, false, true));
            if (getOwner() instanceof LivingEntity ownerOwner) {
                CommonStandComponent standComp = JComponentPlatformUtils.getStandComponent(ownerOwner);
                if (standComp != null && standComp.getStand() instanceof SpeedKingEntity speedKing) {
                    OverheatAttack.triggerAutoOverheat(speedKing, realTarget, 3);
                }
            }
        } else if (isBouncingMode()) {
            realTarget.addEffect(new MobEffectInstance(JStatusRegistry.BOILING.get(), 100, 0, false, true));
            realTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 10, false, true));
            realTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, true));
        } else {
            realTarget.setSecondsOnFire(5);
        }

        destroyPlantsAndWater(target.blockPosition());
    }

    private void createHeatWave(BlockPos impactPos) {
        int radius = 2;
        Vec3 center = Vec3.atCenterOf(impactPos);

        if (!level().isClientSide()) {
            level().explode(this, center.x, center.y, center.z, 0.5f, false, Level.ExplosionInteraction.NONE);
        }

        Entity owner = getOwner();
        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class,
                new net.minecraft.world.phys.AABB(center.add(-radius, -2, -radius), center.add(radius, 2, radius)),
                entity -> entity != owner)) {
            entity.addEffect(new MobEffectInstance(JStatusRegistry.BOILING.get(), 100, 0, false, true));
            JUtils.projectileDamageLogic(this, level(), entity, entity.getEyePosition().subtract(center).normalize(),
                    400, StunType.WINDED.ordinal(), true, 2.0f, 0, CommonHitPropertyComponent.HitAnimation.MID);
        }

        for (int x = -radius; x <= radius; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    destroyPlantsAndWater(impactPos.offset(x, y, z));
                }
            }
        }
    }

    private void handleBounce(BlockHitResult blockHit) {
        if (getBounceCount() >= getMaxBounces()) {
            destroyPlantsAndWater(blockHit.getBlockPos());
            discard();
            return;
        }

        Vec3 velocity = getDeltaMovement();
        Vec3 normal = Vec3.atLowerCornerOf(blockHit.getDirection().getNormal());
        setDeltaMovement(velocity.subtract(normal.scale(2 * velocity.dot(normal))).scale(0.8));
        setBounceCount(getBounceCount() + 1);
        destroyPlantsAndWater(blockHit.getBlockPos());
    }

    private static final int HEAT_TRAP_BLOCK_RADIUS = 3;
    private static final int HEAT_TRAP_BLOCK_DURATION = 200;

    private void handleBlockHit(BlockHitResult blockHit) {
        if (isHeatTrapMode()) {
            BlockPos center = blockHit.getBlockPos();
            java.util.UUID attackerUUID = getOwner() instanceof LivingEntity le ? le.getUUID() : null;
            for (int x = -HEAT_TRAP_BLOCK_RADIUS; x <= HEAT_TRAP_BLOCK_RADIUS; x++) {
                for (int z = -HEAT_TRAP_BLOCK_RADIUS; z <= HEAT_TRAP_BLOCK_RADIUS; z++) {
                    BlockPos pos = center.offset(x, 0, z);
                    if (!level().getBlockState(pos).isAir()) {
                        HeatTrapManager.heatBlock(level(), pos, HEAT_TRAP_BLOCK_DURATION, attackerUUID);
                    }
                }
            }
        }

        destroyPlantsAndWater(blockHit.getBlockPos());
    }

    private void destroyPlantsAndWater(BlockPos center) {
        if (level().getGameRules().getBoolean(JCraft.STAND_GRIEFING)) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        BlockState state = level().getBlockState(pos);

                        if (state.is(BlockTags.FLOWERS) || state.is(BlockTags.CROPS) || state.is(BlockTags.SAPLINGS) ||
                                state.is(Blocks.GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN) ||
                                state.is(Blocks.LARGE_FERN) || state.is(Blocks.DEAD_BUSH) || state.is(Blocks.SEAGRASS) ||
                                state.is(Blocks.TALL_SEAGRASS) || state.is(Blocks.VINE) || state.is(Blocks.LILY_PAD) ||
                                state.is(Blocks.KELP) || state.is(Blocks.KELP_PLANT)) {
                            level().destroyBlock(pos, false);
                        }

                        if (state.is(Blocks.WATER)) {
                            level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }
}
