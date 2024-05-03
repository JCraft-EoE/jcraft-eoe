package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.MoveInputType;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.shared.BarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.NoOpMove;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleMultiHitAttack;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.projectile.MeteorProjectile;
import net.arna.jcraft.common.entity.projectile.SunBeamProjectile;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JParticleTypeRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class TheSunEntity extends StandEntity<TheSunEntity, TheSunEntity.State> {
    private static final TrackedData<Boolean> PASSIVE;
    private static final TrackedData<Float> SCALE;
    public float prevScale = MIN_SCALE;
    public static final float MAX_SCALE = 3.0F, MIN_SCALE = 1.0F;
    public static final double MAX_DISTANCE = 64.0, AIMING_DISTANCE = 128.0;
    private int overextensionTime = 0;
    private Vec3d desiredPosition, targetPosition;

    private static final SimpleAttack<TheSunEntity> FIRE_SUNBEAM = new SimpleAttack<TheSunEntity>(20, 5, 10, 0, 0, 0, 0, 0, 0)
            .withInitAction((attacker, user, ctx) -> attacker.setTargetPosition(user))
            .withAction((attacker, user, ctx, targets) -> fireSunBeam(attacker, user, 0.0f))
            .markRanged()
            .withInfo(
                    Text.of("Fire Sunbeam"),
                    Text.of("""
                            Fires a sunbeam with perfect precision.""")
            );

    private static final SimpleAttack<TheSunEntity> FIRE_METEOR = new SimpleAttack<TheSunEntity>(20, 5, 10, 0, 0, 0, 0, 0, 0)
            .withCrouchingVariant(FIRE_SUNBEAM)
            .withInitAction((attacker, user, ctx) -> attacker.setTargetPosition(user))
            .withAction((attacker, user, ctx, targets) -> {
                Vec3d pos = attacker.randomPos();
                MeteorProjectile meteor = fireMeteor(attacker, user, pos, getLookVector(pos, attacker.targetPosition), 2.5f, 0f);
                meteor.setNoGravity(true);

                if (attacker.getScale() == MAX_SCALE)
                    meteor.setExplosive(true);
            })
            .markRanged()
            .withInfo(
                    Text.of("Fire Meteor"),
                    Text.of("""
                            Fires a high-velocity meteor with perfect precision.
                            At max size, the meteor is explosive.""")
            );

    private static final SimpleMultiHitAttack<TheSunEntity> FIRE_METEORS_1 = new SimpleMultiHitAttack<TheSunEntity>(
            100, 24, 0, 0, 0, 0, 0, 0, IntSet.of(8, 16, 24)
    )
            .withInitAction((attacker, user, ctx) -> attacker.setTargetPosition(user))
            .withAction((attacker, user, ctx, targets) -> fireMeteors1(attacker, user))
            .markRanged()
            .withInfo(
                    Text.of("Starburst"),
                    Text.of("""
                            Fires 3 bursts of 3 meteors with high spread.""")
            );

    private static final BarrageAttack<TheSunEntity> FIRE_METEORS_2 = new BarrageAttack<TheSunEntity>(
            100, 10, 110, 0, 0, 0, 0, 0, 0, 2
    )
            .withAction((attacker, user, ctx, targets) -> {
                for (int i = 0; i < attacker.getScale(); i++)
                    fireMeteor(attacker, user, attacker.randomPos(), JUtils.randUnitVec(attacker.random), 1.25f, 0f);
            })
            .withSound(JSoundRegistry.SUN_SHOWER)
            .withoutSlowness()
            .markRanged()
            .withInfo(
                    Text.of("Meteor Shower"),
                    Text.of("""
                            Fires a hail of meteors in all directions for 5 seconds.
                            Amount of meteors changes proportional to the size of The Sun.""")
            );

    private static final SimpleMultiHitAttack<TheSunEntity> FIRE_BEAM = new SimpleMultiHitAttack<TheSunEntity>(
            200, 24, 0, 0, 0, 0, 0, 0, IntSet.of(8, 12, 16)
    )
            .withInitAction((attacker, user, ctx) -> attacker.setTargetPosition(user))
            .withAction((attacker, user, ctx, targets) -> fireSunBeam(attacker, user, 2.5f))
            .markRanged()
            .withInfo(
                    Text.of("Incinerating Sunshine"),
                    Text.of("Fires 3 sunbeams.")
            );

    private static final NoOpMove<TheSunEntity> CHANGE_SIZE = new NoOpMove<TheSunEntity>(0, 0, 0)
            .withInfo(
                    Text.of("Change Size"),
                    Text.of("""
                            Use while standing to expand size.
                            Crouch to shrink.
                            Size decreases movement speed and increases heat field.""")
            );

    private static final NoOpMove<TheSunEntity> MOVE = new NoOpMove<TheSunEntity>(0, 0, 0)
            .withHoldable()
            .withInfo(
                    Text.of("Move"),
                    Text.of("""
                            Moves The Sun to the looked location.""")
            );

    private static final NoOpMove<TheSunEntity> TOGGLE_PASSIVE = new NoOpMove<TheSunEntity>(0, 0, 0)
            .withCrouchingVariant(MOVE)
            .withInfo(
                    Text.of("Toggle Passive"),
                    Text.of("""
                            Toggles The Sun between an Active and Passive mode.
                            Active mode - the one it's in when summoned, allows usage of stand moves.
                            Passive mode - allows usage of spec moves while keeping the Sun summoned.""")
            );

    private Vec3d randomPos() {
        return randomPos(getScale());
    }

    private Vec3d randomPos(double scale) {
        return new Vec3d(
                getX() + random.nextGaussian() * scale,
                getY() + random.nextGaussian() * scale,
                getZ() + random.nextGaussian() * scale

        );
    }

    private static Vec3d getLookVector(Vec3d origin, Vec3d target) {
        double d = target.x - origin.x;
        double e = target.y - origin.y;
        double f = target.z - origin.z;
        double g = Math.sqrt(d * d + f * f);

        float yaw = MathHelper.wrapDegrees((float) (MathHelper.atan2(-f, -d) * 57.2957763671875) - 90.0F); // deg; X, Z
        float pitch = MathHelper.wrapDegrees((float) (MathHelper.atan2(-e, -g) * 57.2957763671875)); // deg; Y, len

        return new Vec3d(
                -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F),
                -MathHelper.sin((pitch) * 0.017453292F),
                MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F)
        );
    }

    private static MeteorProjectile fireMeteor(TheSunEntity attacker, @NonNull LivingEntity user, Vec3d pos, Vec3d velocity) {
        return fireMeteor(attacker, user, pos, velocity, 1.25f, 10f);
    }

    private static MeteorProjectile fireMeteor(TheSunEntity attacker, @NonNull LivingEntity user, Vec3d pos, Vec3d velocity, float speed, float divergence) {
        MeteorProjectile meteor = new MeteorProjectile(attacker.getWorld(), user);
        meteor.setSkin(attacker.getSkin());
        meteor.assignSun(attacker);
        meteor.setPosition(pos);
        meteor.setVelocity(velocity.x, velocity.y, velocity.z, speed, divergence);

        attacker.getWorld().spawnEntity(meteor);
        if (!attacker.getCurrentMove().isBarrage())
            attacker.playSound(JSoundRegistry.SUN_METEOR_FIRE, 1f, 1f);

        return meteor;
    }

    private static void fireSunBeam(TheSunEntity attacker, @NonNull LivingEntity user, float divergence) {
        Vec3d pos = attacker.randomPos();

        SunBeamProjectile sunBeam = new SunBeamProjectile(attacker.getWorld());
        sunBeam.setSkin(attacker.getSkin());
        sunBeam.setOwner(user);
        sunBeam.assignSun(attacker);
        sunBeam.setPosition(pos);

        Vec3d lookVec = getLookVector(pos, attacker.targetPosition);
        sunBeam.setVelocity(lookVec.x, lookVec.y, lookVec.z, 0.01f, divergence);

        attacker.getWorld().spawnEntity(sunBeam);
        attacker.playSound(JSoundRegistry.SUN_BEAM_RAY, 1f, 1f);
    }

    private static void fireMeteors1(TheSunEntity attacker, LivingEntity user) {
        for (int i = 0; i < 3; i++) {
            Vec3d pos = attacker.randomPos();
            fireMeteor(attacker, user, pos, getLookVector(pos, attacker.targetPosition).multiply(1.75)).setNoGravity(true);
        }
    }

    static {
        PASSIVE = DataTracker.registerData(TheSunEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        SCALE = DataTracker.registerData(TheSunEntity.class, TrackedDataHandlerRegistry.FLOAT);
    }

    public TheSunEntity(World worldIn) {
        super(StandType.THE_SUN, worldIn, JSoundRegistry.SUN_SUMMON);

        idleRotation = 0;

        pros = List.of(
                "longest ranged coverage in the game",
                "powerful area control"
        );
        cons = List.of(
                "cannot block",
                "heat field affects user"
        );

        description = "Long-range Projectile ZONER";

        freespace = "Cannot buffer moves.\n Must stay within "+ MAX_DISTANCE + " of the user, otherwise it loses size and disappears.\nGrace period of 1 second before heat field activates after summoning.\nHeat field applies Nausea > Weakness > Slowness > Burning as entities get closer.\n";

        auraColors = new Vector3f[]{
                new Vector3f(1.0f, 0.8f, 0.4f),
                new Vector3f(1.0f, 1.0f, 0.0f),
                new Vector3f(0.4f, 0.8f, 1.0f),
                new Vector3f(0.6f, 0.1f, 0.8f)
        };

        speed = 0.5f;

        summonAnimDuration = 40;

        setNoGravity(true);

        setAlphaOverride(1.0f);
    }

    @Override
    public boolean canHoldMove(@Nullable MoveInputType type) {
        return type == MoveInputType.ULTIMATE;
    }

    @Override
    protected void registerMoves(MoveMap<TheSunEntity, State> moves) {
        moves.register(MoveType.HEAVY, FIRE_METEOR, null).withCrouchingVariant(null);

        moves.register(MoveType.SPECIAL1, FIRE_METEORS_1, null);
        moves.register(MoveType.SPECIAL2, FIRE_METEORS_2, null);
        moves.register(MoveType.SPECIAL3, FIRE_BEAM, null);

        moves.register(MoveType.ULTIMATE, CHANGE_SIZE, null);

        moves.register(MoveType.UTILITY, TOGGLE_PASSIVE, null).withCrouchingVariant(null);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(PASSIVE, false);
        dataTracker.startTracking(SCALE, MIN_SCALE);
    }

    @Override
    public boolean handleMove(MoveType type) {
        LivingEntity user = getUserOrThrow();
        boolean sneaking = user.isSneaking();

        switch (type) {
            case ULTIMATE -> {
                boolean shrink = user.isSneaking();
                float newScale = getScale() + (shrink ? -0.05f : 0.05f);

                if (!shrink && newScale <= MAX_SCALE) {
                    // Distributes world collision check to minimize lag
                    int roundScale = Math.round(newScale * 1.2f);

                    Box newBox = newBoundingBox(getX() + 1, getY() + 1, getZ() + 1, newScale * 2.0f);
                    BlockPos start = BlockPos.ofFloored(newBox.minX, newBox.minY, newBox.minZ);
                    BlockPos end = BlockPos.ofFloored(newBox.maxX, newBox.maxY, newBox.maxZ);

                    // Detect if world prevents resize
                    for (int x = start.getX(); x < end.getX(); x += roundScale) {
                        for (int y = start.getY(); y < end.getY(); y += roundScale) {
                            for (int z = start.getZ(); z < end.getZ(); z += roundScale) {
                                BlockPos blockPos = new BlockPos(x, y, z);
                                //JCraft.createParticle((ServerWorld) world, x, y, z, JParticleType.BACK_STAB);
                                if (getWorld().isTopSolid(blockPos, this)) {
                                    //JCraft.createParticle((ServerWorld) world, x, y, z, JParticleType.HIT_SPARK_3);
                                    return false;
                                }
                            }
                        }
                    }
                }

                dataTracker.set(SCALE, MathHelper.clamp(newScale, MIN_SCALE, MAX_SCALE));
            }
            case UTILITY -> {
                if (sneaking) {
                    Vec3d eP = user.getEyePos();
                    Vec3d rangeMod = user.getRotationVector().multiply(MAX_DISTANCE);
                    desiredPosition = getWorld().raycast(new RaycastContext(eP, eP.add(rangeMod),
                            RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user)).getPos();
                } else togglePassive();
            }
            default -> {
                return super.handleMove(type);
            }
        }
        return true;
    }

    private void setTargetPosition(LivingEntity user) {
        if (user == null) return;

        Vec3d eP = user.getEyePos();
        Vec3d rangeMod = user.getRotationVector().multiply(AIMING_DISTANCE);
        EntityHitResult eHit = ProjectileUtil.raycast(user, eP, eP.add(rangeMod),
                user.getBoundingBox().expand(AIMING_DISTANCE),
                EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR,
                AIMING_DISTANCE * AIMING_DISTANCE
        );

        if (eHit != null) targetPosition = eHit.getPos();
        else targetPosition = user.getWorld().raycast(new RaycastContext(eP, eP.add(rangeMod),
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user)).getPos();

        if (user instanceof ServerPlayerEntity serverPlayer)
            serverPlayer.networkHandler.sendPacket(new ParticleS2CPacket(JParticleTypeRegistry.SUN_LOCK_ON, true,
            targetPosition.x, targetPosition.y, targetPosition.z, 0, 0, 0, 0, 1));
    }

    @Override
    public void queueMove(MoveInputType type) {
    }

    private void togglePassive() {
        boolean newPassive = !dataTracker.get(PASSIVE);
        dataTracker.set(PASSIVE, newPassive);
        getUserOrThrow().sendMessage(Text.of(newPassive ? "PASSIVE" : "ACTIVE"));
    }

    public boolean isPassive() {
        return dataTracker.get(PASSIVE);
    }

    @Override
    public boolean allowMoveHandling() {
        return !isPassive();
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.isIn(DamageTypeTags.IS_FIRE) || source.isOf(DamageTypes.IN_WALL)) return false;
        return super.damage(source, amount);
    }

    @Override
    public boolean remoteControllable() {
        return false;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public boolean isPushedByFluids() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void tryBlock() {}

    /**
     * Modified from {@link Entity#pushAwayFrom(Entity)}.
     * @param entity The entity to push away.
     */
    @Override
    public void pushAwayFrom(Entity entity) {
        if (!isConnectedThroughVehicle(entity)) {
            if (!entity.noClip && !this.noClip) {
                double d = entity.getX() - this.getX();
                double e = entity.getZ() - this.getZ();
                double f = MathHelper.absMax(d, e);
                if (f >= 0.001) {
                    f = Math.sqrt(f);
                    d /= f;
                    e /= f;
                    double g = 1.0 / f;
                    if (g > 1.0) g = 1.0;

                    d *= g;
                    e *= g;
                    d *= 0.1;
                    e *= 0.1;

                    if (entity.isPushable())
                        entity.addVelocity(d, 0.0, e);
                }
            }
        }
    }

    public boolean collidesWith(Entity other) {
        return other.isCollidable() && !this.isConnectedThroughVehicle(other);
    }


    @Override
    public void tick() {
        super.tick();

        LivingEntity user = getUser();
        if (user == null) return;

        float scale = getScale();
        float heatFieldSize = scale * 20.0F;

        if (getWorld().isClient()) {
            Vec3d pos = randomPos();
            Vec3d vel = JUtils.randUnitVec(random).multiply(0.2 * scale).add(getVelocity());
            for (int i = 0; i < (int)(heatFieldSize); i++)
                getWorld().addParticle( getSkin() == 2 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
                        false, pos.x, pos.y, pos.z,
                        vel.x, vel.y, vel.z
                );

        } else {
            speed = 0.5F / scale;

            Vec3d pos = getPos();
            Vec3d userPos = user.getPos();

            if (!isRemote())
                setRemote(true);

            // Fly away when summoned
            if (desiredPosition == null) {
                Direction gravity = GravityChangerAPI.getGravityDirection(user);
                int desiredHeight = 32;
                desiredPosition = userPos.add(Vec3d.of(gravity.getVector().multiply(-desiredHeight)));
            } else {
                // Prioritize getting closer
                double distance = pos.squaredDistanceTo(userPos);
                if (distance > MAX_DISTANCE * MAX_DISTANCE) {
                    desiredPosition = desiredPosition.add(userPos.subtract(pos).normalize());
                    if (++overextensionTime > 20)
                        dataTracker.set(SCALE, MathHelper.clamp(getScale() - 0.1f, MIN_SCALE, MAX_SCALE));
                } else {
                    overextensionTime = 0;
                }

                // Go where directed
                if (desiredPosition.squaredDistanceTo(pos) > (scale * scale * 3)) {
                    Vec3d towards = desiredPosition.subtract(pos).normalize().multiply(speed);
                    setVelocity(towards);
                } else {
                    setVelocity(getVelocity().multiply(0.5f));
                }
            }

            if (age > 20) {
                if (age % 40 == 0 && random.nextDouble() >= 0.5)
                    playSound(JSoundRegistry.SUN_IDLE, 1f, random.nextFloat());

                if (heatFieldSize > 0) {
                    Collection<Entity> entities = getWorld().getOtherEntities(this, getBoundingBox().expand(heatFieldSize), EntityPredicates.VALID_ENTITY.and(this::canSee));
                    for (Entity entity : entities) {
                        double distance = entity.squaredDistanceTo(this);
                        double exposure = 125.0 * scale;
                        if (distance == 0)
                            exposure *= 10;
                        else
                            exposure *= 1 / distance;

                        if (exposure > 2) {
                            if (exposure > 8)
                                entity.damage(JDamageSources.create( getWorld(), DamageTypes.ON_FIRE), 1.5f);
                            entity.setOnFireFor(2);
                        }

                        if (entity instanceof LivingEntity living && living.isAlive()) {
                            if (exposure > 0.25) {
                                living.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 10, 0, true, false));
                                if (exposure > 0.5) {
                                    living.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 10, 0, true, false));
                                    if (exposure > 1) {
                                        living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 10, 0, true, false));
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

        prevScale = getScale();
    }

    public static void dryOut(ServerWorld serverWorld, BlockPos pos) {
        BlockState blockState;
        blockState = serverWorld.getBlockState(pos);
        if (blockState.getBlock() instanceof FluidDrainable fluidDrainable) {
            fluidDrainable.tryDrainFluid(serverWorld, pos, blockState);
            //if (!itemStack2.isEmpty()) world.emitGameEvent();
        }
    }

    @Override
    protected Box calculateBoundingBox() {
        double x = getX(), y = getY(), z = getZ();
        float scale = getScale() * 1.5f;
        return newBoundingBox(x, y, z, scale);
    }

    private static Box newBoundingBox(double x, double y, double z, float scale) {
        return new Box(
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
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    @NonNull
    public TheSunEntity getThis() {
        return this;
    }

    public float getScale() {
        return dataTracker.get(SCALE);
    }

    // Animation code
    public enum State implements StandAnimationState<TheSunEntity> {
        IDLE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.sun.idle"))),
        ;

        private final BiConsumer<TheSunEntity, AnimationState> animator;

        State(Consumer<AnimationState> animator) {
            this((silverChariot, builder) -> animator.accept(builder));
        }

        State(BiConsumer<TheSunEntity, AnimationState> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(TheSunEntity attacker, AnimationState builder) {
            animator.accept(attacker, builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @NotNull String getSummonAnimation() {
        return "animation.sun.summon";
    }

    @Override
    public State getBlockState() {
        return null;
    }
}
