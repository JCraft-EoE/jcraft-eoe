package net.arna.jcraft.common.entity;

import net.arna.jcraft.common.util.JCraftUtils;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Arm;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.List;

public class LifeDetectorEntity extends LivingEntity implements IAnimatable {
    public LivingEntity target;
    private LivingEntity owner;
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public static TrackedData<Boolean> EXPLODED;
    static { EXPLODED = DataTracker.registerData(LifeDetectorEntity.class, TrackedDataHandlerRegistry.BOOLEAN); }
    public boolean hasExploded() { return this.dataTracker.get(EXPLODED); }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(EXPLODED, false);
    }

    public LifeDetectorEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
    public void setOwner(LivingEntity l) { this.owner = l; }

    @Override
    public boolean canTarget(LivingEntity target) {
        if (target == this) return false;
        if (target == owner) return false;
        if (target.isConnectedThroughVehicle(owner)) return false;
        return target.canTakeDamage();
    }

    private void Explode() {
        setVelocity(0, 0, 0);
        velocityModified = true;

        Vec3d pos = getPos();
        List<LivingEntity> hurt = JCraftUtils.GenerateHitbox(world, pos, 2.25, null);
        for (LivingEntity living :
                hurt) {
            if (living == owner || living.getVehicle() == owner) continue;
            Vec3d kbVec = living.getPos().subtract(pos).normalize();
            StandEntity.damageLogic(world, living, kbVec, 10, 1, false, 5f, true, DamageSource.mob(owner), owner);
        }

        this.dataTracker.set(EXPLODED, true);

        playSound(SoundEvents.ITEM_FIRECHARGE_USE, 1f, 1f);

        kill();
    }

    @Override
    public void tick() {
        super.tick();
        if (hasExploded()) return;

        if (world.isClient) {
            this.world.addParticle(
                    ParticleTypes.FLAME,
                    this.getX() + random.nextFloat() - 0.5f,
                    this.getY() + random.nextFloat() - 0.5f,
                    this.getZ() + random.nextFloat() - 0.5f,
                    0.0, 0.0, 0.0
            );
        } else {
            if (target == null) {
                if (this.age % 2 == 0) {
                    LivingEntity finalTarget = null;
                    List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, this.getBoundingBox().expand(32f), EntityPredicates.VALID_ENTITY);

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
            } else {
                Vec3d eyePos = target.getEyePos();
                lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, eyePos);
                if (this.squaredDistanceTo(eyePos) < 3) Explode(); //If closer than 1.72m
            }

            if ( !hasExploded() && (this.age >= 300 || getHealth() <= 0f) ) Explode();

            // Lerp velocity to simulate inertia
            this.setVelocity(
                    getVelocity().add( getRotationVector().multiply(0.25f) ).multiply(0.5)
            );
            this.velocityModified = true;
        }
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BLOCK_LAVA_EXTINGUISH;
    }
    @Override
    public boolean hasNoGravity() {
        return true;
    }
    public static DefaultAttributeContainer.Builder createDetectorAttributes() {
        return DefaultAttributeContainer.builder()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                .add(EntityAttributes.GENERIC_ARMOR)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
    }
    @Override
    protected Box calculateBoundingBox() { // Centered around 0,0,0 instead of 0,0.5,0
        if (hasExploded()) return new Box(0, 0, 0, 0, 0.1, 0);
        return new Box(getX() + 0.5, getY() + 0.5, getZ() + 0.5, getX() - 0.5, getY() - 0.5, getZ() - 0.5);
    }


    @Override
    public Iterable<ItemStack> getArmorItems() { return List.of(); }
    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) { return ItemStack.EMPTY; }
    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) { }
    @Override
    public Arm getMainArm() { return null; }
    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (hasExploded())
            event.getController().setAnimation(new AnimationBuilder().playOnce("animation.detector.explode"));
        else
            event.getController().setAnimation(new AnimationBuilder().loop("animation.detector.idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() { return this.factory; }
}
