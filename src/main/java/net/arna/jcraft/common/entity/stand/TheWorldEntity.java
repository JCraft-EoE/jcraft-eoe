package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.attack.moves.theworld.FeignBarrageCounterAttack;
import net.arna.jcraft.common.attack.moves.theworld.TWDonutAttack;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.List;
import java.util.function.Consumer;

public class TheWorldEntity extends StandEntity<TheWorldEntity, TheWorldEntity.State> {
    public static final SimpleAttack<TheWorldEntity> LOW_KICK = new SimpleAttack<TheWorldEntity>(20, 8, 14, 0.75f,
            6f, 17, 1.5f, 0.2f, 0.65f)
            .withAnim(State.LOW)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withExtraHitBox(0, 0, 1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHitAnimation(HitPropertyComponent.HitAnimation.LOW)
            .withInfo(
                    Text.literal("Low Kick"),
                    Text.literal("slower, higher stun, low hitbox")
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
                    Text.literal("Punch"),
                    Text.literal("quick combo finisher")
            );
    public static final SimpleAttack<TheWorldEntity> LIGHT = SimpleAttack.<TheWorldEntity>lightAttack(
            5, 7, 0.75f, 5, 10, 0.1f, -0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(LOW_KICK)
            .withInfo(
                    Text.literal("Punch"),
                    Text.literal("quick combo starter")
            );
    public static final MainBarrageAttack<TheWorldEntity> BARRAGE = new MainBarrageAttack<TheWorldEntity>(280,
            0, 40, 0.75f, 1f, 30, 2, 0.25f, 0, 3, Blocks.OBSIDIAN.getHardness())
            .withSound(JSoundRegistry.TW_BARRAGE)
            .withInfo(
                    Text.literal("Barrage"),
                    Text.literal("fast reliable combo starter/extender, high stun")
            );
    public static final SimpleAttack<TheWorldEntity> SWEEP = new SimpleAttack<TheWorldEntity>(40, 6, 16, 0.75f, 5f,
            16, 1.85f, 0.25f, 0.4f)
            .withSound(JSoundRegistry.TW_KICK)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitAnimation(HitPropertyComponent.HitAnimation.LOW)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Sweep"),
                    Text.literal("fast, decent stun")
            );
    public static final UppercutAttack<TheWorldEntity> ROUNDHOUSE = new UppercutAttack<TheWorldEntity>(20, 7, 13, 0.75f, 5f,
            10, 1.75f, 0.25f, -0.2f, 0.4f)
            .withCrouchingVariant(SWEEP)
            .withSound(JSoundRegistry.TW_KICK)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitAnimation(HitPropertyComponent.HitAnimation.HIGH)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Roundhouse"),
                    Text.literal("low stun")
            );
    public static final KnockdownAttack<TheWorldEntity> COUNTER_FOLLOWUP = new KnockdownAttack<TheWorldEntity>(0, 5, 9, 0.75f, 9f, 16, 1.75f, 0.7f, 0.1f, 35)
            .withSound(JSoundRegistry.TW_COUNTER)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withExtraHitBox(1.25)
            .withLaunch()
            .withHyperArmor()
            .withInfo(
                    Text.literal("Counter (Hit)"),
                    Text.literal("quick, armored knockdown")
            );
    public static final FeignBarrageCounterAttack FEIGN_BARRAGE = new FeignBarrageCounterAttack(400, 5,
            50, 0.75f, COUNTER_FOLLOWUP)
            .withSound(JSoundRegistry.TW_BARRAGE)
            .withInfo(
                    Text.literal("Feign Barrage"),
                    Text.literal("counter, 0.25s windup, 2.25s duration, teleports and knocks down on hit")
            );
    public static final TWDonutAttack DONUT = new TWDonutAttack(220, 20, 42, 1f,
            9f, 52, 2f, 1f, 0f)
            .withSound(JSoundRegistry.TW_DONUT)
            .withImpactSound(JSoundRegistry.TW_DONUT_HIT)
            .withExtraHitBox(1.5)
            .withHyperArmor()
            .withLaunch()
            .withInfo(
                    Text.literal("Donut"),
                    Text.literal("slow, uninterruptible combo starter/extender, 1.5s stun on whiff")
            );
    public static final TimeSkipMove<TheWorldEntity> TIME_SKIP = new TimeSkipMove<TheWorldEntity>(300, 14)
            .withSound(JSoundRegistry.TIME_SKIP)
            .withInfo(
                    Text.literal("Timeskip"),
                    Text.literal("14m range")
            );
    public static final SimpleAttack<TheWorldEntity> LUNGE = new SimpleAttack<TheWorldEntity>(160, 9, 14,
            1f, 5f, 12, 1.5f, 0.6f, 0.2f)
            .withExtraHitBox(1)
            .withInitAction(TheWorldEntity::doCharge)
            .withSound(JSoundRegistry.TW_KICK)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(
                    Text.literal("Lunge"),
                    Text.literal("user & stand charge forward, launches")
            );
    private static void doCharge(TheWorldEntity attacker, LivingEntity user, MoveContext moveContext) {
        if (attacker.isFree()) return;
        JUtils.addVelocity(user, attacker.getRotationVector().multiply(0.75));
    }

    public static final ChargeAttack<TheWorldEntity, TheWorldEntity.State> CHARGE = new ChargeAttack<>(
            280, 5, 19, 7.5f, 5f, 20, 1.5f, 0.25f, 0, State.CHARGE_HIT)
            .withCrouchingVariant(LUNGE)
            .withSound(JSoundRegistry.TW_CHARGE)
            .withImpactSound(JSoundRegistry.TW_CHARGE_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withBlockStun(11)
            .withInfo(
                    Text.literal("Forward Charge"),
                    Text.literal("The World detaches from the user and lunges forward, combo starter")
            );
    public static final TimeStopMove<TheWorldEntity> TIME_STOP = new TimeStopMove<TheWorldEntity>(1400,
            45, 52, JServerConfig.TW_TIME_STOP_DURATION::getValue)
            .withSound(JSoundRegistry.TW_TS)
            .withInfo(
                    Text.literal("Timestop"),
                    Text.literal("4 seconds")
            );

    public TheWorldEntity(World worldIn) {
        super(StandType.THE_WORLD, worldIn, JSoundRegistry.TW_SUMMON);
        idleRotation = 225f;

        pros = List.of(
                "fast m1",
                "counter",
                "versatile ranged moves",
                "timestop & timeskip"
        );

        cons = List.of(
                "no knockdowns",
                "donut is high risk/high reward outside combos"
        );

        description = "Mid Range DOMINATOR";

        freespace =
                """
                        BNBs:
                            -the sauce boss
                            (M1>)Charge>cr.M1>Roundhouse>Barrage>M1>Donut>Roundhouse>M1~M1
                            
                            -the afternoon coffee
                            Donut>Roundhouse>Charge>M1>Barrage>Roundhouse>M1~M1""";

        auraColors = new Vector3f[]{
                new Vector3f(1.0f, 0.7f, 0.3f),
                new Vector3f(1.0f, 0f, 0f),
                new Vector3f(1.0f, 0.6f, 0.0f),
                new Vector3f(0.7f, 0.3f, 1.0f)
        };
    }

    @Override
    protected void registerMoves(MoveMap<TheWorldEntity, State> moves) {
        moves.registerImmediate(MoveType.LIGHT, LIGHT, State.LIGHT);

        moves.register(MoveType.HEAVY, DONUT, State.DONUT);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, ROUNDHOUSE, State.ROUNDHOUSE).withCrouchingVariant(State.SWEEP);
        moves.register(MoveType.SPECIAL2, CHARGE, State.CHARGE).withCrouchingVariant(State.LUNGE);
        moves.register(MoveType.SPECIAL3, FEIGN_BARRAGE, State.BARRAGE);
        moves.register(MoveType.ULTIMATE, TIME_STOP, State.TIME_STOP);

        moves.register(MoveType.UTILITY, TIME_SKIP, State.IDLE);
    }

    @Override
    public boolean initMove(MoveType type) {
        if (type == MoveType.LIGHT && curMove != null && curMove.getMoveType() == MoveType.LIGHT && getMoveStun() < curMove.getWindupPoint()) {
            AbstractMove<?, ? super TheWorldEntity> followup = curMove.getFollowup();
            if (followup != null) setMove(followup, (State) followup.getAnimation());

            return true;
        } else return super.initMove(type);
    }

    @Override
    public void desummon() {
        if (tsTime > 0) return;
        super.desummon();
    }

    @Override
    public void setAttackRotationOffset() {
        // Prevents The World from going in front of the user while the Feign Barrage isn't active
        if (curMove != null && curMove.getOriginalMove() == FEIGN_BARRAGE && getMoveStun() > FEIGN_BARRAGE.getDuration() - FEIGN_BARRAGE.getWindup()) {
            setRotationOffset(idleRotation);
            return;
        }
        super.setAttackRotationOffset();
    }

    @Override
    protected void playSummonSound() {
        if (shouldNotPlaySummonSound()) return;

        playSound(JSoundRegistry.TW_SUMMON, 1f, 1f);
        playSound(JSoundRegistry.MUDA_DA, 1f, 1f);
    }

    @Override
    @NonNull
    public TheWorldEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<TheWorldEntity> {
        IDLE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.theworld.idle"))),
        LIGHT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.theworld.light"))),
        BLOCK(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.theworld.block"))),
        DONUT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.theworld.donut"))),
        BARRAGE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.theworld.barrage"))),
        TIME_STOP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.theworld.timestop"))),
        CHARGE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.theworld.charge"))),
        CHARGE_HIT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.theworld.charge_hit"))),
        ROUNDHOUSE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.theworld.roundhouse"))),
        SWEEP(builder -> builder.playAndHold("animation.theworld.sweep")),
        COUNTER_HIT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.theworld.counter_hit"))),
        COUNTER_MISS(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.theworld.counter_miss"))),
        LOW(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.theworld.low"))),
        TIMESKIP(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.theworld.idle"))),
        LIGHT_FOLLOWUP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.theworld.light_followup"))),
        LUNGE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.theworld.lunge")));

        private final Consumer<AnimationState> animator;

        State(Consumer<AnimationState> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(TheWorldEntity attacker, AnimationState builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @Nullable String getSummonAnimation() {
        return "animation.theworld.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
