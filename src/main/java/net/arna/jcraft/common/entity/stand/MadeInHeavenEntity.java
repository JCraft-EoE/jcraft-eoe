package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.madeinheaven.*;
import net.arna.jcraft.common.attack.moves.shared.EffectInflictingAttack;
import net.arna.jcraft.common.attack.moves.shared.KnockdownAttack;
import net.arna.jcraft.common.attack.moves.shared.MainBarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.component.living.CooldownsComponent;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.network.s2c.TimeAccelStatePacket;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class MadeInHeavenEntity extends StandEntity<MadeInHeavenEntity, MadeInHeavenEntity.State> {
    public static final EffectInflictingAttack<MadeInHeavenEntity> SPEED_CHOP = new EffectInflictingAttack<MadeInHeavenEntity>(
            JCraft.LIGHT_COOLDOWN, 6, 11, 0.75f, 3f, 8, 1.5f, 0.5f, -0.1f,
            List.of(new StatusEffectInstance(JStatusRegistry.BLEEDING, 80, 1, true, false, true)))
            .withAnim(State.SPEED_CHOP)
            .withImpactSound(SoundEvents.ITEM_TRIDENT_HIT)
            .withAction(MadeInHeavenEntity::tryIncrementSpeedometer)
            .withHitAnimation(HitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Text.literal("Speed Chop"),
                    Text.literal("tiny stun, procs bleed")
            );
    public static final SimpleAttack<MadeInHeavenEntity> LIGHT_FOLLOWUP = new SimpleAttack<MadeInHeavenEntity>(
            0, 6, 12, 0.75f, 5, 8, 1.5f, 1f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0.25, 1)
            .withAction(MadeInHeavenEntity::tryIncrementSpeedometer)
            .withInfo(
                    Text.literal("Kick"),
                    Text.literal("quick combo finisher")
            );
    public static final SimpleAttack<MadeInHeavenEntity> SLICE = new SimpleAttack<MadeInHeavenEntity>(JCraft.LIGHT_COOLDOWN,
            5, 8, 0.75f, 4f, 10, 1.5f, 0.15f, -0.1f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(SPEED_CHOP)
            .withImpactSound(SoundEvents.ITEM_TRIDENT_HIT)
            .withAction(MadeInHeavenEntity::tryIncrementSpeedometer)
            .withInfo(
                    Text.literal("Slice"),
                    Text.literal("quick combo starter")
            );
    public static final SimpleAttack<MadeInHeavenEntity> BARRAGE_FINISHER = new SimpleAttack<MadeInHeavenEntity>(0,
            6, 9, 0.85f, 1f, 10, 1.5f, 1.1f, 0f)
            .withAction(MadeInHeavenEntity::tryIncrementSpeedometer)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(
                    Text.literal("Barrage (Final Hit)"),
                    Text.empty()
            );
    public static final MainBarrageAttack<MadeInHeavenEntity> BARRAGE = new MainBarrageAttack<MadeInHeavenEntity>(200,
            0, 32, 0.85f, 1.5f, 10, 2f, 0.1f, 0f, 3, Blocks.OAK_PLANKS.getHardness())
            .withFinisher(23, BARRAGE_FINISHER)
            .withAction(MadeInHeavenEntity::tryIncrementSpeedometer)
            .withSound(JSoundRegistry.MIH_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Text.literal("Barrage"),
                    Text.literal("short, knocks back")
            );
    public static final SpeedSliceAttack SPEED_SLICE = new SpeedSliceAttack(300, 10, 11,
            1.25f, 6f, 1.5f, 1f)
            .withAction(MadeInHeavenEntity::tryIncrementSpeedometer)
            .withSound(JSoundRegistry.MIH_SPEEDSLICE)
            .withInfo(
                    Text.literal("Speed Slice"),
                    Text.literal("short windup, harming teleport with hitstun and light knockback")
            );
    public static final KnockdownAttack<MadeInHeavenEntity> LEG_CRUSHER = new KnockdownAttack<MadeInHeavenEntity>(
            80, 9, 19, 0.85f, 7f, 22, 1.5f, 0.35f, 0.2f, 45)
            .withAction(MadeInHeavenEntity::tryIncrementSpeedometer)
            .withSound(JSoundRegistry.MIH_LEGCRUSHER)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withExtraHitBox(0, -0.5, 1)
            .withHitAnimation(HitPropertyComponent.HitAnimation.LOW)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Leg Crusher"),
                    Text.literal("knocks down (2s)")
            );
    public static final SimpleAttack<MadeInHeavenEntity> LOW_KICK = new SimpleAttack<MadeInHeavenEntity>(80,
            8, 17, 0.85f, 6f, 26, 1.5f, 0.25f, 0.2f)
            .withAction(MadeInHeavenEntity::tryIncrementSpeedometer)
            .withCrouchingVariant(LEG_CRUSHER)
            .withSound(JSoundRegistry.MIH_LEGCRUSHER)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withExtraHitBox(0, -0.5, 1)
            .withHitAnimation(HitPropertyComponent.HitAnimation.LOW)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Low Kick"),
                    Text.literal("combo starter/extender, mih hoofs the enemies legs in a quick, stunning attack")
            );
    public static final FuryChopAttack FURY_CHOP = new FuryChopAttack(200, 15, 24, 0.85f,
            7f, 20, 1.6f, 0.25f, 0.2f)
            .withSound(JSoundRegistry.MIH_FURYCHOP)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withAction(MadeInHeavenEntity::tryIncrementSpeedometer)
            .withHitAnimation(HitPropertyComponent.HitAnimation.HIGH)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Fury Chop"),
                    Text.literal("combo extender, on hit gives haste(8s) to user and mining fatigue(8s) to victim, on whiff the fatigue goes to user")
            );
    public static final SimpleAttack<MadeInHeavenEntity> DONUT = new SimpleAttack<MadeInHeavenEntity>(200,
            26, 32, 0.75f, 8.5f, 40, 2f, -0.2f, 0.2f)
            .withSound(JSoundRegistry.STAND_DESUMMON)
            .withImpactSound(JSoundRegistry.IMPACT_7)
            .withAction(MadeInHeavenEntity::tryIncrementSpeedometer)
            .withHyperArmor()
            .withBlockStun(4)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(
                    Text.literal("Roundabout Donut"),
                    Text.literal("feigns stand desummon, uninterruptible combo starter")
            );
    public static final TimeAccelerationMove TIME_ACCELERATION = new TimeAccelerationMove(1400, 20,
            40, 1f, JServerConfig.MIH_TIME_ACCELERATION_DURATION::getValue)
            .withSound(JSoundRegistry.MIH_TACCEL)
            .withAction((attacker, user, ctx, targets) -> attacker.speedometer = 0) // Clear speedometer
            .withInfo(
                    Text.literal("Time Acceleration"),
                    Text.literal("""
                            allows charging the speedometer for 30s
                            it is charged by landing hits
                            the speedometer impacts the level of speed and haste granted by Time Acceleration
                            if the speedometer is full and the charging period finishes, enemies become standless for 15s"""));
    public static final CircleAttack CIRCLE = new CircleAttack(400, 13, 14, 1.25f)
            .withSound(JSoundRegistry.MIH_CIRCLE)
            .withAction(MadeInHeavenEntity::tryIncrementSpeedometer)
            .withInfo(
                    Text.literal("Heaven's Judgement"),
                    Text.literal("rapidly circles a looked-at target within 4m at a radius of 7m")
            );

    public static final JudgementAttack JUDGEMENT = new JudgementAttack(300, 20, 60, 1.25f, 2)
            .withCrouchingVariant(CIRCLE)
            .withAction(MadeInHeavenEntity::tryIncrementSpeedometer)
            .withSound(JSoundRegistry.MIH_JUDGEMENT)
            .withInfo(
                    Text.literal("Divine Severance"),
                    Text.literal("Made in Heaven rapidly speed slices an area, then finishes with a large, launching slice")
            );
    private static final TrackedData<Integer> ACCEL_TIME;
    private static final TrackedData<Integer> SPEEDOMETER;
    private static final TrackedData<Boolean> AFTER_IMAGE;
    private static final TrackedData<Integer> CIRCLING_TARGET;

    public static final int MAXIMUM_SPEEDOMETER = 30;

    private int speedometer = 0;

    static {
        ACCEL_TIME = DataTracker.registerData(MadeInHeavenEntity.class, TrackedDataHandlerRegistry.INTEGER);
        SPEEDOMETER = DataTracker.registerData(MadeInHeavenEntity.class, TrackedDataHandlerRegistry.INTEGER);
        AFTER_IMAGE = DataTracker.registerData(MadeInHeavenEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        CIRCLING_TARGET = DataTracker.registerData(MadeInHeavenEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    public MadeInHeavenEntity(World worldIn) {
        super(StandType.MADE_IN_HEAVEN, worldIn, JSoundRegistry.MIH_SUMMON);
        idleRotation = -45f;

        description = "Lightspeed RUSHDOWN";

        pros = List.of(
                "best mobility",
                "great mixups",
                "good pressure",
                "low cooldowns"
        );

        cons = List.of(
                "bad defensive options",
                "relies on good spacing"
        );

        freespace =
                """
                PASSIVE: Speed I
                
                BNBs:
                    -the flashbang
                    (Donut>M1>)Speed Slice>Low Kick>Fury Chop>M1>Barrage>dash>M1~M1
                    
                    -""";

        auraColors = new Vector3f[]{
                new Vector3f(0.9f, 0.8f, 0.8f),
                new Vector3f(1.0f, 0.0f, 0.0f),
                new Vector3f(0.0f, 0.0f, 0.0f),
                new Vector3f(0.5f, 0.0f, 1.0f)
        };
    }

    @Override
    protected void registerMoves(MoveMap<MadeInHeavenEntity, State> moves) {
        moves.registerImmediate(MoveType.LIGHT, SLICE, State.SLICE);

        moves.register(MoveType.HEAVY, DONUT, State.DONUT);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, LOW_KICK, State.LOW_KICK).withCrouchingVariant(State.LEG_CRUSHER);
        moves.register(MoveType.SPECIAL2, FURY_CHOP, State.FURY_CHOP);
        moves.register(MoveType.SPECIAL3, JUDGEMENT, State.JUDGEMENT).withCrouchingVariant(State.CIRCLE_STARTUP);
        moves.register(MoveType.ULTIMATE, TIME_ACCELERATION, State.TIME_ACCELERATION);

        moves.register(MoveType.UTILITY, SPEED_SLICE, State.SPEED_SLICE);
    }

    @Override
    public boolean initMove(MoveType type) {
        if (type == MoveType.LIGHT && curMove != null && curMove.getMoveType() == MoveType.LIGHT && getMoveStun() < curMove.getWindupPoint()) {
            AbstractMove<?, ? super MadeInHeavenEntity> followup = curMove.getFollowup();
            if (followup != null) setMove(followup, (State) followup.getAnimation());
        } else return super.initMove(type);

        return true;
    }

    public int getAccelTime() {
        return dataTracker.get(ACCEL_TIME);
    }

    public void setAccelTime(int aTime) {
        dataTracker.set(ACCEL_TIME, aTime);
    }

    public int getSpeedometer() {
        return dataTracker.get(SPEEDOMETER);
    }

    public void incrementSpeedometer() {
        if (speedometer >= MAXIMUM_SPEEDOMETER) return;

        speedometer++;
        //JCraft.LOGGER.info("Speedometer increased to: " + speedometer);
    }

    /**
     * Tracks the speedometer value every tick, for actual addition see incrementSpeedometer()
     */
    public void setSpeedometer(int speedometer) {
        dataTracker.set(SPEEDOMETER, speedometer);
    }

    public boolean getAfterimage() {
        return dataTracker.get(AFTER_IMAGE);
    }

    public void setAfterimage(boolean a) {
        dataTracker.set(AFTER_IMAGE, a);
    }

    public LivingEntity getCircleTarget() {
        return getWorld().getEntityById(dataTracker.get(CIRCLING_TARGET)) instanceof LivingEntity entity ? entity : null;
    }

    public void setCirclingTarget(LivingEntity target) {
        dataTracker.set(CIRCLING_TARGET, target == null ? -1 : target.getId());
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        getDataTracker().startTracking(ACCEL_TIME, 0);
        getDataTracker().startTracking(SPEEDOMETER, 0);
        getDataTracker().startTracking(AFTER_IMAGE, false);
        getDataTracker().startTracking(CIRCLING_TARGET, -1);
    }

    @Override
    public boolean handleMove(AbstractMove<?, ? super MadeInHeavenEntity> move, CooldownType cooldownType, State animState) {
        if (!move.canBeInitiated(this)) return false;
        LivingEntity player = getUserOrThrow();

        CooldownsComponent cooldowns = JComponents.getCooldowns(player);
        int cooldown = cooldowns.getCooldown(cooldownType);

        if (cooldown > 0) return false;

        int cdDiv = getAccelTime() > 0 ? 2 : 1;
        cooldowns.setCooldown(cooldownType, move.getCooldown() / cdDiv);

        setMove(move, animState);
        return true;
    }

    private static void tryIncrementSpeedometer(MadeInHeavenEntity attacker, LivingEntity user, MoveContext ctx, Set<LivingEntity> targets) {
        if (attacker.getAccelTime() > 0 && !targets.isEmpty()) attacker.incrementSpeedometer();
    }

    @Override
    public void desummon() {
        if (!getWorld().isClient() && getAccelTime() > 0)
            TimeAccelStatePacket.sendStop(getServer().getPlayerManager(), this);
        super.desummon();
    }

    @Override
    public void tick() {
        super.tick();

        if (!hasUser()) return;
        LivingEntity user = getUserOrThrow();
        int aTime = getAccelTime();

        // Circling
        CIRCLE.tickCircle(this);

        // Time Accel handling
        TIME_ACCELERATION.tickTimeAcceleration(this);

        if (!user.hasStatusEffect(JStatusRegistry.DAZED)) {
            if (aTime > 0) {
                int amplifier = speedometer / 3;
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 20, amplifier, true, false));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 20, amplifier, true, false));
            } else
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 0, true, false));
        }

        if (getWorld().isClient) {
            Entity clientCircleTarget = getCircleTarget();
            if (clientCircleTarget != null)
                lookAtWithoutReset(user, EntityAnchorArgumentType.EntityAnchor.EYES, clientCircleTarget.getEyePos());

            if (getAccelTime() > 1) { // Updating on the client, to make sure all is smooth
                CircleAttack.createSpeedParticles(this, this);

                List<Entity> toCatch = getWorld().getEntitiesByClass(Entity.class, // Lower range by 32 to reduce lag
                        getBoundingBox().expand(96), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                for (Entity entity : toCatch) {
                    if (entity instanceof LivingEntity) continue;
                    if (entity.getPos().squaredDistanceTo(new Vec3d(entity.prevX, entity.prevY, entity.prevZ)) > 0)
                        CircleAttack.createSpeedParticles(this, entity);
                    entity.tick();
                }
            }

            return;
        }

        // Tracking
        setSpeedometer(speedometer);
    }

    // Copied from Entity#lookAt(EntityAnchor, Vec3d), but doesn't set prevYaw, prevPitch and prevHeadYaw to get rid of jitter.
    private static void lookAtWithoutReset(LivingEntity entity, EntityAnchorArgumentType.EntityAnchor anchorPoint, Vec3d target) {
        entity.prevYaw = entity.getYaw();
        entity.prevBodyYaw = entity.getBodyYaw();
        entity.prevHeadYaw = entity.getHeadYaw();
        entity.prevPitch = entity.getPitch();

        Vec3d vec3d = anchorPoint.positionAt(entity);
        double d = target.x - vec3d.x;
        double e = target.y - vec3d.y;
        double f = target.z - vec3d.z;
        double g = Math.sqrt(d * d + f * f);
        entity.setPitch(MathHelper.wrapDegrees((float)(-(MathHelper.atan2(e, g) * 57.2957763671875))));
        entity.setYaw(MathHelper.wrapDegrees((float)(MathHelper.atan2(f, d) * 57.2957763671875) - 90.0f));
        entity.setHeadYaw(entity.getYaw());
    }

    @Override
    @NonNull
    public MadeInHeavenEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<MadeInHeavenEntity> {
        IDLE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.mih.idle"))),
        SLICE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mih.slice"))),
        BLOCK(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.mih.block"))),
        DONUT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mih.donut"))),
        BARRAGE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.mih.barrage"))),
        SPEED_SLICE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mih.speedslice"))),
        JUDGEMENT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mih.judgement"))),
        LEG_CRUSHER(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mih.legcrusher"))),
        FURY_CHOP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mih.furychop"))),
        TIME_ACCELERATION(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mih.taccel"))),
        CIRCLE_STARTUP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mih.circlestartup"))),
        SPEED_CHOP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mih.speedchop"))),
        LIGHT_FOLLOWUP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mih.light_followup"))),
        LOW_KICK(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mih.lowkick")));

        private final Consumer<AnimationState> animator;

        State(Consumer<AnimationState> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(MadeInHeavenEntity attacker, AnimationState builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @Nullable String getSummonAnimation() {
        return "animation.mih.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
