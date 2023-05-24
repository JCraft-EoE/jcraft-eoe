package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.util.Attack;
import net.arna.jcraft.common.util.AttackType;
import net.arna.jcraft.common.util.JCraftUtils;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.block.Block;
import net.minecraft.entity.DamageUtil;
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
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.List;

public class CreamEntity extends StandEntity implements IAnimatable, IAnimationTickable {
    AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

    public static Attack light = new Attack(2, 0.75f, 14, 6, 1.5, 5f, 0.75f, AttackType.BOX, 1f, 0.1f, 0, JSoundRegister.IMPACT_3)
            .setInfo("Punch", "quick combo starter");
    public static Attack heavy = new Attack(14, 1f, 30, 20, 1.5, 10f, 0.1f, AttackType.BOX, 2, 0, 0, JSoundRegister.IMPACT_3).setHitspark(2).setArmor(true)
            .setInfo("Vertical Chop", "slow, uninterruptable combo starter");
    public static Attack combo = new Attack(17, 0.75f, 36, 0, 2.0, 7f, 0.1f, AttackType.MULTIHIT, 1, 0, List.of(10, 17, 25), JSoundRegister.IMPACT_3)
            .setInfo("3-hit Combo", "medium windup, good stun");
    public static Attack consume = new Attack(32, 1f, 40, 35, 2.0, 2f, 0f, AttackType.BOX).setRanged(true)
            .setInfo("Void", "high windup, 6 seconds");
    public static Attack charge = new Attack(20, 4f, 13, 5, 1.5, 8f, 0.25f, AttackType.CHARGE, 1, 0, 8, JSoundRegister.IMPACT_3).setRanged(true)
            .setInfo("Charge", "3.5 block range, combo starter/extender");
    public static Attack grab = new Attack(20, 1f, 20, 8, 1.5, 3f, 0f, AttackType.BOX, 1.5f, 0, 0)
            .setUB(false)
            .setInfo("Grab", "unblockable, knocks back");
    public static Attack grabhit = new Attack(0, 1f, 20, 13, 2.0, 6f, 1.5f, AttackType.BOX, 0.25f).setLaunch();

    public static Attack destroy = new Attack(25, 1f, 30, 21, 2, 0f, 1.25f, AttackType.BOX, 0f, 0f, 0, JSoundRegister.IMPACT_5).setHitspark(2).setArmor(true)
            .setUB(true)
            .setInfo("Destroy", "slow, uninterruptable, unblockable knockdown");

    public static Attack enter = new Attack(2, 15, 10, 0, 0f, AttackType.BOX)
            .setInfo("Enter Cream", "cream consumes itself and the user halfway, increasing mobility and decreasing defense").setMobility(MobilityType.FLIGHT);
    public static Attack exit = new Attack(2, 15, 5, 0, 0f, AttackType.BOX)
            .setInfo("Exit Cream", "cream and its user return from the void");

    public static Attack balllight = new Attack(2, 0f, 14, 7, 2, 5f, 0.75f, AttackType.BOX, 1f, 0.2f, 0, JSoundRegister.IMPACT_3)
            .setInfo("Swipe", "quick air-to-ground poke");
    public static Attack ballheavy = new Attack(14, 0f, 20, 14, 2, 9f, 1.25f, AttackType.BOX, 0.75f, 0.3f, 0, JSoundRegister.TW_KICK_HIT)
            .setHitspark(2).setArmor(true).setLaunch()
            .setInfo("Overhead Smash", "slow, uninterruptable launcher");

    public static TrackedData<Integer> VOIDTIME;
    public static TrackedData<Boolean> HALFBALL;

    public void beginHalfBall() {
        this.dataTracker.set(HALFBALL, true);
        idleDistance = 0f;
        blockDistance = 0f;
        maxStandGauge = 45f;

        moves = List.of(balllight, ballheavy, unusable, unusable, consume, unusable, grab, exit);
    }

    public void endHalfBall() {
        this.dataTracker.set(HALFBALL, false);
        idleDistance = 1.25f;
        blockDistance = 0.75f;
        maxStandGauge = 90f;

        moves = List.of(light, heavy, combo, destroy, consume, charge, grab, enter);
    }

    public boolean getHalfBall() {
        return this.dataTracker.get(HALFBALL);
    }

    public CreamEntity(EntityType<? extends StandEntity> type, World worldIn) {
        super(type, worldIn);
        super.initialize();
        idleRotation = 220f;

        description = "Close Range SETUP";

        pros = List.of(
                "two block bypass moves",
                "unpunishable void state",
                "good poking"
        );

        cons = List.of(
                "very variable reward on hit",
                "predictable playstyle",
                "blind and deaf in the void"
        );

        freespace = "BNBs:\n" +
                "    M1>Charge>Grab\n" +
                "    Chop>Smash>Void\n" +
                "    Chop>M1>Combo>M1>Charge>Grab";

        moves = List.of(light, heavy, combo, destroy, consume, charge, grab, enter);
    }

    static {
        VOIDTIME = DataTracker.registerData(CreamEntity.class, TrackedDataHandlerRegistry.INTEGER);
        HALFBALL = DataTracker.registerData(CreamEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    public int getVoidTime() {
        return this.dataTracker.get(VOIDTIME);
    }

    public void setVoidTime(int vTime) {
        this.dataTracker.set(VOIDTIME, vTime);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.getDataTracker().startTracking(VOIDTIME, 0);
        this.getDataTracker().startTracking(HALFBALL, false);
    }

    @Override
    public boolean canAttack() {
        if (hasUser()) {
            if (!(getUser() instanceof PlayerEntity) && this.getVoidTime() > 0) {
                return false; // Prevents mobs from attacking while in void state and cancelling void early
            }
        }
        return super.canAttack();
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!this.canAttack()) {
            return;
        }
        if (getHalfBall())
            handleAttack(balllight, JCraft.standLightCD, 2);
        else
            handleAttack(light, JCraft.standLightCD, 2);
    }

    @Override
    public void initHeavyAttack() {
        if (!this.canAttack()) {
            return;
        }
        if (getHalfBall()) {
            if (handleAttack(ballheavy, JCraft.standHeavyCD, 4)) {
                this.playSound(JSoundRegister.CREAM_SMASH, 1, 1);
            }
        } else if (handleAttack(heavy, JCraft.standHeavyCD, 4)) {
            this.playSound(JSoundRegister.CREAM_HEAVY, 1, 1);
        }
    }

    @Override
    public void initBarrage() {
        if (!this.canAttack()) {
            return;
        }
        if (getHalfBall()) { //TODO: half-ball barrage

        } else if (handleAttack(combo, JCraft.standBarrageCD, 5)) {
            this.playSound(JSoundRegister.CREAM_COMBO, 1, 1);
        }
    }

    @Override
    public void initUlt() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(consume, JCraft.standUltCD, 6)) {
            this.playSound(JSoundRegister.CREAM_CONSUME, 1, 1);
        }
    }

    @Override
    public void initSpecial1() {
        if (!this.canAttack()) {
            return;
        }
        if (getHalfBall()) { //TODO: half-ball Grab

        } else if (handleAttack(grab, JCraft.standS1CD, 9)) {
            this.playSound(JSoundRegister.CREAM_GRAB, 1, 1);
        }
    }

    @Override
    public void initSpecial2() {
        if (!this.canAttack()) {
            return;
        }
        if (getHalfBall()) { //TODO: Surge

        } else if (handleAttack(charge, JCraft.standS2CD, 7)) {
            this.playSound(JSoundRegister.CREAM_CHARGE, 1, 1);
        }
    }

    @Override
    public void initSpecial3() {
        if (!this.canAttack()) {
            return;
        }
        if (getHalfBall()) { //TODO: half-ball Destroy

        } else if (handleAttack(destroy, JCraft.standS3CD, 13)) {
            this.playSound(JSoundRegister.CREAM_CHARGE, 1, 1);
        }
    }

    @Override
    public void initMiddleClick() {
        if (!this.canAttack()) {
            return;
        }
        if (getHalfBall()) {
            if (handleAttack(exit, JCraft.standMMBCD, 12))
                this.playSound(JSoundRegister.CREAM_EXIT, 1, 1);
        } else {
            if (handleAttack(enter, JCraft.standMMBCD, 11))
                this.playSound(JSoundRegister.CREAM_ENTER, 1, 1);
        }
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        if (attack == combo && this.getMoveStun() == 11) {
            Vec3d rV = this.getRotationVector();

            for (LivingEntity ent : entities) {
                ent.takeKnockback(1, rV.x, rV.z);
                ent.velocityModified = true;
            }
        } else if (attack == consume) {
            endHalfBall();
            setVoidTime(120);
            this.curAttack = null;
        } else if (attack == grab) {
            if (entities.size() > 0) {
                // Grab bypasses and disables block
                for (LivingEntity ent : entities) {
                    stun(ent, 20, 0);

                    if (ent.getFirstPassenger() instanceof StandEntity stand) {
                        stand.blocking = false;
                    }
                }

                this.curAttack = grabhit;
                this.setMoveStun(20);
                this.setState(10);
            }
        } else if (attack == ballheavy) {
            for (LivingEntity ent : entities) {
                if (!JCraftUtils.isBlocking(ent)) {
                    ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 35, 0));
                }
            }
        } else if (attack == destroy) {
            DamageSource playerSource = DamageSource.mob(getUser());

            for (LivingEntity ent :
                    entities) {
                float damage = 10f;
                ent.damage(playerSource, 0.001f);

                // All stands ignore 10% of armor & armor toughness
                damage = DamageUtil.getDamageLeft(damage, (float) ent.getArmor() * 0.9f, (float) ent.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS) * 0.9f);

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
                        ent.getDamageTracker().onDamage(playerSource, h, damage);
                    }
                }

                ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 35, 0));
            }
        } else if (attack == enter) {
            beginHalfBall();
        } else if (attack == exit) {
            endHalfBall();
        }
    }

    @Override
    protected Box calculateBoundingBox() {
        if (getHalfBall()) {
            double x = getX();
            double y = getY();
            double z = getZ();
            return new Box(x - 0.6, y + 0.6, z - 0.6, x + 0.6, y + 2, z + 0.6);
        }
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
    public void tick() {
        if (age == 1) {
            this.world.playSound(null, this.getX(), this.getY(), this.getZ(), JSoundRegister.STAND_SUMMON, SoundCategory.PLAYERS, 1f, 1f);
        }

        super.tick();
        boolean server = !this.world.isClient();

        if (hasUser()) {
            LivingEntity user = this.getUser();
            boolean isPlayer = false;
            boolean notCorS = false;

            Vec3d pos = this.getEyePos();
            int vTime = this.getVoidTime();
            boolean voiding = (vTime > 0);

            // Players get creative flight, and mobs get nogravved and y level equalization (see if voiding)
            if (user instanceof PlayerEntity playerEntity) {
                notCorS = (!playerEntity.isCreative() && !playerEntity.isSpectator());

                if (notCorS) {
                    playerEntity.getAbilities().flying = voiding;
                }

                isPlayer = true;
            }

            if (server) {
                if (this.curAttack != null) {
                    this.setVoidTime(0);
                }
                boolean sVoiding = (this.getVoidTime() > 0);

                this.idleOverride = sVoiding;
                user.setInvulnerable(sVoiding);
            }

            if (voiding) {
                if (server) {
                    if (this.world.getGameRules().getBoolean(JCraft.CREAM_BLACK_HOLE_ERASES_BLOCKS)) {
                        // Unfun 3x4x3 void code
                        for (int x = -1; x < 2; x++) {
                            for (int y = -1; y < 3; y++) {
                                for (int z = -1; z < 2; z++) {
                                    BlockPos curPos = this.getBlockPos().add(x, y, z);

                                    if (this.world.getBlockState(curPos).getBlock().getBlastResistance() > 100.1f) {
                                        continue;
                                    }
                                    this.world.setBlockState(curPos, Block.getStateFromRawId(0));
                                }
                            }
                        }
                    }

                    if (!isPlayer) {
                        double y = user.getY();
                        Vec3d vel = new Vec3d(user.getVelocity().x, 0.0, user.getVelocity().z);
                        // Targetting priority
                        LivingEntity targetEntity = user.getDamageTracker().getBiggestAttacker();
                        if (targetEntity == null && user instanceof MobEntity mob) {
                            targetEntity = mob.getTarget();
                        }
                        if (targetEntity == null) {
                            targetEntity = user.getAttacker();
                        }
                        // If target wasn't found, thrash around
                        Vec3d target = targetEntity != null ? targetEntity.getPos() : this.getPos().add(Math.sin(this.age * 0.2) * 2, Math.sin(this.age * 0.2) / 4, Math.cos(this.age * 0.2) * 2);

                        double dY = MathHelper.clamp(target.getY() - y, -1, 1);
                        y += dY;

                        vel = vel.add(target.subtract(user.getPos().add(random.nextDouble() * 2, random.nextDouble() * 3, random.nextDouble() * 3)).normalize()).multiply(0.3);

                        user.setVelocity(vel);
                        user.setPos(user.getX(), y, user.getZ());

                        if (vTime < 10) {
                            user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 5, 1, true, false));
                        }
                    }

                    List<LivingEntity> toDamage = this.world.getEntitiesByClass(LivingEntity.class,
                            new Box(pos.add(1.5, 1.5, 1.5), pos.subtract(1.5, 1.5, 1.5)), EntityPredicates.VALID_ENTITY);

                    toDamage.remove(user);
                    toDamage.remove(this);

                    for (LivingEntity ent : toDamage)
                        ent.damage(DamageSource.OUT_OF_WORLD, 1.5f);
                    if (notCorS)
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 30, 0, false, false));
                } else {
                    for (int i = 0; i < 16; i++) {
                        this.world.addParticle(ParticleTypes.MYCELIUM,
                                pos.x + (random.nextFloat() - 0.5f) * 2f,
                                pos.y + (random.nextFloat() - 0.5f) * 2f,
                                pos.z + (random.nextFloat() - 0.5f) * 2f,
                                0, 0, 0);
                    }
                }

                this.setVoidTime(vTime - 1);

                this.setDistanceOffset(0);
                this.setState(0);
                this.setAlpha(1f);
            } else {
                if (getHalfBall()) {
                    this.setAlpha(0.1f);
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 5, 9, true, false));

                    // Player Half-Ball controls
                    if (user instanceof ServerPlayerEntity serverPlayer) {
                        if (lastRemoteInputTime - age > 4) {
                            updateRemoteInputs(0, 0, false);
                        }

                        if (!blocking) {
                            Vec3d eP = user.getEyePos();
                            Vec3d groundPos = world.raycast(
                                    new RaycastContext(eP, eP.add(0, -24, 0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user)
                            ).getPos();

                            if (getRemoteJumpInput() && groundPos.squaredDistanceTo(getPos()) < 9)
                                user.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 4, 4, true, false));
                        }

                        Vec3d finalSpeed = Vec3d.ZERO;
                        Vec3d rotVec = user.getRotationVector();
                        finalSpeed = finalSpeed.add(rotVec.multiply(getRemoteForwardInput() / 30)); // Forward movement
                        finalSpeed = finalSpeed.add(rotVec.rotateY(1.5707963f).multiply(getRemoteSideInput() / 30)); // Side movement
                        user.addVelocity(finalSpeed.x, finalSpeed.y, finalSpeed.z);
                        serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(user));
                    }
                } else {
                    this.setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(user) / 2, 0.0, 255.0) / 255f);
                }
            }
        }
    }

    // Animation code
    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.animationFactory;
    }

    @Override
    public int tickTimer() {
        return age;
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationController controller = event.getController();
        AnimationBuilder builder = new AnimationBuilder();

        if (age < 20 && !getHalfBall() && getState() < 2) {
            controller.setAnimation(builder.playOnce("animation.cream.summon"));
            return PlayState.CONTINUE;
        }

        if (this.getSameState()) {
            controller.markNeedsReload();
        }
        if (getHalfBall()) {
            switch (this.getState()) {
                default -> controller.setAnimation(builder.loop("animation.cream.ballidle"));
                case 2 -> controller.setAnimation(builder.playAndHold("animation.cream.balllight"));
                case 3 -> controller.setAnimation(builder.loop("animation.cream.ballblock"));
                case 4 -> controller.setAnimation(builder.playAndHold("animation.cream.ballheavy"));
                case 6 -> controller.setAnimation(builder.playAndHold("animation.cream.consume"));
                case 11 -> controller.setAnimation(builder.playAndHold("animation.cream.enter"));
                case 12 -> controller.setAnimation(builder.playAndHold("animation.cream.exit"));
            }
        } else {
            switch (this.getState()) {
                case 0 -> controller.setAnimation(builder.loop("animation.cream.voididle"));
                default -> controller.setAnimation(builder.loop("animation.cream.idle"));
                case 2 -> controller.setAnimation(builder.playAndHold("animation.cream.light"));
                case 3 -> controller.setAnimation(builder.loop("animation.cream.block"));
                case 4 -> controller.setAnimation(builder.playAndHold("animation.cream.heavy"));
                case 5 -> controller.setAnimation(builder.playAndHold("animation.cream.combo"));
                case 6 -> controller.setAnimation(builder.playAndHold("animation.cream.consume"));
                case 7 -> controller.setAnimation(builder.playAndHold("animation.cream.charge"));
                case 8 -> controller.setAnimation(builder.playAndHold("animation.cream.charge_hit"));
                case 9 -> controller.setAnimation(builder.playAndHold("animation.cream.grab"));
                case 10 -> controller.setAnimation(builder.playAndHold("animation.cream.grab_hit"));
                case 11 -> controller.setAnimation(builder.playAndHold("animation.cream.enter"));
                case 12 -> controller.setAnimation(builder.playAndHold("animation.cream.exit"));
                case 13 -> controller.setAnimation(builder.playAndHold("animation.cream.destroy"));
            }
        }
        return PlayState.CONTINUE;
    }
}
