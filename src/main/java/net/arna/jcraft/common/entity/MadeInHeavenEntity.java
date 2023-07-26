package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.attack.AttackType;
import net.arna.jcraft.common.attack.HitBoxData;
import net.arna.jcraft.common.network.s2c.TimeAccelStatePacket;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.JParticleTypeRegistry;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//TODO: give MiH a trail during speed slice and heaven's judgement
public class MadeInHeavenEntity extends StandEntity {
    // placeholder sound
    public static final Attack light = new Attack(0, JCraft.lightCooldown, 0.75f, 8, 5, 1.5, 4f, 0.75f, AttackType.BOX, 0.5f, -0.1f, 0, SoundEvents.ITEM_TRIDENT_HIT)
            .setInfo("Slice", "quick combo starter");
    public static final Attack barrage = new Attack(2, 17, 0.85f, 32, 0, 2, 1.5f, 0.1f, AttackType.BARRAGE, 0.5f, 0, 3, JSoundRegister.IMPACT_1)
            .setInfo("Barrage", "short, knocks back");
    public static final Attack speedslice = new Attack(7, 18, 1.25f, 11, 10, 0, 6f, 0.5f, AttackType.BOX, 1f, 0, 0)
            .setRanged(true)
            .setMobility(MobilityType.TELEPORT)
            .setInfo("Speed Slice", "short windup, harming teleport with hitstun and light knockback");
    public static final Attack legcrusher = new Attack(3, 16, 0.85f, 17, 8, 1.5, 7f, 0.25f, AttackType.BOX, 1.5f, 0.2f, 0, JSoundRegister.TW_KICK_HIT)
            .appendHitbox(new HitBoxData(0, -0.5, 1))
            .setInfo("Leg Crusher", "combo starter/extender, mih hoofs the enemies legs in a quick, stunning attack");
    public static final Attack furychop = new Attack(4, 19, 0.85f, 24, 15, 1.6, 7f, 0.25f, AttackType.BOX, 1f, 0.2f, 0, JSoundRegister.IMPACT_2)
            .setHitspark(2)
            .setInfo("Fury Chop", "combo extender, on hit gives haste(8s) to user and mining fatigue(8s) to victim, on whiff the fatigue goes to user");
    public static final Attack donut = new Attack(1, 23, 0.75f, 32, 26, 2.5, 8.5f, 0.0f, AttackType.BOX, 3f, 0.2f, 0, JSoundRegister.IMPACT_4)
            .hyperArmor()
            .setHitspark(2)
            .setInfo("Roundabout Donut", "feigns stand desummon, uninterruptable combo starter");
    public static final Attack timeaccel = new Attack(6, 70, 40, 20, 0, AttackType.BOX)
            .setInfo("Time Acceleration", "2s windup, 15s t. accel, enemies standless for 15s after finishing");
    private int circleTime = 0;
    public static final Attack judgement = new Attack(5, 33, 1.25f, 60, 20, 0, 0f, 0.5f, AttackType.BARRAGE, 0, 0, 2, null)
            .setInfo("Divine Severance", "Made in Heaven rapidly speed slices an area, then finishes with a large, launching slice");
    public static final Attack circle = new Attack(8, 40, 14, 13, 0, 1.25f, AttackType.BOX)
            .setRanged(true)
            .setMobility(MobilityType.DASH)
            .crouchingVariation(judgement)
            .setInfo("Heaven's Judgement", "rapidly circles a looked-at target within 4m at a radius of 7m");

    public Vec3d judgementInitPos = Vec3d.ZERO;
    public Vec3d judgementInitRot = Vec3d.ZERO;

    private static final TrackedData<Integer> ACCELTIME;
    private static final TrackedData<Boolean> AFTERIMAGE;
    private static final TrackedData<Integer> TARGETID;

    public MadeInHeavenEntity(World worldIn) {
        super(StandType.MADE_IN_HEAVEN, worldIn);
        super.initialize();
        idleRotation = 225f;

        description = "Lightspeed RUSHDOWN";

        pros = List.of(
                "absurdly good mobility",
                "good mixups",
                "good pressure",
                "low cooldowns"
        );

        cons = List.of(
                "zero defensive options barring running away",
                "highly spacing-dependent"
        );

        freespace =
                """
                        PASSIVE: Speed I
                        BNBs:
                        the white supremacist
                            (Donut>M1>)Speed Slice>Leg Crusher>Fury Chop>M1>Barrage""";

        moves = List.of(light, donut, barrage, legcrusher, timeaccel, furychop, circle, speedslice);
    }

    static {
        ACCELTIME = DataTracker.registerData(MadeInHeavenEntity.class, TrackedDataHandlerRegistry.INTEGER);
        TARGETID = DataTracker.registerData(MadeInHeavenEntity.class, TrackedDataHandlerRegistry.INTEGER);
        AFTERIMAGE = DataTracker.registerData(MadeInHeavenEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    public int getAccelTime() {
        return this.dataTracker.get(ACCELTIME);
    }

    public void setAccelTime(int aTime) {
        this.dataTracker.set(ACCELTIME, aTime);
    }

    public boolean getAfterimage() {
        return this.dataTracker.get(AFTERIMAGE);
    }

    public void setAfterimage(boolean a) {
        this.dataTracker.set(AFTERIMAGE, a);
    }

    public Entity getCircleTarget() {
        int id = dataTracker.get(TARGETID);
        if (id == -1) return null;
        return world.getEntityById(id);
    }

    public void setTargetId(int id) {
        this.dataTracker.set(TARGETID, id);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        getDataTracker().startTracking(ACCELTIME, 0);
        getDataTracker().startTracking(TARGETID, -1);
        getDataTracker().startTracking(AFTERIMAGE, false);
    }

    @Override
    public void cancelAttack() {
        if (curAttack != null && curAttack.id == 8) endCircle();
        super.cancelAttack();
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!canAttack()) return;
        handleAttack(light, JCraft.standLightCD, 2);
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;
        handleAttack(donut, JCraft.standHeavyCD, 4);
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleAttack(barrage, JCraft.standBarrageCD, 5)) {
            playSound(JSoundRegister.MIH_BARRAGE, 1, 1);
        }
    }

    @Override
    public void initSpecial1() {
        if (!canAttack()) return;
        if (handleAttack(legcrusher, JCraft.standS1CD, 8)) {
            playSound(JSoundRegister.MIH_LEGCRUSHER, 1, 1);
        }
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        if (handleAttack(timeaccel, JCraft.standUltCD, 10)) {
            playSound(JSoundRegister.MIH_TACCEL, 1, 1);
        }
    }

    @Override
    public void initSpecial2() {
        if (!canAttack()) return;
        if (handleAttack(furychop, JCraft.standS2CD, 9)) {
            playSound(JSoundRegister.MIH_FURYCHOP, 1, 1);
        }
    }

    private LivingEntity circleTarget;
    private float circleOrbitProg;

    @Override
    public void initSpecial3() {
        if (!canAttack()) return;
        LivingEntity user = getUserOrThrow();
        if (user.isSneaking() && handleAttack(judgement, JCraft.standS3CD, 7)) {
            playSound(JSoundRegister.MIH_JUDGEMENT, 1, 1);
        } else {
            List<? extends LivingEntity> targets = JUtils.generateHitbox(world, user.getEyePos().add(getRotationVector()), 2, List.of(this, user));
            LivingEntity target = null;
            for (LivingEntity living : targets) {
                target = JUtils.getUserIfStand(living);
                break;
            }
            if (target != null && handleAttack(circle, JCraft.standS3CD, 11)) {
                circleTarget = target;
                circleOrbitProg = user.getHeadYaw();
                setTargetId(circleTarget.getId());
                playSound(JSoundRegister.MIH_CIRCLE, 1f, 1f);
            }
        }
    }


    @Override
    public void initUtil() {
        if (!canAttack()) return;
        if (handleAttack(speedslice, JCraft.utilCD, 6)) {
            playSound(JSoundRegister.MIH_SPEEDSLICE, 1, 1);
        }
    }

    @Override
    public boolean handleAttack(Attack attack, String cooldownName, int animState) {
        if (!hasUser()) return false;
        LivingEntity player = getUserOrThrow();
        NbtCompound userData = ((IEntityDataSaver) player).getPersistentData();
        int cooldown = userData.getInt(cooldownName);

        if (cooldown > 0)
            return false;

        int cdMult = (this.getAccelTime() > 0) ? 10 : 20;
        userData.putInt(cooldownName, (int) (attack.cooldown * cdMult));

        setAttack(attack, animState);
        return true;
    }

    private static final Attack barrageFinisher = new Attack(8, 17, 0.85f, 9, 6, 1.5, 1f, 1.1f, AttackType.BOX, 0.5f, 0, 0, JSoundRegister.TW_KICK_HIT)
            .setHitspark(2)
            .setLaunch()
            .setInfo("Barrage (Final Hit)", "");

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        LivingEntity user = this.getUser();
        switch (attack.id) {
            case (2) -> {
                if (getMoveStun() < 10) curAttack = barrageFinisher;
            }
            case (4) -> {
                if (user == null) return;

                if (entities.size() > 0) {
                    for (LivingEntity ent : entities)
                        ent.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 160, 0));
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 160, 0));
                } else user.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 160, 0));
            }
            case (5) -> {
                if (user == null) return;
                if (getMoveStun() > 1) {
                    if (getMoveStun() < 40) {
                        speedSlice(user,
                                judgementInitPos.add(judgementInitRot.multiply(random.nextTriangular(2, 2))),
                                judgementInitPos.add(random.nextTriangular(0, 5), random.nextTriangular(0, 5), random.nextTriangular(0, 5)),
                                1f, 0.1f, 1.75);
                    } else {
                        judgementInitPos = user.getPos();
                        judgementInitRot = Vec3d.fromPolar(0, user.getYaw());
                    }
                } else {
                    speedSlice(user,
                            judgementInitPos.subtract(user.getRotationVector().multiply(3)),
                            judgementInitPos.add(judgementInitRot.multiply(10)), 6, 3, 2.0);
                }
            }
            case (6) -> {
                setAccelTime(300);
                setAfterimage(true);
                TimeAccelStatePacket.sendStart(Objects.requireNonNull(world.getServer()).getPlayerManager(), this, 300);
            }
            case (7) -> {
                if (user == null) return;
                curAttack = null;
                speedSlice(user, user.getEyePos(), user.getEyePos().add(user.getRotationVector().multiply(8)), 6, 1, 1.5);
            }
            case (8) -> startCircle();
        }
    }

    private void startCircle() {
        circleTime = 100;
        setAfterimage(true);
        updateRemoteInputs(0, 0, false);
    }

    private void endCircle() {
        circleTime = 0;
        circleTarget = null;
        setTargetId(-1);
        if (getAccelTime() <= 0) setAfterimage(false);
    }

    private void speedSlice(LivingEntity player, Vec3d start, Vec3d destination, float damage, float kb, double size) {
        HitResult hitResult = this.world.raycast(new RaycastContext(start, destination, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
        Vec3d pos1 = player.getPos();
        Vec3d pos2 = hitResult.getPos();
        Vec3d towardsVec = pos2.subtract(pos1);

        Vec3d kbVec = towardsVec.normalize();

        DamageSource playerSource = DamageSource.mob(player);

        player.teleport(pos2.x, pos2.y, pos2.z);

        List<LivingEntity> hurtAll = new ArrayList<>();

        double count = Math.round(pos1.distanceTo(pos2));

        for (int i = 0; i < count; i++) {
            Vec3d curPos = pos1.add(towardsVec.multiply(i / count));

            Vec3d vec1 = curPos.add(-size, -size, -size);
            Vec3d vec2 = curPos.add(size, size, size);

            JUtils.displayHitbox(getWorld(), vec1, vec2);

            List<LivingEntity> hurt = this.world.getEntitiesByClass(LivingEntity.class, new Box(vec1, vec2), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
            hurt.removeIf(hurtAll::contains);
            hurtAll.addAll(hurt);
        }

        hurtAll.remove(this);
        hurtAll.remove(player);
        //if (!hurtAll.contains(player)) { hurtAll.add(player); }

        for (LivingEntity ent : hurtAll) {
            LivingEntity target = JUtils.getUserIfStand(ent);
            damageLogic(world, target, kbVec.multiply(kb).add(0, kb / 4, 0), 20, 1, false, damage, true, (int) (4 + damage), playerSource, player);
        }

        playSound(JSoundRegister.MIH_ZOOM, 1f, 1f);
    }

    private void createSpeedParticles(Entity entity) {
        Box box = entity.getBoundingBox();
        for (int i = 0; i < box.getAverageSideLength(); i++)
            world.addParticle(JParticleTypeRegistry.SPEEDPARTICLE,
                    random.nextDouble() * box.getXLength() + box.minX,
                    random.nextDouble() * box.getYLength() + box.minY,
                    random.nextDouble() * box.getZLength() + box.minZ,
                    0, 0, 0);
    }

    @Override
    public void tick() {
        if (age == 1) playSound(JSoundRegister.MIH_SUMMON, 1f, 1f);
        super.tick();

        if (!hasUser()) return;
        LivingEntity user = getUserOrThrow();
        setAlpha((float) MathHelper.clamp(255.0 * squaredDistanceTo(user) / 2, 0.0, 255.0) / 255f);
        int aTime = getAccelTime();

        if (world.isClient) {
            Entity clientCircleTarget = getCircleTarget();
            if (clientCircleTarget != null)
                user.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, clientCircleTarget.getEyePos());

            if (aTime > 1) { // Updating on the client, to make sure all is smooth
                createSpeedParticles(this);

                List<Entity> toCatch = world.getEntitiesByClass(Entity.class, // Lower range by 32 to reduce lag
                        getBoundingBox().expand(96), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                for (Entity entity : toCatch) {
                    if (entity instanceof LivingEntity) continue;
                    if (entity.getPos().squaredDistanceTo(new Vec3d(entity.prevX, entity.prevY, entity.prevZ)) > 0)
                        createSpeedParticles(entity);
                    entity.tick();
                }
            }
        } else {
            // Circling
            if (circleTime > 0) {
                circleTime--;
                if (circleTarget == null || !circleTarget.isAlive() || circleTarget.isRemoved())
                    circleTime = 1;
                else {
                    circleOrbitProg += 0.15f;
                    boolean toExit = curAttack != null && curAttack.id != 8;
                    Vec3d rotVec = user.getRotationVector();
                    Vec3d exitVel = Vec3d.ZERO;
                    double side = getRemoteSideInput();
                    double forw = getRemoteForwardInput();
                    // This isn't normalized and idc
                    if (side != 0) {
                        exitVel = exitVel.add(rotVec.rotateY(1.5707963f).multiply(side));
                        toExit = true;
                    }
                    if (forw != 0) {
                        exitVel = exitVel.add(rotVec.multiply(forw));
                        toExit = true;
                    }

                    if (toExit) {
                        user.setVelocity(exitVel.add(0, 0.5, 0));
                        endCircle();
                    } else {
                        Vec3d orbitPos = circleTarget.getEyePos().add(Math.sin(circleOrbitProg) * 7, 0, Math.cos(circleOrbitProg) * 7);
                        Vec3d towardsVel = orbitPos.subtract(user.getPos()).normalize();
                        double stabilization = user.getPos().distanceTo(orbitPos);
                        if (stabilization > 0.5) stabilization = 0.5;
                        user.setVelocity(user.getVelocity().multiply(stabilization).add(towardsVel));
                    }

                    if (user instanceof ServerPlayerEntity serverPlayer)
                        serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayer));
                    else
                        user.velocityModified = true;
                }
            }
            if (circleTime == 1) endCircle();

            // Time Accel handling
            boolean userIsStunned = user.hasStatusEffect(JStatusRegister.DAZED);
            setAccelTime(aTime - 1);

            if (aTime > 1) {
                List<Entity> toCatch = world.getEntitiesByClass(Entity.class,
                        getBoundingBox().expand(96), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
                for (Entity entity : toCatch) {
                    if (entity instanceof LivingEntity) continue;
                    entity.tick();
                }
            } else if (aTime == 1) {
                List<LivingEntity> toCatch = world.getEntitiesByClass(LivingEntity.class,
                        getBoundingBox().expand(96), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                toCatch.remove(this);
                toCatch.remove(user);

                for (LivingEntity entity : toCatch) // 15s of Standless to any victims of Time Acceleration
                    entity.addStatusEffect(new StatusEffectInstance(JStatusRegister.STANDLESS, 300, 0, true, false));

                setAfterimage(false);
            }

            if (userIsStunned) {
                if (circleTime > 0) endCircle();
            } else {
                if (aTime > 0) {
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 20, 2, true, false));
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 20, 2, true, false));
                } else {
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 0, true, false));
                }
            }
        }
    }

    // Animations
    @Override
    protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
        AnimationController<E> controller = event.getController();
        AnimationBuilder builder = new AnimationBuilder();
        if (this.getSameState()) {
            controller.markNeedsReload();
        }
        switch (this.getState()) {
            default -> controller.setAnimation(builder.loop("animation.mih.idle"));
            case 2 -> controller.setAnimation(builder.playAndHold("animation.mih.slice"));
            case 3 -> controller.setAnimation(builder.loop("animation.mih.block"));
            case 4 -> controller.setAnimation(builder.playAndHold("animation.mih.donut"));
            case 5 -> controller.setAnimation(builder.loop("animation.mih.barrage"));
            case 6 -> controller.setAnimation(builder.playAndHold("animation.mih.speedslice"));
            case 7 -> controller.setAnimation(builder.playAndHold("animation.mih.judgement"));
            case 8 -> controller.setAnimation(builder.playAndHold("animation.mih.legcrusher"));
            case 9 -> controller.setAnimation(builder.playAndHold("animation.mih.furychop"));
            case 10 -> controller.setAnimation(builder.playAndHold("animation.mih.taccel"));
            case 11 -> controller.setAnimation(builder.playAndHold("animation.mih.circlestartup"));

            //default -> throw new IllegalStateException("Unexpected value: " + this.getState());
        }
        return PlayState.CONTINUE;
    }
}
