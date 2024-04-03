package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RedBindEntity extends JAttackEntity implements GeoEntity {
    private LivingEntity boundEntity;
    private float boundHealth;
    public static final int ticksToLive = 60;
    private int timeLeft = ticksToLive;
    private static final TrackedData<Boolean> EXPLODED;
    private static final TrackedData<Float> WIDTH;

    static {
        EXPLODED = DataTracker.registerData(RedBindEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        WIDTH = DataTracker.registerData(RedBindEntity.class, TrackedDataHandlerRegistry.FLOAT);
    }

    public boolean hasExploded() {
        return this.dataTracker.get(EXPLODED);
    }

    public float getBoundWidth() {
        return dataTracker.get(WIDTH);
    }

    public void setBoundEntity(@NotNull LivingEntity boundEntity) {
        this.boundEntity = boundEntity;
        this.boundHealth = boundEntity.getHealth();
        this.dataTracker.set(WIDTH, (float) boundEntity.getBoundingBox().getAverageSideLength());
        this.startRiding(boundEntity, true);
        boundEntity.addStatusEffect(new StatusEffectInstance(JStatusRegistry.STANDLESS, timeLeft, 0, true, false));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(EXPLODED, false);
        dataTracker.startTracking(WIDTH, 1f);
    }

    public RedBindEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public double getMountedHeightOffset() {
        return -1.0;
    }

    @Override
    public void tick() {
        if (!getWorld().isClient) {
            if (boundEntity == null) { // If boundEntity data was wiped, attempt to recover
                if (getVehicle() instanceof LivingEntity living) setBoundEntity(living);
            } else if (!hasVehicle() && !hasExploded()) { // If detached
                detonate();
            }

            if (boundEntity == null)
                discard();
            else if ( !hasExploded() && (--timeLeft <= 0 || boundEntity.getHealth() < boundHealth) )
                detonate();

            // In practice, redbind lasts slightly longer than the duration, so to account for this,
            // we add two ticks of standless until we're actually done.
            if (boundEntity != null && boundEntity.getStatusEffect(JStatusRegistry.STANDLESS) == null)
                boundEntity.addStatusEffect(new StatusEffectInstance(JStatusRegistry.STANDLESS, 2, 0, true, false));
        }

        super.tick();
    }

    private void detonate() {
        if (master != null) {
            Vec3d vel = boundEntity.getPos().add(0, 0.5, 0).subtract(master.getPos());
            Vec3d launch = vel.normalize().multiply(1.25);
            StandEntity.damageLogic(boundEntity.getWorld(), boundEntity, launch, 20, 3, true,
                    6, false, 4, getWorld().getDamageSources().mobAttack(master), master, HitPropertyComponent.HitAnimation.MID, false, true);
        }

        dataTracker.set(EXPLODED, true);
        kill();
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BLOCK_LAVA_EXTINGUISH;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLOCK_LAVA_EXTINGUISH;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    // Animations
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<RedBindEntity> state) {
        return state.setAndContinue(RawAnimation.begin().thenLoop(hasExploded() ? "animation.red_bind.explode" : "animation.red_bind.idle"));
    }
}
