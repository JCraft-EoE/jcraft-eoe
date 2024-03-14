package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.BlockableType;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.hierophantgreen.EmeraldSplashAttack;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.attack.moves.whitesnake.*;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class HGEntity extends StandEntity<HGEntity, HGEntity.State> {
    public static final KnockdownAttack<HGEntity> CROUCHING_LIGHT_FOLLOWUP = new KnockdownAttack<HGEntity>(
            0, 9, 16, 0.75f, 6f, 13, 1.75f, 0.75f, 0.4f, 35)
            .withAnim(State.CROUCHING_LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(Text.literal("Sweep"), Text.literal("1.5s knockdown"));
    public static final SimpleAttack<HGEntity> CROUCHING_LIGHT = SimpleAttack.<HGEntity>lightAttack(
                    7, 11, 0.75f, 5f, 12, 0.15f, 0.3f)
            .withAnim(State.CROUCHING_LIGHT)
            .withFollowup(CROUCHING_LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withInfo(Text.literal("Low Punch"), Text.literal("quick combo starter"));

    public static final UppercutAttack<HGEntity> LIGHT_FOLLOWUP = new UppercutAttack<HGEntity>(
            0, 10, 15, 0.75f, 6f, 13, 1.75f, 0.5f, -0.2f, 0.4f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withBlockStun(4)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(Text.literal("Uppercut"), Text.literal("combo extender"));
    public static final SimpleAttack<HGEntity> LIGHT = SimpleAttack.<HGEntity>lightAttack(
            6, 8, 0.75f, 5f, 10, 0.15f, 0.2f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(CROUCHING_LIGHT)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withInfo(Text.literal("Punch"), Text.literal("quick combo starter"));
    public static final SimpleAttack<HGEntity> SENDOFF = new SimpleAttack<HGEntity>(
            60, 11, 20, 1, 8f, 16, 2f, 1.5f, 0)
            .withSound(JSoundRegistry.WS_DONUT)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withLaunch()
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withHyperArmor()
            .withInfo(Text.literal("Sendoff"), Text.literal("uninterruptible launcher"));
    public static final MainBarrageAttack<HGEntity> BARRAGE = new MainBarrageAttack<HGEntity>(
            240, 0, 60, 0.75f, 1, 20, 2, 0.25f, 0, 3, Blocks.OAK_PLANKS.getHardness())
            .withSound(JSoundRegistry.WS_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(Text.literal("Barrage"), Text.literal("fast reliable combo starter/extender, medium stun"));
    public static final SimpleAttack<HGEntity> LEG_CRUSHER = new SimpleAttack<HGEntity>(
            240, 16, 22, 0.75f, 7, 32, 1.75f, 0.35f, 0.2f)
            .withSound(JSoundRegistry.WS_LEGCRUSH)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHitAnimation(HitPropertyComponent.HitAnimation.LOW)
            .withInfo(Text.literal("Leg Crusher"), Text.literal("high stun, medium windup"));
    public static final EmeraldSplashAttack EMERALD_SPLASH = new EmeraldSplashAttack(100, 20, 1, 0, 0, 0, 0,
            IntSet.of(8, 10, 12))
            .withInfo(Text.literal("Emerald Splash"), Text.literal("fires 9 emeralds at the opponent"));
    public static final ChargedSpewAttack CHARGED_SPEW = new ChargedSpewAttack(
            200, 20, 26, 0.75f, 0f, 0, 2f, 0f, 0f)
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withInfo(Text.literal("Poison Spew"), Text.literal("fires a spread of 5 acid projectiles that slow enemies and persist on the surface they hits for 5s"));
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
    public static final PilotModeMove<HGEntity> PILOT_MODE = new PilotModeMove<HGEntity>(20)
            .withInfo(Text.literal("Pilot Mode"), Text.empty());

    public HGEntity(World worldIn) {
        super(StandType.HIEROPHANT_GREEN, worldIn, JSoundRegistry.WS_SUMMON);
        idleRotation = 220f;

        description = "Z";

        pros = List.of(
                "a"
        );

        cons = List.of(
                "b"
        );

        freespace =
                """
                        BNBs:
                            -a""";

        auraColors = new Vec3f[]{
                new Vec3f(0.2f, 0.9f, 0.2f),
                new Vec3f(0.2f, 0.2f, 0.9f),
                new Vec3f(0.4f, 0.4f, 0.5f),
                new Vec3f(1.0f, 0.0f, 0.0f)
        };
    }

    @Override
    protected void registerMoves(MoveMap<HGEntity, State> moves) {
        MoveMap.Entry<HGEntity, State> light = moves.register(MoveType.LIGHT, LIGHT, State.LIGHT);
        light.withFollowUp(State.LIGHT_FOLLOWUP);
        MoveMap.Entry<HGEntity, State> crouchingLight = light.withCrouchingVariant(State.CROUCHING_LIGHT);
        crouchingLight.withFollowUp(State.CROUCHING_LIGHT_FOLLOWUP);

        moves.register(MoveType.HEAVY, SENDOFF, State.SENDOFF);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, EMERALD_SPLASH, State.EMERALD_SPLASH);
        moves.register(MoveType.SPECIAL2, LEG_CRUSHER, State.LEG_CRUSHER);

        moves.register(MoveType.UTILITY, PILOT_MODE);
    }

    @Override
    public void initMove(MoveType type) {
        if (type == MoveType.LIGHT && curMove != null && curMove.getMoveType() == MoveType.LIGHT && getMoveStun() < curMove.getWindupPoint()) {
            AbstractMove<?, ? super HGEntity> followup = curMove.getFollowup();
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

        boolean isRemote = isRemote();
        setNoGravity(isRemote);
        if (!isRemote) return;

        if (world.isClient) {
            // Called for EVERYONE
            JCraft.getClientEntityHandler().hierophantGreenRemoteClientTick(this);
        } else {
            double f = getRemoteForwardInput();
            double s = getRemoteSideInput();
            boolean jump = getRemoteJumpInput();

            tickRemoteMovement(f, s, jump);

            if (getState() == State.IDLE) { // Replace idle anim
                if (s > 0) setStateNoReset(onGround ? State.RIGHT : State.RIGHT_DASH);
                if (s < 0) setStateNoReset(onGround ? State.LEFT : State.LEFT_DASH);
                if (f < 0) setStateNoReset(onGround ? State.BACKWARD : State.BACKWARD_DASH);
                if (f > 0) setStateNoReset(onGround ? State.FORWARD : State.FORWARD_DASH);
            }
        }
    }

    public void tickRemoteMovement(double f, double s, boolean jump) {
        Vec3d pos = getPos();

        // 1 tick of inertia, helping movement be fluid as well as dealing with packet drops
        if (lastRemoteInputTime - age > 2) updateRemoteInputs(0, 0, false);
        Vec3d rotVec = new Vec3d(getRotationVector().x, 0, getRotationVector().z).normalize();

        double dragMult = getMoveStun() > 0 ? 0.2 : 0.4;
        double moveSpeed = 0.24;
        boolean swimming = !world.getFluidState(getBlockPos()).isEmpty();

        if (swimming) dragMult *= 0.5;

        if (jump)
            addVelocity(0, 0.1, 0);

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
    protected @NonNull HGEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<HGEntity> {
        IDLE((hg, builder) -> builder.loop("animation.hg.idle")),
        LIGHT(builder -> builder.playAndHold("animation.hg.light")),
        LIGHT_FOLLOWUP(builder -> builder.playAndHold("animation.hg.light_followup")),
        CROUCHING_LIGHT(builder -> builder.playAndHold("animation.hg.crouching_light")),
        CROUCHING_LIGHT_FOLLOWUP(builder -> builder.playAndHold("animation.hg.crouching_light_followup")),
        BLOCK(builder -> builder.loop("animation.hg.block")),
        SENDOFF(builder -> builder.playAndHold("animation.hg.sendoff")),
        BARRAGE(builder -> builder.loop("animation.hg.barrage")),
        LEG_CRUSHER(builder -> builder.playAndHold("animation.hg.legcrusher")),
        ACID_SPEW(builder -> builder.playAndHold("animation.hg.acidspew")),
        ACID_SPEW_CHARGED(builder -> builder.playAndHold("animation.hg.acidspew_charged")),
        EMERALD_SPLASH(builder -> builder.playAndHold("animation.hg.emerald_splash")),
        DISC_GIVE(builder -> builder.playAndHold("animation.hg.disc_give")),
        UPPERCUT(builder -> builder.playAndHold("animation.hg.uppercut")),

        FORWARD(builder -> builder.loop("animation.hg.forw")),
        BACKWARD(builder -> builder.loop("animation.hg.back")),
        LEFT(builder -> builder.loop("animation.hg.left")),
        RIGHT(builder -> builder.loop("animation.hg.right")),
        FORWARD_DASH(builder -> builder.loop("animation.hg.fdash")),
        BACKWARD_DASH(builder -> builder.loop("animation.hg.bdash")),
        LEFT_DASH(builder -> builder.loop("animation.hg.ldash")),
        RIGHT_DASH(builder -> builder.loop("animation.hg.rdash")),;

        private final BiConsumer<HGEntity, AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this((whiteSnake, builder) -> animator.accept(builder));
        }

        State(BiConsumer<HGEntity, AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(HGEntity attacker, AnimationBuilder builder) {
            animator.accept(attacker, builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @Nullable String getSummonAnimation() {
        return "animation.hg.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
