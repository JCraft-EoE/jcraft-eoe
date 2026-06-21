package net.arna.jcraft.common.entity.stand;

import lombok.Getter;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.api.stand.*;
import net.arna.jcraft.common.attack.moves.tcb.AbsoluteDefenseMove;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

@Getter
public class TCBEntity extends StandEntity<TCBEntity, TCBEntity.State> {

    public static final MoveSet<TCBEntity, TCBEntity.State> MOVE_SET = MoveSetManager.create(
            JStandTypeRegistry.TCB, TCBEntity::registerMoves, TCBEntity.State.class);

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

    public static final AbsoluteDefenseMove ABSOLUTE_DEFENSE =
            new AbsoluteDefenseMove(0, 7, Integer.MAX_VALUE, 0f)
                    .withInfo(
                            Component.literal("Absolute Defense"),
                            Component.literal("Hold ULTIMATE to activate an impenetrable barrier. Any damage received is reflected back at 75% power.")
                    );

    private static final EntityDataAccessor<Boolean> ABSOLUTE_DEFENSE_ACTIVE =
            SynchedEntityData.defineId(TCBEntity.class, EntityDataSerializers.BOOLEAN);

    private int defenseTicks = 0;

    public TCBEntity(Level worldIn) {
        super(JStandTypeRegistry.TCB.get(), worldIn);
        this.setDistanceOffset(0.0f);
        this.setRotationOffset(0.0f);
        auraColors = new Vector3f[]{
                new Vector3f(0f, 0f, 0f),
                new Vector3f(0f, 0f, 0f),
                new Vector3f(0f, 0f, 0f),
                new Vector3f(0f, 0f, 0f),
        };
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ABSOLUTE_DEFENSE_ACTIVE, false);
    }

    public boolean isAbsoluteDefenseActive() {
        return this.entityData.get(ABSOLUTE_DEFENSE_ACTIVE);
    }

    public void setAbsoluteDefenseActive(boolean active) {
        this.entityData.set(ABSOLUTE_DEFENSE_ACTIVE, active);
    }

    public void incrementDefenseTicks() {
        this.defenseTicks++;
    }

    public void resetDefenseTicks() {
        this.defenseTicks = 0;
    }

    private static void registerMoves(MoveMap<TCBEntity, TCBEntity.State> moves) {
        moves.register(MoveClass.ULTIMATE, ABSOLUTE_DEFENSE, State.ABSOLUTE_DEFENSE);
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }

    @Override
    public @NotNull TCBEntity getThis() {
        return this;
    }

    @Override
    public void onUserMoveInput(AbstractMove<?, ? super TCBEntity> currentMove, MoveInputType type, boolean pressed, boolean moveInitiated) {
        // Let releases propagate to the current move (e.g., AbsoluteDefense cancels on release)
        if (!pressed) {
            super.onUserMoveInput(currentMove, type, pressed, moveInitiated);
            return;
        }

        MoveClass moveClass = type.getMoveClass();
        if (moveClass == null) return;

        // ULTIMATE is TCB's own ability (Absolute Defense)
        if (moveClass == MoveClass.ULTIMATE) {
            super.onUserMoveInput(currentMove, type, pressed, moveInitiated);
            return;
        }

        // All other inputs (LIGHT, HEAVY, BARRAGE, SPECIAL, etc.) go to the spec
        if (hasUser() && getUser() instanceof Player player) {
            JSpec<?, ?> spec = JUtils.getSpec(player);
            if (spec != null && spec.canAttack()) {
                if (!spec.initMove(moveClass) && spec.moveStun > 0 && spec.moveStun < JCraft.SPEC_QUEUE_MOVESTUN_LIMIT) {
                    spec.queuedMove = type;
                }
            }
        }
    }

    @Override
    public boolean hurt(final @NotNull DamageSource source, float amount) {
        if (isAbsoluteDefenseActive()
                && !source.is(DamageTypes.FELL_OUT_OF_WORLD)
                && !source.is(DamageTypes.GENERIC_KILL)) {
            onUserHurt(source, amount);
            return false;
        }
        return super.hurt(source, amount);
    }

    public void onUserHurt(DamageSource source, float amount) {
        if (getCurrentMove() instanceof AbsoluteDefenseMove move) {
            move.handleUserHurt(this, source, amount);
        }
    }

    @Override
    public boolean shouldOffsetHeight() {
        return false;
    }

    @Override
    public void idleOverride() {
        setDistanceOffset(0.0f);
        setRotationOffset(0.0f);
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity user = getUser();
        if (user != null) {
            this.setPos(user.getX(), user.getY(), user.getZ());
            this.setPose(user.getPose());
            this.setYRot(user.yBodyRot);
            this.setYHeadRot(user.getYHeadRot());
            this.setYBodyRot(user.yBodyRot);
            if (!level().isClientSide()) {
                this.setDeltaMovement(user.getDeltaMovement());
                this.fallDistance = user.fallDistance;
                this.setOnGround(user.onGround());
            }
        }
    }

    public enum State implements StandAnimationState<TCBEntity> {
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.tcb.block_idle", AzPlayBehaviors.LOOP)),
        ABSOLUTE_DEFENSE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.tcb.block", AzPlayBehaviors.HOLD_ON_LAST_FRAME));

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(TCBEntity attacker) {
            animator.sendForEntity(attacker);
        }
    }
}
