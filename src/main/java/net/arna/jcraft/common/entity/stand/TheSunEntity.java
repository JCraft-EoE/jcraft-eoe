package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.MoveInputType;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.shared.BarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.NoOpMove;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleMultiHitAttack;
import net.arna.jcraft.common.entity.projectile.MeteorProjectile;
import net.arna.jcraft.common.entity.projectile.SunBeamProjectile;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JParticleTypeRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.function.Consumer;

public final class TheSunEntity extends StandEntity<TheSunEntity, TheSunEntity.State> {
    private static final TrackedData<Boolean> PASSIVE;
    private static final TrackedData<Float> SCALE;
    public static final float MAX_SCALE = 3.0F, MIN_SCALE = 1.0F;
    private final int desiredHeight = 32;
    private Vec3d desiredPosition, targetPosition;

    private static final SimpleAttack<TheSunEntity> FIRE_METEOR = new SimpleAttack<TheSunEntity>(20, 5, 10, 0, 0, 0, 0, 0, 0)
            .withInitAction((attacker, user, ctx) -> attacker.setTargetPosition(user))
            .withAction((attacker, user, ctx, targets) -> {
                Vec3d pos = attacker.randomPos();
                fireMeteor(attacker, user, pos, getLookVector(pos, attacker.targetPosition), 2.5f, 0f).setNoGravity(true);
            })
            .withInfo(
                    Text.of("Fire Meteor"),
                    Text.of("Fires a high-velocity meteor with perfect precision")
            );

    private static final SimpleMultiHitAttack<TheSunEntity> FIRE_METEORS_1 = new SimpleMultiHitAttack<TheSunEntity>(
            100, 24, 0, 0, 0, 0, 0, 0, IntSet.of(8, 16, 24)
    )
            .withInitAction((attacker, user, ctx) -> attacker.setTargetPosition(user))
            .withAction((attacker, user, ctx, targets) -> fireMeteors1(attacker, user))
            .withInfo(
                    Text.of("Fire Meteors (Pattern A)"),
                    Text.of("Fires 3 bursts of 3 meteors with high spread")
            );

    private static final BarrageAttack<TheSunEntity> FIRE_METEORS_2 = new BarrageAttack<TheSunEntity>(
            100, 10, 110, 0, 0, 0, 0, 0, 0, 2
    )
            .withAction((attacker, user, ctx, targets) -> fireMeteor(attacker, user, attacker.randomPos(), JUtils.randUnitVec(attacker.random), 1.25f, 0f))
            .withoutSlowness()
            .withInfo(
                    Text.of("Fire Meteors (Pattern B)"),
                    Text.of("Fires a hail of meteors in all directions for 5 seconds")
            );

    private static final SimpleMultiHitAttack<TheSunEntity> FIRE_BEAM = new SimpleMultiHitAttack<TheSunEntity>(
            200, 24, 0, 0, 0, 0, 0, 0, IntSet.of(8, 12, 16)
    )
            .withInitAction((attacker, user, ctx) -> attacker.setTargetPosition(user))
            .withAction((attacker, user, ctx, targets) -> fireSunBeam(attacker, user))
            .withInfo(
                    Text.of("Fire Beams"),
                    Text.of("RANGE: 64m")
            );

    private static final NoOpMove<TheSunEntity> CHANGE_SIZE = new NoOpMove<TheSunEntity>(0, 0, 0)
            .withInfo(
                    Text.of("Change Size"),
                    Text.of("""
                            Use while standing to expand size.
                            Crouch to shrink.
                            
                            Size decreases movement speed and increases heat field.
                            """)
                    //TODO: Size decreases movement speed and increases heat field.
            );

    private static final NoOpMove<TheSunEntity> MOVE = new NoOpMove<TheSunEntity>(0, 0, 0)
            .withInfo(
                    Text.of("Move"),
                    Text.of("""
                            Moves The Sun above the looked location.
                            """)
            );

    private static final NoOpMove<TheSunEntity> TOGGLE_PASSIVE = new NoOpMove<TheSunEntity>(0, 0, 0)
            .withCrouchingVariant(MOVE)
            .withInfo(
                    Text.of("Toggle Passive"),
                    Text.of("""
                            Toggles The Sun between an Active and Passive mode.
                            Active mode - the one it's in when summoned, allows usage of stand moves.
                            Passive mode - allows usage of spec moves while keeping the Sun summoned.
                            """)
            );

    private Vec3d randomPos() {
        return new Vec3d(
                getX() + random.nextGaussian() * getScale(),
                getY() + random.nextGaussian() * getScale(),
                getZ() + random.nextGaussian() * getScale()

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
        meteor.setPosition(pos);
        meteor.setVelocity(velocity.x, velocity.y, velocity.z, speed, divergence);

        attacker.getWorld().spawnEntity(meteor);

        return meteor;
    }

    private static void fireSunBeam(TheSunEntity attacker, @NonNull LivingEntity user) {
        Vec3d pos = attacker.randomPos();

        SunBeamProjectile sunBeam = new SunBeamProjectile(attacker.getWorld());
        sunBeam.setSkin(attacker.getSkin());
        sunBeam.setOwner(user);
        sunBeam.assignSun(attacker);
        sunBeam.setPosition(pos);

        Vec3d lookVec = getLookVector(pos, attacker.targetPosition);
        sunBeam.setVelocity(lookVec.x, lookVec.y, lookVec.z, 0.01f, 2.5f);

        attacker.getWorld().spawnEntity(sunBeam);
    }

    private static void fireMeteors1(TheSunEntity attacker, LivingEntity user) {
        for (int i = 0; i < 3; i++) {
            Vec3d pos = attacker.randomPos();
            fireMeteor(attacker, user, pos, getLookVector(pos, attacker.targetPosition)).setNoGravity(true);
        }
    }

    static {
        PASSIVE = DataTracker.registerData(TheSunEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        SCALE = DataTracker.registerData(TheSunEntity.class, TrackedDataHandlerRegistry.FLOAT);
    }

    public TheSunEntity(World worldIn) {
        super(StandType.THE_SUN, worldIn);

        freespace = """
                Cannot buffer moves.
                """;

        auraColors = new Vec3f[]{
                new Vec3f(1.0f, 0.8f, 4.0f),
                new Vec3f(1.0f, 1.0f, 0.0f),
                new Vec3f(0.4f, 0.8f, 1.0f),
                new Vec3f(0.6f, 0.1f, 0.8f)
        };

        speed = 0.5f;

        summonAnimDuration = 40;

        setAlphaOverride(1.0f);
    }

    @Override
    protected void registerMoves(MoveMap<TheSunEntity, State> moves) {
        moves.register(MoveType.HEAVY, FIRE_METEOR, null);

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
                float newScale = getScale() + (sneaking ? -1.0F : 1.0F);
                dataTracker.set(SCALE, MathHelper.clamp(newScale, MIN_SCALE, MAX_SCALE));
            }
            case UTILITY -> {
                if (sneaking) {
                    Vec3d eP = user.getEyePos();
                    Vec3d rangeMod = user.getRotationVector().multiply(96);
                    desiredPosition = getWorld().raycast(new RaycastContext(eP, eP.add(rangeMod),
                            RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user)).getPos();
                }
                togglePassive();
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
        Vec3d rangeMod = user.getRotationVector().multiply(96);
        EntityHitResult eHit = ProjectileUtil.raycast(user, eP, eP.add(rangeMod),
                user.getBoundingBox().expand(96),
                EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR,
                9216 // Squared
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
        dataTracker.set(PASSIVE, !dataTracker.get(PASSIVE));
    }

    public boolean isPassive() {
        return dataTracker.get(PASSIVE);
    }

    @Override
    public boolean allowMoveHandling() {
        return !isPassive();
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return damageSource.isOutOfWorld();
    }

    @Override
    public void tryBlock() {}

    @Override
    public void tick() {
        super.tick();

        if (!world.isClient()) {
            LivingEntity user = getUser();
            if (user == null) return;

            // Ensure remote state
            //if (hasVehicle()) dismountVehicle();

            Vec3d pos = getPos();

            if (!isFree()) {
                setFree(true);
                setFreePos(new Vec3f(pos));
            }

            // Move where needed
            if (desiredPosition == null) {
                Direction gravity = GravityChangerAPI.getGravityDirection(user);
                desiredPosition = user.getPos().add(Vec3d.of(gravity.getVector().multiply(-desiredHeight)));
            } else if (desiredPosition.squaredDistanceTo(pos) > getScale() * getScale()) {
                Vec3d towards = desiredPosition.subtract(pos).normalize().multiply(speed);
                Vec3d newPos = new Vec3d(getX() + towards.x, getY() + towards.y, getZ() + towards.z);
                if (!world.isTopSolid(new BlockPos(newPos), this)) {
                    setPosition(newPos);
                    setFreePos(new Vec3f(getPos()));
                }
            }
        }
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
        IDLE(builder -> builder.loop("animation.sun.idle")),
        ;
        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(TheSunEntity attacker, AnimationBuilder builder) {
            animator.accept(builder);
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
