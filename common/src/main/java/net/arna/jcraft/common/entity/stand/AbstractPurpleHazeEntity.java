package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.api.attack.enums.MobilityType;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.attack.moves.purplehaze.*;
import net.arna.jcraft.common.attack.moves.shared.KnockdownAttack;
import net.arna.jcraft.common.attack.moves.shared.MainBarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import static net.arna.jcraft.api.registry.JStatusRegistry.PHPOISON;

@Getter
public abstract sealed class AbstractPurpleHazeEntity<E extends AbstractPurpleHazeEntity<E, S>, S extends Enum<S> & StandAnimationState<E>> extends StandEntity<E, S>
        permits PurpleHazeDistortionEntity, PurpleHazeEntity {
    protected PoisonType poisonType = PoisonType.HARMING;

    public static final KnockdownAttack<AbstractPurpleHazeEntity<?, ?>> BACKHAND_FOLLOWUP = new KnockdownAttack<AbstractPurpleHazeEntity<?, ?>>(
            0, 13, 20, 0.75f, 6f, 13, 1.75f, 0.5f, 0.35f, 25)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Hammerfist"),
                    Component.literal("1s knockdown")
            );
    public static final BackhandAttack BACKHAND = new BackhandAttack(14, 6, 14, 0.75f,
            6f, 20, 1.5f, 0.25f, -0.6f, 0.5f)
            .withFollowup(BACKHAND_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withExtraHitBox(0, 0.35, 1.25)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(
                    Component.literal("Backhand"),
                    Component.literal("launches vertically, infects (3s) on hit")
            );

    public static final SimpleAttack<AbstractPurpleHazeEntity<?, ?>> LIGHT_FOLLOWUP = new SimpleAttack<AbstractPurpleHazeEntity<?, ?>>(
            0, 9, 20, 0.75f, 6f, 13, 1.6f, 1.25f, -0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(
                    Component.literal("Kick"),
                    Component.literal("fast combo finisher")
            );

    public static final SimpleAttack<AbstractPurpleHazeEntity<?, ?>> LIGHT = new SimpleAttack<AbstractPurpleHazeEntity<?, ?>>(
            15, 6, 9, 0.75f, 5f, 11, 1.5f, 0.25f, 0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(BACKHAND)
            .noLoopPrevention()
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("fast combo starter")
            );

    public static final SimpleAttack<AbstractPurpleHazeEntity<?, ?>> HEAVY = new SimpleAttack<AbstractPurpleHazeEntity<?, ?>>(
            20, 10, 20, 0.75f, 7f, 14, 2.0f, 1.25f, -0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withInfo(
                    Component.literal("Uppercut"),
                    Component.literal("launcher")
            );

    public static final MainBarrageAttack<AbstractPurpleHazeEntity<?, ?>> BARRAGE = new MainBarrageAttack<AbstractPurpleHazeEntity<?, ?>>(280,
            0, 40, 0.75f, 1f, 30, 2f, 0.25f, 0f, 3, Blocks.DEEPSLATE.defaultDestroyTime())
            .withSound(JSoundRegistry.PH_BARRAGE)
            .withInfo(
                    Component.literal("Barrage"),
                    Component.literal("fast reliable combo starter/extender, high stun")
            );

    public static final LaunchCapsulesAttack LAUNCH_CAPSULES = new LaunchCapsulesAttack(6 * 20, 9, 18, 0.75f)
            .withSound(JSoundRegistry.PH_CAPSULE2)
            .withInfo(
                    Component.literal("Triple Capsule Launch"),
                    Component.literal("launches 3 capsules close by")
            );

    public static final LaunchCapsuleAttack LAUNCH_CAPSULE = new LaunchCapsuleAttack(6 * 20, 7, 14, 0.75f)
            .withSound(JSoundRegistry.PH_CAPSULE1)
            .withCrouchingVariant(LAUNCH_CAPSULES)
            .withInfo(
                    Component.literal("Capsule Launch"),
                    Component.literal("launches a single, fast capsule at the aimed location")
            );

    public static final FullReleaseAttack FULL_RELEASE = new FullReleaseAttack(30 * 20, 30, 0.75f,
            3f, 11, 1.75f, 0.45f, 0.2f, IntSet.of(14, 24))
            .withSound(JSoundRegistry.PH_ULTIMATE)
            .withHitSpark(JParticleType.HIT_SPARK_1)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.LOW)
            .withHyperArmor()
            .withInfo(
                    Component.literal("Full Release"),
                    Component.literal("launches 2 sets of 3 capsules in a hexagonal pattern, uninterruptable")
            );


    // .withFollowup() and .withAnim() must be implemented inside inheritors
    public static final KnockdownAttack<AbstractPurpleHazeEntity<?, ?>> REKKA3 = new KnockdownAttack<AbstractPurpleHazeEntity<?, ?>>(
            0, 10, 20, 1f, 5f, 15, 2f, 0.75f, 0.3f, 55)
            .withSound(JSoundRegistry.PH_REKKA3)
            .withLaunch()
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withBlockStun(8)
            .withInfo(
                    Component.literal("Rekka (Final Hit)"),
                    Component.literal("knockdown, low blockstun")
            );
    public static final SimpleAttack<AbstractPurpleHazeEntity<?, ?>> REKKA2 = new SimpleAttack<AbstractPurpleHazeEntity<?, ?>>
            (0, 9, 18, 1f, 4f, 16, 1.75f, 0.5f, 0f)
            .withSound(JSoundRegistry.PH_REKKA2)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            // .withFollowup(REKKA3)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(
                    Component.literal("Rekka (2nd Hit)"),
                    Component.literal("links into Light")
            );
    public static final PHRekkaAttack REKKA1 = new PHRekkaAttack(100, 7, 14, 1f,
            4f, 15, 1.5f, 0.5f, 0f)
            .withSound(JSoundRegistry.PH_REKKA1)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            // .withFollowup(REKKA2)
            .withExtraHitBox(1.5)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .withMobilityType(MobilityType.DASH)
            .withInfo(
                    Component.literal("Rekka Series"),
                    Component.literal("""
                            A set of three attacks, which cancel into each other during recovery.
                            Last hit knocks down for 2.5s""")
            );
    public static final PHGroundSlamAttack GROUND_SLAM = new PHGroundSlamAttack(6 * 20, 10, 18, 0.75f,
            6f, 10, 1.75f, 0.3f, 0.3f)
            .withSound(JSoundRegistry.PH_GROUNDSLAM)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.LOW)
            .withInfo(
                    Component.literal("Ground Slam"),
                    Component.literal("places down a Purple Haze cloud")
            );

    protected AbstractPurpleHazeEntity(StandType type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean initMove(MoveClass moveClass) {
        if (tryFollowUp(moveClass, MoveClass.LIGHT)) return true;
        if (moveClass == MoveClass.SPECIAL2) {
            final LivingEntity user = getUserOrThrow();
            if (user.hasEffect(JStatusRegistry.DAZED.get())) {
                return false;
            }
            final AbstractMove<?, ? super E> move = getCurrentMove();
            final boolean idling = this.getMoveStun() <= 0;
            if (move == null || move.getMoveClass() != MoveClass.SPECIAL2) {
                if (idling) {
                    return handleMove(MoveClass.SPECIAL2);
                } else {
                    return false;
                }
            } else if (move.getFollowup() != null && move.hasWindupPassed(this)) {
                setMove(move.getFollowup(), (S) move.getFollowup().getAnimation());
            }
            return true;
        }
        return super.handleMove(moveClass);
    }

    @Override
    public void tick() {
        super.tick();
        idleOverride = isRemote();

        if (!isRemoteAndControllable()) {
            return;
        }

        if (level().isClientSide()) {
            JCraft.getClientEntityHandler().purpleHazeRemoteClientTick(this);
        } else {
            final double f = getRemoteForwardInput(), s = getRemoteSideInput();
            final boolean jump = getRemoteJumpInput();

            tickRemoteMovement(f, s, jump);
            tickRemoteState(f, s, onGround());
        }
    }

    public void nextPoisonType() {
        int next = this.poisonType.ordinal() + 1;
        this.poisonType = PoisonType.values()[next % PoisonType.values().length];
    }

    protected abstract void tickRemoteState(double f, double s, boolean dashing);

    /**
     * Code lifted from {@link WhiteSnakeEntity#tickRemoteMovement(double, double, boolean)}
     *
     * @param f    Forward input
     * @param s    +Right/-Left input
     * @param jump Jump input
     */
    public void tickRemoteMovement(double f, double s, boolean jump) {
        final Vec3 pos = position();

        // 1 tick of inertia, helping movement be fluid as well as dealing with packet drops
        if (lastRemoteInputTime - tickCount > 2) {
            updateRemoteInputs(0, 0, false, false);
        }
        final Vec3 rotVec = getLookAngle();

        double dragMult = getMoveStun() > 0 ? 0.2 : 0.4;
        double moveSpeed = 0.24;
        final boolean onGround = onGround();
        final boolean climbing = getFeetBlockState().getTags().anyMatch(tag -> tag == BlockTags.CLIMBABLE);
        final boolean swimming = !level().getFluidState(blockPosition()).isEmpty();

        if (climbing || swimming) {
            dragMult *= 0.5;
        }

        if ((climbing || swimming) && jump) { // Climb or Swim
            push(0, 0.1, 0);
        } else { // Jump
            if (onGround) {
                if (jump) {
                    push(0, 0.75, 0);
                    setRemoteJumpInput(false);
                }
            } else {
                moveSpeed = 0.024;
                dragMult = 0.4;
            }
        }

        remoteSpeed = remoteSpeed
                .add(rotVec.scale(f * moveSpeed)) // Forward movement
                .add(rotVec.yRot(1.5707963f).scale(s * moveSpeed)); // Side movement

        remoteSpeed = remoteSpeed.scale(dragMult);

        final Vec3 userPos = getUserOrThrow().position();
        if (pos.add(remoteSpeed).distanceToSqr(userPos) > 25) {
            remoteSpeed = userPos.subtract(pos).scale(0.05); // 1/20th so it scales with distance
        }

        if (f == 0 && s == 0 && !jump) {
            push(-getDeltaMovement().x * 0.4, -getDeltaMovement().y * 0.4, -getDeltaMovement().z * 0.4);
        }

        push(remoteSpeed.x, remoteSpeed.y, remoteSpeed.z);
        hurtMarked = true;
    }

    public static void infect(LivingEntity target, int ticks) {
        infect(target, ticks, PHPOISON.get());
    }

    public static void infect(LivingEntity target, int ticks, MobEffect effect) {
        final MobEffectInstance instance = target.getEffect(effect);
        if (instance != null) {
            target.addEffect(new MobEffectInstance(effect, instance.getDuration() + ticks, 2));
        } else {
            target.addEffect(new MobEffectInstance(effect, ticks, 2));
        }
    }

    public enum PoisonType {
        HARMING,
        NULLIFYING,
        DEBILITATING;
    }
}
