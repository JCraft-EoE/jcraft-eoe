package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.BlockableType;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.shared.BarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.EffectInflictingAttack;
import net.arna.jcraft.common.attack.moves.shared.MainBarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.whitesnake.ChargedSpewAttack;
import net.arna.jcraft.common.attack.moves.whitesnake.MeltYourHeartAttack;
import net.arna.jcraft.common.attack.moves.whitesnake.PilotModeMove;
import net.arna.jcraft.common.attack.moves.whitesnake.PoisonSpewAttack;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.function.Consumer;

public class WhiteSnakeEntity extends StandEntity<WhiteSnakeEntity, WhiteSnakeEntity.State> {
    public static final SimpleAttack<WhiteSnakeEntity> LIGHT_FOLLOWUP = new SimpleAttack<WhiteSnakeEntity>(
            0, 7, 13, 0.75f, 6f, 10, 1.5f, 1f, 0.2f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withLaunch()
            .withBlockStun(4)
            .withInfo(Text.literal("Finisher"), Text.literal("quick combo finisher"));
    public static final SimpleAttack<WhiteSnakeEntity> LIGHT = SimpleAttack.<WhiteSnakeEntity>lightAttack(
            7, 11, 5f, 13, 0.75f, 0.75f, 0.2f)
            .withFollowup(LIGHT_FOLLOWUP)
            //TODO: WS CROUCHING M1
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(Text.literal("Punch"), Text.literal("quick combo starter"));
    public static final SimpleAttack<WhiteSnakeEntity> MEDIUM = new SimpleAttack<WhiteSnakeEntity>(
            60, 8, 13, 1, 7f, 16, 1.75f, 0, 0)
            .withSound(JSoundRegistry.WS_DONUT)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(Text.literal("Gut Punch"), Text.literal("combo starter/extender"));
    public static final MainBarrageAttack<WhiteSnakeEntity> BARRAGE = new MainBarrageAttack<WhiteSnakeEntity>(
            240, 0, 60, 0.75f, 1, 20, 2, 0.25f, 0, 3, Blocks.OAK_PLANKS.getHardness())
            .withSound(JSoundRegistry.WS_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(Text.literal("Barrage"), Text.literal("fast reliable combo starter/extender, medium stun"));
    public static final EffectInflictingAttack<WhiteSnakeEntity> STAND_DISC = new EffectInflictingAttack<WhiteSnakeEntity>(
            480, 22, 34, 1, 8f, 20, 2, 0, 0,
            List.of(new StatusEffectInstance(JStatusRegistry.STANDLESS, 160, 0)))
            .withSound(JSoundRegistry.WS_STAND_DISC)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withInfo(Text.literal("Stand Disk"), Text.literal("uninterruptible, removes enemy stand for 8s"));
    public static final SimpleAttack<WhiteSnakeEntity> LEG_CRUSHER = new SimpleAttack<WhiteSnakeEntity>(
            240, 16, 22, 0.75f, 7, 32, 1.75f, 0.25f, 0.2f)
            .withSound(JSoundRegistry.WS_LEGCRUSH)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(Text.literal("Leg Crusher"), Text.literal("high stun, medium windup"));
    public static final EffectInflictingAttack<WhiteSnakeEntity> MEMORY_DISC = new EffectInflictingAttack<WhiteSnakeEntity>(
            280, 22, 34, 1, 7f, 20, 2, 0, 0,
            List.of(
                    new StatusEffectInstance(StatusEffects.WEAKNESS, 600, 0),
                    new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 600, 0)
            ))
            .withSound(JSoundRegistry.WS_MEMORY_DISC)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withInfo(Text.literal("Memory Disk"), Text.literal("uninterruptible, mining fatigue & weakness for 30s"));
    public static final ChargedSpewAttack CHARGED_SPEW = new ChargedSpewAttack(
            200, 20, 26, 0.75f, 0f, 0, 2f, 0f, 0f)
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withInfo(Text.literal("Poison Spew"), Text.literal("fires an acid projectile that slows enemies and persists on the surface it hits for 5s"));
    public static final PoisonSpewAttack POISON_SPEW = new PoisonSpewAttack(
            200, 10, 14, 0.75f, 0f, 0, 2f, 0f, 0f)
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withCrouchingVariant(CHARGED_SPEW)
            .withInfo(Text.literal("Poison Spew"), Text.literal("fires an acid projectile that slows enemies and persists on the surface it hits for 5s"));
    public static final MeltYourHeartAttack MELT_YOUR_HEART = new MeltYourHeartAttack(
            800, 40, 50, 1f, 3f, 20, 2f, 1f, 0f)
            .withSound(JSoundRegistry.WS_MYH)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withLaunch()
            .withInfo(Text.literal("Melt your Heart"), Text.literal("remote-only and armored, expels a sphere of poison"));
    public static final PilotModeMove PILOT_MODE = new PilotModeMove(20)
            .withInfo(Text.literal("Pilot Mode"), Text.empty());

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
                            -the el mayo (optimal damage with disk moves)
                            Memory Disk>M1>Barrage>Leg Crusher>Stand Disk>M1
                                        
                            -the gazebo (optimal damage without disk)
                            M1>Barrage>Leg Crusher>Donut>M1
                            
                            -the protein shake (sets up mixups)
                            M1>Barrage>Leg Crusher>Charged Spew""";

        auraColors = new Vec3f[]{
                new Vec3f(1f, 1f, 1f),
                new Vec3f(0.4f, 0.4f, 0.5f),
                new Vec3f(1.0f, 0.0f, 0.0f),
                new Vec3f(1f, 1f, 1f)
        };
    }

    @Override
    protected void registerMoves(MoveMap<WhiteSnakeEntity, State> moves) {
        moves.register(MoveType.LIGHT, LIGHT, State.LIGHT);
        moves.register(MoveType.HEAVY, MEDIUM, State.MEDIUM);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, MEMORY_DISC, State.DISC);
        moves.register(MoveType.SPECIAL2, LEG_CRUSHER, State.LEG_CRUSHER);
        moves.register(MoveType.SPECIAL3, POISON_SPEW, State.ACID_SPEW).withCrouchingVariant(State.ACID_SPEW_CHARGED);
        moves.register(MoveType.ULTIMATE, isRemote() ? MELT_YOUR_HEART : STAND_DISC, isRemote() ? State.MELT_YOUR_HEART : State.DISC);

        moves.register(MoveType.UTILITY, PILOT_MODE);
    }

    @Override
    public void initMove(MoveType type) {
        if (type == MoveType.LIGHT && curMove != null && curMove.getMoveType() == MoveType.LIGHT && getMoveStun() < curMove.getWindupPoint()) {
            AbstractMove<?, ? super WhiteSnakeEntity> followup = curMove.getFollowup();
            if (followup != null) setMove(followup, (State) followup.getAnimation());
        } else super.initMove(type);
    }

    public void togglePilotMode() {
        setRemote(!isRemote());
        registerMoves(); // To switch the ultimate with the proper one.
    }

    @Override
    public void tick() {
        super.tick();

        if (!isRemote() || world.isClient) return;

        double f = getRemoteForwardInput();
        double s = getRemoteSideInput();
        boolean jump = getRemoteJumpInput();

        Vec3d pos = getPos();

        // 3 ticks of inertia, helping movement be fluid as well as dealing with packet drops
        if (lastRemoteInputTime - age > 4) updateRemoteInputs(0, 0, false);
        Vec3d rotVec = new Vec3d(getRotationVector().x, 0, getRotationVector().z).normalize();

        double dragMult = getMoveStun() > 0 ? 0.2 : 0.4;
        double moveSpeed = 0.24;
        //HitResult groundCheck = this.world.raycast(new RaycastContext(getEyePos(), pos.add(0, -1.0E-5F, 0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
        boolean onGround = isOnGround();

        if (getState() == State.IDLE) { // Replace idle anim
            if (s > 0) setStateNoReset(onGround ? State.RIGHT : State.RIGHT_DASH);
            if (s < 0) setStateNoReset(onGround ? State.LEFT : State.LEFT_DASH);
            if (f < 0) setStateNoReset(onGround ? State.BACKWARD : State.BACKWARD_DASH);
            if (f > 0) setStateNoReset(onGround ? State.FORWARD : State.FORWARD_DASH);
        }

        if (onGround) { // If grounded
            if (jump && getMoveStun() < 1) {
                remoteSpeed = new Vec3d(remoteSpeed.x, 0.25, remoteSpeed.z);
                setRemoteJumpInput(false);
            }
        } else {
            //JCraft.LOGGER.info("Airborne");
            moveSpeed = 0.024;
            remoteSpeed = remoteSpeed.add(0, -9.81 / 200, 0); // Account for gravity
            dragMult = 0.4;
        }

        remoteSpeed = remoteSpeed
                .add(rotVec.multiply(f * moveSpeed)) // Forward movement
                .add(rotVec.rotateY(1.5707963f).multiply(s * moveSpeed)); // Side movement

        remoteSpeed = remoteSpeed.multiply(dragMult, 1, dragMult);

        if (pos.add(remoteSpeed).squaredDistanceTo(getUserOrThrow().getPos()) > 400)
            remoteSpeed.multiply(-1);

        addVelocity(remoteSpeed.x, remoteSpeed.y, remoteSpeed.z);
        velocityDirty = true;
        velocityModified = true;
    }

    @Override
    protected @NonNull WhiteSnakeEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<WhiteSnakeEntity> {
        IDLE(builder -> builder.loop("animation.whitesnake.idle")),
        LIGHT(builder -> builder.playAndHold("animation.whitesnake.light")),
        BLOCK(builder -> builder.loop("animation.whitesnake.block")),
        MEDIUM(builder -> builder.playAndHold("animation.whitesnake.medium")),
        BARRAGE(builder -> builder.loop("animation.whitesnake.barrage")),
        LEG_CRUSHER(builder -> builder.playAndHold("animation.whitesnake.legcrusher")),
        ACID_SPEW(builder -> builder.playAndHold("animation.whitesnake.acidspew")),
        ACID_SPEW_CHARGED(builder -> builder.playAndHold("animation.whitesnake.acidspew_charged")),
        DISC(builder -> builder.playAndHold("animation.whitesnake.disc")),

        FORWARD(builder -> builder.loop("animation.whitesnake.forw")),
        BACKWARD(builder -> builder.loop("animation.whitesnake.back")),
        LEFT(builder -> builder.loop("animation.whitesnake.left")),
        RIGHT(builder -> builder.loop("animation.whitesnake.right")),
        FORWARD_DASH(builder -> builder.loop("animation.whitesnake.fdash")),
        BACKWARD_DASH(builder -> builder.loop("animation.whitesnake.bdash")),
        LEFT_DASH(builder -> builder.loop("animation.whitesnake.ldash")),
        RIGHT_DASH(builder -> builder.loop("animation.whitesnake.rdash")),

        MELT_YOUR_HEART(builder -> builder.playAndHold("animation.whitesnake.meltyourheart")),
        LIGHT_FOLLOWUP(builder -> builder.playAndHold("animation.whitesnake.light_followup"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(WhiteSnakeEntity attacker, AnimationBuilder builder) {
            animator.accept(builder);
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
