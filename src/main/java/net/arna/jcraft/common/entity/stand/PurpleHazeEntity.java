package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.MoveInputType;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.StunType;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.player.PhComponent;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class PurpleHazeEntity extends AbstractPurpleHazeEntity<PurpleHazeEntity, PurpleHazeEntity.State> {
    private static final @NonNull KnockdownAttack<AbstractPurpleHazeEntity<?, ?>> CROUCHING_LIGHT_FOLLOWUP_ATTACK = BACKHAND_FOLLOWUP.copy().withAnim(State.BACKHAND_FOLLOWUP).allowHitUser();
    private static final @NonNull UppercutAttack<AbstractPurpleHazeEntity<?, ?>> CROUCHING_LIGHT_ATTACK = BACKHAND.copy().withFollowup(CROUCHING_LIGHT_FOLLOWUP_ATTACK).allowHitUser();
    private static final @NonNull SimpleAttack<AbstractPurpleHazeEntity<?, ?>> LIGHT_FOLLOWUP_ATTACK = LIGHT_FOLLOWUP.copy().withAnim(State.LIGHT_FOLLOWUP).allowHitUser();
    private static final @NonNull SimpleAttack<AbstractPurpleHazeEntity<?, ?>> LIGHT_ATTACK = LIGHT.copy().withFollowup(LIGHT_FOLLOWUP_ATTACK).withCrouchingVariant(CROUCHING_LIGHT_ATTACK).allowHitUser();
    private static final @NonNull MainBarrageAttack<AbstractPurpleHazeEntity<?, ?>> BARRAGE_ATTACK = AbstractPurpleHazeEntity.BARRAGE.copy().allowHitUser();
    private static final @NonNull SimpleAttack<AbstractPurpleHazeEntity<?, ?>> HEAVY_ATTACK = HEAVY.copy().allowHitUser();
    private static final @NonNull KnockdownAttack<AbstractPurpleHazeEntity<?, ?>> REKKA_3 = REKKA3.copy().withAnim(State.REKKA3).allowHitUser();
    private static final @NonNull SimpleAttack<AbstractPurpleHazeEntity<?, ?>> REKKA_2 = REKKA2.copy().withAnim(State.REKKA2).withFollowup(REKKA_3).allowHitUser();
    private static final @NonNull SimpleAttack<AbstractPurpleHazeEntity<?, ?>> REKKA_1 = REKKA1.copy().withAnim(State.REKKA1).withFollowup(REKKA_2).allowHitUser();
    private static final @NonNull SimpleAttack<AbstractPurpleHazeEntity<?, ?>> GROUND_SLAM = AbstractPurpleHazeEntity.GROUND_SLAM.copy().allowHitUser();

    public static final int MAX_RAGE = 20 * 60;
    private int rage = 0;
    private boolean flowerable = false;

    public static final SimpleAttack<AbstractPurpleHazeEntity<?, ?>> GRAB_HIT_FINAL = new SimpleAttack<AbstractPurpleHazeEntity<?, ?>>(0, 27,
            34, 0.75f, 4f, 8, 2f, 1.25f, 0f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .allowHitUser()
            .withInfo(
                    Text.literal("Grab (Final Hit)"),
                    Text.empty()
            );
    public static final SimpleMultiHitAttack<AbstractPurpleHazeEntity<?, ?>> GRAB_HIT = new SimpleMultiHitAttack<AbstractPurpleHazeEntity<?, ?>>(0,
            34, 0.75f, 1f, 10, 2f, 0f, 0f, IntSet.of(6, 8, 10, 12, 14, 16, 18))
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withStunType(StunType.UNBURSTABLE)
            .withFinisher(19, GRAB_HIT_FINAL)
            .allowHitUser()
            .withInfo(
                    Text.literal("Grab (Final Hit)"),
                    Text.empty()
            );
    public static final GrabAttack<PurpleHazeEntity, State> GRAB = new GrabAttack<>(
            280, 12, 24, 0.75f, 0f, 45, 1.5f, 0f, 0f,
            GRAB_HIT, State.GRAB_HIT, 25, 1)
            .withCrouchingVariant(GROUND_SLAM)
            .withSound(JSoundRegistry.D4C_THROW)
            .withImpactSound(JSoundRegistry.PH_GRAB_HIT)
            .allowHitUser()
            .withInfo(
                    Text.literal("Grab"),
                    Text.literal("unblockable, combo finisher")
            );

    public PurpleHazeEntity(World worldIn) {
        super(StandType.PURPLE_HAZE, worldIn);

        description = "Automatic Walking BIOWEAPON";

        cons = List.of(
                "uncontrollable",
                "can and will hurt the user"
        );

        freespace = """
                PASSIVE: Rage
                Builds up while the stand is summoned.
                Maxes out after 1 minute. When maxed, aura turns red.
                Rage decreases by half with each living thing Purple Haze kills.
                Purple Haze has a chance to target it's own user which increases with rage.""";

        auraColors = new Vec3f[]{
                new Vec3f(1.0f, 0.2f, 0.6f),
                new Vec3f(0.3f, 1.0f, 0.6f),
                new Vec3f(1.0f, 1.0f, 1.0f),
                new Vec3f(0.5f, 0.3f, 1.0f)
        };
    }

    @Override
    public Vec3f getAuraColor() {
        if (rage == MAX_RAGE)
            return Vec3f.POSITIVE_X;
        return super.getAuraColor();
    }

    @Override
    protected void registerMoves(MoveMap<PurpleHazeEntity, State> moves) {
        MoveMap.Entry<PurpleHazeEntity, State> light = moves.register(MoveType.LIGHT, LIGHT_ATTACK, State.PUNCH);
        light.withFollowUp(State.LIGHT_FOLLOWUP);
        light.withCrouchingVariant(State.BACKHAND).withFollowUp(State.BACKHAND_FOLLOWUP);

        moves.register(MoveType.BARRAGE, BARRAGE_ATTACK, State.BARRAGE);
        moves.register(MoveType.HEAVY, HEAVY_ATTACK, State.HEAVY);

        moves.register(MoveType.SPECIAL1, LAUNCH_CAPSULE, State.LAUNCH).withCrouchingVariant(State.LAUNCH2);
        moves.register(MoveType.SPECIAL2, REKKA_1, State.REKKA1);
        moves.register(MoveType.SPECIAL3, GRAB, State.GRAB).withCrouchingVariant(State.GROUND_SLAM);

        moves.register(MoveType.ULTIMATE, FULL_RELEASE, State.FULL_RELEASE);
    }

    @Override
    public void queueMove(MoveInputType type) {
        if (!remoteControllable() && queuedMove == MoveInputType.STAND_SUMMON)
            return;
        super.queueMove(type);
    }

    @Override
    public boolean initMove(MoveType type) {
        if (type == MoveType.LIGHT && curMove != null && curMove.getMoveType() == MoveType.LIGHT && getMoveStun() < curMove.getWindupPoint()) {
            AbstractMove<?, ? super PurpleHazeEntity> followup = curMove.getFollowup();
            if (followup != null) {
                setMove(followup, (State) followup.getAnimation());
                return true;
            }
        }

        return super.initMove(type);
    }

    public boolean handleMove(MoveType type) {
        MoveMap.Entry<PurpleHazeEntity, State> entry = getMoveMap().getFirstValidEntry(type, getThis());
        if (entry == null) return false;

        LivingEntity stateChecker = (isRemote() && !remoteControllable()) ? this : getUserOrThrow();

        if (hasUser() && !stateChecker.isOnGround() && entry.getAerialVariant() != null)
            entry = entry.getAerialVariant();
        if (hasUser() && stateChecker.isSneaking() && entry.getCrouchingVariant() != null)
            entry = entry.getCrouchingVariant();
        if (hasUser() && !stateChecker.isOnGround() && entry.getAerialVariant() != null)
            entry = entry.getAerialVariant();

        AbstractMove<?, ? super PurpleHazeEntity> move = entry.getMove();
        return handleMove(move.shouldCopyOnUse() ? move.copy() : move, entry.getCooldownType(), entry.getAnimState());
    }

    @Override
    public boolean allowMoveHandling() {
        return remoteControllable();
    }

    @Override
    public boolean remoteControllable() {
        return false;
    }

    @Override
    public void standBlock() {
        if (!hasUser()) return;
        // Projectile deflection
        List<ProjectileEntity> toDeflect = this.world.getEntitiesByClass(ProjectileEntity.class, this.getBoundingBox().expand(0.75f), EntityPredicates.VALID_ENTITY);

        for (ProjectileEntity projectile : toDeflect) {
            if (projectile.getOwner() == getUserOrThrow()) continue;
            projectile.setVelocity(projectile.getVelocity().multiply(-0.5).add(0, -0.1, 0));
            projectile.velocityModified = true;
        }

        if (!isRemote())
            stun(getUserOrThrow(), 2, 2);
        getUserOrThrow().addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 5, 3, false, false, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (++rage >= MAX_RAGE)
            rage = MAX_RAGE;

        if (!hasUser()) return;
        LivingEntity user = getUser();

        if (!world.isClient()) {
            boolean isRemote = isRemote();

            if (!remoteControllable()) {
                if (getAlphaOverride() != 1.0f)
                    setAlphaOverride(1.0f);

                LivingEntity target = getTarget();
                if (target != null && !target.isAlive())
                    target = null;

                if (target == null) {
                    List<LivingEntity> potentialTargets = world.getEntitiesByClass(
                            LivingEntity.class,
                            getBoundingBox().expand(64.0),
                            EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(EntityPredicates.VALID_LIVING_ENTITY));
                    potentialTargets.remove(this);

                    Comparator<Entity> distanceComparator = (entity1, entity2) -> {
                        double distance1 = this.distanceTo(entity1);
                        double distance2 = this.distanceTo(entity2);
                        return Double.compare(distance1, distance2);
                    };
                    potentialTargets.sort(distanceComparator);

                    for (LivingEntity potentialTarget : potentialTargets) {
                        if (!canSee(potentialTarget)) continue;
                        if (potentialTarget instanceof StandEntity<?,?> standEntity && standEntity.hasUser()) {
                            setTarget(standEntity.getUserOrThrow());
                            break;
                        }
                        if (potentialTarget == user) {
                            if (age % 20 == 0 && random.nextDouble() * MAX_RAGE <= rage) {
                                setTarget(user);
                                break;
                            }
                            continue;
                        }
                        setTarget(potentialTarget);
                        break;
                    }
                } else {
                    double speed = getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                    // user is not null (see above)
                    if (user.hasStatusEffect(JStatusRegistry.DAZED))
                        speed = user.getMovementSpeed();
                    if (age % 4 == 0) // Pathfinding is expensive
                        navigation.startMovingTo(target, speed);

                    standUserAI(this, target, this);
                }

                if (!isRemote)
                    setRemote(true);
            }

            if (isRemote)
                tickRemoteState(getMoveControl().getSpeed(), getMoveControl().sidewaysMovement, onGround);
        }
    }

    @Override
    protected void tickRemoteState(double f, double s, boolean dashing) {
        LivingEntity user = getUserOrThrow();
        if (getState() == State.IDLE) {
            if (JUtils.canAct(user)) {
                if (s > 0) setStateNoReset(dashing ? State.RIGHT : State.RIGHT_DASH);
                if (s < 0) setStateNoReset(dashing ? State.LEFT : State.LEFT_DASH);
                if (f < 0) setStateNoReset(dashing ? State.BACKWARD : State.BACKWARD_DASH);
                if (f > 0) setStateNoReset(dashing ? State.FORWARD : State.FORWARD_DASH);
            } else {
                setStateNoReset(State.HURT);
            }
        }
    }

    @Override
    @NonNull
    public PurpleHazeEntity getThis() {
        return this;
    }

    @Override
    public void freshKill() {
        super.freshKill();
        flowerable = true;
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        JCraft.LOGGER.info("Interacting with PH");
        final ItemStack stack = player.getStackInHand(hand);
        if (player == getUser() && stack.isIn(ItemTags.FLOWERS)) {
            JCraft.LOGGER.info("Recognized user! flowerable="+flowerable);
            if (!flowerable) {
                return ActionResult.FAIL;
            }
            stack.setCount(stack.getCount()-1);
            desummon();
            final PhComponent ph = JComponents.getPhData(player);
            ph.increaseLevel();
            if (ph.getLevel() == 5) {
                // TODO upgrade ph
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    // Animation code
    public enum State implements StandAnimationState<PurpleHazeEntity> {
        IDLE((PurpleHaze, builder) -> builder.loop("animation.purple_haze.idle")),
        PUNCH(builder -> builder.playAndHold("animation.purple_haze.light")),
        BLOCK(builder -> builder.loop("animation.purple_haze.block")),
        HEAVY(builder -> builder.playAndHold("animation.purple_haze.heavy")),

        FULL_RELEASE(builder -> builder.playAndHold("animation.purple_haze.full_release")),
        GROUND_SLAM(builder -> builder.playAndHold("animation.purple_haze.ground_slam")),

        BARRAGE(builder -> builder.loop("animation.purple_haze.barrage")),
        LAUNCH(builder -> builder.playAndHold("animation.purple_haze.launch")),
        LAUNCH2(builder -> builder.playAndHold("animation.purple_haze.launch2")),

        REKKA1(builder -> builder.playAndHold("animation.purple_haze.rekka1")),
        REKKA2(builder -> builder.playAndHold("animation.purple_haze.rekka2")),
        REKKA3(builder -> builder.playAndHold("animation.purple_haze.rekka3")),

        GRAB(builder -> builder.playAndHold("animation.purple_haze.grab")),
        GRAB_HIT(builder -> builder.playAndHold("animation.purple_haze.grab_hit")),

        BACKHAND(builder -> builder.playAndHold("animation.purple_haze.backhand")),
        BACKHAND_FOLLOWUP(builder -> builder.playAndHold("animation.purple_haze.backhand_followup")),
        LIGHT_FOLLOWUP(builder -> builder.playAndHold("animation.purple_haze.light_followup")),
        HURT(builder -> builder.playAndHold("animation.purple_haze.hurt")),

        FORWARD(builder -> builder.loop("animation.purple_haze.forw")),
        BACKWARD(builder -> builder.loop("animation.purple_haze.back")),
        LEFT(builder -> builder.loop("animation.purple_haze.left")),
        RIGHT(builder -> builder.loop("animation.purple_haze.right")),
        FORWARD_DASH(builder -> builder.loop("animation.purple_haze.fdash")),
        BACKWARD_DASH(builder -> builder.loop("animation.purple_haze.bdash")),
        LEFT_DASH(builder -> builder.loop("animation.purple_haze.ldash")),
        RIGHT_DASH(builder -> builder.loop("animation.purple_haze.rdash")),;

        private final BiConsumer<PurpleHazeEntity, AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this((silverChariot, builder) -> animator.accept(builder));
        }

        State(BiConsumer<PurpleHazeEntity, AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(PurpleHazeEntity attacker, AnimationBuilder builder) {
            animator.accept(attacker, builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @NotNull String getSummonAnimation() {
        return "animation.purple_haze.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
