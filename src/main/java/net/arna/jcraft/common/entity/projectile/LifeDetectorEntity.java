package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.IOwnable;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Set;

public class LifeDetectorEntity extends JAttackEntity implements GeoEntity {
    private static final TrackedData<Boolean> EXPLODED;
    public LivingEntity target;

    static {
        EXPLODED = DataTracker.registerData(LifeDetectorEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    public boolean hasExploded() {
        return this.dataTracker.get(EXPLODED);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(EXPLODED, false);
    }

    public LifeDetectorEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        if (target == null || master == null) return false;
        if (target == this || target == master) return false;
        if (target.isConnectedThroughVehicle(master)) return false;
        if (target instanceof IOwnable ownable && ownable.getMaster() == master) return false;
        return target.canTakeDamage() && target.isAlive() && JUtils.canDamage(getWorld().getDamageSources().mobAttack(master), target);
    }

    private void Explode() {
        setVelocity(0, 0, 0);
        velocityModified = true;

        Vec3d pos = getPos();
        Set<LivingEntity> hurt = JUtils.generateHitbox(getWorld(), pos, 2.25, e -> true);
        for (LivingEntity living : hurt) {
            if (!canTarget(living)) continue;
            LivingEntity target = JUtils.getUserIfStand(living);
            Vec3d kbVec = target.getPos().subtract(pos).normalize();
            StandEntity.damageLogic(getWorld(), target, kbVec, 10, 1, false, 5f, true, 9,
                    getWorld().getDamageSources().mobAttack(master), master, HitPropertyComponent.HitAnimation.MID, false);
        }

        dataTracker.set(EXPLODED, true);

        playSound(SoundEvents.ITEM_FIRECHARGE_USE, 1f, 1f);

        kill();
    }

    @Override
    public void tick() {
        super.tick();
        if (master == null) kill();
        if (hasExploded()) return;

        if (getWorld().isClient) getWorld().addParticle(
                ParticleTypes.FLAME,
                this.getX() + random.nextFloat() - 0.5f,
                this.getY() + random.nextFloat() - 0.5f,
                this.getZ() + random.nextFloat() - 0.5f,
                0.0, 0.0, 0.0);
        else {
            if (target == null) {
                if (this.age % 2 == 0) {
                    LivingEntity finalTarget = null;
                    List<LivingEntity> targets = getWorld().getEntitiesByClass(LivingEntity.class, this.getBoundingBox().expand(32f), EntityPredicates.VALID_ENTITY);

                    for (LivingEntity t :
                            targets) {
                        if (!canTarget(t)) continue;
                        if (finalTarget == null) {
                            finalTarget = t;
                            continue;
                        }
                        // Prioritise nearest
                        if (t.getPos().squaredDistanceTo(getPos()) < finalTarget.getPos().squaredDistanceTo(getPos()))
                            finalTarget = t;
                    }

                    target = finalTarget;
                }
            } else if (target.isAlive()) {
                Vec3d eyePos = target.getEyePos();
                lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, eyePos);
                if (this.squaredDistanceTo(eyePos) < 2.5) Explode(); //If closer than 1.58m
            } else target = null;

            if (!hasExploded() && (this.age >= 300 || getHealth() <= 0f)) Explode();

            // Lerp velocity to simulate inertia
            this.setVelocity(
                    getVelocity().add(getRotationVector().multiply(0.5)).multiply(0.25)
            );
            this.velocityModified = true;
        }
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

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        return false;
    }

    public static DefaultAttributeContainer.Builder createDetectorAttributes() {
        return createLivingAttributes() // This must be used instead of DefaultAttributeContainer.builder() due to compatibility with step-height-entity-attribute
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                .add(EntityAttributes.GENERIC_ARMOR)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
    }

    @Override
    protected Box calculateBoundingBox() { // Centered around 0,0,0 instead of 0,0.5,0
        double x = getX();
        double y = getY();
        double z = getZ();
        double s = hasExploded() ? 0.1 : 0.5;
        return new Box(x + s, y + s, z + s, x - s, y - s, z - s);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);
        if (master == null) return;
        boolean ownerIsPlayer = master instanceof PlayerEntity;
        tag.putBoolean("playerOwner", ownerIsPlayer);
        if (ownerIsPlayer) tag.putUuid("ownerUUID", master.getUuid());
        else tag.putInt("ownerID", master.getId());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);
        boolean ownerIsPlayer = tag.getBoolean("playerOwner");
        if (ownerIsPlayer) master = getWorld().getPlayerByUuid(tag.getUuid("ownerUUID"));
        else master = (LivingEntity) getWorld().getEntityById(tag.getInt("ownerID")); // Always is living
    }

    // Animations
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<GeoAnimatable>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<GeoAnimatable> state) {
        return state.setAndContinue(RawAnimation.begin().thenLoop(hasExploded() ? "animation.detector.explode" : "animation.detector.idle"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
