package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JMarkerExtractorRegistry;
import net.arna.jcraft.api.registry.JMarkerInjectorRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.common.attack.moves.mandom.CountdownMove;
import net.arna.jcraft.common.attack.moves.mandom.RewindMove;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import static net.arna.jcraft.JCraft.QUEUE_MOVESTUN_LIMIT;
import static net.arna.jcraft.JCraft.SPEC_QUEUE_MOVESTUN_LIMIT;

public class MandomEntity extends StandEntity<MandomEntity, MandomEntity.State> {
    public static final MoveSet<MandomEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.MANDOM,
            MandomEntity::registerMoves, State.class);

    public static final StandData DATA = StandData.builder()
            .idleDistance(0f)
            .idleRotation(0f)
            .blockDistance(0f)
            .info(StandInfo.builder()
                    .freeSpace(Component.literal("""
                        Mandom is a non-combat stand. its purpose
                        is to trap and reset things
                        that happen or maybe what will happen"""))


                    .name(Component.translatable("entity.jcraft.mandom"))
                    .proCount(2)
                    .conCount(4)
                    .skinName(Component.literal("Phase-Shift"))
                    .skinName(Component.literal("Aperture"))
                    .skinName(Component.literal("Abstract"))
                    .build())
            .summonData(SummonData.builder()
                    .sound(JSoundRegistry.MANDOM_SUMMON)
                    .build())
            .build();

    public static final CountdownMove COUNTDOWN = new CountdownMove(6, 10, 120, 0f, 64, 600, CountdownMove.ENTITY_STUFF_TO_SAVE, JMarkerExtractorRegistry.ALL.get(), JMarkerInjectorRegistry.ALL.get())
            .withSound(JSoundRegistry.MANDOM_COUNTDOWN)
            .withInfo(
                    Component.literal("Countdown"),
                    Component.literal("Saves position data of all entities in a 64 block radius for 30 seconds. Must be active to use Rewind."));

    public static final RewindMove REWIND = new RewindMove(6, 5, 10, 0f, 200)
            .withSound(JSoundRegistry.MANDOM_REWIND)
            .withInfo(
                    Component.literal("Rewind"),
                    Component.literal("Returns all entities to their saved positions. Requires Countdown to be active."));

    public MandomEntity(Level worldIn) {
        super(JStandTypeRegistry.MANDOM.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(1.0f, 0.2f, 0.6f),
                new Vector3f(0.6f, 0.2f, 1.0f),
                new Vector3f(0.7f, 0.7f, 0.7f),
                new Vector3f(1.0f, 1.0f, 0.2f)
        };
    }

    private static void registerMoves(MoveMap<MandomEntity, State> moves) {
        moves.register(MoveClass.ULTIMATE, REWIND, State.REWIND);
        moves.register(MoveClass.UTILITY, COUNTDOWN, State.COUNTDOWN);
    }

    @Override
    public boolean canAttack() {
        if (wantToBlock) {
            wantToBlock = false;
        }
        return super.canAttack();
    }

    @Override
    public void tryBlock() {
        // Do nothing - Mandom cannot block
    }

    @Override
    public void onUserMoveInput(AbstractMove<?, ? super MandomEntity> currentMove, MoveInputType type, boolean pressed, boolean moveInitiated) {
        if (!pressed) return;

        MoveClass moveClass = type.getMoveClass(standby);
        if (moveClass == null) return;

        // Special check for ULTIMATE (Rewind) - only allow if countdown is active
        if (moveClass == MoveClass.ULTIMATE) {
            CountdownMove countdownMove = getMove(CountdownMove.class);

            if (countdownMove == null || !countdownMove.isCountdownActive()) {
                // Countdown is not active, don't allow the ultimate
                return;
            }
        }

        if (moveClass == MoveClass.ULTIMATE || moveClass == MoveClass.UTILITY) {
            if (canAttack()) {
                initMove(moveClass);
            } else if (getMoveStun() > 0 && getMoveStun() < QUEUE_MOVESTUN_LIMIT) {
                queueMove(type);
            }
            return;
        }

        if (hasUser() && getUser() instanceof Player player) {
            JSpec<?, ?> spec = JUtils.getSpec(player);
            if (spec != null && spec.canAttack()) {
                if (spec.initMove(moveClass)) {
                    // Move was successful
                } else if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT) {
                    spec.queuedMove = type;
                }
            }
        }
    }

    @Override
    public AbstractMove<?, ? super MandomEntity> getFallbackMove() {
        return null;
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        // Special check for ULTIMATE (Rewind) - only allow if countdown is active
        if (moveClass == MoveClass.ULTIMATE) {
            CountdownMove countdownMove = getMove(CountdownMove.class);

            if (countdownMove == null || !countdownMove.isCountdownActive()) {
                // Countdown is not active, don't allow the ultimate
                return false;
            }
        }

        return super.initMove(moveClass);
    }

    @Override
    public boolean allowMoveHandling() {
        return true;
    }

    @Override
    @NonNull
    public MandomEntity getThis() {
        return this;
    }

    public enum State implements StandAnimationState<MandomEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "summon", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        COUNTDOWN(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "timer", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        REWIND(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "rewind", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "idle", AzPlayBehaviors.LOOP));

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(MandomEntity attacker) {
            animator.sendForEntity(attacker);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    public State getBlockState() {
        return State.IDLE;
    }

}