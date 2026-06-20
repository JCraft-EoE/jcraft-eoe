package net.arna.jcraft.common.entity.stand;

import com.mojang.datafixers.util.Either;
import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.component.living.CommonCooldownsComponent;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.common.attack.actions.EffectAction;
import net.arna.jcraft.common.attack.moves.madeinheaven.*;
import net.arna.jcraft.common.attack.moves.shared.KnockdownAttack;
import net.arna.jcraft.common.attack.moves.shared.MainBarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.TossChargeMove;
import net.arna.jcraft.common.attack.moves.shared.TossMove;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.effects.ExhaustionEffect;
import net.arna.jcraft.common.network.s2c.TimeAccelStatePacket;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/Made_in_Heaven">Made In Heaven</a>.
 * @see JStandTypeRegistry#MADE_IN_HEAVEN
 * @see net.arna.jcraft.client.renderer.entity.stands.MadeInHeavenRenderer MadeInHeavenRenderer
 * @see CircleAttack
 * @see FuryChopAttack
 * @see JudgementAttack
 * @see SpeedSliceAttack
 * @see TimeAccelerationMove
 */
public class MadeInHeavenEntity extends StandEntity<MadeInHeavenEntity, MadeInHeavenEntity.State> {
    public static final MoveSet<MadeInHeavenEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.MADE_IN_HEAVEN,
            MadeInHeavenEntity::registerMoves, MadeInHeavenEntity.class, State.class);
    public static final StandData DATA = StandData.builder()
            .idleRotation(-45f)
            .evolution(true)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.mih"))
                    .proCount(4)
                    .conCount(2)
                    .freeSpace(Component.literal("""
                        PASSIVE: Acceleration
                            moving forward ramps up to Speed 70 over 1s
                            backstepping cancels the buildup
                            faster movement drains more hunger
                            (backstep drains 2x); autostep is always on

                        BNBs:
                            -the flashbang
                            (Donut>Light>)Speed Slice>Low Kick>Fury Chop>Light>Barrage>dash>Light~Light
                        """))
                    .skinName(Component.literal("Cruel"))
                    .skinName(Component.literal("Daft"))
                    .skinName(Component.literal("Nightmare"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.MIH_SUMMON))
            .build();

    public static final SimpleAttack<MadeInHeavenEntity> SPEED_CHOP = new SimpleAttack<MadeInHeavenEntity>(0,
            6, 11, 0.75f, 3f, 8, 1.5f, 0.5f, -0.1f)
            .withAnim(State.SPEED_CHOP)
            .withAction(EffectAction.inflict(JStatusRegistry.BLEEDING, 80, 1, true, false, true))
            .withImpactSound(SoundEvents.TRIDENT_HIT)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Component.literal("Speed Chop"),
                    Component.literal("tiny stun, procs bleed")
            );
    public static final SimpleAttack<MadeInHeavenEntity> LIGHT_FOLLOWUP = new SimpleAttack<MadeInHeavenEntity>(
            0, 6, 12, 0.75f, 5, 8, 1.5f, 1f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0.25, 1)
            .withInfo(
                    Component.literal("Kick"),
                    Component.literal("quick combo finisher")
            );
    public static final SimpleAttack<MadeInHeavenEntity> SLICE = new SimpleAttack<MadeInHeavenEntity>(11,
            5, 8, 0.75f, 4f, 10, 1.5f, 0.15f, -0.1f)
            .noLoopPrevention()
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(SPEED_CHOP)
            .withImpactSound(SoundEvents.TRIDENT_HIT)
            .withInfo(
                    Component.literal("Slice"),
                    Component.literal("quick combo starter")
            );
    public static final SimpleAttack<MadeInHeavenEntity> BARRAGE_FINISHER = new SimpleAttack<MadeInHeavenEntity>(0,
            6, 9, 0.85f, 1f, 10, 1.5f, 1.1f, 0f)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(
                    Component.literal("Barrage (Final Hit)"),
                    Component.empty()
            );
    public static final MainBarrageAttack<MadeInHeavenEntity> BARRAGE = new MainBarrageAttack<MadeInHeavenEntity>(200,
            0, 32, 0.85f, 1f, 10, 2f, 0.1f, 0f, 2, Blocks.OAK_PLANKS.defaultDestroyTime())
            .withFinisher(23, BARRAGE_FINISHER)
            .withSound(JSoundRegistry.MIH_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Component.literal("Barrage"),
                    Component.literal("short, knocks back")
            );
    public static final SpeedSliceAttack SPEED_SLICE = new SpeedSliceAttack(300, 10, 11,
            1.25f, 6f, 1.5f, 1f)
            .withSound(JSoundRegistry.MIH_SPEEDSLICE)
            .withInfo(
                    Component.literal("Speed Slice"),
                    Component.literal("short windup, harming teleport with hitstun and light knockback")
            );
    public static final KnockdownAttack<MadeInHeavenEntity> LEG_CRUSHER = new KnockdownAttack<MadeInHeavenEntity>(19,
            9, 19, 0.85f, 7f, 22, 1.5f, 0.35f, 0.2f, 45)
            .withSound(JSoundRegistry.MIH_LEGCRUSHER)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withExtraHitBox(0, -0.5, 1)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.LOW)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Leg Crusher"),
                    Component.literal("knocks down (2s)")
            );
    public static final SimpleAttack<MadeInHeavenEntity> LOW_KICK = new SimpleAttack<MadeInHeavenEntity>(17,
            8, 17, 0.85f, 6f, 26, 1.5f, 0.25f, 0.2f)
            .withCrouchingVariant(LEG_CRUSHER)
            .withSound(JSoundRegistry.MIH_LEGCRUSHER)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withExtraHitBox(0, -0.5, 1)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.LOW)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Low Kick"),
                    Component.literal("combo starter/extender, mih hoofs the enemies legs in a quick, stunning attack")
            );
    public static final FuryChopAttack<MadeInHeavenEntity> FURY_CHOP = new FuryChopAttack<MadeInHeavenEntity>(24,
            15, 24, 0.85f,7f, 20, 1.6f, 0.25f, 0.2f)
            .withSound(JSoundRegistry.MIH_FURYCHOP)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Fury Chop"),
                    Component.literal("combo extender, on hit gives haste(8s) to user and mining fatigue(8s) to victim, on whiff the fatigue goes to user")
            );
    public static final SimpleAttack<MadeInHeavenEntity> DONUT = new SimpleAttack<MadeInHeavenEntity>(200,
            26, 32, 0.75f, 8.5f, 40, 2f, -0.2f, 0.2f)
            .withSound(JSoundRegistry.STAND_DESUMMON)
            .withImpactSound(JSoundRegistry.IMPACT_7)
            .withHyperArmor()
            .withBlockStun(4)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(
                    Component.literal("Roundabout Donut"),
                    Component.literal("feigns stand desummon, uninterruptible combo starter")
            );
    public static final TimeAccelerationMove TIME_ACCELERATION = new TimeAccelerationMove(1400, 20,
            40, 1f, Either.right(JServerConfig.MIH_TIME_ACCELERATION_DURATION))
            .withSound(JSoundRegistry.MIH_TACCEL)
            .withInfo(
                    Component.literal("Time Acceleration"),
                    Component.literal("""
                            allows charging the speedometer for 30s
                            it is charged by landing hits
                            the speedometer impacts the level of speed and haste granted by Time Acceleration
                            if the speedometer is full and the charging period finishes, enemies become standless for 15s"""));
    public static final CircleAttack CIRCLE = new CircleAttack(300, 13, 14, 1.25f)
            .withSound(JSoundRegistry.MIH_CIRCLE)
            .withInfo(
                    Component.literal("Heaven's Judgement"),
                    Component.literal("rapidly circles a looked-at target within 4m at a radius of 7m")
            );

    public static final JudgementAttack JUDGEMENT = new JudgementAttack(300, 20, 60, 1.25f, 2)
            .withCrouchingVariant(CIRCLE)
            .withSound(JSoundRegistry.MIH_JUDGEMENT)
            .withInfo(
                    Component.literal("Divine Severance"),
                    Component.literal("Made in Heaven rapidly speed slices an area, then finishes with a large, launching slice")
            );
    // TODO add move info x2
    // TODO balance x2
    public static final TossMove<MadeInHeavenEntity> TOSS = new TossMove<MadeInHeavenEntity>(0, 1, 1, 0.75f,0.5f,2f)
            .withAnim(MadeInHeavenEntity.State.ITEM_TOSS);
    public static final TossChargeMove<MadeInHeavenEntity> TOSS_CHARGE = new TossChargeMove<MadeInHeavenEntity>(30, 1 * 20 + 1, 1 * 20, 1.0f, 0)
            .withFollowup(TOSS);

    private static final EntityDataAccessor<Integer> ACCEL_TIME = SynchedEntityData.defineId(MadeInHeavenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SPEEDOMETER = SynchedEntityData.defineId(MadeInHeavenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> AFTER_IMAGE = SynchedEntityData.defineId(MadeInHeavenEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> CIRCLING_TARGET = SynchedEntityData.defineId(MadeInHeavenEntity.class, EntityDataSerializers.INT);
    // Speed-ramp progress synced to all clients so the afterimage renders the same for every observer.
    private static final EntityDataAccessor<Integer> SPEED_RAMP = SynchedEntityData.defineId(MadeInHeavenEntity.class, EntityDataSerializers.INT);

    public static final int MAXIMUM_SPEEDOMETER = 30;
    public static final int EXHAUSTION_DURATION = 20 * 5; // 5 seconds

    // Acceleration passive: ramps movement speed up to Speed 70 over RAMP_TICKS while moving forward.
    private static final int RAMP_TICKS = 120; // 6 second to reach max speed
    private static final double MAX_SPEED_BONUS = 0.2 * 70; // MULTIPLY_TOTAL bonus equal to Speed 70
    private static final UUID RAMP_SPEED_UUID = UUID.fromString("7a3b2c1d-0e9f-4a8b-9c7d-1e2f3a4b5c6d");
    private static final float AUTOSTEP_HEIGHT = 1.0f; // walk straight up full blocks
    private static final float DEFAULT_STEP_HEIGHT = 0.6f; // vanilla player/mob step height
    private static final float SPRINT_EXHAUSTION = 0.1f; // vanilla sprint exhaustion per meter
    private static final double MOVE_EPSILON = 0.003; // minimum horizontal movement to count as moving
    private static final double DIRECTION_THRESHOLD = 0.3; // dot threshold for forward/backstep classification
    private static final int RAMP_GRACE_TICKS = 3; // idle/strafe ticks tolerated before the ramp resets (absorbs glitches)

    private int speedometer = 0;
    private int speedRamp = 0;
    private int rampIdleTicks = 0; // consecutive non-forward ticks, for the reset grace window
    private int lastAppliedRamp = 0; // ramp value last written to the speed attribute, to skip redundant resyncs
    // Self-tracked previous user position for reliable per-tick movement (server xo/yo/zo are not dependable for players).
    private double lastUserX = Double.NaN;
    private double lastUserZ = Double.NaN;

    public MadeInHeavenEntity(Level worldIn) {
        super(JStandTypeRegistry.MADE_IN_HEAVEN.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(0.9f, 0.8f, 0.8f),
                new Vector3f(1.0f, 0.0f, 0.0f),
                new Vector3f(0.0f, 0.0f, 0.0f),
                new Vector3f(0.5f, 0.0f, 1.0f)
        };
    }

    private static void registerMoves(MoveMap<MadeInHeavenEntity, State> moves) {
        moves.registerImmediate(MoveClass.LIGHT, SLICE, State.SLICE);

        moves.register(MoveClass.HEAVY, DONUT, State.DONUT);
        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveClass.SPECIAL1, LOW_KICK, State.LOW_KICK).withCrouchingVariant(State.LEG_CRUSHER);
        moves.register(MoveClass.SPECIAL2, FURY_CHOP, State.FURY_CHOP);
        moves.register(MoveClass.SPECIAL3, JUDGEMENT, State.JUDGEMENT).withCrouchingVariant(State.CIRCLE_STARTUP);
        moves.register(MoveClass.ULTIMATE, TIME_ACCELERATION, State.TIME_ACCELERATION);

        moves.register(MoveClass.UTILITY, SPEED_SLICE, State.SPEED_SLICE);

        moves.register(MoveClass.TOSS, TOSS_CHARGE, State.ITEM_TOSS_CHARGE).withFollowup(State.ITEM_TOSS);
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        if (!tryFollowUp(moveClass, MoveClass.LIGHT)) {
            return super.initMove(moveClass);
        }

        return true;
    }

    @Override
    public void onPerform(AbstractMove<?, ? super MadeInHeavenEntity> move, Set<LivingEntity> targets) {
        tryIncrementSpeedometer(targets);
    }

    public int getAccelTime() {
        return entityData.get(ACCEL_TIME);
    }

    public void setAccelTime(int aTime) {
        entityData.set(ACCEL_TIME, aTime);
    }

    public int getSpeedometer() {
        return entityData.get(SPEEDOMETER);
    }

    public void incrementSpeedometer() {
        if (speedometer >= MAXIMUM_SPEEDOMETER) {
            return;
        }

        speedometer++;
        //JCraft.LOGGER.info("Speedometer increased to: " + speedometer);
    }

    /**
     * Tracks the speedometer value every tick, for actual addition see incrementSpeedometer()
     */
    public void setSpeedometer(int speedometer) {
        entityData.set(SPEEDOMETER, this.speedometer = speedometer);
    }

    /**
     * @return how far the user is into the Acceleration speed ramp, 0..1. Synced to all clients, so the afterimage
     * renders identically for every observer.
     */
    public float getRampIntensity() {
        return Mth.clamp(entityData.get(SPEED_RAMP) / (float) RAMP_TICKS, 0f, 1f);
    }

    public boolean getAfterimage() {
        return entityData.get(AFTER_IMAGE);
    }

    public void setAfterimage(boolean a) {
        entityData.set(AFTER_IMAGE, a);
    }

    public LivingEntity getCircleTarget() {
        return level().getEntity(entityData.get(CIRCLING_TARGET)) instanceof LivingEntity entity ? entity : null;
    }

    public void setCirclingTarget(LivingEntity target) {
        entityData.set(CIRCLING_TARGET, target == null ? -1 : target.getId());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        getEntityData().define(ACCEL_TIME, 0);
        getEntityData().define(SPEEDOMETER, 0);
        getEntityData().define(AFTER_IMAGE, false);
        getEntityData().define(CIRCLING_TARGET, -1);
        getEntityData().define(SPEED_RAMP, 0);
    }

    @Override
    public boolean handleMove(AbstractMove<?, ? super MadeInHeavenEntity> move, CooldownType cooldownType, State animState) {
        if (!move.canBeInitiated(this)) {
            return false;
        }
        LivingEntity player = getUserOrThrow();

        CommonCooldownsComponent cooldowns = JComponentPlatformUtils.getCooldowns(player);
        int cooldown = cooldowns.getCooldown(cooldownType);

        if (cooldown > 0) {
            return false;
        }

        int cdDiv = getAccelTime() > 0 ? 2 : 1;
        cooldowns.setCooldown(cooldownType, move.getCooldown() / cdDiv);

        setMove(move, animState);
        return true;
    }

    private void tryIncrementSpeedometer(Set<LivingEntity> targets) {
        if (getAccelTime() > 0 && !targets.isEmpty()) {
            incrementSpeedometer();
        }
    }

    @Override
    public void desummon() {
        if (!level().isClientSide() && getAccelTime() > 0) {
            TimeAccelStatePacket.sendStop(this);
        }
        super.desummon();
    }

    @Override
    public void remove(RemovalReason reason) {
        onStandRemoved(); // server-side removal (desummon, switching stands, death, ...)
        super.remove(reason);
    }

    @Override
    public void onClientRemoval() {
        onStandRemoved(); // client-side removal goes through here instead of remove()
        super.onClientRemoval();
    }

    /**
     * Restores the user's autostep and clears the ramp speed. Called on both sides so the local player's client-side
     * step height (which governs actual stepping) is reverted, not just the server copy.
     */
    private void onStandRemoved() {
        if (hasUser()) {
            final LivingEntity user = getUserOrThrow();
            user.setMaxUpStep(DEFAULT_STEP_HEIGHT);
            clearRampSpeed(user);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!hasUser()) {
            return;
        }
        final LivingEntity user = getUserOrThrow();
        final int aTime = getAccelTime();

        if (user.isSprinting()) {
            user.setMaxUpStep(AUTOSTEP_HEIGHT);
            setAfterimage(true);
            user.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 2,10));
            user.addEffect(new MobEffectInstance(JStatusRegistry.WATER_WALKING.get(), 2));
        }
        else {
            user.setMaxUpStep(DEFAULT_STEP_HEIGHT);
            if (getState() != State.TIME_ACCELERATION) {
                setAfterimage(false);
            }
        }

        if (aTime > 0 && !user.hasEffect(JStatusRegistry.DAZED.get())) {
            int amplifier = speedometer / 3;
            user.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, amplifier, true, false));
            user.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20, amplifier, true, false));
        }

        if (level().isClientSide) {
            final Entity clientCircleTarget = getCircleTarget();
            if (clientCircleTarget != null) {
                lookAtWithoutReset(user, EntityAnchorArgument.Anchor.EYES, clientCircleTarget.getEyePosition());
            }

            // TODO move-related client ticking
            if (getAccelTime() > 1) { // Updating on the client, to make sure all is smooth
                CircleAttack.createSpeedParticles(this, this);

                final List<Entity> toCatch = level().getEntitiesOfClass(Entity.class, // Lower range by 32 to reduce lag
                        getBoundingBox().inflate(96), EntitySelector.NO_CREATIVE_OR_SPECTATOR);

                for (Entity entity : toCatch) {
                    if (entity instanceof LivingEntity) {
                        continue;
                    }
                    if (entity.position().distanceToSqr(new Vec3(entity.xo, entity.yo, entity.zo)) > 0) {
                        CircleAttack.createSpeedParticles(this, entity);
                    }
                    entity.tick();
                }
            }

            return;
        }

        // Acceleration passive
        tickSpeedRamp(user);

        // Tracking
        setSpeedometer(speedometer);
    }

    /**
     * Acceleration passive: ramps {@link MobEffects#MOVEMENT_SPEED} up to Speed 70 over {@link #RAMP_TICKS} while the
     * user moves forward, cancels it when backstepping, and drains hunger proportionally to the ramp.
     * <p>
     * Movement direction is inferred from the user's actual horizontal displacement this tick (reliable server-side)
     * relative to their look yaw, since the server cannot see which movement keys a player holds in this version.
     */
    private void tickSpeedRamp(LivingEntity user) {
        // Sample the user's per-tick displacement from our own previous sample, so it stays reliable for server players.
        final double curX = user.getX();
        final double curZ = user.getZ();
        final boolean firstSample = Double.isNaN(lastUserX);
        final double dx = firstSample ? 0.0 : curX - lastUserX;
        final double dz = firstSample ? 0.0 : curZ - lastUserZ;
        lastUserX = curX;
        lastUserZ = curZ;

        if (user.hasEffect(JStatusRegistry.DAZED.get()) || isEatingFood(user)) {
            speedRamp = 0; // being dazed or eating cancels the buildup
        } else {
            final double dist = Math.sqrt(dx * dx + dz * dz);

            boolean forward = false;
            boolean backstep = false;
            if (dist >= MOVE_EPSILON) {
                final float yaw = user.getYRot() * Mth.DEG_TO_RAD;
                final double dot = (dx * -Mth.sin(yaw) + dz * Mth.cos(yaw)) / dist;
                if (dot > DIRECTION_THRESHOLD && user.isSprinting()) {
                    forward = true; // only sprinting builds the ramp; walking does not
                } else if (dot < -DIRECTION_THRESHOLD) {
                    backstep = true;
                }
            }

            if (forward) {
                speedRamp = Math.min(RAMP_TICKS, speedRamp + 1);
                rampIdleTicks = 0;
            } else if (backstep) {
                speedRamp = 0; // backstepping cancels buildup immediately
                rampIdleTicks = 0;
            } else {
                // Idle or strafing. Tolerate a few ticks first: a single server tick can read no movement (packet
                // timing) or briefly misjudge direction mid-turn, and a hard reset there wipes the whole ramp.
                if (++rampIdleTicks >= RAMP_GRACE_TICKS) {
                    speedRamp = 0;
                }
            }

            // Hunger drain (players only; mobs do not use FoodData). Scaled from vanilla sprint exhaustion (0.1/meter).
            if (user instanceof Player player) {
                if (backstep) {
                    player.causeFoodExhaustion((float) dist * SPRINT_EXHAUSTION * 2f);
                } else if (forward && speedRamp > 0) {
                    final float frac = speedRamp / (float) RAMP_TICKS;
                    final float multiplier = JServerConfig.MIH_SPRINT_HUNGER_MULTIPLIER.getValue();
                    player.causeFoodExhaustion((float) dist * SPRINT_EXHAUSTION * multiplier * frac);
                    for (int amplifier = ExhaustionEffect.MAX_LEVEL - 1; amplifier >= 0; amplifier--) {
                        if (speedRamp >= 20 * amplifier) {
                            player.addEffect(new MobEffectInstance(JStatusRegistry.EXHAUSTION.get(), EXHAUSTION_DURATION, amplifier));
                            break;
                        }
                    }
                }
            }
        }

        applyRampSpeed(user);
        entityData.set(SPEED_RAMP, speedRamp); // sync to clients for afterimage rendering
    }

    /**
     * Drives the {@link Attributes#MOVEMENT_SPEED} attribute directly from {@link #speedRamp} via a transient modifier.
     * Unlike a potion effect this gives a stable speed while ramping and an instant drop to zero, and it leaves any
     * other speed sources (potions, Time Acceleration) untouched.
     */
    private void applyRampSpeed(LivingEntity user) {
        if (speedRamp == lastAppliedRamp) {
            return; // unchanged since last tick; avoid needless attribute resyncs
        }
        lastAppliedRamp = speedRamp;

        final AttributeInstance speed = user.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        speed.removeModifier(RAMP_SPEED_UUID);
        if (speedRamp > 0) {
            final double bonus = MAX_SPEED_BONUS * (speedRamp / (double) RAMP_TICKS);
            speed.addTransientModifier(new AttributeModifier(RAMP_SPEED_UUID, "MiH acceleration", bonus,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    private static boolean isEatingFood(LivingEntity user) {
        return user.isUsingItem() && user.getUseItem().getUseAnimation() == UseAnim.EAT;
    }

    private void clearRampSpeed(LivingEntity user) {
        final AttributeInstance speed = user.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(RAMP_SPEED_UUID);
        }
        speedRamp = 0;
        lastAppliedRamp = 0;
    }

    // Copied from Entity#lookAt(EntityAnchor, Vec3d), but doesn't set prevYaw, prevPitch and prevHeadYaw to get rid of jitter.
    private static void lookAtWithoutReset(LivingEntity entity, EntityAnchorArgument.Anchor anchorPoint, Vec3 target) {
        entity.yRotO = entity.getYRot();
        entity.yBodyRotO = entity.getVisualRotationYInDegrees();
        entity.yHeadRotO = entity.getYHeadRot();
        entity.xRotO = entity.getXRot();

        Vec3 vec3d = anchorPoint.apply(entity);
        double d = target.x - vec3d.x;
        double e = target.y - vec3d.y;
        double f = target.z - vec3d.z;
        double g = Math.sqrt(d * d + f * f);
        entity.setXRot(Mth.wrapDegrees((float) (-(Mth.atan2(e, g) * 57.2957763671875))));
        entity.setYRot(Mth.wrapDegrees((float) (Mth.atan2(f, d) * 57.2957763671875) - 90.0f));
        entity.setYHeadRot(entity.getYRot());
    }

    @Override
    @NonNull
    public MadeInHeavenEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<MadeInHeavenEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.mih.idle", AzPlayBehaviors.LOOP)),
        SLICE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mih.slice", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.mih.block", AzPlayBehaviors.LOOP)),
        DONUT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mih.donut", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mih.barrage", AzPlayBehaviors.LOOP)),
        SPEED_SLICE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mih.speedslice", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        JUDGEMENT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mih.judgement", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LEG_CRUSHER(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mih.legcrusher", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        FURY_CHOP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mih.furychop", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        TIME_ACCELERATION(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mih.taccel", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        CIRCLE_STARTUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mih.circlestartup", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        SPEED_CHOP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mih.speedchop", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mih.light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LOW_KICK(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mih.lowkick", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ITEM_TOSS_CHARGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "itemthrow_charge", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ITEM_TOSS(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "itemthrow", AzPlayBehaviors.PLAY_ONCE));

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(MadeInHeavenEntity attacker) {
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
