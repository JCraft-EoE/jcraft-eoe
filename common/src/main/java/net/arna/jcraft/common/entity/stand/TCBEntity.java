package net.arna.jcraft.common.entity.stand;

import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.core.animation.RawAnimation;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.*;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Consumer;

public class TCBEntity extends StandEntity<TCBEntity, TCBEntity.State> {
    public static final MoveSet<TCBEntity, TCBEntity.State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.TCB,
            TCBEntity::registerMoves, TCBEntity.State.class);

    public static final StandData DATA = StandData.builder()
            .idleDistance(0f)
            .idleRotation(0f)
            .blockDistance(0f)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.tcb"))
                    .proCount(2)
                    .conCount(4)
                    .build())
            .summonData(SummonData.of(JSoundRegistry.STAND_SUMMON))
            .build();

/*    public static final AbsoluteDefenseMove ABSOLUTE_DEFENSE = new AbsoluteDefenseMove()
blah blah blah insert random bs here */

    public TCBEntity(Level worldIn) {
        super(JStandTypeRegistry.TCB.get(), worldIn);
        this.setDistanceOffset(0.0f);
        this.setRotationOffset(0.0f); // No rotation offset
        auraColors = new Vector3f[]{
                new Vector3f(0f, 0f, 0f),
                new Vector3f(0f, 0f, 0f),
                new Vector3f(0f, 0f, 0f),
                new Vector3f(0f, 0f, 0f),
        };
    }

    private static void registerMoves(MoveMap<TCBEntity, TCBEntity.State> moves) {

    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    public State getBlockState() {
        return null;
    }



    @Override
    protected String getSummonAnimation() {
        return null; // No summon animation for now
    }

    @Override
    public @NotNull TCBEntity getThis() {
        return this;
    }

    @Override
    public void tick() {
        super.tick();

        // Always follow the user exactly like armor would
        if (hasUser() && !isRemote()) {
            LivingEntity user = getUserOrThrow();

            // Perfect position sync
            this.setPos(user.position());
            this.setOldPosAndRot();

            // Perfect rotation sync
            this.setYRot(user.getYRot());
            this.setXRot(user.getXRot());
            this.setYHeadRot(user.getYHeadRot());
            this.setYBodyRot(user.yBodyRot);

            // Copy all movement properties
            this.setDeltaMovement(user.getDeltaMovement());
            this.fallDistance = user.fallDistance;
            this.setOnGround(user.onGround());

        }
    }

    @Override
    public boolean canAttack() {
        if (wantToBlock) {
            wantToBlock = false;
        }
        return super.canAttack(); //disable blocking. absolute defense will be the block ass opposed to the regular stand block
    }

    @Override
    public void tryBlock() {

    }

    @Override
    public boolean shouldOffsetHeight() {
        return false; // Always stay at exact user position
    }

    @Override
    public void idleOverride() {
        // Always stay exactly on the user
        setDistanceOffset(0.0f);
        setRotationOffset(0.0f);
    }

    public enum State implements net.arna.jcraft.common.util.StandAnimationState<TCBEntity> {
        ABSOLUTE_DEFENSE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("idle")));

        private final Consumer<AnimationState<TCBEntity>> animator;

        State(Consumer<AnimationState<TCBEntity>> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(TCBEntity attacker, AnimationState<TCBEntity> builder) {
            animator.accept(builder);
        }
    }
}