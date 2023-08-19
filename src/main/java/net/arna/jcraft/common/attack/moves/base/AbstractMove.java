package net.arna.jcraft.common.attack.moves.base;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.MobilityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
public abstract class AbstractMove<T extends AbstractMove<T, S>, S extends StandEntity<?, ?>> {
    private final int cooldown, windup;
    private final int duration;
    private final float moveDistance;
    private final List<SoundEvent> sounds = new ArrayList<>(), impactSounds = new ArrayList<>();
    private Text name, description;
    private AbstractMove<?, ? super S> crouchingVariant, followUp;
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

    public T withCrouchingVariant(AbstractMove<?, ? super S> crouchingVariant) {
        this.crouchingVariant = crouchingVariant;
        return getThis();
    }

    /**
     * Sets the move that will be initiated after this move is performed.
     * @param followUp The move that will be initiated after this move is performed.
     * @return This move
     */
    public T withFollowUp(AbstractMove<?, ? super S> followUp) {
        this.followUp = followUp;
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
    public T withUnbreakable() {
        return withArmor(Integer.MAX_VALUE);
    }

    // Logic methods

    /**
     * Called every tick so long as this move is active.
     * Called separately for each stand.
     * Invokes the {@link #perform(StandEntity, LivingEntity, MoveContext)} method if {@link #shouldPerform(StandEntity)}
     * returns {@code true} by default and plays the sound, but can be overridden to do whatever you want it to.
     * @param stand The stand to tick for.
     */
    public void tick(S stand) {
        // Play the sound(s) in the first tick.
        if (sounds != null && stand.getMoveStun() == getDuration())
            sounds.forEach(sound -> stand.playSound(sound, 1f, 1f));

        if (shouldPerform(stand))
            perform(stand, stand.getUserOrThrow(), stand.getMoveContext());
    }

    /**
     * Returns whether {@link #perform(StandEntity, LivingEntity, MoveContext)} should be called this tick.
     * @param stand The stand to check for.
     * @return Whether this move should be performed this tick.
     */
    protected boolean shouldPerform(S stand) {
        return stand.getMoveStun() == duration - windup && stand.hasUser();
    }

    /**
     * Performs this move.
     * @param stand The stand that will be performing this move.
     * @param user The user of the stand. Will never be null.
     * @param ctx The move context in which to store data.
     * @return A set of all targeted entities.
     */
    public abstract @NonNull Set<LivingEntity> perform(S stand, LivingEntity user, MoveContext ctx);

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
    public int getBlow(S stand) {
        return 0;
    }

    // Utility methods

    /**
     * Returns whether the windup has passed.
     * @param stand The stand to check for
     * @return Whether the windup has passed
     */
    public boolean hasWindupPassed(StandEntity<?, ?> stand) {
        return stand.getMoveStun() <= duration - windup;
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
     * Acquires the rotation vector for the given stand, taking gravity into account.
     * @param stand The stand to get the rotation vector for
     * @return The rotation vector for the given stand
     */
    protected Vec3d getRotVec(S stand) {
        Vec3d rotVec = stand.getRotationVector();
        if (getGravDir(stand.getUserOrThrow()) == Direction.UP)
            rotVec = new Vec3d(rotVec.x, -rotVec.y, rotVec.z);

        return rotVec;
    }

    /**
     * Acquires the position of the stand's eyes while taking the gravity of the user into account.
     * @param stand The stand to get the eye position for
     * @return The eye position of the given stand
     */
    protected Vec3d getOffsetHeightPos(S stand) {
        Vec3d upVec = GravityChangerAPI.getEyeOffset(stand.getUserOrThrow());
        Vec3d heightOffset = upVec.multiply(0.5);
        return stand.getPos().add(heightOffset);
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
    protected abstract T getThis();
}
