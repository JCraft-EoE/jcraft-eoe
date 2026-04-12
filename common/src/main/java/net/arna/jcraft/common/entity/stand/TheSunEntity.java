package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.client.renderer.entity.stands.TheSunRenderer;
import net.arna.jcraft.common.attack.moves.shared.NoOpMove;
import net.arna.jcraft.common.attack.moves.thesun.FireMeteorAttack;
import net.arna.jcraft.common.attack.moves.thesun.FireSunBeamAttack;
import net.arna.jcraft.common.attack.moves.thesun.MeteorShowerAttack;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.Objects;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/Sun">The Sun</a>.
 * @see JStandTypeRegistry#THE_SUN
 * @see TheSunRenderer SunRenderer
 */
public final class TheSunEntity extends StandEntity<TheSunEntity, TheSunEntity.State> {
    public static final MoveSet<TheSunEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.THE_SUN,
            TheSunEntity::registerMoves, State.class);

    private static final EntityDataAccessor<Boolean> PASSIVE = SynchedEntityData.defineId(TheSunEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(TheSunEntity.class, EntityDataSerializers.FLOAT);
    // Only used for rendering. Ensures the scale doesn't change halfway during a tick cuz of syncing.
    public float prevScale = MIN_SCALE, curScale = MIN_SCALE;
    public static final float MAX_SCALE = 3.0F, MIN_SCALE = 1.0F;
    public static final double MAX_DISTANCE = 64.0, AIMING_DISTANCE = 128.0;
    private int overextensionTime = 0;
    private Vec3 desiredPosition;

    public static final StandData DATA = StandData.builder()
            .idleRotation(0f)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.the_sun"))
                    .proCount(2)
                    .conCount(2)
                    .freeSpace(Component.literal("Cannot buffer moves.\n Must stay within " + MAX_DISTANCE +
                            " blocks of the user, otherwise it loses size and disappears.\n" +
                            "Grace period of 1 second before heat field activates after summoning.\n" +
                            "Heat field applies Nausea > Weakness > Slowness > Burning as entities get closer."))
                    .skinName(Component.literal(":D"))
                    .skinName(Component.literal("Neutron Star"))
                    .skinName(Component.literal("Dark"))
                    .build())
            .summonData(SummonData.builder()
                    .sound(JSoundRegistry.SUN_SUMMON)
                    .animDuration(40)
                    .build())
            .build();

    private static final FireSunBeamAttack FIRE_SUNBEAM = new FireSunBeamAttack(20, 5, 10, 1, 0)
            .withInfo(
                    Component.nullToEmpty("Fire Sunbeam"),
                    Component.nullToEmpty("""
                            Fires a sunbeam with perfect precision.""")
            );

    private static final FireMeteorAttack FIRE_METEOR = new FireMeteorAttack(20, 10, 1, 1,
            2.5f, 0f, true, IntSet.of(5))
            .withCrouchingVariant(FIRE_SUNBEAM)
            .withInfo(
                    Component.nullToEmpty("Fire Meteor"),
                    Component.nullToEmpty("""
                            Fires a high-velocity meteor with perfect precision.
                            At max size, the meteor is explosive.""")
            );

    private static final FireMeteorAttack STARBURST = new FireMeteorAttack(40, 24, 3, 1.75f,
            2.5f, 10f, false, IntSet.of(8, 16, 24))
            .withInfo(
                    Component.nullToEmpty("Starburst"),
                    Component.nullToEmpty("""
                            Fires 3 bursts of meteors with high spread.
                            Amount of meteors changes proportional to the size of The Sun.""")
            );

    private static final MeteorShowerAttack METEOR_SHOWER = new MeteorShowerAttack(120, 10, 110, 2)
            .withSound(JSoundRegistry.SUN_SHOWER)
            .withoutSlowness()
            .withInfo(
                    Component.nullToEmpty("Meteor Shower"),
                    Component.nullToEmpty("""
                            Fires a hail of meteors in all directions for 5 seconds.
                            Amount of meteors changes proportional to the size of The Sun.""")
            );

    private static final FireSunBeamAttack INCINERATING_SUNSHINE = new FireSunBeamAttack(180, 8, 24, 3, 2.5f)
            .withInfo(
                    Component.nullToEmpty("Incinerating Sunshine"),
                    Component.nullToEmpty("Fires 3 sunbeams.")
            );

    private static final NoOpMove<TheSunEntity> CHANGE_SIZE = new NoOpMove<TheSunEntity>(0, 0, 0)
            .withInfo(
                    Component.nullToEmpty("Change Size"),
                    Component.nullToEmpty("""
                            Use while standing to expand size.
                            Crouch to shrink.
                            Size decreases movement speed and increases heat field.""")
            );

    private static final NoOpMove<TheSunEntity> MOVE = new NoOpMove<TheSunEntity>(0, 0, 0)
            .withHoldable()
            .withInfo(
                    Component.nullToEmpty("Move"),
                    Component.nullToEmpty("""
                            Moves The Sun to the looked location.""")
            );

    private static final NoOpMove<TheSunEntity> TOGGLE_PASSIVE = new NoOpMove<TheSunEntity>(0, 0, 0)
            .withCrouchingVariant(MOVE)
            .withInfo(
                    Component.nullToEmpty("Toggle Passive"),
                    Component.nullToEmpty("""
                            Toggles The Sun between an Active and Passive mode.
                            Active mode - the one it's in when summoned, allows usage of stand moves.
                            Passive mode - allows usage of spec moves while keeping the Sun summoned.""")
            );

    public Vec3 randomPos() {
        return randomPos(getRawScale());
    }

    private Vec3 randomPos(double scale) {
        return new Vec3(
                getX() + random.nextGaussian() * scale,
                getY() + random.nextGaussian() * scale,
                getZ() + random.nextGaussian() * scale
        );
    }

    public TheSunEntity(Level worldIn) {
        super(JStandTypeRegistry.THE_SUN.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(1.0f, 0.8f, 0.4f),
                new Vector3f(1.0f, 1.0f, 0.0f),
                new Vector3f(0.4f, 0.8f, 1.0f),
                new Vector3f(0.6f, 0.1f, 0.8f)
        };

        flyDist = 0.5f;

        setNoGravity(true);

        setAlphaOverride(1.0f);
    }

    @Override
    public boolean canHoldMove(@Nullable MoveInputType type) {
        return type == MoveInputType.ULTIMATE;
    }

    private static void registerMoves(MoveMap<TheSunEntity, State> moves) {
        moves.register(MoveClass.HEAVY, FIRE_METEOR, null).withCrouchingVariant(null);

        moves.register(MoveClass.SPECIAL1, STARBURST, null);
        moves.register(MoveClass.SPECIAL2, METEOR_SHOWER, null);
        moves.register(MoveClass.SPECIAL3, INCINERATING_SUNSHINE, null);

        moves.register(MoveClass.ULTIMATE, CHANGE_SIZE, null);

        moves.register(MoveClass.UTILITY, TOGGLE_PASSIVE, null).withCrouchingVariant(null);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(PASSIVE, false);
        entityData.define(SCALE, MIN_SCALE);
    }

    @Override
    public boolean handleMove(MoveClass moveClass) {
        LivingEntity user = getUserOrThrow();
        boolean sneaking = user.isShiftKeyDown();

        switch (moveClass) {
            case ULTIMATE -> {
                boolean shrink = user.isShiftKeyDown();
                float newScale = getRawScale() + (shrink ? -0.05f : 0.05f);

                if (!shrink && newScale <= MAX_SCALE) {
                    // Distributes world collision check to minimize lag
                    int roundScale = Math.round(newScale * 1.2f);

                    AABB newBox = newBoundingBox(getX() + 1, getY() + 1, getZ() + 1, newScale * 2.0f);
                    BlockPos start = BlockPos.containing(newBox.minX, newBox.minY, newBox.minZ);
                    BlockPos end = BlockPos.containing(newBox.maxX, newBox.maxY, newBox.maxZ);

                    // Detect if world prevents resize
                    for (int x = start.getX(); x < end.getX(); x += roundScale) {
                        for (int y = start.getY(); y < end.getY(); y += roundScale) {
                            for (int z = start.getZ(); z < end.getZ(); z += roundScale) {
                                BlockPos blockPos = new BlockPos(x, y, z);
                                //JCraft.createParticle((ServerWorld) world, x, y, z, JParticleType.BACK_STAB);
                                if (level().loadedAndEntityCanStandOn(blockPos, this)) {
                                    //JCraft.createParticle((ServerWorld) world, x, y, z, JParticleType.HIT_SPARK_3);
                                    return false;
                                }
                            }
                        }
                    }
                }

                entityData.set(SCALE, Mth.clamp(newScale, MIN_SCALE, MAX_SCALE));
            }
            case UTILITY -> {
                if (sneaking) {
                    Vec3 eP = user.getEyePosition();
                    Vec3 rangeMod = user.getLookAngle().scale(MAX_DISTANCE);
                    desiredPosition = level().clip(new ClipContext(eP, eP.add(rangeMod),
                            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user)).getLocation();
                } else {
                    togglePassive();
                }
            }
            default -> {
                return super.handleMove(moveClass);
            }
        }
        return true;
    }

    public Vec3 acquireTargetPosition() {
        final LivingEntity user = getUser();
        if (user == null) {
            return null;
        }

        final Vec3 eP = user.getEyePosition();
        final Vec3 rangeMod = user.getLookAngle().scale(AIMING_DISTANCE);
        final EntityHitResult eHit = ProjectileUtil.getEntityHitResult(user, eP, eP.add(rangeMod),
                user.getBoundingBox().inflate(AIMING_DISTANCE),
                EntitySelector.NO_CREATIVE_OR_SPECTATOR,
                AIMING_DISTANCE * AIMING_DISTANCE
        );

        Vec3 targetPosition = Objects.requireNonNullElseGet(eHit, () -> user.level().clip(new ClipContext(eP, eP.add(rangeMod),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user))).getLocation();

        if (user instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundLevelParticlesPacket(JParticleTypeRegistry.SUN_LOCK_ON.get(), true,
                    targetPosition.x, targetPosition.y, targetPosition.z, 0, 0, 0, 0, 1));
        }

        return targetPosition;
    }

    @Override
    public void queueMove(MoveInputType type) {
    }

    private void togglePassive() {
        boolean newPassive = !entityData.get(PASSIVE);
        entityData.set(PASSIVE, newPassive);
        getUserOrThrow().sendSystemMessage(Component.nullToEmpty(newPassive ? "PASSIVE" : "ACTIVE"));
    }

    public boolean isPassive() {
        return entityData.get(PASSIVE);
    }

    @Override
    public boolean allowMoveHandling() {
        return !isPassive();
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypes.IN_WALL)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean remoteControllable() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, @NonNull DamageSource damageSource) {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void tryBlock() {
    }

    /**
     * Modified from {@link Entity#push(Entity)}.
     *
     * @param entity The entity to push away.
     */
    @Override
    public void push(@NotNull Entity entity) {
        if (isPassengerOfSameVehicle(entity) || entity.noPhysics || this.noPhysics || !entity.isPushable()) return;

        double d = entity.getX() - getX();
        double e = entity.getZ() - getZ();
        double f = Mth.absMax(d, e);
        if (f < 0.001) return;

        f = Math.sqrt(f);
        d /= f;
        e /= f;
        double g = 1.0 / f;
        if (g > 1.0) {
            g = 1.0;
        }

        d *= g;
        e *= g;
        d *= 0.1;
        e *= 0.1;

        entity.push(d, 0.0, e);
    }

    public boolean canCollideWith(@NotNull Entity other) {
        return other.canBeCollidedWith() && !this.isPassengerOfSameVehicle(other);
    }

    @Override
    public double getEngagementDistance() {
        return 128.0;
    }

    @Override
    public void tick() {
        super.tick();

        final LivingEntity user = getUser();
        if (user == null) {
            return;
        }

        final float scale = getRawScale();
        final float heatFieldSize = scale * 20.0F;

        // if (tickCount % 20 == 0) JCraft.prefixedLog(level().isClientSide, "TheSunEntity@" + getId() + " scale: " + scale);

        if (level().isClientSide()) {
            Vec3 pos = randomPos();
            Vec3 vel = JUtils.randUnitVec(random).scale(0.2 * scale).add(getDeltaMovement());
            for (int i = 0; i < (int) (heatFieldSize); i++) {
                level().addParticle(getSkin() == 2 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
                        false, pos.x, pos.y, pos.z,
                        vel.x, vel.y, vel.z
                );
            }

        } else {
            flyDist = 0.5F / scale;

            Vec3 pos = position();
            Vec3 userPos = user.position();

            if (!isRemote()) {
                setRemote(true);
            }

            // Fly away when summoned
            if (desiredPosition == null) {
                Direction gravity = GravityChangerAPI.getGravityDirection(user);
                int desiredHeight = 32;
                desiredPosition = userPos.add(Vec3.atLowerCornerOf(gravity.getNormal().multiply(-desiredHeight)));
            } else {
                if (user instanceof Mob) {
                    // Periodically increase size
                    // Not a real combat strategy, but it does show off the possibility and change his damage output
                    if (Mth.sin(tickCount / 100.0f) > 1.0f) initMove(MoveClass.ULTIMATE);

                    // Stay as close as possible to user without harming them
                    Direction gravity = GravityChangerAPI.getGravityDirection(user);
                    desiredPosition = userPos.add(Vec3.atLowerCornerOf(gravity.getNormal().multiply((int) -heatFieldSize)));
                }

                // Prioritize getting closer
                double distance = pos.distanceToSqr(userPos);
                if (distance > MAX_DISTANCE * MAX_DISTANCE) {
                    desiredPosition = desiredPosition.add(userPos.subtract(pos).normalize());
                    if (++overextensionTime > 20) {
                        entityData.set(SCALE, Mth.clamp(getRawScale() - 0.1f, MIN_SCALE, MAX_SCALE));
                    }
                } else {
                    overextensionTime = 0;
                }

                // Go where directed
                if (desiredPosition.distanceToSqr(pos) > (scale * scale * 3)) {
                    Vec3 towards = desiredPosition.subtract(pos).normalize().scale(flyDist);
                    setDeltaMovement(towards);
                } else {
                    setDeltaMovement(getDeltaMovement().scale(0.5f));
                }
            }

            if (tickCount > 20) {
                if (tickCount % 40 == 0 && random.nextDouble() >= 0.5) {
                    playSound(JSoundRegistry.SUN_IDLE.get(), 1f, random.nextFloat());
                }

                if (heatFieldSize > 0) {
                    Collection<Entity> entities = level().getEntities(this, getBoundingBox().inflate(heatFieldSize), EntitySelector.ENTITY_STILL_ALIVE.and(this::hasLineOfSight));
                    for (Entity entity : entities) {
                        double distance = entity.distanceToSqr(this);
                        double exposure = 125.0 * scale;
                        if (distance == 0) {
                            exposure *= 10;
                        } else {
                            exposure *= 1 / distance;
                        }

                        if (exposure > 2) {
                            if (exposure > 8) {
                                entity.hurt(JDamageSources.create(level(), DamageTypes.ON_FIRE), 1.5f);
                            }
                            entity.setSecondsOnFire(2);
                        }

                        if (entity instanceof LivingEntity living && living.isAlive()) {
                            if (exposure > 0.25) {
                                living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 10, 0, true, false));
                                if (exposure > 0.5) {
                                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 10, 0, true, false));
                                    if (exposure > 1) {
                                        living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 0, true, false));
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

        prevScale = curScale;
        curScale = getRawScale();
    }

    public static void dryOut(ServerLevel serverWorld, BlockPos pos) {
        BlockState blockState;
        blockState = serverWorld.getBlockState(pos);
        if (blockState.getBlock() instanceof BucketPickup fluidDrainable) {
            fluidDrainable.pickupBlock(serverWorld, pos, blockState);
            //if (!itemStack2.isEmpty()) world.emitGameEvent();
        }
    }

    @Override
    protected @NonNull AABB makeBoundingBox() {
        final double x = getX(), y = getY(), z = getZ();
        final float scale = getRawScale() * 1.5f;
        return newBoundingBox(x, y, z, scale);
    }

    private static AABB newBoundingBox(double x, double y, double z, float scale) {
        return new AABB(
                x - scale,
                y - scale,
                z - scale,
                x + scale,
                y + scale,
                z + scale
        );
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    @NonNull
    public TheSunEntity getThis() {
        return this;
    }

    public float getRawScale() {
        return entityData.get(SCALE);
    }

    //@Override
    //public float getScale() {
    //    return entityData.get(SCALE);
    //}

    public float getScale(float tickDelta) {
        return Mth.lerp(tickDelta, prevScale, curScale);
    }

    // Animation code
    public enum State implements StandAnimationState<TheSunEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.sun.idle", AzPlayBehaviors.LOOP)),
        ;

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(TheSunEntity attacker) {
            animator.sendForEntity(attacker);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    public State getBlockState() {
        return null;
    }
}
