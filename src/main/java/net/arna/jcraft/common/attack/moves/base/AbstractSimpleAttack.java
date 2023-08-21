package net.arna.jcraft.common.attack.moves.base;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.BlockableType;
import net.arna.jcraft.common.attack.core.HitBoxData;
import net.arna.jcraft.common.attack.core.StunType;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An attack with just one hit box.
 * Can be extended to support all kinds of box attacks.
 * Moves that don't attack (i.e. don't have a hitbox) such as time-stop or dim-hop,
 * should probably not extend this. Anything else probably should.
 * @param <T>
 * @param <S>
 */
@SuppressWarnings("unused")
@Getter
public abstract class AbstractSimpleAttack<T extends AbstractSimpleAttack<T, S>, S extends StandEntity<?, ?>> extends AbstractMove<T, S> {
    private final Set<HitBoxData> extraHitBoxes = new HashSet<>();
    private float damage;
    private float hitboxSize;
    private float knockback;
    private float offset;
    private StunType stunType = StunType.BURSTABLE;
    private int stun;
    private boolean overrideStun;
    private boolean lift = true, canBackstab = true;
    private int blockStun;
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
        blockStun = (int) (damage + 4);
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

    public T withHitSpark(JParticleType particle) {
        hitSpark = particle;
        return getThis();
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
     * by the given stand, contained in the given boxes.
     * Also maps all stands found to their user. I.e. redirecting damage done to stands to their users.
     * @param stand The stand that will be doing the damage
     * @param boxCenter The center of the box to check in
     * @param boxSize The size of the box to check in
     * @param damageSource The damage source to check for
     * @return All found valid targets
     */
    public static Set<LivingEntity> findHits(StandEntity<?, ?> stand, Vec3d boxCenter, double boxSize, DamageSource damageSource) {
        return findHits(stand, createBox(boxCenter, boxSize), damageSource);
    }

    /**
     * Finds all valid targets that can be damaged with the given damage source
     * by the given stand, contained in the given boxes.
     * Also maps all stands found to their user. I.e. redirecting damage done to stands to their users.
     * @param stand The stand that will be doing the damage
     * @param box The box to check in
     * @param damageSource The damage source to check for
     * @return All found valid targets
     */
    public static Set<LivingEntity> findHits(StandEntity<?, ?> stand, Box box, DamageSource damageSource) {
        return findHits(stand, Set.of(box), damageSource);
    }

    /**
     * Finds all valid targets that can be damaged with the given damage source
     * by the given stand, contained in the given boxes.
     * Also maps all stands found to their user. I.e. redirecting damage done to stands to their users.
     * @param stand The stand that will be doing the damage
     * @param boxes The boxes to check in
     * @param damageSource The damage source to check for
     * @return All found valid targets
     */
    public static Set<LivingEntity> findHits(StandEntity<?, ?> stand, Set<Box> boxes, DamageSource damageSource) {
        return boxes.stream()
                .flatMap(box -> stand.world.getEntitiesByClass(LivingEntity.class, box, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e ->
                        e != stand && e != stand.getUser() && e != stand.getUserOrThrow().getVehicle())).stream())
                .flatMap(e -> e instanceof StandEntity<?,?> hitStand && stand.hasUser() ? Stream.of(e, hitStand.getUserOrThrow()) : Stream.of(e))
                .filter(e -> JUtils.canDamage(damageSource, e)) // This must be done after the previous flatmap call as it excludes stands.
                .collect(Collectors.toSet());
    }

    /**
     * Gets the damage source to use when applying damage to targets.
     * @param stand The stand to get the damage source for
     * @return The damage source
     */
    protected DamageSource getDamageSource(S stand) {
        return JDamageSources.stand(stand);
    }

    // Logic methods
    @Override
    public @NonNull Set<LivingEntity> perform(S attacker, LivingEntity user, MoveContext ctx) {
        if (hitboxSize <= 0 && extraHitBoxes.isEmpty()) return Set.of();

        Vec3d upVec = GravityChangerAPI.getEyeOffset(user);
        Vec3d hPos = getOffsetHeightPos(attacker);
        Vec3d rotVec = getRotVec(attacker);

        Vec3d fPos = getOffsetForwardPos(attacker, hPos, upVec, rotVec);

        DamageSource damageSource = getDamageSource(attacker);
        Set<Box> boxes = new HashSet<>();
        boxes.add(createBox(fPos, hitboxSize));
        extraHitBoxes.forEach(hitBox -> boxes.add(createBox(hPos, rotVec, upVec, hitBox)));
        return attackBoxes(attacker, boxes, damageSource, fPos);
    }

    /**
     * Performs this attack on the given boxes.
     * @param stand The stand that will be performing this attack.
     * @param boxes The boxes in which to search for targets.
     * @param damageSource The damage source to use when applying damage to the targets.
     * @param center The center of this attack. This is where the particle will be spawned at.
     * @return A set of all affected targets.
     */
    protected Set<LivingEntity> attackBoxes(S stand, Set<Box> boxes, DamageSource damageSource, Vec3d center) {
        // TODO allow this method to send boxes in bulk.
        boxes.forEach(box -> JUtils.displayHitbox(stand.world, new Vec3d(box.minX, box.minY, box.minZ), new Vec3d(box.maxX, box.maxY, box.maxZ)));

        Set<LivingEntity> hurt = findHits(stand, boxes, damageSource);
        if (hurt.isEmpty()) return Set.of();

        Random random = stand.getRandom();
        JCraft.createParticle((ServerWorld) stand.world,
                center.x + random.nextGaussian() * 0.25,
                center.y + random.nextGaussian() * 0.25,
                center.z + random.nextGaussian() * 0.25,
                hitSpark);

        getImpactSounds().forEach(sound -> stand.playSound(sound, 1f, 1f));

        Vec3d kbVec = getRotVec(stand).multiply(knockback).add(new Vec3d(0.0, Math.abs(knockback) / 4, 0.0));
        for (LivingEntity target : validateTargets(stand, hurt))
            StandEntity.damageLogic(stand.world, target, kbVec, stun, stunType.ordinal(), overrideStun,
                    damage, lift, blockStun, damageSource, stand.getUserOrThrow(), canBackstab, blockableType.isNonBlockable());

        return hurt;
    }

    protected Set<LivingEntity> validateTargets(S stand, Set<LivingEntity> targets) {
        return targets;
    }

    protected Vec3d getOffsetForwardPos(S stand, Vec3d offsetHeightPos, Vec3d upVec, Vec3d rotVec) {
        return offsetHeightPos.add(rotVec.multiply(getMoveDistance())).add(upVec.multiply(-offset));
    }
}
