package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.StateContainer;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.common.attack.moves.purplehaze.BackhandAttack;
import net.arna.jcraft.common.attack.moves.purplehaze.PHRekkaAttack;
import net.arna.jcraft.common.attack.moves.purplehaze.distortion.DistortionMove;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/Purple_Haze_Distortion">Purple Haze Distortion</a>.
 * @see JStandTypeRegistry#PURPLE_HAZE_DISTORTION
 */
public final class PurpleHazeDistortionEntity extends AbstractPurpleHazeEntity<PurpleHazeDistortionEntity, PurpleHazeDistortionEntity.State> {
    public static final MoveSet<PurpleHazeDistortionEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.PURPLE_HAZE_DISTORTION,
            PurpleHazeDistortionEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.builder()
            .idleRotation(225f)
            .evolution(true)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.purple_haze_distortion"))
                    .proCount(3)
                    .conCount(3)
                    .freeSpace(Component.literal("""
                    PASSIVE: 66% resistance to Purple Haze effect
                    
                    BNBs:
                    Light > Rekka1~Rekka2 > crouching Light > Barrage >...
                        ...crouching Light~Light
                        ...Ground Slam
                        ...Light > Grab"""))
                    .skinName(Component.literal("Black Knight"))
                    .skinName(Component.literal("Vintage"))
                    .skinName(Component.literal("Reversal"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.PH_SUMMON))
            .build();

    private static final @NonNull KnockdownAttack<AbstractPurpleHazeEntity<?, ?>> CROUCHING_LIGHT_FOLLOWUP_ATTACK = BACKHAND_FOLLOWUP.copy().withAnim(State.BACKHAND_FOLLOWUP);
    private static final @NonNull BackhandAttack CROUCHING_LIGHT_ATTACK = BACKHAND.copy().withFollowup(CROUCHING_LIGHT_FOLLOWUP_ATTACK);
    private static final @NonNull SimpleAttack<AbstractPurpleHazeEntity<?, ?>> LIGHT_FOLLOWUP_ATTACK = LIGHT_FOLLOWUP.copy().withAnim(State.LIGHT_FOLLOWUP);
    private static final @NonNull SimpleAttack<AbstractPurpleHazeEntity<?, ?>> LIGHT_ATTACK = LIGHT.copy().withFollowup(LIGHT_FOLLOWUP_ATTACK).withCrouchingVariant(CROUCHING_LIGHT_ATTACK);
    private static final @NonNull KnockdownAttack<AbstractPurpleHazeEntity<?, ?>> REKKA_3 = REKKA3.copy().withAnim(State.REKKA3);
    private static final @NonNull SimpleAttack<AbstractPurpleHazeEntity<?, ?>> REKKA_2 = REKKA2.copy().withAnim(State.REKKA2).withFollowup(REKKA_3);
    private static final @NonNull PHRekkaAttack REKKA_1 = REKKA1.copy().withAnim(State.REKKA1).withFollowup(REKKA_2);

    public static final PilotModeMove<PurpleHazeDistortionEntity> PILOT_MODE = new PilotModeMove<PurpleHazeDistortionEntity>(20)
            .withInfo(
                    Component.literal("Pilot Mode"),
                    Component.literal("5m range")
            );

    public static final DistortionMove DISTORTION = new DistortionMove(20)
            .withCrouchingVariant(PILOT_MODE)
            .withInfo(
                    Component.literal("Distortion"),
                    Component.literal("""
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
                    Component.literal("Grab (Final Hit)"),
                    Component.empty()
            );
    public static final SimpleMultiHitAttack<AbstractPurpleHazeEntity<?, ?>> GRAB_HIT = new SimpleMultiHitAttack<AbstractPurpleHazeEntity<?, ?>>(0,
            34, 0.75f, 1f, 10, 2f, 0f, 0f, IntSet.of(6, 8, 10, 12, 14, 16, 18))
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withStunType(StunType.UNBURSTABLE)
            .withFinisher(19, GRAB_HIT_FINAL)
            .withInfo(
                    Component.literal("Grab (Final Hit)"),
                    Component.empty()
            );
    public static final GrabAttack<PurpleHazeDistortionEntity, State> GRAB = new GrabAttack<>(
            280, 12, 24, 0.75f, 0f, 45, 1.5f, 0f, 0f,
            GRAB_HIT, StateContainer.of(State.GRAB_HIT), 25, 1)
            .withCrouchingVariant(GROUND_SLAM)
            .withSound(JSoundRegistry.D4C_THROW)
            .withImpactSound(JSoundRegistry.PH_GRAB_HIT)
            .withInfo(
                    Component.literal("Grab"),
                    Component.literal("unblockable, combo finisher")
            );

    public PurpleHazeDistortionEntity(Level worldIn) {
        super(JStandTypeRegistry.PURPLE_HAZE_DISTORTION.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(0.8f, 0.2f, 1.0f),
                new Vector3f(1.0f, 0.2f, 0.6f),
                new Vector3f(0.2f, 0.8f, 0.6f),
                new Vector3f(1.0f, 0.3f, 0.5f)
        };
    }

    private static void registerMoves(MoveMap<PurpleHazeDistortionEntity, State> moves) {
        MoveMap.Entry<PurpleHazeDistortionEntity, State> light = moves.register(MoveClass.LIGHT, LIGHT_ATTACK, State.PUNCH);
        light.withFollowup(State.LIGHT_FOLLOWUP);
        light.withCrouchingVariant(State.BACKHAND).withFollowup(State.BACKHAND_FOLLOWUP);

        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE);
        moves.register(MoveClass.HEAVY, HEAVY, State.HEAVY);

        moves.register(MoveClass.SPECIAL1, LAUNCH_CAPSULE, State.LAUNCH).withCrouchingVariant(State.LAUNCH2);
        moves.register(MoveClass.SPECIAL2, REKKA_1, State.REKKA1).withFollowup(State.REKKA2).withFollowup(State.REKKA3);
        moves.register(MoveClass.SPECIAL3, GRAB, State.GRAB).withCrouchingVariant(State.GROUND_SLAM);

        moves.register(MoveClass.ULTIMATE, FULL_RELEASE, State.FULL_RELEASE);

        moves.register(MoveClass.UTILITY, DISTORTION).withCrouchingVariant(CooldownType.UTILITY, null);
    }

    @Override
    protected void tickRemoteState(double f, double s, boolean dashing) {
        LivingEntity user = getUserOrThrow();

        if (getMoveStun() <= 0) {
            if (JUtils.canAct(user)) {
                if (f == 0) {
                    if (s > 0) {
                        setStateNoReset(onGround() ? State.RIGHT : State.RIGHT_DASH);
                    } else if (s < 0) {
                        setStateNoReset(onGround() ? State.LEFT : State.LEFT_DASH);
                    } else {
                        setStateNoReset(State.IDLE);
                    }
                } else {
                    if (f < 0) {
                        setStateNoReset(onGround() ? State.BACKWARD : State.BACKWARD_DASH);
                    }
                    if (f > 0) {
                        setStateNoReset(onGround() ? State.FORWARD : State.FORWARD_DASH);
                    }
                }
            }
        }
    }

    @Override
    @NonNull
    public PurpleHazeDistortionEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<PurpleHazeDistortionEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.purple_haze.idle", AzPlayBehaviors.LOOP)),
        PUNCH(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.purple_haze.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.purple_haze.block", AzPlayBehaviors.LOOP)),
        HEAVY(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.purple_haze.heavy", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),

        FULL_RELEASE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.purple_haze.full_release", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        GROUND_SLAM(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.purple_haze.ground_slam", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),

        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.purple_haze.barrage", AzPlayBehaviors.LOOP)),
        LAUNCH(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.purple_haze.launch", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LAUNCH2(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.purple_haze.launch2", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),

        REKKA1(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.purple_haze.rekka1", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        REKKA2(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.purple_haze.rekka2", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        REKKA3(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.purple_haze.rekka3", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),

        GRAB(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.purple_haze.grab", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        GRAB_HIT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.purple_haze.grab_hit", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),

        BACKHAND(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.purple_haze.backhand", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BACKHAND_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.purple_haze.backhand_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.purple_haze.light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),

        FORWARD(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.purple_haze.forw", AzPlayBehaviors.LOOP)),
        BACKWARD(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.purple_haze.back", AzPlayBehaviors.LOOP)),
        LEFT(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.purple_haze.left", AzPlayBehaviors.LOOP)),
        RIGHT(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.purple_haze.right", AzPlayBehaviors.LOOP)),
        FORWARD_DASH(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.purple_haze.fdash", AzPlayBehaviors.LOOP)),
        BACKWARD_DASH(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.purple_haze.bdash", AzPlayBehaviors.LOOP)),
        LEFT_DASH(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.purple_haze.ldash", AzPlayBehaviors.LOOP)),
        RIGHT_DASH(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.purple_haze.rdash", AzPlayBehaviors.LOOP)),
        ;

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(PurpleHazeDistortionEntity attacker) {
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
