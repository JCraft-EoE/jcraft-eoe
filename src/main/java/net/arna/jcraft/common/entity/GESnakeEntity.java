package net.arna.jcraft.common.entity;

import net.arna.jcraft.common.entity.ai.goal.StunningMeleeAttackGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Arrays;

public class GESnakeEntity extends TameableEntity implements GeoEntity {
    public GESnakeEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
        Arrays.fill(this.handDropChances, 1F);
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(4, new PounceAtTargetGoal(this, 0.4F));
        this.goalSelector.add(5, new StunningMeleeAttackGoal(this, 1.0, true, 10));
        this.goalSelector.add(10, new LookAtEntityGoal(this, LivingEntity.class, 32.0F));
        this.goalSelector.add(10, new LookAroundGoal(this));

        this.goalSelector.add(6, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F, false));

        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(2, new AttackWithOwnerGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (getWorld().isClient()) {
            if (this.handSwinging) {
                this.handSwingTicks += 1;

                if (this.handSwingTicks > 10) {
                    this.handSwinging = false;
                    this.handSwingTicks = 0;
                }
            }
        } else if (this.age == 500) {
            dropStack(getMainHandStack());
            kill();
        } else if (this.isAlive() && this.age > 500) { // Edge case, mostly dealing with unloading
            discard();
        }
    }

    // Animations
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 10, this::predicate));
        controllers.add(new AnimationController<>(this, "attack", 0, this::attackPredicate));
    }

    private PlayState attackPredicate(AnimationState<GESnakeEntity> state) {
        if (!handSwinging) return PlayState.STOP;

        state.setAnimation(RawAnimation.begin().thenLoop("animation.gesnake.attack"));
        return PlayState.CONTINUE;
    }

    private PlayState predicate(AnimationState<GESnakeEntity> state) {
        if (state.isMoving()) {
            state.setAnimation(RawAnimation.begin().thenLoop("animation.gesnake.move"));
            state.getController().setAnimationSpeed(1 + this.getVelocity().length());
        } else {
            state.setAnimation(RawAnimation.begin().thenLoop("animation.gesnake.idle"));
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
