package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.HitBoxData;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.old.Attack;
import net.arna.jcraft.common.attack.core.old.AttackType;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.attack.moves.theworld.FeignBarrageCounterAttack;
import net.arna.jcraft.common.attack.moves.theworld.TWDonutAttack;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.function.Consumer;

public class TheWorldEntity extends StandEntity<TheWorldEntity, TheWorldEntity.State> {
    public static final SimpleAttack<TheWorldEntity> LOW_KICK = new SimpleAttack<TheWorldEntity>(30, 8, 14, 0.75f, 6f, 17, 1.5f, 0.2f, 0.85f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withExtraHitBox(0, 0, 1)
            .withInfo(Text.literal("Low Kick"), Text.literal("slower, higher stun"));
    public static final SimpleAttack<TheWorldEntity> LIGHT = SimpleAttack.<TheWorldEntity>lightAttack(5, 7, 5, 10, 0.1f, 0.75f, -0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withCrouchingVariant(LOW_KICK)
            .withInfo(Text.literal("Punch"), Text.literal("quick combo starter"));
    public static final BarrageAttack<TheWorldEntity> BARRAGE = new BarrageAttack<TheWorldEntity>(340, 0, 50, 0.75f, 1f, 30, 2, 0.1f, 0, 3)
            .withSound(JSoundRegistry.TW_BARRAGE)
            .withInfo(Text.literal("Barrage"), Text.literal("fast reliable combo starter/extender, high stun"));
    public static final SimpleAttack<TheWorldEntity> ROUNDHOUSE = new SimpleAttack<TheWorldEntity>(160, 7, 13, 0.75f, 5f, 9, 1.75f, 0.1f, -0.1f)
            .withSound(JSoundRegistry.TW_KICK)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withInfo(Text.literal("Roundhouse"), Text.literal("fast poke, low stun"));
    public static final KnockdownAttack<TheWorldEntity> COUNTER_FOLLOWUP = new KnockdownAttack<TheWorldEntity>(0, 5, 9, 0.75f, 9f, 16, 1.75f, 0.7f, 0.1f, 35)
            .withSound(JSoundRegistry.TW_COUNTER)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withExtraHitBox(1.25)
            .withLaunch()
            .withHyperArmor()
            .withInfo(Text.literal("Counter (Hit)"), Text.literal("quick, armored knockdown"));
    public static final FeignBarrageCounterAttack FEIGN_BARRAGE = new FeignBarrageCounterAttack(600, 5,
            50, 0.75f, COUNTER_FOLLOWUP)
            .withSound(JSoundRegistry.TW_BARRAGE)
            .withInfo(Text.literal("Feign Barrage"), Text.literal("counter, 0.25s windup, 2.25s duration, teleports and knocks down on hit"));
    public static final TWDonutAttack DONUT = new TWDonutAttack(280, 20, 42, 1f,
            9f, 52, 2f, 1f, 0f)
            .withSound(JSoundRegistry.TW_DONUT)
            .withImpactSound(JSoundRegistry.TW_DONUT_HIT)
            .withExtraHitBox(1.5)
            .withHyperArmor()
            .withInfo(Text.literal("Donut"), Text.literal("slow, uninterruptible combo starter/extender, 1.5s stun on whiff"));
    public static final TimeSkipMove<TheWorldEntity> TIME_SKIP = new TimeSkipMove<TheWorldEntity>(360, 14)
            .withSound(JSoundRegistry.TIME_SKIP)
            .withInfo(Text.literal("Timeskip"), Text.literal("14m range"));
    public static final Attack donut = new Attack(1, 14, 1f, 42, 20, 2, 9f, 1.0f, AttackType.BOX, 2.6f, 0, 0, JSoundRegistry.TW_DONUT_HIT)
            .setLaunch()
            .setHitspark(2)
            .appendHitbox(new HitBoxData(0, 0, 1.5))
            .hyperArmor()
            .setInfo("Donut", "slow, uninterruptible combo starter/extender, 1.5s stun on whiff");
    public static final ChargeAttack<TheWorldEntity, TheWorldEntity.State> CHARGE = new ChargeAttack<>(
            400, 5, 19, 7.5f, 5, 20, 1.5f, 0.25f, 0, State.CHARGE_HIT)
            .withSound(JSoundRegistry.TW_CHARGE)
            .withImpactSound(JSoundRegistry.TW_CHARGE_HIT)
            .withBlockStun(11)
            .withInfo(Text.literal("Forward Charge"), Text.literal("The World detaches from the user and lunges forward, combo starter"));
    public static final TimeStopMove<TheWorldEntity> TIME_STOP = new TimeStopMove<TheWorldEntity>(
            1400, 45, 52, 80)
            .withInfo(Text.literal("Timestop"), Text.literal("4 seconds"));

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
                "no knockdowns or knockbacks",
                "heavy is useless outside of combos"
        );

        description = "Mid Range DOMINATOR";

        freespace =
                """
                        BNBs:
                            the saucy racist
                            (M1>)Charge>M1>Roundhouse>Barrage>M1>Donut>M1
                            
                            the no ts racist
                            Donut>Roundhouse>Charge>M1>Barrage>M1""";

        if (world.isClient) return;
        // TODO use a supplier here. Will do after attack refactor. (Along with the rest of the config values)
        TIME_STOP.setTimeStopDuration(JServerConfig.TW_TIME_STOP_DURATION.getValue() / 20);
    }

    @Override
    protected void registerMoves(MoveMap<TheWorldEntity, State> moves) {
        moves.register(MoveType.LIGHT, LIGHT, State.LIGHT).withCrouchingVariant(State.LOW);
        moves.register(MoveType.HEAVY, DONUT, State.DONUT);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, ROUNDHOUSE, State.ROUNDHOUSE);
        moves.register(MoveType.SPECIAL2, CHARGE, State.CHARGE);
        moves.register(MoveType.SPECIAL3, FEIGN_BARRAGE, State.BARRAGE);

        moves.register(MoveType.ULTIMATE, TIME_STOP, State.TIME_STOP);
        moves.register(MoveType.UTILITY, TIME_SKIP, State.IDLE);
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
    protected @NonNull TheWorldEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<TheWorldEntity> {
        IDLE(builder -> builder.loop("animation.theworld.idle")),
        LIGHT(builder -> builder.playAndHold("animation.theworld.light")),
        BLOCK(builder -> builder.loop("animation.theworld.block")),
        DONUT(builder -> builder.playAndHold("animation.theworld.donut")),
        BARRAGE(builder -> builder.loop("animation.theworld.barrage")),
        TIME_STOP(builder -> builder.playAndHold("animation.theworld.timestop")),
        CHARGE(builder -> builder.loop("animation.theworld.charge")),
        CHARGE_HIT(builder -> builder.playAndHold("animation.theworld.charge_hit")),
        ROUNDHOUSE(builder -> builder.playAndHold("animation.theworld.roundhouse")),
        COUNTER_HIT(builder -> builder.playAndHold("animation.theworld.counter_hit")),
        COUNTER_MISS(builder -> builder.playAndHold("animation.theworld.counter_miss")),
        LOW(builder -> builder.playAndHold("animation.theworld.low")),
        TIMESKIP(builder -> builder.loop("animation.theworld.idle"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(TheWorldEntity attacker, AnimationBuilder builder) {
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
