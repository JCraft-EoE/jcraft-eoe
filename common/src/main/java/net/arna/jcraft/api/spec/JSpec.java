package net.arna.jcraft.api.spec;

import com.google.common.base.MoreObjects;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;
import lombok.Setter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.MoveUsage;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.attack.moves.AbstractMultiHitAttack;
import net.arna.jcraft.api.component.living.CommonCooldownsComponent;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.ai.AttackerBrainInfo;
import net.arna.jcraft.common.ai.CombatEntityContext;
import net.arna.jcraft.common.ai.CombatInstantContext;
import net.arna.jcraft.common.ai.IJAttackerBrain;
import net.arna.jcraft.common.attack.core.MoveMapImpl;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.network.s2c.PlayerAnimPacket;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.tickable.MoveTickQueue;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.DashData;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.SpecAnimationState;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Class that needs to be instantiated per-entity to contain temporary data relating to their current state.
 * Used to handle stand-off attacks.
 */
@Getter
public abstract class JSpec<A extends JSpec<A, S>, S extends Enum<S> & SpecAnimationState<A>>
        implements IAttacker<A, S>, MoveSet.ReloadListener<A, S> {
    private MoveSet<A, S> moveSet;
    private final MoveMap<A, S> moveMap = new MoveMapImpl<>();
    private final SpecType type;
    public final LivingEntity user;
    public final Player player;
    @Setter
    public int moveStun = 0;
    private boolean performedThisTick;

    private S state;

    @Getter
    private MoveUsage moveUsage;
    public AbstractMove<?, ? super A> curMove;
    public AbstractMove<?, ? super A> previousAttack;

    public MoveInputType queuedMove;

    public int armorPoints = 0;

    private boolean holding = false;
    private MoveInputType holdingType = null;

    protected JSpec(SpecType type, LivingEntity livingEntity) {
        this.type = type;
        this.user = livingEntity;
        if (this.user instanceof Player playerEntity) {
            this.player = playerEntity;
        } else {
            this.player = null;
        }

        this.moveSet = MoveSetManager.get(type, "default");
        if (this.moveSet == null) {
            throw new NoSuchElementException("No 'default' move set found for spec " + type);
        }

        moveSet.registerListener(this);
    }

    public SpecData getSpecData() {
        return type.getData();
    }

    @Override
    public LivingEntity getUser() {
        return user;
    }

    @Override
    public LivingEntity getBaseEntity() {
        return user;
    }

    @Override
    public DamageSource getDamageSource() {
        return JDamageSources.create(user.level(), DamageTypes.PLAYER_ATTACK, user);
    }

    @Override
    public boolean hasUser() {
        return user != null;
    }

    @Override
    public LivingEntity getUserOrThrow() {
        return Objects.requireNonNull(user, "Player must not be null");
    }

    @Override
    public AbstractMove<?, ? super A> getCurrentMove() {
        return curMove;
    }

    @Override
    public void setCurrentMove(@Nullable AbstractMove<?, ? super A> move) {
        previousAttack = curMove;
        curMove = move;
        if (curMove != null) {
            moveUsage = new MoveUsage(user.tickCount, move);
        }
    }

    @Override
    public void queueMove(MoveInputType type) {
        if (user == null) {
            return;
        }
        queuedMove = type;
    }

    @Override
    public void setState(S state) {
        this.state = state;
        setAnimation(state.getKey(getThis()), moveStun, 1f);
    }

    @Override
    public void playAttackerSound(SoundEvent sound, float volume, float pitch) {
        user.level().playSound(null, user.getX(), user.getY(), user.getZ(), sound, SoundSource.PLAYERS,
                volume, pitch);
    }

    @Override
    public void onMoveSetReload(MoveSet<A, S> moveSet) {
        if (!this.moveSet.getName().equals(moveSet.getName())) return;

        switchMoveSet(moveSet);
    }

    /**
     * Switches the move set to a different, registered move set.
     * @param name The name of the move set to switch to.
     */
    public void switchMoveSet(String name) {
        MoveSet<A, S> moveSet = MoveSetManager.get(getType(), name);
        if (moveSet == null) {
            JCraft.LOGGER.error("Move set '{}' not found for {}", name, getType());
            return;
        }

        switchMoveSet(moveSet);
    }

    private void switchMoveSet(MoveSet<A, S> moveSet) {
        this.moveSet = moveSet;
        moveSet.registerListener(this); // implementation uses a set, so this is fine
        moveMap.copyFrom(moveSet.getMoveMap());
    }

    public boolean initMove(MoveClass moveClass) {
        if (getCurrentMove() != null) {
            if (getCurrentMove().onInitMove(getThis(), moveClass)) {
                return true;
            }

            if (getCurrentMove().getFollowup() != null && getCurrentMove().getFollowupFrame().isPresent() &&
                    getCurrentMove().getMoveClass() == moveClass && getMoveStun() <= getCurrentMove().getFollowupFrame().getAsInt()) {
                moveMap.initiateFollowup(getThis(), getCurrentMove(), false, 0);
            }
        }

        return handleMove(moveClass);
    }

    public boolean canHoldMove(@Nullable MoveInputType type) {
        if (type == null || type.getMoveClass() == null) {
            return false;
        }

        boolean crouching = hasUser() && user.isShiftKeyDown();
        boolean aerial = hasUser() && !user.onGround();
        MoveMap.Entry<A, S> entry = moveMap.getFirstValidEntry(type.getMoveClass(), getThis(), crouching, aerial);
        return entry == null ? type.isHoldable() : MoreObjects.firstNonNull(entry.getMove().getIsHoldable(), type.isHoldable());
    }

    public final void onUserMoveInput(MoveInputType type, boolean pressed, boolean moveInitiated) {
        onUserMoveInput(curMove, type, pressed, moveInitiated);
    }

    public boolean canAttack() {
        return moveStun <= 0 && !JUtils.isAffectedByTimeStop(user) && !user.hasEffect(JStatusRegistry.DAZED.get());
    }

    public boolean handleMove(MoveClass moveClass) {
        return handleMove(moveClass, 1f);
    }

    public MoveMap.Entry<A, S> getMoveEntry(MoveClass moveClass, boolean crouching, boolean aerial) {
        return moveMap.getFirstValidEntry(moveClass, getThis(), crouching, aerial);
    }

    protected AbstractMove<?, ? super A> overrideMoveSelection(AbstractMove<?, ? super A> original, boolean crouching, boolean aerial) {
        return original;
    }

    public boolean handleMove(MoveClass moveClass, float animationSpeed) {
        boolean crouching = hasUser() && user.isShiftKeyDown();
        boolean aerial = hasUser() && !user.onGround();
        MoveMap.Entry<A, S> entry = getMoveEntry(moveClass, crouching, aerial);
        if (entry == null) {
            return false;
        }

        if (user.isShiftKeyDown()) {
            if (entry.getCrouchingVariant() != null) {
                entry = entry.getCrouchingVariant();
            }
        } else if (!user.onGround() && entry.getAerialVariant() != null) {
            entry = entry.getAerialVariant();
        }

        final AbstractMove<?, ? super A> move = overrideMoveSelection(entry.getMove(), crouching, aerial);

        return handleMove(
                move.isCopyOnUse() ? move.copy() : move,
                entry.getCooldownType(),
                entry.getAnimState(),
                animationSpeed
        );
    }

    public boolean handleMove(AbstractMove<?, ? super A> move, CooldownType cooldownType, S state) {
        return handleMove(move, cooldownType, state, 1f);
    }

    public boolean handleMove(AbstractMove<?, ? super A> move, CooldownType cooldownType, @Nullable S state, float animationSpeed) {
        if (!move.canBeInitiated(getThis())) {
            return false;
        }

        if (cooldownType != null && move.getCooldown() > 0) {
            CommonCooldownsComponent cooldowns = JComponentPlatformUtils.getCooldowns(user);
            int cd = cooldowns.getCooldown(cooldownType);
            if (cd > 0) {
                return false;
            }
            if (!move.isManualCooldown()) cooldowns.setCooldown(cooldownType, move.getCooldown());
        }

        //JCraft.LOGGER.info("SERVER: Handling spec attack: " + attack + " in world: " + serverWorld);

        move = animationSpeed == 1 ? move : move.copy()
                .withDuration((int) (move.getDuration() / animationSpeed))
                .withWindup((int) (move.getWindup() / animationSpeed));

        move.onInitiate(getThis());

        // If the move has a duration of 0, perform it instantly.
        if (move.getDuration() == 0) {
            move.perform(getThis(), getUserOrThrow());
            return true;
        }

        setCurrentMove(move);
        moveStun = move.getDuration();

        if (curMove instanceof AbstractMultiHitAttack<?, ?> multiHitAttack) {
            multiHitAttack.withHitMoments(IntSet.of(multiHitAttack.getHitMoments().intStream()
                    .map(i -> (int) (i / animationSpeed))
                    .toArray()));
        }

        var finisher = move.getFinisher();
        if (finisher != null) {
            int finisherSwapTick = (int) (finisher.leftInt() / animationSpeed);
            move.modifyFinisherTime(finisherSwapTick);
            // Ensure the finisher will happen
            int finisherWindupTime = finisher.right().getWindup() + 1;
            if (moveStun < finisherWindupTime) {
                moveStun = finisherWindupTime;
            }
        }

        armorPoints = move.getArmor();

        final Enum<? extends Enum<?>> overrideAnimation = move.getAnimation();
        if (overrideAnimation != null) {
            state = (S) overrideAnimation;
        }

        if (state != null) {
            setAnimation((this.state = state).getKey(getThis()), moveStun, animationSpeed);
        }

        return true;
    }

    public void setAnimation(String animationID, int duration, float animationSpeed) {
        if (player == null) {
            if (user instanceof JSpecHolder specHolder) {
                specHolder.setAnimation(animationID, animationSpeed);
            } else {
                JCraft.LOGGER.error("Tried to set animation for non-player entity with JSpec that does not implement JSpecHolder!");
            }
        } else {
            PlayerAnimPacket.sendSpec(player, JUtils.around((ServerLevel) user.level(), user.position(), JUtils.PLAYER_ANIMATION_DIST), animationID, duration, animationSpeed);
        }
    }

    public void cancelMove() {
        cancelMove(false);
    }

    /**
     * Cancels the spec's move instantly
     * @param offensiveCancel Whether the move cancellation was initiated by another party,
     *                        and should execute the to-be-cancelled move if {@link AbstractMove#shouldPerform(IAttacker, int)} is true.
     *                        This is used to mitigate the problem of subtick update priority between different IAttackers.
     */
    public void cancelMove(boolean offensiveCancel) {
        if (curMove != null) {
            if (offensiveCancel && !performedThisTick && curMove.shouldPerform(getThis(), getMoveStun() - 1)) {
                setPerformedThisTick(true);
                curMove.perform(getThis(), getUserOrThrow());
            }
            if (curMove != null) {
                curMove.onCancel(getThis());
            }
        }
        curMove = null;
        queuedMove = null;
        armorPoints = 0;
        moveStun = 0;

        if (user == null) {
            return;
        }
        // Cancel player animation if it exists
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeShort(13);
        buf.writeInt(user.getId());
        ServerChannelFeedbackPacket.send(JUtils.around((ServerLevel) user.level(), user.position(), JUtils.PLAYER_ANIMATION_DIST), buf);
    }

    public boolean shouldSneak() {
        return false;
    }

    public void processAttackClient() {
    }

    public void tickSpec() {
        if (user.isSpectator()) {
            return;
        }

        Level world = user.level();

        if (world.isClientSide()) {
            //JCraft.LOGGER.info("CLIENT: Ticking spec " + this);

            if (moveStun > 0) {
                //JCraft.LOGGER.info("CLIENT: Movestun is " + moveStun);

                //player.setSneaking(shouldSneak());

                // Process attack
                moveStun--;
                processAttackClient();
            }

            return;
        }

        moveMap.tickMoves(getThis());

        if (moveStun <= 0) {
            armorPoints = 0;

            if (queuedMove != null) {
                initMove(queuedMove.getMoveClass());
                queuedMove = null;
            }

            if (curMove != previousAttack && curMove != null) {
                previousAttack = curMove;
            }
            return;
        }

        //JCraft.LOGGER.info("SERVER: Movestun is " + moveStun);

        // Process attack
        AbstractMove<?, ? super A> move = this.curMove;
        moveStun--;
        if (move != null) {
            // Make sure the correct holding type is set
            MoveInputType curMoveInputType = MoveInputType.fromMoveClass(move.getMoveClass());
            if (canHoldMove(curMoveInputType) && getHoldingType() != curMoveInputType) {
                setHoldingType(curMoveInputType);
                //setHolding(true);
            }

            MoveTickQueue.queueTick(getThis(), move, getMoveStun());
        }
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public boolean isHolding() {
        return holding;
    }

    @Override
    public void setHolding(boolean holding) {
        this.holding = holding;
    }

    @Override
    public MoveInputType getHoldingType() {
        return holdingType;
    }

    @Override
    public void setHoldingType(MoveInputType holdingType) {
        this.holdingType = holdingType;
    }

    @Override
    public void setPerformedThisTick(boolean b) {
        performedThisTick = b;
    }

    @Override
    public boolean performedThisTick() {
        return performedThisTick;
    }

    public abstract A getThis();

    @Override
    public void executePlan(int aiLevel, AttackerBrainInfo info, CombatInstantContext combatCtx) {
        final CombatEntityContext attackerCtx = combatCtx.getAttackerCtx();
        final CombatEntityContext targetCtx = combatCtx.getTargetCtx();

        final Mob mob = (Mob) attackerCtx.entity(); // Guaranteed by contract
        final PathfinderMob pathfinder = (mob instanceof PathfinderMob pathfinderMob) ? pathfinderMob : null;
        final LivingEntity target = targetCtx.entity();
        final LookControl lookControl = mob.getLookControl();
        final JumpControl jumpControl = mob.getJumpControl();

        final AttackerBrainInfo.State state = info.getState();

        final RandomSource random = getBaseEntity().getRandom();

        switch (state) {
            case IDLE -> {}
            case APPROACH -> {
                final PathNavigation navigation = mob.getNavigation();
                navigation.moveTo(target, 1.0);

                lookControl.setLookAt(target);

                if (aiLevel < IJAttackerBrain.BEGINNER_LEVEL) break;
                if (random.nextFloat() > 0.2f) {
                    DashData.tryDash(random.nextBoolean() ? -1 : 1, random.nextBoolean() ? -1 : 1, user);
                    if (random.nextBoolean())
                        jumpControl.jump();
                }
            }
            case PRESSURE, COMBOING -> {
                final PathNavigation navigation = mob.getNavigation();
                Path path = navigation.createPath(target, 1); // as opposed to a stands 2
                if (path != null) navigation.moveTo(path, 1.0);

                lookControl.setLookAt(target);

                if (info.desiresNoAttack()) break;
                doMoveSelection(
                        info,
                        mob,
                        target,
                        mob.getJumpControl(),
                        targetCtx.stand(),
                        targetCtx.standAttack() != null ? targetCtx.standAttack() : targetCtx.specAttack(),
                        combatCtx.getDistanceBetween(),
                        targetCtx.moveStun(),
                        targetCtx.stun() != null ? targetCtx.stun().getDuration() : 0
                );

                if (aiLevel < IJAttackerBrain.BEGINNER_LEVEL) break;
                if (random.nextFloat() > 0.1f) DashData.tryDash(1, random.nextBoolean() ? -1 : 1, user);
            }
            case DISENGAGE, KEEPAWAY, DEFENSE -> {
                if (pathfinder == null) break;
                if (info.getAwayPos() == null || pathfinder.distanceToSqr(info.getAwayPos()) < 3.0) {
                    info.setAwayPos(DefaultRandomPos.getPosAway(pathfinder, state == AttackerBrainInfo.State.DISENGAGE ? 16 : 8, 7, target.position()));
                }
                final Vec3 away = info.getAwayPos();
                if (away != null) {
                    mob.getNavigation().moveTo(away.x, away.y, away.z, 1.0);
                }

                lookControl.setLookAt(target);

                if (info.desiresNoAttack()) break;
                doMoveSelection(
                        info,
                        mob,
                        target,
                        mob.getJumpControl(),
                        targetCtx.stand(),
                        targetCtx.standAttack() != null ? targetCtx.standAttack() : targetCtx.specAttack(),
                        combatCtx.getDistanceBetween(),
                        targetCtx.moveStun(),
                        targetCtx.stun() != null ? targetCtx.stun().getDuration() : 0
                );

                if (aiLevel < IJAttackerBrain.BEGINNER_LEVEL) break;
                if (random.nextFloat() > 0.1f) DashData.tryDash(1, random.nextBoolean() ? -1 : 1, user);
            }
            case COMBOED -> {
                decideComboBreak(aiLevel, combatCtx);
            }
            default -> throw new IllegalStateException("Unexpected value: " + info.getState());
        }
    }
    @Override
    public AbstractMove<?, ? super A> doMoveSelection(AttackerBrainInfo info, Mob mob, LivingEntity target, JumpControl mobJumpControl,
                                                      StandEntity<?, ?> enemyStand, AbstractMove<?, ?> enemyAttack,
                                                      double distance, int enemyMoveStun, int stunTicks) {
        final StandEntity<?, ?> stand = JUtils.getStand(mob);
        if (stand != null && stand.allowMoveHandling()) return null;

        return IAttacker.super.doMoveSelection(info, mob, target, mobJumpControl, enemyStand, enemyAttack, distance, enemyMoveStun, stunTicks);
    }
}
