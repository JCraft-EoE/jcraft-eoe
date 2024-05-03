package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.attack.moves.starplatinum.theworld.GroundSlamAttack;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.List;
import java.util.function.Consumer;

public final class SPTWEntity extends AbstractStarPlatinumEntity<SPTWEntity, SPTWEntity.State> {
    public static final GroundSlamAttack GROUND_SLAM = new GroundSlamAttack(20, 12, 19,
            0.75f, 7f, 11, 1.8f, 0f, 0.8f)
            .withAnim(State.GROUND_SLAM)
            .withImpactSound(JSoundRegistry.IMPACT_8)
            .withLaunchNoShockwave()
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Ground Slam"),
                    Text.literal("low hitbox, decent damage, launches")
            );
    public static final SimpleAttack<SPTWEntity> LIGHT_FOLLOWUP = new SimpleAttack<SPTWEntity>(
            0, 5, 14, 0.75f, 6, 12, 1.5f, 1f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0.25, 1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Punch"),
                    Text.literal("quick combo finisher")
            );
    public static final SimpleAttack<SPTWEntity> PUNCH = SimpleAttack.<SPTWEntity>lightAttack(5, 7,
                    0.75f, 5f, 10, 0.2f, -0.1f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(GROUND_SLAM)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Text.literal("Punch"),
                    Text.literal("quick combo starter, low knockback")
            );
    public static final MainBarrageAttack<SPTWEntity> BARRAGE = new MainBarrageAttack<SPTWEntity>(280, 0,
            40, 0.75f, 1f, 30, 2f, 0.25f, 0f, 3, Blocks.OBSIDIAN.getHardness())
            .withSound(JSoundRegistry.STAR_PLATINUM_BARRAGE)
            .withInfo(
                    Text.literal("Barrage"),
                    Text.literal("fast reliable combo starter/extender, high stun")
            );
    public static final SimpleAttack<SPTWEntity> TIME_STRIKE = new SimpleAttack<SPTWEntity>(300, 7,
            11, 0.75f, 5f, 12, 1.5f, 0.6f, -0.25f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withExtraHitBox(1f)
            .withInfo(Text.literal("Time Strike"), Text.literal("""
                    teleports forward 2.5m after a short windup, then delivers a fast, low stun hit
                    crouch to turn around after teleport"""));
    public static final SimpleAttack<SPTWEntity> BACKHAND = new SimpleAttack<SPTWEntity>(240, 7, 12,
            0.75f, 6f, 20, 1.5f, 0.25f, 0f)
            .withSound(JSoundRegistry.SPTW_BACKHAND)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withExtraHitBox(1f)
            .withHitAnimation(HitPropertyComponent.HitAnimation.HIGH)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Backhand"),
                    Text.literal("fast poke, great stun")
            );
    public static final KnockdownAttack<SPTWEntity> GRAB_SLAM = new KnockdownAttack<SPTWEntity>(0,
            16, 24, 1f, 9f, 10, 1.75f, 0.4f, 0f, 25)
            .withSound(JSoundRegistry.SPTW_UPPERCUT)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHyperArmor()
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withInfo(
                    Text.literal("What an ugly watch (Slam)"),
                    Text.empty()
            );
    public static final GrabAttack<SPTWEntity, State> GRAB2 = new GrabAttack<>(280, 8, 20,
            1f, 2f, 20, 1.5f, 0.1f, 0f, GRAB_SLAM, State.GRAB_HIT2)
            .withSound(JSoundRegistry.SPTW_GRAB)
            .withImpactSound(JSoundRegistry.SPTW_GRABHIT)
            .withHitAnimation(null)
            .withInfo(
                    Text.literal("What an ugly watch"),
                    Text.literal("grab, high damage combo-finishing knockdown")
            );
    public static final EffectInflictingAttack<SPTWEntity> GRAB_HIT = new EffectInflictingAttack<SPTWEntity>(0,
            16, 24, 1f, 6f, 20, 1.75f, 0.4f, 0f,
            List.of(new StatusEffectInstance(StatusEffects.LEVITATION, 5, 10, true, false)))
            .withSound(JSoundRegistry.SPTW_UPPERCUT)
            .withImpactSound(JSoundRegistry.IMPACT_6)
            .withLaunch()
            .withHyperArmor()
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("What an ugly watch (Uppercut)"),
                    Text.empty()
            );
    public static final GrabAttack<SPTWEntity, State> GRAB = new GrabAttack<>(280, 8, 20,
            1f, 2f, 20, 1.5f, 0.1f, 0f, GRAB_HIT, State.GRAB_HIT)
            .withCrouchingVariant(GRAB2)
            .withSound(JSoundRegistry.SPTW_GRAB)
            .withImpactSound(JSoundRegistry.SPTW_GRABHIT)
            .withHitAnimation(null)
            .withInfo(
                    Text.literal("What an ugly watch"),
                    Text.literal("grab, combo-starting uppercut")
            );
    public static final TimeStopMove<SPTWEntity> TIME_STOP = new TimeStopMove<SPTWEntity>(600, 5, 10,
            JServerConfig.SPTW_TIME_STOP_DURATION::getValue)
            .withSound(JSoundRegistry.STAR_PLATINUM_THE_WORLD)
            .withInfo(
                    Text.literal("Timestop"),
                    Text.literal("1.75 seconds, extremely low windup")
            );
    public static final TimeSkipMove<SPTWEntity> TIME_SKIP = new TimeSkipMove<SPTWEntity>(300, 14)
            .withSound(JSoundRegistry.STAR_PLATINUM_TIMESKIP)
            .withInfo(
                    Text.literal("Timeskip"),
                    Text.empty()
            );
    private boolean turnAround;

    public SPTWEntity(World worldIn) {
        super(StandType.STAR_PLATINUM_THE_WORLD, worldIn);

        idleRotation = 315f;

        description = "High Speed RUSHDOWN";

        pros = List.of(
                "high whiff punish power",
                "high mobility",
                "excellent mixups",
                "near-instant timestop"
        );

        cons = List.of(
                "burns through options quickly",
                "hard to hitconfirm important options without using TS"
        );

        freespace = """
                    BNBs:
                                            
                        -the superman
                        M1>cr.Time Strike>Backhand>What an Ugly Watch>delay M1>Timestop~Star Breaker>dash/Timeskip>Barrage>M1""";

        auraColors = new Vector3f[]{
                new Vector3f(0.8f, 0.6f, 1.0f),
                new Vector3f(1.0f, 0.4f, 0.8f),
                new Vector3f(0.7f, 0.7f, 1.0f),
                new Vector3f(0.8f, 1.0f, 1.0f)
        };
    }

    @Override
    protected void registerMoves(MoveMap<SPTWEntity, State> moves) {
        moves.registerImmediate(MoveType.LIGHT, PUNCH, State.PUNCH);

        moves.register(MoveType.HEAVY, STAR_BREAKER, State.HEAVY).withCrouchingVariant(State.GROUND_BREAKER);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, TIME_STRIKE, State.TIME_STRIKE);
        moves.register(MoveType.SPECIAL2, BACKHAND, State.BACKHAND);
        moves.register(MoveType.SPECIAL3, GRAB, State.GRAB).withCrouchingVariant(State.GRAB);
        moves.register(MoveType.ULTIMATE, TIME_STOP, State.TIME_STOP);

        moves.register(MoveType.UTILITY, TIME_SKIP, State.TIME_SKIP);
    }

    @Override
    public void desummon() {
        if (tsTime > 0) return;
        super.desummon();
    }

    @Override
    public boolean initMove(MoveType type) {
        if (type == MoveType.LIGHT && curMove != null && curMove.getMoveType() == MoveType.LIGHT && getMoveStun() < curMove.getWindupPoint()) {
            AbstractMove<?, ? super SPTWEntity> followup = curMove.getFollowup();
            if (followup != null) setMove(followup, (State) followup.getAnimation());
        } else {
            boolean s = super.initMove(type);
            if (type == MoveType.SPECIAL1) turnAround = getUserOrThrow().isSneaking();
            return s;
        }

        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (!hasUser() || getWorld().isClient || curMove == null || curMove.getOriginalMove() != TIME_STRIKE || getMoveStun() != 7)
            return;

        /*
            NbtCompound userData = ((IEntityDataSaver)user).getPersistentData();
            if (userData.getInt(JCraft.utilCD) < 200)
                userData.putInt(JCraft.utilCD, 200);
             */

        LivingEntity user = getUserOrThrow();
        Vec3d prevPos = user.getEyePos();

        TimeSkipMove.doTimeSkip(this, user, 2.5, List.of(JSoundRegistry.STAR_PLATINUM_TIMESKIP));
        if (turnAround) user.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, prevPos);
    }

    @Override
    @NonNull
    public SPTWEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<SPTWEntity> {
        IDLE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.sptw.idle"))),
        PUNCH(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.sptw.punch"))),
        BLOCK(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.sptw.block"))),
        HEAVY(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.sptw.heavy"))),
        GROUND_BREAKER(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.sptw.ground_break"))),
        BARRAGE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.sptw.barrage"))),
        TIME_STRIKE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.sptw.timestrike"))),
        TIME_STOP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.sptw.timestop"))),
        BACKHAND(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.sptw.backhand"))),
        GRAB(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.sptw.grab"))),
        GRAB_HIT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.sptw.grabhit"))),
        GRAB_HIT2(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.sptw.grabhit2"))),
        TIME_SKIP(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.sptw.idle"))),
        GROUND_SLAM(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.sptw.ground_slam"))),
        LIGHT_FOLLOWUP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.sptw.light_followup")));

        private final Consumer<AnimationState> animator;

        State(Consumer<AnimationState> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(SPTWEntity attacker, AnimationState builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected String getSummonAnimation() {
        return "animation.sptw.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
