package net.arna.jcraft.common.entity.stand;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.old.Attack;
import net.arna.jcraft.common.attack.core.old.AttackType;
import net.arna.jcraft.common.attack.core.HitBoxData;
import net.arna.jcraft.common.component.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.network.s2c.TimeAccelStatePacket;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JParticleTypeRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class MadeInHeavenEntity extends StandEntity<MadeInHeavenEntity, MadeInHeavenEntity.State> {
    public static final Attack crm1 = new Attack(9, JCraft.lightCooldown, 0.75f, 11, 6, 1.5, 3f, 0.75f, AttackType.BOX, 0.4f, -0.1f, 0, SoundEvents.ITEM_TRIDENT_HIT)
            .setInfo("Speed Chop", "tiny stun, procs bleed");
    public static final Attack light = new Attack(0, JCraft.lightCooldown, 0.75f, 8, 5, 1.5, 4f, 0.75f, AttackType.BOX, 0.5f, -0.1f, 0, SoundEvents.ITEM_TRIDENT_HIT)
            .crouchingVariation(crm1)
            .setInfo("Slice", "quick combo starter");
    public static final Attack barrage = Attack.barrageAttack(2, 17, 0.85f, 32, 0, 2, 1.5f, 0.1f, 0.5f, 0, 3, JSoundRegistry.IMPACT_1)
            .setInfo("Barrage", "short, knocks back");
    private static final Attack barrageFinisher = new Attack(8, 17, 0.85f, 9, 6, 1.5, 1f, 1.1f, AttackType.BOX, 0.5f, 0, 0, JSoundRegistry.TW_KICK_HIT)
            .setHitspark(2)
            .setLaunch()
            .setInfo("Barrage (Final Hit)", "");
    public static final Attack speedslice = new Attack(7, 18, 1.25f, 11, 10, 0, 6f, 0.5f, AttackType.BOX, 1f, 0, 0)
            .setRanged(true)
            .setMobility(MobilityType.TELEPORT)
            .setInfo("Speed Slice", "short windup, harming teleport with hitstun and light knockback");
    public static final Attack legcrusher = new Attack(3, 16, 0.85f, 17, 8, 1.5, 7f, 0.25f, AttackType.BOX, 1.5f, 0.2f, 0, JSoundRegistry.TW_KICK_HIT)
            .appendHitbox(new HitBoxData(0, -0.5, 1))
            .setInfo("Leg Crusher", "combo starter/extender, mih hoofs the enemies legs in a quick, stunning attack");
    public static final Attack furychop = new Attack(4, 19, 0.85f, 24, 15, 1.6, 7f, 0.25f, AttackType.BOX, 1f, 0.2f, 0, JSoundRegistry.IMPACT_2)
            .setHitspark(2)
            .setInfo("Fury Chop", "combo extender, on hit gives haste(8s) to user and mining fatigue(8s) to victim, on whiff the fatigue goes to user");
    public static final Attack donut = new Attack(1, 23, 0.75f, 32, 26, 2, 8.5f, 0.0f, AttackType.BOX, 2f, 0.2f, 0, JSoundRegistry.IMPACT_7)
            .hyperArmor()
            .setHitspark(2)
            .setInfo("Roundabout Donut", "feigns stand desummon, uninterruptible combo starter");
    public static final Attack timeaccel = new Attack(6, 70, 40, 20, 0, AttackType.BOX)
            .setInfo("Time Acceleration",
                    """
                            allows charging the speedometer for 30s
                            it is charged by landing hits
                            the speedometer impacts the level of speed and haste granted by Time Acceleration
                            if the speedometer is full and the charging period finishes, enemies become standless for 15s""");
    private int circleTime = 0;
    public static final Attack circle = new Attack(8, 40, 14, 13, 0, 1.25f, AttackType.BOX)
            .setRanged(true)
            .setMobility(MobilityType.DASH)

            .setInfo("Heaven's Judgement", "rapidly circles a looked-at target within 4m at a radius of 7m");
    public static final Attack judgement = Attack.barrageAttack(5, 33, 1.25f, 60, 20, 0, 0f, 0.5f, 0, 0, 2)
            .crouchingVariation(circle)
            .setInfo("Divine Severance", "Made in Heaven rapidly speed slices an area, then finishes with a large, launching slice");

    public MadeInHeavenEntity(World worldIn) {
        super(StandType.MADE_IN_HEAVEN, worldIn, JSoundRegistry.MIH_SUMMON);
        idleRotation = -45f;

        description = "Lightspeed RUSHDOWN";

        pros = List.of(
                "best mobility",
                "great mixups",
                "good pressure",
                "low cooldowns"
        );

        cons = List.of(
                "bad defensive options",
                "relies on good spacing"
        );

        freespace =
                """
                PASSIVE: Speed I
                
                BNBs:
                    -the white supremacist
                    (Donut>M1>)Speed Slice>Leg Crusher>Fury Chop>M1>Barrage""";

        moves = List.of(light, donut, barrage, legcrusher, timeaccel, furychop, judgement, speedslice);

        super.initialize();
    }

    @Override
    protected void registerMoves(MoveMap<MadeInHeavenEntity, State> moves) {

    }

    private static final TrackedData<Integer> ACCELTIME;
    private static final TrackedData<Integer> TARGETID;
    private static final TrackedData<Integer> SPEEDOMETER;
    private static final TrackedData<Boolean> AFTERIMAGE;

    public static final int MAXIMUM_SPEEDOMETER = 30;

    public Vec3d judgementInitPos = Vec3d.ZERO;
    public Vec3d judgementInitRot = Vec3d.ZERO;
    private LivingEntity circleTarget;
    private float circleOrbitProg;

    static {
        ACCELTIME = DataTracker.registerData(MadeInHeavenEntity.class, TrackedDataHandlerRegistry.INTEGER);
        TARGETID = DataTracker.registerData(MadeInHeavenEntity.class, TrackedDataHandlerRegistry.INTEGER);
        SPEEDOMETER = DataTracker.registerData(MadeInHeavenEntity.class, TrackedDataHandlerRegistry.INTEGER);

        AFTERIMAGE = DataTracker.registerData(MadeInHeavenEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    public int getAccelTime() {
        return dataTracker.get(ACCELTIME);
    }

    public void setAccelTime(int aTime) {
        dataTracker.set(ACCELTIME, aTime);
    }

    public int getSpeedometer() {
        return dataTracker.get(SPEEDOMETER);
    }

    private int speedometer = 0;
    private void incrementSpeedometer() {
        if (++speedometer > MAXIMUM_SPEEDOMETER)
            speedometer = MAXIMUM_SPEEDOMETER;
        //JCraft.LOGGER.info("Speedometer increased to: " + speedometer);
    }

    /**
     * Tracks the speedometer value every tick, for actual addition see incrementSpeedometer()
     */
    public void setSpeedometer(int speedometer) {
        dataTracker.set(SPEEDOMETER, speedometer);
    }

    public boolean getAfterimage() {
        return dataTracker.get(AFTERIMAGE);
    }

    public void setAfterimage(boolean a) {
        dataTracker.set(AFTERIMAGE, a);
    }

    private Entity getCircleTarget() {
        int id = dataTracker.get(TARGETID);
        if (id == -1) return null;
        return world.getEntityById(id);
    }

    private void setTargetId(int id) {
        dataTracker.set(TARGETID, id);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        getDataTracker().startTracking(ACCELTIME, 0);
        getDataTracker().startTracking(TARGETID, -1);
        getDataTracker().startTracking(SPEEDOMETER, 0);
        getDataTracker().startTracking(AFTERIMAGE, false);
    }

    @Override
    public void cancelAttack() {
        if (curMove != null && curMove.id == 8) endCircle();
        super.cancelAttack();
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!canAttack()) return;
        if (getUserOrThrow().isSneaking())
            handleAttack(crm1, CooldownType.STAND_LIGHT, State.SPEED_CHOP);
        else
            handleAttack(light, CooldownType.STAND_LIGHT, State.SLICE);
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;
        if (handleAttack(donut, CooldownType.STAND_HEAVY, State.DONUT))
            playSound(JSoundRegistry.STAND_DESUMMON, 1, 1);
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleAttack(barrage, CooldownType.STAND_BARRAGE, State.BARRAGE))
            playSound(JSoundRegistry.MIH_BARRAGE, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (!canAttack()) return;
        if (handleAttack(legcrusher, CooldownType.STAND_SP1, State.LEG_CRUSHER))
            playSound(JSoundRegistry.MIH_LEGCRUSHER, 1, 1);
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        if (handleAttack(timeaccel, CooldownType.STAND_ULTIMATE, State.TIME_ACCELERATION))
            playSound(JSoundRegistry.MIH_TACCEL, 1, 1);
    }

    @Override
    public void initSpecial2() {
        if (!canAttack()) return;
        if (handleAttack(furychop, CooldownType.STAND_SP2, State.FURY_CHOP))
            playSound(JSoundRegistry.MIH_FURYCHOP, 1, 1);
    }

    @Override
    public void initSpecial3() {
        if (!canAttack()) return;
        LivingEntity user = getUserOrThrow();
        if (!user.isSneaking() && handleAttack(judgement, CooldownType.STAND_SP3, State.JUDGEMENT)) {
            playSound(JSoundRegistry.MIH_JUDGEMENT, 1, 1);
            return;
        }

        Set<? extends LivingEntity> targets = JUtils.generateHitbox(world, user.getEyePos().add(getRotationVector()), 2, Set.of(this, user));
        LivingEntity target = targets.stream()
                .filter(e -> e instanceof StandEntity<?, ?> stand && stand.hasUser())
                .map(e -> (StandEntity<?, ?>) e)
                .findFirst()
                .map(StandEntity::getUser)
                .orElse(null);

        if (target != null && handleAttack(circle, CooldownType.STAND_SP3, State.CIRCLE_STARTUP)) {
            circleTarget = target;
            circleOrbitProg = user.getHeadYaw();
            setTargetId(circleTarget.getId());
            playSound(JSoundRegistry.MIH_CIRCLE, 1f, 1f);
        }
    }


    @Override
    public void initUtil() {
        if (!canAttack()) return;
        if (handleAttack(speedslice, CooldownType.UTILITY, State.SPEED_SLICE))
            playSound(JSoundRegistry.MIH_SPEEDSLICE, 1, 1);
    }

    @Override
    public boolean handleAttack(Attack attack, CooldownType cooldownType, State animState) {
        if (!hasUser()) return false;
        LivingEntity player = getUserOrThrow();

        CooldownsComponent cooldowns = JComponents.getCooldowns(player);
        int cooldown = cooldowns.getCooldown(cooldownType);

        if (cooldown > 0)
            return false;

        int cdMult = (this.getAccelTime() > 0) ? 10 : 20;
        cooldowns.setCooldown(cooldownType, (int) (attack.cooldown * cdMult));

        setMove(attack, animState);
        return true;
    }

    @Override
    public void specialAttack(Attack attack, Set<LivingEntity> entities) {
        LivingEntity user = getUser();

        boolean hit = !entities.isEmpty();
        if (getAccelTime() > 0 && hit && speedometer < MAXIMUM_SPEEDOMETER) incrementSpeedometer();

        switch (attack.id) {
            case (2) -> {
                if (getMoveStun() < 10) curMove = barrageFinisher;
            }
            case (4) -> {
                if (user == null) return;

                if (hit) {
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
                int accelTime = 600;
                setAccelTime(accelTime);
                setAfterimage(true);
                TimeAccelStatePacket.sendStart(Objects.requireNonNull(world.getServer()).getPlayerManager(), this, accelTime);
                speedometer = 0;
            }
            case (7) -> {
                if (user == null) return;
                curMove = null;
                speedSlice(user, user.getEyePos(), user.getEyePos().add(user.getRotationVector().multiply(8)), 6, 1, 1.5);
            }
            case (8) -> startCircle();
            case (9) -> {
                for (LivingEntity ent : entities)
                    if (!JUtils.isBlocking(ent))
                        ent.addStatusEffect(new StatusEffectInstance(JStatusRegistry.BLEEDING, 80, 1, true, false, true));
            }
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
        HitResult hitResult = world.raycast(new RaycastContext(start, destination, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
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

            List<LivingEntity> hurt = world.getEntitiesByClass(LivingEntity.class, new Box(vec1, vec2), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
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

        if (getAccelTime() > 0 && !hurtAll.isEmpty()) incrementSpeedometer();

        playSound(JSoundRegistry.MIH_ZOOM, 1f, 1f);
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
        super.tick();

        if (!hasUser()) return;
        LivingEntity user = getUserOrThrow();
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

            return;
        }

        // Circling
        if (circleTime > 0) {
            circleTime--;
            if (circleTarget == null || !circleTarget.isAlive() || circleTarget.isRemoved())
                circleTime = 1;
            else {
                circleOrbitProg += 0.15f;
                boolean toExit = curMove != null && curMove.id != 8;
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
        boolean userIsStunned = user.hasStatusEffect(JStatusRegistry.DAZED);
        setAccelTime(aTime - 1);

        if (aTime > 1) {
            List<Entity> toCatch = world.getEntitiesByClass(Entity.class,
                    getBoundingBox().expand(96), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
            for (Entity entity : toCatch) {
                if (entity instanceof LivingEntity) continue;
                entity.tick();
            }
        } else if (aTime == 1) {
            if (speedometer == MAXIMUM_SPEEDOMETER) {
                List<LivingEntity> toCatch = world.getEntitiesByClass(LivingEntity.class,
                        getBoundingBox().expand(96), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                toCatch.remove(this);
                toCatch.remove(user);

                for (LivingEntity entity : toCatch) // 15s of Standless to any victims of Universe Reset
                    entity.addStatusEffect(new StatusEffectInstance(JStatusRegistry.STANDLESS, 300, 0, true, false));
            }

            setAfterimage(false);

            speedometer = 0;
        }

        if (userIsStunned) {
            if (circleTime > 0) endCircle();
        } else {
            if (aTime > 0) {
                int amplifier = speedometer / 3;
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 20, amplifier, true, false));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 20, amplifier, true, false));
            } else
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 0, true, false));
        }

        // Tracking
        setSpeedometer(speedometer);
    }

    // Animation code
    public enum State implements StandAnimationState<MadeInHeavenEntity> {
        IDLE(builder -> builder.loop("animation.mih.idle")),
        SLICE(builder -> builder.playAndHold("animation.mih.slice")),
        BLOCK(builder -> builder.loop("animation.mih.block")),
        DONUT(builder -> builder.playAndHold("animation.mih.donut")),
        BARRAGE(builder -> builder.loop("animation.mih.barrage")),
        SPEED_SLICE(builder -> builder.playAndHold("animation.mih.speedslice")),
        JUDGEMENT(builder -> builder.playAndHold("animation.mih.judgement")),
        LEG_CRUSHER(builder -> builder.playAndHold("animation.mih.legcrusher")),
        FURY_CHOP(builder -> builder.playAndHold("animation.mih.furychop")),
        TIME_ACCELERATION(builder -> builder.playAndHold("animation.mih.taccel")),
        CIRCLE_STARTUP(builder -> builder.playAndHold("animation.mih.circlestartup")),
        SPEED_CHOP(builder -> builder.playAndHold("animation.mih.speedchop"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(MadeInHeavenEntity attacker, AnimationBuilder builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @Nullable String getSummonAnimation() {
        return "animation.mih.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
