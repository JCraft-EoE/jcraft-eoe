package net.arna.jcraft.common.entity.stand;

import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.MoveInputType;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractBarrageAttack;
import net.arna.jcraft.common.attack.moves.base.AbstractCounterAttack;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.MainBarrageAttack;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.CooldownsComponent;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.network.c2s.PlayerInputPacket;
import net.arna.jcraft.common.network.s2c.ComboCounterPacket;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.mixin.LivingEntityInvoker;
import net.arna.jcraft.registry.JPacketRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.JumpControl;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
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
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

import static net.arna.jcraft.JCraft.comboBreak;
import static net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor;

public abstract class StandEntity<E extends StandEntity<E, S>, S extends Enum<S> & StandAnimationState<E>>
        extends MobEntity implements GeoEntity, IAttacker<E, S>, ICustomDamageHandler {

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

    @Setter
    protected int tsTime = 0;
    @Getter
    private float prevAlpha = 1f;

    @Getter @Setter
    @Nullable
    private LivingEntity user = null;

    public boolean wantToBlock = false;
    public boolean blocking = false;

    private boolean holding = false;
    protected boolean idleOverride = false;

    // In meters and degrees
    protected float idleDistance = 1.25f;
    protected float idleRotation = -45f;
    public final float attackRotation = 90f;
    protected float blockDistance = 0.75f;

    protected float maxStandGauge = 90f;

    protected MoveInputType queuedMove;
    private MoveInputType holdingType;
    public AbstractMove<?, ? super E> curMove;
    public AbstractMove<?, ? super E> prevMove;
    public int armorPoints;

    // Info
    public List<String> pros;
    public List<String> cons;
    public String description = "UNDESCRIBED";
    public String freespace;

    // Player Movement Input
    public int lastRemoteInputTime;
    public Vec3d remoteSpeed = Vec3d.ZERO;
    @Getter
    private double remoteForwardInput = 0;
    @Getter
    private double remoteSideInput = 0;
    @Setter
    private boolean remoteJumpInput = false, remoteSneakInput = false;

    // Summoning
    @Getter
    @Nullable
    private final SoundEvent summonSound;
    private final boolean playGenericSummonSound;
    protected int summonAnimDuration = 19;
    private boolean playSummonAnim = true;
    @Setter
    private boolean playSummonSound = true;
    @Setter
    private boolean playDesummonSound = true;

    // Data
    @Getter
    private final StandType standType;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected Vector3f[] auraColors = {new Vector3f(), new Vector3f(1f, 0f ,0f), new Vector3f(0f, 1f ,0f), new Vector3f(0f, 0f ,1f)};

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
        this.ignoreCameraFrustum = true;
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

    public boolean allowMoveHandling() {
        return true;
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
    public void setStateNoReset(@Nullable S state) {
        if (state == null) return;
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
    public void updateRemoteInputs(int f, int s, boolean j, boolean c) {
        // These persist, so implementation for cleaning should be done in the stand code
        Vec3d v = new Vec3d(f, 0, s).normalize();
        remoteForwardInput = v.x;
        remoteSideInput = v.z;
        remoteJumpInput = j;
        remoteSneakInput = c;
        lastRemoteInputTime = age;
    }

    public boolean getRemoteJumpInput() {
        return remoteJumpInput;
    }

    public boolean getRemoteSneakInput() {
        return remoteSneakInput;
    }

    public boolean isRemote() {
        return this.dataTracker.get(REMOTE);
    }

    /**
     * @return Whether this stand should be controllable in remote mode. Certain stands should not (i.e. {@link PurpleHazeEntity}).
     */
    public boolean remoteControllable() {
        return true;
    }

    /**
     * @return Whether this stand is in remote mode and should be controllable.
     */
    public boolean isRemoteAndControllable() {
        return isRemote() && remoteControllable();
    }

    public void setRemote(boolean r) {
        this.dataTracker.set(REMOTE, r);
        if (r) beginRemote();
        else endRemote();
    }

    public void togglePilotMode() {
        setRemote(!isRemote());
        registerMoves(); // Switching movesets
    }

    /**
     * Puts the stand into remote mode.
     * USE {@link #setRemote(boolean)} FOR PRACTICAL APPLICATION
     */
    protected void beginRemote() {
        if (user == null) return;

        setFree(true);

        Vec3d fPos = user.getPos().add(user.getRotationVector());
        remoteSpeed = user.getVelocity().multiply(2); // Inertia

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
    public Vector3f getFreePos() {
        return new Vector3f(this.dataTracker.get(FREEX), this.dataTracker.get(FREEY), this.dataTracker.get(FREEZ));
    }

    /**
     * Sets the stands position while detached
     *
     * @param freePos new position
     */
    public void setFreePos(Vector3f freePos) {
        this.dataTracker.set(FREEX, freePos.x());
        this.dataTracker.set(FREEY, freePos.y());
        this.dataTracker.set(FREEZ, freePos.z());
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
    public Vec3d getRotationVector() {
        // Ignore pitch in rotation vectors.
        return getRotationVector(0, getYaw());
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
        if (isRemote() && hasStatusEffect(JStatusRegistry.DAZED))
            return false;
        return hasUser() && getMoveStun() <= 0 && !JUtils.isAffectedByTimeStop(user) && !getUserOrThrow().hasStatusEffect(JStatusRegistry.DAZED);
    }

    /**
     * @return whether the stand should change its height depending on the user's look pitch
     * As a general rule, low-hitbox moves should modify this to false, since otherwise players may move the hitbox into the ground
     */
    public boolean shouldOffsetHeight() {
        return getState().ordinal() > 0;
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

        AbstractMove<?, ? super E> move = entry.getMove();
        return handleMove(move.shouldCopyOnUse() ? move.copy() : move, entry.getCooldownType(), entry.getAnimState());
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
     * Instantly sets the stand's move without checking if it can be.
     *
     * @param move    move to set
     * @param animState int identifier for which state to put the stand into
     */
    public void setMove(AbstractMove<?, ? super E> move, @Nullable S animState) {
        move = moveMap.getRegisteredMoveFor(move);
        move.onInitiate(getThis());

        // If the attack has a duration of 0, just perform it immediately.
        if (move.getDuration() == 0) {
            move.doPerform(getThis());
            return;
        }

        curMove = move;
        setMoveStun(move.getDuration());
        //setReset(false); // makes it worse
        if (animState != null) setState(animState);
        armorPoints = move.getArmor();
    }

    public final void onUserMoveInput(MoveInputType type, boolean pressed, boolean moveInitiated) {
        onUserMoveInput(curMove, type, pressed, moveInitiated);
    }

    /**
     * Stuns specified {@link LivingEntity}
     *
     * @param entity    victim to stun
     * @param duration  in ticks
     * @param amplifier level of stun
     */
    public static void stun(LivingEntity entity, int duration, int amplifier) {
        if (entity == null || !entity.isAlive() || duration == 0) return;
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
        if (ent instanceof PlayerEntity) {
            if (world instanceof ServerWorld serverWorld)
                serverWorld.getChunkManager().sendToNearbyPlayers(ent, ServerPlayNetworking.createS2CPacket(
                        JPacketRegistry.S2C_STAND_HURT, PacketByteBufs.create().writeVarInt(ent.getId())));
        } else {
            world.sendEntityStatus(ent, (byte) 2);
        }

        invoker.setLastDamageTaken(damage);
        invoker.setLastDamageSource(damageSource);
        invoker.setLastDamageTime(world.getTime());

        ent.timeUntilRegen = 20;
        ent.maxHurtTime = ent.hurtTime = 10;

        ent.setHealth(h - damage);
        ent.getDamageTracker().onDamage(damageSource, damage);
        ent.emitGameEvent(GameEvent.ENTITY_DAMAGE);
        if (damageSource.getAttacker() instanceof LivingEntity livingAttacker)
            ent.setAttacker(livingAttacker);
        if (ent.isDead())
            ent.onDeath(damageSource);
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

    /**
     * Initiates a move of the specified type.
     * @param type The type of move to initiate.
     * @return Whether the move was initiated.
     */
    public boolean initMove(MoveType type) {
        return handleMove(type);
    }

    public boolean canHoldMove(@Nullable MoveInputType type) {
        if (type == null || type.getMoveType() == null) return false;

        MoveMap.Entry<E, S> entry = moveMap.getFirstValidEntry(type.getMoveType(), getThis());
        return entry == null ? type.isHoldable() : MoreObjects.firstNonNull(entry.getMove().getIsHoldable(), type.isHoldable());
    }

    /**
     * Defines what happens while the stand is blocking
     */
    public void standBlock() {
        if (!hasUser()) return;
        // Projectile deflection
        List<ProjectileEntity> toDeflect = this.getWorld().getEntitiesByClass(ProjectileEntity.class, this.getBoundingBox().expand(0.75f), EntityPredicates.VALID_ENTITY);

        for (ProjectileEntity projectile : toDeflect) {
            if (projectile.getOwner() == user) continue;
            projectile.setVelocity(projectile.getVelocity().multiply(-0.5).add(0, -0.1, 0));
            projectile.velocityModified = true;
        }

        stun(user, 2, 2);
        getUserOrThrow().addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 5, 3, false, false, true));
    }

    public void tryUnblock() {
        if (getMoveStun() < 1) blocking = false;
    }

    // Define desummon conditions
    public void desummon() {
        desummon(true);
    }

    public void desummon(boolean playSound) {
        if (curMove != null || getMoveStun() > 0) return;
        playDesummonSound = playSound;
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

    @Override
    public boolean isUndead() {
        if (user != null)
            return user.isUndead();
        return super.isUndead();
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
        boolean client = getWorld().isClient;
        prevAlpha = getAlphaOverride();

        int moveStun = getMoveStun();
        if (moveStun > 0 && !(blocking && wantToBlock && moveStun == 1))
            setMoveStun(--moveStun); // Counting down animation time or similar
        if (playSummonAnim && (moveStun > 0 || age > summonAnimDuration || getState() == getBlockState()))
            playSummonAnim = false;

        boolean isFree = isFree();
        boolean isRemote = isRemote();

        if (!hasUser()) {
            if (!client && !isFree && !isRemote) discard();
            return;
        }

        // Common code for remote mode
        if (isRemote) {
            if (hasVehicle()) detach();
            if (user.isAlive()) {
                // Clientside rotational sync for controllable remote mode
                if (remoteControllable()) {
                    user.setBodyYaw(user.getHeadYaw());

                    setHeadYaw(user.getHeadYaw());
                    setRotation(user.getYaw(), user.getPitch());
                }
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

        if (client) {
            JCraft.getClientEntityHandler().standEntityClientTick(this);
        } else {
            ServerPlayerEntity userPlayer = null;
            if (user instanceof ServerPlayerEntity serverPlayerEntity)
                userPlayer = serverPlayerEntity;

            // Reset samestate
            if (isSameState()) setSameState(false);

            // Make sure the user is using this stand
            if (JUtils.getStand(user) != this) discard();

            // Block break check
            if (getStandGauge() < 1) {
                user.addStatusEffect(new StatusEffectInstance(JStatusRegistry.DAZED, 40, 2));
                playSound(SoundEvents.ITEM_TOTEM_USE, 1, 0.5f);
                blocking = false;
                kill();
            }

            AbstractMove<?, ? super E> move = this.curMove;
            if (defaultToNear() && moveStun <= 0) {
                if (move == null) {
                    if (this.queuedMove == null)
                        setFree(false);
                } else if (move.isCounter()) { //noinspection unchecked // not an issue here
                    ((AbstractCounterAttack<?, ? super E>) move).whiff(getThis(), user);
                    moveStun = 1;
                }
            }

            boolean isRemoteAndControllable = isRemote && remoteControllable();

            // Rotate with user (provided user controls the stand)
            if (!isFree || isRemoteAndControllable) {
                setHeadYaw(user.getHeadYaw());
                setRotation(user.getYaw(), user.getPitch());
            }

            // Remote mode users cannot move while controlling
            if (isRemoteAndControllable)
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 5, 9, true, false));

            // Attack logic
            if (move != null) {
                move.tick(getThis());

                // Make sure the correct holding type is set
                MoveInputType curMoveInputType = MoveInputType.fromMoveType(move.getMoveType());
                if (canHoldMove(curMoveInputType) && getHoldingType() != curMoveInputType) {
                    setHoldingType(curMoveInputType);
                    //setHolding(true);
                }

                if (moveStun >= 0 && !blocking) {
                    float attackDist = move.getMoveDistance();

                    if (!move.isCharge()) {
                        if (!isRemote)
                            user.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 5, 4, true, false));

                        setAttackRotationOffset();
                        setDistanceOffset(attackDist);
                    }
                }
            }

            if (wantToBlock && !blocking && (user == null || !JCraft.isDashing(user)) && canAttack()) {
                if (isFree() && !isRemote())
                    setFree(false);
                tryBlock();
            }

            if (moveStun <= 0 && !blocking) {
                // Attack buffering
                if (queuedMove != null) {
                    if (queuedMove == MoveInputType.STAND_SUMMON) {
                        curMove = null;
                        desummon();
                    } else {
                        if (userPlayer != null && canHoldMove(queuedMove)) {
                            setHolding(PlayerInputPacket.getInputStateManager(userPlayer).heldInputs.containsKey(queuedMove));
                            if (isHolding())
                                setHoldingType(queuedMove);
                        }
                        initMove(queuedMove.getMoveType());
                    }

                    queuedMove = null;
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
                if (wantToBlock) {
                    curMove = null;

                    if (moveStun < 1) setMoveStun(1);

                    setDistanceOffset(blockDistance);
                    setRotationOffset(attackRotation);
                    standBlock();
                    setStateNoReset(getBlockState()); // Set after standBlock() so blocking logic can account for previous state
                } else tryUnblock();
            }

            tsTime--;
        }

        // JCraft.LOGGER.info( "State: " + this.getState() + " Movestun: " + curMoveStun + " Currently attacking: " + (this.curAttack != null)); // Massive debug log

        if (curMove != prevMove && curMove != null)
            //JCraft.LOGGER.info("Logged previous attack change: " + this.curAttack + " " + this.previousAttack);
            prevMove = curMove;
    }

    public void tryBlock() {
        this.blocking = true;
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
                                   Entity attacker, HitPropertyComponent.HitAnimation hitAnimation, boolean canBackstab, boolean unblockable) {
        if (world == null || world.isClient || ent == null || !ent.canTakeDamage()) return;
        if (world.getGameRules().getBoolean(JCraft.COMBO_COUNTER) && attacker instanceof ServerPlayerEntity playerEntity)
            comboCounterLogic(playerEntity, ent);

        baseDamageLogic(ent, kbVec, stunTicks, stunLevel, overrideStun, damage, lift, blockstun, source, attacker, hitAnimation, canBackstab, unblockable);
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
    public static void damageLogic(World world, LivingEntity ent, Vec3d kbVec, int stunTicks, int stunLevel, boolean overrideStun, float damage, boolean lift, int blockstun, DamageSource source, Entity attacker, HitPropertyComponent.HitAnimation hitAnimation, boolean canBackstab) {
        if (world == null || world.isClient || ent == null || !ent.canTakeDamage()) return;
        if (world.getGameRules().getBoolean(JCraft.COMBO_COUNTER) && attacker instanceof ServerPlayerEntity playerEntity)
            comboCounterLogic(playerEntity, ent);

        baseDamageLogic(ent, kbVec, stunTicks, stunLevel, overrideStun, damage, lift, blockstun, source, attacker, hitAnimation, canBackstab, false);
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
    public static void damageLogic(World world, LivingEntity ent, Vec3d kbVec, int stunTicks, int stunLevel, boolean overrideStun, float damage, boolean lift, int blockstun, DamageSource source, Entity attacker, HitPropertyComponent.HitAnimation hitAnimation) {
        if (world == null || world.isClient || ent == null || !ent.canTakeDamage()) return;
        if (world.getGameRules().getBoolean(JCraft.COMBO_COUNTER) && attacker instanceof ServerPlayerEntity playerEntity)
            comboCounterLogic(playerEntity, ent);
        baseDamageLogic(ent, kbVec, stunTicks, stunLevel, overrideStun, damage, lift, blockstun, source, attacker, hitAnimation, false, false);
    }

    /**
     * Handles combo counting for specific player
     *
     * @param playerEntity attacker
     */
    private static void comboCounterLogic(ServerPlayerEntity playerEntity, LivingEntity victim) {
        if (victim instanceof IOwnable ownable && ownable.getMaster() == playerEntity)
            return;
        if (victim != null && !JServerConfig.ENABLE_FRIENDLY_FIRE.getValue() && victim.isTeammate(playerEntity))
            return;

        IComboCounter comboCounter = (IComboCounter) playerEntity;

        if (comboCounter.jcraft$getLastAttacked() != victim)
            comboCounter.jcraft$setComboCount(1);
        else {
            StatusEffectInstance stun = comboCounter.jcraft$getLastAttacked().getStatusEffect(JStatusRegistry.DAZED);
            if (stun != null && stun.getAmplifier() != 2)
                comboCounter.jcraft$incrementComboCount();
            else comboCounter.jcraft$setComboCount(1);

            ComboCounterPacket.send(playerEntity, comboCounter.jcraft$getComboCount(), ((IDamageScaler) victim).jcraft$getDamageScaling());
        }

        comboCounter.jcraft$setLastAttacked(victim);
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
     * @param hitAnimation animation the opponent will do when they are hit
     */
    private static void baseDamageLogic(LivingEntity ent, Vec3d kbVec, int stunTicks, int stunLevel, boolean overrideStun,
                                        float damage, boolean lift, int blockstun, DamageSource source, @Nullable Entity attacker,
                                        HitPropertyComponent.HitAnimation hitAnimation, boolean canBackstab, boolean unblockable) {
        if (ent instanceof ICustomDamageHandler customDamageHandler)
            if (!customDamageHandler.handleDamage(kbVec, stunTicks, stunLevel, overrideStun, damage, lift, blockstun, source, attacker, hitAnimation, canBackstab, unblockable))
                return;

        if (ent != null && !JServerConfig.ENABLE_FRIENDLY_FIRE.getValue() && ent.isTeammate(attacker))
            return;

        boolean hit = true;
        boolean tsHit = JUtils.isAffectedByTimeStop(ent);

        StandEntity<?, ?> stand = JUtils.getStand(ent);
        if (stand != null) {
            AbstractMove<?, ?> standAttack = stand.curMove;
            if (standAttack != null) {
                // Counter check
                if (!tsHit && standAttack.isCounter() && stand.getMoveStun() < standAttack.getWindupPoint()) {
                    //noinspection unchecked
                    ((AbstractCounterAttack<?, StandEntity<?, ?>>) standAttack).counter(stand, attacker, source);
                    ent.removeStatusEffect(JStatusRegistry.DAZED);
                    return;
                }

                if (--stand.armorPoints < 0)
                    stand.cancelMove();
                else
                    JComponents.getMiscData(ent).displayArmoredHit();
            }

            if (stand.blocking && !stand.isRemote()) {
                boolean backstabbed = false;
                if (attacker != null) {
                    double delta = Math.abs((ent.headYaw + 90.0f) % 360.0f - (attacker.getHeadYaw() + 90.0f) % 360.0f);
                    if (canBackstab && (360.0 - delta % 360.0 < 45 || delta % 360.0 < 45) && ent.squaredDistanceTo(attacker.getPos()) >= 1.5625) { // Backstab logic
                        JCraft.createParticle((ServerWorld) attacker.getWorld(), ent.getX(), attacker.getEyeY(), ent.getZ(), JParticleType.BACK_STAB);
                        stand.playSound(JSoundRegistry.BACKSTAB, 1, 1);
                        stand.blocking = false;
                        overrideStun = true;
                        backstabbed = true;
                    }
                }

                if (!backstabbed && !unblockable) { // Didn't backstab, not unblockable
                    //JCraft.LOGGER.info("Enemy blocked attack, setting blockstun to: " + blockstun);
                    stand.setMoveStun(blockstun);
                    stand.setStandGauge(stand.getStandGauge() - 2 * damage);
                    stand.playSound(JSoundRegistry.STAND_BLOCK, 1, 1);
                    hit = false;
                    overrideStun = false;
                } else stand.blocking = false;
            }
        }

        if (tsHit) {
            stunLevel = 3;
            if (stunTicks > 20) stunTicks = 20;
            lift = false;
        }

        // Stun application & overriding
        IDamageScaler damageScaler = (IDamageScaler) ent;

        if (JServerConfig.ENABLE_IPS.getValue()) {
            float scaling = damageScaler.jcraft$getDamageScaling();
            stunTicks *= scaling * 0.2 + 0.8;
        }

        if (hit) {
            damageScaler.jcraft$increaseHitCount();

            StatusEffectInstance stun = ent.getStatusEffect(JStatusRegistry.DAZED);
            if (stun != null)
                if (overrideStun) ent.removeStatusEffect(JStatusRegistry.DAZED);

            stun(ent, stunTicks, stunLevel);

            if (hitAnimation != null)
                JComponents.getHitProperties(ent).setHitAnimation(hitAnimation, stunTicks);

            if (!tsHit)
                ent.addVelocity(kbVec.x, kbVec.y, kbVec.z);
        }

        // Interrupting spec moves
        if (ent instanceof PlayerEntity playerEntity) {
            JSpec<?, ?> spec = JUtils.getSpec(playerEntity);
            if (spec != null && spec.curMove != null) {
                if (--spec.armorPoints < 0)
                    spec.cancelMove();
                else
                    JComponents.getMiscData(playerEntity).displayArmoredHit();
            }
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
        if ((ent.isDead() || ent.getHealth() <= 0f) && attacker instanceof final LivingEntity livingAttacker) {
            final StandEntity<?, ?> standAttacker = JUtils.getStand(livingAttacker);
            if (standAttacker != null) {
                standAttacker.freshKill(ent);
            }
        }

        if (tsHit)
            JComponents.getTimeStopData(ent).addTotalVelocity(kbVec);
        else
            JUtils.syncVelocityUpdate(ent);
    }

    protected boolean shouldNotPlaySummonSound() {
        return user instanceof ArmorStandEntity || !playSummonSound;
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
        if (isRemote() || getWorld().isClient) return;

        if (playDesummonSound) playSound(JSoundRegistry.STAND_DESUMMON, 1, 1);
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
        if (user == null ||
                source.getAttacker() == user ||
                user.isInvulnerableTo(source) ||
                source.isOf(DamageTypes.FALLING_BLOCK) ||
                source.isOf(DamageTypes.DROWN) ) return false;

        if (blocking && source.isOf(DamageTypes.MOB_PROJECTILE))
            return false;

        if (source.isOf(DamageTypes.MAGIC) || source.isOf(DamageTypes.EXPLOSION)) // AoE effects have damage nerfed
            amount /= 2.0F;

        if (getStandGauge() <= 0.0F || source.isOf(DamageTypes.OUT_OF_WORLD))
            return super.damage(source, amount);
        return user.damage(source, amount);
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

    public abstract @NonNull E getThis();

    // Physical properties
    @Override
    public void pushAwayFrom(Entity entity) {}

    @Override
    public boolean collidesWith(Entity other) {
        return false;
    }

    @Override
    public boolean addStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
        if (getWorld().isClient || user == null) return false;
        return user.addStatusEffect(effect, source);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        if (damageSource.getAttacker() == this) return true;
        // Non-remote stands redirect damage within the AbstractSimpleAttack targetting filters.
        // Remote stands take normal damage, then redirect it within this classes damage() method.
        return !isRemote() && !damageSource.isOf(DamageTypes.OUT_OF_WORLD);
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    /**
     * Handles AI for mob stand users
     */
    private static final double sideswitchDistance = 1.25;
    public static void standUserAI(MobEntity mob, LivingEntity target, StandEntity<?, ?> stand) {
        if (mob == target || !JUtils.canDamage(JDamageSources.stand(stand), target)) return;

        JumpControl mobJumpControl = mob.getJumpControl();

        mob.lookAtEntity(target, 30, 30); // Point body at enemy
        mob.getLookControl().lookAt(target); // Usually detrimental not to

        JSpec<?, ?> enemySpec;
        StandEntity<?, ?> enemyStand = JUtils.getStand(target);
        if (enemyStand == stand) // Stands that attack their users would tweak tf out otherwise
            enemyStand = null;
        AbstractMove<?, ?> enemyAttack = null;
        boolean enemyHasStand = enemyStand != null;

        double distance = target.distanceTo(mob);
        int enemyMoveStun = 0;
        int blockPlusTicks = 0;

        boolean wantToBlock = stand.wantToBlock;

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
        if (enemyAttack != null && enemyMoveStun > 0) { // Only block if the attack is actually active
            // Block regardless of range if the attack is ranged, or is a barrage
            if (enemyAttack.isRanged() || enemyAttack instanceof AbstractBarrageAttack<?,?>)
                wantToBlock = true;
            // Block if the attack isn't ranged, but is within hitting distance, and doesn't block break/bypass
            if (enemyAttack instanceof AbstractSimpleAttack<?, ?> simpleEnemyAttack &&
                    enemyAttack.getMoveDistance() + simpleEnemyAttack.getHitboxSize() * 0.66 > distance &&
                    simpleEnemyAttack.getDamage() * 2 < stand.getStandGauge() && !simpleEnemyAttack.getBlockableType().isNonBlockable())
                wantToBlock = true;
        } else wantToBlock = false;

        if (!enemyHasStand) { // Blocking logic against standless opponents
            CooldownsComponent cooldowns = JComponents.getCooldowns(mob);
            if (cooldowns.getCooldown(CooldownType.DASH) > 0) // Careful approach
                wantToBlock = distance > 2 && distance < 5; // Block at range <2, 5>
            else
                wantToBlock = false;
        }

        // Block if falling or there are projectiles nearby
        // 2 tick check interval is efficient because block doesn't run out by then, and finding entities is expensive
        if (stand.age % 2 == 0) {
            List<ProjectileEntity> nearbyProjectiles = stand.getWorld().getEntitiesByClass(ProjectileEntity.class, mob.getBoundingBox().expand(3), EntityPredicates.VALID_ENTITY);
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

            if (anyInAir) wantToBlock = true;
        }

        if (mob.fallDistance > 3) wantToBlock = true;

        //JCraft.LOGGER.info("Want to block: " + wantToBlock);
        stand.wantToBlock = wantToBlock;
        if (wantToBlock) {
            if (!stand.blocking && stand.canAttack() && !JCraft.isDashing(mob))
                stand.tryBlock();
        } else {
            stand.blocking = false;
        }

        StatusEffectInstance mobStun = mob.getStatusEffect(JStatusRegistry.DAZED);
        // If stunned, and about to get hit by another move, combo break sometimes
        if (mobStun != null)
            if (!stand.blocking && enemyAttack != null && enemyMoveStun > enemyAttack.getWindup() && stand.random.nextFloat() < 0.1f)
                comboBreak((ServerWorld) stand.getWorld(), mob, mobStun);


        EntityNavigation entityNavigation = mob.getNavigation();
        boolean evade = enemyAttack != null;
        if ( // in range (to get hit)
            (enemyAttack instanceof AbstractSimpleAttack<?,?> simpleEnemyAttack && !enemyAttack.isRanged() &&
                    distance < enemyAttack.getMoveDistance() + simpleEnemyAttack.getHitboxSize() * 1.5)
        ) entityNavigation.setSpeed(-0.25);

        if (!stand.blocking) {
            StatusEffectInstance stun = target.getStatusEffect(JStatusRegistry.DAZED);
            // Overestimating stun up to 1/4 of a second for longer combos and frametraps
            int stunTicks = stun != null ? stun.getDuration() + stand.random.nextInt(5) : 0;
            stunTicks += blockPlusTicks;
            stunTicks += JComponents.getTimeStopData(target).getTicks();

            // Only select or buffer attacks when necessary
            if (stand.getMoveStun() <= 1) {
                // Ensures the cooldowns are read/written to the correct entity.
                Pair<AbstractMove<?, ?>, Boolean> selectedAttackData;
                if (mob instanceof StandEntity<?, ?> standEntity && standEntity.hasUser())
                    selectedAttackData = stand.selectAttack(
                            JComponents.getCooldowns(standEntity.getUser()),
                            mob, target, stunTicks, enemyMoveStun, distance, enemyStand, enemyAttack);
                else
                    selectedAttackData = stand.selectAttack(
                            JComponents.getCooldowns(mob),
                            mob, target, stunTicks, enemyMoveStun, distance, enemyStand, enemyAttack);

                if (selectedAttackData != null) {
                    AbstractMove<?, ?> selectedAttack = selectedAttackData.getLeft();

                    if (selectedAttack != null) {
                        boolean shouldPerformMove = stand.getMoveStun() < 1;

                        if (stand.curMove != null && stand.curMove.getFollowup() != null)
                            shouldPerformMove = true;

                        mob.setSneaking(selectedAttackData.getRight());
                        if (selectedAttack.isAerialVariant()) {
                            mobJumpControl.setActive();
                            mob.setOnGround(false);
                        }

                        if (shouldPerformMove) {
                            //JCraft.LOGGER.info("Stand User AI: Performing attack " + selectedAttack);
                            if (selectedAttack.getMoveType() == null)
                                JCraft.LOGGER.error("Attempting to use attack with unset MoveType: " + selectedAttack.getName().getString() + ", stand: " + stand);
                            else
                                stand.initMove(selectedAttack.getMoveType());
                        } else
                            stand.queueMove(MoveInputType.fromMoveType(selectedAttack.getMoveType()));

                        if ( // in range (to attack)
                                (selectedAttack instanceof AbstractSimpleAttack<?, ?> simpleAttack &&
                                        distance < selectedAttack.getMoveDistance() + simpleAttack.getHitboxSize() * 0.75)
                        ) entityNavigation.setSpeed(0.25);
                    }
                }
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

        } else if (stand.getMoveStun() > 4) { // blocking & movestun > 4 likely means the enemy made you block
            // Don't buffer any attacks as you are minus and will DIE
            stand.queuedMove = null;
        }
    }

    public void queueMove(MoveInputType type) {
        if (user == null) return;
        // This check helps users intuitively use light and its followup without mis-inputting
        // Such a check should be applied to any quick move with a followup
        if (type != MoveInputType.LIGHT || JComponents.COOLDOWNS.get(user).getCooldown(CooldownType.STAND_LIGHT) <= 0)
            queuedMove = type;
    }

    public Vector3f getAuraColor() {
        return auraColors[getSkin()];
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

    /**
     * Used to help AIs that use stands with unique moves
     */
    public MoveSelectionResult specificMoveSelectionCriterion(AbstractMove<?, ? super E> attack, LivingEntity mob, LivingEntity target, int stunTicks,
                                                              int enemyMoveStun, double distance, StandEntity<?, ?> enemyStand, AbstractMove<?, ?> enemyAttack) {
        return MoveSelectionResult.PASS;
    }

    private @Nullable Pair<AbstractMove<?, ?>, Boolean> selectAttack(CooldownsComponent cooldowns, LivingEntity mob, LivingEntity target, int stunTicks, int enemyMoveStun, double distance, StandEntity<?, ?> enemyStand, AbstractMove<?, ?> enemyAttack) {
        AbstractMove<?, ? super E> selectedAttack;
        boolean needsCrouch = false;
        boolean doFinalChecks = true; // Refuses to run the move if certain conditions are met
        boolean enemyIsAttacking = enemyAttack != null;

        // If the opponent is countering, don't attack
        if (enemyIsAttacking && enemyAttack.isCounter()) return null;
        int movesOnCooldown = 0;

        MoveMap.Entry<E, S> lightEntry = getMoveMap().getFirstValidEntry(MoveType.LIGHT, getThis());
        if (lightEntry == null) {
            MoveMap.Entry<E, S> heavyEntry = getMoveMap().getFirstValidEntry(MoveType.HEAVY, getThis());
            if (heavyEntry == null) {
                JCraft.LOGGER.warn("Couldn't find light or heavy attack entry while running selectAttack on stand: " + this);
                return null;
            } else {
                selectedAttack = heavyEntry.getMove();
            }
        } else {
            selectedAttack = lightEntry.getMove();
        }

        int selectedAttackInitTime = selectedAttack.getDuration() - selectedAttack.getWindup();

        for (AbstractMove<?, ? super E> attack : getMoveMap().asMovesList()) {
            needsCrouch = attack.isCrouchingVariant();
            int windupPoint = attack.getWindupPoint();

            // Discount any on-cooldown non-followup attacks
            if (!attack.isFollowup() && cooldowns.getCooldown(attack.getMoveType().getDefaultCooldownType()) > 0) {
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

            boolean isBarrage = attack.isBarrage();
            boolean isCharge = attack.isCharge();
            if (distance <= 5) {
                //todo: expand on mob.canSee(target), because placing fences down doesn't cause them to want to break through
                if (isBarrage && !isCharge && !mob.canSee(target)) // Mine towards target if possible
                    if (attack instanceof MainBarrageAttack<?>) {
                        selectedAttack = attack;
                        needsCrouch = true;
                        doFinalChecks = false; // Disregards range limitation
                        break;
                    }

            /*
            Use a barrage (or variant thereof) if the opponent is stunned, not blocking, and it's off cooldown,
            because it's a free combo extender and has a lower windup than light
             */
                if (distance <= 2) {
                    if (isBarrage || (attack.isMultiHit() && attack.hasWindupPassed(this))) {
                        // Combo extend
                        if (enemyStand == null || !enemyStand.blocking) {
                            selectedAttack = attack;
                            break;
                        }
                        continue;
                    }
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

        if (movesOnCooldown > 5 && !(mob instanceof StandEntity<?,?>)) cooldowns.cooldownCancel(); // >5 = 80+%

        if (doFinalChecks) {
            if (selectedAttack.isCounter()) {
                if (stunTicks > 0) selectedAttack = null; // You can't combo into a counter
            } else {
                if ( // Non-ranged offensive attacks aren't chosen if the opponent is too far
                        selectedAttack.getMobilityType() == null &&
                                selectedAttack instanceof AbstractSimpleAttack<?, ?> boxAttack &&
                                boxAttack.getHitboxSize() > 0 &&
                                !selectedAttack.isRanged() &&
                                distance > selectedAttack.getMoveDistance() + boxAttack.getHitboxSize())
                    selectedAttack = null;
            }
        }

        return new Pair<>(selectedAttack, needsCrouch);
    }

    // Animation code

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController(getThis(), "controller", 0 , this::predicate));
    }

    private PlayState predicate(AnimationState state) {
        AnimationController<E> controller = state.getController();

        String summonAnimation = getSummonAnimation();
        if (playSummonAnim && summonAnimation != null) {
            return state.setAndContinue(RawAnimation.begin().thenPlay(summonAnimation));
        }

        if (isSameState()) controller.forceAnimationReset();

        S superState = getState();
        superState.playAnimation(getThis(), state);
        superState.configureController(getThis(), controller);

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean isSilent() {
        // Make stands silent if their users are.
        return super.isSilent() || (user != null && user.isSilent());
    }

    @Override
    public boolean reflectsDamage() {
        return false;
    }

    /**
     * Redirects damage from the stand to its user.
     * @return whether the damage logic should proceed in harming the stand itself
     */
    @Override
    public boolean handleDamage(Vec3d kbVec, int stunTicks, int stunLevel, boolean overrideStun, float damage, boolean lift, int blockstun, DamageSource source, Entity attacker, HitPropertyComponent.HitAnimation hitAnimation, boolean canBackstab, boolean unblockable) {
        if (hasUser()) {
            boolean hit = true;

            // Remote stands can only block for themselves
            if (isRemote()) {
                if (blocking) {
                    boolean backstabbed = false;
                    if (attacker != null) {
                        double delta = Math.abs((headYaw + 90.0f) % 360.0f - (attacker.getHeadYaw() + 90.0f) % 360.0f);
                        if (canBackstab && (360.0 - delta % 360.0 < 45 || delta % 360.0 < 45) && squaredDistanceTo(attacker.getPos()) >= 1.5625) { // Backstab logic
                            JCraft.createParticle((ServerWorld) attacker.getWorld(), getX(), attacker.getEyeY(), getZ(), JParticleType.BACK_STAB);
                            playSound(JSoundRegistry.BACKSTAB, 1, 1);
                            blocking = false;
                            overrideStun = true;
                            backstabbed = true;
                        }
                    }

                    if (!backstabbed && !unblockable) { // Didn't backstab, not unblockable
                        setMoveStun(blockstun);
                        setStandGauge(getStandGauge() - 2 * damage);
                        playSound(JSoundRegistry.STAND_BLOCK, 1, 1);
                        hit = false;
                        overrideStun = false;
                    } else blocking = false;

                    if (!backstabbed) hit = false;
                } else {
                    setStandGauge(getStandGauge() - damage * 2);
                }
            }

            if (hit) damageLogic(getWorld(), user, kbVec, stunTicks, stunLevel, overrideStun, damage, lift, blockstun, source, attacker, hitAnimation, canBackstab, unblockable);
        }
        return false;
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

    /**
     * Should be used on the CLIENT only, due to the sub-tick delay between setting {@link StandEntity#blocking} and {@link StandEntity#STATE}
     * @return whether the stands state indicates it is blocking.
     */
    public boolean isBlocking() {
        return getState() == getBlockState();
    }

    @Nullable
    protected abstract String getSummonAnimation();

    /**
     * Gets called after damage calculation if the damaged entity was slain.
     */
    protected void freshKill(@Nullable LivingEntity entity) { }
}
