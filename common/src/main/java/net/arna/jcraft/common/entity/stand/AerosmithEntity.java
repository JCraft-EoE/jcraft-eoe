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
import net.arna.jcraft.common.attack.moves.aerosmith.*;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AerosmithEntity extends StandEntity<AerosmithEntity, AerosmithEntity.State> {
    public static final MoveSet<AerosmithEntity, AerosmithEntity.State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.AEROSMITH,
            AerosmithEntity::registerDefaultMoves, AerosmithEntity.State.class);

    public static final EntityDataAccessor<Float> OVERHEAT = SynchedEntityData.defineId(AerosmithEntity.class, EntityDataSerializers.FLOAT);
    public static final float OVERHEAT_MAX = 15f;

    public static final double SLOW_CRUISE_SPEED = 0.075;
    public static final double CRUISE_SPEED = 0.15;

    public enum FlyState {
        NONE,
        FLYBY,
        PATROL,
        RETURN,
    }

    @Getter @Setter
    private float patrolRadius = 48.0f;

    @NonNull @Getter @Setter
    private Vec3 flyTarget = Vec3.ZERO;

    @Getter @Setter
    private FlyState flyState = FlyState.NONE;

    // client-side rotation tracking for rendering
    public float oldPitch = 0.0f, oldYaw = 0.0f, oldRoll = 0.0f;
    public float pitch = 0.0f, yaw = 0.0f, roll = 0.0f;

    // TODO: balance this
    public static final MuzzleHitscanAttack BULLET = new MuzzleHitscanAttack(
            1, 1, 2, 0f, 1f, 0, 0f, 30f, 10f, 1/6f, 0.01f)
            .withSound(JSoundRegistry.AS_SHOOT)
            .withHitSpark(JParticleType.HIT_SPARK_1)
            .withShootSpark(JParticleType.LEMON)
            .withStunType(StunType.WINDED)
            .withInfo(
                    Component.literal("Fire Autocannons"),
                    Component.literal("Shoots supersonic bullets in front of Aerosmith. Prolonged use overheats the guns, making them far less accurate.")
            );

    public static final ItemDropAttack ITEM_DROP = new ItemDropAttack(200, 30f)
            .withInfo(
                    Component.literal("Item Drop"),
                    Component.literal("Orders Aerosmith to drop an item above a given location.")
            );

    public static final BombDropAttack BOMB_DROP = new BombDropAttack(200, 30f)
            .withCrouchingVariant(ITEM_DROP)
            .withInfo(
                    Component.literal("Bomb Drop"),
                    Component.literal("Orders Aerosmith to drop a bomb above a given location.")
            );

    public static final PatrolMove PATROL = new PatrolMove(0, 32f, 48f)
            .withInfo(
                    Component.literal("Patrol Location"),
                    Component.literal("Orders Aerosmith to fly around a given location.")
            );

    public static final AerosmithChargeAttack CHARGE = new AerosmithChargeAttack(
            300, 50, 1.0f, 15, 1.66f, 0.1f, 0.0f,
            IntSet.of(10, 15, 20, 25, 30, 35, 40, 45, 50))
            .withStaticY()
            .withStunType(StunType.LAUNCH)
            .withSound(JSoundRegistry.AS_SUMMON)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Component.literal("Dive Charge"),
                    Component.literal("Non-remote: a straight charge, rising at the end. Carries enemies with Aerosmith.")
            );

    public static final StandData DATA = StandData.builder()
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.aerosmith"))
                    .skinName(Component.literal("Manga"))
                    .skinName(Component.literal("Vento Aureo"))
                    .skinName(Component.literal("Interceptor"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.AS_SUMMON).withAnimDuration(48))
            .build();

    private CommonMiscComponent miscComponent;
    private int overheatTick;

    public AerosmithEntity(final Level world) {
        super(JStandTypeRegistry.AEROSMITH.get(), world);
        // setYDistanceOffset(10f); // TODO for patrol mode
        setYDistanceOffset(1.2f);
        setNoGravity(true);
    }

    @NonNull
    public ItemStack getHeldItem() { return getItemBySlot(EquipmentSlot.FEET); }
    public void setHeldItem(final @NonNull ItemStack stack) { setItemSlot(EquipmentSlot.FEET, stack); }

    @Override
    public boolean remoteControllable() {
        return false;
    }

    @Override
    public boolean allowMoveHandling() {
        return getCurrentMove() == null &&
                getMoveStun() < JCraft.QUEUE_MOVESTUN_LIMIT &&
                getMove(BombDropAttack.class).getDropLocation() == null;
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
    }

    private float xRotPersist = 0.0f;

    @Override
    public void tick() {
        setXRot(xRotPersist);

        super.tick();

        resetFallDistance();

        final LivingEntity user = getUser();
        if (user == null) return;

        final AbstractMove<?, ? super AerosmithEntity> currentMove = getCurrentMove();

        if (!(currentMove instanceof MuzzleHitscanAttack) && ++overheatTick % 5 == 0) {
            addOverheat(-0.4f);
        }

        if (currentMove == null) {
            switch (flyState) {
                case PATROL -> {
                    final Vec3 offset = RotationUtil.vecPlayerToWorld(
                            Mth.sin(tickCount / 100.0f) * patrolRadius, 1.0, Mth.cos(tickCount / 100.0f) * patrolRadius,
                            GravityChangerAPI.getGravityDirection(this)
                    );

                    JCraft.createParticle((ServerLevel) level(), flyTarget.x + offset.x, flyTarget.y + offset.y, flyTarget.z + offset.z, JParticleType.GO);

                    lookAt(flyTarget.add(offset), 6f, 6f);

                    setDeltaMovement(getDeltaMovement().scale(0.9).add(getLookAngle().scale(SLOW_CRUISE_SPEED)));
                }
                case FLYBY -> {
                    lookAt(flyTarget, 6f, 6f);

                    setDeltaMovement(getDeltaMovement().scale(0.9).add(getLookAngle().scale(CRUISE_SPEED)));

                    if (distanceToSqr(flyTarget) <= 4.0) {
                        flyState = FlyState.RETURN;
                    }
                }
                case RETURN -> {
                    final Vec3 targetPos = user.position();

                    lookAt(targetPos, 6f, 12f);

                    final double distanceSqr = distanceToSqr(targetPos);

                    final double cruiseSpeed = distanceSqr <= 49.0 ? SLOW_CRUISE_SPEED : CRUISE_SPEED;

                    setDeltaMovement(getDeltaMovement().scale(0.9).add(getLookAngle().scale(cruiseSpeed)));

                    if (distanceSqr <= 4.0) {
                        setRemote(false);
                    }
                }
            }
        }

        xRotPersist = getXRot();
    }

    @Override
    public void desummon(final boolean playSound) {
        final LivingEntity user = getUser();

        if (user != null) {
            if (isRemote()) {
                if (flyState == FlyState.RETURN) {
                    flyTarget = position();
                    flyState = FlyState.PATROL;
                } else {
                    getMove(BombDropAttack.class).clearDropLocation();
                    flyState = FlyState.RETURN;
                }
            }

            if (distanceToSqr(user) >= 4.0) return;
        }

        super.desummon(playSound);
    }

    private void lookAt(final Vec3 target, final float maxYRotIncrease, final float maxXRotIncrease) {
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
