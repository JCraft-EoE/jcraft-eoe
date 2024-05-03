package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.hierophantgreen.EmeraldSplashAttack;
import net.arna.jcraft.common.attack.moves.hierophantgreen.NetSetMove;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.entity.projectile.HGNetEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.IOwnable;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.arna.jcraft.common.attack.moves.hierophantgreen.EmeraldSplashAttack.CHARGE_TIME;

public class HGEntity extends StandEntity<HGEntity, HGEntity.State> {
    public static final UppercutAttack<HGEntity> AIR_LIGHT = new UppercutAttack<HGEntity>(
            JCraft.LIGHT_COOLDOWN, 7, 14, 0.75f, 5f, 15, 1.5f, 0.4f, -0.3f, 0.4f)
            .withAnim(State.AIR_LIGHT)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(
                    Text.literal("Backward Flip Kick"),
                    Text.literal("launches up")
            );
    public static final KnockdownAttack<HGEntity> CROUCHING_LIGHT_FOLLOWUP = new KnockdownAttack<HGEntity>(
            0, 9, 16, 0.75f, 6f, 13, 1.75f, 0.75f, 0.4f, 35)
            .withSound(JSoundRegistry.HG_CROUCH_LIGHT)
            .withAnim(State.CROUCHING_LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Sweep"),
                    Text.literal("1.5s knockdown")
            );
    public static final SimpleAttack<HGEntity> CROUCHING_LIGHT = SimpleAttack.<HGEntity>lightAttack(
                    7, 11, 0.75f, 5f, 12, 0.15f, 0.3f)
            .withAnim(State.CROUCHING_LIGHT)
            .withFollowup(CROUCHING_LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(
                    Text.literal("Low Punch"),
                    Text.literal("quick combo starter")
            );

    public static final UppercutAttack<HGEntity> LIGHT_FOLLOWUP = new UppercutAttack<HGEntity>(
            0, 10, 15, 0.75f, 6f, 13, 1.75f, 0.5f, -0.2f, 0.4f)
            .withSound(JSoundRegistry.HG_LIGHT_FOLLOWUP)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withBlockStun(4)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Uppercut"),
                    Text.literal("reset tool, combos back into light")
            );
    public static final SimpleAttack<HGEntity> LIGHT = SimpleAttack.<HGEntity>lightAttack(
            7, 9, 0.75f, 5f, 10, 0.15f, 0.2f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(CROUCHING_LIGHT)
            .withAerialVariant(AIR_LIGHT)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(
                    Text.literal("Punch"),
                    Text.literal("quick combo starter")
            );
    public static final SimpleAttack<HGEntity> SENDOFF = new SimpleAttack<HGEntity>(
            180, 11, 20, 1, 8f, 16, 2f, 1.5f, 0)
            .withSound(JSoundRegistry.WS_DONUT)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withLaunch()
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withHyperArmor()
            .withInfo(
                    Text.literal("Sendoff"),
                    Text.literal("uninterruptible launcher")
            );
    public static final SimpleMultiHitAttack<HGEntity> BARRAGE = new SimpleMultiHitAttack<HGEntity>(
            200, 28, 1, 2f, 20, 2f, 0.3f, 0.25f,
            IntSet.of(3, 9, 15, 17, 25))
            .withSound(JSoundRegistry.HG_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(
                    Text.literal("Barrage"),
                    Text.literal("fast reliable combo starter/extender, medium stun")
            );

    public static final SimpleAttack<HGEntity> EXTEND_FORWARD_SECOND = new SimpleAttack<HGEntity>(
            0, 13, 21, 1f, 5, 16, 0, 0.4f, 0)
            .withHitAnimation(HitPropertyComponent.HitAnimation.LOW)
            .withExtraHitBox(2.5, -0.5, 1.5)
            .withExtraHitBox(3.5    , -0.6, 1.5)
            .withInfo(
                    Text.literal("Extend (Forward, Second Hit)"),
                    Text.empty()
            );
    public static final SimpleAttack<HGEntity> EXTEND_FORWARD = new SimpleAttack<HGEntity>(
            100, 10, 21, 1f, 5, 15, 1.5f, 0.7f, 0.2f)
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withSound(JSoundRegistry.HG_EXTEND)
            .withExtraHitBox(2, -0.1, 1.5)
            .withFinisher(12, EXTEND_FORWARD_SECOND)
            .withInfo(
                    Text.literal("Extend (Forward)"),
                    Text.literal("Hierophant extends its arm forward in a far-reaching attack")
            );

    public static final SimpleAttack<HGEntity> EXTEND_UP_SECOND = new SimpleAttack<HGEntity>(
            0, 13, 21, 1f, 5, 16, 0, 0.4f, 0)
            .withHitAnimation(HitPropertyComponent.HitAnimation.HIGH)
            .withExtraHitBox(2, 0.5, 1.5)
            .withExtraHitBox(3, 0.75, 1.5)
            .withInfo(
                    Text.literal("Extend (Upward, Second Hit)"),
                    Text.empty()
            );
    public static final SimpleAttack<HGEntity> EXTEND_UP = new SimpleAttack<HGEntity>(
            100, 10, 21, 1f, 5, 15, 1.5f, 0.7f, -0.2f)
            .withCrouchingVariant(EXTEND_FORWARD)

            .withSound(JSoundRegistry.HG_EXTEND)
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withExtraHitBox(2, 0.1, 1.5)
            .withFinisher(12, EXTEND_UP_SECOND)
            .withInfo(
                    Text.literal("Extend (Upward)"),
                    Text.literal("Hierophant extends its arm upward in a far-reaching attack")
            );

    public static final EmeraldSplashAttack EMERALD_SPLASH = new EmeraldSplashAttack(0, 12, 1, 0, 0, 0, 0,
            IntSet.of(1, 3, 5), 1.5f)
            .withSound(JSoundRegistry.HG_SPLASH)
            .withInfo(
                    Text.literal("Emerald Splash (Fire)"),
                    Text.empty()
            );
    public static final HoldableMove<HGEntity, State> EMERALD_CHARGE = new HoldableMove<>(100, 0, 40, 1,
            EMERALD_SPLASH, State.EMERALD_SPLASH, 7)
            .withInitAction(
                    (attacker, user, ctx) -> ctx.setInt(CHARGE_TIME, 0)
            )
            .withInfo(
                    Text.literal("Emerald Splash"),
                    Text.literal("""
                    Fires 3 bursts of emeralds at the opponent.
                    Bursts contain 3-6 emeralds depending on how long you hold."""));

    public static final NetSetMove NET_SET = new NetSetMove(200, 9, 15, 1f)
            .withSound(JSoundRegistry.HG_NET_SET)
            .withInfo(
                    Text.literal("Tentacle Place"),
                    Text.literal("""
                    Places a Hierophant Tentacle at Hierophant's feet.
                    Tentacles automatically grasp anything that touches them that isn't the user (10s cooldown).
                    Use crouching Emerald Splash to fire from the Tentacles remotely.
                    Tentacles cannot fire if grabbing.
                    """));
    public static final PilotModeMove<HGEntity> PILOT_MODE = new PilotModeMove<HGEntity>(20)
            .withInfo(
                    Text.literal("Pilot Mode"),
                    Text.empty()
            );

    public static final EmeraldSplashAttack EMERALD_SUPER = new EmeraldSplashAttack(500, 40, 1, 0, 0, 0, 0,
            IntSet.of(12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32), 2f)
            .withInitAction(
                    (attacker, user, ctx) -> ctx.setInt(CHARGE_TIME, 0)
            )
            .withInitAction(
                    (attacker, user, ctx) -> {
                        LivingEntity shooter = attacker.isRemote() ? attacker : user;

                        Vec3d upVec = GravityChangerAPI.getEyeOffset(shooter);
                        Vec3d heightOffset = upVec.multiply(0.5);
                        Vec3d eyePos = shooter.getPos().add(heightOffset);
                        Vec3d pos = JUtils.raycastAll(shooter, eyePos, eyePos.add(user.getRotationVector().multiply(96)), RaycastContext.FluidHandling.NONE,
                                (entity -> !(entity instanceof IOwnable ownable) || ownable.getMaster() != user));

                        user.getWorld().getEntitiesByClass(HGNetEntity.class, user.getBoundingBox().expand(96), EntityPredicates.VALID_LIVING_ENTITY)
                                .stream()
                                .filter(hgNetEntity -> hgNetEntity.getMaster() == user)
                                .forEach(hgNetEntity -> hgNetEntity.tryFireAt(pos, true));
                    }
            )
            .withReflect()
            .withSound(JSoundRegistry.HG_SPLASH)
            .withInfo(
                    Text.literal("All-Consuming Emerald Splash"),
                    Text.literal("""
                    Fires a long, oppressive stream of emeralds at the opponent.
                    These emeralds may bounce off walls up to 5 times.
                    Nearby Tentacles will do the same, but immediately start wilting after use.
                    """));

    public HGEntity(World worldIn) {
        super(StandType.HIEROPHANT_GREEN, worldIn, JSoundRegistry.HG_SUMMON);
        idleRotation = 220f;

        description = "Long-range ZONER";

        pros = List.of(
                "best ranged coverage",
                "good speed on most moves",
                "tentacles are extremely multipurpose"
        );

        cons = List.of(
                "mediocre combos without tentacles",
                "mediocre close-range coverage"
        );

        freespace =
                """
                        BNBs:
                            -the calamari
                            M1>Barrage>Net Set>delay.Emarald Splash>crouch.Emerald Splash>
                            ...Extend>crouch.M1~M1
                            ...Sendoff""";

        auraColors = new Vector3f[]{
                new Vector3f(0.2f, 0.9f, 0.2f),
                new Vector3f(0.2f, 0.2f, 0.9f),
                new Vector3f(0.4f, 0.4f, 0.5f),
                new Vector3f(1.0f, 0.65f, 0.44f)
        };
    }

    @Override
    protected void registerMoves(MoveMap<HGEntity, State> moves) {
        MoveMap.Entry<HGEntity, State> light = moves.register(MoveType.LIGHT, LIGHT, State.LIGHT);
        light.withFollowUp(State.LIGHT_FOLLOWUP);
        MoveMap.Entry<HGEntity, State> crouchingLight = light.withCrouchingVariant(State.CROUCHING_LIGHT);
        crouchingLight.withFollowUp(State.CROUCHING_LIGHT_FOLLOWUP);
        light.withAerialVariant(State.AIR_LIGHT);

        moves.register(MoveType.HEAVY, SENDOFF, State.SENDOFF);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, EMERALD_CHARGE, State.EMERALD_CHARGE);
        moves.register(MoveType.SPECIAL2, EXTEND_UP, State.EXTEND_UP).withCrouchingVariant(State.EXTEND_FORWARD);
        moves.register(MoveType.SPECIAL3, NET_SET, State.NET_SET);

        moves.register(MoveType.ULTIMATE, EMERALD_SUPER, State.EMERALD_SUPER);

        moves.register(MoveType.UTILITY, PILOT_MODE);
    }

    @Override
    public boolean initMove(MoveType type) {
        LivingEntity user = getUserOrThrow();
        if (type == MoveType.LIGHT && curMove != null && curMove.getMoveType() == MoveType.LIGHT && getMoveStun() < curMove.getWindupPoint()) {
            AbstractMove<?, ? super HGEntity> followup = curMove.getFollowup();
            if (followup != null) setMove(followup, (State) followup.getAnimation());
        } else if (type == MoveType.SPECIAL1 && user.isSneaking()) {
            if (!JUtils.canAct(user)) return false;

            List<HGNetEntity> nets = getWorld().getEntitiesByClass(HGNetEntity.class,
                    getBoundingBox().expand(64), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

            LivingEntity shooter = isRemote() ? this : user;

            Vec3d upVec = GravityChangerAPI.getEyeOffset(shooter);
            Vec3d heightOffset = upVec.multiply(0.5);
            Vec3d eyePos = shooter.getPos().add(heightOffset);

            if (!nets.isEmpty()) {
                Vec3d pos = JUtils.raycastAll(shooter, eyePos, eyePos.add(user.getRotationVector().multiply(96)), RaycastContext.FluidHandling.NONE,
                        (entity -> {
                            if (entity instanceof IOwnable ownable && ownable.getMaster() == user)
                                return false;
                            return true;
                        }));

                for (HGNetEntity net : nets) {
                    if (net.getMaster() != user) continue;
                    net.tryFireAt(pos, false);
                }
            }
        } else return super.initMove(type);

        return true;
    }

    public void togglePilotMode() {
        setRemote(!isRemote());
        registerMoves(); // To switch the ultimate with the proper one.
    }

    @Override
    public void tick() {
        super.tick();

        if (!getWorld().isClient) {
            if (curMove != null && curMove.getOriginalMove() == EMERALD_CHARGE)
                getMoveContext().incrementInt(CHARGE_TIME, 1);
        }

        boolean isRemote = isRemote();
        setNoGravity(isRemote);
        if (!isRemote) return;

        if (getWorld().isClient) {
            // Called for EVERYONE
            JCraft.getClientEntityHandler().hierophantGreenRemoteClientTick(this);
        } else {
            double f = getRemoteForwardInput();
            double s = getRemoteSideInput();

            tickRemoteMovement(f, s, getRemoteJumpInput(), getRemoteSneakInput());

            if (getState() == State.IDLE && getMoveStun() <= 0) { // Replace idle anim
                if (s > 0) setStateNoReset(State.RIGHT);
                if (s < 0) setStateNoReset(State.LEFT);
                if (f < 0) setStateNoReset(State.BACKWARD);
                if (f > 0) setStateNoReset(State.FORWARD);
            }
        }
    }

    public void tickRemoteMovement(double f, double s, boolean jump, boolean sneak) {
        Vec3d pos = getPos();
        onLanding();

        // 1 tick of inertia, helping movement be fluid as well as dealing with packet drops
        if (lastRemoteInputTime - age > 2) updateRemoteInputs(0, 0, false, false);
        Vec3d rotVec = new Vec3d(getRotationVector().x, 0, getRotationVector().z).normalize();

        double dragMult = getMoveStun() > 0 ? 0.1 : 0.2;
        double moveSpeed = 0.5;

        Vec3d upVec = GravityChangerAPI.getEyeOffset(this);

        if (jump)
            remoteSpeed = remoteSpeed.add(upVec.multiply(moveSpeed));

        if (sneak)
            remoteSpeed = remoteSpeed.subtract(upVec.multiply(moveSpeed));

        remoteSpeed = remoteSpeed
                .add(rotVec.multiply(f * moveSpeed)) // Forward movement
                .add(rotVec.rotateY(1.5707963f).multiply(s * moveSpeed)); // Side movement

        remoteSpeed = remoteSpeed.multiply(dragMult);

        Vec3d userPos = getUserOrThrow().getPos();
        if (pos.add(remoteSpeed).squaredDistanceTo(userPos) > 30 * 30)
            remoteSpeed = userPos.subtract(pos).multiply(0.025); // 1/40th so it scales with distance

        addVelocity(-getVelocity().x * 0.2, -getVelocity().y * 0.2, -getVelocity().z * 0.2);
        addVelocity(remoteSpeed.x, remoteSpeed.y, remoteSpeed.z);
        velocityDirty = true;
        velocityModified = true;
    }


    @Override
    @NonNull
    public HGEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<HGEntity> {
        IDLE((hg, builder) -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.hg.idle"))),
        LIGHT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.hg.light"))),
        LIGHT_FOLLOWUP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.hg.light_followup"))),
        CROUCHING_LIGHT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.hg.crouching_light"))),
        CROUCHING_LIGHT_FOLLOWUP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.hg.crouching_light_followup"))),
        AIR_LIGHT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.hg.air_light"))),
        BLOCK(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.hg.block"))),
        SENDOFF(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.hg.sendoff"))),
        BARRAGE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.hg.barrage"))),
        NET_SET(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.hg.net_place"))),

        EMERALD_CHARGE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.hg.emerald_charge"))),
        EMERALD_SPLASH(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.hg.emerald_splash"))),
        EMERALD_SUPER(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.hg.emerald_super"))),
        EXTEND_UP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.hg.extend_up"))),
        EXTEND_FORWARD(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.hg.extend_forward"))),

        UPPERCUT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.hg.uppercut"))),

        FORWARD(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.hg.forw"))),
        BACKWARD(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.hg.back"))),
        LEFT(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.hg.left"))),
        RIGHT(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.hg.right")));

        private final BiConsumer<HGEntity, AnimationState> animator;

        State(Consumer<AnimationState> animator) {
            this((whiteSnake, builder) -> animator.accept(builder));
        }

        State(BiConsumer<HGEntity, AnimationState> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(HGEntity attacker, AnimationState builder) {
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
