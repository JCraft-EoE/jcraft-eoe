package net.arna.jcraft.common.attack.moves.base;

import it.unimi.dsi.fastutil.ints.IntObjectPair;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.MoveInputType;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.MobilityType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Getter
public abstract class AbstractMove<T extends AbstractMove<T, A>, A extends IAttacker<? extends A, ?>> {
    private final List<SoundEvent> sounds = new ArrayList<>(), impactSounds = new ArrayList<>();
    private final List<Predicate<? super A>> conditions = new ArrayList<>();
    private final List<InitAction<? super A>> initActions = new ArrayList<>();
    private final List<MoveAction<? super A>> actions = new ArrayList<>();
    private MoveType moveType;
    private int cooldown, windup;
    private int duration;
    private float moveDistance;
    /**
     * This move's assigned animation
     */
    @Getter
    private Enum<?> animation;
    @NonNull
    private Text name = Text.empty(), description = Text.empty();
    /**
     * The move this move was copied from.
     * Defaults to {@code this}.
     */
    private T originalMove = getThis();
    private @Nullable AbstractMove<?, ? super A> crouchingVariant, aerialVariant, followup;
    private boolean isCrouchingVariant, isAerialVariant, isFollowup;
    private int armor;
    private IntObjectPair<AbstractMove<?, ? super A>> finisher;
    protected MobilityType mobilityType;
    @Getter
    private Boolean isHoldable;
    // Used to help AI know how and when to use this attack.
    protected boolean ranged, barrage, multiHit, charge, counter, dash, grab;
    protected boolean copyOnUse;
    protected boolean mayHitUser;
    private boolean copiedExtras; // See #testCopy()

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
     * Assigns an animation state to be used by the move, in case it can't be done in the movemap
     * @param state This moves animation
     * @return This move
     */
    public T withAnim(Enum<?> state) {
        this.animation = state;
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
     * This should be set via the constructor; this is only to modify copies.
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
    public T withInfo(@NonNull Text name, @NonNull Text description) {
        this.name = name;
        this.description = description;
        return getThis();
    }

    /**
     * Sets the crouching variant of this move. When invoking this move while crouching,
     * this variant is invoked instead.
     * @param crouchingVariant The crouching variant of this move.
     * @return This move
     */
    public T withCrouchingVariant(AbstractMove<?, ? super A> crouchingVariant) {
        if (isCrouchingVariant) throw new IllegalStateException("Can't assign a crouching variant to a crouching variant.");
        if (crouchingVariant.getCrouchingVariant() != null) throw new IllegalArgumentException("Given move has a " +
                "crouching variant. Crouching variants cannot have crouching variants.");

        this.crouchingVariant = crouchingVariant.copy();
        this.crouchingVariant.isCrouchingVariant = true;
        return getThis();
    }

    /**
     * Sets the aerial variant of this move. When invoking this move while in the air,
     * this variant is invoked instead.
     * @param aerialVariant The aerial variant of this move.
     * @return This move
     */
    public T withAerialVariant(AbstractMove<?, ? super A> aerialVariant) {
        if (isAerialVariant) throw new IllegalStateException("Can't assign an aerial variant to an aerial variant.");

        this.aerialVariant = aerialVariant.copy();
        this.aerialVariant.isAerialVariant = true;
        return getThis();
    }

    /**
     * Marks the move as a ranged move.
     * @return This move
     */
    public T markRanged() {
        this.ranged = true;
        return getThis();
    }

    /**
     * Allows the stand to hit its own user
     * @return This move
     */
    public T allowHitUser() {
        this.mayHitUser = true;
        return getThis();
    }

    /**
     * Sets the move that will be initiated after this move is performed.
     * @param followup The move that will be initiated after this move is performed.
     * @return This move
     */
    public T withFollowup(AbstractMove<?, ? super A> followup) {
        this.followup = followup.copy();
        this.followup.isFollowup = true;
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
     * Sets the number of hits this attack can withstand before breaking.
     * @param armor The number of hits this attack can withstand
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
     * Adds a new condition to the list of conditions.
     * All conditions must be met for a move to be allowed to be initiated.
     * If any return {@code false}, the move is not initiated.
     * @param condition The condition to add
     * @return This move
     */
    public T withCondition(Predicate<? super A> condition) {
        conditions.add(condition);
        return getThis();
    }

    /**
     * Adds a new init action to this move.
     * Init actions are called at the end of {@link #onInitiate(IAttacker)}.
     * @param action An action
     * @return This move
     */
    public T withInitAction(InitAction<? super A> action) {
        initActions.add(action);
        return getThis();
    }

    /**
     * Adds an action to this move.
     * Actions are called after {@link #perform(IAttacker, LivingEntity, MoveContext)} is called.
     * @param action An action
     * @return This move
     */
    public T withAction(MoveAction<? super A> action) {
        actions.add(action);
        return getThis();
    }

    /**
     * Sets the mobility type the Stand User AI will use to determine how to use this attack.
     * @param mobilityType The mobility type of this attack
     * @return This attack
     */
    public T withMobilityType(MobilityType mobilityType) {
        this.mobilityType = mobilityType;
        return getThis();
    }

    /**
     * Sets this move to be holdable.
     * @see #withHoldable(Boolean)
     * @return This move
     */
    public T withHoldable() {
        return withHoldable(true);
    }

    /**
     * Forces move handling to copy this move when initiated.
     */
    public T withCopyOnUse() {
        this.copyOnUse = true;
        return getThis();
    }

    public boolean shouldCopyOnUse() {
        return copyOnUse;
    }

    /**
     * Sets whether this move can be held.
     * @param holdable Whether this move can be held. {@code null} for default behavior (dependent on the move-type).
     * @return This move
     */
    public T withHoldable(Boolean holdable) {
        this.isHoldable = holdable;
        return getThis();
    }

    /**
     * Sets the move this move should finish with and when.
     * When the given tick is reached, the current move of the attacker will switch
     * seamlessly to the given attack without changing any values. (Such as move stun or cooldown)
     * This allows for some quick and dirty ways to achieve special handling without making a new move for it
     * or without reusing code from other moves.
     * @param tick How many ticks after the initiation of this attack the switch should occur
     * @param move The move to switch to
     * @return This move
     */
    public T withFinisher(int tick, AbstractMove<?, ? super A> move) {
        finisher = IntObjectPair.of(tick, move);
        return getThis();
    }

    public T modifyFinisherTime(int tick) {
        if (finisher == null) {
            throw new IllegalStateException("modifyFinisherTime(" + tick + ") called without a pre-set finisher!");
        } else {
            finisher = IntObjectPair.of(tick, finisher.right());
        }
        return getThis();
    }

    // Lombok does not understand these variable names already start with 'is'.
    public boolean isCrouchingVariant() {
        return isCrouchingVariant;
    }

    public boolean isAerialVariant() {
        return isAerialVariant;
    }

    public boolean isFollowup() {
        return isFollowup;
    }

    /**
     * Called when this move is registered to a {@link net.arna.jcraft.common.attack.core.MoveMap MoveMap}.
     * Not supposed to be called anywhere else.
     * @param type The MoveType this move is registered as
     */
    @ApiStatus.Internal
    public final void onRegister(MoveType type) {
        moveType = type;

        if (crouchingVariant != null) crouchingVariant.onRegister(type);
        if (aerialVariant != null) aerialVariant.onRegister(type);
        if (followup != null) followup.onRegister(type);
        if (finisher != null) finisher.right().onRegister(type);

        // TODO convert these to actual tests
        // THATS TOO BAD!
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) return;
        testCopy();
        assert getThis() == this;
    }

    // Logic methods

    /**
     * Whether this attack may be initiated.
     * @param attacker The attacker to check for
     * @return Whether this attack may be initiated
     */
    public boolean canBeInitiated(A attacker) {
        // Followups generally don't check canAttack() cuz they require that move-stun > 0 while canAttack() requires the opposite.
        return (isFollowup() || attacker.canAttack()) && conditions.stream().allMatch(condition -> condition.test(attacker));
    }

    /**
     * Called when this move is initialized.
     * By default, only plays the sound(s) and invokes the init actions, if any.
     */
    public void onInitiate(A attacker) {
        initActions.forEach(action -> action.perform(attacker, attacker.getUser(), attacker.getMoveContext()));
        sounds.forEach(sound -> attacker.playAttackerSound(sound, 1f, 1f));
    }

    /**
     * Called when this move is canceled. Does nothing by default.
     */
    public void onCancel(A attacker) {}

    /**
     * Whether this attack should be allowed to move onto its finisher.
     * Certain attacks shouldn't always be able to, see: {@link net.arna.jcraft.common.attack.moves.shared.MainBarrageAttack#canFinish(IAttacker)}
     */
    public boolean canFinish(A attacker) {
        return true;
    }

    /**
     * Called every tick so long as this move is active.
     * Called separately for each attacker.
     * Invokes the {@link #perform(IAttacker, LivingEntity, MoveContext)} method if {@link #shouldPerform(IAttacker)}
     * returns {@code true} by default, but can be overridden to do whatever you want it to.
     * @param attacker The attacker to tick for.
     */
    public void tick(A attacker) {
        if (finisher != null && canFinish(attacker) && finisher.leftInt() <= getDuration() - attacker.getMoveStun())
            attacker.setCurrentMove(finisher.right());
        if (shouldPerform(attacker)) doPerform(attacker);
    }

    /**
     * Returns whether {@link #perform(IAttacker, LivingEntity, MoveContext)} should be called this tick.
     * Ensures the attacker has a valid user.
     * @param attacker The attacker to check for.
     * @return Whether this move should be performed this tick.
     */
    protected boolean shouldPerform(A attacker) {
        return attacker.getMoveStun() == getWindupPoint() && attacker.hasUser();
    }

    /**
     * Invokes all {@link #withAction(MoveAction) actions} and calls {@link #perform(IAttacker, LivingEntity, MoveContext)}.
     * @param attacker The attacker that will be performing this move.
     */
    public final void doPerform(A attacker) {
        LivingEntity user = attacker.getUserOrThrow();
        MoveContext ctx = attacker.getMoveContext();

        Set<LivingEntity> targets = perform(attacker, user, ctx);
        actions.forEach(action -> action.perform(attacker, user, ctx, targets));
    }

    /**
     * Performs this move.
     * @param attacker The attacker that will be performing this move.
     * @param user The user of the attacker. Will never be null.
     * @param ctx The move context in which to store data.
     * @return A set of all targeted entities.
     */
    public abstract @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx);

    /**
     * Register entries in the move context of an attacker to be used by this move.
     * @param ctx The context in which to register entries.
     */
    public void registerContextEntries(MoveContext ctx) {}

    /**
     * Gets the current blow this move is at.
     * For simple moves, this will always be 0.
     * For barrages or multi-hit moves, this can be greater than 0.
     * @param attacker The attacker to get the blow for
     * @return The current blow of this move for the given attacker
     */
    public int getBlow(A attacker) {
        return 0;
    }

    // Utility methods

    /**
     * Returns the point at which the windup has passed.
     * @return The point at which the windup has passed.
     */
    public int getWindupPoint() {
        return duration - windup;
    }

    /**
     * Returns whether the windup has passed.
     * @param attacker The attacker to check for
     * @return Whether the windup has passed
     */
    public boolean hasWindupPassed(IAttacker<?, ?> attacker) {
        return attacker.getMoveStun() <= getWindupPoint();
    }

    /**
     * Acquires the rotation vector for the given attacker, taking gravity into account.
     * @param attacker The attacker to get the rotation vector for
     * @return The rotation vector for the given attacker
     */
    public static Vec3d getRotVec(IAttacker<?, ?> attacker) {
        Vec3d rotVec = attacker.getBaseEntity().getRotationVector();
        if (GravityChangerAPI.getGravityDirection(attacker.getUserOrThrow()) == Direction.UP)
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
     * Called when a user inputs this move.
     * @param attacker The attacker that input this move.
     * @param type The {@link MoveInputType} of the move.
     * @param pressed Whether the move was pressed or released.
     * @param moveInitiated Whether the move was initiated (or rejected because of e.g., a cooldown).
     */
    public void onUserMoveInput(A attacker, MoveInputType type, boolean pressed, boolean moveInitiated) {}

    /**
     * Simply returns {@code this}. Can only be implemented by final moves.
     * This means that any intermediary move class (one that forms a base for other moves)
     * cannot implement this.
     * This also means that subclasses cannot override this.
     * This all together means that you must create an abstract class that represents your move
     * and an (empty) implementation if you wish to use this move both standalone and as a basis for other moves.
     * An example of this is {@link SimpleAttack SimpleAttack} and
     * {@link AbstractSimpleAttack}. SimpleAttack is simply an empty implementation of AbstractSimpleAttack so that
     * AbstractSimpleAttack can be used standalone while also being able to be extended by other moves.
     * @return This move
     */
    protected abstract @NonNull T getThis();

    /**
     * Copies all extra data that is not included in the move's constructor to the copy.
     * Should be called in {@link #copy()} and should always call the super method.
     * @param base The instance to copy the data to.
     * @return The given base with copied data.
     */
    protected @NonNull T copyExtras(@NonNull T base) {
        AbstractMove<T, A> cast = base; // Required to access private fields
        cast.sounds.addAll(sounds);
        cast.impactSounds.addAll(impactSounds);
        cast.conditions.addAll(conditions);
        cast.initActions.addAll(initActions);
        cast.actions.addAll(actions);
        cast.moveType = moveType;
        cast.name = name;
        cast.description = description;
        cast.followup = followup == null ? null : followup.copy();
        cast.crouchingVariant = crouchingVariant == null ? null : crouchingVariant.copy();
        cast.aerialVariant = aerialVariant == null ? null : aerialVariant.copy();
        cast.ranged = ranged;
        cast.isCrouchingVariant = isCrouchingVariant;
        cast.isAerialVariant = isAerialVariant;
        cast.isFollowup = isFollowup;
        cast.armor = armor;
        cast.isHoldable = isHoldable;
        cast.copyOnUse = copyOnUse;
        cast.finisher = finisher == null ? null : IntObjectPair.of(finisher.leftInt(), finisher.right().copy());
        cast.mobilityType = mobilityType;
        cast.originalMove = originalMove; // Set the original move to this move
        cast.animation = animation;
        cast.mayHitUser = mayHitUser;
        copiedExtras = true;
        return base;
    }

    /**
     * Creates a copy of this attack.
     * @return A copy of this attack.
     */
    public abstract @NonNull T copy();

    /**
     * Ensures the copy method does not return {@code null} and calls {@link #copyExtras(AbstractMove)}
     * (and the override calls the super method if applicable).
     * This is to prevent mistakes that are easily made and easily fixed but can have devastating consequences.
     * Called in {@link #onRegister(MoveType)}.
     */
    private void testCopy() {
        copiedExtras = false;
        T copy = copy();
        //noinspection ConstantValue // That's the idea.
        if (copy == null) throw new NullPointerException(getClass().getSimpleName() + "#copy() returned null");
        if (!copiedExtras) throw new IllegalStateException(getClass().getSimpleName() + "#copy() does not " +
                "call #copyExtras(AbstractMove).");

        if (crouchingVariant != null) crouchingVariant.testCopy();
        if (aerialVariant != null) aerialVariant.testCopy();
        if (followup != null) followup.testCopy();
    }

    @FunctionalInterface
    public interface InitAction<A extends IAttacker<? extends A, ?>> {
        void perform(A attacker, LivingEntity user, MoveContext ctx);
    }

    @FunctionalInterface
    public interface MoveAction<A extends IAttacker<? extends A, ?>> {
        void perform(A attacker, LivingEntity user, MoveContext ctx, Set<LivingEntity> targets);
    }
}
