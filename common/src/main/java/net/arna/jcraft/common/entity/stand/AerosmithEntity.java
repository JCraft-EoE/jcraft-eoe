package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.AttackData;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.component.living.CommonCooldownsComponent;
import net.arna.jcraft.api.component.living.CommonMiscComponent;
import net.arna.jcraft.api.misc.BoundSoundPlayer;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.common.ai.AttackerBrainInfo;
import net.arna.jcraft.common.ai.CombatInstantContext;
import net.arna.jcraft.common.attack.conditions.RemoteCondition;
import net.arna.jcraft.common.attack.moves.aerosmith.*;
import net.arna.jcraft.common.attack.moves.shared.MainBarrageAttack;
import net.arna.jcraft.common.entity.CarbonDioxideRadarEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class AerosmithEntity extends StandEntity<AerosmithEntity, AerosmithEntity.State> {
    public static final MoveSet<AerosmithEntity, AerosmithEntity.State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.AEROSMITH,
            AerosmithEntity::registerDefaultMoves, AerosmithEntity.class, AerosmithEntity.State.class);

    public static final EntityDataAccessor<Float> OVERHEAT = SynchedEntityData.defineId(AerosmithEntity.class, EntityDataSerializers.FLOAT);
    public static final float OVERHEAT_MAX = 15f;
    public static final int OVERHEAT_LOSS_COOLDOWN_MAX = 20;
    public static final double SLOW_CRUISE_SPEED = 0.075;
    public static final double CRUISE_SPEED = 0.15;
    public static final double MAX_SPEED = 0.22;
    public static final float DEFAULT_PATROL_RADIUS = 48.0f;
    public static final int FORCED_RETURN_ULT_COOLDOWN = 30 * 20;

    private static final float STUN_TURN_RATE_MULTIPLIER = 0.7f;
    private static final float STUN_SPEED_MULTIPLIER = 0.9f;

    public enum FlyState {
        NONE,
        FLYBY,
        PATROL,
        RETURN,
    }

    @Getter @Setter
    private float patrolRadius = DEFAULT_PATROL_RADIUS;

    @NonNull @Getter @Setter
    private Vec3 flyTarget = Vec3.ZERO;

    @Getter
    private FlyState flyState = FlyState.NONE;

    private boolean forcedReturn = false;

    @Getter
    private boolean inLineOfSight = false;

    private int overheatLossCooldown = 0;

    // client-side rotation tracking for rendering
    public float oldPitch = 0.0f, oldYaw = 0.0f, oldRoll = 0.0f;
    public float pitch = 0.0f, yaw = 0.0f, roll = 0.0f;

    public static final MuzzleHitscanAttack BULLET = new MuzzleHitscanAttack(
            1, 1, 2, 1.5f, 2f, 0, 0.12f, 30f, 0.01f)
            .withBlockStun(0)
            .withSound(JSoundRegistry.AS_SHOOT)
            .withHitSpark(JParticleType.HIT_SPARK_1)
            .withShootSpark(JParticleType.LEMON)
            .withStunType(StunType.WINDED)
            .withInfo(
                    Component.literal("Fire Gunpods"),
                    Component.literal("Shoots supersonic bullets in front of Aerosmith. Prolonged use overheats the guns, making them far less accurate.")
            );

    public static final ItemDropAttack ITEM_DROP = new ItemDropAttack(200, 67f)
            .withInfo(
                    Component.literal("Item Drop"),
                    Component.literal("Orders Aerosmith to drop an item above a given location.")
            );

    public static final BombDropAttack BOMB_DROP = new BombDropAttack(200, 67f)
            .withCrouchingVariant(ITEM_DROP)
            .withInfo(
                    Component.literal("Bomb Drop"),
                    Component.literal("Orders Aerosmith to drop a bomb above a given location.")
            );

    public static final AerosmithChargeAttack CHARGE = new AerosmithChargeAttack(
            100, 50, 1.0f, 15, 1.66f, 0.1f, 0.0f,
            IntSet.of(10, 15, 20, 25, 30, 35, 40, 45, 50))
            .withStaticY()
            .withStunType(StunType.LAUNCH)
            .withSound(JSoundRegistry.AS_MANEUVER)
            .withImpactSound(JSoundRegistry.AS_BARRAGE_HIT)
            .withInfo(
                    Component.literal("Dive Charge"),
                    Component.literal("Non-remote: a straight charge, rising at the end. Carries enemies with Aerosmith.")
            );

    public static final MainBarrageAttack<AerosmithEntity> SAWBLADE = new MainBarrageAttack<AerosmithEntity>(280,
            0, 24, 0.9f, 1f, 13, 1.5f, 0.1f, 0f, 3, Blocks.OAK_LEAVES.defaultDestroyTime())
            .withSound(JSoundRegistry.AS_NAME)
            .withImpactSound(JSoundRegistry.AS_BARRAGE_HIT)
            .withAerialVariant(CHARGE)
            .withInfo(
                    Component.literal("Propeller Strike"),
                    Component.literal("A makeshift barrage wielding Aerosmith as a saw-blade.")
            );

    public static final AerosmithAttackOrderMove ATTACK_ORDER_MOVE = new AerosmithAttackOrderMove(0, 0)
            .withInfo(
                    Component.literal("Attack Order"),
                    Component.literal("Orders Aerosmith to attack the entity at a detected location.")
            );

    public static final FlybyMove FLYBY = new FlybyMove(0, 67f)
            .withInfo(
                    Component.literal("Fly by Location"),
                    Component.literal("Orders Aerosmith to fly to a given location, returning after arriving.")
            );

    public static final BombThrowAttack BOMB_THROW = new BombThrowAttack(200, 9, 42, 1.2f)
            .withSound(JSoundRegistry.AS_BARRAGE)
            .withInfo(
                    Component.literal("Loft Bombing"),
                    Component.literal("Orders Aerosmith to throw a bomb in front of the user using its inertia.")
            )
            .withCondition(new RemoteCondition(false));

    public static final BreathXrayMove<AerosmithEntity> XRAY = new BreathXrayMove<AerosmithEntity>(0, 0, 128, true)
            .withInfo(
                    Component.literal("Breath Detection"),
                    Component.literal("Aerosmith scans the surroundings for the breath of living things.")
            );

    public static final PatrolMove PATROL = new PatrolMove(0, 67f, 48f)
            .withInfo(
                    Component.literal("Patrol Location"),
                    Component.literal("Orders Aerosmith to fly around a given location. Use while crouching to invert the patrol direction.")
            );

    public static final StandData DATA = StandData.builder()
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.aerosmith"))
                    .skinName(Component.literal("Manga"))
                    .skinName(Component.literal("Vento Aureo"))
                    .skinName(Component.literal("Interceptor"))
                    .freeSpace(Component.literal("Remote-oriented stand with individually powerful moves that are weak up-close. While remote, Aerosmith's moves cannot be cancelled by the user being hit."))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.AS_SUMMON).withAnimDuration(48))
            .build();

    private CommonMiscComponent miscComponent;
    private int overheatTick;

    @Getter
    private final MuzzleHitscanAttack shootAttack;
    @Getter
    private final BombDropAttack bombDropAttack;
    @Getter
    private final ItemDropAttack itemDropAttack;
    @Getter
    private final AerosmithChargeAttack chargeAttack;
    @Getter
    private final BreathXrayMove<AerosmithEntity> breathXrayMove;
    @Getter
    private final AerosmithAttackOrderMove attackOrderMove;

    // Tracks the move that was active just before cancelMove() was called,
    // so setMove() can tell apart re-initiating the same move in a continuous loop
    // (e.g. light attack holding) from actually starting a new move.
    @Nullable
    private AbstractMove<?, ? super AerosmithEntity> preCancelMove = null;
    private BoundSoundPlayer.SoundHandle volaHandle;

    private static final EntityDataAccessor<Boolean> ALLOW_MOVE_HANDLING = SynchedEntityData.defineId(AerosmithEntity.class, EntityDataSerializers.BOOLEAN);
    public boolean isAllowingMoveHandling() { return entityData.get(ALLOW_MOVE_HANDLING); }

    public enum PatrolDirection {
        CLOCKWISE(1),
        COUNTER_CLOCKWISE(-1);

        @Getter
        private final float value;

        PatrolDirection(float value) {
            this.value = value;
        }

        public PatrolDirection invert() {
            if (value == 1)
                return COUNTER_CLOCKWISE;
            return CLOCKWISE;
        }
    }

    @Getter @Setter
    private PatrolDirection patrolDirection = PatrolDirection.CLOCKWISE;

    public AerosmithEntity(final Level world) {
        super(JStandTypeRegistry.AEROSMITH.get(), world);
        // setYDistanceOffset(10f); // TODO for patrol mode
        setYDistanceOffset(1.2f);
        setNoGravity(true);

        shootAttack = getMove(MuzzleHitscanAttack.class);
        bombDropAttack = getMove(BombDropAttack.class);
        itemDropAttack = getMove(ItemDropAttack.class);
        chargeAttack = getMove(AerosmithChargeAttack.class);
        breathXrayMove = getMove(BreathXrayMove.class);
        attackOrderMove = getMove(AerosmithAttackOrderMove.class);

        auraColors = new Vector3f[]{
                new Vector3f(0.9f, 0.3f, 0.2f),
                new Vector3f(0.2f, 0.4f, 0.7f),
                new Vector3f(0.8f, 0.2f, 0.5f),
                new Vector3f(0.0f, 0.1f, 1.0f)
        };
    }

    @NonNull
    public ItemStack getHeldItem() { return getItemBySlot(EquipmentSlot.FEET); }
    public void setHeldItem(final @NonNull ItemStack stack) { setItemSlot(EquipmentSlot.FEET, stack); }

    @Override
    public boolean remoteControllable() {
        return false;
    }

    @Override
    public boolean shouldOffsetHeight() {
        return getState() == State.IDLE || getState() == State.BLOCK;
    }

    @Override
    public boolean allowMoveHandling() {
        if (forcedReturn) return false;
        return getCurrentMove() == shootAttack ||
                (
                getCurrentMove() == null &&
                getMoveStun() < JCraft.QUEUE_MOVESTUN_LIMIT &&
                bombDropAttack.getDropLocation() == null &&
                itemDropAttack.getDropLocation() == null &&
                attackOrderMove.getCurrentTarget() == null
                );
    }

    public void triggerForcedReturn() {
        forcedReturn = true;
        bombDropAttack.clearDropLocation();
        itemDropAttack.clearDropLocation();
        cancelMove(false);
        setFlyState(FlyState.RETURN);

        if (!hasUser()) return;
        CommonCooldownsComponent cooldowns = JComponentPlatformUtils.getCooldowns(getUserOrThrow());
        cooldowns.setCooldown(CooldownType.ULTIMATE, FORCED_RETURN_ULT_COOLDOWN);
    }

    @Override
    public @NonNull AerosmithEntity getThis() {
        return this;
    }

    private static void registerDefaultMoves(final @NonNull MoveMap<AerosmithEntity, AerosmithEntity.State> moves) {
        moves.register(MoveClass.LIGHT, BULLET, State.LIGHT);
        moves.register(MoveClass.HEAVY, BOMB_DROP, State.ACTIVE);
        moves.register(MoveClass.BARRAGE, SAWBLADE, State.SAWBLADE).withAerialVariant(State.CHARGE);
        moves.register(MoveClass.SPECIAL1, FLYBY);
        moves.register(MoveClass.SPECIAL2, BOMB_THROW, State.BOMB);
        moves.register(MoveClass.SPECIAL3, XRAY);
        moves.register(MoveClass.ULTIMATE, ATTACK_ORDER_MOVE);
        moves.register(MoveClass.UTILITY, PATROL, State.ACTIVE);
    }

    private boolean xRotChangeAllowed = false;
    public void allowXRotChange() { xRotChangeAllowed = true; }
    public void disallowXRotChange() { xRotChangeAllowed = false; }

    @Override
    public void setXRot(float xRot) {
        if (!xRotChangeAllowed) return;
        super.setXRot(xRot);
    }

    @Override
    public void lookAt(@NonNull final EntityAnchorArgument.Anchor anchor, @NonNull final Vec3 target) {
        boolean store = xRotChangeAllowed;
        xRotChangeAllowed = true;
        super.lookAt(anchor, target);
        xRotChangeAllowed = store;
    }

    @Override
    public boolean canBlock() {
        if (isRemote()) return false;
        return super.canBlock();
    }

    @Override
    public void cancelMove(boolean offensiveCancel) {
        if (offensiveCancel && isRemote()) return;
        preCancelMove = getCurrentMove();
        super.cancelMove(offensiveCancel);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (!level().isClientSide()) {
            attackOrderMove.onHitTarget(this, attackOrderMove.getCurrentTarget());
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void onJCraftDamageReceived(AttackData attackData) {
        attackOrderMove.onHitTarget(this, attackOrderMove.getCurrentTarget());
    }

    @Override
    public boolean hasAlphaOverride() {
        return super.hasAlphaOverride() || !isRemote();
    }

    @Override
    public float getAlphaOverride() {
        if (!isRemote())
            return 0.15f;
        return super.getAlphaOverride();
    }

    @Override
    public void tick() {
        xRotChangeAllowed = false;
        noPhysics = true;

        if (isRemote()) wantToBlock = false;
        super.tick();

        if (flyState == FlyState.RETURN && getState() != State.RECALL)
            setState(State.RECALL);

        noPhysics = false;
        xRotChangeAllowed = true;

        resetFallDistance();

        final LivingEntity user = getUser();

        if (!isAlive() || user == null)
            return;

        if (level().isClientSide()) {
            inLineOfSight = isRemote() && hasUser() && user.hasLineOfSight(this);
            return;
        }

        entityData.set(ALLOW_MOVE_HANDLING, allowMoveHandling());

        if (forcedReturn) {
            final ServerLevel serverLevel = (ServerLevel) level();
            final Vec3 behind = position().subtract(getLookAngle().scale(0.4));
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    behind.x, behind.y, behind.z,
                    2, 0.15, 0.15, 0.15, 0.02);
        }

        BulletHeatManager.tick((ServerLevel) level());

        // Spawn the radar entity on the first valid server tick so it follows the user.
        if (tickCount == 1) {
            final CarbonDioxideRadarEntity radar = new CarbonDioxideRadarEntity(level());
            radar.setUser(user);
            radar.setPos(user.getX(), user.getY(), user.getZ());
            level().addFreshEntity(radar);
        }

        final AbstractMove<?, ? super AerosmithEntity> currentMove = getCurrentMove();

        boolean isShooting = currentMove instanceof MuzzleHitscanAttack;

        if (isShooting) {
            overheatLossCooldown = OVERHEAT_LOSS_COOLDOWN_MAX;
        } else if (overheatLossCooldown-- <= 0) {
            if (++overheatTick % 5 == 0)
                addOverheat(-0.4f);
        }

        if (isInWall() && patrolRadius > patrolRadius / 2) {
            patrolRadius -= 0.2f;
        }

        if (currentMove != chargeAttack) {
            final boolean isStunned = user.hasEffect(JStatusRegistry.DAZED.get());
            final float turnMult = isStunned ? STUN_TURN_RATE_MULTIPLIER : 1f;
            final float speedMult = isStunned ? STUN_SPEED_MULTIPLIER : 1f;

            switch (flyState) {
                case PATROL -> {
                    final float theta = tickCount / patrolRadius / 2.0f * patrolDirection.getValue();
                    final Vec3 offset = RotationUtil.vecPlayerToWorld(
                            Mth.sin(theta) * patrolRadius, 1.0, Mth.cos(theta) * patrolRadius,
                            GravityChangerAPI.getGravityDirection(this)
                    );

                    lookAt(flyTarget.add(offset), 6f * turnMult, 6f * turnMult);

                    setDeltaMovement(getDeltaMovement().scale(0.9).add(getLookAngle().scale(SLOW_CRUISE_SPEED * speedMult)));
                }
                case FLYBY -> {
                    lookAt(flyTarget, 6f * turnMult, 6f * turnMult);

                    final double distanceSqr = distanceToSqr(flyTarget);
                    double cruiseSpeed = distanceSqr <= 49.0 ? SLOW_CRUISE_SPEED : CRUISE_SPEED;

                    final var attackTarget = attackOrderMove.getCurrentTarget();
                    final boolean hasAttackTarget = attackTarget != null;

                    if (hasAttackTarget) {
                        // moving away from target
                        if (distanceToSqr(attackTarget) > attackTarget.distanceToSqr(xOld, yOld, zOld)) {
                            cruiseSpeed = MAX_SPEED;
                        }
                    }

                    setDeltaMovement(getDeltaMovement().scale(0.9).add(getLookAngle().scale(cruiseSpeed * speedMult)));

                    if (distanceToSqr(flyTarget) <= 4.0
                            && bombDropAttack.getDropLocation() == null
                            && itemDropAttack.getDropLocation() == null) {
                        setFlyState(hasAttackTarget ? FlyState.PATROL : FlyState.RETURN);
                    }
                }
                case RETURN -> {
                    final Vec3 targetPos = user.position();
                    lookAt(targetPos, 6f * turnMult, 12f * turnMult);

                    final double distanceSqr = distanceToSqr(targetPos);
                    final double cruiseSpeed = distanceSqr <= 49.0 ? SLOW_CRUISE_SPEED : CRUISE_SPEED;

                    setDeltaMovement(getDeltaMovement().scale(0.9).add(getLookAngle().scale(cruiseSpeed * speedMult)));

                    if (distanceSqr <= 6.25) {
                        setRemote(false);
                        setFlyState(FlyState.NONE);
                        playSound(JSoundRegistry.AS_LANDING.get());
                    }
                }
            }
        }

        xRotChangeAllowed = false;
    }

    public void setFlyState(FlyState flyState) {
        FlyState prev = this.flyState;
        this.flyState = flyState;

        if (flyState == FlyState.NONE && prev == FlyState.RETURN) {
            setState(State.RECALL_TOUCHDOWN);
            forcedReturn = false;
        }
    }

    @Override
    public void executePlan(int aiLevel, AttackerBrainInfo info, CombatInstantContext combatCtx) {
        final LivingEntity user = getUser();

        if (user == null) return;

        if (user instanceof Mob mob) {
            final var target = mob.getTarget();

            if (target != null)
                attackOrderMove.setCurrentTarget(target);
        }

        if (user.distanceToSqr(this) > 64 * 64)
            info.setDesiredStandOffTime(random.nextInt(40));
        else
            info.setDesiredStandOffTime(0);
    }

    @Override
    public void setMove(AbstractMove<?, ? super AerosmithEntity> move, @Nullable AerosmithEntity.State animState) {
        super.setMove(move, animState);

        // A "continuation" is when the same move is re-initiated immediately after cancelling itself
        // (e.g. MuzzleHitscanAttack looping while the button is held). In that case we skip the
        // maneuver sound so it only plays on genuine new starts.
        boolean isContinuation = (move == preCancelMove);
        preCancelMove = null;

        if (!isContinuation && !(move instanceof BreathXrayMove)) {
            if (isRemote()) playBoundSound(JSoundRegistry.AS_MANEUVER.get(), 1f, 1f);

            if (getUser() != null) {
                if (move instanceof MuzzleHitscanAttack)
                    volaHandle = BoundSoundPlayer.playSoundFrom(getUser(), JSoundRegistry.AS_VOLA.get(),
                            getUser().getSoundSource(), 1f, 1f);
                else if (isRemote()) BoundSoundPlayer.playSoundFrom(getUser(), JSoundRegistry.AS_SHOUT.get(),
                        getUser().getSoundSource(), 1f, 1f);
            }
        }
    }

    @Override
    public void queueMove(@Nullable MoveInputType type) {
        if (type != MoveInputType.LIGHT) super.queueMove(type);
    }

    public void patrol(@NonNull final Vec3 targetPos, final float radius) {
        setFlyState(FlyState.PATROL);
        flyTarget = targetPos;
        patrolRadius = radius;
    }

    @Override
    public void desummon(final boolean playSound) {
        final LivingEntity user = getUser();

        if (user != null) {
            forcedReturn = false;
            bombDropAttack.clearDropLocation();
            itemDropAttack.clearDropLocation();
            attackOrderMove.clearCurrentTarget();

            if (isRemote()) {
                if (flyState != FlyState.RETURN) {
                    setFlyState(FlyState.RETURN);
                    playBoundSound(JSoundRegistry.AS_MANEUVER.get(), 1f, 1f);

                    if (hasUser()) BoundSoundPlayer.playSoundFrom(getUser(), JSoundRegistry.AS_SHOUT.get(),
                            getUser().getSoundSource(), 1f, 1f);
                }

                if (distanceToSqr(user) >= 4.0) return;
            }
        }

        super.desummon(playSound);
    }

    public void stopVolaSound() {
        if (volaHandle != null) volaHandle.stop();
    }

    public void lookAt(final Vec3 target, final float maxYRotIncrease, final float maxXRotIncrease) {
        double d = target.x - getX();
        double e = target.z - getZ();
        double f = target.y - getY();

        double g = Math.sqrt(d * d + e * e);
        float h = (float)(Mth.atan2(e, d) * 180.0 / (float)Math.PI) - 90.0F;
        float i = (float)(-(Mth.atan2(f, g) * 180.0 / (float)Math.PI));
        setXRot(rotlerp(getXRot(), i, maxXRotIncrease));
        setYRot(rotlerp(getYRot(), h, maxYRotIncrease));
    }

    private float rotlerp(final float angle, final float targetAngle, final float maxIncrease) {
        float f = Mth.wrapDegrees(targetAngle - angle);
        if (f > maxIncrease) {
            f = maxIncrease;
        }

        if (f < -maxIncrease) {
            f = -maxIncrease;
        }

        return angle + f;
    }

    @Override
    protected void pushEntities() {
        if (getState() == State.CHARGE) // slice through enemies while charging
            return;
        super.pushEntities();
    }

    @Override
    public void setUser(@Nullable final LivingEntity user) {
        super.setUser(user);
        if (user == null) {
            return;
        }
        miscComponent = JComponentPlatformUtils.getMiscData(getUser());
        if (miscComponent == null) {
            return;
        }
        setOverheat(miscComponent.getAerosmithOverheat());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        float startValue = miscComponent == null ? 0f : miscComponent.getAerosmithOverheat();
        entityData.define(OVERHEAT, startValue);
        entityData.define(ALLOW_MOVE_HANDLING, false);
    }

    public float getOverheat() {
        return entityData.get(OVERHEAT);
    }

    public void setOverheat(final float overheat) {
        entityData.set(OVERHEAT, overheat);
        if (miscComponent == null) {
            return;
        }
        miscComponent.setAerosmithOverheat(overheat);
    }

    public void addOverheat(float amount) {
        setOverheat(Mth.clamp(entityData.get(OVERHEAT) + amount, 0f, OVERHEAT_MAX));
    }

    @Override
    public void setRemote(final boolean r) {
        super.setRemote(r);
        setAlphaOverride(r ? 1f : -1f);
    }

    @Override
    public void desummon() {
        if (!level().isClientSide() && !getHeldItem().isEmpty()) {
            Containers.dropItemStack(level(), getX(), getY(), getZ(), getHeldItem());
        }
        super.desummon();
    }

    @Override
    public @NotNull Vec3 getLookAngle() {
        return calculateViewVector(getXRot(), getYRot());
    }

    public enum State implements StandAnimationState<AerosmithEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "idle", AzPlayBehaviors.LOOP)),
        ACTIVE(AzCommand.create(JCraft.BASE_CONTROLLER, "idle", AzPlayBehaviors.LOOP)),
        CHARGE(AzCommand.create(JCraft.BASE_CONTROLLER, "charge", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT(AzCommand.create(JCraft.BASE_CONTROLLER, "burst", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "block", AzPlayBehaviors.LOOP)),
        SAWBLADE(AzCommand.create(JCraft.BASE_CONTROLLER, "sawblade", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BOMB(AzCommand.create(JCraft.BASE_CONTROLLER, "bomb", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        RECALL(AzCommand.controllerBuilder()
                .playSequence(JCraft.BASE_CONTROLLER, sb -> sb
                        .queue("recall landing pattern", p -> p.withPlayBehavior(AzPlayBehaviors.PLAY_ONCE))
                        .queue("recall aproach idle", p -> p.withPlayBehavior(AzPlayBehaviors.LOOP)))
                .build()),
        RECALL_TOUCHDOWN(AzCommand.controllerBuilder()
                .playSequence(JCraft.BASE_CONTROLLER, sb -> sb
                        .queue("recall touchdown", p -> p.withPlayBehavior(AzPlayBehaviors.PLAY_ONCE))
                        .queue("idle", p -> p.withPlayBehavior(AzPlayBehaviors.LOOP)))
                .build());

        private final AzCommand animator;

        State(final @NonNull AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(final @NonNull AerosmithEntity attacker) {
            animator.sendForEntity(attacker);
        }

        @Override
        public boolean mayLinger() {
            return this == RECALL || this == RECALL_TOUCHDOWN;
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
