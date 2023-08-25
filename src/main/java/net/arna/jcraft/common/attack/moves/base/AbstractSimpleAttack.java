package net.arna.jcraft.common.attack.moves.base;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.BlockableType;
import net.arna.jcraft.common.attack.core.HitBoxData;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.StunType;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An attack with just one hit box.
 * Can be extended to support all kinds of box attacks.
 * Moves that don't attack (i.e. don't have a hitbox) such as time-stop or dim-hop,
 * should probably not extend this. Anything else probably should.
 * @param <T>
 * @param <A>
 */
@SuppressWarnings("unused")
@Getter
public abstract class AbstractSimpleAttack<T extends AbstractSimpleAttack<T, A>, A extends IAttacker<?, ?>> extends AbstractMove<T, A> {
    private final List<TargetProcessor<? super A>> targetProcessors = new ArrayList<>();
    private final Set<HitBoxData> extraHitBoxes = new HashSet<>();
    private float damage;
    private StunType stunType = StunType.BURSTABLE;
    private int stun;
    private float hitboxSize;
    private float knockback;
    private float offset;
    private boolean overrideStun;
    private boolean lift = true, canBackstab = true;
    private int blockStun = -1;
    private BlockableType blockableType = BlockableType.BLOCKABLE;
    protected JParticleType hitSpark = JParticleType.HIT_SPARK_1;

    protected AbstractSimpleAttack(int cooldown, int windup, int duration, float moveDistance, float damage,
                                   int stun, float hitboxSize, float knockback, float offset) {
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
     * @param damage The damage of this attack
     * @return This attack
     */
    public T withDamage(float damage) {
        this.damage = damage;
        return getThis();
    }

    /**
     * Sets the hitbox size of this attack.
     * Should be set using the constructor. This is only to modify copies.
     * @param hitboxSize The hitbox size of this attack
     * @return This attack
     */
    public T withHitboxSize(float hitboxSize) {
        this.hitboxSize = hitboxSize;
        return getThis();
    }

    /**
     * Sets the knockback of this attack.
     * Should be set using the constructor. This is only to modify copies.
     * @param knockback The knockback of this attack
     * @return This attack
     */
    public T withKnockback(float knockback) {
        this.knockback = knockback;
        return getThis();
    }

    /**
     * Sets the offset of this attack.
     * Should be set using the constructor. This is only to modify copies.
     * @param offset The offset of this attack
     * @return This attack
     */
    public T withOffset(float offset) {
        this.offset = offset;
        return getThis();
    }

    /**
     * Sets the stun of this attack.
     * Should be set using the constructor. This is only to modify copies.
     * @param stun The stun of this attack
     * @return This attack
     */
    public T withStun(int stun) {
        this.stun = stun;
        return getThis();
    }

    /**
     * Sets the type to stun the target with.
     * @param type The type of stun to apply
     * @return This attack
     */
    public T withStunType(StunType type) {
        this.stunType = type;
        return getThis();
    }

    /**
     * Sets that the current stun should be removed from targets when applying stun.
     * Defaults to {@code false}.
     * @see #withOverrideStun(boolean)
     * @return This attack
     */
    public T withOverrideStun() {
        return withOverrideStun(true);
    }

    /**
     * Sets whether the current stun should be removed from targets when applying stun.
     * Defaults to {@code false}.
     * @return This attack
     */
    public T withOverrideStun(boolean overrideStun) {
        this.overrideStun = overrideStun;
        return getThis();
    }

    /**
     * Sets whether targets should remain stuck in the air while this attack is active.
     * Defaults to {@code true}
     * @param lift The new value of {@code lift}
     * @return This attack
     */
    public T withLift(boolean lift) {
        this.lift = lift;
        return getThis();
    }

    /**
     * Sets whether the attack can backstab.
     * Defaults to {@code true}.
     * @param canBackstab Whether the attack can backstab
     * @return This attack
     */
    public T withBackstab(boolean canBackstab) {
        this.canBackstab = canBackstab;
        return getThis();
    }

    /**
     * Sets the stun applied to the user when this attack is performed on a target that is blocking.
     * A positive value implies that the default calculation of {@code damage + 4} should be overridden
     * by the value passed here.
     * @param blockStun The amount of ticks to stun for
     * @return This attack
     */
    public T withBlockStun(int blockStun) {
        this.blockStun = blockStun;
        return getThis();
    }

    /**
     * Sets the blockable type of this attack.
     * Defaults to {@link BlockableType#BLOCKABLE BLOCKABLE}.
     * @param blockableType The new blockable type
     * @return This attack
     */
    public T withBlockableType(@NonNull BlockableType blockableType) {
        this.blockableType = blockableType;
        return getThis();
    }

    /**
     * Adds an extra hitbox with the given size to use with every attack
     * along with the main hitbox.
     * @param size The size of the hitbox
     * @see #withExtraHitBox(double, double, double)
     * @return This attack
     */
    public T withExtraHitBox(double size) {
        return withExtraHitBox(new HitBoxData(size));
    }

    /**
     * Adds an extra hitbox with the given size and offsets to use with every attack
     * along with the main hitbox.
     * @param forwardOffset The forward offset of the hitbox
     * @param verticalOffset The vertical offset of the hitbox
     * @param size The size of the hitbox
     * @return This attack
     */
    public T withExtraHitBox(double forwardOffset, double verticalOffset, double size) {
        return withExtraHitBox(new HitBoxData(forwardOffset, verticalOffset, size));
    }

    /**
     * Adds an extra hitbox to use with every attack along with the main hitbox.
     * @param hitBox The hitbox to add
     * @return This attack
     */
    public T withExtraHitBox(HitBoxData hitBox) {
        extraHitBoxes.add(hitBox);
        return getThis();
    }

    /**
     * Marks this attack as a launch attack.
     * @return This attack
     */
    public T withLaunch() {
        stunType = StunType.LAUNCH;
        overrideStun = true;
        return getThis();
    }

    /**
     * Sets the hit spark particle this attack will use when it hits something.
     * @param particle The hit spark particle to use
     * @return This attack
     */
    public T withHitSpark(JParticleType particle) {
        hitSpark = particle;
        return getThis();
    }

    /**
     * Adds a new target processor to this attack.
     * @param targetProcessor The target processor to add
     * @return This attack
     */
    public T withTargetProcessor(TargetProcessor<? super A> targetProcessor) {
        targetProcessors.add(targetProcessor);
        return getThis();
    }

    public int getBlockStun() {
        return blockStun < 0 ? (int) (damage + 4) : blockStun;
    }

    // Utility methods
    public static Box createBox(Vec3d center, double size) {
        double axisSize = size / 2;

        Vec3d min = center.subtract(axisSize, axisSize, axisSize);
        Vec3d max = center.add(axisSize, axisSize, axisSize);
        return new Box(min, max);
    }

    public static Box createBox(Vec3d offsetHeightPos, Vec3d rotVec, Vec3d upVec, HitBoxData data) {
        return createBox(offsetHeightPos.add(rotVec.multiply(data.forwardOffset()))
                .add(upVec.multiply(data.verticalOffset())), data.size());
    }

    /**
     * Finds all valid targets that can be damaged with the given damage source
     * by the given attacker, contained in the given boxes.
     * Also maps all attackers found to their user. I.e. redirecting damage done to attackers to their users.
     * @param attacker The attacker that will be doing the damage
     * @param boxCenter The center of the box to check in
     * @param boxSize The size of the box to check in
     * @param damageSource The damage source to check for
     * @return All found valid targets
     */
    public static Set<LivingEntity> findHits(IAttacker<?, ?> attacker, Vec3d boxCenter, double boxSize, @Nullable DamageSource damageSource) {
        return findHits(attacker, createBox(boxCenter, boxSize), damageSource);
    }

    /**
     * Finds all valid targets that can be damaged with the given damage source
     * by the given attacker, contained in the given boxes.
     * Also maps all attackers found to their user. I.e. redirecting damage done to attackers to their users.
     * @param attacker The attacker that will be doing the damage
     * @param box The box to check in
     * @param damageSource The damage source to check for
     * @return All found valid targets
     */
    public static Set<LivingEntity> findHits(IAttacker<?, ?> attacker, Box box, @Nullable DamageSource damageSource) {
        return findHits(attacker, Set.of(box), damageSource);
    }

    /**
     * Finds all valid targets that can be damaged with the given damage source
     * by the given attacker, contained in the given boxes.
     * Also maps all attackers found to their user. I.e. redirecting damage done to attackers to their users.
     * @param attacker The attacker that will be doing the damage
     * @param boxes The boxes to check in
     * @param damageSource The damage source to check for
     * @return All found valid targets
     */
    public static Set<LivingEntity> findHits(IAttacker<?, ?> attacker, Set<Box> boxes, @Nullable DamageSource damageSource) {
        return findHits(attacker, boxes, damageSource, LivingEntity.class);
    }

    /**
     * Finds all valid targets that can be damaged with the given damage source
     * by the given attacker, contained in the given boxes.
     * Also maps all attackers found to their user. I.e. redirecting damage done to stands to their users.
     * @param attacker The attacker that will be doing the damage
     * @param boxes The boxes to check in
     * @param damageSource The damage source to check for
     * @param type The type of entities to look for
     * @return All found valid targets
     */
    public static <T extends Entity> @NonNull Set<T> findHits(IAttacker<?, ?> attacker, @NonNull Set<Box> boxes,
                                                     @Nullable DamageSource damageSource, Class<T> type) {
        return boxes.stream()
                .flatMap(box -> attacker.getWorld().getEntitiesByClass(type, box, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e ->
                        e != attacker && e != attacker.getUser() && e != attacker.getUserOrThrow().getVehicle())).stream())
                .flatMap(e -> e instanceof StandEntity<?,?> hitStand && hitStand.hasUser() &&
                        type.isInstance(hitStand.getUserOrThrow()) ? Stream.of(e, type.cast(hitStand.getUserOrThrow())) : Stream.of(e))
                .filter(e -> damageSource == null || JUtils.canDamage(damageSource, e)) // This must be done after the previous flatmap call as it excludes stands.
                .collect(Collectors.toSet());
    }

    // Logic methods
    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        Vec3d upVec = GravityChangerAPI.getEyeOffset(user);
        Vec3d hPos = getOffsetHeightPos(attacker);
        Vec3d rotVec = getRotVec(attacker);

        Vec3d fPos = getOffsetForwardPos(attacker, hPos, upVec, rotVec);

        Set<Box> boxes = calculateBoxes(attacker, user, rotVec, upVec, hPos, fPos);
        DamageSource damageSource = attacker.getDamageSource();
        return attackBoxes(attacker, boxes, damageSource, fPos);
    }

    /**
     * Calculates the boxes for this attack.
     * Called in {@link #perform(IAttacker, LivingEntity, MoveContext)}
     *
     * @param attacker The attacker that invoked this attack
     * @param user     The user of the attacker
     * @param rotVec   The rotation vector of the attacker
     * @param upVec    The up-facing vector
     * @param hPos     The offset height position
     * @param fPos     The offset forward position
     * @return All boxes that should be attacked
     */
    protected Set<Box> calculateBoxes(A attacker, LivingEntity user, Vec3d rotVec, Vec3d upVec, Vec3d hPos, Vec3d fPos) {
        if (hitboxSize <= 0 && extraHitBoxes.isEmpty()) return Set.of();

        Set<Box> boxes = new HashSet<>();
        boxes.add(createBox(fPos, hitboxSize));
        extraHitBoxes.forEach(hitBox -> boxes.add(createBox(hPos, rotVec, upVec, hitBox)));

        return boxes;
    }

    /**
     * Performs this attack on the given boxes.
     * @param attacker The attacker that will be performing this attack.
     * @param boxes The boxes in which to search for targets.
     * @param damageSource The damage source to use when applying damage to the targets.
     * @param center The center of this attack. This is where the particle will be spawned at.
     * @return A set of all affected targets.
     */
    protected final Set<LivingEntity> attackBoxes(A attacker, Set<Box> boxes, DamageSource damageSource, Vec3d center) {
        JUtils.displayHitboxes(attacker.getWorld(), boxes);

        Set<LivingEntity> targets = findHits(attacker, boxes, damageSource);
        if (targets.isEmpty()) return Set.of();

        Random random = Random.create();
        JCraft.createParticle((ServerWorld) attacker.getWorld(),
                center.x + random.nextGaussian() * 0.25,
                center.y + random.nextGaussian() * 0.25,
                center.z + random.nextGaussian() * 0.25,
                hitSpark);

        getImpactSounds().forEach(sound -> attacker.playSound(sound, 1f, 1f));
        if (getDamage() <= 0) return targets;

        Vec3d kbVec = getRotVec(attacker).multiply(knockback).add(new Vec3d(0.0, Math.abs(knockback) / 4, 0.0));
        for (LivingEntity target : validateTargets(attacker, targets)) {
            targetProcessors.forEach(processor -> processor.processTarget(attacker, target, kbVec, damageSource));
            processTarget(attacker, target, kbVec, damageSource);
        }

        return targets;
    }

    /**
     * Gets called for every target hit by {@link #attackBoxes(IAttacker, Set, DamageSource, Vec3d)}.
     * @param attacker The attacker that performed this
     * @param target The target to process
     * @param kbVec The knockback vector to pass to {@link StandEntity#damageLogic(World, LivingEntity, Vec3d, int, int,
     * boolean, float, boolean, int, DamageSource, Entity, boolean, boolean)}
     * @param damageSource The damage source to apply damage with
     */
    protected void processTarget(A attacker, LivingEntity target, Vec3d kbVec, DamageSource damageSource) {
        StandEntity.damageLogic(attacker.getWorld(), target, kbVec, stun, stunType.ordinal(), overrideStun,
                damage, lift, blockStun, damageSource, attacker.getUserOrThrow(), canBackstab, blockableType.isNonBlockable());
    }

    protected Set<LivingEntity> validateTargets(A attacker, Set<LivingEntity> targets) {
        return targets;
    }

    protected Vec3d getOffsetForwardPos(A attacker, Vec3d offsetHeightPos, Vec3d upVec, Vec3d rotVec) {
        return offsetHeightPos.add(rotVec.multiply(getMoveDistance())).add(upVec.multiply(-offset));
    }

    @Override
    protected @NonNull T copyExtras(@NonNull T base) {
        AbstractSimpleAttack<T, A> cast = super.copyExtras(base);
        cast.targetProcessors.addAll(targetProcessors);
        cast.stunType = stunType;
        cast.overrideStun = overrideStun;
        cast.lift = lift;
        cast.canBackstab = canBackstab;
        cast.blockStun = blockStun;
        cast.blockableType = blockableType;
        cast.hitSpark = hitSpark;
        return base;
    }

    @FunctionalInterface
    public interface TargetProcessor<A extends IAttacker<?, ?>> {
        void processTarget(A attacker, LivingEntity target, Vec3d kbVec, DamageSource damageSource);
    }
}
