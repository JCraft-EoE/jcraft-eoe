package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.shared.KnockdownAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.UppercutAttack;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class PurpleHazeDistortionEntity extends AbstractPurpleHazeEntity<PurpleHazeDistortionEntity, PurpleHazeDistortionEntity.State> {
    private static final @NonNull KnockdownAttack<AbstractPurpleHazeEntity<?, ?>> CROUCHING_LIGHT_FOLLOWUP_ATTACK = BACKHAND_FOLLOWUP.copy().withAnim(State.BACKHAND_FOLLOWUP);
    private static final @NonNull UppercutAttack<AbstractPurpleHazeEntity<?, ?>> CROUCHING_LIGHT_ATTACK = BACKHAND.copy().withFollowup(CROUCHING_LIGHT_FOLLOWUP_ATTACK);
    private static final @NonNull SimpleAttack<AbstractPurpleHazeEntity<?, ?>> LIGHT_FOLLOWUP_ATTACK = LIGHT_FOLLOWUP.copy().withAnim(State.LIGHT_FOLLOWUP);
    private static final @NonNull SimpleAttack<AbstractPurpleHazeEntity<?, ?>> LIGHT_ATTACK = LIGHT.copy().withFollowup(LIGHT_FOLLOWUP_ATTACK).withCrouchingVariant(CROUCHING_LIGHT_ATTACK);
    private static final @NonNull KnockdownAttack<AbstractPurpleHazeEntity<?, ?>> REKKA_3 = REKKA3.copy().withAnim(State.REKKA3);
    private static final @NonNull SimpleAttack<AbstractPurpleHazeEntity<?, ?>> REKKA_2 = REKKA2.copy().withAnim(State.REKKA2).withFollowup(REKKA_3);
    private static final @NonNull SimpleAttack<AbstractPurpleHazeEntity<?, ?>> REKKA_1 = REKKA1.copy().withAnim(State.REKKA1).withFollowup(REKKA_2);

    public PurpleHazeDistortionEntity(World worldIn) {
        super(StandType.PURPLE_HAZE_DISTORTION, worldIn);
        auraColors = new Vec3f[]{
                new Vec3f(0.8f, 0.2f, 1.0f),
                new Vec3f(1.0f, 0.2f, 0.6f),
                new Vec3f(0.2f, 0.8f, 0.6f),
                new Vec3f(1.0f, 0.3f, 0.5f)
        };
    }

    @Override
    protected void registerMoves(MoveMap<PurpleHazeDistortionEntity, State> moves) {
        MoveMap.Entry<PurpleHazeDistortionEntity, State> light = moves.register(MoveType.LIGHT, LIGHT_ATTACK, State.PUNCH);
        light.withFollowUp(State.LIGHT_FOLLOWUP);
        light.withCrouchingVariant(State.BACKHAND).withFollowUp(State.BACKHAND_FOLLOWUP);

        //moves.register(MoveType.HEAVY, STAR_BREAKER, State.HEAVY).withCrouchingVariant(State.GROUND_BREAKER);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, LAUNCH_CAPSULE, State.LAUNCH).withCrouchingVariant(State.LAUNCH2);
        moves.register(MoveType.SPECIAL2, REKKA_1, State.REKKA1);
        moves.register(MoveType.SPECIAL3, GROUND_SLAM, State.GROUND_SLAM);
    }

    @Override
    public boolean initMove(MoveType type) {
        if (type == MoveType.LIGHT && curMove != null && curMove.getMoveType() == MoveType.LIGHT && getMoveStun() < curMove.getWindupPoint()) {
            AbstractMove<?, ? super PurpleHazeDistortionEntity> followup = curMove.getFollowup();
            if (followup != null) {
                setMove(followup, (State) followup.getAnimation());
                return true;
            }
        }

        return super.initMove(type);
    }

    @Override
    @NonNull
    public PurpleHazeDistortionEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<PurpleHazeDistortionEntity> {
        IDLE((PurpleHaze, builder) -> builder.loop("animation.purple_haze.idle")),
        PUNCH(builder -> builder.playAndHold("animation.purple_haze.light")),
        BLOCK(builder -> builder.loop("animation.purple_haze.block")),
        HEAVY(builder -> builder.playAndHold("animation.purple_haze.heavy")),
        GROUND_SLAM(builder -> builder.playAndHold("animation.purple_haze.ground_slam")),
        BARRAGE(builder -> builder.loop("animation.purple_haze.barrage")),
        LAUNCH(builder -> builder.playAndHold("animation.purple_haze.launch")),
        LAUNCH2(builder -> builder.playAndHold("animation.purple_haze.launch2")),

        REKKA1(builder -> builder.playAndHold("animation.purple_haze.rekka1")),
        REKKA2(builder -> builder.playAndHold("animation.purple_haze.rekka2")),
        REKKA3(builder -> builder.playAndHold("animation.purple_haze.rekka3")),

        BACKHAND(builder -> builder.playAndHold("animation.purple_haze.backhand")),
        BACKHAND_FOLLOWUP(builder -> builder.playAndHold("animation.purple_haze.backhand_followup")),
        LIGHT_FOLLOWUP(builder -> builder.playAndHold("animation.purple_haze.light_followup"));

        private final BiConsumer<PurpleHazeDistortionEntity, AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this((silverChariot, builder) -> animator.accept(builder));
        }

        State(BiConsumer<PurpleHazeDistortionEntity, AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(PurpleHazeDistortionEntity attacker, AnimationBuilder builder) {
            animator.accept(attacker, builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @NotNull String getSummonAnimation() {
        return "animation.purple_haze.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
