package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.common.attack.moves.hierophantgreen.EmeraldSplashAttack;
import net.arna.jcraft.common.attack.moves.hierophantgreen.NetSetMove;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.entity.projectile.HGNetEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.IOwnable;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/Hierophant_Green">Hierophant Green</a>.
 * @see JStandTypeRegistry#HIEROPHANT_GREEN
 * @see EmeraldSplashAttack
 * @see NetSetMove
 */
public class HGEntity extends StandEntity<HGEntity, HGEntity.State> {
    public static final MoveSet<HGEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.HIEROPHANT_GREEN,
            HGEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.builder()
            .idleRotation(220f)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.hierophantgreen"))
                    .proCount(3)
                    .conCount(2)
                    .freeSpace(Component.literal("""
                        BNBs:
                            -the calamari
                            Light>Barrage>Net Set>delay.Emarald Splash>crouch.Emerald Splash>
                            ...Extend>crouch.Light~Light
                            ...Sendoff"""))
                    .skinName(Component.literal("Cold"))
                    .skinName(Component.literal("Burning"))
                    .skinName(Component.literal("Seaside"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.HG_SUMMON))
            .build();

    public static final SimpleUppercutAttack<HGEntity> AIR_LIGHT = new SimpleUppercutAttack<HGEntity>(0,
            7, 14, 0.75f, 5f, 15, 1.5f, 0.4f, -0.3f, 0.4f)
            .withAnim(State.AIR_LIGHT)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(
                    Component.literal("Backward Flip Kick"),
                    Component.literal("launches up")
            );
    public static final KnockdownAttack<HGEntity> CROUCHING_LIGHT_FOLLOWUP = new KnockdownAttack<HGEntity>(0,
            9, 16, 0.75f, 6f, 13, 1.75f, 0.75f, 0.4f, 35)
            .withSound(JSoundRegistry.HG_CROUCH_LIGHT)
            .withAnim(State.CROUCHING_LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Sweep"),
                    Component.literal("1.5s knockdown")
            );
    public static final SimpleAttack<HGEntity> CROUCHING_LIGHT = SimpleAttack.<HGEntity>lightAttack(
                    7, 11, 0.75f, 5f, 12, 0.15f, 0.3f)
            .withAnim(State.CROUCHING_LIGHT)
            .withFollowup(CROUCHING_LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(
                    Component.literal("Low Punch"),
                    Component.literal("quick combo starter")
            );

    public static final SimpleUppercutAttack<HGEntity> LIGHT_FOLLOWUP = new SimpleUppercutAttack<HGEntity>(0,
            10, 15, 0.75f, 6f, 13, 1.75f, 0.5f, -0.2f, 0.4f)
            .withSound(JSoundRegistry.HG_LIGHT_FOLLOWUP)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withBlockStun(4)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Uppercut"),
                    Component.literal("reset tool, combos back into light")
            );
    public static final SimpleAttack<HGEntity> LIGHT = SimpleAttack.<HGEntity>lightAttack(
                    7, 9, 0.75f, 5f, 10, 0.15f, 0.2f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(CROUCHING_LIGHT)
            .withAerialVariant(AIR_LIGHT)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("quick combo starter")
            );
    public static final SimpleAttack<HGEntity> SENDOFF = new SimpleAttack<HGEntity>(0,
            11, 20, 1, 8f, 16, 2f, 1.5f, 0)
            .withSound(JSoundRegistry.WS_DONUT)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withLaunch()
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withHyperArmor()
            .withInfo(
                    Component.literal("Sendoff"),
                    Component.literal("uninterruptible launcher")
            );
    public static final SimpleMultiHitAttack<HGEntity> BARRAGE = new SimpleMultiHitAttack<HGEntity>(
            200, 28, 1, 2f, 20, 2f, 0.3f, 0.25f,
            IntSet.of(3, 9, 15, 17, 25))
            .withSound(JSoundRegistry.HG_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(
                    Component.literal("Barrage"),
                    Component.literal("fast reliable combo starter/extender, medium stun")
            );

    public static final SimpleAttack<HGEntity> EXTEND_FORWARD_SECOND = new SimpleAttack<HGEntity>(0,
            13, 21, 1f, 5, 16, 0, 0.4f, 0)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.LOW)
            .withExtraHitBox(2.5, -0.5, 1.5)
            .withExtraHitBox(3.5, -0.6, 1.5)
            .withInfo(
                    Component.literal("Extend (Forward, Second Hit)"),
                    Component.empty()
            );
    public static final SimpleAttack<HGEntity> EXTEND_FORWARD = new SimpleAttack<HGEntity>(0,
            10, 21, 1f, 5, 15, 1.5f, 0.7f, 0.2f)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withSound(JSoundRegistry.HG_EXTEND)
            .withExtraHitBox(2, -0.1, 1.5)
            .withFinisher(12, EXTEND_FORWARD_SECOND)
            .withInfo(
                    Component.literal("Extend (Forward)"),
                    Component.literal("Hierophant extends its arm forward in a far-reaching attack")
            );

    public static final SimpleAttack<HGEntity> EXTEND_UP_SECOND = new SimpleAttack<HGEntity>(0,
            13, 21, 1f, 5, 16, 0, 0.4f, 0)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .withExtraHitBox(2, 0.5, 1.5)
            .withExtraHitBox(3, 0.75, 1.5)
            .withInfo(
                    Component.literal("Extend (Upward, Second Hit)"),
                    Component.empty()
            );
    public static final SimpleAttack<HGEntity> EXTEND_UP = new SimpleAttack<HGEntity>(0,
            10, 21, 1f, 5, 15, 1.5f, 0.7f, -0.2f)
            .withCrouchingVariant(EXTEND_FORWARD)

            .withSound(JSoundRegistry.HG_EXTEND)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withExtraHitBox(2, 0.1, 1.5)
            .withFinisher(12, EXTEND_UP_SECOND)
            .withInfo(
                    Component.literal("Extend (Upward)"),
                    Component.literal("Hierophant extends its arm upward in a far-reaching attack")
            );

    public static final EmeraldSplashAttack EMERALD_SPLASH = new EmeraldSplashAttack(0, 12,
            1, 0, 0, 0, 0, IntSet.of(1, 3, 5), 1.5f, false)
            .withSound(JSoundRegistry.HG_SPLASH)
            .withInfo(
                    Component.literal("Emerald Splash (Fire)"),
                    Component.empty()
            );
    public static final SimpleHoldableMove<HGEntity> EMERALD_CHARGE = new SimpleHoldableMove<HGEntity>(100,
            0, 40, 1, 7)
            .withFollowup(EMERALD_SPLASH)
            .withInfo(
                    Component.literal("Emerald Splash"),
                    Component.literal("""
                            Fires 3 bursts of emeralds at the opponent.
                            Bursts contain 3-6 emeralds depending on how long you hold."""));

    public static final NetSetMove NET_SET = new NetSetMove(200, 9, 15, 1f)
            .withSound(JSoundRegistry.HG_NET_SET)
            .withInfo(
                    Component.literal("Tentacle Place"),
                    Component.literal("""
                            Places a Hierophant Tentacle at Hierophant's feet.
                            Tentacles automatically grasp anything that touches them that isn't the user (10s cooldown).
                            Use crouching Emerald Splash to fire from the Tentacles remotely.
                            Tentacles cannot fire if grabbing.
                            """));
    public static final PilotModeMove<HGEntity> PILOT_MODE = new PilotModeMove<HGEntity>(20)
            .withInfo(
                    Component.literal("Pilot Mode"),
                    Component.empty()
            );

    public static final EmeraldSplashAttack EMERALD_SUPER = new EmeraldSplashAttack(500, 40,
            1, 0, 0, 0, 0, IntSet.of(12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32), 2f, true)
            .withReflect()
            .withSound(JSoundRegistry.HG_SPLASH)
            .withInfo(
                    Component.literal("All-Consuming Emerald Splash"),
                    Component.literal("""
                            Fires a long, oppressive stream of emeralds at the opponent.
                            These emeralds may bounce off walls up to 5 times.
                            Nearby Tentacles will do the same, but immediately start wilting after use.
                            """));

    public HGEntity(Level worldIn) {
        super(JStandTypeRegistry.HIEROPHANT_GREEN.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(0.2f, 0.9f, 0.2f),
                new Vector3f(0.2f, 0.2f, 0.9f),
                new Vector3f(0.4f, 0.4f, 0.5f),
                new Vector3f(1.0f, 0.65f, 0.44f)
        };
    }

    private static void registerMoves(MoveMap<HGEntity, State> moves) {
        MoveMap.Entry<HGEntity, State> light = moves.register(MoveClass.LIGHT, LIGHT, State.LIGHT);
        light.withFollowup(State.LIGHT_FOLLOWUP);
        MoveMap.Entry<HGEntity, State> crouchingLight = light.withCrouchingVariant(State.CROUCHING_LIGHT);
        crouchingLight.withFollowup(State.CROUCHING_LIGHT_FOLLOWUP);
        light.withAerialVariant(State.AIR_LIGHT);

        moves.register(MoveClass.HEAVY, SENDOFF, State.SENDOFF);
        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveClass.SPECIAL1, EMERALD_CHARGE, State.EMERALD_CHARGE).withFollowup(State.EMERALD_SPLASH);
        moves.register(MoveClass.SPECIAL2, EXTEND_UP, State.EXTEND_UP).withCrouchingVariant(State.EXTEND_FORWARD);
        moves.register(MoveClass.SPECIAL3, NET_SET, State.NET_SET);

        moves.register(MoveClass.ULTIMATE, EMERALD_SUPER, State.EMERALD_SUPER);

        moves.register(MoveClass.UTILITY, PILOT_MODE);
    }

    private void fireNearbyNets(@NonNull final LivingEntity user, boolean isSuper) {
        final List<HGNetEntity> nets = level().getEntitiesOfClass(HGNetEntity.class,
                getBoundingBox().inflate(64), EntitySelector.NO_CREATIVE_OR_SPECTATOR);

        final LivingEntity shooter = isRemote() ? this : user;

        final Vec3 heightOffset = GravityChangerAPI.getEyeOffset(shooter).scale(0.5);
        final Vec3 eyePos = shooter.position().add(heightOffset);

        if (nets.isEmpty()) return;

        final Vec3 pos = JUtils.raycastAll(shooter, eyePos, eyePos.add(user.getLookAngle().scale(96)), ClipContext.Fluid.NONE,
                (entity -> !(entity instanceof IOwnable ownable) || ownable.getMaster() != user)).getLocation();

        for (HGNetEntity net : nets) {
            if (net.getMaster() != user) {
                continue;
            }
            net.tryFireAt(pos, isSuper);
        }
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        final LivingEntity user = getUserOrThrow();
        if (tryFollowUp(moveClass, MoveClass.LIGHT)) return true;

        if (moveClass != MoveClass.SPECIAL1 || !user.isShiftKeyDown()) {
            // Ultimate Remote Fire
            if (moveClass == MoveClass.ULTIMATE) {
                if (super.initMove(MoveClass.ULTIMATE)) {
                    fireNearbyNets(user, true);
                    return true;
                }
                return false;
            }

            return super.initMove(moveClass);
        }

        // cr.SP1 Remote Fire
        if (!JUtils.canAct(user)) return false;
        fireNearbyNets(user, false);
        return true;
    }

    public void togglePilotMode() {
        setRemote(!isRemote());
        switchMoveSet(MOVE_SET.getName()); // To switch the ultimate with the proper one.
    }

    @Override
    public double getEngagementDistance() {
        return 64.0;
    }

    @Override
    public void tick() {
        super.tick();

        final boolean isRemote = isRemote();
        idleOverride = isRemote;
        setNoGravity(isRemote);

        if (!isRemote) {
            return;
        }

        if (level().isClientSide) {
            // Called for EVERYONE
            JCraft.getClientEntityHandler().hierophantGreenRemoteClientTick(this);
        } else {
            final double f = getRemoteForwardInput(), s = getRemoteSideInput();

            tickRemoteMovement(f, s, getRemoteJumpInput(), getRemoteSneakInput());

            if (getMoveStun() <= 0) {
                if (f == 0) {
                    if (s > 0) {
                        setStateNoReset(State.RIGHT);
                    } else if (s < 0) {
                        setStateNoReset(State.LEFT);
                    } else {
                        setStateNoReset(State.IDLE);
                    }
                } else {
                    if (f < 0) {
                        setStateNoReset(State.BACKWARD);
                    }
                    if (f > 0) {
                        setStateNoReset(State.FORWARD);
                    }
                }
            }
        }
    }

    public void tickRemoteMovement(double f, double s, boolean jump, boolean sneak) {
        resetFallDistance();

        final Vec3 pos = position();
        // 1 tick of inertia, helping movement be fluid as well as dealing with packet drops
        if (lastRemoteInputTime - tickCount > 2) {
            updateRemoteInputs(0, 0, false, false);
        }
        final Vec3 rotVec = new Vec3(getLookAngle().x, 0, getLookAngle().z).normalize();

        final double dragMult = getMoveStun() > 0 ? 0.1 : 0.2;
        final double moveSpeed = 0.5;

        final Vec3 upVec = GravityChangerAPI.getEyeOffset(this);

        if (jump) {
            remoteSpeed = remoteSpeed.add(upVec.scale(moveSpeed));
        }

        if (sneak) {
            remoteSpeed = remoteSpeed.subtract(upVec.scale(moveSpeed));
        }

        remoteSpeed = remoteSpeed
                .add(rotVec.scale(f * moveSpeed)) // Forward movement
                .add(rotVec.yRot(1.5707963f).scale(s * moveSpeed)); // Side movement

        remoteSpeed = remoteSpeed.scale(dragMult);

        final double stabilization = (f == 0 && s == 0 && !jump && !sneak) ? 0.7 : 0.2;

        final Vec3 userPos = getUserOrThrow().position();
        if (pos.add(remoteSpeed).distanceToSqr(userPos) > 30 * 30) {
            remoteSpeed = userPos.subtract(pos).scale(0.025); // 1/40th so it scales with distance
        }

        push(-getDeltaMovement().x * stabilization, -getDeltaMovement().y * stabilization, -getDeltaMovement().z * stabilization);
        push(remoteSpeed.x, remoteSpeed.y, remoteSpeed.z);
        hasImpulse = true;
        hurtMarked = true;
    }


    @Override
    @NonNull
    public HGEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<HGEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.hg.idle", AzPlayBehaviors.LOOP)),
        LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.hg.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.hg.block", AzPlayBehaviors.LOOP)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.hg.light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        CROUCHING_LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.hg.crouching_light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        CROUCHING_LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.hg.crouching_light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        AIR_LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.hg.air_light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        SENDOFF(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.hg.sendoff", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.hg.barrage", AzPlayBehaviors.LOOP)),
        NET_SET(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.hg.net_place", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),

        EMERALD_CHARGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.hg.emerald_charge", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        EMERALD_SPLASH(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.hg.emerald_splash", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        EMERALD_SUPER(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.hg.emerald_super", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        EXTEND_UP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.hg.extend_up", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        EXTEND_FORWARD(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.hg.extend_forward", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),

        UPPERCUT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.hg.uppercut", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),

        FORWARD(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.hg.forw", AzPlayBehaviors.LOOP)),
        BACKWARD(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.hg.back", AzPlayBehaviors.LOOP)),
        LEFT(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.hg.left", AzPlayBehaviors.LOOP)),
        RIGHT(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.hg.right", AzPlayBehaviors.LOOP));

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(HGEntity attacker) {
            animator.sendForEntity(attacker);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
