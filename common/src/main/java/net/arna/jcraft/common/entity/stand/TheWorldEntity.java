package net.arna.jcraft.common.entity.stand;

import com.mojang.datafixers.util.Either;
import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.pose.ModifierCondition;
import net.arna.jcraft.api.pose.PoseModifiers;
import net.arna.jcraft.api.pose.modifier.IPoseModifier;
import net.arna.jcraft.api.pose.modifier.PoseModifierGroup;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.common.attack.actions.LungeAction;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.attack.moves.theworld.FeignBarrageCounterAttack;
import net.arna.jcraft.common.attack.moves.theworld.TWChargeAttack;
import net.arna.jcraft.common.attack.moves.theworld.TWDonutAttack;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3f;

import java.util.function.Supplier;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/The_World">The World</a>.
 * @see JStandTypeRegistry#THE_WORLD
 * @see FeignBarrageCounterAttack
 * @see TWDonutAttack
 */
public final class TheWorldEntity extends AbstractTheWorldEntity<TheWorldEntity, TheWorldEntity.State> {
    public static final MoveSet<TheWorldEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.THE_WORLD,
            TheWorldEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.builder()
            .evolution(true)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.theworld"))
                    .proCount(4)
                    .conCount(2)
                    .freeSpace(Component.literal("""
                            BNBs:
                                -the sauce boss
                                (Light>)Charge>cr.Light>Roundhouse>Barrage>Light>Donut>Sweep>Light~Light
                            
                                -the afternoon coffee
                                Donut>Sweep>Charge>Light>Barrage>Roundhouse>Light~Light"""))
                    .skinName(Component.literal("OVA"))
                    .skinName(Component.literal("Black"))
                    .skinName(Component.literal("Greatest High"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.TW_SUMMON))
            .build();
    // Arms near hips, the DIO pose in HFTF
    public static final Supplier<IPoseModifier> POSE = () -> PoseModifierGroup.builder()
            .modifier(PoseModifiers.parse("""
                    leftArm.yRot = 15deg;
                    leftArm.zRot = 2deg;
                    """, ModifierCondition.LEFT_ARM_EMPTY))
            .modifier(PoseModifiers.parse("""
                    rightArm.yRot = -15deg;
                    rightArm.zRot = -2deg;
                    """, ModifierCondition.RIGHT_ARM_EMPTY))
            .modifier(PoseModifiers.parse("""
                    leftArm.xRot -= 10deg;
                    rightArm.xRot -= 10deg;
                    body.xRot -= 10deg;
                    
                    leftLeg.z -= 2;
                    rightLeg.z -= 2;
                    
                    leftArm.z += 0.25;
                    rightArm.z += 0.25;
                    leftArm.x += 0.5;
                    rightArm.x -= 0.5;
                    """, ModifierCondition.USER_NOT_SPRINTING))
            .build();

    public static final SimpleAttack<TheWorldEntity> LOW_KICK = new SimpleAttack<TheWorldEntity>(0, 8, 14, 0.75f,
            6f, 17, 1.5f, 0.2f, 0.65f)
            .withAnim(State.LOW)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withExtraHitBox(0, 0, 1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.LOW)
            .withInfo(
                    Component.literal("Low Kick"),
                    Component.literal("slower, higher stun, low hitbox")
            );
    public static final SimpleAttack<TheWorldEntity> LIGHT_FOLLOWUP = new SimpleAttack<TheWorldEntity>(
            0, 7, 11, 0.75f, 6f, 8, 1.5f, 1f, 0)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0, 1)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("quick combo finisher")
            );
    public static final SimpleAttack<TheWorldEntity> LIGHT = SimpleAttack.<TheWorldEntity>lightAttack(
                    5, 7, 0.75f, 5, 10, 0.1f, -0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(LOW_KICK)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("quick combo starter")
            );
    public static final MainBarrageAttack<TheWorldEntity> BARRAGE = new MainBarrageAttack<TheWorldEntity>(280,
            0, 40, 0.75f, 1f, 30, 2, 0.25f, 0, 3, Blocks.OBSIDIAN.defaultDestroyTime())
            .withSound(JSoundRegistry.TW_BARRAGE)
            .withInfo(
                    Component.literal("Barrage"),
                    Component.literal("fast reliable combo starter/extender, high stun")
            );
    public static final SimpleAttack<TheWorldEntity> SWEEP = new SimpleAttack<TheWorldEntity>(16,
            6, 16, 0.75f, 5f, 16, 1.85f, 0.5f, 0.4f)
            .withSound(JSoundRegistry.TW_KICK)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.LOW)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Sweep"),
                    Component.literal("fast, decent stun")
            );
    public static final SimpleUppercutAttack<TheWorldEntity> ROUNDHOUSE = new SimpleUppercutAttack<TheWorldEntity>(13,
            7, 13, 0.75f, 5f, 10, 1.75f, 0.5f, -0.2f, 0.4f)
            .withCrouchingVariant(SWEEP)
            .withSound(JSoundRegistry.TW_KICK)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Roundhouse"),
                    Component.literal("low stun")
            );
    public static final KnockdownAttack<TheWorldEntity> COUNTER_FOLLOWUP = new KnockdownAttack<TheWorldEntity>(0,
            5, 9, 0.75f, 9f, 16, 1.75f, 0.7f, 0.1f, 35)
            .withSound(JSoundRegistry.TW_COUNTER)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withExtraHitBox(1.25)
            .withLaunch()
            .withHyperArmor()
            .withInfo(
                    Component.literal("Counter (Hit)"),
                    Component.literal("quick, armored knockdown")
            );
    public static final FeignBarrageCounterAttack FEIGN_BARRAGE = new FeignBarrageCounterAttack(400,
            5,50, 0.75f, COUNTER_FOLLOWUP)
            .withSound(JSoundRegistry.TW_BARRAGE)
            .withInfo(
                    Component.literal("Feign Barrage"),
                    Component.literal("counter, 0.25s windup, 2.25s duration, teleports and knocks down on hit")
            );
    public static final TWDonutAttack DONUT = new TWDonutAttack(42,
            20, 42, 1f,9f, 52, 2f, 1f, 0f)
            .withSound(JSoundRegistry.TW_DONUT)
            .withImpactSound(JSoundRegistry.TW_DONUT_HIT)
            .withExtraHitBox(1.5)
            .withHyperArmor()
            .withLaunch()
            .withInfo(
                    Component.literal("Donut"),
                    Component.literal("slow, uninterruptible combo starter/extender, 1.5s stun on whiff")
            );
    public static final TimeSkipMove<TheWorldEntity> TIME_SKIP = new TimeSkipMove<TheWorldEntity>(300, 14)
            .withSound(JSoundRegistry.TIME_SKIP)
            .withInfo(
                    Component.literal("Timeskip"),
                    Component.literal("14m range")
            );
    public static final SimpleAttack<TheWorldEntity> LUNGE = new SimpleAttack<TheWorldEntity>(100,
            9, 14,1f, 5f, 12, 1.5f, 0.6f, 0.2f)
            .withExtraHitBox(1)
            .withInitAction(LungeAction.lunge(0.75f, 0f).isNotFree())
            .withSound(JSoundRegistry.TW_KICK)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(
                    Component.literal("Lunge"),
                    Component.literal("user & stand charge forward, launches")
            );
    public static final TWChargeAttack CHARGE = new TWChargeAttack(100,
            5, 19, 7.5f, 5f, 20, 1.5f, 0.25f, 0)
            .withCrouchingVariant(LUNGE)
            .withSound(JSoundRegistry.TW_CHARGE)
            .withImpactSound(JSoundRegistry.TW_CHARGE_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withBlockStun(11)
            .withInfo(
                    Component.literal("Forward Charge"),
                    Component.literal("The World detaches from the user and lunges forward, combo starter")
            );
    public static final TimeStopMove<TheWorldEntity> TIME_STOP = new TimeStopMove<TheWorldEntity>(1400,
            45, 52, Either.right(JServerConfig.TW_TIME_STOP_DURATION))
            .withSound(JSoundRegistry.TW_TS)
            .withInfo(
                    Component.literal("Timestop"),
                    Component.literal("4 seconds")
            );

    public TheWorldEntity(Level worldIn) {
        super(JStandTypeRegistry.THE_WORLD.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(1.0f, 0.7f, 0.3f),
                new Vector3f(1.0f, 0f, 0f),
                new Vector3f(1.0f, 0.6f, 0.0f),
                new Vector3f(0.7f, 0.3f, 1.0f)
        };
    }

    private static void registerMoves(MoveMap<TheWorldEntity, State> moves) {
        moves.registerImmediate(MoveClass.LIGHT, LIGHT, State.LIGHT);

        moves.register(MoveClass.HEAVY, DONUT, State.DONUT);
        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveClass.SPECIAL1, ROUNDHOUSE, State.ROUNDHOUSE).withCrouchingVariant(State.SWEEP);
        moves.register(MoveClass.SPECIAL2, CHARGE, State.CHARGE).withCrouchingVariant(State.LUNGE);
        moves.register(MoveClass.SPECIAL3, FEIGN_BARRAGE, State.BARRAGE);
        moves.register(MoveClass.ULTIMATE, TIME_STOP, State.TIME_STOP);

        moves.register(MoveClass.UTILITY, TIME_SKIP, State.IDLE);
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        if (tryFollowUp(moveClass, MoveClass.LIGHT)) {
            return true;
        } else {
            return super.initMove(moveClass);
        }
    }

    @Override
    protected void playSummonSound() {
        if (shouldNotPlaySummonSound()) {
            return;
        }

        playSound(JSoundRegistry.TW_SUMMON.get(), 1f, 1f);
        playSound(JSoundRegistry.MUDA_DA.get(), 1f, 1f);
    }

    @Override
    @NonNull
    public TheWorldEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<TheWorldEntity> {
        IDLE(AzCommand.create("base_controller", "animation.theworld.idle", AzPlayBehaviors.LOOP)),
        LIGHT(Attacks.createAnimationCommand("base_controller", "animation.theworld.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create("base_controller", "animation.theworld.block", AzPlayBehaviors.LOOP)),
        DONUT(Attacks.createAnimationCommand("base_controller", "animation.theworld.donut", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BARRAGE(Attacks.createAnimationCommand("base_controller", "animation.theworld.barrage", AzPlayBehaviors.LOOP)),
        TIME_STOP(Attacks.createAnimationCommand("base_controller", "animation.theworld.timestop", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        CHARGE(Attacks.createAnimationCommand("base_controller", "animation.theworld.charge", AzPlayBehaviors.LOOP)),
        CHARGE_HIT(Attacks.createAnimationCommand("base_controller", "animation.theworld.charge_hit", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ROUNDHOUSE(Attacks.createAnimationCommand("base_controller", "animation.theworld.roundhouse", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        SWEEP(Attacks.createAnimationCommand("base_controller", "animation.theworld.sweep", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        COUNTER_HIT(Attacks.createAnimationCommand("base_controller", "animation.theworld.counter_hit", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        COUNTER_MISS(Attacks.createAnimationCommand("base_controller", "animation.theworld.counter_miss", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LOW(Attacks.createAnimationCommand("base_controller", "animation.theworld.low", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        TIMESKIP(Attacks.createAnimationCommand("base_controller", "animation.theworld.idle", AzPlayBehaviors.LOOP)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand("base_controller", "animation.theworld.light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LUNGE(Attacks.createAnimationCommand("base_controller", "animation.theworld.lunge", AzPlayBehaviors.HOLD_ON_LAST_FRAME));

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(TheWorldEntity attacker) {
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
