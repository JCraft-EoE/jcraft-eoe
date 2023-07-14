package net.arna.jcraft.common.entity;

import lombok.Getter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.spec.JCraftSpec;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.mixin.LivingEntityInvoker;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.arna.jcraft.JCraft.ComboBreak;
import static net.arna.jcraft.JCraft.CooldownCancel;

public abstract class StandEntity extends MobEntity {

    // TODO: finish custom player idle poses for all stands
    // TODO: add skin select

    // All variables that the player can see in action (i.e. time erase time, timestop time, alpha, state) have to be tracked.
    public List<Attack> moves = List.of();

    private static final TrackedData<Integer> STATE;
    private static final TrackedData<Boolean> SAMESTATE; // Marks if the state was set to what it already was during the last setState() call
    private static final TrackedData<Integer> MOVESTUN;

    private static final TrackedData<Float> ROTATIONOFFSET;
    private static final TrackedData<Float> DISTANCEOFFSET;

    private static final TrackedData<Float> ALPHA;

    private static final TrackedData<Float> STANDGAUGE;

    private static final TrackedData<Float> FREEX;
    private static final TrackedData<Float> FREEY;
    private static final TrackedData<Float> FREEZ;

    private static final TrackedData<Boolean> FREE;
    private static final TrackedData<Boolean> REMOTE;

    protected int tsTime = 0;


    public Boolean blocking = false;
    public Boolean idleOverride = false;

    public Float idleDistance = 1.25f;
    public Float idleRotation = -45f;
    public final float attackRotation = 90f;
    public float blockDistance = 0.75f;

    public float maxStandGauge = 90f;

    public AttackQueue queuedAttack;
    public Attack curAttack;
    public Attack previousAttack;
    public int armorPoints;

    public static final List<String> attackCooldowns = List.of(JCraft.standLightCD, JCraft.standHeavyCD, JCraft.standBarrageCD, JCraft.standS1CD, JCraft.standUltCD, JCraft.standS2CD, JCraft.standS3CD, JCraft.utilCD);

    // Info
    public List<String> pros;
    public List<String> cons;
    public String description = "UNDESCRIBED";
    public String freespace;

    @Getter
    private final StandType standType;

    protected int summonAnimDuration = 19;
    protected boolean playSummonAnim = true;

    protected StandEntity(StandType type, World world) {
        super(type.getEntityType(), world);
        standType = type;
    }

    // State controls
    static {
        SAMESTATE = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        STATE = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.INTEGER);

        MOVESTUN = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.INTEGER);

        ROTATIONOFFSET = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.FLOAT);
        DISTANCEOFFSET = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.FLOAT);

        ALPHA = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.FLOAT);

        STANDGAUGE = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.FLOAT);

        FREE = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        REMOTE = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

        FREEX = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.FLOAT);
        FREEY = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.FLOAT);
        FREEZ = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.FLOAT);
    }

    private LivingEntity user = null;
    /**
     * Sets the stands user if there isn't one
     */
    public void setUser(LivingEntity user) {
        //if (this.user != null)
        //    JCraft.LOGGER.info("Overriding stand user for stand: " + this);
        this.user = user;
    }
    public LivingEntity getUser() {
        return user;
    }
    public boolean hasUser() {
        return user != null;
    }

    public int getState() {
        return this.dataTracker.get(STATE);
    }
    /**
     * Sets the stands state directly
     */
    public void setStateNoReset(int s) {
        this.dataTracker.set(STATE, s);
    }
    /**
     * Sets the stands state with extra processing
     */
    public void setState(int s) {
        int state = this.getState();
        this.dataTracker.set(SAMESTATE, state == s || state == 1); // Pretty much just an animation reset flag
        this.dataTracker.set(STATE, s);
    }

    public boolean getSameState() {
        return this.dataTracker.get(SAMESTATE);
    }
    public void setSameState(boolean samestate) {
        this.dataTracker.set(SAMESTATE, samestate);
    }

    public int getMoveStun() {
        return this.dataTracker.get(MOVESTUN);
    }
    /**
     * Sets how many ticks the stand will be occupied doing an animation for
     */
    public void setMoveStun(int moveStun) {
        this.dataTracker.set(MOVESTUN, moveStun);
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

    public float getAlpha() {
        return this.dataTracker.get(ALPHA);
    }
    public void setAlpha(float alpha) {
        this.dataTracker.set(ALPHA, alpha);
    }

    public float getStandGauge() {
        return this.dataTracker.get(STANDGAUGE);
    }
    public void setStandGauge(float standGauge) {
        this.dataTracker.set(STANDGAUGE, standGauge);
    }

    public boolean getFree() {
        return this.dataTracker.get(FREE);
    }
    /**
     * Changes whether the stand is detached from the user
     */
    public void setFree(boolean free) {
        this.dataTracker.set(FREE, free);
    }

    public int lastRemoteInputTime;
    public Vec3d remoteSpeed = Vec3d.ZERO;
    private double remoteForwardInput = 0;
    private double remoteSideInput = 0;
    private boolean remoteJumpInput = false;

    public double getRemoteForwardInput() {
        return remoteForwardInput;
    }
    public double getRemoteSideInput() {
        return remoteSideInput;
    }
    public void setRemoteJumpInput(boolean b) {
        remoteJumpInput = b;
    }
    public boolean getRemoteJumpInput() {
        return remoteJumpInput;
    }

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

    public boolean getRemote() {
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
        setFree(true);

        Vec3d fPos = user.getPos().add( user.getRotationVector() );
        remoteSpeed = user.getVelocity(); // Inertia
        remoteSpeed = new Vec3d(remoteSpeed.x * 5, remoteSpeed.y / 2, remoteSpeed.z * 5);

        setAlpha(0.1f);

        detach();

        this.noClip = false;

        this.velocityDirty = true;
        setPos(fPos.x, user.getY() + 0.5, fPos.z);
    }

    /**
     * Ends remote mode instantly
     */
    protected void endRemote() {
        setFree(false);

        setAlpha(1);

        startRiding(user);

        this.noClip = true;
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
     * @param freePos new position
     */
    public void setFreePos(Vec3f freePos) {
        this.dataTracker.set(FREEX, freePos.getX());
        this.dataTracker.set(FREEY, freePos.getY());
        this.dataTracker.set(FREEZ, freePos.getZ());
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.PLAYERS;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(STATE, 0);
        this.dataTracker.startTracking(SAMESTATE, false);

        this.dataTracker.startTracking(MOVESTUN, 0);

        this.dataTracker.startTracking(ROTATIONOFFSET, -90f);
        this.dataTracker.startTracking(DISTANCEOFFSET, 1f);

        this.dataTracker.startTracking(ALPHA, 0f);

        this.dataTracker.startTracking(STANDGAUGE, 45f);

        this.dataTracker.startTracking(FREE, false);
        this.dataTracker.startTracking(REMOTE, false);

        this.dataTracker.startTracking(FREEX, 0f);
        this.dataTracker.startTracking(FREEY, 0f);
        this.dataTracker.startTracking(FREEZ, 0f);
    }

    public void initialize() {
        this.noClip = true;
        setInvulnerable(true);
        addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 999999, 9, false, false));
    }

    // Attack controls
    /**
     * @return whether the stand should be able to attack
     */
    public boolean canAttack() {
        if (hasUser()) {
            ITimeStop timeStop = (ITimeStop) user;
            return this.getMoveStun() < 1 && timeStop.getTimeStopTicks() < 1 && !user.hasStatusEffect(JStatusRegister.DAZED);
        }
        return false;
    }

    /**
     * @return whether the stand should change its height depending on the user's look pitch
     */
    public boolean shouldOffsetHeight() {
        return getState() > 1;
    }

    /**
     * Struct used for storing extra information relating to the stands ability to attack
     */
    public static class CanAttackData {
        public final LivingEntity user;
        public final boolean canAttack;

        public CanAttackData(LivingEntity l, boolean b) {
            this.user = l;
            this.canAttack = b;
        }
    }

    /**
     * Returns a {@link CanAttackData} with information relating to the stands ability to attack
     */
    public CanAttackData canAttackWithData() {
        if (hasUser()) {
            ITimeStop timeStop = (ITimeStop) user;
            return new CanAttackData(user, this.getMoveStun() < 1 && timeStop.getTimeStopTicks() < 1 && !user.hasStatusEffect(JStatusRegister.DAZED));
        }
        return new CanAttackData(null, false);
    }

    /**
     * Initiates an attack with the stand
     * @param attack attack to handle
     * @param cooldownName string identifier for which cooldown to start
     * @param animState int identifier for which state to put the stand into
     */
    public boolean handleAttack(Attack attack, String cooldownName, int animState) {
        NbtCompound userData = ((IEntityDataSaver) user).getPersistentData();
        int cooldown = userData.getInt(cooldownName);
        if (cooldown > 0) {
            return false;
        }
        userData.putInt(cooldownName, attack.cooldown * 20);
        this.setAttack(attack, animState);
        return true;
    }

    /**
     * Instantly sets the stands attack
     * @param attack attack to set
     * @param animState int identifier for which state to put the stand into
     */
    public void setAttack(Attack attack, int animState) {
        this.curAttack = attack;
        this.setMoveStun(attack.moveStun);
        this.setState(animState);
        this.armorPoints = attack.armor;
    }

    /**
     * Stuns specified {@link LivingEntity}
     * @param entity victim to stun
     * @param duration in ticks
     * @param amplifier type of stun
     */
    public static void stun(LivingEntity entity, int duration, int amplifier) {
        if (entity == null || duration == 0) return;
        entity.addStatusEffect(new StatusEffectInstance(JStatusRegister.DAZED, duration, amplifier, false, false, true));
        //JCraft.LOGGER.info("Stunned: " + entity.getEntityName() + " for: " + duration);
    }

    /**
     * Basic damage method, you likely want to use baseDamageLogic or damageLogic instead
     * @param damage damage in half hearts
     * @param damageSource source of damage
     * @param ent entity to harm
     */
    public static void damage(float damage, DamageSource damageSource, LivingEntity ent) {
        if (ent == null || ent.isRemoved() || ent.isDead()) { return; }
        ent.damage(damageSource, 0.001f);

        // All stands ignore 10% of armor & armor toughness
        damage = DamageUtil.getDamageLeft(damage, (float) ent.getArmor() * 0.9f, (float) ent.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS) * 0.9f);
        damage = ((LivingEntityInvoker) ent).invokeModifyAppliedDamage(damageSource, damage);

        // Apply absorption
        float f = damage;
        damage = Math.max(damage - ent.getAbsorptionAmount(), 0.0F);
        ent.setAbsorptionAmount(ent.getAbsorptionAmount() - (f - damage));

        if (damage <= 0) return;

        float h = ent.getHealth();
        ent.setHealth(h - damage);
        ent.getDamageTracker().onDamage(damageSource, h, damage);
        if (ent.isDead())
            ent.onDeath(damageSource);
    }

    // Stock attacks to define
    public void initLightAttack() {
    }

    public void initHeavyAttack() {
    }

    public void initBarrage() {
    }

    // Specials to define within the specific stand
    public void initSpecial1() {
    }

    public void initSpecial2() {
    }

    public void initSpecial3() {
    }

    public void initUlt() {
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
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 5, 3, false, false, true));
    }

    protected Vec3d timeSkip(double distance, @NotNull SoundEvent sound) {
        Vec3d eyePos = user.getEyePos();
        HitResult hitResult = world.raycast(
                new RaycastContext(
                        eyePos,
                        eyePos.add(user.getRotationVector().multiply(distance)),
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE, user));
        Vec3d telePos = hitResult.getPos();

        // 3s minimum ult cooldown
        NbtCompound userData = ((IEntityDataSaver) user).getPersistentData();
        if (userData.getInt(JCraft.standUltCD) < 60)
            userData.putInt(JCraft.standUltCD, 60);

        user.teleport(telePos.x, telePos.y, telePos.z);
        world.playSound(null, telePos.x, telePos.y, telePos.z, sound, SoundCategory.PLAYERS, 1f, 1f);

        return telePos;
    }

    // Define utility
    public void initUtil() {
    }

    // Define special attack actions
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
    }

    // Define desummon conditions
    public void desummon() {
        if (curAttack != null || getMoveStun() > 0) return;
        discard();
        if (user != null)
            ((IEntityDataSaver)user).setStand(null);
    }

    // Define idle override
    public void idleOverride(LivingEntity player) {
    }

    /**
     * Defines what happens if the stand successfully countered something
     * @param entity countered entity
     * @param source exact source of damage
     */
    public void counter(Entity entity, DamageSource source) {
        this.curAttack = null;
        this.setMoveStun(0);
    }

    /**
     * Cancels the stands attack instantly
     */
    public void cancelAttack() {
        this.curAttack = null;
        this.setMoveStun(0);
        this.setState(0);
    }

    /**
     * Returns whether the stand defaults to returning to the user while idle and detached
     */
    public boolean defaultToNear() {
        return !getRemote();
    }

    @Override
    public boolean hasNoGravity() {
        if (getFree() && !getRemote()) return true;
        return super.hasNoGravity();
    }

    /**
     * does evrything :)
     */
    @Override
    public void tick() {
        super.tick();

        if (isDead()) return;

        boolean client = world.isClient;

        if (user == null) {
            if (client && getVehicle() instanceof LivingEntity living)
                user = living;
            return;
        } //else if (this.owner == null) { this.owner = player; }
        Entity vehicle = user.getVehicle();

        setMoveStun(getMoveStun() - 1); // Counting down animation time or similar
        if (playSummonAnim && (getMoveStun() > 0 || age > summonAnimDuration) )
            playSummonAnim = false;

        Attack attack = this.curAttack;

        Vec3d pos = this.getPos();
        Vec3d rotVec = this.getRotationVector();
        Vec3d eyePos = this.getEyePos();
        boolean isFree = getFree();
        boolean isRemote = getRemote();

        // Common code for remote mode
        if (isRemote) {
            if (hasVehicle()) detach();
            if (!user.isAlive())
                kill();

            // Clientside rotational sync for remote mode
            user.setBodyYaw(user.getHeadYaw());

            setHeadYaw(user.getHeadYaw());
            setRotation(user.getYaw(), user.getPitch());
        } else if (!hasVehicle() && !getFree())
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
            if (getSameState()) setSameState(false);

            // Make sure the user is using this stand
            if (((IEntityDataSaver)user).getStand() != this) discard();

            // Block break check
            if (getStandGauge() < 1) {
                user.addStatusEffect(new StatusEffectInstance(JStatusRegister.DAZED, 40, 2));
                playSound(SoundEvents.ITEM_TOTEM_USE, 1, 0.5f);
                kill();
            }

            // Return to user after stand detach move, provided it's finished recovering and there's no queued followup
            if (defaultToNear() && getMoveStun() < 1 && this.queuedAttack == null && attack == null) setFree(false);

            // Rotate with user
            if (!isFree || isRemote) {
                setHeadYaw(user.getHeadYaw());
                setRotation(user.getYaw(), user.getPitch());
            }

            // Remote mode
            if (isRemote) user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 5, 9, true, false));

            int curMoveStun = this.getMoveStun();

            // Attack logic
            if (curMoveStun >= 0 && !this.blocking && attack != null) {
                int stunTicks = (int) (attack.stun * 20f);
                //LOGGER.info("Stun ticks: " + stunTicks);

                int moveStun = attack.moveStun;
                float damage = attack.damage;
                float attackDist = attack.attackDist;

                int realInitTime = (moveStun - attack.initTime);

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
                        this.setPosition(user.getPos());
                        this.setRotationOffset(this.attackRotation);
                    }
                } else {
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 5, 4, true, false));

                    this.setRotationOffset(this.attackRotation);
                    this.setDistanceOffset(attackDist);
                }

                if (attack.attackType == AttackType.TIMESTOP && curMoveStun == realInitTime) {
                    tsTime = stunTicks;
                    this.curAttack = null;

                    StatusEffectInstance tsBlind = new StatusEffectInstance(StatusEffects.BLINDNESS, 19, 0, false, false, false);
                    user.addStatusEffect(tsBlind);

                    JCraft.stopTime(user, pos, (ServerWorld) world, stunTicks);
                }

                boolean isBarrage = attack.isBarrage();

                if (attack.attackType == AttackType.BARRAGE) { // Excludes charge barrages
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 10, 2, true, false));
                }

                if (
                        (attack.attackType == AttackType.BOX && curMoveStun == realInitTime)
                                || (isBarrage && curMoveStun % attack.interval == 0 && curMoveStun <= realInitTime)
                                || (attack.attackType == AttackType.CHARGE && curMoveStun <= realInitTime)
                                || (attack.attackType == AttackType.MULTIHIT && attack.attackTimes.contains(moveStun - curMoveStun))
                ) {
                    //JCraft.LOGGER.info(curMoveStun + " ACTIVE " + attack.interval);
                    List<LivingEntity> hurt = new ArrayList<>();

                    if (attack.hitboxSize > 0) {
                        Vec3d hPos = pos.add(0.0, user.getHeight() / 2, 0.0);
                        Vec3d fPos = (isChargeAttack) ? hPos.add(rotVec) :
                                hPos.add(rotVec.multiply(attackDist)).subtract(0, attack.offset, 0);

                        List<Entity> filter = new ArrayList<>(List.of(this, user));
                        if (vehicle != null) filter.add(vehicle);

                        hurt = JUtils.generateHitbox(world, fPos, attack.hitboxSize, filter);
                        for (Attack.HitboxData data : attack.extraHitboxes) {
                            List<LivingEntity> extraHurt = JUtils.generateHitbox(world,
                                    hPos.add(rotVec.multiply(data.forwardOffset)).add(0, data.verticalOffset, 0), data.hitboxSize, filter);
                            for (LivingEntity hurtEntity : extraHurt)
                                if (!hurt.contains(hurtEntity)) hurt.add(hurtEntity);
                        }
                        if (!hurt.isEmpty()) {
                            JCraft.CreateParticle((ServerWorld) this.world,
                                    fPos.x + random.nextGaussian() * 0.25,
                                    fPos.y + random.nextGaussian() * 0.25,
                                    fPos.z + random.nextGaussian() * 0.25,
                                    attack.hitspark + 1);

                            if (attack.impactSound != null) playSound(attack.impactSound, 1, 1);

                            if (attack.attackType == AttackType.CHARGE) {
                                this.setMoveStun(10);
                                this.setState(attack.interval); // Interval is hitAnim for charges
                                this.curAttack = null;
                            }
                        }

                        float kb = attack.knockback;
                        Vec3d kbVec = rotVec.multiply(kb).add(new Vec3d(0.0, Math.abs(attack.knockback) / 4, 0.0));

                        List<LivingEntity> clashed = new ArrayList<>();

                        for (LivingEntity livingEntity : hurt) {
                            if (livingEntity instanceof StandEntity stand) {
                                // Barrage clashing
                                if (isBarrage && stand.curAttack != null && stand.curAttack.attackType == AttackType.BARRAGE) {
                                    // Override stun with high priority 0.5s stun, also stops all current sounds for cleaner audio cue
                                    clashed.add(user);
                                    if (stand.hasUser()) {
                                        clashed.add(stand.getUser());

                                        if (stand.getUser() instanceof ServerPlayerEntity serverPlayer)
                                            serverPlayer.networkHandler.sendPacket(new StopSoundS2CPacket(null, SoundCategory.PLAYERS));
                                    }
                                    if (user instanceof ServerPlayerEntity serverPlayer)
                                        serverPlayer.networkHandler.sendPacket(new StopSoundS2CPacket(null, SoundCategory.PLAYERS));

                                    // Cancels both barrages
                                    cancelAttack();
                                    stand.cancelAttack();
                                    Vec3d midPos = stand.getPos().add(getPos()).multiply(0.5);
                                    this.world.playSound(null, midPos.x, midPos.y, midPos.z, JSoundRegister.IMPACT_1, SoundCategory.NEUTRAL, 1, 0.5f);
                                }
                                continue;
                            }
                            damageLogic(world, livingEntity, kbVec, stunTicks, attack.stunType, attack.overrideStun, damage, attack.lift, attack.getEffectiveBlockstun(), JDamageSources.stand(this, user), user, attack.canBackstab);
                        }

                        for (LivingEntity livingEntity : clashed) {
                            livingEntity.removeStatusEffect(JStatusRegister.DAZED);
                            livingEntity.addStatusEffect(new StatusEffectInstance(JStatusRegister.DAZED, 10, 3, true, false));
                        }
                    }

                    this.specialAttack(attack, hurt);
                }
                /*
                else {
                    JCraft.LOGGER.info(this.getMoveStun() + " N " + attack.interval);
                }
                 */
            }

            if (curMoveStun <= 0 && !this.blocking) {
                // Attack buffering
                if (this.queuedAttack != null) {
                    switch (this.queuedAttack) {
                        case LIGHT -> this.initLightAttack();
                        case HEAVY -> this.initHeavyAttack();
                        case BARRAGE -> this.initBarrage();
                        case SPECIAL1 -> this.initSpecial1();
                        case ULTIMATE -> this.initUlt();
                        case SPECIAL2 -> this.initSpecial2();
                        case SPECIAL3 -> this.initSpecial3();
                        case MIDDLEMOUSE -> this.initUtil();
                        case STANDSUMMON -> {
                            this.curAttack = null;
                            this.desummon();
                        }
                    }

                    this.queuedAttack = null;
                } else if (!this.idleOverride) {
                    // Process idle
                    this.curAttack = null;

                    this.setStandGauge(MathHelper.clamp(this.getStandGauge() + 0.5f, 0, maxStandGauge));

                    if (this.getState() != 1) {
                        this.setState(1);

                        this.setDistanceOffset(this.idleDistance);
                        this.setRotationOffset(this.idleRotation);
                    }
                } else {
                    idleOverride(user);
                }
            } else if (this.blocking) { // Process block
                this.curAttack = null;
                this.setStateNoReset(3);

                if (curMoveStun < 4) setMoveStun(4);
                setDistanceOffset(this.blockDistance);
                setRotationOffset(this.attackRotation);
                standBlock();
            }
        }

        // JCraft.LOGGER.info( "State: " + this.getState() + " Movestun: " + curMoveStun + " Currently attacking: " + (this.curAttack != null)); // Massive debug log

        // Minor aspects of timestop logic, actual stopping is handled at JServerTickEvents
        if (tsTime > 0) {
            user.stopRiding();

            for (int h = 0; h < 1500 / tsTime; ++h)
                world.addParticle(
                        ParticleTypes.MYCELIUM,
                        eyePos.x + random.nextTriangular(0, tsTime),
                        eyePos.y + random.nextTriangular(0, tsTime) / 4,
                        eyePos.z + random.nextTriangular(0, tsTime),
                        0.0, 0.0, 0.0
                );

            if (!client) tsTime--;
        }

        if (this.curAttack != this.previousAttack && this.curAttack != null)
            //JCraft.LOGGER.info("Logged previous attack change: " + this.curAttack + " " + this.previousAttack);
            this.previousAttack = this.curAttack;

        //this.pastAttack = this.curAttack;
    }

    /**
     * Highest level damage method, handles combo counting
     * @param world world to process damage in
     * @param ent victim
     * @param kbVec knockback vector to apply
     * @param stunTicks stun duration in ticks
     * @param overrideStun will the attack override all other types of stun?
     * @param damage damage in half hearts
     * @param lift will the attack lift the victim upon an aerial hit?
     */
    public static void damageLogic(World world, LivingEntity ent, Vec3d kbVec, int stunTicks, int stunType, boolean overrideStun, float damage, boolean lift, int blockstun, DamageSource source, Entity attacker, boolean canBackstab) {
        if (world == null || world.isClient || ent == null) return;
        if (world.getGameRules().getBoolean(JCraft.COMBO_COUNTER) && attacker instanceof PlayerEntity playerEntity)
            comboCounterLogic(playerEntity, ent);

        baseDamageLogic(ent, kbVec, stunTicks, stunType, overrideStun, damage, lift, blockstun, source, attacker, canBackstab);
    }

    /**
     * Highest level damage method, handles combo counting, DEFAULTS canBackstab TO FALSE
     * @param world world to process damage in
     * @param ent victim
     * @param kbVec knockback vector to apply
     * @param stunTicks stun duration in ticks
     * @param overrideStun will the attack override all other types of stun?
     * @param damage damage in half hearts
     * @param lift will the attack lift the victim upon an aerial hit?
     */
    public static void damageLogic(World world, LivingEntity ent, Vec3d kbVec, int stunTicks, int stunType, boolean overrideStun, float damage, boolean lift, int blockstun, DamageSource source, Entity attacker) {
        if (world == null || world.isClient || ent == null) return;
        if (world.getGameRules().getBoolean(JCraft.COMBO_COUNTER) && attacker instanceof PlayerEntity playerEntity)
            comboCounterLogic(playerEntity, ent);
        baseDamageLogic(ent, kbVec, stunTicks, stunType, overrideStun, damage, lift, blockstun, source, attacker, false);
    }

    /**
     * Handles combo counting for specific player
     * @param playerEntity attacker
     */
    private static void comboCounterLogic(PlayerEntity playerEntity, LivingEntity victim) {
        IComboCounter comboCounter = (IComboCounter) playerEntity;
        if (comboCounter.getLastAttacked() != victim)
            comboCounter.jcraft$setComboCount(1);
        else {
            StatusEffectInstance stun = victim.getStatusEffect(JStatusRegister.DAZED);
            if (stun != null && stun.getAmplifier() != 2) //LOGGER.info("Target stun: " + stun.getDuration());
                comboCounter.incrementComboCount();
            else
                comboCounter.jcraft$setComboCount(1);

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(6);
            buf.writeInt(comboCounter.jcraft$getComboCount());
            ServerChannelFeedbackPacket.send((ServerPlayerEntity) playerEntity, buf);
        }
        comboCounter.setLastAttacked(victim);
    }

    /**
     * Mid-level damage method, handles blocking, lifting, counters, velocity modification
     * @param ent victim
     * @param kbVec knockback vector to apply
     * @param stunTicks stun duration in ticks
     * @param overrideStun will the attack override all other types of stun?
     * @param damage damage in half hearts
     * @param lift will the attack lift the victim upon an aerial hit?
     */
    public static void baseDamageLogic(LivingEntity ent, Vec3d kbVec, int stunTicks, int stunType, boolean overrideStun, float damage, boolean lift, int blockstun, DamageSource source, Entity attacker, boolean canBackstab) {
        boolean hit = true;
        boolean tsHit = ( (ITimeStop)ent ).getTimeStopTicks() > 0;

        StandEntity stand = ((IEntityDataSaver)ent).getStand();
        if (stand != null) {
            Attack standAttack = stand.curAttack;
            if (standAttack != null) {
                // Counter check
                if (!tsHit && standAttack.attackType == AttackType.COUNTER && stand.getMoveStun() < (standAttack.moveStun - standAttack.initTime)) {
                    stand.counter(attacker, source);
                    ent.removeStatusEffect(JStatusRegister.DAZED);
                    return;
                }

                if (--stand.armorPoints < 0) stand.cancelAttack();
            }

            if (stand.blocking && !stand.getRemote()) {
                double delta = Math.abs((ent.headYaw + 90.0f) % 360.0f - (attacker.getHeadYaw() + 90.0f) % 360.0f);
                if ( canBackstab && (360.0 - delta % 360.0 < 90 || delta % 360.0 < 90) && ent.squaredDistanceTo(attacker.getPos()) >= 1.5625 ) { // Backstab logic
                    JCraft.CreateParticle((ServerWorld) attacker.getWorld(), ent.getX(), attacker.getEyeY(), ent.getZ(), -2);
                    stand.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
                    stand.blocking = false;
                    overrideStun = true;

                } else {
                    stand.setMoveStun(blockstun);
                    stand.setStandGauge(stand.getStandGauge() - 2 * damage);
                    stand.playSound(JSoundRegister.STAND_BLOCK, 1, 1);
                    hit = false;
                }
            }
        }

        if (tsHit) {
            stunType = 3;
            if (stunTicks > 20) stunTicks = 20;
            lift = false;
        } else {
            // Velocity modification synchronisation
            if (ent instanceof ServerPlayerEntity serverPlayer)
                serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayer));
            else
                ent.velocityModified = true;
        }

        // Stun application & overriding
        if (hit) {
            if (overrideStun)
                ent.removeStatusEffect(JStatusRegister.DAZED);
            stun(ent, stunTicks, stunType);
            ent.addVelocity(kbVec.x, kbVec.y, kbVec.z);
        }

        // Interrupting spec moves
        if (ent instanceof PlayerEntity playerEntity) {
            JCraftSpec spec = JUtils.getSpec(playerEntity);
            if (spec != null && spec.curAttack != null && --spec.armorPoints < 0) spec.cancelAttack();
        }

        // Aerial hits keep the victim up
        if (lift) {
            Vec3d vel = ent.getVelocity();
            double finalY = vel.y;

            if (!ent.isOnGround())
                finalY = MathHelper.clamp(vel.y / 2, 0.085, 0.25);

            ent.setVelocity(
                    MathHelper.clamp(vel.x, -1, 1),
                    MathHelper.clamp(finalY, -0.25, 0.25),
                    MathHelper.clamp(vel.z, -1, 1)
            );
        }

        damage(damage, source, ent);
    }

    @Override
    public void stopRiding() {
        if (!getRemote()) {
            playSound(JSoundRegister.STAND_DESUMMON, 1, 1);
            kill();
        }
        super.stopRiding();
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        if (hasUser() && getUser() instanceof ArmorStandEntity) return;
        kill(); // Whenever the stand is being loaded, kill it, it'll break
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.isMagic() || source.isExplosive()) return false;
        return super.damage(source, amount);
    }

    // Physical properties
    @Override
    public void pushAwayFrom(Entity entity) { }
    @Override
    public boolean collidesWith(Entity other) { return false; }

    @Override
    public boolean addStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
        if (world.isClient || user == null) return false;
        return user.addStatusEffect(effect, source);
    }

    /**
     * Handles AI for mob stand users
     */
    public static void standUserAI(MobEntity mob, LivingEntity target, StandEntity stand) {
        if (mob == target) return;
        if (target == null || !target.isAlive() || target.isRemoved()) return;

        mob.lookAtEntity(target, 30, 30); // Point body at enemy
        mob.getLookControl().lookAt(target); // Usually detrimental not to

        JCraftSpec enemySpec;
        StandEntity enemyStand = ((IEntityDataSaver)target).getStand();
        Attack enemyAttack = null;
        boolean enemyHasStand = enemyStand != null;

        double distance = target.distanceTo(mob);
        int enemyMoveStun = 0;
        int blockPlusTicks = 0;

        // Get enemy stand attack (most common)
        if (enemyHasStand) {
            enemyMoveStun = enemyStand.getMoveStun();
            enemyAttack = enemyStand.curAttack;

            if (enemyStand.blocking)
                blockPlusTicks = enemyMoveStun;

            distance = enemyStand.distanceTo(mob);
        }
        // If none was found, try to find a spec attack
        if (enemyAttack == null) {
            if (target instanceof PlayerEntity player) {
                enemySpec = ((ISpec) player).getSpec();

                if (enemySpec != null) {
                    enemyMoveStun = enemySpec.moveStun;
                    enemyAttack = enemySpec.curAttack;
                }
            }
        }

        // Blocking logic
        boolean wantToBlock = false;
        if (enemyAttack != null && enemyMoveStun > 0) { // Only block if the attack is actually active
            // Block regardless of range if the attack is ranged, or is a barrage
            if (enemyAttack.isRanged || enemyAttack.attackType == AttackType.BARRAGE)
                wantToBlock = true;
            // Block if the attack isn't ranged, but is within hitting distance, and doesn't block break
            if (enemyAttack.attackDist + enemyAttack.hitboxSize * 0.66 > distance && enemyAttack.damage * 2 < stand.getStandGauge())
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

        StatusEffectInstance mobStun = mob.getStatusEffect(JStatusRegister.DAZED);
        // If stunned, and about to get hit by another move, combo break sometimes
        if (mobStun != null)
            if (!stand.blocking && enemyAttack != null && enemyMoveStun > enemyAttack.initTime && stand.random.nextFloat() < 0.1f)
                ComboBreak((ServerWorld) stand.world, mob, mobStun);

        if (!stand.blocking) {
            StatusEffectInstance stun = target.getStatusEffect(JStatusRegister.DAZED);
            // Overestimating stun up to 1/4 of a second for longer combos and frametraps
            int stunTicks = stun != null ? stun.getDuration() + stand.random.nextInt(5) : 0;
            stunTicks += blockPlusTicks;
            stunTicks += ((ITimeStop) target).getTimeStopTicks();
            int move = stand.selectMove(mob, target, stunTicks, enemyMoveStun, distance, enemyStand, enemyAttack);
            Attack selectedAttack = null;

            boolean shouldPerformMove = stand.getMoveStun() < 1;
            if (stand.curAttack != null && stand.curAttack.hasFollowup())
                shouldPerformMove = true;

            if (move != -1) {
                selectedAttack = stand.moves.get(move);

                if (shouldPerformMove) {
                    switch (move) {
                        case 0 -> stand.initLightAttack();
                        case 1 -> stand.initHeavyAttack();
                        case 2 -> stand.initBarrage();
                        case 3 -> stand.initSpecial1();
                        case 4 -> stand.initUlt();
                        case 5 -> stand.initSpecial2();
                        case 6 -> stand.initSpecial3();
                        case 7 -> stand.initUtil();
                    }
                } else {
                    stand.queuedAttack = AttackQueue.values()[move];
                }
            }

            // Dash to targeted location
            BlockPos targetPos = mob.getNavigation().getTargetPos();
            if (targetPos != null && mob.isOnGround() && targetPos.getSquaredDistance(target.getPos()) > 4)
                JCraft.tryDash(1, 0, mob);

            double sideswitchDistance = 1.25;

            // If in range
            if (
                    (selectedAttack != null && distance < selectedAttack.attackDist + selectedAttack.hitboxSize * 0.75) ||
                            (enemyAttack != null && !enemyAttack.isRanged && distance < enemyAttack.attackDist + enemyAttack.hitboxSize * 1.5)
            ) {
                // Move towards or away depending on distance and intent
                mob.getNavigation().setSpeed(distance < sideswitchDistance && selectedAttack == null ? 0.25 : -0.25);
            }

            float sStrafe = MathHelper.sin(stand.age * 0.02f) / 3f;

            // Move away during combo to prevent point-blank misses
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
                    mob.getJumpControl().setActive();
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


    /**
     * Used to help AIs that use stands with unique moves
     */
    public MoveSelectionResult specificMoveSelectionCriterion(Attack attack, MobEntity mob, LivingEntity target, int stunTicks, int enemyMoveStun, double distance, StandEntity enemyStand, Attack enemyAttack) {
        return MoveSelectionResult.PASS;
    }

    private int selectMove(MobEntity mob, LivingEntity target, int stunTicks, int enemyMoveStun, double distance, StandEntity enemyStand, Attack enemyAttack) {
        int chosenMove = 0; //random.nextInt(0, 4);
        int chosenMoveInitTime = this.moves.get(chosenMove).initTime;
        int movesOnCooldown = 0;

        NbtCompound userData = ((IEntityDataSaver) mob).getPersistentData();

        for (int i = 0; i < this.moves.size(); i++) {
            Attack attack = this.moves.get(i);

            int initTime = attack.realInitTime();

            // If the opponent is countering, don't attack
            if (enemyAttack != null) {
                if (enemyAttack.attackType == AttackType.COUNTER) {
                    chosenMove = -1;
                    break;
                }
            }

            // Skip attacks on cooldown
            if (userData.getInt(attackCooldowns.get(i)) > 0) {
                movesOnCooldown += 1;
                // If the button matches the current attack's button, and it has a followup, then consider said followup
                // This logic was chosen because simply checking this.curAttack.hasFollowup() only goes up to a depth of 1
                if (this.curAttack != null && this.curAttack.hasFollowup() && AttackQueue.values()[i] == this.curAttack.button) {
                    //JCraft.LOGGER.info("Followup detected");
                    attack = this.curAttack.followup;
                    initTime = stunTicks; // Followups should always win the initTime contest, given that they cancel the current move
                } else {
                    continue;
                }
            }

            // Selection of characteristic moves with custom usage logic
            MoveSelectionResult result = specificMoveSelectionCriterion(attack, mob, target, stunTicks, enemyMoveStun, distance, enemyStand, enemyAttack);
            if (result == MoveSelectionResult.USE) {
                chosenMove = i;
                break;
            }
            if (result == MoveSelectionResult.STOP) continue;

            // Use mobility if opponent is far away
            if (attack.mobilityType != null) {
                // ...and isn't being comboed or is blocking
                if (stunTicks > 0) continue;

                if (attack.mobilityType != MobilityType.HIGHJUMP && distance > 6) {
                    if (target.isOnGround()) {
                        if (attack.mobilityType == MobilityType.TELEPORT) {
                            // Intentionally looks at target's feet as to hit the ground exactly at it
                            mob.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, target.getPos());
                        } else if (attack.mobilityType == MobilityType.DASH) {
                            // Look at target itself as a dash works best at that angle
                            mob.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, target.getEyePos().add(0, 0.2, 0));
                        }
                    }

                    if (attack.mobilityType == MobilityType.FLIGHT) {
                        mob.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, target.getEyePos());
                    }

                    chosenMove = i;
                    break;
                } // If target is considerably above the mob, or the mob is going to get hit
                else if (target.getY() > mob.getY() + 2 || (enemyAttack != null && enemyStand != null && enemyMoveStun > enemyAttack.realInitTime())) {
                    chosenMove = i;
                    break;
                }
            }

            // Use counter if opponent is using a non-ranged move
            if (!attack.isRanged && attack.attackType == AttackType.COUNTER) {
                if (enemyStand != null && !enemyStand.blocking && enemyMoveStun > 0) {
                    chosenMove = i;
                    break;
                }
                continue;
            }

            // Use a barrage (or variant thereof) if the opponent is stunned, not blocking, and it's off cooldown,
            // because it's a free combo extender and has a lower init. time than light
            if (distance < 1.4) {
                if (attack.attackType == AttackType.BARRAGE || (attack.attackType == AttackType.MULTIHIT && initTime <= stunTicks)) {
                    if (enemyStand == null) {
                        chosenMove = i;
                        break;
                    } else if (!enemyStand.blocking) {
                        chosenMove = i;
                        break;
                    }
                    continue;
                }
            }

            // If the opponent is out of exactly twice the range it would take him to get to the user within the move being complete, use a projectile
            if (attack.isRanged && distance > attack.moveStun * target.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * 2) {
                mob.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, target.getEyePos());
                chosenMove = i;
                break;
            }

            // If the opponent isn't using a move, prioritize attack with higher or equal initiation time
            if (initTime <= stunTicks && initTime >= chosenMoveInitTime) {
                chosenMoveInitTime = initTime;
                chosenMove = i;
            }
        }

        if (movesOnCooldown > 5) { // >5 = 80+%
            CooldownCancel((ServerWorld) this.world, mob);
        }

        // Non ranged offensive attacks are cancelled if the opponent is too far (and -1 causes an out-of-bounds error)
        if (chosenMove != -1) {
            Attack chosenAttack = this.moves.get(chosenMove);
            if (chosenAttack.attackType != AttackType.COUNTER && chosenAttack.mobilityType == null && chosenAttack.hitboxSize > 0 && !chosenAttack.isRanged && distance > chosenAttack.attackDist + chosenAttack.hitboxSize) {
                chosenMove = -1;
            }
        }

        return chosenMove;
    }
}
