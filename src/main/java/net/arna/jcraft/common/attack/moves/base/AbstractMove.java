package net.arna.jcraft.common.attack.moves.base;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.MobilityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
public abstract class AbstractMove<T extends AbstractMove<T, A>, A extends IAttacker<?, ?>> {
    private final List<SoundEvent> sounds = new ArrayList<>(), impactSounds = new ArrayList<>();
    private MoveType moveType;
    private int cooldown, windup;
    private int duration;
    private float moveDistance;
    private Text name, description;
    private AbstractMove<?, ? super A> crouchingVariant, followUp;
    private boolean isCrouchingVariant;
    private int armor;
    protected MobilityType mobilityType;
    // Used to help AI know how and when to use this attack.
    protected boolean ranged, barrage, multiHit, charge, counter, dash, grab;

    protected AbstractMove(int cooldown, int windup, int duration, float moveDistance) {
        this.cooldown = cooldown;
        this.windup = windup;
        this.duration = duration;
        this.moveDistance = moveDistance;
    }

    // Properties alteration methods

    /**
     * Sets the cooldown of this move.
     * This is how many ticks the user has to wait to be able to use this attack again.
     * Should be set via the constructor, this is only to modify copies.
     * @param cooldown The cooldown of this move in ticks
     * @return This move
     */
    public T withCooldown(int cooldown) {
        this.cooldown = cooldown;
        return getThis();
    }

    /**
     * Sets the windup of this move.
     * This is how long it takes for the attack to perform after being initiated.
     * Should be set via the constructor, this is only to modify copies.
     * @param windup The windup of this move in ticks
     * @return This move
     */
    public T withWindup(int windup) {
        this.windup = windup;
        return getThis();
    }

    /**
     * Sets the duration of this move.
     * This is how long this attack lasts. It is also how long the user has to wait before they
     * can initiate another attack.
     * Should be set via the constructor, this is only to modify copies.
     * @param duration The duration of this move in ticks
     * @return This move
     */
    public T withDuration(int duration) {
        this.duration = duration;
        return getThis();
    }

    /**
     * Sets the move distance of this move.
     * This is how far away the stand is moved from the user when performing this move.
     * Should be set via the constructor, this is only to modify copies.
     * @param moveDistance The move distance of this move
     * @return This move
     */
    public T withMoveDistance(float moveDistance) {
        this.moveDistance = moveDistance;
        return getThis();
    }

    /**
     * Sets some information about this move displayed in commands.
     * @param name The name of this move
     * @param description The description of this move
     * @return This move
     */
    public T withInfo(Text name, Text description) {
        this.name = name;
        this.description = description;
        return getThis();
    }

    public T withCrouchingVariant(AbstractMove<?, ? super A> crouchingVariant) {
        this.crouchingVariant = crouchingVariant.copy();
        this.crouchingVariant.isCrouchingVariant = true;
        return getThis();
    }

    /**
     * Sets the move that will be initiated after this move is performed.
     * @param followUp The move that will be initiated after this move is performed.
     * @return This move
     */
    public T withFollowUp(AbstractMove<?, ? super A> followUp) {
        this.followUp = followUp.copy();
        return getThis();
    }

    /**
     * Adds a sound to play when this move is performed.
     * Can be called multiple times.
     * @param sound A sound to play when this move is performed.
     * @return This move
     */
    public T withSound(SoundEvent sound) {
        sounds.add(sound);
        return getThis();
    }

    /**
     * Adds a sound to play when this move hits something.
     * Can be called multiple times.
     * @param sound A sound to play when this move hits something.
     * @return This move
     */
    public T withImpactSound(SoundEvent sound) {
        impactSounds.add(sound);
        return getThis();
    }

    /**
     * Sets the amount of hits this attack can withstand before breaking.
     * @param armor The amount of hits this attack can withstand
     * @return This move
     */
    public T withArmor(int armor) {
        this.armor = armor;
        return getThis();
    }

    /**
     * Sets the armor value to {@link Integer#MAX_VALUE}.
     * @see #withArmor(int)
     * @return This move
     */
    public T withHyperArmor() {
        return withArmor(Integer.MAX_VALUE);
    }

    /**
     * Called when this move is registered to a {@link net.arna.jcraft.common.attack.core.MoveMap MoveMap}.
     * Not supposed to be called by anything else.
     * @param type The MoveType this move is registered as
     */
    @ApiStatus.Internal
    public void onRegister(MoveType type) {
        moveType = type;
    }

    // Logic methods

    /**
     * Called when this move is initialized.
     * By default, only plays the sound(s).
     */
    public void onInitialize(A attacker) {
        if (attacker.getMoveStun() == getDuration())
            sounds.forEach(sound -> attacker.playSound(sound, 1f, 1f));
    }

    /**
     * Called every tick so long as this move is active.
     * Called separately for each attacker.
     * Invokes the {@link #perform(IAttacker, LivingEntity, MoveContext)} method if {@link #shouldPerform(IAttacker)}
     * returns {@code true} by default, but can be overridden to do whatever you want it to.
     * @param attacker The attacker to tick for.
     */
    public void tick(A attacker) {
        if (shouldPerform(attacker))
            perform(attacker, attacker.getUserOrThrow(), attacker.getMoveContext());
    }

    /**
     * Returns whether {@link #perform(IAttacker, LivingEntity, MoveContext)} should be called this tick.
     * @param attacker The stand to check for.
     * @return Whether this move should be performed this tick.
     */
    protected boolean shouldPerform(A attacker) {
        return attacker.getMoveStun() == duration - windup && attacker.hasUser();
    }

    /**
     * Performs this move.
     * @param attacker The stand that will be performing this move.
     * @param user The user of the stand. Will never be null.
     * @param ctx The move context in which to store data.
     * @return A set of all targeted entities.
     */
    public abstract @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx);

    /**
     * Register entries in the move context of a stand to be used by this move.
     * @param ctx The context in which to register entries.
     */
    public void registerContextEntries(MoveContext ctx) {}

    /**
     * Gets the current blow this move is at.
     * For simple moves, this will always be 0.
     * For barrages or multi-hit moves, this can be greater than 0.
     * @param stand The stand to get the blow for
     * @return The current blow of this move for the given stand
     */
    public int getBlow(A stand) {
        return 0;
    }

    // Utility methods

    public LivingEntity getUser(A attacker) {
        return attacker.getUserOrThrow();//attacker instanceof StandEntity<?,?> stand ? stand.getUserOrThrow() : attacker;
    }

    /**
     * Returns the point at which the windup has passed.
     * @return The point at which the windup has passed.
     */
    public int getWindupPoint() {
        return duration - windup;
    }

    /**
     * Returns whether the windup has passed.
     * @param stand The stand to check for
     * @return Whether the windup has passed
     */
    public boolean hasWindupPassed(IAttacker<?, ?> stand) {
        return stand.getMoveStun() <= getWindupPoint();
    }

    /**
     * Gets the gravity direction for the given user.
     * @param user The user to get the gravity direction for
     * @return The gravity direction for the given user
     */
    protected Direction getGravDir(LivingEntity user) {
        return GravityChangerAPI.getGravityDirection(user);
    }

    /**
     * Acquires the rotation vector for the given attacker, taking gravity into account.
     * @param attacker The attacker to get the rotation vector for
     * @return The rotation vector for the given attacker
     */
    protected Vec3d getRotVec(A attacker) {
        LivingEntity baseEntity = attacker.getBaseEntity();
        Vec3d rotVec = baseEntity.getRotationVector();
        if (getGravDir(attacker.getUserOrThrow()) == Direction.UP)
            rotVec = new Vec3d(rotVec.x, -rotVec.y, rotVec.z);

        return rotVec;
    }

    /**
     * Acquires the position of the attacker's eyes while taking the gravity of the user into account.
     * @param attacker The attacker to get the eye position for
     * @return The eye position of the given attacker
     */
    protected Vec3d getOffsetHeightPos(A attacker) {
        Vec3d upVec = GravityChangerAPI.getEyeOffset(attacker.getUserOrThrow());
        Vec3d heightOffset = upVec.multiply(0.5);
        return attacker.getBaseEntity().getPos().add(heightOffset);
    }

    /**
     * Simply returns {@code this}. Can only be implemented by final moves.
     * This means that any intermediary move class (one that forms a base for other moves)
     * cannot implement this.
     * This also means that this cannot be overridden by subclasses.
     * This all together means that you must create an abstract class that represents your move
     * and an (empty) implementation if you wish to use this move both standalone and as a basis for other moves.
     * An example of this is {@link SimpleAttack SimpleAttack} and
     * {@link AbstractSimpleAttack}. SimpleAttack is simply an empty implementation of AbstractSimpleAttack so that
     * AbstractSimpleAttack can be used standalone while also being able to be extended by other moves.
     * @return This move
     */
    protected abstract @NonNull T getThis();

    /**
     * Creates a copy of this attack.
     * @return A copy of this attack.
     */
    public abstract @NonNull T copy();
}
