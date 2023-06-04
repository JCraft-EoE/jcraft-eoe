package net.arna.jcraft.common.entity;

import net.arna.jcraft.common.entity.ai.goal.StunningMeleeAttackGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.Arrays;

public class GESnakeEntity extends TameableEntity implements IAnimatable, IAnimationTickable {
    AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

    public GESnakeEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
        Arrays.fill(this.handDropChances, 1F);
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    protected void initDataTracker() {
        super.initDataTracker();
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(4, new PounceAtTargetGoal(this, 0.4F));
        this.goalSelector.add(5, new StunningMeleeAttackGoal(this, 1.0, true, 10));
        this.goalSelector.add(6, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F, false));
        this.goalSelector.add(10, new LookAtEntityGoal(this, LivingEntity.class, 32.0F));
        this.goalSelector.add(10, new LookAroundGoal(this));

        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(2, new AttackWithOwnerGoal(this));
        this.targetSelector.add(8, new UniversalAngerGoal(this, true));
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "movement", 10, this::predicate));
        animationData.addAnimationController(new AnimationController(this, "attack", 0, this::attackPredicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.animationFactory;
    }

    @Override
    public int tickTimer() {
        return age;
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationController controller = event.getController();
        if (event.isMoving()) {
            controller.setAnimation(new AnimationBuilder().loop("animation.gesnake.move"));
            controller.setAnimationSpeed(1 + this.getVelocity().length());
        } else {
            controller.setAnimation(new AnimationBuilder().loop("animation.gesnake.idle"));
        }

        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState attackPredicate(AnimationEvent<E> event) {
        if (!this.handSwinging) {
            return PlayState.STOP;
        }
        event.getController().setAnimation(new AnimationBuilder().loop("animation.gesnake.attack"));
        return PlayState.CONTINUE;
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isClient()) {
            if (this.handSwinging) {
                this.handSwingTicks += 1;

                if (this.handSwingTicks > 10) {
                    this.handSwinging = false;
                    this.handSwingTicks = 0;
                }
            }
        } else if (this.age == 500) {
            dropStack(getMainHandStack());
            this.kill();
        } else if (this.isAlive() && this.age > 500) { // Edge case, mostly dealing with unloading
            this.discard();
        }
    }
}
