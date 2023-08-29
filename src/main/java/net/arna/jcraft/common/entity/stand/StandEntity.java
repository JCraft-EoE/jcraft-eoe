package net.arna.jcraft.common.entity.stand;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.MoveQueue;
import net.arna.jcraft.common.attack.moves.base.AbstractBarrageAttack;
import net.arna.jcraft.common.attack.moves.base.AbstractCounterAttack;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.component.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.network.s2c.ComboCounterPacket;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.mixin.LivingEntityInvoker;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.JumpControl;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.List;

import static net.arna.jcraft.JCraft.comboBreak;
import static net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor;

public abstract class StandEntity<E extends StandEntity<E, S>, S extends Enum<S> & StandAnimationState<E>>
        extends MobEntity implements IAnimatable, IAnimationTickable, IAttacker<E, S> {

    // TODO: finish custom player idle poses for all stands

    @SuppressWarnings("NotNullFieldNotInitialized") // It does get initialized by a method called in the constructor.
    @Getter
    private @NonNull MoveMap<E, S> moveMap;
    @Getter
    protected final MoveContext moveContext = new MoveContext();

    private static final TrackedData<Integer> STATE;
    private static final TrackedData<Boolean> SAMESTATE; // Marks if the state was set to what it already was during the last setState() call
    private static final TrackedData<Boolean> RESET; // Set to true when state is set to idle. Set back to false when the after-idle reset code has run.
    private static final TrackedData<Integer> MOVESTUN;

    private static final TrackedData<Integer> SKIN;
    private static final TrackedData<Float> ROTATIONOFFSET;
    private static final TrackedData<Float> DISTANCEOFFSET;

    private static final TrackedData<Float> ALPHA_OVERRIDE;

    private static final TrackedData<Float> STANDGAUGE;

    private static final TrackedData<Float> FREEX;
    private static final TrackedData<Float> FREEY;
    private static final TrackedData<Float> FREEZ;

    private static final TrackedData<Boolean> FREE;
    private static final TrackedData<Boolean> REMOTE;

    @Getter
    @Nullable
    private final SoundEvent summonSound;
    private final boolean playGenericSummonSound;

    @Setter
    protected int tsTime = 0;
    @Getter
    private float prevAlpha = 1f;

    @Getter @Setter
    @Nullable
    private LivingEntity user = null;


    public boolean blocking = false;
    protected boolean idleOverride = false;

    protected float idleDistance = 1.25f;
    protected float idleRotation = -45f;
    public final float attackRotation = 90f;
    protected float blockDistance = 0.75f;

    protected float maxStandGauge = 90f;

    public MoveQueue queuedAttack;
    public AbstractMove<?, ? super E> curMove;
    public AbstractMove<?, ? super E> prevMove;
    public int armorPoints;

    // Info
    public List<String> pros;
    public List<String> cons;
    public String description = "UNDESCRIBED";
    public String freespace;

    public int lastRemoteInputTime;
    public Vec3d remoteSpeed = Vec3d.ZERO;
    @Getter
    private double remoteForwardInput = 0;
    @Getter
    private double remoteSideInput = 0;
    private boolean remoteJumpInput = false;

    @Getter
    private final StandType standType;

    private final AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);
    protected int summonAnimDuration = 19;
    private boolean playSummonAnim = true;

    protected StandEntity(StandType type, World world) {
        this(type, world, null, true);
    }

    protected StandEntity(StandType type, World world, @Nullable SoundEvent summonSound) {
        this(type, world, summonSound, false);
    }

    protected StandEntity(StandType type, World world, @Nullable SoundEvent summonSound, boolean playGenericSummonSound) {
        super(type.getEntityType(), world);
        noClip = true;
        standType = type;
        this.summonSound = summonSound;
        this.playGenericSummonSound = playGenericSummonSound;

        assert getThis() == this;

        registerMoves();
    }

    // State controls
    static {
        STATE = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.INTEGER);
        SAMESTATE = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        RESET = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

        MOVESTUN = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.INTEGER);

        SKIN = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.INTEGER);
        ROTATIONOFFSET = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.FLOAT);
        DISTANCEOFFSET = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.FLOAT);

        ALPHA_OVERRIDE = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.FLOAT);

        STANDGAUGE = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.FLOAT);

        FREE = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        REMOTE = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

        FREEX = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.FLOAT);
        FREEY = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.FLOAT);
        FREEZ = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.FLOAT);
    }

    @NotNull
    public LivingEntity getUserOrThrow() {
        if (user == null) throw new NullPointerException("No user set");
        return user;
    }

    public boolean hasUser() {
        return user != null;
    }

    public S getState() {
        return boxState(getRawState());
    }

    public int getRawState() {
        return dataTracker.get(STATE);
    }

    private boolean isReset() {
        return dataTracker.get(RESET);
    }

    protected void setReset(boolean reset) {
        dataTracker.set(RESET, reset);
    }

    /**
     * Sets the stands state directly
     */
    public void setStateNoReset(S state) {
        setRawStateNoReset(state.ordinal());
    }

    public void setRawStateNoReset(int state) {
        dataTracker.set(STATE, state);
    }

    /**
     * Sets the stands state with extra processing
     */
    public void setState(S state) {
        setRawState(state.ordinal());
    }

    public void setRawState(int state) {
        int oldState = getRawState();
        boolean sameState = oldState == state || oldState <= 1;
        dataTracker.set(STATE, state);
        dataTracker.set(SAMESTATE, sameState); // Pretty much just an animation reset flag
        // If we're switched states and are moving to idle, perform reset logic.
        setReset(!sameState && state == getIdleState().ordinal());
    }

    public boolean isSameState() {
        return dataTracker.get(SAMESTATE);
    }

    public void setSameState(boolean sameState) {
        dataTracker.set(SAMESTATE, sameState);
    }

    public int getMoveStun() {
        return dataTracker.get(MOVESTUN);
    }

    /**
     * Sets how many ticks the stand will be occupied doing an animation for
     */
    public void setMoveStun(int moveStun) {
        dataTracker.set(MOVESTUN, moveStun);
    }

    public int getSkin() {
        return dataTracker.get(SKIN);
    }

    public void setSkin(int skin) {
        if (skin < 0 || skin > getStandType().getSkinCount()) skin = 0;
        dataTracker.set(SKIN, skin);
    }

    public float getRotationOffset() {
        return this.dataTracker.get(ROTATIONOFFSET);
    }

    /**
     * Sets the angle of the offset the stand is at relative to the user, used in the cylindrical coordinates system in {@link net.arna.jcraft.mixin.EntityMixin}
     */
    public void setRotationOffset(float rotationOffset) {
        this.dataTracker.set(ROTATIONOFFSET, rotationOffset);
    }

    public float getDistanceOffset() {
        return this.dataTracker.get(DISTANCEOFFSET);
    }

    /**
     * Sets the distance between the stand and user
     */
    public void setDistanceOffset(float distanceOffset) {
        this.dataTracker.set(DISTANCEOFFSET, distanceOffset);
    }

    public boolean hasAlphaOverride() {
        return getAlphaOverride() >= 0;
    }

    public float getAlphaOverride() {
        return this.dataTracker.get(ALPHA_OVERRIDE);
    }

    public void setAlphaOverride(float alpha) {
        dataTracker.set(ALPHA_OVERRIDE, alpha);
    }

    public void resetAlphaOverride() {
        setAlphaOverride(-1);
    }

    public float getStandGauge() {
        return this.dataTracker.get(STANDGAUGE);
    }

    public void setStandGauge(float standGauge) {
        this.dataTracker.set(STANDGAUGE, standGauge);
    }

    public boolean isFree() {
        return this.dataTracker.get(FREE);
    }

    /**
     * Changes whether the stand is detached from the user
     */
    public void setFree(boolean free) {
        this.dataTracker.set(FREE, free);
    }

    public void setRemoteJumpInput(boolean b) {
        remoteJumpInput = b;
    }

    public boolean getRemoteJumpInput() {
        return remoteJumpInput;
    }

    /**
     * Called in the constructor of this class. Registers all moves by calling {@link #registerMoves(MoveMap)}.
     * Call this if you wish to re-register the moves for some reason. Doing so will reset the {@link MoveMap}.
     */
    protected final void registerMoves() {
        registerMoves(moveMap = new MoveMap<>());
        moveMap.freeze();
        moveMap.forEach(entry -> entry.getMove().registerContextEntries(moveContext));
    }

    protected abstract void registerMoves(MoveMap<E, S> moves);

    /**
     * Synchronises the user inputs serverside
     */
    public void updateRemoteInputs(int f, int s, boolean j) {
        // These persist, so implementation for cleaning should be done in the stand code
        Vec3d v = new Vec3d(f, 0, s).normalize();
        remoteForwardInput = v.x;
        remoteSideInput = v.z;
        remoteJumpInput = j;
        lastRemoteInputTime = age;
    }

    public boolean isRemote() {
        return this.dataTracker.get(REMOTE);
    }

    public void setRemote(boolean r) {
        this.dataTracker.set(REMOTE, r);
        if (r) beginRemote();
        else endRemote();
    }

    /**
     * Puts the stand into remote mode
     */
    protected void beginRemote() {
        if (user == null) return;

        setFree(true);

        Vec3d fPos = user.getPos().add(user.getRotationVector());
        remoteSpeed = user.getVelocity(); // Inertia
        remoteSpeed = new Vec3d(remoteSpeed.x * 5, remoteSpeed.y / 2, remoteSpeed.z * 5);

        setAlphaOverride(0.1f);

        detach();

        noClip = false;
        velocityDirty = true;
        setPos(fPos.x, user.getY() + 0.5, fPos.z);
    }

    /**
     * Ends remote mode instantly
     */
    protected void endRemote() {
        setFree(false);
        resetAlphaOverride();
        startRiding(user);
        noClip = true;
    }

    /*
     * Returns whether the utility should be used by the stand, otherwise calls initClientUtility()
     */
    /*
    @Environment(EnvType.CLIENT)
    public boolean allowUtilityUse() {
        return true;
    }
    public void initClientUtility() {
    }
    */

    /**
     * Gets the stands position while detached
     */
    public Vec3f getFreePos() {
        return new Vec3f(this.dataTracker.get(FREEX), this.dataTracker.get(FREEY), this.dataTracker.get(FREEZ));
    }

    /**
     * Sets the stands position while detached
     *
     * @param freePos new position
     */
    public void setFreePos(Vec3f freePos) {
        this.dataTracker.set(FREEX, freePos.getX());
        this.dataTracker.set(FREEY, freePos.getY());
        this.dataTracker.set(FREEZ, freePos.getZ());
    }

    @Override
    public LivingEntity getBaseEntity() {
        return this;
    }

    @Override
    public DamageSource getDamageSource() {
        return JDamageSources.stand(this);
    }

    @Override
    public AbstractMove<?, ? super E> getCurrentMove() {
        return curMove;
    }

    @Override
    public void setCurrentMove(AbstractMove<?, ? super E> move) {
        curMove = move;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.PLAYERS;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(STATE, 0);
        dataTracker.startTracking(SAMESTATE, false);
        dataTracker.startTracking(RESET, true);

        dataTracker.startTracking(MOVESTUN, 0);

        dataTracker.startTracking(SKIN, 0);
        dataTracker.startTracking(ROTATIONOFFSET, -90f);
        dataTracker.startTracking(DISTANCEOFFSET, 1f);

        dataTracker.startTracking(ALPHA_OVERRIDE, -1f);

        dataTracker.startTracking(STANDGAUGE, 45f);

        dataTracker.startTracking(FREE, false);
        dataTracker.startTracking(REMOTE, false);

        dataTracker.startTracking(FREEX, 0f);
        dataTracker.startTracking(FREEY, 0f);
        dataTracker.startTracking(FREEZ, 0f);
    }

    // Attack controls

    /**
     * @return whether the stand should be able to attack
     */
    public boolean canAttack() {
        return hasUser() && getMoveStun() <= 0 && !JUtils.isAffectedByTimeStop(user) && !getUserOrThrow().hasStatusEffect(JStatusRegistry.DAZED);
    }

    /**
     * @return whether the stand should change its height depending on the user's look pitch
     */
    public boolean shouldOffsetHeight() {
        return getState().ordinal() > 1;
    }

    public boolean handleMove(MoveType type) {
        MoveMap.Entry<E, S> entry = getMoveMap().getFirstValidEntry(type, getThis());
        if (entry == null) return false;

        if (hasUser() && !getUserOrThrow().isOnGround() && entry.getAerialVariant() != null)
            entry = entry.getAerialVariant();
        // This means crouching aerial variants are also supported. :O
        if (hasUser() && getUserOrThrow().isSneaking() && entry.getCrouchingVariant() != null)
            entry = entry.getCrouchingVariant();
        // Ensure a crouching variant of an aerial variant and an aerial variant of a crouching variant both work.
        if (hasUser() && !getUserOrThrow().isOnGround() && entry.getAerialVariant() != null)
            entry = entry.getAerialVariant();

        return handleMove(entry.getMove(), entry.getCooldownType(), entry.getAnimState());
    }

    /**
     * Initiates an attack with the stand
     *
     * @param move         move to handle
     * @param cooldownType type of cooldown to start
     * @param animState    the state to put the stand into
     */
    public boolean handleMove(AbstractMove<?, ? super E> move, CooldownType cooldownType, @Nullable S animState) {
        if (!move.canBeInitiated(getThis())) return false;
        move.onInitialize(getThis());

        if (cooldownType != null && move.getCooldown() > 0) {
            CooldownsComponent cooldowns = JComponents.getCooldowns(getUser());
            int cooldown = cooldowns.getCooldown(cooldownType);

            if (cooldown > 0) return false;

            cooldowns.setCooldown(cooldownType, move.getCooldown());
        }

        setMove(move, animState);
        return true;
    }

    /**
     * Instantly sets the stand's move
     *
     * @param move    move to set
     * @param animState int identifier for which state to put the stand into
     */
    public void setMove(AbstractMove<?, ? super E> move, @Nullable S animState) {
        // If the attack has a duration of 0, just perform it immediately.
        if (move.getDuration() == 0) {
            move.doPerform(getThis());
            return;
        }

        curMove = move;
        setMoveStun(move.getDuration());
        if (animState != null) setState(animState);
        armorPoints = move.getArmor();
    }

    /**
     * Stuns specified {@link LivingEntity}
     *
     * @param entity    victim to stun
     * @param duration  in ticks
     * @param amplifier level of stun
     */
    public static void stun(LivingEntity entity, int duration, int amplifier) {
        if (entity == null || duration == 0) return;
        entity.addStatusEffect(new StatusEffectInstance(JStatusRegistry.DAZED, duration, amplifier, false, false, true));
        //JCraft.LOGGER.info("Stunned: " + entity.getEntityName() + " for: " + duration + " with stunType: " + amplifier);
    }

    /**
     * Basic damage method, you likely want to use baseDamageLogic or damageLogic instead
     *
     * @param damage       damage in half hearts
     * @param damageSource source of damage
     * @param ent          entity to harm
     */
    public static void damage(float damage, DamageSource damageSource, LivingEntity ent) {
        if (!JUtils.canDamage(damageSource, ent)) return;

        float scaling = ((IDamageScaler)ent).jcraft$getDamageScaling();
        //JCraft.LOGGER.info("Damaging entity: " + ent + " with damage: " + damage + " and scaling: " + scaling);
        damage *= scaling;

        // All stands ignore 10% of armor & armor toughness
        damage = DamageUtil.getDamageLeft(damage, (float) ent.getArmor() * 0.9f, (float) ent.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS) * 0.9f);
        damage = ((LivingEntityInvoker) ent).invokeModifyAppliedDamage(damageSource, damage);

        // Apply absorption
        applyAbsorptionAndStats(damage, damageSource, ent);
    }

    private static void applyAbsorptionAndStats(float damage, DamageSource damageSource, LivingEntity ent) {
        float f = damage;
        damage = Math.max(damage - ent.getAbsorptionAmount(), 0.0F);
        ent.setAbsorptionAmount(ent.getAbsorptionAmount() - (f - damage));

        if (damage <= 0) return;

        float h = ent.getHealth();

        LivingEntityInvoker invoker = (LivingEntityInvoker) ent;

        // Statistics
        World world = ent.getWorld();
        if (!(ent instanceof PlayerEntity)) world.sendEntityStatus(ent, (byte) 2);

        invoker.setLastDamageTaken(damage);
        invoker.setLastDamageSource(damageSource);
        invoker.setLastDamageTime(world.getTime());

        ent.timeUntilRegen = 20;
        ent.maxHurtTime = ent.hurtTime = 10;

        ent.setHealth(h - damage);
        ent.getDamageTracker().onDamage(damageSource, h, damage);
        ent.emitGameEvent(GameEvent.ENTITY_DAMAGE);
        if (ent.isDead()) ent.onDeath(damageSource);
    }

    /**
     * Basic damage method, ignores potion effects and enchantments, accounts for armor and damage scaling
     *
     * @param damage       damage in half hearts
     * @param damageSource source of damage
     * @param ent          entity to harm
     */
    public static void trueDamage(float damage, DamageSource damageSource, LivingEntity ent) {
        if (ent == null || ent.isRemoved() || ent.isDead()) return;

        float scaling = ((IDamageScaler)ent).jcraft$getDamageScaling();
        //JCraft.LOGGER.info("True damaging entity: " + ent + " with damage: " + damage + " and scaling: " + scaling);
        damage *= scaling;

        // All stands ignore 10% of armor & armor toughness
        damage = DamageUtil.getDamageLeft(damage, (float) ent.getArmor() * 0.9f, (float) ent.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS) * 0.9f);

        // Apply absorption
        applyAbsorptionAndStats(damage, damageSource, ent);
    }

    // Stock attacks to define
    public void initMove(MoveType type) {
        handleMove(type);
    }

    /**
     * Defines what happens while the stand is blocking
     */
    public void standBlock() {
        if (!hasUser()) return;
        // Projectile deflection
        List<ProjectileEntity> toDeflect = this.world.getEntitiesByClass(ProjectileEntity.class, this.getBoundingBox().expand(0.75f), EntityPredicates.VALID_ENTITY);

        for (ProjectileEntity projectile : toDeflect) {
            if (projectile.getOwner() == user) continue;
            projectile.setVelocity(projectile.getVelocity().multiply(-0.5).add(0, -0.1, 0));
            projectile.velocityModified = true;
        }

        stun(user, 2, 2);
        getUserOrThrow().addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 5, 3, false, false, true));
    }

    // Define desummon conditions
    public void desummon() {
        if (curMove != null || getMoveStun() > 0) return;
        discard();
    }

    // Define idle override
    public void idleOverride() {}

    /**
     * Cancels the stand's move instantly
     */
    public void cancelMove() {
        if (curMove != null) curMove.onCancel(getThis());
        curMove = null;
        setMoveStun(0);
        setState(getIdleState());
        setReset(true);
    }

    /**
     * Returns whether the stand defaults to returning to the user while idle and detached
     */
    public boolean defaultToNear() {
        return !isRemote();
    }

    @Override
    public boolean hasNoGravity() {
        if (isFree() && !isRemote()) return true;
        return super.hasNoGravity();
    }

    /**
     * does evrything :)
     */
    @Override
    public void tick() {
        if (user == null && getVehicle() instanceof LivingEntity vehicle) user = vehicle;

        super.tick();

        if (isDead()) return;

        if (age == 1) playSummonSound();

        boolean client = world.isClient;

        prevAlpha = getAlphaOverride();

        if (user == null) {
            if (client && getVehicle() instanceof LivingEntity living)
                user = living;
            return;
        } //else if (this.owner == null) { this.owner = player; }

        setMoveStun(getMoveStun() - 1); // Counting down animation time or similar
        if (playSummonAnim && (getMoveStun() > 0 || age > summonAnimDuration))
            playSummonAnim = false;

        AbstractMove<?, ? super E> attack = this.curMove;

        Direction gravDir = GravityChangerAPI.getGravityDirection(user);

        Vec3d pos = this.getPos();
        Vec3d rotVec = getRotationVector();
        if (gravDir == Direction.UP)
            rotVec = new Vec3d(rotVec.x, -rotVec.y, rotVec.z);

        boolean isFree = isFree();
        boolean isRemote = isRemote();

        // Common code for remote mode
        if (isRemote) {
            if (hasVehicle()) detach();
            if (user.isAlive()) {
                // Clientside rotational sync for remote mode
                user.setBodyYaw(user.getHeadYaw());

                setHeadYaw(user.getHeadYaw());
                setRotation(user.getYaw(), user.getPitch());
            } else discard();
        } else if (!hasVehicle() && !isFree())
            startRiding(user, true);

        /*
        JCraft.LOGGER.info(
                (client ? "CLIENT:" : "SERVER:") + " Ticking stand " + this +
                        "\nUser: " + user +
                        "\nVehicle (stand): " + getVehicle() +
                        "\nFree: " + getFree() +
                        "\nRemote: " + getRemote()
        );
         */

        if (!client) {
            // Reset samestate
            if (isSameState()) setSameState(false);

            // Make sure the user is using this stand
            if (JUtils.getStand(user) != this) discard();

            // Block break check
            if (getStandGauge() < 1) {
                user.addStatusEffect(new StatusEffectInstance(JStatusRegistry.DAZED, 40, 2));
                playSound(SoundEvents.ITEM_TOTEM_USE, 1, 0.5f);
                kill();
            }


            if (defaultToNear() && getMoveStun() < 1) {
                if (attack == null) {
                    if (this.queuedAttack == null)
                        setFree(false);
                } else if (attack.isCounter()) ((AbstractCounterAttack<?, ? super E>) attack).whiff(getThis(), user);
            }

            // Rotate with user
            if (!isFree || isRemote) {
                setHeadYaw(user.getHeadYaw());
                setRotation(user.getYaw(), user.getPitch());
            }

            // Remote mode
            if (isRemote) user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 5, 9, true, false));

            int curMoveStun = this.getMoveStun();

            // Attack logic
            if (attack != null) {
                attack.tick(getThis());

                if (curMoveStun >= 0 && !blocking) {
                    int moveStun = attack.getDuration();
                    float attackDist = attack.getMoveDistance();

                    int realInitTime = (moveStun - attack.getWindup());

                    boolean isChargeAttack = attack.isCharge();
                    // Positioning
                    if (isChargeAttack) {
                        if (curMoveStun <= realInitTime) {
                            //float t = 1f - (float) curMoveStun / (float) realInitTime;
                            Vec3d newPos = pos.add(rotVec.multiply(attackDist / realInitTime));
                            //this.setDistanceOffset(1 + attackDist * t * t);
                            this.setFreePos(new Vec3f((float) newPos.x, (float) newPos.y, (float) newPos.z));
                            this.setFree(true);
                        } else {
                            setPosition(user.getPos());
                            setRotationOffset(attackRotation);
                        }
                    } else {
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 5, 4, true, false));

                        setAttackRotationOffset();
                        setDistanceOffset(attackDist);
                    }
                }
            }

            if (curMoveStun <= 0 && !blocking) {
                // Attack buffering
                if (queuedAttack != null) {
                    if (queuedAttack == MoveQueue.STAND_SUMMON) {
                        curMove = null;
                        desummon();
                    } else handleMove(queuedAttack.getMoveType());

                    queuedAttack = null;
                } else if (!idleOverride) {
                    // Process idle
                    curMove = null;

                    setStandGauge(MathHelper.clamp(this.getStandGauge() + 0.5f, 0, maxStandGauge));

                    if (getRawState() != 0 || isReset()) {
                        setRawState(0);
                        setReset(false);

                        setDistanceOffset(idleDistance);
                        setRotationOffset(idleRotation);
                    }
                } else idleOverride();
            } else if (blocking) { // Process block
                curMove = null;
                setStateNoReset(getBlockState());

                if (curMoveStun < 4) setMoveStun(4);
                setDistanceOffset(blockDistance);
                setRotationOffset(attackRotation);
                standBlock();
            }

            tsTime--;
        }

        // JCraft.LOGGER.info( "State: " + this.getState() + " Movestun: " + curMoveStun + " Currently attacking: " + (this.curAttack != null)); // Massive debug log

        if (curMove != prevMove && curMove != null)
            //JCraft.LOGGER.info("Logged previous attack change: " + this.curAttack + " " + this.previousAttack);
            prevMove = curMove;
    }

    /**
     * Called when curAttack isn't null, and it's being processed
     * Sets the StandEntities rotation (in cylindrical coordinates) to the attack position
     */
    public void setAttackRotationOffset() {
        setRotationOffset(attackRotation);
    }

    /**
     * Highest level damage method, handles combo counting, DEFAULTS unblockable TO FALSE
     *
     * @param world        world to process damage in
     * @param ent          victim
     * @param kbVec        knockback vector to apply
     * @param stunTicks    stun duration in ticks
     * @param overrideStun will the attack override all other types of stun?
     * @param damage       damage in half hearts
     * @param lift         will the attack lift the victim upon an aerial hit?
     */
    public static void damageLogic(World world, LivingEntity ent, Vec3d kbVec, int stunTicks, int stunLevel,
                                   boolean overrideStun, float damage, boolean lift, int blockstun, DamageSource source,
                                   Entity attacker, boolean canBackstab, boolean unblockable) {
        if (world == null || world.isClient || ent == null || !ent.canTakeDamage()) return;
        if (world.getGameRules().getBoolean(JCraft.COMBO_COUNTER) && attacker instanceof ServerPlayerEntity playerEntity)
            comboCounterLogic(playerEntity, ent);

        baseDamageLogic(ent, kbVec, stunTicks, stunLevel, overrideStun, damage, lift, blockstun, source, attacker, canBackstab, unblockable);
    }

    /**
     * Highest level damage method, handles combo counting, DEFAULTS unblockable TO FALSE
     *
     * @param world        world to process damage in
     * @param ent          victim
     * @param kbVec        knockback vector to apply
     * @param stunTicks    stun duration in ticks
     * @param overrideStun will the attack override all other types of stun?
     * @param damage       damage in half hearts
     * @param lift         will the attack lift the victim upon an aerial hit?
     */
    public static void damageLogic(World world, LivingEntity ent, Vec3d kbVec, int stunTicks, int stunLevel, boolean overrideStun, float damage, boolean lift, int blockstun, DamageSource source, Entity attacker, boolean canBackstab) {
        if (world == null || world.isClient || ent == null || !ent.canTakeDamage()) return;
        if (world.getGameRules().getBoolean(JCraft.COMBO_COUNTER) && attacker instanceof ServerPlayerEntity playerEntity)
            comboCounterLogic(playerEntity, ent);

        baseDamageLogic(ent, kbVec, stunTicks, stunLevel, overrideStun, damage, lift, blockstun, source, attacker, canBackstab, false);
    }

    /**
     * Highest level damage method, handles combo counting, DEFAULTS canBackstab and unblockable TO FALSE
     *
     * @param world        world to process damage in
     * @param ent          victim
     * @param kbVec        knockback vector to apply
     * @param stunTicks    stun duration in ticks
     * @param overrideStun will the attack override all other types of stun?
     * @param damage       damage in half hearts
     * @param lift         will the attack lift the victim upon an aerial hit?
     */
    public static void damageLogic(World world, LivingEntity ent, Vec3d kbVec, int stunTicks, int stunLevel, boolean overrideStun, float damage, boolean lift, int blockstun, DamageSource source, Entity attacker) {
        if (world == null || world.isClient || ent == null || !ent.canTakeDamage()) return;
        if (world.getGameRules().getBoolean(JCraft.COMBO_COUNTER) && attacker instanceof ServerPlayerEntity playerEntity)
            comboCounterLogic(playerEntity, ent);
        baseDamageLogic(ent, kbVec, stunTicks, stunLevel, overrideStun, damage, lift, blockstun, source, attacker, false, false);
    }

    /**
     * Handles combo counting for specific player
     *
     * @param playerEntity attacker
     */
    private static void comboCounterLogic(ServerPlayerEntity playerEntity, LivingEntity victim) {
        IComboCounter comboCounter = (IComboCounter) playerEntity;

        if (comboCounter.getLastAttacked() != victim)
            comboCounter.jcraft$setComboCount(1);
        else {
            StatusEffectInstance stun = victim.getStatusEffect(JStatusRegistry.DAZED);
            if (stun != null && stun.getAmplifier() != 2) //LOGGER.info("Target stun: " + stun.getDuration());
                comboCounter.jcraft$incrementComboCount();
            else comboCounter.jcraft$setComboCount(1);

            ComboCounterPacket.send(playerEntity, comboCounter.jcraft$getComboCount(), ((IDamageScaler) victim).jcraft$getDamageScaling());
        }

        comboCounter.setLastAttacked(victim);
    }

    /**
     * Mid-level damage method, handles blocking, lifting, counters, velocity modification
     *
     * @param ent          victim
     * @param kbVec        knockback vector to apply
     * @param stunTicks    stun duration in ticks
     * @param overrideStun will the attack override all other types of stun?
     * @param damage       damage in half hearts
     * @param lift         will the attack lift the victim upon an aerial hit?
     */
    private static void baseDamageLogic(LivingEntity ent, Vec3d kbVec, int stunTicks, int stunLevel, boolean overrideStun,
                                        float damage, boolean lift, int blockstun, DamageSource source, Entity attacker,
                                        boolean canBackstab, boolean unblockable) {
        boolean hit = true;
        boolean tsHit = JUtils.isAffectedByTimeStop(ent);

        StandEntity<?, ?> stand = JUtils.getStand(ent);
        if (stand != null) {
            AbstractMove<?, ?> standAttack = stand.curMove;
            if (standAttack != null) {
                // Counter check
                if (!tsHit && standAttack.isCounter() && stand.getMoveStun() < (standAttack.getDuration() - standAttack.getWindup())) {
                    ((AbstractCounterAttack<?, StandEntity<?, ?>>) standAttack).counter(stand, attacker, source);
                    ent.removeStatusEffect(JStatusRegistry.DAZED);
                    return;
                }

                if (--stand.armorPoints < 0) stand.cancelMove();
            }

            if (stand.blocking && !stand.isRemote()) {
                double delta = Math.abs((ent.headYaw + 90.0f) % 360.0f - (attacker.getHeadYaw() + 90.0f) % 360.0f);
                if (canBackstab && (360.0 - delta % 360.0 < 90 || delta % 360.0 < 90) && ent.squaredDistanceTo(attacker.getPos()) >= 1.5625) { // Backstab logic
                    JCraft.createParticle((ServerWorld) attacker.getWorld(), ent.getX(), attacker.getEyeY(), ent.getZ(), JParticleType.BACK_STAB);
                    stand.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
                    stand.blocking = false;
                    overrideStun = true;
                } else if (!unblockable) { // Didn't backstab, not unblockable
                    stand.setMoveStun(blockstun);
                    stand.setStandGauge(stand.getStandGauge() - 2 * damage);
                    stand.playSound(JSoundRegistry.STAND_BLOCK, 1, 1);
                    hit = false;
                    overrideStun = false;
                } else {
                    stand.blocking = false;
                }
            }
        }

        if (tsHit) {
            stunLevel = 3;
            if (stunTicks > 20) stunTicks = 20;
            lift = false;
        }

        // Stun application & overriding
        IDamageScaler damageScaler = (IDamageScaler) ent;

        if (hit) {
            damageScaler.jcraft$increaseHitCount();

            StatusEffectInstance stun = ent.getStatusEffect(JStatusRegistry.DAZED);
            if (stun != null) {
                if (overrideStun) ent.removeStatusEffect(JStatusRegistry.DAZED);
            }

            stun(ent, stunTicks, stunLevel);

            ent.addVelocity(kbVec.x, kbVec.y, kbVec.z);
        }

        // Interrupting spec moves
        if (ent instanceof PlayerEntity playerEntity) {
            JSpec<?, ?> spec = JUtils.getSpec(playerEntity);
            if (spec != null && spec.curMove != null && --spec.armorPoints < 0) spec.cancelMove();
        }

        // Aerial hits keep the victim up
        if (lift) {
            Vec3d vel = ent.getVelocity();
            double finalY = vel.y;

            if (!ent.isOnGround())
                finalY = MathHelper.clamp(vel.y / 2, 0.085, 0.25);

            GravityChangerAPI.setWorldVelocity(ent,
                    new Vec3d(
                            MathHelper.clamp(vel.x, -1, 1),
                            MathHelper.clamp(finalY, -0.25, 0.25),
                            MathHelper.clamp(vel.z, -1, 1)
                    ));
        }

        damage(damage, source, ent);

        if (!tsHit) {
            // Velocity modification synchronisation
            if (ent instanceof ServerPlayerEntity serverPlayer)
                serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayer));
            else ent.velocityModified = true;
        }
    }

    protected boolean shouldNotPlaySummonSound() {
        return user instanceof ArmorStandEntity;
    }

    protected void playSummonSound() {
        if (shouldNotPlaySummonSound()) return;

        if (summonSound != null) playSound(summonSound, 1f, 1f);
        if (summonSound == null || playGenericSummonSound)
            playSound(JSoundRegistry.STAND_SUMMON, 1f, 1f);
    }

    @Override
    public void stopRiding() {
        if (getVehicle() == null) return;

        super.stopRiding();
        if (isRemote() || world.isClient) return;

        playSound(JSoundRegistry.STAND_DESUMMON, 1, 1);
        discard();
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        setSkin(nbt.getInt("Skin"));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        nbt.putInt("Skin", getSkin());
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.isMagic() || source.isExplosive()) return false;
        return super.damage(source, amount);
    }

    protected abstract @NonNull E getThis();

    // Physical properties
    @Override
    public void pushAwayFrom(Entity entity) {
    }

    @Override
    public boolean collidesWith(Entity other) {
        return false;
    }

    @Override
    public boolean addStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
        if (world.isClient || user == null) return false;
        return user.addStatusEffect(effect, source);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return !damageSource.isOutOfWorld();
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    /**
     * Handles AI for mob stand users
     */
    public static void standUserAI(MobEntity mob, LivingEntity target, StandEntity<?, ?> stand) {
        if (mob == target || !JUtils.canDamage(JDamageSources.stand(stand), target)) return;

        JumpControl mobJumpControl = mob.getJumpControl();

        mob.lookAtEntity(target, 30, 30); // Point body at enemy
        mob.getLookControl().lookAt(target); // Usually detrimental not to

        JSpec<?, ?> enemySpec;
        StandEntity<?, ?> enemyStand = JUtils.getStand(target);
        AbstractMove<?, ?> enemyAttack = null;
        boolean enemyHasStand = enemyStand != null;

        double distance = target.distanceTo(mob);
        int enemyMoveStun = 0;
        int blockPlusTicks = 0;

        // Get enemy stand attack (most common)
        if (enemyHasStand) {
            enemyMoveStun = enemyStand.getMoveStun();
            enemyAttack = enemyStand.curMove;

            if (enemyStand.blocking)
                blockPlusTicks = enemyMoveStun;

            distance = enemyStand.distanceTo(mob);
        }
        // If none was found, try to find a spec attack
        if (enemyAttack == null) {
            if (target instanceof PlayerEntity player) {
                enemySpec = JComponents.getSpecData(player).getSpec();

                if (enemySpec != null) {
                    enemyMoveStun = enemySpec.moveStun;
                    enemyAttack = enemySpec.curMove;
                }
            }
        }

        // Blocking logic
        boolean wantToBlock = false;
        if (enemyAttack != null && enemyMoveStun > 0) { // Only block if the attack is actually active
            // Block regardless of range if the attack is ranged, or is a barrage
            if (enemyAttack.isRanged() || enemyAttack instanceof AbstractBarrageAttack<?,?>)
                wantToBlock = true;
            // Block if the attack isn't ranged, but is within hitting distance, and doesn't block break/bypass
            if (enemyAttack instanceof AbstractSimpleAttack<?, ?> simpleEnemyAttack &&
                    enemyAttack.getMoveDistance() + simpleEnemyAttack.getHitboxSize() * 0.66 > distance &&
                    simpleEnemyAttack.getDamage() * 2 < stand.getStandGauge() && !simpleEnemyAttack.getBlockableType().isNonBlockable())
                wantToBlock = true;
        }

        // Block if falling or there are projectiles nearby
        // 2 tick check interval is efficient because block doesn't run out by then, and finding entities is expensive
        if (stand.age % 2 == 0) {
            List<ProjectileEntity> nearbyProjectiles = stand.world.getEntitiesByClass(ProjectileEntity.class, mob.getBoundingBox().expand(3), EntityPredicates.VALID_ENTITY);
            boolean anyInAir = false;
            Vec3d pos = stand.getPos();
            for (ProjectileEntity projectile : nearbyProjectiles) {
                if (projectile.getOwner() == mob) continue;
                // Is it moving towards the stand?
                if (projectile.squaredDistanceTo(pos) < new Vec3d(projectile.prevX, projectile.prevY, projectile.prevZ).squaredDistanceTo(pos)) {
                    anyInAir = true;
                    break;
                }
            }

            if (mob.fallDistance > 2 || anyInAir) wantToBlock = true;
        }

        //JCraft.LOGGER.info("Want to block: " + wantToBlock);
        stand.blocking = wantToBlock && stand.canAttack();

        StatusEffectInstance mobStun = mob.getStatusEffect(JStatusRegistry.DAZED);
        // If stunned, and about to get hit by another move, combo break sometimes
        if (mobStun != null)
            if (!stand.blocking && enemyAttack != null && enemyMoveStun > enemyAttack.getWindup() && stand.random.nextFloat() < 0.1f)
                comboBreak((ServerWorld) stand.world, mob, mobStun);

        if (!stand.blocking) {
            StatusEffectInstance stun = target.getStatusEffect(JStatusRegistry.DAZED);
            // Overestimating stun up to 1/4 of a second for longer combos and frametraps
            int stunTicks = stun != null ? stun.getDuration() + stand.random.nextInt(5) : 0;
            stunTicks += blockPlusTicks;
            stunTicks += JComponents.getTimeStopData(target).getTicks();

            AbstractMove<?, ?> selectedAttack = stand.selectAttack(mob, target, stunTicks, enemyMoveStun, distance, enemyStand, enemyAttack);

            if (selectedAttack != null) {
                boolean shouldPerformMove = stand.getMoveStun() < 1;

                if (stand.curMove != null && stand.curMove.getFollowup() != null)
                    shouldPerformMove = true;

                mob.setSneaking(selectedAttack.isCrouchingVariant());
                if (selectedAttack.isAerialVariant()) {
                    mobJumpControl.setActive();
                    mob.setOnGround(false);
                }

                if (shouldPerformMove) {
                    //JCraft.LOGGER.info("Stand User AI: Performing attack " + selectedAttack);

                    if (selectedAttack.getMoveType() == null) {
                        JCraft.LOGGER.error("Attempting to use attack with unset MoveType: " + selectedAttack.getName().getString() + ", stand: " + stand);
                    } else stand.handleMove(selectedAttack.getMoveType());
                } else stand.queuedAttack = MoveQueue.fromMoveType(selectedAttack.getMoveType());
            }

            double sideswitchDistance = 1.25;

            EntityNavigation entityNavigation = mob.getNavigation();

            boolean evade = enemyAttack != null;
            // If in range (to attack or get hit)
            if (
                    (selectedAttack instanceof AbstractSimpleAttack<?,?> simpleAttack &&
                            distance < selectedAttack.getMoveDistance() + simpleAttack.getHitboxSize() * 0.75) ||
                            (enemyAttack instanceof AbstractSimpleAttack<?,?> simpleEnemyAttack && !enemyAttack.isRanged() &&
                                    distance < enemyAttack.getMoveDistance() + simpleEnemyAttack.getHitboxSize() * 1.5)
            ) {
                // Move towards or away depending on distance and intent
                entityNavigation.setSpeed(evade ? -0.25 : 0.25);
            }

            // Dash to targeted location/evasion
            BlockPos targetPos = entityNavigation.getTargetPos();
            if (targetPos != null && mob.isOnGround() && targetPos.getSquaredDistance(target.getPos()) > 2.25)
                JCraft.tryDash(evade ? -1 : 1, evade ? stand.random.nextInt(2) - 1 : 0, mob);

            // Move away during combo to prevent point-blank misses
            float sStrafe = MathHelper.sin(stand.age * 0.02f) / 3f;
            if (stunTicks > 0) {
                float back = -0.5f;
                if (enemyHasStand && enemyStand.blocking) {
                    back = 0f;
                }
                mob.getMoveControl().strafeTo(back, sStrafe);
            } else if (distance < sideswitchDistance * 8) { // Outside of combo, strafe or jump over if close
                float fStrafe = 0f;

                // Jump if extremely close to opponent in attempt to sideswitch
                if (distance < sideswitchDistance) {
                    fStrafe = 1;
                    mobJumpControl.setActive();
                }

                mob.getMoveControl().strafeTo(fStrafe, sStrafe);
            }

        } else if (stand.getMoveStun() > 4) { // if blocking & movestun > 4 means the enemy made you block
            // Don't buffer any attacks as you are minus and will DIE
            stand.queuedAttack = null;
        }
    }

    /**
     * Tells the AI to:
     * PASS - ignore this and continue move evaluation
     * USE - use the move
     * STOP - skip to next evaluation
     */
    public enum MoveSelectionResult {
        PASS,
        USE,
        STOP
    }

    //todo: make stand user AI aware of attack variations


    /**
     * Used to help AIs that use stands with unique moves
     */
    public MoveSelectionResult specificMoveSelectionCriterion(AbstractMove<?, ? super E> attack, MobEntity mob, LivingEntity target, int stunTicks,
                                                              int enemyMoveStun, double distance, StandEntity<?, ?> enemyStand, AbstractMove<?, ?> enemyAttack) {
        return MoveSelectionResult.PASS;
    }

    private @Nullable AbstractMove<?, ? super E> selectAttack(MobEntity mob, LivingEntity target, int stunTicks, int enemyMoveStun, double distance, StandEntity<?, ?> enemyStand, AbstractMove<?, ?> enemyAttack) {
        AbstractMove<?, ? super E> selectedAttack = null;
        boolean enemyIsAttacking = enemyAttack != null;
        CooldownsComponent cooldowns = JComponents.getCooldowns(mob);

        // If the opponent is countering, don't attack
        if (enemyIsAttacking && enemyAttack.isCounter()) return null;
        int movesOnCooldown = 0;

        if (curMove != null) {
            if (curMove.getFollowup() != null)
                selectedAttack = curMove.getFollowup();
        } else {
            MoveMap.Entry<E, S> lightEntry = getMoveMap().getFirstValidEntry(MoveType.LIGHT, getThis());
            if (lightEntry == null) return null;

            selectedAttack = lightEntry.getMove();
            int selectedAttackInitTime = selectedAttack.getDuration() - selectedAttack.getWindup();

            for (MoveMap.Entry<E, S> entry : getMoveMap()) {
                AbstractMove<?, ? super E> attack = entry.getMove();
                int windupPoint = attack.getWindupPoint();

                // Discount any on-cooldown non-followup attacks
                if (cooldowns.getCooldown(entry.getCooldownType()) > 0) {
                    movesOnCooldown++;
                    continue;
                }

                // Selection of characteristic moves with custom usage logic
                MoveSelectionResult result = specificMoveSelectionCriterion(attack, mob, target, stunTicks, enemyMoveStun, distance, enemyStand, enemyAttack);
                if (result == MoveSelectionResult.USE) {
                    selectedAttack = attack;
                    break;
                }
                if (result == MoveSelectionResult.STOP) continue;

                // Use mobility if opponent is far away
                if (attack.getMobilityType() != null) {
                    // ...and isn't being comboed or is blocking
                    if (stunTicks > 0) continue;

                    if (attack.getMobilityType() != MobilityType.HIGHJUMP && distance > 6) {
                        if (target.isOnGround()) {
                            if (attack.getMobilityType() == MobilityType.TELEPORT) {
                                // Intentionally looks at target's feet as to hit the ground exactly at it
                                mob.lookAt(EntityAnchor.EYES, target.getPos());
                            } else if (attack.getMobilityType() == MobilityType.DASH) {
                                // Look at target itself as a dash works best at that angle
                                mob.lookAt(EntityAnchor.EYES, target.getEyePos().add(0, 0.5, 0));
                            }
                        }

                        if (attack.getMobilityType() == MobilityType.FLIGHT) mob.lookAt(EntityAnchor.EYES, target.getEyePos());

                        selectedAttack = attack;
                        break;
                    } // If target is considerably above the mob, or the mob is going to get hit
                    else if (target.getY() > mob.getY() + 2 || (enemyAttack != null && enemyStand != null && enemyAttack.hasWindupPassed(enemyStand))) {
                        selectedAttack = attack;
                        break;
                    }
                }

                // Use counter if opponent is using a non-ranged move
                if (enemyIsAttacking && enemyAttack != null && !enemyAttack.isRanged() && attack.isCounter()) {
                    if (enemyStand != null && !enemyStand.blocking && enemyMoveStun > 0) {
                        selectedAttack = attack;
                        break;
                    }
                    continue;
                }

                /*
                Use a barrage (or variant thereof) if the opponent is stunned, not blocking, and it's off cooldown,
                because it's a free combo extender and has a lower windup than light
                 */
                if (distance < 1.4) {
                    if (attack.isBarrage() || (attack.isMultiHit() && attack.hasWindupPassed(this))) {
                        if (enemyStand == null) {
                            selectedAttack = attack;
                            break;
                        } else if (!enemyStand.blocking) {
                            selectedAttack = attack;
                            break;
                        }
                        continue;
                    }
                }

                // If the opponent is out of exactly twice the range it would take him to get to the user within the move being complete, use a projectile
                if (attack.isRanged() && distance > attack.getDuration() * target.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * 2) {
                    mob.lookAt(EntityAnchor.EYES, target.getEyePos());
                    selectedAttack = attack;
                    break;
                }

                // If the opponent isn't using a move, prioritize attack with higher or equal initiation time
                if (windupPoint <= stunTicks && windupPoint >= selectedAttackInitTime) {
                    selectedAttackInitTime = windupPoint;
                    selectedAttack = attack;
                }
            }
        }

        if (movesOnCooldown > 5) cooldowns.cooldownCancel(); // >5 = 80+%

        // Non ranged offensive attacks are cancelled if the opponent is too far (and -1 causes an out-of-bounds error)
        if (selectedAttack != null) {
            if (!selectedAttack.isCounter() &&
                    selectedAttack.getMobilityType() == null &&
                    selectedAttack instanceof AbstractSimpleAttack<?, ?> boxAttack &&
                    boxAttack.getHitboxSize() > 0 &&
                    !selectedAttack.isRanged() &&
                    distance > selectedAttack.getMoveDistance() + boxAttack.getHitboxSize())
                selectedAttack = null;
        }

        return selectedAttack;
    }

    // Animation code
    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 0, event -> {
            AnimationController<StandEntity<E, S>> controller = event.getController();
            AnimationBuilder builder = new AnimationBuilder();

            String summonAnimation = getSummonAnimation();
            if (playSummonAnim && summonAnimation != null) {
                controller.setAnimation(builder.playOnce(summonAnimation));
                return PlayState.CONTINUE;
            }

            if (isSameState()) controller.markNeedsReload();

            getState().playAnimation(getThis(), builder);
            controller.setAnimation(builder);

            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimationFactory getFactory() {
        return animationFactory;
    }

    @Override
    public int tickTimer() {
        return age;
    }

    /**
     * Needed because the super constructor invokes some things that need this.
     * Meaning we can't use a constructor parameter.
     * @return literally just {@code State.values()}
     */
    protected abstract S[] getStateValues();

    public S boxState(int rawState) {
        return getStateValues()[rawState];
    }

    public S getIdleState() {
        return boxState(0);
    }

    public abstract S getBlockState();

    public boolean isIdle() {
        return getRawState() == 0;
    }

    public boolean isBlocking() {
        return getState() == getBlockState();
    }

    @Nullable
    protected abstract String getSummonAnimation();
}
