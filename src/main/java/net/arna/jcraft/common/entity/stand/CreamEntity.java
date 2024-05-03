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
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
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
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.arna.jcraft.common.attack.moves.cream.SurpriseMove.OUT_DIR;
import static net.arna.jcraft.common.attack.moves.cream.SurpriseMove.OUT_POS;

public class CreamEntity extends StandEntity<CreamEntity, CreamEntity.State> {
    public static final EffectInflictingAttack<CreamEntity> BITE = new EffectInflictingAttack<CreamEntity>(20,
            7, 13, 0.75f, 6f, 20, 1.75f, 0.75f, 0.3f,
            List.of(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1)))
            .withAnim(State.BITE)
            .withHitAnimation(HitPropertyComponent.HitAnimation.LOW)
            .withSound(SoundEvents.ENTITY_EVOKER_FANGS_ATTACK)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Bite"),
                    Text.literal("applies Slowness II (2s) on hit"));
    public static final SimpleAttack<CreamEntity> LIGHT_FOLLOWUP = new SimpleAttack<CreamEntity>(
            0, 7, 14, 0.75f, 6f, 8, 1.75f, 1.1f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0.25, 1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Chop"),
                    Text.literal("quick combo finisher"));
    public static final SimpleAttack<CreamEntity> PUNCH = SimpleAttack.<CreamEntity>lightAttack(6, 14,
                    0.75f, 5f, 20, 0.3f, -0.1f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(BITE)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(
                    Text.literal("Backhand"),
                    Text.literal("quick combo starter"));
    public static final SimpleAttack<CreamEntity> VERTICAL_CHOP = new SimpleAttack<CreamEntity>(200, 20,
            30, 1f, 8f, 40, 1.5f, 0.8f, 0f)
            .withSound(JSoundRegistry.CREAM_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHyperArmor()
            .withHitAnimation(HitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Text.literal("Vertical Chop"),
                    Text.literal("slow, uninterruptible combo starter"));
    public static final CreamComboAttack COMBO = new CreamComboAttack(280, 36, 0.75f,
            5f, 20, 2f, 0.2f, 0f, IntSet.of(10, 17, 25))
            .withSound(JSoundRegistry.CREAM_COMBO)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Assault"),
                    Text.literal("medium windup, good stun"));
    public static final SimpleAttack<CreamEntity> GRAB_HIT = new SimpleAttack<CreamEntity>(0, 13, 20,
            1f, 6f, 5, 2f, 1.5f, 0f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Grab (Hit)"),
                    Text.empty());
    public static final GrabAttack<CreamEntity, State> GRAB = new GrabAttack<>(320, 8, 20,
            1f, 3f, 30, 1.5f, 0f, 0f, GRAB_HIT, State.GRAB_HIT)
            .withSound(JSoundRegistry.CREAM_GRAB)
            .withInfo(
                    Text.literal("Grab"),
                    Text.literal("unblockable, knocks back"));
    public static final SurpriseMove SURPRISE = new SurpriseMove(300, 14, 24, 1f)
            .withSound(JSoundRegistry.CREAM_SUMMON)
            .withInitAction((attacker, user, ctx) -> {
                Vec3d rotVec = user.getRotationVector();
                if (user.isSneaking()) {
                    attacker.getMoveContext().set(OUT_POS, user.getPos().add(rotVec).toVector3f());
                } else {
                    Vec3d eyePos = user.getEyePos();
                    HitResult hitResult = attacker.getWorld().raycast(new RaycastContext(eyePos, eyePos.add(rotVec.multiply(16)),
                            RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
                    attacker.getMoveContext().set(OUT_POS, hitResult.getPos().toVector3f());
                }
            })
            .withAction((attacker, user, ctx, targets) -> {
                var outDir = GravityChangerAPI.getGravityDirection(attacker).getUnitVector();
                outDir.mul(-1f);
                ctx.set(OUT_DIR, outDir);
            })
            .withInfo(
                    Text.literal("Surprise"),
                    Text.literal("""
                    Cream disappears into the ground, then pops out in a nearby looked location.
                    If used while crouching, Cream appears in front of the user.
                    """));
    public static final ChargeBarrageAttack<CreamEntity> CHARGE = new ChargeBarrageAttack<CreamEntity>(200, 15, 30,
            4f, 2f, 10, 1.5f, 0.5f, 0f, 3, false)
            .withAction(
                    ((attacker, user, ctx, targets) ->
                            targets.forEach(target -> target.addStatusEffect(
                                            new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 25, 0, true, false)
                                    )
                            )
                    )
            )
            .withLaunchNoShockwave()
            .withImpactSound(JSoundRegistry.IMPACT_5)
            .withBlockableType(BlockableType.NON_BLOCKABLE)
            .withInfo(
                    Text.literal("Charge"),
                    Text.literal("4 block range, unblockable knockdown"));
    public static final DestroyAttack DESTROY = new DestroyAttack(320, 21, 30, 1f,
            8f, 5, 2f, 1.25f, 0f)
            .withCrouchingVariant(CHARGE)
            .withSound(JSoundRegistry.CREAM_OVERHEAD)
            .withImpactSound(JSoundRegistry.IMPACT_5)
            .withLaunch()
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE)
            .withInfo(
                    Text.literal("Destroy"),
                    Text.literal("slow, uninterruptible, unblockable knockdown"));
    public static final ConsumeAttack CONSUME = new ConsumeAttack(640, 35, 40, 1f,
            2f, 0, 2f, 0f, 0f)
            .withSound(JSoundRegistry.CREAM_CONSUME)
            .withInfo(
                    Text.literal("Void"),
                    Text.literal("high windup, 6 seconds"));
    public static final BallModeMove ENTER = new BallModeMove(40, 10, 15, 0f, true)
            .withSound(JSoundRegistry.CREAM_ENTER)
            .withInfo(
                    Text.literal("Enter Cream"),
                    Text.literal("Cream consumes itself and the user halfway, increasing mobility and decreasing defense"));
    public static final BallModeMove EXIT = new BallModeMove(40, 5, 15, 0f, false)
            .withSound(JSoundRegistry.CREAM_EXIT)
            .withInfo(
                    Text.literal("Exit Cream"),
                    Text.literal("Cream and its user return from the void"));
    public static final SimpleAttack<CreamEntity> SWIPE = new SimpleAttack<CreamEntity>(20, 7,
            14, 0.5f, 5f, 20, 2f, 0.75f, 0.2f)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withHitAnimation(HitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Text.literal("Swipe"),
                    Text.literal("quick air-to-ground poke"));
    public static final KnockdownAttack<CreamEntity> OVERHEAD_SMASH = new KnockdownAttack<CreamEntity>(160,
            14, 20, 0.5f, 9f, 15, 2f, 1.25f, 0.3f, 35)
            .withSound(JSoundRegistry.CREAM_SMASH)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHyperArmor()
            .withLaunch()
            .withInfo(
                    Text.literal("Overhead Smash"),
                    Text.literal("slow, uninterruptible launcher"));
    public static final SimpleMultiHitAttack<CreamEntity> BALL_COMBO = new SimpleMultiHitAttack<CreamEntity>(200,
            36, 0.5f, 7f, 15, 2f, 0.1f, 0.3f, IntSet.of(10, 17, 25))
            .withSound(JSoundRegistry.CREAM_COMBO)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withHitAnimation(HitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Text.literal("Aerial Assault"),
                    Text.literal("less stun than grounded version"));
    public static final BallChargeAttack BALL_CHARGE = new BallChargeAttack(300, 13, 28, 1f)
            .withSound(JSoundRegistry.CREAM_BALLDASH)
            .withInfo(
                    Text.literal("Void Charge"),
                    Text.literal("Cream quickly transforms into a black hole and charges in the pointed direction"));
    public static final SurpriseMove DETACH_CHARGE = new SurpriseMove(300, 13, 28, 1f)
            .withSound(JSoundRegistry.CREAM_BALLDASH)
            .withInitAction((attacker, user, ctx) -> {
                attacker.endHalfBall();
                attacker.getMoveContext().set(OUT_POS, user.getPos().toVector3f());
                ctx.set(OUT_DIR, user.getRotationVector().multiply(0.75).toVector3f());
            })
            .withInfo(
                    Text.literal("Detaching Void Charge"),
                    Text.literal("""
                    Cream quickly transforms into a black hole and charges in the pointed direction.
                    The user exits cream upon performing this move."""));
    public static final BallChargeAttack BALL_DESTROY = new BallChargeAttack(300, 13, 28, 1f)
            .withSound(JSoundRegistry.CREAM_BALLDASH)
            .withInfo(
                    Text.literal("Destroy"),
                    Text.literal("Cream quickly transforms into a black hole and charges in a downward curve"));

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
                "above average cooldowns"
        );

        freespace = """
                BNBs (i. - in Cream):
                    M1>Assault>M1>Grab
                    i.M1>land+s.OFF>s.ON+Assault>M1>Charge>Grab
                    Chop>Destroy>Surprise
                    Chop>Void""";

        auraColors = new Vector3f[]{
                new Vector3f(0.5f, 0.1f, 0.3f),
                new Vector3f(0.5f, 0.6f, 0.8f),
                new Vector3f(1.0f, 0.5f, 0.7f),
                new Vector3f(1.0f, 0.5f, 0.5f)
        };
    }

    @Override
    public Vector3f getAuraColor() {
        if (getVoidTime() > 0) return new Vector3f();
        return super.getAuraColor();
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
            moves.register(MoveType.SPECIAL2, DETACH_CHARGE, State.BALL_CONSUME);
            moves.register(MoveType.SPECIAL3, BALL_DESTROY, State.BALL_CONSUME);

            moves.register(MoveType.UTILITY, EXIT, State.EXIT);
        } else {
            moves.registerImmediate(MoveType.LIGHT, PUNCH, State.LIGHT);

            moves.register(MoveType.HEAVY, VERTICAL_CHOP, State.HEAVY);
            moves.register(MoveType.BARRAGE, COMBO, State.COMBO);

            moves.register(MoveType.SPECIAL1, GRAB, State.GRAB);
            moves.register(MoveType.SPECIAL2, SURPRISE, State.SURPRISE);
            moves.register(MoveType.SPECIAL3, DESTROY, State.DESTROY).withCrouchingVariant(State.CHARGE);

            moves.register(MoveType.UTILITY, ENTER, State.ENTER);
        }

        moves.register(MoveType.ULTIMATE, CONSUME, State.CONSUME);
    }

    @Override
    public boolean initMove(MoveType type) {
        if (type == MoveType.LIGHT && curMove != null && curMove.getMoveType() == MoveType.LIGHT && getMoveStun() < curMove.getWindupPoint()) {
            AbstractMove<?, ? super CreamEntity> followup = curMove.getFollowup();
            if (followup != null) {
                setMove(followup, (State) followup.getAnimation());
                return true;
            }
        }

        return super.initMove(type);
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
    public boolean shouldOffsetHeight() {
        if (isHalfBall()) return false;
        return super.shouldOffsetHeight();
    }

    @Override
    protected Box calculateBoundingBox() {
        double x = getX();
        double y = getY();
        double z = getZ();

        if (isHalfBall())
            return new Box(x - 0.6, y + 0.0, z - 0.6, x + 0.6, y + 1.4, z + 0.6);
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
        boolean server = !getWorld().isClient();

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
                if (getWorld().getGameRules().getBoolean(JCraft.STAND_GRIEFING)) {
                    // Unfun 3x4x3 void code
                    for (int x = -1; x < 2; x++) {
                        for (int y = -1; y < 3; y++) {
                            for (int z = -1; z < 2; z++) {
                                BlockPos curPos = this.getBlockPos().add(x, y, z);
                                if (getWorld().getBlockState(curPos).getBlock().getBlastResistance() > 100.1f)
                                    continue;
                                getWorld().setBlockState(curPos, Block.getStateFromRawId(0));
                            }
                        }
                    }
                }

                if (notCorS && !isFree())
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 25, 0, false, false));

                if (charging) {
                    if (isFree()) { // Surprise move
                        Vector3f newPos = new Vector3f(getFreePos());
                        newPos.add(getMoveContext().get(SurpriseMove.OUT_DIR));
                        setFreePos(newPos);
                        if (getMoveStun() == 1) setFree(false);
                    } else if (chargeDir != null) { // Void Charge move
                        user.setVelocity(chargeDir);
                        user.velocityModified = true;
                        if (user instanceof ServerPlayerEntity player)
                            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(user));

                        if (curMove != null && curMove.getOriginalMove() == BALL_DESTROY)
                            chargeDir = chargeDir.add(
                                    new Vec3d(GravityChangerAPI.getGravityDirection(user).getUnitVector()).multiply(0.1)
                            ).normalize().multiply(0.5);
                    }
                } else { // Ultimate
                    setStateNoReset(State.IDLE);

                    if (!isPlayer)
                        handleAIVoid(user, voidTime);
                }

                Box damageBox = new Box(pos.add(1.5, 1.5, 1.5), pos.subtract(1.5, 1.5, 1.5));
                List<Entity> toDamage = getWorld().getEntitiesByClass(Entity.class,
                        damageBox, EntityPredicates.VALID_ENTITY);
                JUtils.displayHitbox(getWorld(), damageBox);

                toDamage.remove(user);
                toDamage.remove(this);

                boolean hurt;
                int stun = 2;
                float damage = 1.5f;
                if (charging) {
                    hurt = getMoveStun() % 2 == 0; // More consistent
                    stun = 4;
                    damage = 5.0f;
                } else {
                    hurt = age % 4 == 0;

                    setAlphaOverride(0);
                }

                for (Entity ent : toDamage) {
                    if (ent instanceof ItemEntity) {
                        ent.discard();
                        continue;
                    }
                    if (ent instanceof LivingEntity livingEntity) {
                        if (hurt) {
                            stun(livingEntity, stun, 0);
                            JUtils.cancelMoves(livingEntity);
                        }

                        livingEntity.damage(getWorld().getDamageSources().outOfWorld(), damage);
                    }
                }

                voidTime--;
                if (voidTime < 1)
                    resetAlphaOverride();
                setVoidTime(voidTime);
                setDistanceOffset(0);
            } else {
                for (int i = 0; i < 16; i++)
                    getWorld().addParticle(ParticleTypes.MYCELIUM,
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
                if (serverPlayer.isFallFlying())
                    serverPlayer.stopFallFlying();

                if (lastRemoteInputTime - age > 4) updateRemoteInputs(0, 0, false, false);

                Vec3d finalSpeed = Vec3d.ZERO;
                if (!blocking && !user.hasStatusEffect(JStatusRegistry.DAZED)) {
                    Direction gravity = GravityChangerAPI.getGravityDirection(this);
                    Vec3d gravityVec = new Vec3d(gravity.getUnitVector());

                    Vec3d userVel = JUtils.deltaPos(user);
                    Vec3d userPos = user.getPos();
                    Vec3d groundPos = getWorld().raycast(
                            new RaycastContext(
                                    userPos, userPos.add(gravityVec.multiply(24)),
                                    RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.SOURCE_ONLY, user) ).getPos();

                    double groundDist = groundPos.distanceTo(pos);
                    if (groundDist < 2) groundDist = 2; // Prevents extremely high jumps
                    Vec3d stabilization = userVel.multiply(gravityVec).multiply(10 / groundDist);

                    if (getRemoteJumpInput()) {
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 10, 2, true, false));
                        if (groundDist < 5)
                            GravityChangerAPI.addWorldVelocity(user, stabilization.subtract(gravityVec.multiply(0.25 / groundDist)));
                    }

                    Vec3d rotVec = Vec3d.fromPolar(getPitch(), getYaw());
                    Vec3d moveRotVec = Vec3d.ZERO;
                    float forward = (float) getRemoteForwardInput();
                    if (forward != 0) moveRotVec = moveRotVec.add(rotVec.multiply(forward)); // Forward movement
                    float side = (float) getRemoteSideInput();
                    if (side != 0) moveRotVec = moveRotVec.add(rotVec.rotateY(1.57079632679f * side)); // Side movement

                    finalSpeed = finalSpeed.add(moveRotVec.normalize().multiply(0.034));

                    user.addVelocity(finalSpeed.x, finalSpeed.y, finalSpeed.z);
                    user.velocityModified = true;
                }
            } else resetAlphaOverride();
        }
    }

    private void handleAIVoid(LivingEntity user, int voidTime) {
        double y = user.getY();
        Vec3d vel = new Vec3d(user.getVelocity().x, 0.0, user.getVelocity().z);

        // Targeting priority
        var damageRecord = user.getDamageTracker().getBiggestFall();
        Entity targetEntity = null;
        if (damageRecord != null) {
            targetEntity = damageRecord.damageSource().getAttacker();
        }
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
    @NonNull
    public CreamEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<CreamEntity> {
        IDLE((cream, builder) -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.cream." + ( cream.getVoidTime() > 0 ? "void" : cream.isHalfBall() ? "ball" : "" ) + "idle"))),
        LIGHT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cream.light"))),
        LIGHT_FOLLOWUP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cream.light_followup"))),
        BALL_LIGHT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cream.balllight"))),
        BLOCK(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.cream.block"))),
        BALL_BLOCK(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.cream.ballblock"))),
        HEAVY(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cream.heavy"))),
        BALL_HEAVY(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cream.ballheavy"))),
        COMBO(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cream.combo"))),
        BALL_COMBO(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cream.ballcombo"))),
        CONSUME(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cream.consume"))),
        BALL_CONSUME(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cream.ballconsume"))),
        SURPRISE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cream.surprise"))),
        CHARGE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cream.charge"))),
        GRAB(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cream.grab"))),
        GRAB_HIT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cream.grab_hit"))),
        ENTER(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cream.enter"))),
        EXIT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cream.exit"))),
        DESTROY(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cream.destroy"))),
        BITE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cream.bite")));

        private final BiConsumer<CreamEntity, AnimationState> animator;

        State(Consumer<AnimationState> animator) {
            this((creamEntity, builder) -> animator.accept(builder));
        }

        State(BiConsumer<CreamEntity, AnimationState> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(CreamEntity attacker, AnimationState state) {
            animator.accept(attacker, state);
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
