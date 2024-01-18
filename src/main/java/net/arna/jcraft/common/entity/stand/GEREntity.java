package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.goldexperience.requiem.*;
import net.arna.jcraft.common.attack.moves.shared.BarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.HealMove;
import net.arna.jcraft.common.attack.moves.shared.KnockdownAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.function.Consumer;

public class GEREntity extends StandEntity<GEREntity, GEREntity.State> {
    public static final SimpleAttack<GEREntity> LIGHT_FOLLOWUP = new SimpleAttack<GEREntity>(
            0, 6, 13, 0.75f, 6f, 8, 1.5f, 1f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0.25, 1)
            .withInfo(Text.literal("Punch"), Text.literal("quick combo finisher"));
    public static final SimpleAttack<GEREntity> DOWNWARD_KICK = new SimpleAttack<GEREntity>(JCraft.LIGHT_COOLDOWN,
            5, 12, 0.75f, 4f, 20, 1.25f, 0.75f, 0.33f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withExtraHitBox(0, -1, 1)
            .withInfo(Text.literal("Downward Kick"), Text.literal("medium stun combo starter, low hitbox, low blockstun"));
    public static final OverheadKickAttack OVERHEAD_KICK = new OverheadKickAttack(220, 14, 24,
            1f, 9f, 40, 1.5f, 0.8f, 0.25f)
            .withSound(JSoundRegistry.GER_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withExtraHitBox(0, -1, 1)
            .withInfo(Text.literal("Overhead Kick"), Text.literal("slow, high stun combo starter"));
    public static final SimpleAttack<GEREntity> KICK_BARRAGE_FINISHER = new SimpleAttack<GEREntity>(0,
            6, 9, 1f, 1f, 10, 1.75f, 1.1f, 0f)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(Text.literal("Kick Barrage (Final Hit)"), Text.empty());
    public static final BarrageAttack<GEREntity> KICK_BARRAGE = new BarrageAttack<GEREntity>(280, 0, 48,
            1f, 1f, 20, 1.5f, 0.3f, 0f, 3)
            .withFinisher(37, KICK_BARRAGE_FINISHER)
            .withSound(JSoundRegistry.GER_KICKBARRAGE)
            .withInfo(Text.literal("Kick Barrage"), Text.literal("fast combo finisher, knocks back"));
    // JCraft.lightCooldown -> 0 | 0.55f -> 0.4f
    public static final SimpleAttack<GEREntity> PUNCH = new SimpleAttack<GEREntity>(JCraft.LIGHT_COOLDOWN / 2,
            5, 9, 0.75f, 5f, 8, 1.5f, 0.75f, -0.1f)
            .withAerialVariant(DOWNWARD_KICK)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(Text.literal("Punch"), Text.literal("quick combo starter"));
    public static final KnockdownAttack<GEREntity> OVERHEAD_SMASH = new KnockdownAttack<GEREntity>(220, 10, 19,
            1f, 9f, 10, 1.5f, 1.1f, 0f, 30)
            .withAerialVariant(OVERHEAD_KICK)
            .withSound(JSoundRegistry.GER_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHyperArmor()
            .withLaunch()
            .withExtraHitBox(1.5)
            .withInfo(Text.literal("Overhead Smash"), Text.literal("slow, uninterruptible knockdown"));
    public static final BarrageAttack<GEREntity> BARRAGE = new BarrageAttack<GEREntity>(280, 0, 30,
            0.75f, 1f, 30, 2f, 0.25f, 0f, 3)
            .withAerialVariant(KICK_BARRAGE)
            .withSound(JSoundRegistry.GE_BARRAGE)
            .withInfo(Text.literal("Barrage"), Text.literal("fast reliable combo starter/extender, high stun"));
    public static final HealMove<GEREntity> HEAL = new HealMove<GEREntity>(520, 10, 16,
            1f, 1.25f, 0f, 6f, HealMove.HealTarget.TARGETS, GEREntity::pacifyMobs)
            .withSound(JSoundRegistry.GE_HEAL)
            .withInfo(Text.literal("Healing Hand (Others)"), Text.empty());
    public static final HealMove<GEREntity> HEAL_SELF = new HealMove<GEREntity>(520, 10, 14,
            1f, 0f, 0f, 4f, HealMove.HealTarget.USER)
            .withCrouchingVariant(HEAL)
            .withSound(JSoundRegistry.GE_HEAL)
            .withInfo(Text.literal("Healing Hand"), Text.literal("standing: heals user for 2 hearts, crouching: heals others for 3 hearts, pacifies angered mobs"));
    public static final LifeBeamAttack CHARGED_LIFE_BEAM = new LifeBeamAttack(280, 18, 28,
            1.1f, true)
            .withSound(JSoundRegistry.GER_SLOW_LASER)
            .withInfo(Text.literal("Life Beam (Charged)"), Text.literal("slower, poisoning variant"));
    public static final LifeBeamAttack LIFE_BEAM = new LifeBeamAttack(280, 10, 20, 1f, false)
            .withCrouchingVariant(CHARGED_LIFE_BEAM)
            .withSound(JSoundRegistry.GER_LASER)
            .withInfo(Text.literal("Life Beam"), Text.literal("summons a quick, stunning rock projectile " +
                    "that turns into a scorpion a small time after landing"));
    public static final NullificationAttack NULLIFICATION = new NullificationAttack(480, 5, 35, 1f)
            .withSound(JSoundRegistry.GE_HEAL)
            .withInfo(Text.literal("Nullification"), Text.literal("0.25s windup, 1.5s counter, stuns on hit"));
    public static final ReturnToZeroMove RETURN_TO_ZERO = new ReturnToZeroMove(1200, 30, 32, 1f)
            .withSound(JSoundRegistry.GER_SETUP)
            .withInfo(Text.literal("Return to Zero"), Text.literal("initial press: saves the state of " +
                    "every entity in a 4 chunk radius, second press: reverts all states except users\nDoesn't affect player inventories"));
    public static final FlightMove FLIGHT = new FlightMove(320, 1, 0, 0f)
            .withSound(JSoundRegistry.GER_FLY)
            .withInfo(Text.literal("Flight"), Text.literal("1 second of flight"));

    private static final TrackedData<Integer> FLIGHT_TIME;

    static {
        FLIGHT_TIME = DataTracker.registerData(GEREntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    public GEREntity(World worldIn) {
        super(StandType.GOLD_EXPERIENCE_REQUIEM, worldIn);

        idleRotation = 0f;

        description = "Impossible Ascended DEFENSE";

        pros = List.of(
                "very mobile",
                "wide toolkit",
                "excellent defense",
                "setplay/combo tool (life beam)",
                "undo button"
        );

        cons = List.of(
                "low damage output",
                "limited pressure"
        );

        freespace = """
                BNBs:
                the scorpy patty (sets up stand off transition)
                (M1>)Barrage>jump>Overhead Kick>Life Beam>M1>Life Beam (second hit)
                knockdown experience
                M1>Barrage>Life Beam>M1~Overhead Smash>Life Beam (second hit)""";
    }

    @Override
    protected void registerMoves(MoveMap<GEREntity, State> moves) {
        moves.register(MoveType.LIGHT, PUNCH, State.LIGHT).withAerialVariant(State.AIR_LIGHT);
        moves.register(MoveType.HEAVY, OVERHEAD_SMASH, State.HEAVY).withAerialVariant(State.AIR_HEAVY);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE).withAerialVariant(State.AIR_BARRAGE);

        moves.register(MoveType.SPECIAL1, HEAL_SELF, State.HEAL_SELF).withCrouchingVariant(State.HEAL);
        moves.register(MoveType.SPECIAL2, LIFE_BEAM, State.LASER).withCrouchingVariant(State.SLOW_LASER);
        moves.register(MoveType.SPECIAL3, NULLIFICATION, State.COUNTER);
        moves.register(MoveType.ULTIMATE, RETURN_TO_ZERO, State.SETUP);

        moves.register(MoveType.UTILITY, FLIGHT);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(FLIGHT_TIME, 0);
    }

    @Override
    public void initMove(MoveType type) {
        if (type == MoveType.ULTIMATE && !moveContext.get(ReturnToZeroMove.ENTITY_DATA).isEmpty())
            RETURN_TO_ZERO.returnToZero(this);
        else if (type == MoveType.LIGHT && curMove != null && curMove.getMoveType() == MoveType.LIGHT && getMoveStun() < curMove.getWindupPoint()) {
            AbstractMove<?, ? super GEREntity> followup = curMove.getFollowup();
            if (followup != null) setMove(followup, (State) followup.getAnimation());
        } else super.initMove(type);
    }

    public int getFlightTime() {
        return this.dataTracker.get(FLIGHT_TIME);
    }

    public void setFlightTime(int i) {
        this.dataTracker.set(FLIGHT_TIME, i);
    }

    private static void pacifyMobs(LivingEntity target) {
        target.setAttacker(null);

        if (!(target instanceof MobEntity mob)) return;
        stun(mob, 10, 0);
        mob.setTarget(null);
        mob.setAttacking(null);
        if (mob instanceof Angerable angerable)
            angerable.stopAnger();
    }

    @Override
    public void desummon() {
        if (getFlightTime() > 0) {
            setFlightTime(0);
            return;
        }
        super.desummon();
    }

    @Override
    public void tick() {
        if (age == 1) playSound(JSoundRegistry.GER_SUMMON, 1f, 1f);
        super.tick();

        FLIGHT.tickFlight(this);
        RETURN_TO_ZERO.tickReturnInfo(this);
    }

    @Override
    protected @NonNull GEREntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<GEREntity> {
        IDLE(builder -> builder.loop("animation.ger.idle")),
        LIGHT(builder -> builder.playAndHold("animation.ger.light")),
        BLOCK(builder -> builder.loop("animation.ger.block")),
        HEAVY(builder -> builder.playAndHold("animation.ger.heavy")),
        BARRAGE(builder -> builder.loop("animation.ger.barrage")),
        HEAL_SELF(builder -> builder.playAndHold("animation.ger.healself")),
        HEAL(builder -> builder.playAndHold("animation.ger.heal")),
        LASER(builder -> builder.playAndHold("animation.ger.laser")),
        SLOW_LASER(builder -> builder.playAndHold("animation.ger.slowlaser")),
        COUNTER(builder -> builder.playAndHold("animation.ger.counter")),
        COUNTER_MISS(builder -> builder.playAndHold("animation.ger.counter_miss")),
        AIR_HEAVY(builder -> builder.playAndHold("animation.ger.airheavy")),
        AIR_LIGHT(builder -> builder.playAndHold("animation.ger.airlight")),
        AIR_BARRAGE(builder -> builder.playAndHold("animation.ger.airbarrage")),
        SETUP(builder -> builder.playAndHold("animation.ger.setup")),
        LIGHT_FOLLOWUP(builder -> builder.playAndHold("animation.ger.light_followup"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(GEREntity attacker, AnimationBuilder builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @Nullable String getSummonAnimation() {
        return "animation.ger.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
