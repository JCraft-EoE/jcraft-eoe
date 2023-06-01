package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.Attack;
import net.arna.jcraft.common.util.AttackType;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.registry.JEntityTypeRegister;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
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
    public static Attack airbarrage = new Attack(3, 20, 1f, 30, 0, 2, 1f, 0.1f, AttackType.BARRAGE, 0.5f, 0, 3);

    public static Attack combo = new Attack(2, 17, 1.5f, 31, 0, 1.75, 4.5f, 0.1f, AttackType.MULTIHIT, 1f, -0.1f, List.of(8, 16, 20, 21), JSoundRegister.IMPACT_2)
            .setInfo("3-hit combo (grounded) / Burn Rubber (aerial)", "knockdown on final hit / slows down all movement, combo starter/extender");
    public static Attack launch = new Attack(1, 16, 1.25f, 20, 16, 2, 8f, 0.5f, AttackType.BOX, 1.25f, -0.3f, 0, JSoundRegister.IMPACT_2).setHitspark(2).setArmor(true)
            .setInfo("Launch", "uninterruptable, slow, launching uppercut");
    public static Attack pound = new Attack(4, 24, 1.25f, 23, 0, 1.5, 4f, 0.1f, AttackType.MULTIHIT, 1.25f, -0.1f, List.of(7, 15), JSoundRegister.IMPACT_2).setLift(false)
            .setInfo("Pound", "two-hitter, sends opponent up on first hit, and down on the second");
    public static Attack sandclone = new Attack(6, 30, 1, 11, 7, 0, 0f, 0.0f, AttackType.BOX).setRanged(true)
            .setInfo("Sand Clone", "in a blinding cloud, summons a slow sand clone which attacks alongside you");
    public static Attack sandwave = new Attack(8, 27, 0f, 80, 0, 2, 1f, 0.1f, AttackType.BARRAGE, 0, 0, 3).setRanged(true)
            .setInfo("Sandwave - for 4s", "turn into a quick sandwave that knocks anything it touches down");
    public static Attack charge = new Attack(5, 22, 5f, 22, 5, 1.5, 6f, 1.2f, AttackType.CHARGE, 0.5f, 0, 11, JSoundRegister.IMPACT_2).setRanged(true).setLaunch()
            .setInfo("Charge", "The Fool detaches from the user and charges forward, dealing knockback on hit");
    public static Attack sandstorm = new Attack(7, 50, 1.5f, 41, 28, 2, 7f, 0.1f, AttackType.BOX, 1, 0, 0, JSoundRegister.TW_KICK_HIT).setHitspark(2)
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
                "BNBs:\n" +
                        "    M1>Pound>Launch>M1>Burn Rubber>Finisher*\n" +
                        "    Burn Rubber>M1>Pound>Launch>Finisher*\n" +
                        "    Launch>M1>Burn Rubber>M1>Pound>Finisher*\n" +
                        "\n" +
                        "    Stylish:\n" +
                        "    the social distancing\n" +
                        "    M1>Pound>M1>Combo>Charge>Sandwave\n" +
                        "    the pancake flip\n" +
                        "    Launch>Pound>M1>Burn Rubber>Finisher*\n" +
                        "\n" +
                        "    *Finisher: M1>Charge>(Sand Clone/Sandwave)";

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
        this.getDataTracker().startTracking(ISSAND, false);
        this.getDataTracker().startTracking(ISWAVE, false);
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
        List<ProjectileEntity> toDeflect = this.world.getEntitiesByClass(ProjectileEntity.class, this.getBoundingBox().expand(0.75f), EntityPredicates.VALID_ENTITY);

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
        if (!this.canAttack()) {
            return;
        }
        handleAttack(light, JCraft.standLightCD, 2);
    }

    @Override
    public void initBarrage() {
        if (!this.canAttack()) {
            return;
        }
        if (this.getUser().isOnGround()) {
            handleAttack(combo, JCraft.standBarrageCD, 4);
        } else {
            handleAttack(airbarrage, JCraft.standBarrageCD, 5);
        }
    }

    @Override
    public void initHeavyAttack() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(launch, JCraft.standHeavyCD, 6)) {
            this.setSand(true);
            this.playSound(JSoundRegister.FOOL_LAUNCH, 1, 1);
        }
    }

    @Override
    public void initSpecial1() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(pound, JCraft.standS1CD, 7)) {
            this.playSound(JSoundRegister.FOOL_BARK2, 1, 1);
        }
    }

    @Override
    public void initUlt() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(sandstorm, JCraft.standUltCD, 12)) {
            this.playSound(JSoundRegister.FOOL_CHARGE, 1, 1);
        }
    }

    @Override
    public void initSpecial2() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(charge, JCraft.standS2CD, 8)) {
            this.playSound(JSoundRegister.FOOL_CHARGE, 1, 1);
        }
    }

    @Override
    public void initSpecial3() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(sandclone, JCraft.standS3CD, 9)) {
            this.setSand(true);
            this.playSound(SoundEvents.BLOCK_SAND_PLACE, 1, 1);
        }
    }

    @Override
    public void initMiddleClick() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(sandwave, JCraft.standMMBCD, 10)) {
            this.setSand(true);
            this.setWave(true);
            this.setFree(false);

            this.playSound(JSoundRegister.FOOL_BARK1, 1, 1);
        }
    }

    @Override
    public void desummon() {
        // Remove everything that The Fool summoned before removing the stand itself
        if (this.sandClone != null) {
            this.sandClone.kill();
        }
        for (FallingBlockEntity sand : sands) {
            sand.discard();
        }

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

                for (PlayerEntity sendPlayer : world.getPlayers()) {
                    if (sendPlayer instanceof ServerPlayerEntity serverPlayerEntity)
                        ServerChannelFeedbackPacket.send(serverPlayerEntity, buf);
                    if (sendPlayer == user)
                        continue;

                    // Blind players caught in the cloud
                    if (sendPlayer.isInRange(user, 4)) {
                        sendPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 40, 0, true, false));
                    }
                }

                // Summon clone
                if (user instanceof PlayerEntity playerEntity) {
                    PlayerCloneEntity playerCloneEntity = new PlayerCloneEntity(JEntityTypeRegister.PLAYER_ENTITY_CLONE, this.world);
                    playerCloneEntity.copyPositionAndRotation(playerEntity);
                    playerCloneEntity.setOwner(playerEntity);
                    playerCloneEntity.sandClone = true;
                    this.world.spawnEntity(playerCloneEntity);

                    this.sandClone = playerCloneEntity;
                } else if (user instanceof MobEntity mob) {
                    //Code sourced from MobEntity.class convertTo()
                    EntityType<?> entityType = mob.getType();
                    MobEntity newMob = (MobEntity) entityType.create(this.world);

                    newMob.copyPositionAndRotation(this);
                    newMob.setBaby(mob.isBaby());

                    if (mob.hasCustomName()) {
                        newMob.setCustomName(mob.getCustomName());
                        newMob.setCustomNameVisible(mob.isCustomNameVisible());
                    }

                    this.world.spawnEntity(newMob);
                    newMob.age = mob.age;
                    IEntityDataSaver newMobData = (IEntityDataSaver) newMob;
                    newMobData.getPersistentData().putInt("StandID", 0);

                    this.sandClone = newMob;
                }

                this.sandClone.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 2, true, false));
            }
            case (7) -> {
                if (!entities.isEmpty()) {
                    this.superTarget = entities.get(0);

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
        }
    }

    @Override
    public void tick() {
        if (age == 1) this.world.playSound(null, this.getX(), this.getY(), this.getZ(), JSoundRegister.STAND_SUMMON, SoundCategory.PLAYERS, 1f, 1f);

        super.tick();

        boolean client = this.world.isClient();
        if (hasUser()) {
            LivingEntity user = this.getUser();
            if (client) {
                if (this.age % 4 == 0) {
                    Vec3d pos = this.getPos();
                    // If the fool is using any morphing attack, the amount of sand multiplies and it changes color
                    int particleNum = this.isWave() ? 64 : 1 + MathHelper.clamp(this.getMoveStun() / 2, 0, 10) * (this.isSand() ? 3 : 1);
                    int height = this.isWave() || this.isBlocking() ? 1 : 2;

                    for (int i = 0; i < particleNum; i++) {
                        ParticleEffect effect = new BlockStateParticleEffect(ParticleTypes.FALLING_DUST, Blocks.SAND.getDefaultState());
                        if (this.isWave() && random.nextFloat() * 0.5f > 0) {
                            effect = new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.SAND.getDefaultState());
                        }
                        this.world.addParticle(
                                effect,
                                pos.x + random.nextTriangular(0, 1),
                                pos.y + random.nextTriangular(height / 2f, height / 2f),
                                pos.z + random.nextTriangular(0, 1),
                                0, 0, 0);
                    }
                }
            } else {
                Attack attack = this.curAttack;
                if (attack != null) {
                    // The air barrage slows you down
                    if (attack == airbarrage) {
                        user.setVelocity(user.getVelocity().multiply(0.5).add(0, 0.01, 0));
                        user.velocityModified = true;
                    } else if (attack == sandwave && user.isOnGround()) {
                        Vec3d rotVec = user.getRotationVector().multiply(0.5);
                        user.addVelocity(rotVec.x, 0, rotVec.z);
                        user.velocityModified = true;
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
                if (this.sandClone != null) {
                    // Detect if clone switched to thin
                    if (this.sandClone instanceof PlayerCloneEntity playerClone) {
                        if (playerClone.switched) {
                            //JCraft.LOGGER.info("Switch detected.");
                            this.sandClone = playerClone.switchedTo;
                        }
                    }

                    //JCraft.LOGGER.info(this.sandClone.getHealth() + " " + this.sandClone.age);

                    // Pops after a single hit, or after 10s
                    if (this.sandClone.getHealth() < 20 || this.sandClone.age > 200) {
                        this.sandClone.kill();
                        this.sandClone = null;
                    }
                }
            }

            this.setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(user) / 2, 0.0, 255.0) / 255f);
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
            case 7 -> controller.setAnimation(builder.playAndHold("animation.thefool.pound"));
            case 8 -> controller.setAnimation(builder.loop("animation.thefool.charge"));
            case 9 -> controller.setAnimation(builder.playAndHold("animation.thefool.create"));
            case 10 -> controller.setAnimation(builder.loop("animation.thefool.sandwave"));
            case 11 -> controller.setAnimation(builder.playAndHold("animation.thefool.charge_hit"));
            case 12 -> controller.setAnimation(builder.playAndHold("animation.thefool.sandstorm"));
        }
        return PlayState.CONTINUE;
    }
}
