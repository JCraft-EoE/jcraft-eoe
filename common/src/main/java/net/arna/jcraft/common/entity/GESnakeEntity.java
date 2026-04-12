package net.arna.jcraft.common.entity;

import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.util.MoveAnalysis;
import net.arna.jcraft.common.entity.ai.goal.StunningMeleeAttackGoal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class GESnakeEntity extends TamableAnimal {
    public static final String ATTACK_CONTROLLER = "attack_controller";
    public static final String MOVEMENT_CONTROLLER = "movement_controller";
    public MoveAnalysis moveAnalysis = new MoveAnalysis(this);

    public GESnakeEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
        Arrays.fill(this.handDropChances, 1F);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NonNull ServerLevel world, @NonNull AgeableMob entity) {
        return null;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(5, new StunningMeleeAttackGoal(this, 1.0, true, 10));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, LivingEntity.class, 32.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));

        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F, false));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            moveAnalysis.update();

            if (this.swinging) { // attacking
                ATTACK.sendForEntity(this);
                this.swingTime += 1;

                if (this.swingTime > 10) {
                    this.swinging = false;
                    this.swingTime = 0;
                }
            }
        } else if (this.tickCount == 500) {
            spawnAtLocation(getMainHandItem());
            kill();
        } else if (this.isAlive() && this.tickCount > 500) { // Edge case, mostly dealing with unloading
            discard();
        }
    }

    public static final AzCommand MOVE = AzCommand.create(MOVEMENT_CONTROLLER, "animation.gesnake.move", AzPlayBehaviors.LOOP);
    public static final AzCommand ATTACK = AzCommand.create(ATTACK_CONTROLLER, "animation.gesnake.attack");
}
