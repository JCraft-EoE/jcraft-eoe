package net.arna.jcraft.api.attack.moves;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectFloatPair;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.AttackData;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.core.HitBoxData;
import net.arna.jcraft.api.attack.enums.BlockableType;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.misc.JBlockBreaker;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.attack.core.data.AttackMoveExtras;
import net.arna.jcraft.common.attack.core.data.BaseMoveExtras;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.ExtraProducts;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.arna.jcraft.api.Attacks.damageLogic;

/**
 * An attack with just one hit box.
 * Can be extended to support all kinds of box attacks.
 * Moves that don't attack (i.e. don't have a hitbox) such as time-stop or dim-hop,
 * should probably not extend this. Anything else probably should.
 *
 * @param <T>
 * @param <A>
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@Getter
public abstract class AbstractSimpleAttack<T extends AbstractSimpleAttack<T, A>, A extends IAttacker<? extends A, ?>> extends AbstractMove<T, A> {
    private final Set<HitBoxData> extraHitBoxes = new HashSet<>();
    private float damage;
    @NonNull
    private StunType stunType = StunType.BURSTABLE;
    private int stun;
    private float hitboxSize;
    private float knockback;
    private float offset;
    private boolean overrideStun;
    private boolean lift = true, canBackstab = true;
    private int blockStun = -1;
    private boolean staticY;
    private boolean doShockwaves = false;
    private @Nullable CommonHitPropertyComponent.HitAnimation hitAnimation = CommonHitPropertyComponent.HitAnimation.MID;
    private @NonNull BlockableType blockableType = BlockableType.BLOCKABLE;
    protected @Nullable JParticleType hitSpark = JParticleType.HIT_SPARK_1;

    protected AbstractSimpleAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                                   final float damage, final int stun, final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance);
        this.damage = damage;
        this.stun = stun;
        this.hitboxSize = hitboxSize;
        this.knockback = knockback;
        this.offset = offset;
    }

    // Properties alteration methods

    /**
     * Sets the damage of this attack.
     * Should be set using the constructor. This is only to modify copies.
     *
     * @param damage The damage of this attack
     * @return This attack
     */
    public T withDamage(final float damage) {
        this.damage = damage;
        return getThis();
    }

    /**
     * Sets the hitbox size of this attack.
     * Should be set using the constructor. This is only to modify copies.
     *
     * @param hitboxSize The hitbox size of this attack
     * @return This attack
     */
    public T withHitboxSize(final float hitboxSize) {
        this.hitboxSize = hitboxSize;
        return getThis();
    }

    /**
     * Sets the knockback of this attack.
     * Should be set using the constructor. This is only to modify copies.
     *
     * @param knockback The knockback of this attack
     * @return This attack
     */
    public T withKnockback(final float knockback) {
        this.knockback = knockback;
        return getThis();
    }

    /**
     * Sets the offset of this attack.
     * Should be set using the constructor. This is only to modify copies.
     *
     * @param offset The offset of this attack
     * @return This attack
     */
    public T withOffset(final float offset) {
        this.offset = offset;
        return getThis();
    }

    /**
     * Sets the stun of this attack.
     * Should be set using the constructor. This is only to modify copies.
     *
     * @param stun The stun of this attack
     * @return This attack
     */
    public T withStun(final int stun) {
        this.stun = stun;
        return getThis();
    }

    /**
     * Sets the type to stun the target with.
     *
     * @param type The type of stun to apply
     * @return This attack
     */
    public T withStunType(@NonNull final StunType type) {
        this.stunType = type;
        return getThis();
    }

    /**
     * Sets that the current stun should be removed from targets when applying stun.
     * Defaults to {@code false}.
     *
     * @return This attack
     * @see #withOverrideStun(boolean)
     */
    public T withOverrideStun() {
        return withOverrideStun(true);
    }

    /**
     * Sets whether the current stun should be removed from targets when applying stun.
     * Defaults to {@code false}.
     *
     * @return This attack
     */
    public T withOverrideStun(final boolean overrideStun) {
        this.overrideStun = overrideStun;
        return getThis();
    }

    /**
     * Sets whether targets should remain stuck in the air while this attack is active.
     * Defaults to {@code true}
     *
     * @param lift The new value of {@code lift}
     * @return This attack
     */
    public T withLift(final boolean lift) {
        this.lift = lift;
        return getThis();
    }

    /**
     * Sets whether the attack can backstab.
     * Defaults to {@code true}.
     *
     * @param canBackstab Whether the attack can backstab
     * @return This attack
     */
    public T withBackstab(final boolean canBackstab) {
        this.canBackstab = canBackstab;
        return getThis();
    }

    /**
     * Sets the stun applied to the user when this attack is performed on a target that is blocking.
     * A positive value implies that the default calculation of {@code damage + 4} should be overridden
     * by the value passed here.
     *
     * @param blockStun The number of ticks to stun for
     * @return This attack
     */
    public T withBlockStun(final int blockStun) {
        this.blockStun = blockStun;
        return getThis();
    }

    /**
     * Sets whether the user's pitch should influence the positioning of hitboxes.
     * If {@code false}, the hitbox will be moved up or down depending on the user's pitch,
     * otherwise, the y-position (or whatever it is, depending on the gravity) will be static.
     *
     * @return This attack
     */
    public T withStaticY() {
        return withStaticY(true);
    }

    public T withStaticY(final boolean staticHeight) {
        this.staticY = staticHeight;
        return getThis();
    }

    /**
     * Sets the blockable type of this attack.
     * Defaults to {@link BlockableType#BLOCKABLE BLOCKABLE}.
     *
     * @param blockableType The new blockable type
     * @return This attack
     */
    public T withBlockableType(final @NonNull BlockableType blockableType) {
        this.blockableType = blockableType;
        return getThis();
    }

    /**
     * Sets the hit animation the enemy will perform when hit by this attack.
     *
     * @return This attack
     */
    public T withHitAnimation(final CommonHitPropertyComponent.HitAnimation hitAnimation) {
        this.hitAnimation = hitAnimation;
        return getThis();
    }

    /**
     * Adds an extra hitbox with the given size to use with every attack
     * along with the main hitbox.
     *
     * @param size The size of the hitbox
     * @return This attack
     * @see #withExtraHitBox(double, double, double)
     */
    public T withExtraHitBox(final double size) {
        return withExtraHitBox(new HitBoxData(size));
    }

    /**
     * Adds an extra hitbox with the given size and offsets to use with every attack
     * along with the main hitbox.
     *
     * @param forwardOffset  The forward offset of the hitbox
     * @param verticalOffset The vertical offset of the hitbox
     * @param size           The size of the hitbox
     * @return This attack
     */
    public T withExtraHitBox(final double forwardOffset, final double verticalOffset, final double size) {
        return withExtraHitBox(new HitBoxData(forwardOffset, verticalOffset, size));
    }

    /**
     * Adds an extra hitbox to use with every attack along with the main hitbox.
     *
     * @param hitBox The hitbox to add
     * @return This attack
     */
    public T withExtraHitBox(final HitBoxData hitBox) {
        extraHitBoxes.add(hitBox);
        return getThis();
    }

    /**
     * Marks this attack as a launch attack.
     *
     * @return This attack
     */
    public T withLaunch() {
        withLaunchNoShockwave();
        withShockwaves();
        return getThis();
    }

    public T withLaunchNoShockwave() {
        stunType = StunType.LAUNCH;
        overrideStun = true;
        hitAnimation = CommonHitPropertyComponent.HitAnimation.LAUNCH;
        return getThis();
    }

    public T withShockwaves() {
        return withShockwaves(true);
    }

    public T withShockwaves(final boolean shockwaves) {
        this.doShockwaves = shockwaves;
        return getThis();
    }

    /**
     * Sets the hit spark particle this attack will use when it hits something.
     *
     * @param particle The hit spark particle to use
     * @return This attack
     */
    public T withHitSpark(final JParticleType particle) {
        hitSpark = particle;
        return getThis();
    }

    public AttackMoveExtras getAttackExtras() {
        return AttackMoveExtras.fromMove(getThis());
    }

    public int getBlockStun() {
        return blockStun < 0 ? (int) (damage + 4) : blockStun;
    }

    // Utility methods
    public static AABB createBox(final Vec3 center, final double size) {
        double axisSize = size / 2;

        final Vec3 min = center.subtract(axisSize, axisSize, axisSize);
        final Vec3 max = center.add(axisSize, axisSize, axisSize);
        return new AABB(min, max);
    }

    public static AABB createBox(final Vec3 offsetHeightPos, final Vec3 rotVec, final Vec3 upVec, final HitBoxData data) {
        return createBox(offsetHeightPos.add(rotVec.scale(data.forwardOffset()))
                .add(upVec.scale(data.verticalOffset())), data.size());
    }

    /**
     * Finds all valid targets that can be damaged with the given damage source
     * by the given attacker, contained in the given boxes.
     * Also maps all attackers found to their user. I.e. redirecting damage done to attackers to their users.
     *
     * @param attacker     The attacker that will be doing the damage
     * @param boxCenter    The center of the box to check in
     * @param boxSize      The size of the box to check in
     * @param damageSource The damage source to check for
     * @return All found valid targets
     */
    public static Set<LivingEntity> findHits(final IAttacker<?, ?> attacker, final Vec3 boxCenter, final double boxSize, final @Nullable DamageSource damageSource) {
        return findHits(attacker, createBox(boxCenter, boxSize), damageSource);
    }

    /**
     * Finds all valid targets that can be damaged with the given damage source
     * by the given attacker, contained in the given boxes.
     * Also maps all attackers found to their user. I.e. redirecting damage done to attackers to their users.
     *
     * @param attacker     The attacker that will be doing the damage
     * @param box          The box to check in
     * @param damageSource The damage source to check for
     * @return All found valid targets
     */
    public static Set<LivingEntity> findHits(final IAttacker<?, ?> attacker, final AABB box, final @Nullable DamageSource damageSource) {
        return findHits(attacker, Set.of(box), damageSource);
    }

    /**
     * Finds all valid targets that can be damaged with the given damage source
     * by the given attacker, contained in the given boxes.
     * Also maps all attackers found to their user. I.e., redirecting damage done to attackers to their users.
     *
     * @param attacker     The attacker that will be doing the damage
     * @param boxes        The boxes to check in
     * @param damageSource The damage source to check for
     * @return All found valid targets
     */
    public static Set<LivingEntity> findHits(final IAttacker<?, ?> attacker, final Set<AABB> boxes, final @Nullable DamageSource damageSource) {
        return findHits(attacker, boxes, damageSource, LivingEntity.class);
    }

    /**
     * Finds all valid targets that can be damaged with the given damage source
     * by the given attacker, contained in the given boxes.
     * Also maps all attackers found to their user. I.e., redirecting damage done to attackers to their users.
     *
     * @param attacker     The attacker that will be doing the damage
     * @param boxes        The boxes to check in
     * @param damageSource The damage source to check for
     * @param mayHitUser   Whether the user of the attacker can be hit
     * @return All found valid targets
     */
    public static Set<LivingEntity> findHits(final IAttacker<?, ?> attacker, final Set<AABB> boxes, final @Nullable DamageSource damageSource,
                                             final boolean mayHitUser) {
        return findHits(attacker, boxes, damageSource, LivingEntity.class, mayHitUser);
    }

    /**
     * Finds all valid targets that can be damaged with the given damage source
     * by the given attacker, contained in the given boxes.
     * Also maps all attackers found to their user.
     * I.e., redirecting damage done to stands to their users.
     *
     * @param attacker     The attacker that will be doing the damage
     * @param boxes        The boxes to check in
     * @param damageSource The damage source to check for
     * @param type         The type of entities to look for
     * @return All found valid targets
     */
    public static <T extends Entity> @NonNull Set<T> findHits(final IAttacker<?, ?> attacker, final @NonNull Set<AABB> boxes,
                                                              final @Nullable DamageSource damageSource, final Class<T> type) {
        return findHits(attacker, boxes, damageSource, type, false);
    }

    /**
     * Finds all valid targets that can be damaged with the given damage source
     * by the given attacker, contained in the given boxes.
     * Also maps all attackers found to their user. I.e., redirecting damage done to stands to their users.
     *
     * @param attacker     The attacker that will be doing the damage
     * @param boxes        The boxes to check in
     * @param damageSource The damage source to check for
     * @param type         The type of entities to look for
     * @param mayHitUser   Whether the user of the attacker can be hit
     * @return All found valid targets
     */
    public static <T extends Entity> @NonNull Set<T> findHits(final IAttacker<?, ?> attacker, final @NonNull Set<AABB> boxes,
                                                              final @Nullable DamageSource damageSource, final Class<T> type, final boolean mayHitUser) {
        final LivingEntity user = attacker.getUser();
        final Set<T> result = new HashSet<>();
        final Predicate<? super Entity> filter = e -> e != attacker && (mayHitUser || (e != user && e != user.getVehicle() && e != JUtils.getStand(user)));
        final Predicate<? super Entity> isCreativeStand = e -> e instanceof final StandEntity<?,?> stand && stand.getUser() instanceof final Player player && (player.isCreative() || player.isSpectator());
        for (final AABB box : boxes) {
            for (final T entity : attacker.getEntityWorld().getEntitiesOfClass(type, box, EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(filter).and(isCreativeStand.negate()))) {
                if (damageSource == null || JUtils.canDamage(damageSource, entity)) {
                    result.add(entity);
                }
                if (entity instanceof final StandEntity<?,?> hitStand && !hitStand.isRemote() && hitStand.hasUser() && type.isInstance(hitStand.getUserOrThrow())) {
                    T hitUser = type.cast(hitStand.getUserOrThrow());
                    if (damageSource == null || JUtils.canDamage(damageSource, hitUser)) {
                        result.add(hitUser);
                    }
                }
            }
        }
        return result;
    }

    // Logic methods
    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        Vec3 userRotVec = user.getLookAngle();
        final Direction gravDir = GravityChangerAPI.getGravityDirection(user);
        if (gravDir == Direction.UP) {
            userRotVec = new Vec3(userRotVec.x, -userRotVec.y, userRotVec.z);
        }

        final Vec3 hPos = getOffsetHeightPos(attacker);
        Vec3 rotVec = (staticY || attacker.isRemote()) ? getRotVec(attacker) : userRotVec;
        final Vec3 upVec = new Vec3(gravDir.step()).scale(-1.0);

        if (staticY) {
            rotVec = rotVec.with(gravDir.getAxis(), 0);
        }

        final Vec3 fPos = getOffsetForwardPos(attacker, hPos, upVec, rotVec);

        final Set<AABB> boxes = calculateBoxes(attacker, user, rotVec, upVec, hPos, fPos);
        final DamageSource damageSource = attacker.getDamageSource();
        final Set<LivingEntity> targets = attackBoxes(attacker, boxes, damageSource, fPos);

        // Do shockwaves
        if (doShockwaves && !targets.isEmpty()) {
            createShockwaves(attacker, user);
        }

        performHook(attacker, targets, boxes, damageSource, fPos, rotVec);
        return targets;
    }

    /**
     * A hook for processing the attack with more context than {@link AbstractMove#perform(IAttacker, LivingEntity)}
     *
     * @param targets        The valid targets found within the attack's hitboxes
     * @param boxes          The attack's hitboxes
     * @param damageSource   The attacker's damageSource
     * @param forwardPos     The offset forward position
     * @param rotationVector The attacker's rotation unit vector
     */
    protected void performHook(final A attacker, final Set<LivingEntity> targets, final Set<AABB> boxes,final DamageSource damageSource,
                               final Vec3 forwardPos, final Vec3 rotationVector) {
    }

    /**
     * Gets the multiplier that determines how much this move breaks blocks.
     * @return how much this move breaks blocks.
     */
    protected float getBlockDestructionMultiplier(A attacker) {
        return getDamage() / 2.5f;
    }

    /**
     * Spawns shockwaves for the given attacker and user.
     * Can be overridden to change shockwave behavior.
     * @param attacker The attacker
     * @param user The user of the attacker
     */
    protected void createShockwaves(A attacker, LivingEntity user) {
        LivingEntity attackerEntity = attacker.getBaseEntity();
        Vec3 shockwavePos = attackerEntity.position();
        shockwavePos = shockwavePos.add(attackerEntity.getLookAngle());
        shockwavePos = shockwavePos.add(RotationUtil.vecPlayerToWorld(new Vec3(0, attackerEntity.getBbHeight() / 2.0 - offset, 0), GravityChangerAPI.getGravityDirection(user)));
        JComponentPlatformUtils.getShockwaveHandler(attacker.getEntityWorld())
                .addShockwave(shockwavePos, attackerEntity.getLookAngle(), damage / 2.5f);
    }

    /**
     * Calculates the boxes for this attack.
     * Called in {@link AbstractMove#perform(IAttacker, LivingEntity)}
     *
     * @param attacker The attacker that invoked this attack
     * @param user     The user of the attacker
     * @param rotVec   The rotation vector of the attacker
     * @param upVec    The up-facing vector
     * @param hPos     The offset height position
     * @param fPos     The offset forward position
     * @return All boxes that should be attacked
     */
    protected Set<AABB> calculateBoxes(final A attacker, final LivingEntity user, final Vec3 rotVec, final Vec3 upVec, final Vec3 hPos, final Vec3 fPos) {
        if (hitboxSize <= 0 && extraHitBoxes.isEmpty()) {
            return Set.of();
        }

        Set<AABB> boxes = new HashSet<>();
        boxes.add(createBox(fPos, hitboxSize));
        extraHitBoxes.forEach(hitBox -> boxes.add(createBox(hPos, rotVec, upVec, hitBox)));

        return boxes;
    }

    /**
     * Performs this attack on the given boxes.
     *
     * @param attacker     The attacker that will be performing this attack.
     * @param boxes        The boxes in which to search for targets.
     * @param damageSource The damage source to use when applying damage to the targets.
     * @param center       The center of this attack. This is where the particle will be spawned at.
     * @return A set of all affected targets.
     */
    protected final Set<LivingEntity> attackBoxes(final A attacker, final Set<AABB> boxes, final DamageSource damageSource, final Vec3 center) {
        JUtils.displayHitboxes(attacker.getEntityWorld(), boxes);

        Set<LivingEntity> targets = findHits(attacker, boxes, damageSource, mayHitUser);
        if (targets.isEmpty()) {
            // Break blocks
            breakBlocks(attacker, boxes);

            return Set.of();
        }

        targets = validateTargets(attacker, targets);
        final ServerLevel serverWorld = (ServerLevel) attacker.getEntityWorld();

        // Particles
        final RandomSource random = RandomSource.create();
        boolean anyHit = false;

        // Process targets
        final Vec3 rotVec = getRotVec(attacker);
        final Vec3 kbVec = rotVec.scale(knockback).add(new Vec3(0.0, Math.abs(knockback) / 4, 0.0));
        for (LivingEntity target : targets) {
            final Vec3 pos = target.position().add(GravityChangerAPI.getEyeOffset(target).scale(0.65)).subtract(rotVec.scale(0.65));
            final boolean blocking = JUtils.isBlocking(target);
            if (blocking) {
                JCraft.createHitsparks(serverWorld, pos.x(), pos.y(), pos.z(), JParticleType.BLOCK_SPARK, 3, 0);
            } else {
                JCraft.createHitsparks(serverWorld, pos.x(), pos.y(), pos.z(), JParticleType.PIXEL, 2 + (int) damage * 2, 0.5);

                JCraft.createParticle(serverWorld,
                        pos.x + random.nextGaussian() * 0.25,
                        pos.y + random.nextGaussian() * 0.25,
                        pos.z + random.nextGaussian() * 0.25,
                        hitSpark);
            }

            processTarget(attacker, target, kbVec, damageSource);
        }

        return targets;
    }

    private void breakBlocks(final A attacker, final Set<AABB> boxes) {
        Level level = attacker.getEntityWorld();

        Object2FloatMap<BlockPos> breakages = boxes.stream()
                .flatMap(box -> BlockPos.betweenClosedStream(box)
                        .map(BlockPos::new) // make an immutable copy
                        .filter(p -> mayBreak(attacker.getUser(), p)) // check if we can break this block
                        // Map each block pos to a breakage
                        .map(p -> ObjectFloatPair.of(new BlockPos(p), getBreakage(attacker, level, p, box)))
                )
                // Collect the block positions and their breakage into a map
                .collect(Collectors.toMap(Pair::left, ObjectFloatPair::rightFloat,
                        (f1, f2) -> f1, Object2FloatOpenHashMap::new));

        // Send the breakages, treat block attack as coming from the user.
        JBlockBreaker.setBreakState(level, attacker.getUser(), breakages, attacker.getUser().position());
    }

    protected float getBreakage(A attacker, Level level, BlockPos pos, AABB box) {
        double distance = pos.getCenter().distanceTo(box.getCenter());
        double mult = Mth.clamp(1.5 - distance, 0, 1);
        mult *= mult;

        float strength = getBlockDestructionMultiplier(attacker) * JServerConfig.BLOCK_BREAKAGE_MULTIPLIER.getValue();
        return (float) (JBlockBreaker.calcBreakage(level, pos, strength) * mult);
    }

    /**
     * Gets called for every target hit by {@link #attackBoxes(IAttacker, Set, DamageSource, Vec3)}.
     *
     * @param attacker     The attacker that performed this
     * @param target       The target to process
     * @param kbVec        The knockback vector to pass to {@link net.arna.jcraft.api.Attacks#damageLogic(Level, LivingEntity, AttackData)}
     * @param damageSource The damage source to apply damage with
     */
    protected void processTarget(final A attacker, final LivingEntity target, final Vec3 kbVec, final DamageSource damageSource) {
        damageLogic(
                attacker.getEntityWorld(),
                target,
                new AttackData(
                    kbVec, stun, stunType.ordinal(), overrideStun,
                    damage, lift, getBlockStun(), damageSource, attacker.getUserOrThrow(),
                    hitAnimation, attacker.getMoveUsage(), canBackstab, blockableType.isNonBlockable(), true
                )
        );
    }

    protected Set<LivingEntity> validateTargets(final A attacker, final Set<LivingEntity> targets) {
        targets.removeIf(target -> !target.isAlive());
        return targets;
    }

    protected Vec3 getOffsetForwardPos(final A attacker, final Vec3 offsetHeightPos, final Vec3 upVec, final Vec3 rotVec) {
        return offsetHeightPos.add(rotVec.scale(getMoveDistance())).add(upVec.scale(-offset));
    }

    @Override
    protected @NonNull T copyExtras(final @NonNull T base) {
        AbstractSimpleAttack<T, A> cast = super.copyExtras(base);
        cast.extraHitBoxes.addAll(extraHitBoxes);
        cast.stunType = stunType;
        cast.overrideStun = overrideStun;
        cast.lift = lift;
        cast.canBackstab = canBackstab;
        cast.doShockwaves = doShockwaves;
        cast.blockStun = blockStun;
        cast.staticY = staticY;
        cast.blockableType = blockableType;
        cast.hitSpark = hitSpark;
        cast.hitAnimation = hitAnimation;
        return base;
    }

    @FunctionalInterface
    public interface TargetProcessor<A extends IAttacker<? extends A, ?>> {
        void processTarget(A attacker, LivingEntity target, Vec3 kbVec, DamageSource damageSource, boolean blocking);
    }

    protected abstract static class Type<M extends AbstractSimpleAttack<? extends M, ?>> extends AbstractMove.Type<M> {
        protected RecordCodecBuilder<M, AttackMoveExtras> attackExtras() {
            return AttackMoveExtras.CODEC.fieldOf("attack_extras").forGetter(AbstractSimpleAttack::getAttackExtras);
        }

        protected RecordCodecBuilder<M, Float> damage() {
            return Codec.FLOAT.fieldOf("damage").forGetter(AbstractSimpleAttack::getDamage);
        }

        protected RecordCodecBuilder<M, Integer> stun() {
            return Codec.INT.fieldOf("stun").forGetter(AbstractSimpleAttack::getStun);
        }

        protected RecordCodecBuilder<M, Float> hitboxSize() {
            return Codec.FLOAT.fieldOf("hitbox_size").forGetter(AbstractSimpleAttack::getHitboxSize);
        }

        protected RecordCodecBuilder<M, Float> knockback() {
            return Codec.FLOAT.fieldOf("knockback").forGetter(AbstractSimpleAttack::getKnockback);
        }

        protected RecordCodecBuilder<M, Float> offset() {
            return Codec.FLOAT.fieldOf("offset").forGetter(AbstractSimpleAttack::getOffset);
        }

        protected BiFunction<BaseMoveExtras, AttackMoveExtras, M> applyAttackExtras(Supplier<M> function) {
            return (extras, attackExtras) ->
                    attackExtras.apply(extras.apply(function.get()));
        }

        protected <T1> Function3<BaseMoveExtras, AttackMoveExtras, T1, M> applyAttackExtras(Function<T1, M> function) {
            return (extras, attackExtras, t1) ->
                    attackExtras.apply(extras.apply(function.apply(t1)));
        }

        protected <T1, T2> Function4<BaseMoveExtras, AttackMoveExtras, T1, T2, M>
        applyAttackExtras(BiFunction<T1, T2, M> function) {
            return (extras, attackExtras, t1, t2) ->
                    attackExtras.apply(extras.apply(function.apply(t1, t2)));
        }

        protected <T1, T2, T3> Function5<BaseMoveExtras, AttackMoveExtras, T1, T2, T3, M>
        applyAttackExtras(Function3<T1, T2, T3, M> function) {
            return (extras, attackExtras, t1, t2, t3) ->
                    attackExtras.apply(extras.apply(function.apply(t1, t2, t3)));
        }

        protected <T1, T2, T3, T4> Function6<BaseMoveExtras, AttackMoveExtras, T1, T2, T3, T4, M>
        applyAttackExtras(Function4<T1, T2, T3, T4, M> function) {
            return (extras, attackExtras, t1, t2, t3, t4) ->
                    attackExtras.apply(extras.apply(function.apply(t1, t2, t3, t4)));
        }

        protected <T1, T2, T3, T4, T5> Function7<BaseMoveExtras, AttackMoveExtras, T1, T2, T3, T4, T5, M>
        applyAttackExtras(Function5<T1, T2, T3, T4, T5, M> function) {
            return (extras, attackExtras, t1, t2, t3, t4, t5) ->
                    attackExtras.apply(extras.apply(function.apply(t1, t2, t3, t4, t5)));
        }

        protected <T1, T2, T3, T4, T5, T6> Function8<BaseMoveExtras, AttackMoveExtras, T1, T2, T3, T4, T5, T6, M>
        applyAttackExtras(Function6<T1, T2, T3, T4, T5, T6, M> function) {
            return (extras, attackExtras, t1, t2, t3, t4, t5, t6) ->
                    attackExtras.apply(extras.apply(function.apply(t1, t2, t3, t4, t5, t6)));
        }

        protected <T1, T2, T3, T4, T5, T6, T7> Function9<BaseMoveExtras, AttackMoveExtras, T1, T2, T3, T4, T5, T6, T7, M>
        applyAttackExtras(Function7<T1, T2, T3, T4, T5, T6, T7, M> function) {
            return (extras, attackExtras, t1, t2, t3, t4, t5, t6,
                    t7) -> attackExtras.apply(extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7)));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8> Function10<BaseMoveExtras, AttackMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, M>
        applyAttackExtras(Function8<T1, T2, T3, T4, T5, T6, T7, T8, M> function) {
            return (extras, attackExtras, t1, t2, t3, t4, t5, t6,
                    t7, t8) -> attackExtras.apply(extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8)));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9> Function11<BaseMoveExtras, AttackMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, M>
        applyAttackExtras(Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, M> function) {
            return (extras, attackExtras, t1, t2, t3, t4, t5, t6,
                    t7, t8, t9) ->
                    attackExtras.apply(extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9)));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>
        Function12<BaseMoveExtras, AttackMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, M>
        applyAttackExtras(Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, M> function) {
            return (extras, attackExtras, t1, t2, t3, t4, t5, t6,
                    t7, t8, t9, t10) ->
                    attackExtras.apply(extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10)));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>
        Function13<BaseMoveExtras, AttackMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, M>
        applyAttackExtras(Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, M> function) {
            return (extras, attackExtras, t1, t2, t3, t4, t5, t6,
                    t7, t8, t9, t10, t11) ->
                    attackExtras.apply(extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11)));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>
        Function14<BaseMoveExtras, AttackMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, M>
        applyAttackExtras(Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, M> function) {
            return (extras, attackExtras, t1, t2, t3, t4, t5, t6,
                    t7, t8, t9, t10, t11, t12) ->
                    attackExtras.apply(extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12)));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>
        Function15<BaseMoveExtras, AttackMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, M>
        applyAttackExtras(Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, M> function) {
            return (extras, attackExtras, t1, t2, t3, t4, t5, t6,
                    t7, t8, t9, t10, t11, t12, t13) ->
                    attackExtras.apply(extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13)));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>
        Function16<BaseMoveExtras, AttackMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, M>
        applyAttackExtras(Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, M> function) {
            return (extras, attackExtras, t1, t2, t3, t4, t5, t6,
                    t7, t8, t9, t10, t11, t12, t13, t14) ->
                    attackExtras.apply(extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14)));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>
        ExtraProducts.P17.Function17<BaseMoveExtras, AttackMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, M>
        applyAttackExtras(Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, M> function) {
            return (extras, attackExtras, t1, t2, t3, t4, t5, t6,
                    t7, t8, t9, t10, t11, t12, t13, t14, t15) ->
                    attackExtras.apply(extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15)));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>
        ExtraProducts.P18.Function18<BaseMoveExtras, AttackMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, M>
        applyAttackExtras(Function16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, M> function) {
            return (extras, attackExtras, t1, t2, t3, t4, t5, t6,
                    t7, t8, t9, t10, t11, t12, t13, t14, t15, t16) ->
                    attackExtras.apply(extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16)));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17>
        ExtraProducts.P19.Function19<BaseMoveExtras, AttackMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, M>
        applyAttackExtras(ExtraProducts.P17.Function17<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, M> function) {
            return (extras, attackExtras, t1, t2, t3, t4, t5, t6,
                    t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17) ->
                    attackExtras.apply(extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17)));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18>
        ExtraProducts.P20.Function20<BaseMoveExtras, AttackMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, M>
        applyAttackExtras(ExtraProducts.P18.Function18<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, M> function) {
            return (extras, attackExtras, t1, t2, t3, t4, t5, t6,
                    t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18) ->
                    attackExtras.apply(extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18)));
        }

        /**
         * Creates the default attack codec base.
         * Can be used as a base to extend upon or as a standalone codec.
         * (see {@link #attackDefault(RecordCodecBuilder.Instance, Function9)}).
         * @param instance The instance to create the codec with
         * @return The default attack codec base
         */
        protected Products.P11<RecordCodecBuilder.Mu<M>, BaseMoveExtras, AttackMoveExtras, Integer, Integer, Integer, Float, Float, Integer, Float, Float, Float>
        attackDefault(RecordCodecBuilder.Instance<M> instance) {
            return instance.group(extras(), attackExtras(), cooldown(), windup(), duration(), moveDistance(), damage(),
                    stun(), hitboxSize(), knockback(), offset());
        }

        /**
         * Creates the default attack codec.
         * Can be used directly as a return value of {@link #buildCodec(RecordCodecBuilder.Instance)}
         * @param instance The instance to create the codec with
         * @param function The constructor function used to create a new instance of the move
         * @return The default attack codec
         */
        protected App<RecordCodecBuilder.Mu<M>, M> attackDefault(RecordCodecBuilder.Instance<M> instance, Function9<Integer,
                Integer, Integer, Float, Float, Integer, Float, Float, Float, M> function) {
            return attackDefault(instance).apply(instance, applyAttackExtras(function));
        }
    }
}
