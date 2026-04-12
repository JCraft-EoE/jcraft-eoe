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
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.attack.moves.thehand.*;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3f;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/The_Hand">The Hand</a>.
 * @see JStandTypeRegistry#THE_HAND
 * @see net.arna.jcraft.client.renderer.entity.stands.TheHandRenderer TheHandRenderer
 */
public class TheHandEntity extends StandEntity<TheHandEntity, TheHandEntity.State> {
    public static final MoveSet<TheHandEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.THE_HAND,
            TheHandEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.builder()
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.the_hand"))
                    .proCount(4)
                    .conCount(2)
                    .freeSpace(Component.literal("""
                            BNBs:
                                -the stand up comedian
                                Light>Barrage>Sweep>cr.M1~M1>Stomp Barrage
                            
                                -the st. louis devastator
                                (Sweep>)cr.M1~M1>Barrage>Rage"""))
                    .skinName(Component.literal("Shift"))
                    .skinName(Component.literal("Inversion"))
                    .skinName(Component.literal("Deletion"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.THE_HAND_SUMMON))
            .build();

    public static final Stomp2Attack CROUCHING_LIGHT_FOLLOWUP = new Stomp2Attack(0,
            13, 20, 0.6f, 6f, 15, 1.75f, 0.3f, 0.4f, -0.3f)
            .withAnim(State.CROUCHING_LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withExtraHitBox(0, 0, 1)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Stomp (Second Hit)"),
                    Component.literal("Lifts knocked down enemies off the ground.")
            );
    public static final SimpleAttack<TheHandEntity> CROUCHING_LIGHT = new SimpleAttack<TheHandEntity>(JCraft.LIGHT_COOLDOWN,
            9, 14, 0.5f, 5f, 15, 1.5f, 0.25f, 0.4f)
            .withFollowup(CROUCHING_LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.LOW)
            .withBlockStun(12)
            .withInfo(
                    Component.literal("Stomp"),
                    Component.literal("""
                                            Relatively quick combo starter.
                                            Shorter range.
                                            High blockstun.""")
            );
    public static final SimpleUppercutAttack<TheHandEntity> LIGHT_FOLLOWUP = new SimpleUppercutAttack<TheHandEntity>(
            0, 9, 14, 0.75f, 6f, 8, 1.6f, 0.3f, -0.1f, 0.3f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withBlockStun(4)
            .withExtraHitBox(0, 0, 1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Gut Punch"),
                    Component.empty());
    public static final SimpleAttack<TheHandEntity> LIGHT = new SimpleAttack<TheHandEntity>(JCraft.LIGHT_COOLDOWN,
            5, 10, 0.75f, 4f, 12, 1.5f, 0.25f, -0.1f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(CROUCHING_LIGHT)
            .withImpactSound(JSoundRegistry.IMPACT_6)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .noLoopPrevention()
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("Relatively quick combo starter."));

    public static final KnockdownAttack<TheHandEntity> STOMP_BARRAGE_FINISHER = new KnockdownAttack<TheHandEntity>(0,
            6, 10, 1.0f, 6f, 6, 1.75f, 0.5f, 0.3f, 35)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withExtraHitBox(0, 0, 1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Stomp Barrage (Last Hit)"),
                    Component.literal("Lifts knocked down enemies off the ground.")
            );
    public static final SimpleMultiHitAttack<TheHandEntity> STOMP_BARRAGE = new SimpleMultiHitAttack<TheHandEntity>(100,
            33, 1.0f, 1.0f, 10, 1.5f, 0.3f, 0.2f, IntSet.of(6, 9, 13, 16, 19, 22))
            .withSound(JSoundRegistry.THE_HAND_KICK_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withFinisher(23, STOMP_BARRAGE_FINISHER)
            .withInfo(
                    Component.literal("Stomp Barrage"),
                    Component.literal("""
                            Fast startup, and good advantage on block.
                            Last hit knocks down, and cannot be comboed out of with cr.M1""")
            );

    public static final MainBarrageAttack<TheHandEntity> BARRAGE = new MainBarrageAttack<TheHandEntity>(240, 0,
            40, 0.75f, 0.8f, 30, 2f, 0.25f, 0f, 3, Blocks.DEEPSLATE.defaultDestroyTime())
            .withSound(JSoundRegistry.D4C_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withInfo(
                    Component.literal("Barrage"),
                    Component.literal("fast reliable combo starter/extender, high stun")
            );

    public static final KnockdownAttack<TheHandEntity> SWEEP = new KnockdownAttack<TheHandEntity>(0, 13, 18, 1.0f,
            9f, 15, 1.6f, 0.4f, 0.3f, 35)
            .withSound(JSoundRegistry.D4C_LIGHT)
            .withAnim(State.SWEEP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withInfo(
                    Component.literal("Sweep"),
                    Component.literal("Can be comboed out of with cr.M1~M1>...")
            );
    public static final SimpleUppercutAttack<TheHandEntity> KICK = new SimpleUppercutAttack<TheHandEntity>(0, 13, 24, 0.75f,
            9f, 12, 2f, 1.1f, 0.1f, 0.3f)
            .withSound(JSoundRegistry.D4C_LIGHT)
            .withCrouchingVariant(SWEEP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHyperArmor()
            .withLaunch()
            .withInfo(
                    Component.literal("Home Run!"),
                    Component.literal("Uninterruptible launcher.")
            );
    public static final EraseGroundAttack ERASE_GROUND = new EraseGroundAttack(120, 18, 29, 0.75f,
            8.0f, 14, 2.0f, 0, 0.35f)
            .withSound(JSoundRegistry.THE_HAND_SWIPE)
            .withAnim(State.ERASE_GROUND)
            .withImpactSound(JSoundRegistry.IMPACT_12)
            .withInfo(
                    Component.literal("Erase"),
                    Component.literal("""
                            §eCan be cancelled back into itself.§r
                            Two points of armor granted by the initial attack.
                            Erases the ground in front of the user.
                            Works on any non-indestructible block.""")
            )
            .withStaticY()
            .withArmor(2);
    public static final SimpleEraseAttack ERASE = new SimpleEraseAttack(120, 18, 29, 0.75f,
            8.0f, 14, 2.0f, 0, 0)
            .withSound(JSoundRegistry.THE_HAND_SWIPE)
            .withAnim(State.ERASE)
            .withCrouchingVariant(ERASE_GROUND)
            .withImpactSound(JSoundRegistry.IMPACT_12)
            .withArmor(2)
            .withInfo(
                    Component.literal("Erase"),
                    Component.literal("""
                            §eCan be cancelled back into itself.§r
                            Two points of armor granted by the initial attack.
                            Slow, unblockable attack.""")
            );
    public static final SimpleAttack<TheHandEntity> GRAB_HIT = new SimpleAttack<TheHandEntity>(0, 14, 16,
            0.75f, 8f, 5, 1.75f, 1.5f, 0f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Grab (Hit)"),
                    Component.empty());
    public static final GrabAttack<TheHandEntity, State> GRAB = new GrabAttack<>(300, 10, 20,
            0.75f, 0f, 16, 1.5f, 0f, 0f, GRAB_HIT, StateContainer.of(State.GRAB_HIT))
            .withSound(JSoundRegistry.THE_HAND_GRAB)
            .withInfo(
                    Component.literal("Grab"),
                    Component.literal("unblockable, knocks back"));
    public static final EraseSpaceAttack ERASE_SPACE = new EraseSpaceAttack(300, 12,
            20, 0.75f, 4.0f, 6, 2.0f, -0.5f, 0.0f)
            .withSound(JSoundRegistry.THE_HAND_SWIPE_QUICK)
            .withAnim(State.ERASE_GROUND)
            .withImpactSound(JSoundRegistry.IMPACT_12)
            .withInfo(
                    Component.literal("Erase Space"),
                    Component.literal("""
                            Brings any looked at entity.
                            If not looking at anything, will bring you forward.""")
            );
    public static final RageAttack RAGE = new RageAttack(30 * 20,
            57, 0.75f, 6.0f, 20, 2.0f, 0.2f, 0.0f, IntSet.of(26, 40))
            .withSound(JSoundRegistry.THE_HAND_RAGE)
            .withImpactSound(JSoundRegistry.IMPACT_12)
            .withHyperArmor()
            .withInfo(
                    Component.literal("Rage"),
                    Component.literal("""
                            First two hits are uninterruptible.
                            If the second hit makes contact, The Hand will beat the opponent down.
                            """)
            );

    public TheHandEntity(final Level world) {
        super(JStandTypeRegistry.THE_HAND.get(), world);

        auraColors = new Vector3f[] {
                new Vector3f(0, 0, 1.0f),
                new Vector3f(0.6f, 0.2f, 0f),
                new Vector3f(0.8f, 0.2f, 0.8f),
                new Vector3f(0.2f, 0, 0.5f),
        };
    }

    private static void registerMoves(final MoveMap<TheHandEntity, State> moves) {
        var light = moves.register(MoveClass.LIGHT, LIGHT, State.LIGHT);
        light.withFollowup(State.LIGHT_FOLLOWUP);
        light.withCrouchingVariant(State.CROUCHING_LIGHT).withFollowup(State.CROUCHING_LIGHT_FOLLOWUP);

        moves.registerImmediate(MoveClass.HEAVY, KICK, State.KICK);
        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE);
        moves.registerImmediate(MoveClass.SPECIAL1, ERASE, State.ERASE);
        moves.register(MoveClass.SPECIAL2, STOMP_BARRAGE, State.STOMP_BARRAGE);
        moves.registerImmediate(MoveClass.SPECIAL3, GRAB, State.GRAB);

        moves.register(MoveClass.ULTIMATE, RAGE, State.RAGE);

        moves.register(MoveClass.UTILITY, ERASE_SPACE, State.ERASE_SPACE);
    }

    @Override
    public boolean initMove(final MoveClass moveClass) {
        if (tryFollowUp(moveClass, MoveClass.LIGHT)) return true;
        if (moveClass == MoveClass.SPECIAL1
                && getCurrentMove() != null
                && getCurrentMove().getMoveClass() == MoveClass.SPECIAL1
                && getMoveStun() < 4
        ) {
            final AbstractMove<?, ? super TheHandEntity> repeat = getCurrentMove();
            if (repeat != null) {
                setMove(repeat, (State) repeat.getAnimation());
                return true;
            }
        }
        return super.initMove(moveClass);
    }

    @Override
    public void queueMove(final MoveInputType type) {
        if (type == MoveInputType.SPECIAL1
                && getCurrentMove() != null
                && getCurrentMove().getMoveClass() == MoveClass.SPECIAL1) {
            return;
        }
        super.queueMove(type);
    }

    @Override
    public boolean shouldOffsetHeight() {
        if (getState() == State.ERASE_GROUND) return false;
        return super.shouldOffsetHeight();
    }

    @Override
    public @NonNull TheHandEntity getThis() {
        return this;
    }

    public enum State implements StandAnimationState<TheHandEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.the_hand.idle", AzPlayBehaviors.LOOP)),
        LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.the_hand.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.the_hand.light2", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        CROUCHING_LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.the_hand.crouching_light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        CROUCHING_LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.the_hand.crouching_light2", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.the_hand.block", AzPlayBehaviors.LOOP)),
        KICK(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.the_hand.heavy", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.the_hand.barrage", AzPlayBehaviors.LOOP)),
        ERASE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.the_hand.erase", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ERASE_GROUND(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.the_hand.erase_ground", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ERASE_SPACE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.the_hand.erase_space", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        SWEEP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.the_hand.sweep", AzPlayBehaviors.LOOP)),
        GRAB(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.the_hand.grab", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        GRAB_HIT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.the_hand.grab_hit", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        STOMP_BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.the_hand.stomp_barrage", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        RAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.the_hand.rage", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        RAGE_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.the_hand.rage_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ;

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(TheHandEntity attacker) {
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
