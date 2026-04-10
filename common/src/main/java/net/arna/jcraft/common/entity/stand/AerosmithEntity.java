package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.common.attack.moves.aerosmith.BombDropAttack;
import net.arna.jcraft.common.attack.moves.aerosmith.MuzzleHitscanAttack;
import net.arna.jcraft.common.attack.moves.aerosmith.PatrolMove;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class AerosmithEntity extends StandEntity<AerosmithEntity, AerosmithEntity.State> {
    public static final MoveSet<AerosmithEntity, AerosmithEntity.State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.AEROSMITH,
            AerosmithEntity::registerDefaultMoves, AerosmithEntity.State.class);

    // TODO Arna balance this
    public static final MuzzleHitscanAttack<AerosmithEntity> BULLET = new MuzzleHitscanAttack<AerosmithEntity>(
            1, 1, 2, 0f, 1f, 0, 0f, 30f, 10f, 1/6f, 0.05f)
            .withSound(JSoundRegistry.BULLET_PENETRATE) // TODO record improve
            .withHitSpark(JParticleType.HIT_SPARK_2) // TODO record improve
            .withShootSpark(JParticleType.HIT_SPARK_1); // TODO record improve // TODO Planet why isn't this working?
    // TODO Arna description

    // TODO Arna balance this
    public static final BombDropAttack<AerosmithEntity> BOMB_DROP = new BombDropAttack<>(
            200, 1, 100, 0f, 30f);
    // TODO Arna description

    // TODO Arna balance this
    public static final PatrolMove<AerosmithEntity> PATROL = new PatrolMove<>(
            200, 1, 100, 0f, 30f, 10f, 0.5f);
    // TODO Arna description

    public static final StandData DATA = StandData.builder()
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.aerosmith"))
                    .build())
            .build();

    public AerosmithEntity(final Level world) {
        super(JStandTypeRegistry.AEROSMITH.get(), world);
//        setYDistanceOffset(10f); // TODO for patrol mode
        setYDistanceOffset(1.2f);
        setNoGravity(true);
    }

    @Override
    public boolean remoteControllable() {
        return false;
    }

    @Override
    public void playSummonAnimation() {
        // intentionally left empty // TODO remove this override
    }

    @Override
    public @NonNull AerosmithEntity getThis() {
        return this;
    }

    private static void registerDefaultMoves(final @NonNull MoveMap<AerosmithEntity, AerosmithEntity.State> moves) {
        moves.registerImmediate(MoveClass.LIGHT, BULLET, State.LIGHT);
        moves.registerImmediate(MoveClass.HEAVY, BOMB_DROP, State.IDLE);
        moves.registerImmediate(MoveClass.UTILITY, PATROL, State.IDLE);
    }

    public enum State implements StandAnimationState<AerosmithEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "idle", AzPlayBehaviors.LOOP)),
        LIGHT(AzCommand.create(JCraft.BASE_CONTROLLER, "light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "block", AzPlayBehaviors.LOOP))
        ;

        private final AzCommand animator;

        State(final @NonNull AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(final @NonNull AerosmithEntity attacker) {
            animator.sendForEntity(attacker);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
