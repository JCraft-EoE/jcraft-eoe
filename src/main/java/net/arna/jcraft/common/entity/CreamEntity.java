package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.attack.AttackType;
import net.arna.jcraft.common.attack.StunType;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.arna.jcraft.common.attack.Attack.unusable;

public class CreamEntity extends StandEntity<CreamEntity, CreamEntity.State> {
    public static final Attack crm1 = new Attack(14, JCraft.lightCooldown, 0.75f, 15, 9, 1.75, 5f, 0.75f, AttackType.BOX, 1f, 0.3f, 0, JSoundRegistry.IMPACT_3)
            .setInfo("Bite", "applies Slowness II (2s) on hit");
    public static final Attack light = new Attack(0, JCraft.lightCooldown, 0.75f, 14, 6, 1.5, 5f, 0.75f, AttackType.BOX, 1f, 0.1f, 0, JSoundRegistry.IMPACT_3)
            .crouchingVariation(crm1)
            .setInfo("Punch", "quick combo starter");
    public static final Attack heavy = new Attack(1, 14, 1f, 30, 20, 1.5, 8f, 0.1f, AttackType.BOX, 2, 0, 0, JSoundRegistry.IMPACT_3)
            .setHitspark(2)
            .hyperArmor()
            .setInfo("Vertical Chop", "slow, uninterruptable combo starter");
    public static final Attack combo = new Attack(2, 17, 0.75f, 36, 0, 2.0, 5f, 0.1f, AttackType.MULTIHIT, 1, 0, List.of(10, 17, 25), JSoundRegistry.IMPACT_3)
            .setInfo("3-hit Combo", "medium windup, good stun");
    public static final Attack grab = new Attack(3, 20, 1f, 20, 8, 1.5, 3f, 0f, AttackType.BOX, 1.5f, 0, 0)
            .setGrab()
            .setInfo("Grab", "unblockable, knocks back");
    public static final Attack grabhit = new Attack(4, 0, 1f, 20, 13, 2.0, 6f, 1.5f, AttackType.BOX, 0.25f, 0, 0, JSoundRegistry.IMPACT_1)
            .setLaunch()
            .setHitspark(2)
            .setInfo("Grab (Hit)", "");
    /*
    public static final Attack charge = new Attack(5, 20, 4f, 13, 5, 1.5, 8f, 0.25f, AttackType.CHARGE, 1, 0, State.CHARGE_HIT.ordinal(), JSoundRegistry.IMPACT_3)
            .setRanged(true)
            .setInfo("Charge", "3.5 block range, combo starter/extender");
     */
    public static final Attack surprise = new Attack(5, 20, 24, 14, 0, AttackType.BOX)
            .setInfo("Surprise", "Cream disappears into the ground, then pops out in a nearby looked location");
    public static final Attack destroy = new Attack(6, 20, 1f, 30, 21, 2, 0f, 1.25f, AttackType.BOX, 0.25f, 0f, 0, JSoundRegistry.IMPACT_5)
            .setHitspark(2)
            .setStunOverride(true)
            .setStunType(StunType.LAUNCH)
            .hyperArmor()
            .setUB(false)
            .setInfo("Destroy", "slow, uninterruptable, unblockable knockdown");
    public static final Attack consume = new Attack(7, 32, 1f, 40, 35, 2.0, 2f, 0f, AttackType.BOX)
            .setRanged(true)
            .setInfo("Void", "high windup, 6 seconds");
    public static final Attack enter = new Attack(8, 2, 15, 10, 0, 0f, AttackType.BOX)
            .setInfo("Enter Cream", "cream consumes itself and the user halfway, increasing mobility and decreasing defense").setMobility(MobilityType.FLIGHT);
    public static final Attack exit = new Attack(9, 2, 15, 5, 0, 0f, AttackType.BOX)
            .setInfo("Exit Cream", "cream and its user return from the void");

    public static final Attack balllight = new Attack(10, 2, 0.1f, 14, 7, 2, 5f, 0.75f, AttackType.BOX, 1f, 0.2f, 0, JSoundRegistry.IMPACT_3)
            .setInfo("Swipe", "quick air-to-ground poke");
    public static final Attack ballheavy = new Attack(11, 14, 0.1f, 20, 14, 2, 9f, 1.25f, AttackType.BOX, 0.75f, 0.3f, 0, JSoundRegistry.TW_KICK_HIT)
            .setHitspark(2)
            .hyperArmor()
            .setLaunch()
            .setInfo("Overhead Smash", "slow, uninterruptable launcher");
    public static final Attack ballcombo = new Attack(12, 14, 0.1f, 36, 0, 2, 7f, 0.1f, AttackType.MULTIHIT, 0.75f, 0.3f, List.of(10, 17, 25), JSoundRegistry.IMPACT_3)
            .setInfo("3-hit Combo", "less stun than grounded version");
    public static final Attack ballcharge = new Attack(13, 20, 28, 13, 0, AttackType.BOX)
            .setInfo("Void Charge", "cream quickly transforms into a black hole and charges in the pointed direction");

    private static final TrackedData<Integer> VOID_TIME;
    private static final TrackedData<Boolean> HALF_BALL;
    private Vec3d chargeDir;
    private Vec3f outDir;
    private boolean charging = false;

    static {
        VOID_TIME = DataTracker.registerData(CreamEntity.class, TrackedDataHandlerRegistry.INTEGER);
        HALF_BALL = DataTracker.registerData(CreamEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    public CreamEntity(World worldIn) {
        super(StandType.CREAM, worldIn);

        idleRotation = 220f;

        description = "Close Range SETUP";

        pros = List.of(
                "many block bypassing options",
                "powerful void state",
                "good poking",
                "good mobility"
        );

        cons = List.of(
                "very variable reward on hit",
                "blind and deaf in the void",
                "below average speed"
        );

        freespace = """
                BNBs (i. - in Cream):
                    M1>Combo>M1>Charge>Grab
                    Chop>Void
                    i.M1>land+s.OFF>s.ON+Combo>M1>Charge>Grab""";

        moves = List.of(light, heavy, combo, grab, consume, surprise, destroy, enter);

        super.initialize();
    }

    public void beginHalfBall() {
        this.dataTracker.set(HALF_BALL, true);
        idleDistance = 0f;
        blockDistance = 0f;
        maxStandGauge = 45f;

        moves = List.of(balllight, ballheavy, ballcombo, ballcharge, consume, unusable, unusable, exit);
        markAllAttackButtons();
        gatherAllAttacks();
    }

    public void endHalfBall() {
        this.dataTracker.set(HALF_BALL, false);
        idleDistance = 1.25f;
        blockDistance = 0.75f;
        maxStandGauge = 90f;

        moves = List.of(light, heavy, combo, grab, consume, surprise, destroy, enter);
        markAllAttackButtons();
        gatherAllAttacks();
    }

    public boolean isHalfBall() {
        return dataTracker.get(HALF_BALL);
    }

    public int getVoidTime() {
        return dataTracker.get(VOID_TIME);
    }

    public void setVoidTime(int vTime) {
        dataTracker.set(VOID_TIME, vTime);
        if (vTime == 0) setReset(true);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        getDataTracker().startTracking(VOID_TIME, 0);
        getDataTracker().startTracking(HALF_BALL, false);
    }

    @Override
    public boolean canAttack() {
        if (hasUser() && !(getUser() instanceof PlayerEntity) && getVoidTime() > 0)
            return false; // Prevents mobs from attacking while in void state and cancelling void early
        return super.canAttack();
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!canAttack()) return;

        if (isHalfBall()) handleAttack(balllight, CooldownType.STAND_LIGHT, State.BALL_LIGHT);
        else if (getUserOrThrow().isSneaking()) handleAttack(crm1, CooldownType.STAND_LIGHT, State.BITE);
        else handleAttack(light, CooldownType.STAND_LIGHT, State.LIGHT);
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;

        if (isHalfBall()) {
            if (handleAttack(ballheavy, CooldownType.STAND_HEAVY, State.BALL_HEAVY))
                playSound(JSoundRegistry.CREAM_SMASH, 1, 1);
        } else if (handleAttack(heavy, CooldownType.STAND_HEAVY, State.HEAVY))
            playSound(JSoundRegistry.CREAM_HEAVY, 1, 1);
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;

        if (isHalfBall() && handleAttack(ballcombo, CooldownType.STAND_BARRAGE, State.BALL_COMBO))
            playSound(JSoundRegistry.CREAM_COMBO, 1, 1);
        else if (handleAttack(combo, CooldownType.STAND_BARRAGE, State.COMBO))
            playSound(JSoundRegistry.CREAM_COMBO, 1, 1);
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;

        if (handleAttack(consume, CooldownType.STAND_ULT, State.CONSUME))
            playSound(JSoundRegistry.CREAM_CONSUME, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (!canAttack()) return;

        if (isHalfBall() && handleAttack(ballcharge, CooldownType.STAND_SP1, State.BALL_CONSUME))
            playSound(JSoundRegistry.CREAM_BALLDASH, 1, 1);
        else if (handleAttack(grab, CooldownType.STAND_SP1, State.GRAB))
            playSound(JSoundRegistry.CREAM_GRAB, 1, 1);
    }

    private Vec3f outPos;
    @Override
    public void initSpecial2() {
        if (isHalfBall() || !canAttack()) return;
        LivingEntity user = getUserOrThrow();
        Vec3d eyePos = user.getEyePos();
        Vec3d rotVec = user.getRotationVector();
        HitResult hitResult = world.raycast(new RaycastContext(eyePos, eyePos.add(rotVec.multiply(16)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));

        if (hitResult.getType() != HitResult.Type.MISS && handleAttack(surprise, CooldownType.STAND_SP2, State.SURPRISE)) {
            setFree(true);
            setFreePos(new Vec3f(user.getPos()));
            outPos = new Vec3f(hitResult.getPos());
            playSound(JSoundRegistry.CREAM_SUMMON, 1, 1);
        }
    }

    @Override
    public void initSpecial3() {
        if (!canAttack()) return;

        if (!isHalfBall() && handleAttack(destroy, CooldownType.STAND_SP3, State.DESTROY))
            playSound(JSoundRegistry.CREAM_OVERHEAD, 1, 1);
    }

    @Override
    public void initUtil() {
        if (!canAttack()) return;

        if (isHalfBall()) {
            if (handleAttack(exit, CooldownType.UTIL, State.EXIT))
                playSound(JSoundRegistry.CREAM_EXIT, 1, 1);
        } else if (handleAttack(enter, CooldownType.UTIL, State.ENTER))
            playSound(JSoundRegistry.CREAM_ENTER, 1, 1);
    }

    @Override
    public void specialAttack(Attack attack, Set<LivingEntity> entities) {
        switch (attack.id) {
            case (2) -> {
                if (getMoveStun() == 11) {
                    Vec3d rV = getRotationVector();

                    for (LivingEntity ent : entities) {
                        ent.takeKnockback(1, rV.x, rV.z);
                        ent.velocityModified = true;
                    }
                }
            }
            case (3) -> {
                if (!entities.isEmpty()) {
                    // Grab bypasses and disables block
                    for (LivingEntity ent : entities) {
                        stun(ent, 20, 0);

                        if (ent.getFirstPassenger() instanceof StandEntity<?, ?> stand) stand.blocking = false;
                    }

                    setAttack(grabhit, State.GRAB_HIT);
                }
            }
            case (5) -> {
                charging = true;
                outDir = GravityChangerAPI.getGravityDirection(this).getUnitVector();
                outDir.scale(-1f);

                outPos.subtract(outDir);
                setFreePos(outPos);

                setVoidTime(surprise.moveStun - surprise.initTime);

                playSound(JSoundRegistry.IMPACT_5, 1, 0.75f);
            }
            case (6) -> {
                DamageSource playerSource = DamageSource.mob(getUser());

                for (LivingEntity ent : entities) {
                    trueDamage(8, playerSource, ent);
                    ent.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 35, 0));
                }
            }
            case (7) -> {
                endHalfBall();
                setVoidTime(120);
                charging = false;
                this.curAttack = null;
            }
            case (8) -> beginHalfBall();
            case (9) -> endHalfBall();
            case (11) -> {
                for (LivingEntity ent : entities)
                    if (!JUtils.isBlocking(ent))
                        ent.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 35, 0));
            }
            case (13) -> {
                if (!hasUser()) return;

                playSound(JSoundRegistry.CREAM_CHARGE, 1, 1);
                charging = true;
                chargeDir = getUserOrThrow().getRotationVector().multiply(0.5);
                setVoidTime(15);
            }
            case (14) -> {
                for (LivingEntity ent : entities)
                    if (!JUtils.isBlocking(ent))
                        ent.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1));
            }
        }
    }

    @Override
    protected Box calculateBoundingBox() {
        double x = getX();
        double y = getY();
        double z = getZ();

        if (isHalfBall())
            return new Box(x - 0.6, y + 0.6, z - 0.6, x + 0.6, y + 2, z + 0.6);
        if (getState() == State.SURPRISE)
            return new Box(x - 0.6, y + 0, z - 0.6, x + 0.6, y + 0.3, z + 0.6);
        return super.calculateBoundingBox();
    }

    @Override
    public void desummon() {
        // Stop voiding if voiding
        if (this.getVoidTime() > 0) {
            this.setVoidTime(0);
            return;
        }

        // Real desummon if not voiding
        super.desummon();
    }

    @Override
    public boolean defaultToNear() {
        if (charging) return false;
        return super.defaultToNear();
    }

    @Override
    public void tick() {
        if (age == 1) playSound(JSoundRegistry.CREAM_SUMMON, 1f, 1f);
        super.tick();
        boolean server = !this.world.isClient();

        if (hasUser()) {
            LivingEntity user = getUserOrThrow();
            boolean isPlayer = false;
            boolean notCorS = false;

            Vec3d pos = this.getEyePos();
            int vTime = this.getVoidTime();
            boolean voiding = (vTime > 0);

            // Players get creative flight, and mobs get nogravved and y level equalization (see: if voiding)
            if (user instanceof PlayerEntity playerEntity) {
                notCorS = (!playerEntity.isCreative() && !playerEntity.isSpectator());
                if (notCorS && !charging)
                    playerEntity.getAbilities().flying = voiding;
                isPlayer = true;
            }

            if (server) {
                if (!charging) {
                    if (this.curAttack != null) {
                        this.setVoidTime(0);
                        voiding = false;
                    }
                    this.idleOverride = this.getVoidTime() > 0;
                }

                user.setInvulnerable(this.getVoidTime() > 0);
            }

            if (voiding) {
                if (server) {
                    if (world.getGameRules().getBoolean(JCraft.STAND_GRIEFING)) {
                        // Unfun 3x4x3 void code
                        for (int x = -1; x < 2; x++) {
                            for (int y = -1; y < 3; y++) {
                                for (int z = -1; z < 2; z++) {
                                    BlockPos curPos = this.getBlockPos().add(x, y, z);
                                    if (this.world.getBlockState(curPos).getBlock().getBlastResistance() > 100.1f)
                                        continue;
                                    this.world.setBlockState(curPos, Block.getStateFromRawId(0));
                                }
                            }
                        }
                    }

                    if (charging) {
                        if (getFree()) {
                            Vec3f newPos = getFreePos().copy();
                            newPos.add(outDir);
                            setFreePos(newPos);
                        } else if (chargeDir != null) {
                            user.setVelocity(chargeDir);
                            user.velocityModified = true;
                            if (user instanceof ServerPlayerEntity player)
                                player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(user));
                        }
                    } else {
                        setStateNoReset(State.IDLE);

                        if (!isPlayer) {
                            double y = user.getY();
                            Vec3d vel = new Vec3d(user.getVelocity().x, 0.0, user.getVelocity().z);

                            // Targetting priority
                            LivingEntity targetEntity = user.getDamageTracker().getBiggestAttacker();
                            if (targetEntity == null && user instanceof MobEntity mob)
                                targetEntity = mob.getTarget();
                            if (targetEntity == null)
                                targetEntity = user.getAttacker();

                            // If target wasn't found, thrash around
                            Vec3d target = targetEntity != null ? targetEntity.getPos() : this.getPos().add(Math.sin(this.age * 0.2) * 2, Math.sin(this.age * 0.2) / 4, Math.cos(this.age * 0.2) * 2);

                            double dY = MathHelper.clamp(target.getY() - y, -1, 1);
                            y += dY;

                            vel = vel.add(target.subtract(user.getPos().add(random.nextDouble() * 2, random.nextDouble() * 3, random.nextDouble() * 3)).normalize()).multiply(0.3);

                            user.setVelocity(vel);
                            user.setPos(user.getX(), y, user.getZ());

                            if (vTime < 10)
                                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 5, 1, true, false));
                        }
                    }

                    List<LivingEntity> toDamage = world.getEntitiesByClass(LivingEntity.class,
                            new Box(pos.add(1.5, 1.5, 1.5), pos.subtract(1.5, 1.5, 1.5)), EntityPredicates.VALID_ENTITY);

                    toDamage.remove(user);
                    toDamage.remove(this);

                    if (charging) {
                        for (LivingEntity ent : toDamage) {
                            if (getMoveStun() % 2 == 0) { // More consistent
                                stun(ent, 4, 0);

                                StandEntity<?, ?> enemyStand = JUtils.getStand(ent);
                                if (enemyStand != null) enemyStand.cancelAttack();
                            }

                            ent.damage(DamageSource.OUT_OF_WORLD, 5);
                        }
                    } else {
                        for (LivingEntity ent : toDamage) {
                            if (age % 4 == 0) {
                                stun(ent, 2, 0);

                                StandEntity<?, ?> enemyStand = JUtils.getStand(ent);
                                if (enemyStand != null) enemyStand.cancelAttack();
                            }

                            ent.damage(DamageSource.OUT_OF_WORLD, 2.5f);
                        }

                        setAlphaOverride(0);
                    }

                    if (notCorS && !getFree())
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 25, 0, false, false));
                } else {
                    for (int i = 0; i < 16; i++)
                        world.addParticle(ParticleTypes.MYCELIUM,
                                pos.x + (random.nextFloat() - 0.5f) * 2f,
                                pos.y + (random.nextFloat() - 0.5f) * 2f,
                                pos.z + (random.nextFloat() - 0.5f) * 2f,
                                0, 0, 0);
                }

                setVoidTime(vTime - 1);
                setDistanceOffset(0);
            } else {
                if (isIdle() && charging) {
                    charging = false;
                    setFree(false);
                }

                if (!isHalfBall()) return;
                setAlphaOverride(0.1f);
                user.onLanding();
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 5, 9, true, false));

                // Player Half-Ball controls
                if (user instanceof ServerPlayerEntity serverPlayer) {
                    if (lastRemoteInputTime - age > 4) updateRemoteInputs(0, 0, false);

                    Vec3d finalSpeed = Vec3d.ZERO;
                    if (!blocking && !user.hasStatusEffect(JStatusRegistry.DAZED)) {
                        Vec3d eP = user.getEyePos();
                        Vec3d groundPos = world.raycast(
                                new RaycastContext(eP, eP.add(0, -24, 0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user)
                        ).getPos();

                        double groundDist = groundPos.distanceTo(pos);
                        double stabilization = user.getVelocity().y;
                        if (stabilization < 0) stabilization *= -0.75;
                        else stabilization = 0;

                        if (getRemoteJumpInput()) {
                            if (groundDist < 5) {
                                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 10, 2, true, false));
                                if (groundDist < 3)
                                    finalSpeed = finalSpeed.add(0, 0.25 / groundDist + stabilization, 0);
                            }
                        }

                        Vec3d rotVec = user.getRotationVector();
                        finalSpeed = finalSpeed.add(rotVec.multiply(getRemoteForwardInput() / 30)); // Forward movement
                        finalSpeed = finalSpeed.add(rotVec.rotateY(1.5707963f).multiply(getRemoteSideInput() / 30)); // Side movement
                        user.addVelocity(finalSpeed.x, finalSpeed.y, finalSpeed.z);
                        serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(user));
                    }
                } else resetAlphaOverride();
            }
        }
    }

    // Animation code
    public enum State implements StandAnimationState<CreamEntity> {
        IDLE((cream, builder) -> builder.loop("animation.cream." + ( cream.getVoidTime() > 0 ? "void" : cream.isHalfBall() ? "ball" : "" ) + "idle")),
        LIGHT(builder -> builder.playAndHold("animation.cream.light")),
        BALL_LIGHT(builder -> builder.playAndHold("animation.cream.balllight")),
        BLOCK(builder -> builder.loop("animation.cream.block")),
        BALL_BLOCK(builder -> builder.loop("animation.cream.ballblock")),
        HEAVY(builder -> builder.playAndHold("animation.cream.heavy")),
        BALL_HEAVY(builder -> builder.playAndHold("animation.cream.ballheavy")),
        COMBO(builder -> builder.playAndHold("animation.cream.combo")),
        BALL_COMBO(builder -> builder.playAndHold("animation.cream.ballcombo")),
        CONSUME(builder -> builder.playAndHold("animation.cream.consume")),
        BALL_CONSUME(builder -> builder.playAndHold("animation.cream.ballconsume")),
        SURPRISE(builder -> builder.playAndHold("animation.cream.surprise")),
        CHARGE_HIT(builder -> builder.playAndHold("animation.cream.charge_hit")),
        GRAB(builder -> builder.playAndHold("animation.cream.grab")),
        GRAB_HIT(builder -> builder.playAndHold("animation.cream.grab_hit")),
        ENTER(builder -> builder.playAndHold("animation.cream.enter")),
        EXIT(builder -> builder.playAndHold("animation.cream.exit")),
        DESTROY(builder -> builder.playAndHold("animation.cream.destroy")),
        BITE(builder -> builder.playAndHold("animation.cream.bite"));

        private final BiConsumer<CreamEntity, AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this((creamEntity, builder) -> animator.accept(builder));
        }

        State(BiConsumer<CreamEntity, AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(CreamEntity stand, AnimationBuilder builder) {
            animator.accept(stand, builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected String getSummonAnimation() {
        return "animation.cream.summon";
    }

    @Override
    public State getIdleState() {
        return State.IDLE;
    }

    @Override
    public State getBlockState() {
        return isHalfBall() ? State.BALL_BLOCK : State.BLOCK;
    }
}
