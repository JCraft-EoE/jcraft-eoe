package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.StateContainer;
import net.arna.jcraft.api.attack.enums.BlockableType;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.common.attack.actions.EffectAction;
import net.arna.jcraft.common.attack.moves.cream.*;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/Cream">Cream</a>.
 * @see JStandTypeRegistry#CREAM
 * @see net.arna.jcraft.client.renderer.entity.stands.CreamRenderer CreamRenderer
 * @see BallChargeAttack
 * @see BallModeExitMove
 * @see ConsumeAttack
 * @see CreamComboAttack
 * @see DestroyAttack
 * @see AbstractSurpriseMove
 */
@Getter
public class CreamEntity extends StandEntity<CreamEntity, CreamEntity.State> {
    public static final MoveSet<CreamEntity, CreamEntity.State> DEFAULT_MOVE_SET = MoveSetManager.create(JStandTypeRegistry.CREAM,
            CreamEntity::registerDefaultMoves, State.class);
    public static final MoveSet<CreamEntity, CreamEntity.State> HALF_BALL_MOVE_SET = MoveSetManager.create(JStandTypeRegistry.CREAM,
            "half_ball", CreamEntity::registerHalfBallMoves, State.class);
    public static final StandData DATA = StandData.builder()
            .idleRotation(220f)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.cream"))
                    .proCount(4)
                    .conCount(3)
                    .freeSpace(Component.literal("""
                BNBs (i. - in Cream):
                    Light>Assault>Light>Grab
                    i.Light>land+s.OFF>s.ON+Assault>Light>Charge>Grab
                    Chop>Destroy>Surprise
                    Chop>Void"""))
                    .skinName(Component.literal("Menace"))
                    .skinName(Component.literal("Eraser"))
                    .skinName(Component.literal("White Void"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.CREAM_SUMMON))
            .build();

    public static final SimpleAttack<CreamEntity> BITE = new SimpleAttack<CreamEntity>(0,
            7, 13, 0.75f, 6f, 20, 1.75f, 0.75f, 0.3f)
            .withAction(EffectAction.inflict(MobEffects.MOVEMENT_SLOWDOWN, 40, 1))
            .withAnim(State.BITE)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.LOW)
            .withSound(SoundEvents.EVOKER_FANGS_ATTACK)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Bite"),
                    Component.literal("applies Slowness II (2s) on hit"));
    public static final SimpleAttack<CreamEntity> LIGHT_FOLLOWUP = new SimpleAttack<CreamEntity>(
            0, 7, 14, 0.75f, 6f, 8, 1.75f, 1.1f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0.25, 1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Chop"),
                    Component.literal("quick combo finisher"));
    public static final SimpleAttack<CreamEntity> PUNCH = SimpleAttack.<CreamEntity>lightAttack(
            6, 14,0.75f, 5f, 20, 0.3f, -0.1f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(BITE)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(
                    Component.literal("Backhand"),
                    Component.literal("quick combo starter"));
    public static final SimpleAttack<CreamEntity> VERTICAL_CHOP = new SimpleAttack<CreamEntity>(30,
            20,30, 1f, 8f, 40, 1.5f, 0.8f, 0f)
            .withSound(JSoundRegistry.CREAM_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHyperArmor()
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Component.literal("Vertical Chop"),
                    Component.literal("slow, uninterruptible combo starter"));
    public static final CreamComboAttack COMBO = new CreamComboAttack(0,
            36, 0.75f,5f, 20, 2f, 0.2f, 0f, IntSet.of(10, 17, 25))
            .withSound(JSoundRegistry.CREAM_COMBO)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Assault"),
                    Component.literal("medium windup, good stun"));
    public static final SimpleAttack<CreamEntity> GRAB_HIT = new SimpleAttack<CreamEntity>(0,
            13, 20,1f, 6f, 5, 2f, 1.5f, 0f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Grab (Hit)"),
                    Component.empty());
    public static final GrabAttack<CreamEntity, State> GRAB = new GrabAttack<>(280,
            8, 20,1f, 3f, 30, 1.5f, 0f, 0f, GRAB_HIT, StateContainer.of(State.GRAB_HIT))
            .withSound(JSoundRegistry.CREAM_GRAB)
            .withInfo(
                    Component.literal("Grab"),
                    Component.literal("unblockable, knocks back"));
    public static final SurpriseMove SURPRISE = new SurpriseMove(100, 14, 24, 1f)
            .withSound(JSoundRegistry.CREAM_SUMMON)
            .withInfo(
                    Component.literal("Surprise"),
                    Component.literal("""
                            Cream disappears into the ground, then pops out in a nearby looked location.
                            If used while crouching, Cream appears in front of the user.
                            """));
    public static final ChargeBarrageAttack<CreamEntity> CHARGE = new ChargeBarrageAttack<CreamEntity>(100, 15, 30,
            4f, 2f, 10, 1.5f, 0.5f, 0f, 3, false)
            .withAction(EffectAction.inflict(JStatusRegistry.KNOCKDOWN, 25, 0, true, false))
            .withLaunchNoShockwave()
            .withImpactSound(JSoundRegistry.IMPACT_5)
            .withBlockableType(BlockableType.NON_BLOCKABLE)
            .withInfo(
                    Component.literal("Charge"),
                    Component.literal("4 block range, unblockable knockdown")
            );
    public static final DestroyAttack DESTROY = new DestroyAttack(100, 21, 30, 1f,
            8f, 5, 2f, 1.25f, 0f, 35)
            .withCrouchingVariant(CHARGE)
            .withSound(JSoundRegistry.CREAM_OVERHEAD)
            .withImpactSound(JSoundRegistry.IMPACT_5)
            .withLaunch()
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE)
            .withInfo(
                    Component.literal("Destroy"),
                    Component.literal("slow, uninterruptible, unblockable knockdown"));
    public static final ConsumeAttack CONSUME = new ConsumeAttack(640, 35, 40, 1f,
            2f, 0, 2f, 0f, 0f)
            .withSound(JSoundRegistry.CREAM_CONSUME)
            .withInfo(
                    Component.literal("Void"),
                    Component.literal("high windup, 6 seconds"));
    public static final BallModeEnterMove ENTER = new BallModeEnterMove(40, 10, 15, 0f)
            .withSound(JSoundRegistry.CREAM_ENTER)
            .withInfo(
                    Component.literal("Enter Cream"),
                    Component.literal("Cream consumes itself and the user halfway, increasing mobility and decreasing defense"));
    public static final BallModeExitMove EXIT = new BallModeExitMove(40, 5, 15, 0f)
            .withSound(JSoundRegistry.CREAM_EXIT)
            .withInfo(
                    Component.literal("Exit Cream"),
                    Component.literal("Cream and its user return from the void"));
    public static final SimpleAttack<CreamEntity> SWIPE = new SimpleAttack<CreamEntity>(0, 7,
            14, 0.5f, 5f, 20, 2f, 0.75f, 0.2f)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Component.literal("Swipe"),
                    Component.literal("quick air-to-ground poke"));
    public static final KnockdownAttack<CreamEntity> OVERHEAD_SMASH = new KnockdownAttack<CreamEntity>(0,
            14, 20, 0.5f, 9f, 15, 2f, 1.25f, 0.3f, 35)
            .withSound(JSoundRegistry.CREAM_SMASH)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHyperArmor()
            .withLaunch()
            .withInfo(
                    Component.literal("Overhead Smash"),
                    Component.literal("slow, uninterruptible launcher"));
    public static final SimpleMultiHitAttack<CreamEntity> BALL_COMBO = new SimpleMultiHitAttack<CreamEntity>(0,
            36, 0.5f, 7f, 15, 2f, 0.1f, 0.3f, IntSet.of(10, 17, 25))
            .withSound(JSoundRegistry.CREAM_COMBO)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Component.literal("Aerial Assault"),
                    Component.literal("less stun than grounded version"));
    public static final BallChargeAttack BALL_CHARGE = new BallChargeAttack(200, 13, 28, 1f, false)
            .withSound(JSoundRegistry.CREAM_BALLDASH)
            .withInfo(
                    Component.literal("Void Charge"),
                    Component.literal("Cream quickly transforms into a black hole and charges in the pointed direction"));
    public static final DetachChargeMove DETACH_CHARGE = new DetachChargeMove(200, 13, 28, 1f)
            .withSound(JSoundRegistry.CREAM_BALLDASH)
            .withInfo(
                    Component.literal("Detaching Void Charge"),
                    Component.literal("""
                            Cream quickly transforms into a black hole and charges in the pointed direction.
                            The user exits cream upon performing this move.""")
            );
    public static final BallChargeAttack BALL_DESTROY = new BallChargeAttack(200, 13, 28, 1f, true)
            .withSound(JSoundRegistry.CREAM_BALLDASH)
            .withInfo(
                    Component.literal("Destroy"),
                    Component.literal("Cream quickly transforms into a black hole and charges in a downward curve"));

    private static final EntityDataAccessor<Integer> VOID_TIME = SynchedEntityData.defineId(CreamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HALF_BALL = SynchedEntityData.defineId(CreamEntity.class, EntityDataSerializers.BOOLEAN);
    @Setter
    private Vec3 chargeDir;
    @Setter
    private boolean charging = false;

    public CreamEntity(Level worldIn) {
        super(JStandTypeRegistry.CREAM.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(0.5f, 0.1f, 0.3f),
                new Vector3f(0.5f, 0.6f, 0.8f),
                new Vector3f(1.0f, 0.5f, 0.7f),
                new Vector3f(1.0f, 0.5f, 0.5f)
        };
    }

    @Override
    public Vector3f getAuraColor() {
        if (getVoidTime() > 0) {
            return new Vector3f();
        }
        return super.getAuraColor();
    }

    public void beginHalfBall() {
        entityData.set(HALF_BALL, true);
        maxStandGauge = 45f;

        switchMoveSet(HALF_BALL_MOVE_SET.getName());
    }

    public void endHalfBall() {
        entityData.set(HALF_BALL, false);
        maxStandGauge = 90f;

        switchMoveSet(DEFAULT_MOVE_SET.getName());
    }

    @Override
    public StandData getStandData() {
        StandData data = super.getStandData();

        if (isHalfBall()) {
            // Cream is the only one that needs this, so this hack is fine.
            // The getStandData() method is always used to get the StandData,
            // the StandType#getData() method is never used directly.
            return data
                    .withIdleDistance(0f)
                    .withBlockDistance(0f);
        }

        return data;
    }

    public boolean isHalfBall() {
        return entityData.get(HALF_BALL);
    }

    public int getVoidTime() {
        return entityData.get(VOID_TIME);
    }

    public void setVoidTime(int vTime) {
        entityData.set(VOID_TIME, vTime);
        if (vTime == 0) {
            setReset(true);
        }
    }

    private static void registerHalfBallMoves(MoveMap<CreamEntity, State> moves) {
        moves.register(MoveClass.LIGHT, SWIPE, State.BALL_LIGHT);

        moves.register(MoveClass.HEAVY, OVERHEAD_SMASH, State.BALL_HEAVY);
        moves.register(MoveClass.BARRAGE, BALL_COMBO, State.BALL_COMBO);

        moves.register(MoveClass.SPECIAL1, BALL_CHARGE, State.BALL_CONSUME);
        moves.register(MoveClass.SPECIAL2, DETACH_CHARGE, State.BALL_CONSUME);
        moves.register(MoveClass.SPECIAL3, BALL_DESTROY, State.BALL_CONSUME);

        moves.register(MoveClass.UTILITY, EXIT, State.EXIT);

        moves.register(MoveClass.ULTIMATE, CONSUME, State.CONSUME);
    }

    private static void registerDefaultMoves(MoveMap<CreamEntity, State> moves) {
        moves.registerImmediate(MoveClass.LIGHT, PUNCH, State.LIGHT);

        moves.register(MoveClass.HEAVY, VERTICAL_CHOP, State.HEAVY);
        moves.register(MoveClass.BARRAGE, COMBO, State.COMBO);

        moves.register(MoveClass.SPECIAL1, GRAB, State.GRAB);
        moves.register(MoveClass.SPECIAL2, SURPRISE, State.SURPRISE);
        moves.register(MoveClass.SPECIAL3, DESTROY, State.DESTROY).withCrouchingVariant(State.CHARGE);

        moves.register(MoveClass.UTILITY, ENTER, State.ENTER);

        moves.register(MoveClass.ULTIMATE, CONSUME, State.CONSUME);
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        if (tryFollowUp(moveClass, MoveClass.LIGHT)) return true;
        return super.initMove(moveClass);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        getEntityData().define(VOID_TIME, 0);
        getEntityData().define(HALF_BALL, false);
    }

    @Override
    public boolean canAttack() {
        if (hasUser() && !(getUser() instanceof Player) && getVoidTime() > 0) {
            return false; // Prevents mobs from attacking while in void state and cancelling void early
        }
        return super.canAttack();
    }

    @Override
    public boolean shouldOffsetHeight() {
        if (isHalfBall()) {
            return false;
        }
        return super.shouldOffsetHeight();
    }

    @Override
    protected @NonNull AABB makeBoundingBox() {
        final double x = getX(), y = getY(), z = getZ();

        if (isHalfBall()) {
            return new AABB(x - 0.6, y + 0.0, z - 0.6, x + 0.6, y + 1.4, z + 0.6);
        }
        if (getState() == State.SURPRISE) {
            return new AABB(x - 0.6, y + 0, z - 0.6, x + 0.6, y + 0.3, z + 0.6);
        }
        return super.makeBoundingBox();
    }

    @Override
    public void desummon() {
        // Stop voiding if voiding
        if (getVoidTime() > 0) {
            setVoidTime(0);
            if (hasUser()) {
                getUser().setInvulnerable(false);
            }
            return;
        }

        // Real desummon if not voiding
        super.desummon();
    }

    @Override
    public void remove(@NonNull RemovalReason reason) {
        if (getUser() instanceof Player player && !player.isCreative() && !player.isSpectator()) {
            final Abilities abilities = player.getAbilities();

            abilities.flying = false;
            abilities.mayfly = false;

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(abilities));
            }
        }
        super.remove(reason);
    }

    @Override
    public boolean defaultToNear() {
        if (charging) {
            return false;
        }
        return super.defaultToNear();
    }

    @Override
    public void tick() {
        super.tick();
        boolean server = !level().isClientSide();

        if (!hasUser()) {
            return;
        }
        final LivingEntity user = getUserOrThrow();
        final boolean userIsPlayer;
        boolean notCreativeOrSpectator = false;

        final Vec3 pos = getEyePosition();
        int voidTime = getVoidTime();
        boolean voiding = (voidTime > 0);

        // Players get creative flight, and mobs get gravity removed and y level equalization with target; see: handleAIVoid()
        if (user instanceof Player playerEntity) {
            notCreativeOrSpectator = (!playerEntity.isCreative() && !playerEntity.isSpectator());
            if (notCreativeOrSpectator && !charging && !isFree()) {
                playerEntity.getAbilities().flying = voiding;
            }
            userIsPlayer = true;
        } else {
            userIsPlayer = false;
        }

        if (server) {
            if (!charging) {
                if (getCurrentMove() != null) {
                    setVoidTime(0);
                    resetAlphaOverride();
                    voiding = false;
                }
                idleOverride = getVoidTime() > 0;
            }

            user.setInvulnerable(getVoidTime() > 0);
        }

        if (voiding) {
            tickVoiding(server, notCreativeOrSpectator, user, userIsPlayer, voidTime, pos);
        } else {
            tickNotVoiding(user, pos);
        }
    }

    private void tickNotVoiding(LivingEntity user, Vec3 pos) {
        if (isIdle() && charging) {
            charging = false;
            setFree(false);
        }

        if (!isHalfBall()) {
            return;
        }
        setAlphaOverride(0.1f);
        user.resetFallDistance();
        user.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 5, 9, true, false));

        // Player Half-Ball controls
        if (user instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.isFallFlying()) {
                serverPlayer.stopFallFlying();
            }

            if (lastRemoteInputTime - tickCount > 4) {
                updateRemoteInputs(0, 0, false, false);
            }

            Vec3 finalSpeed = Vec3.ZERO;
            if (!blocking && !user.hasEffect(JStatusRegistry.DAZED.get())) {
                final Vec3 gravityVec = new Vec3(GravityChangerAPI.getGravityDirection(this).step());

                final Vec3 userVel = JUtils.deltaPos(user);
                final Vec3 userPos = user.position();
                final Vec3 groundPos = level().clip(
                        new ClipContext(
                                userPos, userPos.add(gravityVec.scale(24)),
                                ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, user)).getLocation();

                double groundDist = groundPos.distanceTo(pos);
                if (groundDist < 2) {
                    groundDist = 2; // Prevents extremely high jumps
                }
                final Vec3 stabilization = userVel.multiply(gravityVec).scale(10 / groundDist);

                if (getRemoteJumpInput()) {
                    user.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 10, 2, true, false));
                    if (groundDist < 5) {
                        GravityChangerAPI.addWorldVelocity(user, stabilization.subtract(gravityVec.scale(0.25 / groundDist)));
                    }
                }

                final Vec3 rotVec = Vec3.directionFromRotation(getXRot(), getYRot());
                Vec3 moveRotVec = Vec3.ZERO;
                float forward = (float) getRemoteForwardInput();
                if (forward != 0) {
                    moveRotVec = moveRotVec.add(rotVec.scale(forward)); // Forward movement
                }
                float side = (float) getRemoteSideInput();
                if (side != 0) {
                    moveRotVec = moveRotVec.add(rotVec.yRot(1.57079632679f * side)); // Side movement
                }

                finalSpeed = finalSpeed.add(moveRotVec.normalize().scale(0.034));

                user.push(finalSpeed.x, finalSpeed.y, finalSpeed.z);
                user.hurtMarked = true;
            }
        } else {
            resetAlphaOverride();
        }
    }

    private void tickVoiding(boolean server, boolean notCreativeOrSpectator, LivingEntity user, boolean userIsPlayer, int voidTime, Vec3 pos) {
        if (!server) {
            for (int i = 0; i < 16; i++) {
                level().addParticle(ParticleTypes.MYCELIUM,
                        pos.x + (random.nextFloat() - 0.5f) * 2f,
                        pos.y + (random.nextFloat() - 0.5f) * 2f,
                        pos.z + (random.nextFloat() - 0.5f) * 2f,
                        0, 0, 0);
            }

            return;
        }

        if (level().getGameRules().getBoolean(JCraft.STAND_GRIEFING)) {
            final BlockPos blockPos = blockPosition();
            // Fun 3x4x3 void code
            BlockPos from = blockPos.offset(-1, -1, -1);
            BlockPos to = blockPos.offset(1, 2, 1);
            BlockPos.betweenClosed(from, to).forEach(p -> {
                if (!AbstractMove.mayBreak(user, p, b -> b.getBlock().getExplosionResistance() <= 100f))
                    return;

                if (!JServerConfig.CREAM_ITEM_ERASE.getValue()) {
                    // Drop items before destroying the block
                    Block.dropResources(level().getBlockState(p), level(), p);
                }

                level().setBlockAndUpdate(p, Block.stateById(0));
            });
        }

        // Blind normal players while in void
        if (notCreativeOrSpectator && !isFree()) {
            user.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 25, 0, false, false));
        }

        if (charging) {
            if (isFree()) { // Surprise move
                final Vector3f newPos = getFreePos();
                // Find outDir
                Vector3f outDir = getCurrentMove() instanceof AbstractSurpriseMove<?> surpriseMove ?
                        surpriseMove.getOutDir() : new Vector3f();
                newPos.add(outDir);
                setFreePos(newPos);
                if (getMoveStun() == 1) {
                    setFree(false);
                }
            } else if (chargeDir != null) { // Void Charge move
                user.setDeltaMovement(chargeDir);
                user.hurtMarked = true;
                if (user instanceof ServerPlayer player) {
                    player.connection.send(new ClientboundSetEntityMotionPacket(user));
                }
            }
        } else { // Ultimate
            setStateNoReset(State.IDLE);

            if (!userIsPlayer) {
                handleAIVoid(user, voidTime);
            }
        }

        final AABB damageBox = new AABB(pos.add(1.5, 1.5, 1.5), pos.subtract(1.5, 1.5, 1.5));
        final List<Entity> toDamage = level().getEntitiesOfClass(Entity.class,
                damageBox, EntitySelector.ENTITY_STILL_ALIVE.and(EntitySelector.NO_CREATIVE_OR_SPECTATOR));
        JUtils.displayHitbox(level(), damageBox);

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
            hurt = tickCount % 4 == 0;

            setAlphaOverride(0);
        }

        for (Entity ent : toDamage) {
             if (ent instanceof ItemEntity) {
                 if (JServerConfig.CREAM_ITEM_ERASE.getValue()) {
                     ent.discard();
                 }
                 continue;
            }
            if (ent instanceof LivingEntity livingEntity) {
                if (hurt) {
                    JCraft.stun(livingEntity, stun, 0, user);
                    JUtils.cancelMoves(livingEntity);
                }

                livingEntity.hurt(level().damageSources().fellOutOfWorld(), damage);
            }
        }

        voidTime--;
        if (voidTime < 1) {
            resetAlphaOverride();
        }
        setVoidTime(voidTime);
        setDistanceOffset(0);
    }

    private void handleAIVoid(LivingEntity user, int voidTime) {
        double y = user.getY();
        Vec3 vel = new Vec3(user.getDeltaMovement().x, 0.0, user.getDeltaMovement().z);

        // Targeting priority
        var damageRecord = user.getCombatTracker().getMostSignificantFall();
        Entity targetEntity = null;
        if (damageRecord != null) {
            targetEntity = damageRecord.source().getEntity();
        }
        if (targetEntity == null && user instanceof Mob mob) {
            targetEntity = mob.getTarget();
        }
        if (targetEntity == null) {
            targetEntity = user.getLastHurtByMob();
        }

        // If target wasn't found, thrash around
        Vec3 target = targetEntity != null ? targetEntity.position() : this.position().add(Math.sin(this.tickCount * 0.2) * 2, Math.sin(this.tickCount * 0.2) / 4, Math.cos(this.tickCount * 0.2) * 2);

        final double dY = Mth.clamp(target.y() - y, -1, 1);
        y += dY;

        vel = vel.add(target.subtract(user.position().add(random.nextDouble() * 2, random.nextDouble() * 3, random.nextDouble() * 3)).normalize()).scale(0.3);

        user.setDeltaMovement(vel);
        user.setPosRaw(user.getX(), y, user.getZ());

        if (voidTime < 10) {
            user.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 5, 1, true, false));
        }
    }

    @Override
    @NonNull
    public CreamEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<CreamEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.cream.idle", AzPlayBehaviors.LOOP)),
        LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cream.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cream.light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BALL_LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cream.balllight", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.cream.block", AzPlayBehaviors.LOOP)),
        BALL_BLOCK(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cream.ballblock", AzPlayBehaviors.LOOP)),
        HEAVY(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cream.heavy", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BALL_HEAVY(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cream.ballheavy", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        COMBO(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cream.combo", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BALL_COMBO(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cream.ballcombo", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        CONSUME(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cream.consume", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BALL_CONSUME(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cream.ballconsume", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        SURPRISE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cream.surprise", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        CHARGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cream.charge", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        GRAB(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cream.grab", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        GRAB_HIT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cream.grab_hit", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ENTER(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cream.enter", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        EXIT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cream.exit", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        DESTROY(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cream.destroy", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BITE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cream.bite", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),

        VOID_IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.cream.voididle", AzPlayBehaviors.LOOP)),
        HALF_BALL_IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.cream.ballidle", AzPlayBehaviors.LOOP)),

        ;

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(CreamEntity attacker) {
            if (animator == IDLE.animator) {
                if (attacker.getVoidTime() > 0) {
                    VOID_IDLE.animator.sendForEntity(attacker);
                    return;
                } else if (attacker.isHalfBall()) {
                    HALF_BALL_IDLE.animator.sendForEntity(attacker);
                    return;
                }
            }

            animator.sendForEntity(attacker);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
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
