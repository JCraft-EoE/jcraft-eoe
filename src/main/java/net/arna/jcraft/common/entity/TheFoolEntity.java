package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.JEntityTypeRegister;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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

import java.util.ArrayList;
import java.util.List;

public class TheFoolEntity extends StandEntity implements IAnimatable, IAnimationTickable {
    AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

    public static Attack light = new Attack(0, 2, 1.5f, 14, 7, 2, 6f, 0.8f, AttackType.BOX, 0.75f, -0.1f, 0, JSoundRegister.IMPACT_2)
            .setInfo("Swipe", "slow, long-reaching poke");
    public static Attack airbarrage = new Attack(3, 17, 1f, 30, 0, 2, 1f, 0.1f, AttackType.BARRAGE, 0.5f, 0, 3);

    public static Attack combo = new Attack(2, 15, 1.5f, 31, 0, 1.75, 4.5f, 0.1f, AttackType.MULTIHIT, 1f, -0.1f, List.of(8, 16, 20, 21), JSoundRegister.IMPACT_2)
            .setInfo("3-hit combo (grounded) / Burn Rubber (aerial)", "knockdown on final hit / slows down all movement, combo starter/extender");
    public static Attack launch = new Attack(1, 16, 1.25f, 20, 16, 2, 8f, 0.5f, AttackType.BOX, 1.25f, -0.3f, 0, JSoundRegister.IMPACT_2)
            .appendHitbox(new Attack.HitboxData(1.5))
            .setHitspark(2)
            .setArmor(true)
            .setInfo("Launch", "uninterruptable, slow, launching uppercut");
    public static Attack slam = new Attack(10, 0, 1.25f, 10, 4, 2, 4f, 0.2f, AttackType.BOX, 1.2f, 0.1f, 0, JSoundRegister.IMPACT_2);
    public static Attack pound = new Attack(4, 18, 1.25f, 22, 7, 1.5, 4f, 0.1f, AttackType.BOX, 1.25f, -0.1f, 0, JSoundRegister.IMPACT_2)
            .setLift(false)
            .setFollowup(slam)
            .setInfo("Pound", """
                    has followups which create different sand patterns based on which key was pressed;
                    SPECIAL 1 - no sand
                    SPECIAL 2 - semicircle
                    SPECIAL 3 - diagonal pattern (influenced by where the user is looking)""", AttackQueue.SPECIAL1);
    public static Attack sandclone = new Attack(6, 30, 1, 11, 7, 0, 0f, 0.0f, AttackType.BOX)
            .setRanged(true)
            .setInfo("Sand Manipulation", "creates a blinding sand cloud, then a clone or (if crouching) circles of sand");
    public static Attack sandwave = new Attack(8, 27, 0f, 80, 0, 2, 1f, 0.1f, AttackType.BARRAGE, 0, 0, 3)
            .setMobility(MobilityType.DASH)
            .setRanged(true)
            .disableBackstab()
            .setInfo("Sandwave/Glider", "The Fool turns into a quick sandwave that knocks anything it touches down/in air turns into a glider");
    public static Attack glide = new Attack(9, 27, 0f, 85, 5, 0, 0, 0, AttackType.BOX)
            .setMobility(MobilityType.FLIGHT)
            .setInfo("Glider", "turns The Fool into a glider");
    public static Attack charge = new Attack(5, 22, 5f, 22, 5, 1.5, 6f, 1.2f, AttackType.CHARGE, 0.5f, 0, 11, JSoundRegister.IMPACT_2)
            .setRanged(true)
            .setLaunch()
            .setInfo("Charge", "The Fool detaches from the user and charges forward, dealing knockback on hit");
    public static Attack sandstorm = new Attack(7, 40, 1.5f, 41, 28, 2, 7f, 0.1f, AttackType.BOX, 1, 0, 0, JSoundRegister.TW_KICK_HIT)
            .appendHitbox(new Attack.HitboxData(1.5))
            .setHitspark(2)
            .setArmor(true)
            .setUB(true)
            .setInfo("Suffocating Sandstorm", "very slow, traps the opponent in a cloud of slowing sand");

    public static TrackedData<Boolean> ISSAND;
    public static TrackedData<Boolean> ISWAVE;

    private LivingEntity superTarget;
    private final ArrayList<FallingBlockEntity> sands = new ArrayList<>();

    private MobEntity sandClone;

    public TheFoolEntity(World worldIn) {
        super(StandType.THE_FOOL, worldIn);
        super.initialize();
        idleRotation = 225f;
        idleDistance = 2f;

        pros = List.of(
                "long reach",
                "easy setups",
                "excellent combo tools",
                "doesn't receive chip damage on block"
        );

        cons = List.of(
                "slowest light in the game",
                "extremely susceptible to rushdown",
                "immobile while blocking"
        );

        description = "Poke and Setup-based ZONER";

        freespace =
                """
                        BNBs:
                            M1>Pound>Launch>M1>Burn Rubber>Finisher*
                            Burn Rubber>M1>Pound>Launch>Finisher*
                            Launch>M1>Burn Rubber>M1>Pound>Finisher*

                            Stylish:
                            the social distancing
                            M1>Pound>M1>Combo>Charge>Sandwave
                            the pancake flip
                            Launch>Pound>M1>Burn Rubber>Finisher*

                            *Finisher: M1>Charge>(Sand Clone/Sandwave)""";

        moves = List.of(light, launch, combo, pound, sandstorm, charge, sandclone, sandwave);
    }

    public boolean isSand() {
        return this.dataTracker.get(ISSAND);
    }

    public void setSand(boolean b) {
        this.dataTracker.set(ISSAND, b);
    }

    public boolean isWave() {
        return this.dataTracker.get(ISWAVE);
    }

    public void setWave(boolean b) {
        this.dataTracker.set(ISWAVE, b);
    }

    static {
        ISSAND = DataTracker.registerData(TheFoolEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        ISWAVE = DataTracker.registerData(TheFoolEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        getDataTracker().startTracking(ISSAND, false);
        getDataTracker().startTracking(ISWAVE, false);
    }

    @Override
    public void standBlock() {
        LivingEntity user = getUser();
        if (user == null) return;

        // The Fool does a special block depending on your height
        boolean sand = user.getHeight() < 1.8f;
        this.setSand(sand);
        if (sand) this.setDistanceOffset(0);

        // Projectile deflection
        List<ProjectileEntity> toDeflect = world.getEntitiesByClass(ProjectileEntity.class, this.getBoundingBox().expand(0.75f), EntityPredicates.VALID_ENTITY);

        for (ProjectileEntity projectile : toDeflect) {
            if (projectile.getOwner() == user) continue;
            projectile.setVelocity(projectile.getVelocity().multiply(-0.5).add(0, -0.1, 0));
            projectile.velocityModified = true;
        }

        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 2, 9, false, false, true));
        stun(user, 2, 2);
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 5, 4, false, false, true));
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!canAttack()) return;
        handleAttack(light, JCraft.standLightCD, 2);
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (getUser().isOnGround()) {
            handleAttack(combo, JCraft.standBarrageCD, 4);
        } else {
            handleAttack(airbarrage, JCraft.standBarrageCD, 5);
        }
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;
        if (handleAttack(launch, JCraft.standHeavyCD, 6)) {
            this.setSand(true);
            playSound(JSoundRegister.FOOL_LAUNCH, 1, 1);
        }
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        if (handleAttack(sandstorm, JCraft.standUltCD, 12)) {
            playSound(JSoundRegister.FOOL_CHARGE, 1, 1);
        }
    }

    private int slamType = 0;
    private void initSlam(int type) {
        slamType = type;
        setAttack(slam, 14);
        playSound(JSoundRegister.FOOL_BARK1, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (curAttack != null && curAttack.id == pound.id && getMoveStun() < 12) initSlam(1);
        if (canAttack() && handleAttack(pound, JCraft.standS1CD, 7))
            playSound(JSoundRegister.FOOL_BARK2, 1, 1);
    }

    @Override
    public void initSpecial2() {
        if (curAttack != null && curAttack.id == pound.id && getMoveStun() < 12) initSlam(2);
        if (canAttack() && handleAttack(charge, JCraft.standS2CD, 8)) {
            playSound(JSoundRegister.FOOL_CHARGE, 1, 1);
        }
    }

    @Override
    public void initSpecial3() {
        if (curAttack != null && curAttack.id == pound.id && getMoveStun() < 12) initSlam(3);
        if (canAttack() && handleAttack(sandclone, JCraft.standS3CD, 9)) {
            setSand(true);
            playSound(SoundEvents.BLOCK_SAND_PLACE, 1, 1);
        }
    }

    @Override
    public void initMiddleClick() {
        if (!canAttack()) return;
        LivingEntity user = getUser();
        if (user.isOnGround() && handleAttack(sandwave, JCraft.utilCD, 10)) {
            this.setSand(true);
            this.setWave(true);
            this.setFree(false);

            playSound(JSoundRegister.FOOL_BARK1, 1, 1);
        } else if (handleAttack(glide, JCraft.utilCD, 13)) {
            this.setSand(true);
            this.setFree(false);

            playSound(JSoundRegister.FOOL_BARK2, 1, 1);
        }
    }

    @Override
    public boolean shouldOffsetHeight() {
        if (getState() == 13 || getState() == 10 || getState() == 3) return false;
        return super.shouldOffsetHeight();
    }

    @Override
    public boolean canAttack() {
        LivingEntity user = getUser();
        if (hasUser()) {
            ITimeStop timeStop = (ITimeStop) user;
            if (timeStop.getTimeStopTicks() > 0 || user.hasStatusEffect(JStatusRegister.DAZED)) return false;
            if (curAttack != null && curAttack.id == glide.id) return true;
            return getMoveStun() < 1;
        }
        return false;
    }

    @Override
    public void setAttack(Attack attack, int state) {
        if (getUser().isSneaking()) {
            setSand(true);
            super.setAttack(
                    Attack.copyOf(attack).setDist(attack.attackDist / 2f)
                    , state);
        } else {
            super.setAttack(attack, state);
        }
    }

    @Override
    public void desummon() {
        // Remove everything that The Fool summoned before removing the stand itself
        if (sandClone != null) sandClone.kill();
        for (FallingBlockEntity sand : sands) sand.discard();
        super.desummon();
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        LivingEntity user = this.getUser();
        switch (attack.id) {
            case (1) -> {
                for (LivingEntity ent : entities)
                    ent.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 5, 19, true, false));
            }
            case (2) -> {
                if (this.getMoveStun() < 11)
                    for (LivingEntity ent : entities)
                        ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 20, 0));
            }
            case (4) -> {
                for (LivingEntity ent : entities) {
                    Vec3d vel = ent.getVelocity();
                    ent.setVelocity(vel.x, (this.getMoveStun() > 14) ? 0.5 : -1, vel.y);
                    ent.velocityModified = true;
                }
            }
            case (6) -> {
                // Display sand effect
                PacketByteBuf buf = PacketByteBufs.create();

                Vec3d pos = user.getEyePos();

                buf.writeShort(11);

                buf.writeDouble(pos.x);
                buf.writeDouble(pos.y);
                buf.writeDouble(pos.z);

                for (ServerPlayerEntity sendPlayer : PlayerLookup.world((ServerWorld) world)) {
                    ServerChannelFeedbackPacket.send(sendPlayer, buf);
                    if (sendPlayer == user) continue;
                    if (sendPlayer.isInRange(user, 4)) // Blind players caught in the cloud
                        sendPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 40, 0, true, false));
                }

                if (user.isSneaking()) {
                    for (int i = 0; i < 32; i++) {
                        double y = 0.4;
                        double h = i * 3.1415 / 8;
                        double hDiv = 5;
                        if (i >= 16) {
                            y = 0.8;
                            hDiv = 10;
                        }
                        createFoolishSand(new Vec3d(Math.sin(h) / hDiv, y, Math.cos(h) / hDiv));
                    }
                } else {
                    // Summon clone
                    if (user instanceof PlayerEntity playerEntity) {
                        PlayerCloneEntity playerCloneEntity = new PlayerCloneEntity(JEntityTypeRegister.PLAYER_ENTITY_CLONE, world);
                        playerCloneEntity.copyPositionAndRotation(playerEntity);
                        playerCloneEntity.setOwner(playerEntity);
                        playerCloneEntity.markSand();

                        setSandClone(playerCloneEntity);
                    } else if (user instanceof MobEntity mob) {
                        EntityType<?> entityType = mob.getType();
                        MobEntity newMob = (MobEntity) entityType.create(world);
                        newMob.copyPositionAndRotation(this);
                        newMob.setBaby(mob.isBaby());

                        if (mob.hasCustomName()) {
                            newMob.setCustomName(mob.getCustomName());
                            newMob.setCustomNameVisible(mob.isCustomNameVisible());
                        }

                        newMob.age = mob.age;
                        ((IEntityDataSaver) newMob).getPersistentData().putInt("StandID", 0);

                        setSandClone(newMob);
                    }

                    world.spawnEntity(sandClone);
                }
            }
            case (7) -> {
                if (!entities.isEmpty()) {
                    this.superTarget = JCraftUtils.getUserIfStand(entities.get(0));

                    for (int i = 0; i < 8; i++) {
                        FallingBlockEntity sand = FallingBlockEntity.spawnFromBlock(this.world, superTarget.getBlockPos(), Blocks.SAND.getDefaultState());
                        sand.timeFalling = -32767;
                        sand.noClip = true;
                        sand.dropItem = false;
                        sand.setBoundingBox(new Box(0, 0, 0, 0, 0, 0));
                        sand.setNoGravity(true);
                        sands.add(sand);
                    }
                }
            }
            case (8) -> {
                for (LivingEntity ent : entities)
                    ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 15, 0));
            }
            case (9) -> setSand(false); // Ends transformation state
            case (10) -> {
                switch (slamType) {
                    case (2) -> {
                        Vec3d leftVec = user.getRotationVector().rotateY(1.75f);
                        for (int i = 0; i < 8; i++) {
                            leftVec = leftVec.rotateY(-3.141592f / 8).normalize();
                            createFoolishSand( new Vec3d(leftVec.x / 4, 0.25, leftVec.z / 4) );
                        }
                    }
                    case (3) -> {
                        Vec3d rotVec = user.getRotationVector();
                        for (double i = 0; i < 8; i++)
                            for (double j = 0; j < i; j++) {
                                double hDiv = 5.0 * (1 + j/i);
                                createFoolishSand(new Vec3d(rotVec.x * Math.sqrt(i) / hDiv, j / 5.0, rotVec.z * Math.sqrt(i) / hDiv));
                            }
                    }
                    default -> { }
                }
            }
        }
    }

    private void createFoolishSand(Vec3d vel) {
        FallingBlockEntity sand = FallingBlockEntity.spawnFromBlock(world, getBlockPos().add(0, 1, 0), JObjectRegistry.FOOLISH_SAND_BLOCK.getDefaultState());
        sand.dropItem = false;
        sand.setVelocity(vel);
        sand.velocityModified = true;
        sand.velocityDirty = true;
        world.spawnEntity(sand);
    }
    private void setSandClone(MobEntity clone) {
        //JCraft.LOGGER.info("Setting sand clone to: " + clone + " from " + sandClone);
        if (sandClone != null) sandClone.kill();
        this.sandClone = clone;
        if (clone == null) return;
        applySandCloneModifiers(clone);
    }
    public static void applySandCloneModifiers(LivingEntity entity)
    {
        if (entity == null) {
            JCraft.LOGGER.error("Tried to apply sand clone attribute modifiers to invalid entity!");
            return;
        }
        //JCraft.LOGGER.info("Applying sand clone modifiers to: " + entity);
        entity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).addPersistentModifier(
                new EntityAttributeModifier("Sand Clone Max Health Modifier", -1.0, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
        );
    }
    @Override
    public void tick() {
        if (age == 1) this.world.playSound(null, this.getX(), this.getY(), this.getZ(), JSoundRegister.STAND_SUMMON, SoundCategory.PLAYERS, 1f, 1f);

        super.tick();

        boolean client = this.world.isClient();
        if (hasUser()) {
            LivingEntity user = this.getUser();
            if (client) {
                if (this.age % 2 == 0) {
                    Vec3d pos = this.getPos();
                    // If the fool is using any morphing attack, the amount of sand multiplies and it changes color
                    int particleNum = this.isWave() ? 32 : 1 + MathHelper.clamp(this.getMoveStun() / 2, 0, 5) * (this.isSand() ? 2 : 1);
                    int height = this.isWave() || this.isBlocking() ? 1 : 2;

                    for (int i = 0; i < particleNum; i++) {
                        ParticleEffect effect = new BlockStateParticleEffect(ParticleTypes.FALLING_DUST, Blocks.SAND.getDefaultState());
                        if (isWave() && random.nextFloat() * 0.5f > 0)
                            effect = new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.SAND.getDefaultState());
                        world.addParticle(
                                effect,
                                pos.x + random.nextTriangular(0, 1),
                                pos.y + random.nextTriangular(height / 2f, height / 2f),
                                pos.z + random.nextTriangular(0, 1),
                                0, 0, 0);
                    }
                }
            } else {
                Attack attack = this.curAttack;
                if (lastRemoteInputTime - age > 4) updateRemoteInputs(0, 0, false);
                if (attack != null) {
                    if (attack.id == pound.id) queuedAttack = null;
                    switch (attack.id) {
                        case (3) -> {
                            user.setVelocity(user.getVelocity().multiply(0.5).add(0, 0.01, 0));
                            user.velocityModified = true;
                        }
                        case (8) -> {
                            if (user.isOnGround()) {
                                Vec3d rotVec = user.getRotationVector().multiply(0.25);
                                user.addVelocity(rotVec.x, 0, rotVec.z);
                                user.velocityModified = true;
                            }
                        }
                        case (9) -> {
                            user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 4, 4, true, false));
                            double yVel = getRemoteJumpInput() ? 0.07 : 0;
                            Vec3d rotVec = user.getRotationVector().multiply(0.04);
                            user.addVelocity(rotVec.x, yVel, rotVec.z);
                            user.velocityModified = true;
                        }
                    }
                    /*
                    else {
                        for (int i = 0; i < sands.size(); i++) { sands.get(i).discard(); } // Not doing super, discard all sand entities
                    }
                     */
                } else if (!this.blocking) { // If idle, reset back to normal material
                    this.setSand(false);
                    this.setWave(false);
                }

                // Suffocating Sandstorm logic
                if (sands.isEmpty()) {
                    superTarget = null;
                } else {
                    superTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 10, 0, true, false));

                    // Every second, a sand block from the grab disappears
                    if (this.age % 20 == 0) {
                        sands.get(0).discard();
                        sands.remove(0);
                    }

                    Vec3d targetPos = superTarget.getPos().add(0, superTarget.getHeight() / 2, 0);
                    for (FallingBlockEntity sand : sands) {
                        Vec3d newVel = sand.getVelocity().multiply(0.25).add( // Suppress current velocity
                                // And add tracking
                                targetPos.subtract(
                                        // MathHelper.sin(t) * 2, (isEven ? MathHelper.sin(t) : MathHelper.cos(t)) * 2, MathHelper.cos(t) * 2
                                        sand.getPos().add(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5)
                                ).normalize().multiply(0.5)
                        );

                        sand.setVelocity(newVel);
                        sand.velocityModified = true;
                    }
                }

                // Sand clone logic
                if (sandClone != null) {
                    if (sandClone.age > 200)
                        setSandClone(null);
                    if (this.sandClone instanceof PlayerCloneEntity playerClone && playerClone.switched) { // Detect if clone switched to thin
                        playerClone.switchedTo.markSand();
                        setSandClone(playerClone.switchedTo);
                    }
                }
            }

            setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(user) / 2, 0.0, 255.0) / 255f);
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
        int state = this.getState();
        String blockAnim = this.isSand() ? "animation.thefool.crouchblock" : "animation.thefool.block";

        AnimationController controller = event.getController();
        AnimationBuilder builder = new AnimationBuilder();
        if (this.getSameState()) {
            controller.markNeedsReload();
        }
        switch (state) {
            default -> controller.setAnimation(builder.loop("animation.thefool.idle"));
            case 2 -> controller.setAnimation(builder.playAndHold("animation.thefool.swipe"));
            case 3 -> controller.setAnimation(builder.loop(blockAnim));
            case 4 -> controller.setAnimation(builder.playAndHold("animation.thefool.combo"));
            case 5 -> controller.setAnimation(builder.playAndHold("animation.thefool.airbarrage"));
            case 6 -> controller.setAnimation(builder.playAndHold("animation.thefool.launch"));
            case 7 -> controller.setAnimation(builder.playAndHold("animation.thefool.poundup"));
            case 8 -> controller.setAnimation(builder.loop("animation.thefool.charge"));
            case 9 -> controller.setAnimation(builder.playAndHold("animation.thefool.create"));
            case 10 -> controller.setAnimation(builder.loop("animation.thefool.sandwave"));
            case 11 -> controller.setAnimation(builder.playAndHold("animation.thefool.charge_hit"));
            case 12 -> controller.setAnimation(builder.playAndHold("animation.thefool.sandstorm"));
            case 13 -> controller.setAnimation(builder.loop("animation.thefool.glide"));
            case 14 -> controller.setAnimation(builder.playAndHold("animation.thefool.pounddown"));
        }
        return PlayState.CONTINUE;
    }
}
