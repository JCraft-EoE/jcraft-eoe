package net.arna.jcraft.common.entity.stand;

import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.old.Attack;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.attack.moves.theworld.FeignBarrageCounterAttack;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class TheWorldEntity extends StandEntity<TheWorldEntity, TheWorldEntity.State> {
    public static final SimpleAttack<TheWorldEntity> LOW_KICK = new SimpleAttack<TheWorldEntity>(30, 8, 14, 6f, 17, 1.5f, 0.2f, 0.75f, 0.85f)
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
    public static final SimpleAttack<TheWorldEntity> ROUNDHOUSE = new SimpleAttack<TheWorldEntity>(160, 7, 13, 5f, 9, 1.75f, 0.1f, 0.75f, -0.1f)
            .withSound(JSoundRegistry.TW_KICK)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withInfo(Text.literal("Roundhouse"), Text.literal("fast poke, low stun"));
    public static final FeignBarrageCounterAttack FEIGN_BARRAGE = new FeignBarrageCounterAttack(600, 5, 50, 0.75f)
            .withSound(JSoundRegistry.TW_BARRAGE)
            .withInfo(Text.literal("Feign Barrage"), Text.literal("counter, 0.25s windup, 2.25s duration, teleports and knocks down on hit"));
    public static final KnockdownAttack<TheWorldEntity> COUNTER_FOLLOWUP = new KnockdownAttack<TheWorldEntity>(0, 5, 9, 0.75f, 9f, 16, 1.75f, 0.7f, 0.1f, 35)
            .withSound(JSoundRegistry.TW_COUNTER)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withExtraHitBox(1.25)
            .withLaunch()
            .withHyperArmor()
            .withInfo(Text.literal("Counter (Hit)"), Text.literal("quick, armored knockdown"));
    /*
    public static final Attack donut = new Attack(1, 14, 1f, 42, 20, 2, 9f, 1.0f, AttackType.BOX, 2.6f, 0, 0, JSoundRegistry.TW_DONUT_HIT)
            .setLaunch()
            .setHitspark(2)
            .appendHitbox(new HitBoxData(0, 0, 1.5))
            .hyperArmor()
            .setInfo("Donut", "slow, uninterruptable combo starter/extender, 1.5s stun on whiff");
    private static final Attack timeskip = new Attack(-2, 18, 2, 2)
            .setMobility(MobilityType.TELEPORT)
            .setInfo("Timeskip", "14m range");
    private static final Attack counterMiss = new Attack(8, 0, 10, 11)
            .setInfo("Counter (Whiff)", "");
     */
    public static final ChargeAttack<TheWorldEntity, TheWorldEntity.State> CHARGE = new ChargeAttack<>(400, 5, 19, 7.5f, 5, 20, 1.5f, 0.25f, 0, State.CHARGE_HIT)
            .withSound(JSoundRegistry.TW_CHARGE)
            .withImpactSound(JSoundRegistry.TW_CHARGE_HIT)
            .withBlockStun(11)
            .withInfo(Text.literal("Forward Charge"), Text.literal("The World detaches from the user and lunges forward, combo starter"));
    public static final TimestopAttack<TheWorldEntity> TIMESTOP = new TimestopAttack<TheWorldEntity>(1400, 45, 52, 1, 80)
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

        super.initialize();

        if (world.isClient) return;
        TIMESTOP.setTimestopDuration(JServerConfig.TW_TIME_STOP_DURATION.getValue() / 20);
    }

    @Override
    protected void registerMoves(MoveMap<TheWorldEntity, State> moves) {
        moves.register(MoveType.LIGHT, LIGHT, State.LIGHT).withCrouchingVariant(State.LOW);
        moves.register(MoveType.HEAVY, DONUT, State.DONUT);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, ROUNDHOUSE, State.ROUNDHOUSE);
        moves.register(MoveType.SPECIAL2, CHARGE, State.CHARGE);
        moves.register(MoveType.SPECIAL3, FEIGN_BARRAGE, State.BARRAGE);

        moves.register(MoveType.ULTIMATE, TIMESTOP, State.TIME_STOP);
        moves.register(MoveType.UTILITY, TIMESKIP, State.IDLE);
    }

    @Override
    public void desummon() {
        if (tsTime > 0) return;
        super.desummon();
    }

    public void initHeavyAttack() {
        if (!canAttack()) return;
        if (handleAttack(donut, CooldownType.STAND_HEAVY, State.DONUT))
            playSound(JSoundRegistry.TW_DONUT, 1, 1);
    }

    public void initSpecial1() {
        if (!canAttack()) return;
        if (handleAttack(roundhouse, CooldownType.STAND_SP1, State.ROUNDHOUSE))
            playSound(JSoundRegistry.TW_KICK, 1, 1);
    }

    public void initUlt() {
        if (!canAttack()) return;
        if (handleAttack(timestop, CooldownType.STAND_ULT, State.TIME_STOP))
            playSound(JSoundRegistry.TW_TS, 1, 1);
    }

    public void initSpecial2() {
        if (!canAttack()) return;
        if (handleAttack(charge, CooldownType.STAND_SP2, State.CHARGE))
            playSound(JSoundRegistry.TW_CHARGE, 1, 1);
    }

    public void initUtil() {
        if (!canAttack() || tsTime > 0) return;
        handleAttack(timeskip, CooldownType.UTILITY, State.TIMESKIP);
    }

    public void specialAttack(Attack attack, Set<LivingEntity> entities) {
        switch (attack.id) {
            case (-2) -> timeSkip(14, JSoundRegistry.TIME_SKIP);
            case (1) -> {
                LivingEntity user = getUser();
                // If missed, stun the user for 1.5 seconds
                if (entities.isEmpty()) stun(user, 30, 0);
                    /* If hit, impale and set position to middle of arm
                else for (LivingEntity entity : entities) {
                    Vec3d pos = this.getPos().add(this.getRotationVector().multiply(1.5));
                    entity.teleport(pos.x, entity.getY(), pos.z);
                }
                     */
            }
        }
    }

    @Override
    public void setAttackRotationOffset() {
        // Prevents The World from going in front of the user while the Feign Barrage isn't active
        if (curMove == FEIGN_BARRAGE && getMoveStun() > FEIGN_BARRAGE.getDuration() - FEIGN_BARRAGE.getWindup()) {
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
    protected TheWorldEntity getThis() {
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
