package net.arna.jcraft.api.attack.moves;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.RegistrySupplier;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.MoveSelectionResult;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.core.MoveAction;
import net.arna.jcraft.api.attack.core.MoveCondition;
import net.arna.jcraft.api.attack.core.RunMoment;
import net.arna.jcraft.api.attack.enums.MobilityType;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.attack.actions.PlaySoundAction;
import net.arna.jcraft.common.attack.core.data.BaseMoveExtras;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.compat.FtbChunksCompat;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.ExtraProducts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings({"UnusedReturnValue", "unused"}) // API, s
@Getter
public abstract class AbstractMove<T extends AbstractMove<T, A>, A extends IAttacker<? extends A, ?>> {
    // Used to store the time this move was charged for, if any.
    // Set when this move is initiated using a charge time.
    private final List<MoveCondition<?, ? super A>> conditions = new ArrayList<>();
    private final List<MoveAction<?, ? super A>> actions = new ArrayList<>();
    /**
     * Be VERY careful when using this.
     * There's NO connection between static moves defined in fields
     * and moves that are actually executed (because of (de)serialization).
     * This is for internal use only.
     */
    private T originalMove = getThis();
    private MoveClass moveClass;
    private int cooldown;
    private int windup, duration;
    private float moveDistance;
    /**
     * This move's assigned animation
     */
    private Enum<?> animation;
    @NonNull
    private Component name = Component.empty(), description = Component.empty();
    private @Nullable AbstractMove<?, ? super A> crouchingVariant, aerialVariant, followup;
    private boolean isCrouchingVariant, isAerialVariant, isFollowup;
    private int armor;
    private IntObjectPair<AbstractMove<?, ? super A>> finisher;
    protected MobilityType mobilityType;
    private Boolean isHoldable;
    private boolean loopPrevention = true;
    private OptionalInt followupFrame = OptionalInt.empty();

    // Properties that are NOT serialized (usually set in constructor)
    // Used to help AI know how and when to use this attack.
    protected boolean ranged, barrage, multiHit, charge, counter, dash, grab; // todo: bitset (u8)
    protected boolean copyOnUse;
    protected boolean mayHitUser;
    /**
     * If true, the move handles setting cooldown itself rather than the attacker.
     */
    protected boolean manualCooldown;
    private boolean copiedExtras; // See #testCopy()

    // STATE VARIABLES

    // How long this move was charged for, if any.
    @Getter @Setter
    private int chargeTime = 0;

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
     * This should be set via the constructor; this is only to modify copies.
     *
     * @param cooldown The cooldown of this move in ticks
     * @return This move
     */
    public T withCooldown(final int cooldown) {
        this.cooldown = cooldown;
        return getThis();
    }

    /**
     * Sets the windup of this move.
     * This is how long it takes for the attack to perform after being initiated.
     * Should be set via the constructor, this is only to modify copies.
     *
     * @param windup The windup of this move in ticks
     * @return This move
     */
    public T withWindup(final int windup) {
        this.windup = windup;
        return getThis();
    }

    /**
     * Assigns an animation state to be used by the move, in case it can't be done in the movemap
     *
     * @param state This moves animation
     * @return This move
     */
    public T withAnim(final Enum<?> state) {
        this.animation = state;
        return getThis();
    }

    /**
     * Sets the duration of this move.
     * This is how long this attack lasts. It is also how long the user has to wait before they
     * can initiate another attack.
     * Should be set via the constructor, this is only to modify copies.
     *
     * @param duration The duration of this move in ticks
     * @return This move
     */
    public T withDuration(final int duration) {
        this.duration = duration;
        return getThis();
    }

    /**
     * Sets the move distance of this move.
     * This is how far away the stand is moved from the user when performing this move.
     * This should be set via the constructor; this is only to modify copies.
     *
     * @param moveDistance The move distance of this move
     * @return This move
     */
    public T withMoveDistance(final float moveDistance) {
        this.moveDistance = moveDistance;
        return getThis();
    }

    /**
     * Sets some information about this move displayed in commands.
     *
     * @param name        The name of this move
     * @param description The description of this move
     * @return This move
     */
    public T withInfo(final @NonNull Component name, final @NonNull Component description) {
        this.name = name;
        this.description = description;
        return getThis();
    }

    /**
     * Sets the crouching variant of this move. When invoking this move while crouching,
     * this variant is invoked instead.
     *
     * @param crouchingVariant The crouching variant of this move.
     * @return This move
     */
    public T withCrouchingVariant(final AbstractMove<?, ? super A> crouchingVariant) {
        if (isCrouchingVariant) {
            throw new IllegalStateException("Can't assign a crouching variant to a crouching variant.");
        }
        if (crouchingVariant.getCrouchingVariant() != null) {
            throw new IllegalArgumentException("Given move has a " +
                    "crouching variant. Crouching variants cannot have crouching variants.");
        }

        this.crouchingVariant = crouchingVariant.copy();
        this.crouchingVariant.isCrouchingVariant = true;
        return getThis();
    }

    /**
     * Sets the aerial variant of this move. When invoking this move while in the air,
     * this variant is invoked instead.
     *
     * @param aerialVariant The aerial variant of this move.
     * @return This move
     */
    public T withAerialVariant(final AbstractMove<?, ? super A> aerialVariant) {
        if (isAerialVariant) {
            throw new IllegalStateException("Can't assign an aerial variant to an aerial variant.");
        }

        this.aerialVariant = aerialVariant.copy();
        this.aerialVariant.isAerialVariant = true;
        return getThis();
    }

    /**
     * Disables loop prevention on this move.
     *
     * @return This move
     */
    public T noLoopPrevention() {
        this.loopPrevention = false;
        return getThis();
    }

    /**
     * Marks the move as a ranged move.
     *
     * @return This move
     */
    public T markRanged() {
        this.ranged = true;
        return getThis();
    }

    /**
     * Allows the stand to hit its own user
     *
     * @return This move
     */
    public T allowHitUser() {
        this.mayHitUser = true;
        return getThis();
    }

    /**
     * Sets the move that will be initiated after this move is performed.
     *
     * @param followup The move that will be initiated after this move is performed.
     * @return This move
     */
    public T withFollowup(final AbstractMove<?, ? super A> followup) {
        this.followup = followup.copy();
        this.followup.isFollowup = true;
        return getThis();
    }

    /**
     * Sets the number of hits this attack can withstand before breaking.
     *
     * @param armor The number of hits this attack can withstand
     * @return This move
     */
    public T withArmor(final int armor) {
        this.armor = armor;
        return getThis();
    }

    /**
     * Sets the armor value to {@link Integer#MAX_VALUE}.
     *
     * @return This move
     * @see #withArmor(int)
     */
    public T withHyperArmor() {
        return withArmor(Integer.MAX_VALUE);
    }

    /**
     * Sets the mobility type the Stand User AI will use to determine how to use this attack.
     *
     * @param mobilityType The mobility type of this attack
     * @return This attack
     */
    public T withMobilityType(final MobilityType mobilityType) {
        this.mobilityType = mobilityType;
        return getThis();
    }

    /**
     * Sets this move to be holdable.
     *
     * @return This move
     * @see #withHoldable(Boolean)
     */
    public T withHoldable() {
        return withHoldable(true);
    }

    /**
     * Adds a new condition to this move.
     * @param condition The condition to add
     * @return This move
     */
    public T withCondition(final MoveCondition<?, ? super A> condition) {
        conditions.add(condition);
        return getThis();
    }

    /**
     * Adds multiple conditions to this move.
     * @param conditions The conditions to add
     * @return This move
     */
    public T withConditions(final Collection<MoveCondition<?, ? super A>> conditions) {
        this.conditions.addAll(conditions);
        return getThis();
    }

    @ApiStatus.Internal
    @SuppressWarnings({"unchecked", "RedundantCast", "rawtypes"})
    public T withConditionsRaw(final Collection<MoveCondition<?, ?>> conditions) {
        this.conditions.addAll((Collection<? extends MoveCondition<?,? super A>>) (Collection) conditions);
        return getThis();
    }

    /**
     * Adds a new action to this move.
     * @param action The action to add
     * @return This move
     */
    public T withAction(final MoveAction<?, ? super A> action) {
        actions.add(action);
        return getThis();
    }

    /**
     * Adds a new action to this move.
     * @param action The action to add
     * @return This move
     */
    public T withAction(final MoveAction<?, ? super A> action, RunMoment runMoment) {
        action.setRunMoment(runMoment);
        actions.add(action);
        return getThis();
    }

    /**
     * Adds multiple actions to this move.
     * @param actions The actions to add
     * @return This move
     */
    public T withActions(final Collection<MoveAction<?, ? super A>> actions) {
        this.actions.addAll(actions);
        return getThis();
    }

    /**
     * Adds multiple actions to this move.
     * @param actions The actions to add
     * @return This move
     */
    public T withActions(final Collection<MoveAction<?, ? super A>> actions, RunMoment runMoment) {
        this.actions.addAll(actions.stream().peek(a -> a.setRunMoment(runMoment)).toList());
        return getThis();
    }

    @ApiStatus.Internal
    @SuppressWarnings({"unchecked", "RedundantCast", "rawtypes"})
    public T withActionsRaw(final Collection<MoveAction<?, ?>> actions) {
        this.actions.addAll((Collection<? extends MoveAction<?,? super A>>) (Collection) actions);
        return getThis();
    }

    /**
     * Adds a new action to this move that is performed when this move is initiated.
     * @param action The action to add
     * @return This move
     */

    public T withInitAction(final MoveAction<?, ? super A> action) {
        return withAction(action, RunMoment.AT_INIT);
    }

    /**
     * Adds multiple actions to this move that are performed when this move is initiated.
     * @param actions The actions to add
     * @return This move
     */
    public T withInitActions(final Collection<MoveAction<?, ? super A>> actions) {
        return withActions(actions, RunMoment.AT_INIT);
    }

    @ApiStatus.Internal
    @SuppressWarnings({"unchecked", "RedundantCast", "rawtypes"})
    public T withInitActionsRaw(final Collection<MoveAction<?, ?>> actions) {
        return withInitActions((Collection<MoveAction<?,? super A>>) (Collection) actions);
    }

    /**
     * Adds a sound to play when this move is initiated.
     * @param sound The sound to play
     * @return This move
     */
    public T withSound(final RegistrySupplier<SoundEvent> sound) {
        return withInitAction(PlaySoundAction.playSound(sound));
    }

    /**
     * Adds a sound to play when this move is initiated.
     * @param sound The sound to play
     * @return This move
     */
    public T withSound(final SoundEvent sound) {
        return withInitAction(PlaySoundAction.playSound(sound));
    }

    /**
     * Adds a sound to play when this move hits something.
     * @param sound The sound to play
     * @return This move
     */
    public T withImpactSound(final RegistrySupplier<SoundEvent> sound) {
        return withAction(PlaySoundAction.playImpactSound(sound));
    }

    /**
     * Adds a sound to play when this move hits something.
     * @param sound The sound to play
     * @return This move
     */
    public T withImpactSound(final SoundEvent sound) {
        return withAction(PlaySoundAction.playImpactSound(sound));
    }

    /**
     * Sets whether this move can be held.
     *
     * @param holdable Whether this move can be held. {@code null} for default behavior (dependent on the move-type).
     * @return This move
     */
    public T withHoldable(final Boolean holdable) {
        this.isHoldable = holdable;
        return getThis();
    }

    /**
     * Sets the move this move should finish with and when.
     * When the given tick is reached, the current move of the attacker will switch
     * seamlessly to the given attack without changing any values. (Such as move stun or cooldown)
     * This allows for some quick and dirty ways to achieve special handling without making a new move for it
     * or without reusing code from other moves.
     *
     * @param tick How many ticks after the initiation of this attack the switch should occur
     * @param move The move to switch to
     * @return This move
     */
    public T withFinisher(final int tick, final AbstractMove<?, ? super A> move) {
        finisher = IntObjectPair.of(tick, move);
        return getThis();
    }

    public T modifyFinisherTime(final int tick) {
        if (finisher == null) {
            throw new IllegalStateException("modifyFinisherTime(" + tick + ") called without a pre-set finisher!");
        } else {
            finisher = IntObjectPair.of(tick, finisher.right());
        }
        return getThis();
    }

    /**
     * If this move is re-initiated within the given frame,
     * the followup will be initiated immediately <b>after</b> checking conditions.
     * Frame is the number of ticks from the end of the move (so movestun <= frame).
     * @param frame Ticks from the end of the move to initiate the followup in.
     * @return This move
     */
    public T withFollowupFrame(final int frame) {
        return withFollowupFrame(OptionalInt.of(frame));
    }

    /**
     * If this move is re-initiated within the given frame,
     * the followup will be initiated immediately <b>after</b> checking conditions.
     * Frame is the number of ticks from the end of the move (so movestun <= frame).
     * @param frame Ticks from the end of the move to initiate the followup in.
     * @return This move
     */
    public T withFollowupFrame(final OptionalInt frame) {
        followupFrame = frame;
        return getThis();
    }

    // Lombok does not understand these variable names already start with 'is',
    // even though IntelliJ thinks it does.
    // Also, for some reason, suppressing the warning gives a warning about the suppression being redundant,
    // even though it is not.
    @SuppressWarnings({"LombokGetterMayBeUsed", "RedundantSuppression"})
    public boolean isCrouchingVariant() {
        return isCrouchingVariant;
    }

    public T markCrouchingVariant() {
        isCrouchingVariant = true;
        return getThis();
    }

    @SuppressWarnings({"LombokGetterMayBeUsed", "RedundantSuppression"})
    public boolean isAerialVariant() {
        return isAerialVariant;
    }

    public T markAerialVariant() {
        isAerialVariant = true;
        return getThis();
    }

    @SuppressWarnings({"LombokGetterMayBeUsed", "RedundantSuppression"})
    public boolean isFollowup() {
        return isFollowup;
    }

    public T markFollowup() {
        isFollowup = true;
        return getThis();
    }


    /**
     * Called when this move is registered to a {@link MoveMap MoveMap}.
     * Not supposed to be called anywhere else.
     *
     * @param moveClass The MoveClass this move is registered as
     */
    @ApiStatus.Internal
    public final void onRegister(final MoveClass moveClass) {
        this.moveClass = moveClass;

        if (crouchingVariant != null) {
            crouchingVariant.onRegister(moveClass);
        }
        if (aerialVariant != null) {
            aerialVariant.onRegister(moveClass);
        }
        if (followup != null) {
            followup.onRegister(moveClass);
        }
        if (finisher != null) {
            finisher.right().onRegister(moveClass);
        }

        // TODO convert these to actual tests
        // THATS TOO BAD!
        // lmao
        if (!Platform.isDevelopmentEnvironment()) return;

        testCopy();
        assert getThis() == this;

        //noinspection ConstantValue // that's the point
        if (getMoveType() == null) {
            throw new IllegalStateException("MoveType not set for " + this);
        }
    }

    public BaseMoveExtras getExtras() {
        return BaseMoveExtras.fromMove(getThis());
    }

    /**
     * Gets the type of this move. Holds the codec used to serialize and deserialize this move.
     * @return The type of this move
     */
    @NonNull
    public abstract MoveType<T> getMoveType();

    // Logic methods

    /**
     * Whether this attack may be initiated.
     * If you wish to add checks, consider overriding {@link #conditionsMet(IAttacker)} instead.
     *
     * @param attacker The attacker to check for
     * @return Whether this attack may be initiated
     */
    public final boolean canBeInitiated(final A attacker) {
        // Followups generally don't check canAttack() cuz they require that move-stun > 0 while canAttack() requires the opposite.
        return (isFollowup() || attacker.canAttack()) && conditionsMet(attacker);
    }

    /**
     * Checks whether all conditions are met for this move to be initiated.
     * Required to be {@code true} for {@link #canBeInitiated(IAttacker)} to return {@code true} and
     * checked to find the first valid entry when attempting to initiate a move.
     * Overrides should call {@code super.conditionsMet(attacker)} to ensure all conditions are checked.
     *
     * @param attacker The attacker to check for
     * @return Whether all conditions are met for this move to be initiated
     */
    public boolean conditionsMet(final A attacker) {
        return conditions.stream().allMatch(condition -> condition.test(attacker));
    }

    /**
     * Called when this move is initialized.
     * By default, only plays the sound(s) and invokes the init actions, if any.
     */
    public void onInitiate(final A attacker) {
        LivingEntity user = attacker.getUser();
        Set<LivingEntity> targets = Set.of(); // Obviously none yet
        for (final MoveAction<?, ? super A> action : actions) {
            if (action.getRunMoment() == RunMoment.AT_INIT) {
                action.perform(attacker, user, targets);
            }
        }
    }

    /**
     * Called when this move is canceled. Does nothing by default.
     */
    public void onCancel(final A attacker) {}

    /**
     * Whether this attack should be allowed to move onto its finisher.
     * Certain attacks shouldn't always be able to, see:
     * {@link net.arna.jcraft.common.attack.moves.shared.MainBarrageAttack#canFinish(IAttacker) MainBarageAttack#canFinish(IAttacker)}
     */
    public boolean canFinish(final A attacker) {
        return true;
    }

    /**
     * Whether this move is allowed to be queued when initiated while another move is active.
     * Most moves should be able to be queued, but some moves may not be able to be queued (like KC prediction).
     * @param attacker The attacker to check for
     * @return Whether this move is allowed to be queued
     */
    public boolean canBeQueued(final A attacker) {
        return true;
    }

    /**
     * Called every tick so long as this move is active.
     * Called separately for each attacker.
     * Invokes the {@link #perform(IAttacker, LivingEntity)} method if {@link #shouldPerform(IAttacker, int)}
     * returns {@code true} by default, but can be overridden to do whatever you want it to.
     *
     * @param attacker The attacker to tick for.
     */
    public void activeTick(final A attacker, final int moveStun) {
        Set<LivingEntity> targets = Set.of();
        for (final MoveAction<?, ? super A> action : actions) {
            if (action.getRunMoment() == RunMoment.EVERY_TICK ||
                    (action.getRunMoment() == RunMoment.AT_END && moveStun == 0) ||
                    action.getRunMoment().shouldRun(getThis(), attacker, attacker.getUser(), getDuration() - moveStun, targets)) {
                action.perform(attacker, attacker.getUserOrThrow(), targets);
            }
        }

        if (finisher != null && canFinish(attacker) && finisher.leftInt() <= getDuration() - moveStun) {
            attacker.setCurrentMove(finisher.right());
        }
        if (shouldPerform(attacker, moveStun)) {
            attacker.setPerformedThisTick(true);
            doPerform(attacker);
        }
    }

    /**
     * Called every tick when the move map this move is registered to is ticked.
     * Called regardless of whether this move is active.
     * Should be used to apply effects or do some things based on values of context variables.
     * (Example is Vampire's Night Vision move)
     * @param attacker The attacker to tick for.
     */
    public void tick(final A attacker) {}

    /**
     * Returns whether {@link #perform(IAttacker, LivingEntity)} should be called this tick.
     * Ensures the attacker has a valid user.
     *
     * @param attacker The attacker to check for.
     * @return Whether this move should be performed this tick.
     */
    public boolean shouldPerform(final A attacker, final int moveStun) {
        return moveStun == getWindupPoint() && attacker.hasUser();
    }

    /**
     * Calls {@link #perform(IAttacker, LivingEntity)}.
     *
     * @param attacker The attacker that will be performing this move.
     */
    public final void doPerform(final A attacker) {
        LivingEntity user = attacker.getUserOrThrow();

        Set<LivingEntity> targets = perform(attacker, user);
        boolean hit = !targets.isEmpty();

        for (final MoveAction<?, ? super A> action : actions) {
            if (action.getRunMoment() == RunMoment.ON_STRIKE || hit && action.getRunMoment() == RunMoment.ON_HIT) {
                action.perform(attacker, attacker.getUserOrThrow(), targets);
            }
        }
        attacker.onPerform(this, targets);
    }

    /**
     * Performs this move.
     * After performing, the attacker's {@link IAttacker#getCurrentMove()} may be null.
     *
     * @param attacker The attacker that will be performing this move.
     * @param user     The user of the attacker. Will never be null.
     * @return A set of all targeted entities. Should be empty if no entities were (directly) targeted.
     */
    public abstract @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user);

    /**
     * Gets the current blow this move is at.
     * For simple moves, this will always be 0.
     * For barrages or multi-hit moves, this can be greater than 0.
     *
     * @param attacker The attacker to get the blow for
     * @return The current blow of this move for the given attacker
     */
    public int getBlow(final A attacker) {
        return 0;
    }

    // Utility methods

    /**
     * Returns the point at which the windup has passed.
     *
     * @return The point at which the windup has passed.
     */
    public int getWindupPoint() {
        return duration - windup;
    }

    /**
     * Returns whether the windup has passed.
     *
     * @param attacker The attacker to check for
     * @return Whether the windup has passed
     */
    public boolean hasWindupPassed(final IAttacker<?, ?> attacker) {
        return attacker.getMoveStun() <= getWindupPoint();
    }

    /**
     * Returns whether the windup has passed. Uses stored moveStun value from {@link net.arna.jcraft.common.tickable.MoveTickQueue}
     * Use for attack ticking logic, otherwise use the other one.
     *
     * @param attacker The attacker to check for
     * @return Whether the windup has passed
     */
    public boolean hasWindupPassed(final IAttacker<?, ?> attacker, final int moveStun) {
        return moveStun <= getWindupPoint();
    }

    /**
     * Acquires the rotation vector for the given attacker, taking gravity into account.
     *
     * @param attacker The attacker to get the rotation vector for
     * @return The rotation vector for the given attacker
     */
    public static Vec3 getRotVec(final IAttacker<?, ?> attacker) {
        Vec3 rotVec = attacker.getBaseEntity().getLookAngle();
        if (GravityChangerAPI.getGravityDirection(attacker.getUserOrThrow()) == Direction.UP) {
            rotVec = new Vec3(rotVec.x, -rotVec.y, rotVec.z);
        }

        return rotVec;
    }

    /**
     * Acquires the position of the attacker's eyes while taking the gravity of the user into account.
     *
     * @param attacker The attacker to get the eye position for
     * @return The eye position of the given attacker
     */
    protected Vec3 getOffsetHeightPos(final A attacker) {
        final Vec3 upVec = GravityChangerAPI.getEyeOffset(attacker.getUserOrThrow());
        final Vec3 heightOffset = upVec.scale(0.5);
        return attacker.getBaseEntity().position().add(heightOffset);
    }

    /**
     * Helper method that determines whether the given user may break the given block.
     * For a general check, you can pass {@code null} as the pos.
     * @param user The user to check for
     * @param pos The position to check for, or null if no position is applicable yet.
     * @return Whether the user may break either the given blocks or blocks in general.
     */
    public static boolean mayBreak(final LivingEntity user, @Nullable final BlockPos pos) {
        return mayBreak(user, pos, null);
    }

    /**
     * Helper method that determines whether the given user may break the given block.
     * For a general check, you can pass {@code null} as the pos.
     * @param user The user to check for
     * @param pos The position to check for, or null if no position is applicable yet.
     * @param pred An optional predicate to add extra checks for a state (such as explosion resistance).
     *             Prevents the need to acquire the state again for such checks afterward.
     * @return Whether the user may break either the given blocks or blocks in general.
     */
    public static boolean mayBreak(final LivingEntity user, @Nullable final BlockPos pos, @Nullable Predicate<BlockState> pred) {
        return mayBreak(user.level(), user, pos, pred);
    }

    /**
     * Helper method that determines whether the given user may break the given block.
     * For a general check, you can pass {@code null} as the pos.
     * @param level The level to check in
     * @param user The user to check for
     * @param pos The position to check for, or null if no position is applicable yet.
     * @param pred An optional predicate to add extra checks for a state (such as explosion resistance).
     *             Prevents the need to acquire the state again for such checks afterward.
     * @return Whether the user may break either the given blocks or blocks in general.
     */
    public static boolean mayBreak(final @NonNull Level level, @Nullable final LivingEntity user, @Nullable final BlockPos pos,
                                   @Nullable Predicate<BlockState> pred) {
        ServerLevel serverLevel = level instanceof ServerLevel sl ? sl : null;
        ServerPlayer player = user instanceof ServerPlayer p ? p : null;

        boolean isPlayer = player != null;
        boolean mobGriefing = level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        boolean standGriefing = level.getGameRules().getBoolean(JCraft.STAND_GRIEFING);
        boolean isSpawnProtected = serverLevel != null && player != null && pos != null &&
                serverLevel.getServer().isUnderSpawnProtection(serverLevel, pos, player);
        boolean mayBuild = !isPlayer || pos == null ||
                player.mayBuild() && FtbChunksCompat.get().mayEdit(player, serverLevel, pos);

        boolean mayAttempt = (isPlayer || mobGriefing) && standGriefing && !isSpawnProtected && mayBuild;
        if (!mayAttempt || pos == null) return mayAttempt;

        // The move may attempt to destroy this block, check if it should be possible to.
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        float destroyTime = block.defaultDestroyTime();

        boolean mayDestroy = destroyTime > 0;
        boolean extraChecks = pred == null || pred.test(state);

        return mayDestroy && extraChecks;
    }

    /**
     * Called when a user inputs this move.
     *
     * @param attacker      The attacker that input this move.
     * @param type          The {@link MoveInputType} of the move.
     * @param pressed       Whether the move was pressed or released.
     * @param moveInitiated Whether the move was initiated (or rejected because of e.g., a cooldown).
     */
    public void onUserMoveInput(final A attacker, final MoveInputType type, final boolean pressed, final boolean moveInitiated) {}

    /**
     * If this move is the current move, this move may dictate how AIs should use it.
     * @param mob The user of the attacker (mob)
     * @param target The entity the mob is targeting
     * @param stunTicks The amount of stun ticks the target currently has.
     * @param enemyMoveStun The move stun the target has.
     * @param distance The distance between the mob and the target.
     * @param enemyStand The stand of the target.
     * @param enemyAttack The attack the target is currently performing (if any).
     * @return The result of the move selection criterion. Pass by default.
     * @see MoveSelectionResult
     */
    public MoveSelectionResult specificMoveSelectionCriterion(A attacker, LivingEntity mob, LivingEntity target, int stunTicks,
                                                                                  int enemyMoveStun, double distance,
                                                                                  StandEntity<?, ?> enemyStand, AbstractMove<?, ?> enemyAttack) {
        return MoveSelectionResult.PASS;
    }

    /**
     * Called when a move is attempted to be initiated while this move is the current move.
     * Can be used to do some kind of selection logic (like D4C's clone spawn move).
     * @param attacker The attacker that is attempting to initiate this move.
     * @param moveClass The move class of the move that is being initiated.
     * @return {@code true} if the event was consumed and the move
     * should not be initiated/queued, {@code false} otherwise.
     */
    public boolean onInitMove(A attacker, MoveClass moveClass) {
        return false;
    }

    /**
     * Whether this move being active prevents the user from performing another move.
     * Returns {@code true} by default.
     * @return Whether this move prevents the user from performing another move.
     */
    public boolean preventsMoves() {
        return true;
    }

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
     *
     * @return This move
     */
    protected abstract @NonNull T getThis();

    /**
     * Copies all extra data that is not included in the move's constructor to the copy.
     * Should be called in {@link #copy()} and should always call the super method.
     *
     * @param base The instance to copy the data to.
     * @return The given base with copied data.
     */
    protected @NonNull T copyExtras(final @NonNull T base) {
        AbstractMove<T, A> cast = base; // Required to access private fields
        cast.originalMove = originalMove;
        cast.moveClass = moveClass;
        cast.name = name;
        cast.description = description;
        cast.followup = followup == null ? null : followup.copy();
        cast.crouchingVariant = crouchingVariant == null ? null : crouchingVariant.copy();
        cast.aerialVariant = aerialVariant == null ? null : aerialVariant.copy();
        cast.conditions.addAll(conditions);
        cast.actions.addAll(actions);
        cast.followupFrame = followupFrame;
        cast.ranged = ranged;
        cast.isCrouchingVariant = isCrouchingVariant;
        cast.isAerialVariant = isAerialVariant;
        cast.isFollowup = isFollowup;
        cast.armor = armor;
        cast.isHoldable = isHoldable;
        cast.copyOnUse = copyOnUse;
        cast.finisher = finisher == null ? null : IntObjectPair.of(finisher.leftInt(), finisher.right().copy());
        cast.mobilityType = mobilityType;
        cast.animation = animation;
        cast.mayHitUser = mayHitUser;
        cast.loopPrevention = loopPrevention;
        copiedExtras = true;
        return base;
    }

    /**
     * Creates a copy of this attack.
     *
     * @return A copy of this attack.
     */
    public abstract @NonNull T copy();

    /**
     * Ensures the copy method does not return {@code null} and calls {@link #copyExtras(AbstractMove)}
     * (and the override calls the super method if applicable).
     * This is to prevent mistakes that are easily made and easily fixed but can have devastating consequences.
     * Called in {@link #onRegister(MoveClass)}.
     */
    private void testCopy() {
        copiedExtras = false;
        T copy = copy();
        //noinspection ConstantValue // That's the idea.
        if (copy == null) {
            throw new NullPointerException(getClass().getSimpleName() + "#copy() returned null");
        }
        if (!copiedExtras) {
            throw new IllegalStateException(getClass().getSimpleName() + "#copy() does not " +
                    "call #copyExtras(AbstractMove).");
        }

        if (crouchingVariant != null) {
            crouchingVariant.testCopy();
        }
        if (aerialVariant != null) {
            aerialVariant.testCopy();
        }
        if (followup != null) {
            followup.testCopy();
        }
        if (finisher != null) {
            finisher.right().testCopy();
        }
    }

    protected abstract static class Type<M extends AbstractMove<? extends M, ?>> implements MoveType<M> {
        @Getter(lazy = true)
        private final Codec<M> codec = RecordCodecBuilder.create(this::buildCodec);

        protected RecordCodecBuilder<M, BaseMoveExtras> extras() {
            return BaseMoveExtras.CODEC.get().fieldOf("extras").forGetter(AbstractMove::getExtras);
        }

        protected RecordCodecBuilder<M, Integer> cooldown() {
            return Codec.INT.fieldOf("cooldown").forGetter(AbstractMove::getCooldown);
        }

        protected RecordCodecBuilder<M, Integer> windup() {
            return Codec.INT.fieldOf("windup").forGetter(AbstractMove::getWindup);
        }

        protected RecordCodecBuilder<M, Integer> duration() {
            return Codec.INT.fieldOf("duration").forGetter(AbstractMove::getDuration);
        }

        protected RecordCodecBuilder<M, Float> moveDistance() {
            return Codec.FLOAT.fieldOf("move_distance").forGetter(AbstractMove::getMoveDistance);
        }

        /**
         * Builds the codec for this move type.
         * See the methods in this class that return {@link RecordCodecBuilder} for fields that can be used.
         * Not all fields have to be used, moves may have default/hardcoded values for some of them.
         * <br><br>
         * Example implementation (from {@link SimpleAttack}):
         * <pre>{@code
         * @Override
         * protected App<RecordCodecBuilder.Mu<SimpleAttack<?>>, SimpleAttack<?>> buildCodec(RecordCodecBuilder.Instance<SimpleAttack<?>> instance) {
         *     return instance.group(
         *         extras(), attackExtras(), cooldown(), windup(), duration(),
         *         moveDistance(), damage(), stun(), hitboxSize(), knockback(), offset()
         *     ).apply(instance, SimpleAttack::new);
         * }
         * }</pre>
         * <br><br>
         * <b>NOTE:</b>
         * If the above implementation example suits your needs,
         * it is likely you can use a default method instead.
         * Each base move has a Type class
         * that has a <code>&lt;move&gt;Default(RecordCodecBuilder.Instance, Function)</code> method.
         * <br>
         * (For example {@link #baseDefault(RecordCodecBuilder.Instance, Function4)})
         *
         * @param instance The instance to build the codec with
         * @return The codec for this move type
         */
        @NonNull
        protected abstract App<RecordCodecBuilder.Mu<M>, M> buildCodec(final RecordCodecBuilder.Instance<M> instance);

        protected Function<BaseMoveExtras, M> applyExtras(Supplier<M> function) {
            return extras -> extras.apply(function.get());
        }

        protected <T1> BiFunction<BaseMoveExtras, T1, M> applyExtras(Function<T1, M> function) {
            return (extras, t1) -> extras.apply(function.apply(t1));
        }

        protected <T1, T2> Function3<BaseMoveExtras, T1, T2, M> applyExtras(BiFunction<T1, T2, M> function) {
            return (extras, t1,  t2) -> extras.apply(function.apply(t1, t2));
        }

        protected <T1, T2, T3> Function4<BaseMoveExtras, T1, T2, T3, M> applyExtras(Function3<T1, T2, T3, M> function) {
            return (extras, t1, t2, t3) -> extras.apply(function.apply(t1, t2, t3));
        }

        protected <T1, T2, T3, T4> Function5<BaseMoveExtras, T1, T2, T3, T4, M>
        applyExtras(Function4<T1, T2, T3, T4, M> function) {
            return (extras, t1, t2, t3, t4) -> extras.apply(function.apply(t1, t2, t3, t4));
        }

        protected <T1, T2, T3, T4, T5> Function6<BaseMoveExtras, T1, T2, T3, T4, T5, M>
        applyExtras(Function5<T1, T2, T3, T4, T5, M> function) {
            return (extras, t1, t2, t3, t4, t5) ->
                    extras.apply(function.apply(t1, t2, t3, t4, t5));
        }

        protected <T1, T2, T3, T4, T5, T6> Function7<BaseMoveExtras, T1, T2, T3, T4, T5, T6, M>
        applyExtras(Function6<T1, T2, T3, T4, T5, T6, M> function) {
            return (extras, t1, t2, t3, t4, t5, t6) ->
                    extras.apply(function.apply(t1, t2, t3, t4, t5, t6));
        }

        protected <T1, T2, T3, T4, T5, T6, T7> Function8<BaseMoveExtras, T1, T2, T3, T4, T5, T6, T7, M>
        applyExtras(Function7<T1, T2, T3, T4, T5, T6, T7, M> function) {
            return (extras, t1, t2, t3, t4, t5, t6, t7) ->
                    extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8> Function9<BaseMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, M>
        applyExtras(Function8<T1, T2, T3, T4, T5, T6, T7, T8, M> function) {
            return (extras, t1, t2, t3, t4, t5, t6, t7, t8) ->
                    extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9> Function10<BaseMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, M>
        applyExtras(Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, M> function) {
            return (extras, t1, t2, t3, t4, t5, t6, t7, t8, t9) ->
                    extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Function11<BaseMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, M>
        applyExtras(Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, M> function) {
            return (extras, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10) ->
                    extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>
        Function12<BaseMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, M>
        applyExtras(Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, M> function) {
            return (extras, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11) ->
                    extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>
        Function13<BaseMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, M>
        applyExtras(Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, M> function) {
            return (extras, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12) ->
                    extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>
        Function14<BaseMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, M>
        applyExtras(Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, M> function) {
            return (extras, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13) ->
                    extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>
        Function15<BaseMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, M>
        applyExtras(Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, M> function) {
            return (extras, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14) ->
                    extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>
        Function16<BaseMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, M>
        applyExtras(Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, M> function) {
            return (extras, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15) ->
                    extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>
        ExtraProducts.P17.Function17<BaseMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, M>
        applyExtras(Function16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, M> function) {
            return (extras, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16) ->
                    extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17>
        ExtraProducts.P18.Function18<BaseMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, M>
        applyExtras(ExtraProducts.P17.Function17<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, M> function) {
            return (extras, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17) ->
                    extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18>
        ExtraProducts.P19.Function19<BaseMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, M>
        applyExtras(ExtraProducts.P18.Function18<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, M> function) {
            return (extras, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18) ->
                    extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18));
        }

        protected <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19>
        ExtraProducts.P20.Function20<BaseMoveExtras, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, M>
        applyExtras(ExtraProducts.P19.Function19<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, M> function) {
            return (extras, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19) ->
                    extras.apply(function.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19));
        }

        protected Products.P5<RecordCodecBuilder.Mu<M>, BaseMoveExtras, Integer, Integer, Integer, Float> baseDefault(RecordCodecBuilder.Instance<M> instance) {
            return instance.group(extras(), cooldown(), windup(), duration(), moveDistance());
        }

        protected App<RecordCodecBuilder.Mu<M>, M> baseDefault(RecordCodecBuilder.Instance<M> instance,
                                                               Function4<Integer, Integer, Integer, Float, M> function) {
            return baseDefault(instance).apply(instance, applyExtras(function));
        }
    }
}
