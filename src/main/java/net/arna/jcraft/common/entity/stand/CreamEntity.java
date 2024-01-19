package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.BlockableType;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.cream.*;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CreamEntity extends StandEntity<CreamEntity, CreamEntity.State> {
    public static final EffectInflictingAttack<CreamEntity> BITE = new EffectInflictingAttack<CreamEntity>(30,
            9, 15, 0.75f, 5f, 20, 1.75f, 0.75f, 0.3f,
            List.of(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1)))
            .withInfo(Text.literal("Bite"), Text.literal("applies Slowness II (2s) on hit"));
    public static final SimpleAttack<CreamEntity> LIGHT_FOLLOWUP = new SimpleAttack<CreamEntity>(
            0, 7, 14, 0.75f, 6, 8, 1.75f, 1.1f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0.25, 1)
            .withInfo(Text.literal("Chop"), Text.literal("quick combo finisher"));
    public static final SimpleAttack<CreamEntity> PUNCH = SimpleAttack.<CreamEntity>lightAttack(6, 14,
            5f, 20, 0.75f, 0.75f, 0.1f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(BITE)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(Text.literal("Punch"), Text.literal("quick combo starter"));
    public static final SimpleAttack<CreamEntity> VERTICAL_CHOP = new SimpleAttack<CreamEntity>(200, 20,
            30, 1f, 8f, 40, 1.5f, 0.1f, 0f)
            .withSound(JSoundRegistry.CREAM_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHyperArmor()
            .withInfo(Text.literal("Vertical Chop"), Text.literal("slow, uninterruptible combo starter"));
    public static final CreamComboAttack COMBO = new CreamComboAttack(280, 36, 0.75f,
            5f, 20, 2f, 0.1f, 0f, IntSet.of(10, 17, 25))
            .withSound(JSoundRegistry.CREAM_COMBO)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(Text.literal("Assault"), Text.literal("medium windup, good stun"));
    public static final SimpleAttack<CreamEntity> GRAB_HIT = new SimpleAttack<CreamEntity>(0, 13, 20,
            1f, 6f, 5, 2f, 1.5f, 0f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(Text.literal("Grab (Hit)"), Text.empty());
    public static final GrabAttack<CreamEntity, State> GRAB = new GrabAttack<>(320, 8, 20,
            1f, 3f, 30, 1.5f, 0f, 0f, GRAB_HIT, State.GRAB_HIT)
            .withSound(JSoundRegistry.CREAM_GRAB)
            .withInfo(Text.literal("Grab"), Text.literal("unblockable, knocks back"));
    public static final SurpriseMove SURPRISE = new SurpriseMove(300, 14, 24, 1f)
            .withSound(JSoundRegistry.CREAM_SUMMON)
            .withInfo(Text.literal("Surprise"), Text.literal("Cream disappears into the ground, then pops out in a nearby looked location"));
    public static final DestroyAttack DESTROY = new DestroyAttack(320, 21, 30, 1f,
            8f, 5, 2f, 1.25f, 0f)
            .withSound(JSoundRegistry.CREAM_OVERHEAD)
            .withImpactSound(JSoundRegistry.IMPACT_5)
            .withLaunch()
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE)
            .withInfo(Text.literal("Destroy"), Text.literal("slow, uninterruptible, unblockable knockdown"));
    public static final ConsumeAttack CONSUME = new ConsumeAttack(640, 35, 40, 1f,
            2f, 0, 2f, 0f, 0f)
            .withSound(JSoundRegistry.CREAM_CONSUME)
            .withInfo(Text.literal("Void"), Text.literal("high windup, 6 seconds"));
    public static final BallModeMove ENTER = new BallModeMove(40, 10, 15, 0f, true)
            .withSound(JSoundRegistry.CREAM_ENTER)
            .withInfo(Text.literal("Enter Cream"), Text.literal("cream consumes itself and the user halfway, increasing mobility and decreasing defense"));
    public static final BallModeMove EXIT = new BallModeMove(40, 5, 15, 0f, false)
            .withSound(JSoundRegistry.CREAM_EXIT)
            .withInfo(Text.literal("Exit Cream"), Text.literal("cream and its user return from the void"));
    public static final SimpleAttack<CreamEntity> SWIPE = new SimpleAttack<CreamEntity>(20, 7,
            14, 0.1f, 5f, 20, 2f, 0.75f, 0.2f)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(Text.literal("Swipe"), Text.literal("quick air-to-ground poke"));
    public static final KnockdownAttack<CreamEntity> OVERHEAD_SMASH = new KnockdownAttack<CreamEntity>(160,
            14, 20, 0.1f, 9f, 15, 2f, 1.25f, 0.3f, 35)
            .withSound(JSoundRegistry.CREAM_SMASH)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHyperArmor()
            .withLaunch()
            .withInfo(Text.literal("Overhead Smash"), Text.literal("slow, uninterruptible launcher"));
    public static final SimpleMultiHitAttack<CreamEntity> BALL_COMBO = new SimpleMultiHitAttack<CreamEntity>(200,
            36, 0.1f, 7f, 15, 2f, 0.1f, 0.3f, IntSet.of(10, 17, 25))
            .withSound(JSoundRegistry.CREAM_COMBO)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(Text.literal("Aerial Assault"), Text.literal("less stun than grounded version"));
    public static final BallChargeAttack BALL_CHARGE = new BallChargeAttack(300, 13, 28, 1f)
            .withSound(JSoundRegistry.CREAM_BALLDASH)
            .withInfo(Text.literal("Void Charge"), Text.literal("cream quickly transforms into a black hole and charges in the pointed direction"));

    private static final TrackedData<Integer> VOID_TIME;
    private static final TrackedData<Boolean> HALF_BALL;
    @Setter
    private Vec3d chargeDir;
    @Getter @Setter
    private boolean charging = false;

    static {
        VOID_TIME = DataTracker.registerData(CreamEntity.class, TrackedDataHandlerRegistry.INTEGER);
        HALF_BALL = DataTracker.registerData(CreamEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    public CreamEntity(World worldIn) {
        super(StandType.CREAM, worldIn, JSoundRegistry.CREAM_SUMMON);

        idleRotation = 220f;

        description = "Close Range SETUP";

        pros = List.of(
                "many block bypassing options",
                "powerful void state",
                "good poking",
                "good mobility"
        );

        cons = List.of(
                "very variable reward on hit",
                "blind and deaf in the void",
                "below average speed"
        );

        freespace = """
                BNBs (i. - in Cream):
                    M1>Assault>M1>Charge>Grab
                    i.M1>land+s.OFF>s.ON+Assault>M1>Charge>Grab
                    Chop>Destroy>Surprise
                    Chop>Void""";
    }

    public void beginHalfBall() {
        dataTracker.set(HALF_BALL, true);
        idleDistance = 0f;
        blockDistance = 0f;
        maxStandGauge = 45f;

        registerMoves();
    }

    public void endHalfBall() {
        dataTracker.set(HALF_BALL, false);
        idleDistance = 1.25f;
        blockDistance = 0.75f;
        maxStandGauge = 90f;

        registerMoves();
    }

    public boolean isHalfBall() {
        return dataTracker.get(HALF_BALL);
    }

    public int getVoidTime() {
        return dataTracker.get(VOID_TIME);
    }

    public void setVoidTime(int vTime) {
        dataTracker.set(VOID_TIME, vTime);
        if (vTime == 0) setReset(true);
    }

    @Override
    protected void registerMoves(MoveMap<CreamEntity, State> moves) {
        if (isHalfBall()) {
            moves.register(MoveType.LIGHT, SWIPE, State.BALL_LIGHT);
            moves.register(MoveType.HEAVY, OVERHEAD_SMASH, State.BALL_HEAVY);
            moves.register(MoveType.BARRAGE, BALL_COMBO, State.BALL_COMBO);

            moves.register(MoveType.SPECIAL1, BALL_CHARGE, State.BALL_CONSUME);

            moves.register(MoveType.UTILITY, EXIT, State.EXIT);
        } else {
            moves.register(MoveType.LIGHT, PUNCH, State.LIGHT).withCrouchingVariant(State.BITE);
            moves.register(MoveType.HEAVY, VERTICAL_CHOP, State.HEAVY);
            moves.register(MoveType.BARRAGE, COMBO, State.COMBO);

            moves.register(MoveType.SPECIAL1, GRAB, State.GRAB);

            moves.register(MoveType.UTILITY, ENTER, State.ENTER);
        }

        moves.register(MoveType.SPECIAL2, SURPRISE, State.SURPRISE);
        moves.register(MoveType.SPECIAL3, DESTROY, State.DESTROY);
        moves.register(MoveType.ULTIMATE, CONSUME, State.CONSUME);
    }

    @Override
    public void initMove(MoveType type) {
        if (type == MoveType.LIGHT && curMove != null && curMove.getMoveType() == MoveType.LIGHT && getMoveStun() < curMove.getWindupPoint()) {
            AbstractMove<?, ? super CreamEntity> followup = curMove.getFollowup();
            if (followup != null) {
                setMove(followup, (State) followup.getAnimation());
                return;
            }
        }

        super.initMove(type);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        getDataTracker().startTracking(VOID_TIME, 0);
        getDataTracker().startTracking(HALF_BALL, false);
    }

    @Override
    public boolean canAttack() {
        if (hasUser() && !(getUser() instanceof PlayerEntity) && getVoidTime() > 0)
            return false; // Prevents mobs from attacking while in void state and cancelling void early
        return super.canAttack();
    }

    @Override
    protected Box calculateBoundingBox() {
        double x = getX();
        double y = getY();
        double z = getZ();

        if (isHalfBall())
            return new Box(x - 0.6, y + 0.6, z - 0.6, x + 0.6, y + 2, z + 0.6);
        if (getState() == State.SURPRISE)
            return new Box(x - 0.6, y + 0, z - 0.6, x + 0.6, y + 0.3, z + 0.6);
        return super.calculateBoundingBox();
    }

    @Override
    public void desummon() {
        // Stop voiding if voiding
        if (this.getVoidTime() > 0) {
            this.setVoidTime(0);
            return;
        }

        // Real desummon if not voiding
        super.desummon();
    }

    @Override
    public boolean defaultToNear() {
        if (charging) return false;
        return super.defaultToNear();
    }

    @Override
    public void tick() {
        super.tick();
        boolean server = !world.isClient();

        if (!hasUser()) return;
        LivingEntity user = getUserOrThrow();
        boolean isPlayer = false;
        boolean notCorS = false;

        Vec3d pos = getEyePos();
        int voidTime = getVoidTime();
        boolean voiding = (voidTime > 0);

        // Players get creative flight, and mobs get gravity removed and y level equalization with target; see: handleAIVoid()
        if (user instanceof PlayerEntity playerEntity) {
            notCorS = (!playerEntity.isCreative() && !playerEntity.isSpectator());
            if (notCorS && !charging && !isFree())
                playerEntity.getAbilities().flying = voiding;
            isPlayer = true;
        }

        if (server) {
            if (!charging) {
                if (curMove != null) {
                    setVoidTime(0);
                    resetAlphaOverride();
                    voiding = false;
                }
                idleOverride = getVoidTime() > 0;
            }

            user.setInvulnerable(getVoidTime() > 0);
        }

        if (voiding) {
            if (server) {
                if (world.getGameRules().getBoolean(JCraft.STAND_GRIEFING)) {
                    // Unfun 3x4x3 void code
                    for (int x = -1; x < 2; x++) {
                        for (int y = -1; y < 3; y++) {
                            for (int z = -1; z < 2; z++) {
                                BlockPos curPos = this.getBlockPos().add(x, y, z);
                                if (world.getBlockState(curPos).getBlock().getBlastResistance() > 100.1f)
                                    continue;
                                world.setBlockState(curPos, Block.getStateFromRawId(0));
                            }
                        }
                    }
                }

                if (charging) {
                    if (isFree()) { // Surprise move
                        Vec3f newPos = getFreePos().copy();
                        newPos.add(getMoveContext().get(SurpriseMove.OUT_DIR));
                        setFreePos(newPos);
                    } else if (chargeDir != null) { // Void Charge move
                        user.setVelocity(chargeDir);
                        user.velocityModified = true;
                        if (user instanceof ServerPlayerEntity player)
                            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(user));
                    }
                } else { // Ultimate
                    setStateNoReset(State.IDLE);

                    if (!isPlayer)
                        handleAIVoid(user, voidTime);
                }

                List<LivingEntity> toDamage = world.getEntitiesByClass(LivingEntity.class,
                        new Box(pos.add(1.5, 1.5, 1.5), pos.subtract(1.5, 1.5, 1.5)), EntityPredicates.VALID_ENTITY);

                toDamage.remove(user);
                toDamage.remove(this);

                if (charging) {
                    for (LivingEntity ent : toDamage) {
                        if (getMoveStun() % 2 == 0) { // More consistent
                            stun(ent, 4, 0);
                            JUtils.cancelMoves(ent);
                        }

                        ent.damage(DamageSource.OUT_OF_WORLD, 5);
                    }
                } else {
                    for (LivingEntity ent : toDamage) {
                        if (age % 4 == 0) {
                            stun(ent, 2, 0);
                            JUtils.cancelMoves(ent);
                        }

                        ent.damage(DamageSource.OUT_OF_WORLD, 1.5f);
                    }

                    setAlphaOverride(0);
                }

                if (notCorS && !isFree())
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 25, 0, false, false));

                voidTime--;
                if (voidTime < 1)
                    resetAlphaOverride();
                setVoidTime(voidTime);
                setDistanceOffset(0);
            } else {
                for (int i = 0; i < 16; i++)
                    world.addParticle(ParticleTypes.MYCELIUM,
                            pos.x + (random.nextFloat() - 0.5f) * 2f,
                            pos.y + (random.nextFloat() - 0.5f) * 2f,
                            pos.z + (random.nextFloat() - 0.5f) * 2f,
                            0, 0, 0);
            }
        } else { // Not voiding
            if (isIdle() && charging) {
                charging = false;
                setFree(false);
            }

            if (!isHalfBall()) return;
            setAlphaOverride(0.1f);
            user.onLanding();
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 5, 9, true, false));

            // Player Half-Ball controls
            if (user instanceof ServerPlayerEntity serverPlayer) {
                if (lastRemoteInputTime - age > 4) updateRemoteInputs(0, 0, false);

                Vec3d finalSpeed = Vec3d.ZERO;
                if (!blocking && !user.hasStatusEffect(JStatusRegistry.DAZED)) {
                    Vec3d eP = user.getEyePos();
                    Vec3d groundPos = world.raycast(
                            new RaycastContext(eP, eP.add(0, -24, 0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user)
                    ).getPos();

                    double groundDist = groundPos.distanceTo(pos);
                    double stabilization = user.getVelocity().y;
                    if (stabilization < 0) stabilization *= -0.75;
                    else stabilization = 0;

                    if (getRemoteJumpInput()) {
                        if (groundDist < 5) {
                            user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 10, 2, true, false));
                            if (groundDist < 3)
                                finalSpeed = finalSpeed.add(0, 0.25 / groundDist + stabilization, 0);
                        }
                    }

                    Vec3d rotVec = user.getRotationVector();
                    finalSpeed = finalSpeed.add(rotVec.multiply(getRemoteForwardInput() / 30)); // Forward movement
                    finalSpeed = finalSpeed.add(rotVec.rotateY(1.5707963f).multiply(getRemoteSideInput() / 30)); // Side movement
                    user.addVelocity(finalSpeed.x, finalSpeed.y, finalSpeed.z);
                    serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(user));
                }
            } else resetAlphaOverride();
        }
    }

    private void handleAIVoid(LivingEntity user, int voidTime) {
        double y = user.getY();
        Vec3d vel = new Vec3d(user.getVelocity().x, 0.0, user.getVelocity().z);

        // Targeting priority
        LivingEntity targetEntity = user.getDamageTracker().getBiggestAttacker();
        if (targetEntity == null && user instanceof MobEntity mob)
            targetEntity = mob.getTarget();
        if (targetEntity == null)
            targetEntity = user.getAttacker();

        // If target wasn't found, thrash around
        Vec3d target = targetEntity != null ? targetEntity.getPos() : this.getPos().add(Math.sin(this.age * 0.2) * 2, Math.sin(this.age * 0.2) / 4, Math.cos(this.age * 0.2) * 2);

        double dY = MathHelper.clamp(target.getY() - y, -1, 1);
        y += dY;

        vel = vel.add(target.subtract(user.getPos().add(random.nextDouble() * 2, random.nextDouble() * 3, random.nextDouble() * 3)).normalize()).multiply(0.3);

        user.setVelocity(vel);
        user.setPos(user.getX(), y, user.getZ());

        if (voidTime < 10)
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 5, 1, true, false));
    }

    @Override
    protected @NonNull CreamEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<CreamEntity> {
        IDLE((cream, builder) -> builder.loop("animation.cream." + ( cream.getVoidTime() > 0 ? "void" : cream.isHalfBall() ? "ball" : "" ) + "idle")),
        LIGHT(builder -> builder.playAndHold("animation.cream.light")),
        LIGHT_FOLLOWUP(builder -> builder.playAndHold("animation.cream.light_followup")),
        BALL_LIGHT(builder -> builder.playAndHold("animation.cream.balllight")),
        BLOCK(builder -> builder.loop("animation.cream.block")),
        BALL_BLOCK(builder -> builder.loop("animation.cream.ballblock")),
        HEAVY(builder -> builder.playAndHold("animation.cream.heavy")),
        BALL_HEAVY(builder -> builder.playAndHold("animation.cream.ballheavy")),
        COMBO(builder -> builder.playAndHold("animation.cream.combo")),
        BALL_COMBO(builder -> builder.playAndHold("animation.cream.ballcombo")),
        CONSUME(builder -> builder.playAndHold("animation.cream.consume")),
        BALL_CONSUME(builder -> builder.playAndHold("animation.cream.ballconsume")),
        SURPRISE(builder -> builder.playAndHold("animation.cream.surprise")),
        CHARGE_HIT(builder -> builder.playAndHold("animation.cream.charge_hit")),
        GRAB(builder -> builder.playAndHold("animation.cream.grab")),
        GRAB_HIT(builder -> builder.playAndHold("animation.cream.grab_hit")),
        ENTER(builder -> builder.playAndHold("animation.cream.enter")),
        EXIT(builder -> builder.playAndHold("animation.cream.exit")),
        DESTROY(builder -> builder.playAndHold("animation.cream.destroy")),
        BITE(builder -> builder.playAndHold("animation.cream.bite"));

        private final BiConsumer<CreamEntity, AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this((creamEntity, builder) -> animator.accept(builder));
        }

        State(BiConsumer<CreamEntity, AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(CreamEntity attacker, AnimationBuilder builder) {
            animator.accept(attacker, builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected String getSummonAnimation() {
        return "animation.cream.summon";
    }

    @Override
    public State getIdleState() {
        return State.IDLE;
    }

    @Override
    public State getBlockState() {
        return isHalfBall() ? State.BALL_BLOCK : State.BLOCK;
    }
}
