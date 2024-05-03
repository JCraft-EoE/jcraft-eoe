package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.BlockableType;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.attack.moves.whitesnake.*;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class WhiteSnakeEntity extends StandEntity<WhiteSnakeEntity, WhiteSnakeEntity.State> {
    public static final UppercutAttack<WhiteSnakeEntity> UPPERCUT = new UppercutAttack<WhiteSnakeEntity>(
            20, 8, 14, 1, 6f, 16, 1.25f, 0.5f, -0.5f, 0.5f)
            .withAnim(State.UPPERCUT)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withExtraHitBox(1)
            .withInfo(
                    Text.literal("Uppercut"),
                    Text.literal("decent stun, launches up")
            );
    public static final SimpleAttack<WhiteSnakeEntity> LIGHT_FOLLOWUP = new SimpleAttack<WhiteSnakeEntity>(
            0, 7, 13, 0.75f, 6f, 10, 1.5f, 1f, 0.2f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withLaunch()
            .withBlockStun(4)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Finisher"),
                    Text.literal("quick combo finisher")
            );
    public static final SimpleAttack<WhiteSnakeEntity> LIGHT = SimpleAttack.<WhiteSnakeEntity>lightAttack(
            7, 11, 0.75f, 5f, 13, 0.2f, 0.2f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(UPPERCUT)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(
                    Text.literal("Punch"),
                    Text.literal("quick combo starter")
            );
    public static final SimpleAttack<WhiteSnakeEntity> MEDIUM = new SimpleAttack<WhiteSnakeEntity>(
            60, 8, 13, 1, 7f, 16, 1.75f, 0.4f, 0)
            .withSound(JSoundRegistry.WS_DONUT)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(
                    Text.literal("Gut Punch"),
                    Text.literal("combo starter/extender")
            );
    public static final MainBarrageAttack<WhiteSnakeEntity> BARRAGE = new MainBarrageAttack<WhiteSnakeEntity>(
            240, 0, 40, 0.75f, 1, 20, 2, 0.25f, 0, 3, Blocks.OAK_PLANKS.getHardness())
            .withSound(JSoundRegistry.WS_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(
                    Text.literal("Barrage"),
                    Text.literal("fast reliable combo starter/extender, medium stun")
            );
    public static final GiveStandAttack GIVE_STAND = new GiveStandAttack(400, 22, 34, 1, 1, 2, 0, 0)
            .withSound(JSoundRegistry.WS_STAND_DISC)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(null)
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE)
            .withInfo(
                    Text.literal("Give Stand Disk"),
                    Text.literal("gives a single hit target a stand, provided they do not have one already, from a disk in the user's off hand")
            );
    public static final EffectInflictingAttack<WhiteSnakeEntity> STAND_DISC = new EffectInflictingAttack<WhiteSnakeEntity>(
            480, 22, 34, 1, 8f, 20, 2, 0.5f, 0,
            List.of(new StatusEffectInstance(JStatusRegistry.STANDLESS, 160, 0)))
            .withSound(JSoundRegistry.WS_STAND_DISC)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withHitAnimation(HitPropertyComponent.HitAnimation.HIGH)
            .withCrouchingVariant(GIVE_STAND)
            .withInfo(
                    Text.literal("Take Stand Disk"),
                    Text.literal("uninterruptible & unblockable, removes enemy stand for 8s")
            );
    public static final SimpleAttack<WhiteSnakeEntity> LEG_CRUSHER = new SimpleAttack<WhiteSnakeEntity>(
            240, 16, 22, 0.75f, 7, 32, 1.75f, 0.35f, 0.4f)
            .withSound(JSoundRegistry.WS_LEGCRUSH)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHitAnimation(HitPropertyComponent.HitAnimation.LOW)
            .withInfo(
                    Text.literal("Leg Crusher"),
                    Text.literal("high stun, medium windup")
            );
    public static final EffectInflictingAttack<WhiteSnakeEntity> MEMORY_DISC = new EffectInflictingAttack<WhiteSnakeEntity>(
            280, 22, 34, 1, 7f, 20, 2, 0.5f, 0,
            List.of(
                    new StatusEffectInstance(StatusEffects.WEAKNESS, 600, 0),
                    new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 600, 0)
            ))
            .withSound(JSoundRegistry.WS_MEMORY_DISC)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withHitAnimation(HitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Text.literal("Take Memory Disk"),
                    Text.literal("uninterruptible& unblockable, gives mining fatigue & weakness for 30s")
            );
    public static final ChargedSpewAttack CHARGED_SPEW = new ChargedSpewAttack(
            200, 20, 26, 0.75f, 0f, 0, 2f, 0f, 0f)
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withInfo(
                    Text.literal("Poison Spew"),
                    Text.literal("fires a spread of 5 acid projectiles that slow enemies and persist on the surface they hits for 5s")
            );
    public static final PoisonSpewAttack POISON_SPEW = new PoisonSpewAttack(
            200, 10, 14, 0.75f, 0f, 0, 2f, 0f, 0f)
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withCrouchingVariant(CHARGED_SPEW)
            .withInfo(
                    Text.literal("Poison Spew"),
                    Text.literal("fires an acid projectile that slows enemies and persists on the surface it hits for 5s")
            );
    public static final MeltYourHeartAttack MELT_YOUR_HEART = new MeltYourHeartAttack(
            800, 40, 50, 1f, 3f, 20, 2f, 1f, 0f)
            .withSound(JSoundRegistry.WS_MYH)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withLaunch()
            .withInfo(
                    Text.literal("Melt your Heart"),
                    Text.literal("remote-only and armored, expels a sphere of poison")
            );
    public static final PilotModeMove<WhiteSnakeEntity> PILOT_MODE = new PilotModeMove<WhiteSnakeEntity>(20)
            .withInfo(
                    Text.literal("Pilot Mode"),
                    Text.empty()
            );

    public WhiteSnakeEntity(World worldIn) {
        super(StandType.WHITE_SNAKE, worldIn, JSoundRegistry.WS_SUMMON);
        idleRotation = 220f;

        description = "All Range DISABLER";

        pros = List.of(
                "coverage on all ranges",
                "high versatility",
                "accessible win condition"
        );

        cons = List.of(
                "no mobility options",
                "slow pokes",
                "below average damage"
        );

        freespace =
                """
                        BNBs:
                            -the gimp
                            M1>Gut Punch>Poison Spew
                        
                            -the el mayo (optimal damage with disk moves)
                            Memory Disk>M1>Barrage>Leg Crusher>Stand Disk>M1~M1
                                        
                            -the gazebo (optimal damage without disk)
                            M1>Barrage>Leg Crusher>Donut>M1~M1
                            
                            -the protein shake (sets up mixups)
                            M1>Barrage>Leg Crusher>Charged Spew""";

        auraColors = new Vector3f[]{
                new Vector3f(1f, 1f, 1f),
                new Vector3f(1f, 1f, 1f),
                new Vector3f(0.4f, 0.4f, 0.5f),
                new Vector3f(1.0f, 0.0f, 0.0f)
        };
    }

    @Override
    protected void registerMoves(MoveMap<WhiteSnakeEntity, State> moves) {
        moves.registerImmediate(MoveType.LIGHT, LIGHT, State.LIGHT);

        moves.register(MoveType.HEAVY, MEDIUM, State.MEDIUM);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, MEMORY_DISC, State.DISC_TAKE);
        moves.register(MoveType.SPECIAL2, LEG_CRUSHER, State.LEG_CRUSHER);
        moves.register(MoveType.SPECIAL3, POISON_SPEW, State.ACID_SPEW).withCrouchingVariant(State.ACID_SPEW_CHARGED);
        if (isRemote())
            moves.register(MoveType.ULTIMATE, MELT_YOUR_HEART, State.MELT_YOUR_HEART);
        else
            moves.register(MoveType.ULTIMATE, STAND_DISC, State.DISC_TAKE).withCrouchingVariant(State.DISC_GIVE);

        moves.register(MoveType.UTILITY, PILOT_MODE);
    }

    @Override
    public boolean initMove(MoveType type) {
        if (type == MoveType.LIGHT && curMove != null && curMove.getMoveType() == MoveType.LIGHT && getMoveStun() < curMove.getWindupPoint()) {
            AbstractMove<?, ? super WhiteSnakeEntity> followup = curMove.getFollowup();
            if (followup != null) setMove(followup, (State) followup.getAnimation());

            return true;
        } else return super.initMove(type);
    }

    @Override
    public void tick() {
        super.tick();

        if (!isRemoteAndControllable()) return;

        if (getWorld().isClient) {
            // Called for EVERYONE
            JCraft.getClientEntityHandler().whiteSnakeRemoteClientTick(this);
        } else {
            double f = getRemoteForwardInput();
            double s = getRemoteSideInput();
            boolean jump = getRemoteJumpInput();

            tickRemoteMovement(f, s, jump);

            if (getState() == State.IDLE) { // Replace idle anim
                if (s > 0) setStateNoReset(isOnGround() ? State.RIGHT : State.RIGHT_DASH);
                if (s < 0) setStateNoReset(isOnGround() ? State.LEFT : State.LEFT_DASH);
                if (f < 0) setStateNoReset(isOnGround() ? State.BACKWARD : State.BACKWARD_DASH);
                if (f > 0) setStateNoReset(isOnGround() ? State.FORWARD : State.FORWARD_DASH);
            }
        }
    }

    /**
     * Movement control for a grounded remote stand.
     * @param f Forward input
     * @param s +Right/-Left input
     * @param jump Jump input
     */
    public void tickRemoteMovement(double f, double s, boolean jump) {
        Vec3d pos = getPos();

        // 1 tick of inertia, helping movement be fluid as well as dealing with packet drops
        if (lastRemoteInputTime - age > 2) updateRemoteInputs(0, 0, false, false);
        Vec3d rotVec = new Vec3d(getRotationVector().x, 0, getRotationVector().z).normalize();

        double dragMult = getMoveStun() > 0 ? 0.2 : 0.4;
        double moveSpeed = 0.24;
        boolean onGround = isOnGround();
        boolean climbing = getBlockStateAtPos().streamTags().anyMatch(tag -> tag == BlockTags.CLIMBABLE);
        boolean swimming = !getWorld().getFluidState(getBlockPos()).isEmpty();

        if (climbing || swimming) dragMult *= 0.5;

        if ( (climbing || swimming ) && jump) { // Climb or Swim
            addVelocity(0, 0.1, 0);
        } else { // Jump
            if (onGround) {
                if (jump) {
                    addVelocity(0, 0.75, 0);
                    setRemoteJumpInput(false);
                }
            } else {
                //JCraft.LOGGER.info("Airborne");
                moveSpeed = 0.024;
                dragMult = 0.4;
            }
        }

        remoteSpeed = remoteSpeed
                .add(rotVec.multiply(f * moveSpeed)) // Forward movement
                .add(rotVec.rotateY(1.5707963f).multiply(s * moveSpeed)); // Side movement

        remoteSpeed = remoteSpeed.multiply(dragMult);

        Vec3d userPos = getUserOrThrow().getPos();
        if (pos.add(remoteSpeed).squaredDistanceTo(userPos) > 400)
            remoteSpeed = userPos.subtract(pos).multiply(0.025); // 1/40th so it scales with distance

        addVelocity(remoteSpeed.x, remoteSpeed.y, remoteSpeed.z);
        velocityDirty = true;
        velocityModified = true;
    }


    @Override
    @NonNull
    public WhiteSnakeEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<WhiteSnakeEntity> {
        IDLE((whitesnake, builder) -> builder.setAnimation(RawAnimation.begin().thenLoop(whitesnake.isRemote() ? "animation.whitesnake.remote_idle" : "animation.whitesnake.idle"))),
        LIGHT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.whitesnake.light"))),
        BLOCK(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.whitesnake.block"))),
        MEDIUM(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.whitesnake.medium"))),
        BARRAGE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.whitesnake.barrage"))),
        LEG_CRUSHER(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.whitesnake.legcrusher"))),
        ACID_SPEW(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.whitesnake.acidspew"))),
        ACID_SPEW_CHARGED(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.whitesnake.acidspew_charged"))),
        DISC_TAKE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.whitesnake.disc_take"))),
        DISC_GIVE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.whitesnake.disc_give"))),
        UPPERCUT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.whitesnake.uppercut"))),

        FORWARD(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.whitesnake.forw"))),
        BACKWARD(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.whitesnake.back"))),
        LEFT(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.whitesnake.left"))),
        RIGHT(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.whitesnake.right"))),
        FORWARD_DASH(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.whitesnake.fdash"))),
        BACKWARD_DASH(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.whitesnake.bdash"))),
        LEFT_DASH(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.whitesnake.ldash"))),
        RIGHT_DASH(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.whitesnake.rdash"))),

        MELT_YOUR_HEART(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.whitesnake.meltyourheart"))),
        LIGHT_FOLLOWUP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.whitesnake.light_followup")));

        private final BiConsumer<WhiteSnakeEntity, AnimationState> animator;

        State(Consumer<AnimationState> animator) {
            this((whiteSnake, builder) -> animator.accept(builder));
        }

        State(BiConsumer<WhiteSnakeEntity, AnimationState> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(WhiteSnakeEntity attacker, AnimationState builder) {
            animator.accept(attacker, builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @Nullable String getSummonAnimation() {
        return "animation.whitesnake.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
