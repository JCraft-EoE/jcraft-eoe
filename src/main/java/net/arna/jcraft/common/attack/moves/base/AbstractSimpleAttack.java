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
@Getter
public abstract class AbstractSimpleAttack<T extends AbstractSimpleAttack<T, S>, S extends StandEntity<?, ?>> extends AbstractMove<T, S> {
    private final float damage;
    private final float hitBoxSize;
    private final float knockBack;
    private final float offset;
    private final Set<HitBoxData> extraHitBoxes = new HashSet<>();
    private StunType stunType = StunType.BURSTABLE;
    private int stun = 0;
    private boolean overrideStun;
    private boolean lift = true, canBackStab = true;
    private int blockStun;
    private BlockableType blockableType = BlockableType.BLOCKABLE;
    protected JParticleType hitSpark = JParticleType.HIT_SPARK_1;

    protected AbstractSimpleAttack(int cooldown, int windup, int duration, float attackDistance, float damage,
                                   int stun, float hitBoxSize, float knockBack, float offset) {
        super(cooldown, windup, duration, attackDistance);
        this.damage = damage;
        this.stun = stun;
        this.hitBoxSize = hitBoxSize;
        this.knockBack = knockBack;
        this.offset = offset;
        blockStun = (int) (damage + 4);
    }

    // Properties alteration methods

    /**
     * Stuns the targets, so they can no longer attack and/or move for a set period of time.
     * Defaults to {@code 0}.
     * @param stunTicks The duration for which targets should be stunned in ticks.
     * @see #withStun(StunType, int)
     * @return This attack
     */
    public T withStun(int stunTicks) {
        return withStun(StunType.BURSTABLE, stunTicks);
    }

    /**
     * Stuns the targets with a given type, so they can no longer attack and/or move for a set period of time.
     * Defaults to {@code 0}.
     * @param stunTicks The duration for which targets should be stunned in ticks.
     * @param type The type of stun to apply
     * @return This attack
     */
    public T withStun(StunType type, int stunTicks) {
        this.stunType = type;
        this.stun = stunTicks;
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
     * Sets whether the user can be back-stabbed while performing this attack.
     * Defaults to {@code true}.
     * @param canBackStab Whether the user can be back-stabbed
     * @return This attack
     */
    public T withBackStab(boolean canBackStab) {
        this.canBackStab = canBackStab;
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
     * Adds an extra hit-box with the given size to use with every attack
     * along with the main hit-box.
     * @param size The size of the hit-box
     * @see #withExtraHitBox(double, double, double)
     * @return This attack
     */
    public T withExtraHitBox(double size) {
        return withExtraHitBox(new HitBoxData(size));
    }

    /**
     * Adds an extra hit-box with the given size and offsets to use with every attack
     * along with the main hit-box.
     * @param size The size of the hit-box
     * @param forwardOffset The forward offset of the hit-box
     * @param verticalOffset The vertical offset of the hit-box
     * @param size The size of the hit-box
     * @return This attack
     */
    public T withExtraHitBox(double forwardOffset, double verticalOffset, double size) {
        return withExtraHitBox(new HitBoxData(forwardOffset, verticalOffset, size));
    }

    /**
     * Adds an extra hit-box to use with every attack along with the main hit-box.
     * @param hitBox The hit-box to add
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
    public @NonNull Set<LivingEntity> perform(S stand, LivingEntity user, MoveContext ctx) {
        if (hitBoxSize <= 0 && extraHitBoxes.isEmpty()) return Set.of();

        Vec3d upVec = GravityChangerAPI.getEyeOffset(user);
        Vec3d hPos = getOffsetHeightPos(stand);
        Vec3d rotVec = getRotVec(stand);

        Vec3d fPos = hPos.add(rotVec.multiply(getMoveDistance())).add(upVec.multiply(-offset));

        DamageSource damageSource = getDamageSource(stand);
        Set<Box> boxes = new HashSet<>();
        boxes.add(createBox(fPos, hitBoxSize));
        extraHitBoxes.forEach(hitBox -> boxes.add(createBox(hPos, rotVec, upVec, hitBox)));
        return attackBoxes(stand, boxes, damageSource, fPos);
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

        Vec3d kbVec = getRotVec(stand).multiply(knockBack).add(new Vec3d(0.0, Math.abs(knockBack) / 4, 0.0));
        for (LivingEntity target : validateTargets(stand, hurt))
            StandEntity.damageLogic(stand.world, target, kbVec, stun, stunType.ordinal(), overrideStun,
                    damage, lift, blockStun, damageSource, stand.getUserOrThrow(), canBackStab, blockableType.isNonBlockable());

        return hurt;
    }

    protected Set<LivingEntity> validateTargets(S stand, Set<LivingEntity> targets) {
        return targets;
    }

}
