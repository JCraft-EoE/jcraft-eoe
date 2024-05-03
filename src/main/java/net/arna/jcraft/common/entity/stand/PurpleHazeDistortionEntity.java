package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.StunType;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

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

    public static final PilotModeMove<PurpleHazeDistortionEntity> PILOT_MODE = new PilotModeMove<PurpleHazeDistortionEntity>(20)
            .withInfo(
                    Text.literal("Pilot Mode"),
                    Text.literal("5m range")
            );

    public static final NoOpMove<PurpleHazeDistortionEntity> DISTORTION = new NoOpMove<PurpleHazeDistortionEntity>(20, 0, 0)
            .withInitAction((attacker, user, ctx) -> attacker.nextPoisonType())
            .withCrouchingVariant(PILOT_MODE)
            .withInfo(
                    Text.literal("Distortion"),
                    Text.literal("""
                            Toggles virus effects between:
                            Harming - standard effect, deals damage over time
                            Nullifying - removes status effects
                            Debilitating - gives blindness and slowness""")
            );

    public static final SimpleAttack<AbstractPurpleHazeEntity<?, ?>> GRAB_HIT_FINAL = new SimpleAttack<AbstractPurpleHazeEntity<?, ?>>(0, 27,
            34, 0.75f, 4f, 8, 2f, 1.25f, 0f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(
                    Text.literal("Grab (Final Hit)"),
                    Text.empty()
            );
    public static final SimpleMultiHitAttack<AbstractPurpleHazeEntity<?, ?>> GRAB_HIT = new SimpleMultiHitAttack<AbstractPurpleHazeEntity<?, ?>>(0,
            34, 0.75f, 1f, 10, 2f, 0f, 0f, IntSet.of(6, 8, 10, 12, 14, 16, 18))
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withStunType(StunType.UNBURSTABLE)
            .withFinisher(19, GRAB_HIT_FINAL)
            .withInfo(
                    Text.literal("Grab (Final Hit)"),
                    Text.empty()
            );
    public static final GrabAttack<PurpleHazeDistortionEntity, State> GRAB = new GrabAttack<>(
            280, 12, 24, 0.75f, 0f, 45, 1.5f, 0f, 0f,
            GRAB_HIT, State.GRAB_HIT, 25, 1)
            .withCrouchingVariant(GROUND_SLAM)
            .withSound(JSoundRegistry.D4C_THROW)
            .withImpactSound(JSoundRegistry.PH_GRAB_HIT)
            .withInfo(
                    Text.literal("Grab"),
                    Text.literal("unblockable, combo finisher")
            );

    public PurpleHazeDistortionEntity(World worldIn) {
        super(StandType.PURPLE_HAZE_DISTORTION, worldIn);

        freespace += """
                PASSIVE: 66% resistance to Purple Haze effect""";

        auraColors = new Vector3f[]{
                new Vector3f(0.8f, 0.2f, 1.0f),
                new Vector3f(1.0f, 0.2f, 0.6f),
                new Vector3f(0.2f, 0.8f, 0.6f),
                new Vector3f(1.0f, 0.3f, 0.5f)
        };
    }

    @Override
    protected void registerMoves(MoveMap<PurpleHazeDistortionEntity, State> moves) {
        MoveMap.Entry<PurpleHazeDistortionEntity, State> light = moves.register(MoveType.LIGHT, LIGHT_ATTACK, State.PUNCH);
        light.withFollowUp(State.LIGHT_FOLLOWUP);
        light.withCrouchingVariant(State.BACKHAND).withFollowUp(State.BACKHAND_FOLLOWUP);

        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);
        moves.register(MoveType.HEAVY, HEAVY, State.HEAVY);

        moves.register(MoveType.SPECIAL1, LAUNCH_CAPSULE, State.LAUNCH).withCrouchingVariant(State.LAUNCH2);
        moves.register(MoveType.SPECIAL2, REKKA_1, State.REKKA1);
        moves.register(MoveType.SPECIAL3, GRAB, State.GRAB).withCrouchingVariant(State.GROUND_SLAM);

        moves.register(MoveType.ULTIMATE, FULL_RELEASE, State.FULL_RELEASE);

        moves.register(MoveType.UTILITY, DISTORTION).withCrouchingVariant(CooldownType.UTILITY, null);
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
    protected void tickRemoteState(double f, double s, boolean dashing) {
        if (getState() == State.IDLE) { // Replace idle anim
            if (s > 0) setStateNoReset(dashing ? State.RIGHT : State.RIGHT_DASH);
            if (s < 0) setStateNoReset(dashing ? State.LEFT : State.LEFT_DASH);
            if (f < 0) setStateNoReset(dashing ? State.BACKWARD : State.BACKWARD_DASH);
            if (f > 0) setStateNoReset(dashing ? State.FORWARD : State.FORWARD_DASH);
        }
    }

    @Override
    @NonNull
    public PurpleHazeDistortionEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<PurpleHazeDistortionEntity> {
        IDLE((PurpleHaze, builder) -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.purple_haze.idle"))),
        PUNCH(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.purple_haze.light"))),
        BLOCK(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.purple_haze.block"))),
        HEAVY(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.purple_haze.heavy"))),

        FULL_RELEASE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.purple_haze.full_release"))),
        GROUND_SLAM(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.purple_haze.ground_slam"))),

        BARRAGE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.purple_haze.barrage"))),
        LAUNCH(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.purple_haze.launch"))),
        LAUNCH2(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.purple_haze.launch2"))),

        REKKA1(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.purple_haze.rekka1"))),
        REKKA2(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.purple_haze.rekka2"))),
        REKKA3(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.purple_haze.rekka3"))),

        GRAB(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.purple_haze.grab"))),
        GRAB_HIT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.purple_haze.grab_hit"))),

        BACKHAND(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.purple_haze.backhand"))),
        BACKHAND_FOLLOWUP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.purple_haze.backhand_followup"))),
        LIGHT_FOLLOWUP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.purple_haze.light_followup"))),

        FORWARD(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.purple_haze.forw"))),
        BACKWARD(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.purple_haze.back"))),
        LEFT(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.purple_haze.left"))),
        RIGHT(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.purple_haze.right"))),
        FORWARD_DASH(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.purple_haze.fdash"))),
        BACKWARD_DASH(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.purple_haze.bdash"))),
        LEFT_DASH(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.purple_haze.ldash"))),
        RIGHT_DASH(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.purple_haze.rdash"))),;

        private final BiConsumer<PurpleHazeDistortionEntity, AnimationState> animator;

        State(Consumer<AnimationState> animator) {
            this((silverChariot, builder) -> animator.accept(builder));
        }

        State(BiConsumer<PurpleHazeDistortionEntity, AnimationState> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(PurpleHazeDistortionEntity attacker, AnimationState state) {
            animator.accept(attacker, state);
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
