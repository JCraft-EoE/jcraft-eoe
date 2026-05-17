package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.component.living.CommonMiscComponent;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.common.ai.AttackerBrainInfo;
import net.arna.jcraft.common.ai.CombatInstantContext;
import net.arna.jcraft.common.attack.moves.aerosmith.*;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class AerosmithEntity extends StandEntity<AerosmithEntity, AerosmithEntity.State> {
    public static final MoveSet<AerosmithEntity, AerosmithEntity.State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.AEROSMITH,
            AerosmithEntity::registerDefaultMoves, AerosmithEntity.State.class);

    public static final EntityDataAccessor<Float> OVERHEAT = SynchedEntityData.defineId(AerosmithEntity.class, EntityDataSerializers.FLOAT);
    public static final float OVERHEAT_MAX = 15f;
    public static final int OVERHEAT_LOSS_COOLDOWN_MAX = 20;
    public static final double SLOW_CRUISE_SPEED = 0.075;
    public static final double CRUISE_SPEED = 0.15;
    public static final double MAX_SPEED = 0.22;
    public static final float DEFAULT_PATROL_RADIUS = 48.0f;

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

    @Getter @Setter
    private FlyState flyState = FlyState.NONE;

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

    public static final FlybyMove FLYBY = new FlybyMove(0, 67f)
            .withInfo(
                    Component.literal("Fly by Location"),
                    Component.literal("Orders Aerosmith to fly to a given location, returning after arriving.")
            );

    public static final PatrolMove PATROL = new PatrolMove(0, 67f, 48f)
            .withInfo(
                    Component.literal("Patrol Location"),
                    Component.literal("Orders Aerosmith to fly around a given location. Use while crouching to invert the patrol direction.")
            );

    public static final AerosmithChargeAttack CHARGE = new AerosmithChargeAttack(
            100, 50, 1.0f, 15, 1.66f, 0.1f, 0.0f,
            IntSet.of(10, 15, 20, 25, 30, 35, 40, 45, 50))
            .withStaticY()
            .withStunType(StunType.LAUNCH)
            .withSound(JSoundRegistry.AS_BARRAGE)
            .withImpactSound(JSoundRegistry.AS_BARRAGE_HIT)
            .withInfo(
                    Component.literal("Dive Charge"),
                    Component.literal("Non-remote: a straight charge, rising at the end. Carries enemies with Aerosmith.")
            );

    public static final AerosmithAttackOrderMove ATTACK_ORDER_MOVE = new AerosmithAttackOrderMove(0, 0)
            .withInfo(
                    Component.literal("Attack Order"),
                    Component.literal("Orders Aerosmith to attack the entity at a detected location.")
            );

    public static final BreathXrayMove<AerosmithEntity> XRAY = new BreathXrayMove<AerosmithEntity>(0, 0, 128, true)
            .withInfo(
                    Component.literal("Breath Detection"),
                    Component.literal("Aerosmith scans the surroundings for the breath of living things.")
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
    };
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
        return getCurrentMove() == shootAttack ||
                (
                getCurrentMove() == null &&
                getMoveStun() < JCraft.QUEUE_MOVESTUN_LIMIT &&
                bombDropAttack.getDropLocation() == null &&
                itemDropAttack.getDropLocation() == null &&
                attackOrderMove.getCurrentTarget() == null
                );
    }

    @Override
    public @NonNull AerosmithEntity getThis() {
        return this;
    }

    private static void registerDefaultMoves(final @NonNull MoveMap<AerosmithEntity, AerosmithEntity.State> moves) {
        moves.registerImmediate(MoveClass.LIGHT, BULLET, State.LIGHT);
        moves.registerImmediate(MoveClass.HEAVY, BOMB_DROP, State.ACTIVE);
        moves.registerImmediate(MoveClass.UTILITY, PATROL, State.ACTIVE);
        moves.registerImmediate(MoveClass.BARRAGE, CHARGE, State.CHARGE);
        moves.register(MoveClass.ULTIMATE, ATTACK_ORDER_MOVE);
        moves.register(MoveClass.SPECIAL1, FLYBY);
        moves.register(MoveClass.SPECIAL3, XRAY);
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
    public boolean canBlock() {
        if (isRemote()) return false;
        return super.canBlock();
    }

    @Override
    public void cancelMove(boolean offensiveCancel) {
        if (offensiveCancel && isRemote()) return;
        super.cancelMove(offensiveCancel);
    }

    public double lastMovementToLocalPlayerAngle = 0.0;

    @Override
    public void tick() {
        xRotChangeAllowed = false;
        noPhysics = true;

        if (isRemote()) wantToBlock = false;
        super.tick();

        noPhysics = false;
        xRotChangeAllowed = true;

        resetFallDistance();

        if (!isAlive()) return;

        if (level().isClientSide()) {
            JCraft.getClientEntityHandler().aerosmithClientTick(this);
            return;
        }

        final LivingEntity user = getUser();

        if (user == null) return;

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
            switch (flyState) {
                case PATROL -> {
                    final float theta = tickCount / patrolRadius / 2.0f * patrolDirection.getValue();
                    final Vec3 offset = RotationUtil.vecPlayerToWorld(
                            Mth.sin(theta) * patrolRadius, 1.0, Mth.cos(theta) * patrolRadius,
                            GravityChangerAPI.getGravityDirection(this)
                    );

                    JCraft.createParticle((ServerLevel) level(), flyTarget.x + offset.x, flyTarget.y + offset.y, flyTarget.z + offset.z, JParticleType.GO);

                    lookAt(flyTarget.add(offset), 6f, 6f);

                    setDeltaMovement(getDeltaMovement().scale(0.9).add(getLookAngle().scale(SLOW_CRUISE_SPEED)));
                }
                case FLYBY -> {
                    lookAt(flyTarget, 6f, 6f);

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

                    setDeltaMovement(getDeltaMovement().scale(0.9).add(getLookAngle().scale(cruiseSpeed)));

                    if (distanceToSqr(flyTarget) <= 4.0
                            && bombDropAttack.getDropLocation() == null
                            && itemDropAttack.getDropLocation() == null) {
                        flyState = hasAttackTarget ? FlyState.PATROL : FlyState.RETURN;
                    }
                }
                case RETURN -> {
                    final Vec3 targetPos = user.position();

                    lookAt(targetPos, 6f, 12f);

                    final double distanceSqr = distanceToSqr(targetPos);

                    final double cruiseSpeed = distanceSqr <= 49.0 ? SLOW_CRUISE_SPEED : CRUISE_SPEED;

                    setDeltaMovement(getDeltaMovement().scale(0.9).add(getLookAngle().scale(cruiseSpeed)));

                    if (distanceSqr <= 6.25) {
                        setRemote(false);
                        flyState = FlyState.NONE;
                        playSound(JSoundRegistry.AS_LANDING.get());
                    }
                }
            }
        }

        xRotChangeAllowed = false;
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

    public void patrol(final Vec3 targetPos, final float radius) {
        flyState = FlyState.PATROL;
        flyTarget = targetPos;
        patrolRadius = radius;
    }

    @Override
    public void desummon(final boolean playSound) {
        final LivingEntity user = getUser();

        if (user != null) {
            bombDropAttack.clearDropLocation();
            itemDropAttack.clearDropLocation();
            attackOrderMove.clearCurrentTarget();

            if (isRemote()) {
                if (flyState != FlyState.RETURN) {
                    flyState = FlyState.RETURN;
                }

                if (distanceToSqr(user) >= 4.0) return;
            }
        }

        super.desummon(playSound);
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
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "block", AzPlayBehaviors.LOOP))
        ;

        private final AzCommand animator;

        State(final @NonNull AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(final @NonNull AerosmithEntity attacker) {
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
