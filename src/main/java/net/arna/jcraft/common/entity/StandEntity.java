package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.client.network.s2c.ShaderActivationPacket;
import net.arna.jcraft.common.spec.JCraftSpec;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.mixin.LivingEntityInvoker;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
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
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.arna.jcraft.JCraft.*;

public abstract class StandEntity extends MobEntity {

    // TODO: finish custom player idle poses for all stands

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

    public static TrackedData<Integer> TIMESTOPTIME;
    public Boolean blocking = false;
    public Boolean idleOverride = false;

    public Float idleDistance = 1.25f;
    public Float idleRotation = -45f;
    public float attackRotation = 90f;
    public float blockDistance = 0.75f;

    public float maxStandGauge = 90f;

    //public int tsRes = 20;

    public AttackQueue queuedAttack;
    public Attack curAttack;
    public Attack previousAttack;

    public static List<String> attackCooldowns = List.of(JCraft.standLightCD, JCraft.standHeavyCD, JCraft.standBarrageCD, JCraft.standS1CD, JCraft.standUltCD, JCraft.standS2CD, JCraft.standS3CD, JCraft.standMMBCD);
    static Attack unusable = new Attack(999, 999, 999, 0, AttackType.BOX).setInfo("NONE", "NONE");

    // Info
    public List<String> pros;
    public List<String> cons;
    public String description = "UNDESCRIBED";
    public String freespace;

    protected StandEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
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

        TIMESTOPTIME = DataTracker.registerData(StandEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    private LivingEntity user = null;

    public void setUser(LivingEntity l) {
        if (user == null)
            user = l;
    }

    public LivingEntity getUser() {
        return this.user;
    }

    public boolean hasUser() {
        return user != null;
    }

    public int getState() {
        return this.dataTracker.get(STATE);
    }

    public void setState(int s) {
        int state = this.getState();
        this.dataTracker.set(SAMESTATE, state == s || state == 1);
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

    public void setMoveStun(int moveStun) {
        this.dataTracker.set(MOVESTUN, moveStun);
    }

    public float getRotationOffset() {
        return this.dataTracker.get(ROTATIONOFFSET);
    }

    public void setRotationOffset(float rotationOffset) {
        this.dataTracker.set(ROTATIONOFFSET, rotationOffset);
    }

    public float getDistanceOffset() {
        return this.dataTracker.get(DISTANCEOFFSET);
    }

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
        if (r) {
            BeginRemote();
        } else {
            endRemote();
        }
    }

    protected void BeginRemote() {
        setFree(true);
        Vec3d fPos = user.getPos().add(user.getRotationVector());
        setFreePos(new Vec3f(fPos));
        setPos(fPos.x, fPos.y, fPos.z);
        remoteSpeed = user.getVelocity(); // Inertia
        setAlpha(0.1f);
    }

    protected void endRemote() {
        setFree(false);
        setAlpha(1);
    }

    public Vec3f getFreePos() {
        return new Vec3f(this.dataTracker.get(FREEX), this.dataTracker.get(FREEY), this.dataTracker.get(FREEZ));
    }

    public void setFreePos(Vec3f freePos) {
        this.dataTracker.set(FREEX, freePos.getX());
        this.dataTracker.set(FREEY, freePos.getY());
        this.dataTracker.set(FREEZ, freePos.getZ());
    }

    public int getTSTime() {
        return this.dataTracker.get(TIMESTOPTIME);
    }

    public void setTSTime(int tsTime) {
        this.dataTracker.set(TIMESTOPTIME, tsTime);
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

        this.dataTracker.startTracking(TIMESTOPTIME, 0);
    }

    public void initialize() {
        this.noClip = true;
        this.setInvulnerable(true);
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 999999, 9, false, false));
    }

    // Attack controls
    public boolean canAttack() {
        if (hasUser()) {
            ITimeStop timeStop = (ITimeStop) user;
            return this.getMoveStun() < 1 && timeStop.getTimeStopTicks() < 1 && !user.hasStatusEffect(JStatusRegister.DAZED);
        }
        return false;
    }

    public class CanAttackData {
        public LivingEntity user;
        public boolean canAttack;

        public CanAttackData(LivingEntity l, boolean b) {
            this.user = l;
            this.canAttack = b;
        }
    }

    public CanAttackData canAttackWithData() {
        if (hasUser()) {
            ITimeStop timeStop = (ITimeStop) user;
            return new CanAttackData(user, this.getMoveStun() < 1 && timeStop.getTimeStopTicks() < 1 && !user.hasStatusEffect(JStatusRegister.DAZED));
        }
        return new CanAttackData(null, false);
    }

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

    public void setAttack(Attack attack, int animState) {
        this.curAttack = attack;
        this.setMoveStun(attack.moveStun);
        this.setState(animState);
    }

    public static void stun(LivingEntity entity, int duration, int amplifier) {
        if (duration == 0) {
            return;
        }
        entity.addStatusEffect(new StatusEffectInstance(JStatusRegister.DAZED, duration, amplifier, false, false, true));
        //JCraft.LOGGER.info("Stunned: " + entity.getEntityName() + " for: " + duration);
    }

    public static void damage(float damage, DamageSource damageSource, LivingEntity ent) {
        ent.damage(damageSource, 0.001f);

        // All stands ignore 10% of armor & armor toughness
        damage = DamageUtil.getDamageLeft(damage, (float) ent.getArmor() * 0.9f, (float) ent.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS) * 0.9f);
        damage = ((LivingEntityInvoker) ent).invokeModifyAppliedDamage(damageSource, damage);

        // Apply absorption
        float f = damage;
        damage = Math.max(damage - ent.getAbsorptionAmount(), 0.0F);
        ent.setAbsorptionAmount(ent.getAbsorptionAmount() - (f - damage));

        if (damage != 0.0F) {
            float h = ent.getHealth();
            if ((h - damage) <= 0) {
                ent.kill();
            } else {
                ent.setHealth(h - damage);
                ent.getDamageTracker().onDamage(damageSource, h, damage);
            }
        }
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

    // Define what happens within your stand block
    public void standBlock(LivingEntity player) {
        if (player == null) {
            return;
        }
        // Projectile deflection
        List<ProjectileEntity> toDeflect = this.world.getEntitiesByClass(ProjectileEntity.class, this.getBoundingBox().expand(0.75f), EntityPredicates.VALID_ENTITY);

        for (ProjectileEntity projectile : toDeflect) {
            if (projectile.getOwner() == player) {
                continue;
            }
            projectile.setVelocity(projectile.getVelocity().multiply(-0.5).add(0, -0.1, 0));
            projectile.velocityModified = true;
        }

        stun(player, 2, 2);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 5, 3, false, false, true));
    }

    // Define Middle Click Action
    public void initMiddleClick() {
    }

    // Define special attack actions
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
    }

    // Define desummon conditions
    public void desummon() {
        if (this.curAttack == null && this.getMoveStun() <= 0) {
            this.discard();
        }
    }

    // Define idle override
    public void idleOverride(LivingEntity player) {
    }

    // Define counter action
    public void counter(Entity entity, DamageSource source) {
        this.curAttack = null;
        this.setMoveStun(0);
    }

    public void cancelAttack() {
        this.curAttack = null;
        this.setMoveStun(0);
        this.setState(0);
    }

    // Does the stand default to being near the user?
    public boolean defaultToNear() {
        return !getRemote();
    }

    // Main
    @Override
    public void tick() {
        super.tick();

        if (this.isDead()) {
            return;
        }

        if (this.user == null) {
            if (world.isClient && this.getVehicle() instanceof LivingEntity living) {
                user = living;
            }
            return;
        } //else if (this.owner == null) { this.owner = player; }
        Entity vehicle = user.getVehicle();

        this.setMoveStun(this.getMoveStun() - 1);

        Attack attack = this.curAttack;

        Vec3d pos = this.getPos();
        Vec3d rotVec = this.getRotationVector();
        Vec3d eyePos = this.getEyePos();
        boolean isFree = getFree();
        boolean isRemote = getRemote();

        boolean client = this.world.isClient();
        if (client) {
            if (isRemote)
                user.setBodyYaw(user.getHeadYaw());
        } else {
            // Reset samestate
            if (this.getSameState()) {
                this.setSameState(false);
            }

            // Block break check
            if (this.getStandGauge() < 1) {
                user.addStatusEffect(new StatusEffectInstance(JStatusRegister.DAZED, 40, 2));
                this.playSound(SoundEvents.ITEM_TOTEM_USE, 1, 0.5f);
                this.kill();
            }

            // Return to user after stand detach move, provided it's finished recovering and there's no queued followup
            if (this.defaultToNear() && this.getMoveStun() < 1 && this.queuedAttack == null && attack == null) {
                this.setFree(false);
            }

            // Rotate with user
            if (!isFree || isRemote) {
                this.setHeadYaw(user.getHeadYaw());
                this.setRotation(user.getYaw(), user.getPitch());
            }

            // Remote mode
            if (isRemote) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 5, 9, true, false));
            }

            // Attack logic
            if (this.getMoveStun() >= 0 && !this.blocking && attack != null) {
                int stunTicks = (int) (attack.stun * 20f);
                //LOGGER.info("Stun ticks: " + stunTicks);

                int moveStun = attack.moveStun;
                float damage = attack.damage;
                float attackDist = attack.attackDist;

                int realInitTime = (moveStun - attack.initTime);

                boolean isChargeAttack = attack.attackType == AttackType.CHARGE || attack.attackType == AttackType.CHARGEBARRAGE;
                // Positioning
                if (isChargeAttack) {
                    if (this.getMoveStun() <= realInitTime) {
                        //float t = 1f - (float) this.getMoveStun() / (float) realInitTime;
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

                if (attack.attackType == AttackType.TIMESTOP && this.getMoveStun() == realInitTime) {
                    this.setTSTime(stunTicks);
                    this.curAttack = null;

                    StatusEffectInstance tsBlind = new StatusEffectInstance(StatusEffects.BLINDNESS, 19, 0, false, false, false);
                    user.addStatusEffect(tsBlind);

                    JCraftUtils.activeTimestops.add(new DimValues(this, pos, this.world.getRegistryKey()));

                    List<PlayerEntity> toCooldown = world.getEntitiesByClass(PlayerEntity.class,
                            new Box(eyePos.add(96.0, 96.0, 96.0), eyePos.subtract(96.0, 96.0, 96.0)), EntityPredicates.VALID_LIVING_ENTITY);

                    for (PlayerEntity player : toCooldown) {
                        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                        // Shader handling
                        ShaderActivationPacket.send(serverPlayer, this, 0, stunTicks, ShaderActivationPacket.Type.ZA_WARUDO);
                        if (serverPlayer == user || serverPlayer.isCreative()) continue;
                        // Puts all player items besides armor into cooldown for entire duration of timestop
                        for (int i = 0; i < serverPlayer.getInventory().main.size(); i++)
                            serverPlayer.getItemCooldownManager().set(serverPlayer.getInventory().main.get(i).getItem(), stunTicks);
                        serverPlayer.getItemCooldownManager().set(serverPlayer.getOffHandStack().getItem(), stunTicks);
                    }
                }

                boolean isBarrage = attack.attackType == AttackType.BARRAGE || attack.attackType == AttackType.CHARGEBARRAGE;

                if (
                        (attack.attackType == AttackType.BOX && this.getMoveStun() == realInitTime)
                                || (isBarrage && this.getMoveStun() % attack.interval == 0 && this.getMoveStun() <= realInitTime)
                                || (attack.attackType == AttackType.CHARGE && this.getMoveStun() <= realInitTime)
                                || (attack.attackType == AttackType.MULTIHIT && attack.attackTimes.contains(moveStun - this.getMoveStun()))
                ) {
                    //JCraft.LOGGER.info(this.getMoveStun() + " ACTIVE " + attack.interval);
                    Vec3d hPos = pos.add(0.0, user.getHeight() / 2, 0.0);
                    Vec3d fPos = (isChargeAttack) ?
                            hPos.add(rotVec) :
                            hPos.add(rotVec.multiply(attackDist)).subtract(0, attack.offset, 0);

                    List<Entity> filter = new ArrayList<>(List.of(this, user));
                    if (vehicle != null) {
                        filter.add(vehicle);
                    }

                    List<LivingEntity> hurt = JCraftUtils.GenerateHitbox(world, fPos, attack.hitboxSize, filter);
                    //JCraft.LOGGER.info("Hurt: " + hurt + " at world time: " + world.getTime());
                    //if (!hurt.contains(player)) { hurt.add(player); } // Damage Debugging

                    DamageSource playerSource = (user instanceof PlayerEntity playerEntity) ? DamageSource.player(playerEntity) : DamageSource.mob(user);

                    if (!hurt.isEmpty()) {
                        JCraft.CreateParticle((ServerWorld) this.world,
                                fPos.x + random.nextGaussian() * 0.25,
                                fPos.y + random.nextGaussian() * 0.25,
                                fPos.z + random.nextGaussian() * 0.25,
                                attack.hitspark + 1);

                        if (attack.impactSound != null) {
                            this.playSound(attack.impactSound, 1, 1);
                        }

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

                                    if (stand.getUser() instanceof ServerPlayerEntity serverPlayer) {
                                        serverPlayer.networkHandler.sendPacket(new StopSoundS2CPacket(null, SoundCategory.PLAYERS));
                                    }
                                }
                                if (user instanceof ServerPlayerEntity serverPlayer) {
                                    serverPlayer.networkHandler.sendPacket(new StopSoundS2CPacket(null, SoundCategory.PLAYERS));
                                }

                                // Cancels both barrages
                                cancelAttack();
                                stand.cancelAttack();
                                Vec3d midPos = stand.getPos().add(getPos()).multiply(0.5);
                                this.world.playSound(null, midPos.x, midPos.y, midPos.z, JSoundRegister.IMPACT_1, SoundCategory.NEUTRAL, 1, 0.5f);
                            }
                            continue;
                        }
                        damageLogic(world, livingEntity, kbVec, stunTicks, attack.stunType, attack.overrideStun, damage, attack.lift, playerSource, user);
                    }

                    for (LivingEntity livingEntity : clashed) {
                        livingEntity.removeStatusEffect(JStatusRegister.DAZED);
                        livingEntity.addStatusEffect(new StatusEffectInstance(JStatusRegister.DAZED, 10, 3, true, false));
                    }

                    this.specialAttack(attack, hurt);
                }
                /*
                else {
                    JCraft.LOGGER.info(this.getMoveStun() + " N " + attack.interval);
                }
                 */
            }

            if (this.getMoveStun() <= 0 && !this.blocking) {
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
                        case MIDDLEMOUSE -> this.initMiddleClick();
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
            } else if (this.blocking) {
                // Process block
                this.curAttack = null;

                this.setState(3);

                //JCraft.LOGGER.info(this.getMoveStun());
                if (this.getMoveStun() < 4) {
                    setMoveStun(4);
                }
                setDistanceOffset(this.blockDistance);
                setRotationOffset(this.attackRotation);

                standBlock(user);
            }
        }

        // JCraft.LOGGER.info( "State: " + this.getState() + " Movestun: " + this.getMoveStun() + " Currently attacking: " + (this.curAttack != null)); // Massive debug log

        int tsTime = this.getTSTime();

        if (tsTime > 0) {
            user.stopRiding();

            for (int h = 0; h < 1500 / tsTime; ++h) {
                this.world.addParticle(
                        ParticleTypes.MYCELIUM,
                        eyePos.x + random.nextTriangular(0, tsTime),
                        eyePos.y + random.nextTriangular(0, tsTime) / 4,
                        eyePos.z + random.nextTriangular(0, tsTime),
                        0.0, 0.0, 0.0
                );
            }

            List<Entity> toStop = world.getEntitiesByClass(Entity.class,
                    new Box(eyePos.add(96.0, 96.0, 96.0), eyePos.subtract(96.0, 96.0, 96.0)), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

            toStop.remove(this);
            toStop.remove(user);

            for (Entity entity : toStop) {
                /*
                if (entity.getFirstPassenger() instanceof StandEntity stand) {
                    JCraft.LOGGER.info("TSing stand: " + stand.getName());
                    if (tsTime - stand.tsRes > 0)
                }
                 */
                ITimeStop ts = ((ITimeStop) entity);
                ts.setTimeStopTicks(2);
            }

            if (!client) {
                this.setTSTime(tsTime - 1);
            }
        }

        if (this.curAttack != this.previousAttack && this.curAttack != null) {
            //JCraft.LOGGER.info("Logged previous attack change: " + this.curAttack + " " + this.previousAttack);
            this.previousAttack = this.curAttack;
        }

        //this.pastAttack = this.curAttack;
    }

    public static void damageLogic(World world, LivingEntity ent, Vec3d kbVec, int stunTicks, int stunType, boolean overrideStun, float damage, boolean lift, DamageSource playerSource, Entity attacker) {
        if (world.getGameRules().getBoolean(JCraft.COMBO_COUNTER) && !world.isClient()) {
            if (attacker instanceof PlayerEntity playerEntity) {
                IComboCounter comboCounter = (IComboCounter) playerEntity;
                if (comboCounter.getLastAttacked() != ent) {
                    comboCounter.setComboCount(1);
                } else {
                    StatusEffectInstance stun = ent.getStatusEffect(JStatusRegister.DAZED);
                    if (stun != null && stun.getAmplifier() == 1) {
                        //LOGGER.info("Target stun: " + stun.getDuration());
                        comboCounter.incrementComboCount();
                    } else {
                        comboCounter.setComboCount(1);
                    }

                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeShort(6);
                    buf.writeInt(comboCounter.getComboCount());
                    if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
                        ServerChannelFeedbackPacket.send(serverPlayerEntity, buf);
                    }
                }
                comboCounter.setLastAttacked(ent);
            }
        }

        baseDamageLogic(ent, kbVec, stunTicks, stunType, overrideStun, damage, lift, playerSource, attacker);
    }

    public static void baseDamageLogic(LivingEntity ent, Vec3d kbVec, int stunTicks, int stunType, boolean overrideStun, float damage, boolean lift, DamageSource source, Entity attacker) {
        boolean hit = true;

        if (ent.getFirstPassenger() instanceof StandEntity stand) {
            Attack standAttack = stand.curAttack;
            if (standAttack != null) {
                // Counter check
                if (standAttack.attackType == AttackType.COUNTER && stand.getMoveStun() < (standAttack.moveStun - standAttack.initTime)) {
                    stand.counter(attacker, source);
                    ent.removeStatusEffect(JStatusRegister.DAZED);
                    return;
                }

                // Move interruption
                if (!standAttack.hasArmor) {
                    stand.curAttack = null;
                    stand.setMoveStun(0);
                }
            }

            if (stand.blocking && !stand.getRemote()) {
                stand.setMoveStun(stand.getMoveStun() + (int) damage);
                stand.setStandGauge(stand.getStandGauge() - 2 * damage);
                stand.playSound(JSoundRegister.STAND_BLOCK, 1, 1);
                hit = false;
            }
        }

        // Velocity modification synchronisation
        if (ent instanceof ServerPlayerEntity serverPlayer)
            serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(ent));
        ent.velocityModified = true;

        // Stun application & overriding
        if (hit) {
            if (overrideStun)
                ent.removeStatusEffect(JStatusRegister.DAZED);
            stun(ent, stunTicks, stunType);
            ent.addVelocity(kbVec.x, kbVec.y, kbVec.z);
        }

        // Interrupting spec moves
        if (ent instanceof PlayerEntity playerEntity) {
            JCraftSpec spec = JCraftUtils.getSpec(playerEntity);
            if (spec != null && spec.curAttack != null && !spec.curAttack.hasArmor) {
                spec.CancelAttack();
            }
        }

        // Aerial hits keep the victim up
        if (lift) {
            Vec3d vel = ent.getVelocity();
            double finalY = vel.y;

            if (!ent.isOnGround()) {
                finalY = MathHelper.clamp(vel.y / 2, 0.085, 0.25);
            }

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
            this.playSound(JSoundRegister.STAND_DESUMMON, 1, 1);
            this.kill();
        }
        super.stopRiding();
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.isMagic() || source.isExplosive()) {
            return false;
        }
        return super.damage(source, amount);
    }

    // Physical properties
    @Override
    public void pushAwayFrom(Entity entity) { }

    @Override
    public boolean collidesWith(Entity other) { return false; }

    @Override
    public boolean addStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
        if (!hasUser()) return false;
        return getUser().addStatusEffect(effect, source);
    }

    // The fun stuff
    public void mobAI(MobEntity mob, LivingEntity target) {
        if (mob == target) {
            return;
        }
        if (target == null) {
            return;
        }
        if (!target.isAlive()) {
            return;
        }

        mob.getLookControl().lookAt(target); // Usually detrimental not to

        JCraftSpec enemySpec;
        StandEntity enemyStand = null;
        Attack enemyAttack = null;
        boolean enemyHasStand = false;

        double distance = target.distanceTo(mob);
        int enemyMoveStun = 0;
        int blockPlusTicks = 0;

        // Get enemy stand attack (most common)
        if (target.getFirstPassenger() instanceof StandEntity stand) {
            enemyHasStand = true;
            enemyStand = stand;

            enemyMoveStun = enemyStand.getMoveStun();
            enemyAttack = enemyStand.curAttack;

            if (enemyStand.blocking) {
                blockPlusTicks = enemyMoveStun;
            }

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
            if (enemyAttack.isRanged || enemyAttack.attackType == AttackType.BARRAGE) {
                wantToBlock = true;
            }

            // Block if the attack isn't ranged, but is within hitting distance, and doesn't block break
            if (enemyAttack.attackDist + enemyAttack.hitboxSize * 0.66 > distance && enemyAttack.damage * 2 < this.getStandGauge()) {
                wantToBlock = true;
            }
        }

        // Block if falling or there are projectiles nearby
        // 2 tick check interval is efficient because block doesn't run out by then, and finding entities is expensive
        if (this.age % 2 == 0) {
            List<ProjectileEntity> nearbyProjectiles = this.world.getEntitiesByClass(ProjectileEntity.class, mob.getBoundingBox().expand(3), EntityPredicates.VALID_ENTITY);
            boolean anyInAir = false;
            for (ProjectileEntity projectile : nearbyProjectiles) {
                if (projectile.getOwner() == mob) {
                    continue;
                }
                // No, checking for the projectile velocity does NOT work
                if (projectile.squaredDistanceTo(new Vec3d(projectile.prevX, projectile.prevY, projectile.prevZ)) > 0) {
                    anyInAir = true;
                    break;
                }
            }

            if (mob.fallDistance > 2 || anyInAir) {
                wantToBlock = true;
            }
        }
        //JCraft.LOGGER.info("WTB: " + wantToBlock);

        if (wantToBlock) {
            if (this.canAttack()) {
                this.blocking = true;
            }
        } else {
            this.blocking = false;
        }

        boolean stunned = mob.hasStatusEffect(JStatusRegister.DAZED);
        // If stunned, and about to get hit by another move, combo break sometimes
        if (stunned) {
            StatusEffectInstance mobStun = mob.getStatusEffect(JStatusRegister.DAZED);
            if (!this.blocking && enemyAttack != null && enemyMoveStun > enemyAttack.initTime && this.random.nextFloat() < 0.1f) {
                ComboBreak((ServerWorld) this.world, mob, mobStun);
            }
        }

        if (!this.blocking) {
            StatusEffectInstance stun = target.getStatusEffect(JStatusRegister.DAZED);
            // Overestimating stun up to 1/4 of a second for longer combos and frametraps
            int stunTicks = stun != null ? stun.getDuration() + random.nextInt(5) : 0;
            stunTicks += blockPlusTicks;
            stunTicks += ((ITimeStop) target).getTimeStopTicks();
            int move = SelectMove(mob, target, stunTicks, enemyMoveStun, distance, enemyStand, enemyAttack);
            Attack selectedAttack = null;

            boolean shouldPerformMove = this.getMoveStun() < 1;
            if (curAttack != null && curAttack.hasFollowup()) {
                shouldPerformMove = true;
            }

            if (move != -1) {
                selectedAttack = this.moves.get(move);

                if (shouldPerformMove) {
                    switch (move) {
                        case 0 -> this.initLightAttack();
                        case 1 -> this.initHeavyAttack();
                        case 2 -> this.initBarrage();
                        case 3 -> this.initSpecial1();
                        case 4 -> this.initUlt();
                        case 5 -> this.initSpecial2();
                        case 6 -> this.initSpecial3();
                        case 7 -> this.initMiddleClick();
                    }
                } else {
                    this.queuedAttack = JCraft.idToButton.get(move);
                }
            }

            double sideswitchDistance = 1.25;

            // If in range
            if (
                    (selectedAttack != null && distance < selectedAttack.attackDist + selectedAttack.hitboxSize * 0.75) ||
                            (enemyAttack != null && !enemyAttack.isRanged && distance < enemyAttack.attackDist + enemyAttack.hitboxSize * 1.5)
            ) {
                // Point body at enemy
                mob.lookAtEntity(target, 30, 30);

                // Move towards or away depending on distance and intent
                mob.getNavigation().setSpeed(distance < sideswitchDistance && selectedAttack == null ? 0.25 : -0.25);
            }

            float sStrafe = MathHelper.sin(this.age * 0.02f) / 3f;

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

        } else if (this.getMoveStun() > 4) { // if blocking & movestun > 4 means the enemy made you block
            // Don't buffer any attacks as you are minus and will DIE
            this.queuedAttack = null;
        }
    }

    public enum MoveSelectionResult {
        PASS,
        USE,
        STOP
    }

    // Used to help AIs that use stands with unique moves
    public MoveSelectionResult SpecificMoveSelectionCriterion(Attack attack, MobEntity mob, LivingEntity target, int stunTicks, int enemyMoveStun, double distance, StandEntity enemyStand, Attack enemyAttack) {
        return MoveSelectionResult.PASS;
    }

    private int SelectMove(MobEntity mob, LivingEntity target, int stunTicks, int enemyMoveStun, double distance, StandEntity enemyStand, Attack enemyAttack) {
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
                if (this.curAttack != null && this.curAttack.hasFollowup() && idToButton.get(i) == this.curAttack.button) {
                    //JCraft.LOGGER.info("Followup detected");
                    attack = this.curAttack.followup;
                    initTime = stunTicks; // Followups should always win the initTime contest, given that they cancel the current move
                } else {
                    continue;
                }
            }

            // Selection of characteristic moves with custom usage logic
            MoveSelectionResult result = SpecificMoveSelectionCriterion(attack, mob, target, stunTicks, enemyMoveStun, distance, enemyStand, enemyAttack);
            if (result == MoveSelectionResult.USE) {
                chosenMove = i;
                break;
            }
            if (result == MoveSelectionResult.STOP) {
                continue;
            }

            // Use mobility if opponent is far away
            if (attack.mobilityType != null) {
                // ...and isn't being comboed or is blocking
                if (stunTicks > 0) {
                    continue;
                }

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
