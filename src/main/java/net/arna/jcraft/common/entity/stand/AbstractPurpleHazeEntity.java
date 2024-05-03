package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.entity.PurpleHazeCloudEntity;
import net.arna.jcraft.common.entity.projectile.PHCapsuleProjectile;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

import static net.arna.jcraft.registry.JStatusRegistry.PHPOISON;

public abstract sealed class AbstractPurpleHazeEntity<E extends AbstractPurpleHazeEntity<E, S>, S extends Enum<S> & StandAnimationState<E>> extends StandEntity<E, S>
        permits PurpleHazeDistortionEntity, PurpleHazeEntity {
    public enum PoisonType {
        HARMING,
        NULLIFYING,
        DEBILITATING;
        static final int count = values().length;
    }
    protected PoisonType poisonType = PoisonType.HARMING;
    protected void nextPoisonType() {
        int next = this.poisonType.ordinal() + 1;
        this.poisonType = PoisonType.values()[next % PoisonType.count];
    }

    public static final KnockdownAttack<AbstractPurpleHazeEntity<?, ?>> BACKHAND_FOLLOWUP = new KnockdownAttack<AbstractPurpleHazeEntity<?, ?>>(
            0, 13, 20, 0.75f, 6f, 13, 1.75f, 0.5f, 0.35f, 25)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Hammerfist"),
                    Text.literal("1s knockdown")
            );
    public static final UppercutAttack<AbstractPurpleHazeEntity<?, ?>> BACKHAND = new UppercutAttack<AbstractPurpleHazeEntity<?, ?>>(20,
            6, 14, 0.75f, 6f, 20, 1.5f, 0.25f, -0.6f, 0.5f)
            .withTargetPostProcessor((attacker, target, kbVec, damageSource, blocking) -> {
                if (!blocking)
                    infect(target, 3 * 20);
            })
            .withFollowup(BACKHAND_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withExtraHitBox(0, 0.35, 1.25)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(
                    Text.literal("Backhand"),
                    Text.literal("launches vertically, infects (3s) on hit")
            );

    public static final SimpleAttack<AbstractPurpleHazeEntity<?, ?>> LIGHT_FOLLOWUP = new SimpleAttack<AbstractPurpleHazeEntity<?, ?>>(
            0, 9, 20, 0.75f, 6f, 13, 1.6f, 1.25f, -0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(
                    Text.literal("Kick"),
                    Text.literal("fast combo finisher")
            );

    public static final SimpleAttack<AbstractPurpleHazeEntity<?, ?>> LIGHT = new SimpleAttack<AbstractPurpleHazeEntity<?, ?>>(
            30, 6, 9, 0.75f, 5f, 11, 1.5f, 0.25f, 0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(BACKHAND)
            .withInfo(
                    Text.literal("Punch"),
                    Text.literal("fast combo starter")
            );

    public static final SimpleAttack<AbstractPurpleHazeEntity<?, ?>> HEAVY = new SimpleAttack<AbstractPurpleHazeEntity<?, ?>>(
            20 * 5, 10, 20, 0.75f, 7f, 14, 2.0f, 1.25f, -0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withInfo(
                    Text.literal("Uppercut"),
                    Text.literal("launcher")
            );

    public static final MainBarrageAttack<AbstractPurpleHazeEntity<?, ?>> BARRAGE = new MainBarrageAttack<AbstractPurpleHazeEntity<?, ?>>(280,
            0, 40, 0.75f, 1f, 30, 2f, 0.25f, 0f, 3, Blocks.DEEPSLATE.getHardness())
            .withSound(JSoundRegistry.PH_BARRAGE)
            .withInfo(
                    Text.literal("Barrage"),
                    Text.literal("fast reliable combo starter/extender, high stun")
            );

    public static final SimpleAttack<AbstractPurpleHazeEntity<?, ?>> LAUNCH_CAPSULES = new SimpleAttack<AbstractPurpleHazeEntity<?, ?>>(
            8 * 20, 9, 18, 0.75f, 0, 0, 0, 0, 0)
            .withSound(JSoundRegistry.PH_CAPSULE2)
            .markRanged()
            .withAction(
                    (attacker, user, ctx, targets) -> {
                        LivingEntity shooter = (attacker.isRemote() && !attacker.remoteControllable()) ? attacker : user;
                        Direction gravity = GravityChangerAPI.getGravityDirection(shooter);
                        for (int i = 0; i < 3; i++)
                            launchCapsule(attacker, shooter, gravity, 0.4F, shooter.getYaw() - 45F + i * 45F);
                    }
            )
            .withInfo(
                    Text.literal("Triple Capsule Launch"),
                    Text.literal("launches 3 capsules close by")
            );

    public static final SimpleAttack<AbstractPurpleHazeEntity<?, ?>> LAUNCH_CAPSULE = new SimpleAttack<AbstractPurpleHazeEntity<?, ?>>(
            8 * 20, 7, 14, 0.75f, 0, 0, 0, 0, 0)
            .withSound(JSoundRegistry.PH_CAPSULE1)
            .withCrouchingVariant(LAUNCH_CAPSULES)
            .markRanged()
            .withAction(
                    (attacker, user, ctx, targets) -> {
                        LivingEntity shooter = (attacker.isRemote() && !attacker.remoteControllable()) ? attacker : user;
                        launchCapsule(attacker, shooter, GravityChangerAPI.getGravityDirection(shooter), 0.8F, shooter.getYaw());
                    }
            )
            .withInfo(
                    Text.literal("Capsule Launch"),
                    Text.literal("launches a single, fast capsule at the aimed location")
            );

    public static final SimpleMultiHitAttack<AbstractPurpleHazeEntity<?, ?>> FULL_RELEASE = new SimpleMultiHitAttack<AbstractPurpleHazeEntity<?, ?>>(
            30 * 20, 30, 0.75f, 3f, 11, 1.75f, 0.45f, 0.2f, IntSet.of(14, 24))
            .withSound(JSoundRegistry.PH_ULTIMATE)
            .withHitSpark(JParticleType.HIT_SPARK_1)
            .withHitAnimation(HitPropertyComponent.HitAnimation.LOW)
            .withAction(AbstractPurpleHazeEntity::performUlt)
            .withHyperArmor()
            .withInfo(
                    Text.literal("Full Release"),
                    Text.literal("launches 2 sets of 3 capsules in a hexagonal pattern, uninterruptable")
            );


    // .withFollowup() and .withAnim() must be implemented inside inheritors
    public static final KnockdownAttack<AbstractPurpleHazeEntity<?, ?>> REKKA3 = new KnockdownAttack<AbstractPurpleHazeEntity<?, ?>>
            (0, 10, 20, 1f, 5f, 15, 2f, 0.75f, 0.3f, 55)
            .withSound(JSoundRegistry.PH_REKKA3)
            .withLaunch()
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withBlockStun(8)
            .withInfo(
                    Text.literal("Rekka (Final Hit)"),
                    Text.literal("knockdown, low blockstun")
            );
    public static final SimpleAttack<AbstractPurpleHazeEntity<?, ?>> REKKA2 = new SimpleAttack<AbstractPurpleHazeEntity<?, ?>>
            (0, 9, 18, 1f, 4f, 16, 1.75f, 0.5f, 0f)
            .withSound(JSoundRegistry.PH_REKKA2)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            // .withFollowup(REKKA3)
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(
                    Text.literal("Rekka (2nd Hit)"),
                    Text.literal("links into Light")
            );
    public static final SimpleAttack<AbstractPurpleHazeEntity<?, ?>> REKKA1 = new SimpleAttack<AbstractPurpleHazeEntity<?, ?>>
            (160, 7, 14, 1f, 4f, 15, 1.5f, 0.5f, 0f)
            .withSound(JSoundRegistry.PH_REKKA1)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            // .withFollowup(REKKA2)
            .withExtraHitBox(1.5)
            .withHitAnimation(HitPropertyComponent.HitAnimation.HIGH)
            .withInitAction(AbstractPurpleHazeEntity::lunge)
            .withMobilityType(MobilityType.DASH)
            .withInfo(
                    Text.literal("Rekka Series"),
                    Text.literal("""
                    A set of three attacks, which cancel into each other during recovery.
                    Last hit knocks down for 2.5s""")
            );
    public static final SimpleAttack<AbstractPurpleHazeEntity<?, ?>> GROUND_SLAM = new SimpleAttack<AbstractPurpleHazeEntity<?, ?>>(
            7 * 20, 10, 18, 0.75f, 6f, 10, 1.75f, 0.3f, 0.3f)
            .withSound(JSoundRegistry.PH_GROUNDSLAM)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHitAnimation(HitPropertyComponent.HitAnimation.LOW)
            .withAction(AbstractPurpleHazeEntity::groundSlam)
            .withInfo(
                    Text.literal("Ground Slam"),
                    Text.literal("places down a Purple Haze cloud")
            );

    protected AbstractPurpleHazeEntity(StandType type, World worldIn) {
        super(type, worldIn, JSoundRegistry.PH_SUMMON);
        idleRotation = 225f;

        description = "Fast Toxic FOOTSIES";

        pros = List.of(
                "fast buttons",
                "oppressive, fast pressure",
                "great area control"
        );

        cons = List.of(
                "only one, rarely accessible armored move",
                "mediocre damage without virus",
                "no movement utility"
        );

        freespace =
                """
                        BNBs:
                        M1 > Rekka1~Rekka2 > crouching M1 > Barrage >...
                            ...crouching M1~M1
                            ...Ground Slam
                            ...M1 > Grab
                        """;
    }

    @Override
    public boolean initMove(MoveType type) {
        if (type == MoveType.SPECIAL2) {
            LivingEntity user = getUserOrThrow();
            if (user.hasStatusEffect(JStatusRegistry.DAZED)) return false;
            boolean idling = this.getMoveStun() <= 0;
            if (curMove == null || curMove.getMoveType() != MoveType.SPECIAL2) {
                if (idling) return handleMove(MoveType.SPECIAL2);
                else return false;
            } else if (curMove.getFollowup() != null && curMove.hasWindupPassed(this))
                setMove(curMove.getFollowup(), (S) curMove.getFollowup().getAnimation());
            return true;
        }
        return super.handleMove(type);
    }

    @Override
    public void tick() {
        super.tick();

        if (!isRemoteAndControllable()) return;

        if (getWorld().isClient()) {
            JCraft.getClientEntityHandler().purpleHazeRemoteClientTick(this);
        } else {
            double f = getRemoteForwardInput();
            double s = getRemoteSideInput();
            boolean jump = getRemoteJumpInput();

            tickRemoteMovement(f, s, jump);
            tickRemoteState(f, s, isOnGround());
        }
    }

    protected abstract void tickRemoteState(double f, double s, boolean dashing);

    /**
     * Code lifted from {@link WhiteSnakeEntity#tickRemoteMovement(double, double, boolean)}
     * @param f Forward input
     * @param s +Right/-Left input
     * @param jump Jump input
     */
    public void tickRemoteMovement(double f, double s, boolean jump) {
        Vec3d pos = getPos();

        // 1 tick of inertia, helping movement be fluid as well as dealing with packet drops
        if (lastRemoteInputTime - age > 2) updateRemoteInputs(0, 0, false, false);
        Vec3d rotVec = new Vec3d(getRotationVector().x, 0, getRotationVector().z).normalize();

        double dragMult = getMoveStun() > 0 ? 0.2 : 0.4;
        double moveSpeed = 0.24;
        boolean onGround = isOnGround();
        boolean climbing = getBlockStateAtPos().streamTags().anyMatch(tag -> tag == BlockTags.CLIMBABLE);
        boolean swimming = !getWorld().getFluidState(getBlockPos()).isEmpty();

        if (climbing || swimming) dragMult *= 0.5;

        if ( (climbing || swimming ) && jump) { // Climb or Swim
            addVelocity(0, 0.1, 0);
        } else { // Jump
            if (onGround) {
                if (jump) {
                    addVelocity(0, 0.75, 0);
                    setRemoteJumpInput(false);
                }
            } else {
                moveSpeed = 0.024;
                dragMult = 0.4;
            }
        }

        remoteSpeed = remoteSpeed
                .add(rotVec.multiply(f * moveSpeed)) // Forward movement
                .add(rotVec.rotateY(1.5707963f).multiply(s * moveSpeed)); // Side movement

        remoteSpeed = remoteSpeed.multiply(dragMult);

        Vec3d userPos = getUserOrThrow().getPos();
        if (pos.add(remoteSpeed).squaredDistanceTo(userPos) > 25)
            remoteSpeed = userPos.subtract(pos).multiply(0.05); // 1/20th so it scales with distance

        addVelocity(remoteSpeed.x, remoteSpeed.y, remoteSpeed.z);
        velocityDirty = true;
        velocityModified = true;
    }

    public static void infect(LivingEntity target, int ticks) {
        infect(target, ticks, PHPOISON);
    }

    public static void infect(LivingEntity target, int ticks, StatusEffect effect) {
        StatusEffectInstance instance = target.getStatusEffect(effect);
        if (instance != null)
            target.addStatusEffect(new StatusEffectInstance(effect, instance.getDuration() + ticks, 2));
        else
            target.addStatusEffect(new StatusEffectInstance(effect, ticks, 2));
    }

    // Attack methods
    private static void lunge(AbstractPurpleHazeEntity<?, ?> attacker, LivingEntity user, MoveContext moveContext) {
        if (attacker.isRemote()) return;
        JUtils.addVelocity(user, attacker.getRotationVector().multiply(0.6));
    }

    private static void performUlt(AbstractPurpleHazeEntity<?, ?> attacker, LivingEntity user, MoveContext ctx, Set<LivingEntity> targets) {
        float baseYaw = user.getYaw();
        Direction gravity = GravityChangerAPI.getGravityDirection(attacker);

        if (attacker.getMoveStun() == 6)
            baseYaw += 60.0F;

        for (int i = 0; i < 3; i++) {
            launchCapsule(attacker, user, gravity, 0.6F, baseYaw + i * 120.0F);
        }
    }

    private static void launchCapsule(AbstractPurpleHazeEntity<?, ?> attacker, LivingEntity user, Direction gravity, float speed, float yaw) {
        PHCapsuleProjectile capsule = new PHCapsuleProjectile(user, attacker.getWorld(), attacker.poisonType);

        Vec2f corrected = RotationUtil.rotPlayerToWorld(yaw, user.getPitch(), gravity);
        // Y,P to P,Y,R
        JUtils.shoot(capsule, user, corrected.y, corrected.x, 0.0F, speed, 0.1F);

        Vec3d upVec = GravityChangerAPI.getEyeOffset(attacker.getUserOrThrow());
        Vec3d heightOffset = upVec.multiply(0.5);
        capsule.setPosition(attacker.getBaseEntity().getPos().add(heightOffset));

        attacker.getWorld().spawnEntity(capsule);
    }

    private static void groundSlam(AbstractPurpleHazeEntity<?, ?> attacker, LivingEntity user, MoveContext ctx, Set<LivingEntity> targets) {
        PurpleHazeCloudEntity cloud = new PurpleHazeCloudEntity(attacker.getWorld(), 3.0f, attacker.poisonType);
        cloud.copyPositionAndRotation(attacker);
        attacker.getWorld().spawnEntity(cloud);
    }
}
