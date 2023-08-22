package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.attack.moves.starplatinum.theworld.GroundSlamAttack;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.function.Consumer;

public final class SPTWEntity extends AbstractStarPlatinumEntity<SPTWEntity, SPTWEntity.State> {
    public static final GroundSlamAttack GROUND_SLAM = new GroundSlamAttack(30, 12, 19,
            0.75f, 7f, 11, 1.8f, 0f, 0.8f)
            .withImpactSound(JSoundRegistry.IMPACT_8)
            .withLaunch()
            .withInfo(Text.literal("Ground Slam"), Text.literal("low hitbox, decent damage, launches"));
    public static final SimpleAttack<SPTWEntity> PUNCH = SimpleAttack.<SPTWEntity>lightAttack(5, 7,
            5f, 10, 0.2f, 0.75f, -0.1f)
            .withCrouchingVariant(GROUND_SLAM)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(Text.literal("PUNCH"), Text.literal("quick combo starter, low knockback"));
    public static final BarrageAttack<SPTWEntity> BARRAGE = new BarrageAttack<SPTWEntity>(340, 0, 60,
            0.75f, 1f, 40, 2f, 0.25f, 0f, 3)
            .withSound(JSoundRegistry.STAR_PLATINUM_BARRAGE)
            .withInfo(Text.literal("Barrage"), Text.literal("fast reliable combo starter/extender, high stun"));
    public static final SimpleAttack<SPTWEntity> TIME_STRIKE = new SimpleAttack<SPTWEntity>(400, 7,
            11, 0.75f, 5f, 12, 1.5f, 0.75f, -0.25f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withExtraHitBox(1f)
            .withInfo(Text.literal("Time Strike"), Text.literal("teleports forward 2.5m after a short windup, then delivers a fast, low stun hit/crouch to turn around after teleport"));
    public static final SimpleAttack<SPTWEntity> BACKHAND = new SimpleAttack<SPTWEntity>(12, 7, 12,
            0.75f, 6f, 20, 1.5f, 0.25f, 0f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withExtraHitBox(1f)
            .withInfo(Text.literal("Backhand"), Text.literal("fast poke, decent stun"));
    public static final EffectInflictingAttack<SPTWEntity> GRAB_HIT = new EffectInflictingAttack<SPTWEntity>(0,
            16, 24, 1f, 9f, 20, 1.75f, 0.4f, 0f,
            List.of(new StatusEffectInstance(StatusEffects.LEVITATION, 5, 10, true, false)))
            .withImpactSound(JSoundRegistry.IMPACT_6)
            .withLaunch()
            .withHyperArmor()
            .withInfo(Text.literal("What an Ugly Watch (Hit)"), Text.empty());
    public static final GrabAttack<SPTWEntity, State> GRAB = new GrabAttack<>(520, 8, 20,
            1f, 2f, 20, 1.5f, 0.1f, 0f, GRAB_HIT, State.GRAB_HIT)
            .withSound(JSoundRegistry.SPTW_GRAB)
            .withImpactSound(JSoundRegistry.SPTW_GRABHIT)
            .withBlockStun(4)
            .withInfo(Text.literal("What an Ugly Watch"), Text.literal("grab, high recovery"));
    public static final TimeStopMove<SPTWEntity> TIME_STOP = new TimeStopMove<SPTWEntity>(600, 5, 10, 35)
            .withSound(JSoundRegistry.STAR_PLATINUM_THE_WORLD)
            .withInfo(Text.literal("Timestop"), Text.literal("1.75 seconds, extremely low windup"));
    public static final TimeSkipMove<SPTWEntity> TIME_SKIP = new TimeSkipMove<SPTWEntity>(360, 14)
            .withSound(JSoundRegistry.STAR_PLATINUM_TIMESKIP)
            .withInfo(Text.literal("Timeksip"), Text.empty());
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

        super.initialize();

        if (world.isClient) return;
//        timestop.stun = JServerConfig.SPTW_TIME_STOP_DURATION.getValue() / 20.0f; // TODO
    }

    @Override
    protected void registerMoves(MoveMap<SPTWEntity, State> moves) {
        moves.register(MoveType.LIGHT, PUNCH, State.PUNCH);
        moves.register(MoveType.HEAVY, STAR_BREAKER, State.HEAVY);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, TIME_STRIKE, State.TIME_STRIKE);
        moves.register(MoveType.SPECIAL2, BACKHAND, State.BACKHAND);
        moves.register(MoveType.SPECIAL3, GRAB, State.GRAB);
        moves.register(MoveType.ULTIMATE, TIME_STOP, State.TIME_STOP);

        moves.register(MoveType.UTILITY, TIME_SKIP, State.TIME_SKIP);
    }

    @Override
    public void desummon() {
        if (tsTime > 0) return;
        super.desummon();
    }

    @Override
    public void initMove(MoveType type) {
        super.initMove(type);

        if (type == MoveType.SPECIAL1) turnAround = getUserOrThrow().isSneaking();
    }

    @Override
    public void tick() {
        super.tick();
        if (!hasUser() || world.isClient || curMove == null || curMove.getOriginalMove() != TIME_STRIKE || getMoveStun() != 7)
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
    protected @NonNull SPTWEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<SPTWEntity> {
        IDLE(builder -> builder.loop("animation.sptw.idle")),
        PUNCH(builder -> builder.playAndHold("animation.sptw.punch")),
        BLOCK(builder -> builder.loop("animation.sptw.block")),
        HEAVY(builder -> builder.playAndHold("animation.sptw.heavy")),
        BARRAGE(builder -> builder.loop("animation.sptw.barrage")),
        TIME_STRIKE(builder -> builder.playAndHold("animation.sptw.timestrike")),
        TIME_STOP(builder -> builder.playAndHold("animation.sptw.timestop")),
        BACKHAND(builder -> builder.playAndHold("animation.sptw.backhand")),
        GRAB(builder -> builder.playAndHold("animation.sptw.grab")),
        GRAB_HIT(builder -> builder.playAndHold("animation.sptw.grabhit")),
        TIME_SKIP(builder -> builder.loop("animation.sptw.idle")),
        GROUND_SLAM(builder -> builder.playAndHold("animation.sptw.groundslam"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(SPTWEntity attacker, AnimationBuilder builder) {
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
