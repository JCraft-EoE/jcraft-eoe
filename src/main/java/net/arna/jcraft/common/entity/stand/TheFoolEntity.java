package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.HitBoxData;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.old.Attack;
import net.arna.jcraft.common.attack.core.old.AttackType;
import net.arna.jcraft.common.attack.core.old.MoveQueue;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.shared.BarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleMultiHitAttack;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.arna.jcraft.common.entity.projectile.SandTornadoEntity;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TheFoolEntity extends StandEntity<TheFoolEntity, TheFoolEntity.State> {
    public static final SimpleMultiHitAttack<TheFoolEntity> DRILL = new SimpleMultiHitAttack<TheFoolEntity>(30, 14, 2.5f, 5, 1.5f, 0.2f, 1.5f, 0.25f, IntSet.of(5, 8, 11))
            .withBlockStun(4)
            .withInfo(Text.literal("Drill"), Text.literal("fast, multi-hitting combo starter, low stun and blockstun"));
    public static final SimpleAttack<TheFoolEntity> LIGHT = new SimpleAttack<TheFoolEntity>( 30, 7, 14, 6, 15, 2, 0.8f, 1.5f, -0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withExtraHitBox(0, 0.25, 1)
            .withCrouchingVariant(DRILL)
            .withInfo(Text.literal("Swipe"), Text.literal("slow, long-reaching poke"));
    //todo: air barrage slows down movement
    public static final BarrageAttack<TheFoolEntity> AIR_BARRAGE = new BarrageAttack<TheFoolEntity>(340, 0, 30, 1, 1, 10, 2, 0.1f, 0, 3)
            .withInfo(Text.literal("Burn Rubber"), Text.literal("slows down all movement, combo starter/extender"));
    public static final Attack combo = new Attack(2, 15, 1.5f, 29, 0, 1.75, 4.5f, 0.1f, AttackType.MULTIHIT, 1f, -0.1f, List.of(6, 14, 18, 19), JSoundRegistry.IMPACT_2)
            .appendHitbox(new HitBoxData(0.5, 0, 1.25))
            .aerialVariation(airbarrage)
            .setInfo("3-hit Combo", "fast knockdown provider");
    public static final Attack launch = new Attack(1, 16, 1.25f, 20, 16, 2, 8f, 0.5f, AttackType.BOX, 1.25f, -0.3f, 0, JSoundRegistry.IMPACT_2)
            .appendHitbox(new HitBoxData(1.5))
            .setHitspark(2)
            .hyperArmor()
            .setInfo("Launch", "uninterruptable, slow, launching uppercut");
    public static final Attack slam = new Attack(10, 0, 1.25f, 10, 4, 2, 4f, 0.2f, AttackType.BOX, 1.2f, 0.1f, 0, JSoundRegistry.IMPACT_2);
    public static final Attack pound = new Attack(4, 13, 1.25f, 22, 7, 1.5, 4f, 0.1f, AttackType.BOX, 1.25f, -0.1f, 0, JSoundRegistry.IMPACT_2)
            .setLift(false)
            .setFollowup(slam)
            .setInfo("Pound", """
                    has followups which create different sand patterns based on which key was pressed;
                    SPECIAL 1 - no sand
                    SPECIAL 2 - semicircle
                    SPECIAL 3 - diagonal pattern (influenced by where the user is looking)""", MoveQueue.SPECIAL1);
    public static final Attack sandclone = new Attack(6, 30, 1, 11, 7, 0, 0f, 0.0f, AttackType.BOX)
            .setRanged(true)
            .setInfo("Sand Manipulation", "creates a blinding sand cloud, then a clone or (if crouching) circles of sand");
    public static final Attack glide = new Attack(9, 27, 0f, 85, 5, 0, 0, 0, AttackType.BOX)
            .setMobility(MobilityType.FLIGHT)
            .setInfo("Glider", "turns The Fool into a glider");
    public static final Attack sandwave = Attack.barrageAttack(8, 27, 0f, 80, 0, 2, 1f, 0.1f, 0, 0, 3)
            .setMobility(MobilityType.DASH)
            .setRanged(true)
            .disableBackstab()
            .aerialVariation(glide)
            .setInfo("Sandwave", "The Fool turns into a quick sandwave that knocks anything it touches down");
    //todo: sand tornado tracking (projectile-only code)
    public static final Attack tornado = new Attack(11, 25, 1, 13, 12, 0, 0f, 0.0f, AttackType.BOX)
            .setRanged(true)
            .setInfo("Sand Tornado", "summons a slow, stunning sand tornado");
    public static final Attack charge = new Attack(5, 20, 7f, 20, 5, 1.5, 6f, 1.2f, AttackType.CHARGE, 0.5f, 0, State.CHARGE_HIT.ordinal(), JSoundRegistry.IMPACT_2)
            .setRanged(true)
            .setLaunch()
            .disableBackstab()
            .aerialVariation(tornado)
            .setInfo("Charge", "The Fool detaches from the user and charges forward, dealing knockback on hit");
    public static final Attack sandstorm = new Attack(7, 40, 1.5f, 41, 28, 2, 7f, 0.1f, AttackType.BOX, 1, 0, 0, JSoundRegistry.TW_KICK_HIT)
            .appendHitbox(new HitBoxData(1.5))
            .setHitspark(2)
            .hyperArmor()
            .setUB(true)
            .setInfo("Suffocating Sandstorm", "very slow, traps the opponent in a cloud of slowing sand");

    private static final TrackedData<Boolean> ISSAND;
    private static final TrackedData<Boolean> ISWAVE;

    private LivingEntity superTarget;
    private final ArrayList<FallingBlockEntity> sands = new ArrayList<>();

    private MobEntity sandClone;
    private int slamType = 0;

    public TheFoolEntity(World worldIn) {
        super(StandType.THE_FOOL, worldIn);
        idleRotation = 225f;
        idleDistance = 2f;

        pros = List.of(
                "long reach",
                "easy, accessible space control using crouching and multiple armored options",
                "easy setups",
                "good combo tools",
                "doesn't receive chip damage on block"
        );

        cons = List.of(
                "overall slow",
                "extremely susceptible to rushdown",
                "immobile while blocking"
        );

        description = "Poke and Setup-based ZONER";

        freespace =
                """
                        CROUCHING reduces attack distance by half, allowing better space control
                        
                        BNBs:
                            M1>Pound~Slam>Launch>M1>Burn Rubber>Finisher*
                            Burn Rubber>M1>Pound~Slam>Launch>Finisher*
                            Launch>M1>Burn Rubber>M1>Pound~Slam>Finisher*

                            Stylish:
                            the social distancing
                            M1>Pound~Slam>M1>Combo>Charge>Sandwave
                            the pancake flip
                            Launch>Pound~Slam>M1>Burn Rubber>Finisher*

                            *Finisher: M1>...
                                       Charge/Tornado>...
                                       Sand Clone/Sandwave""";

        moves = List.of(light, launch, combo, pound, sandstorm, charge, sandclone, sandwave);

        super.initialize();
    }

    @Override
    protected void registerMoves(MoveMap<TheFoolEntity, State> moves) {
        moves.register(MoveType.LIGHT, LIGHT, State.SWIPE).withCrouchingVariant(State.DRILL);
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
        setSand(sand);
        if (sand) this.setDistanceOffset(0);

        // Projectile deflection
        List<ProjectileEntity> toDeflect = world.getEntitiesByClass(ProjectileEntity.class, getBoundingBox().expand(0.75f), EntityPredicates.VALID_ENTITY);

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
        handleMove(light, CooldownType.STAND_LIGHT, State.SWIPE);
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (getUser() != null && getUser().isOnGround())
            handleMove(combo, CooldownType.STAND_BARRAGE, State.COMBO);
        else handleMove(airbarrage, CooldownType.STAND_BARRAGE, State.AIR_BARRAGE);
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;
        if (handleMove(launch, CooldownType.STAND_HEAVY, State.LAUNCH)) {
            setSand(true);
            playSound(JSoundRegistry.FOOL_LAUNCH, 1, 1);
        }
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        if (handleMove(sandstorm, CooldownType.STAND_ULTIMATE, State.SANDSTORM))
            playSound(JSoundRegistry.FOOL_ULT, 1, 1);
    }

    private void initSlam(int type) {
        slamType = type;
        setAttack(slam, State.POUND_DOWN);
        playSound(JSoundRegistry.FOOL_BARK1, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (curMove != null && curMove.id == pound.id && getMoveStun() < 12) initSlam(1);
        if (canAttack() && handleMove(pound, CooldownType.STAND_SP1, State.POUND_UP))
            playSound(JSoundRegistry.FOOL_BARK2, 1, 1);
    }

    @Override
    public void initSpecial2() {
        if (curMove != null && curMove.id == pound.id && getMoveStun() < 12) initSlam(2);

        if (!canAttack()) return;

        if (getUser() != null && getUser().isOnGround() && handleMove(charge, CooldownType.STAND_SP2, State.CHARGE))
            playSound(JSoundRegistry.FOOL_CHARGE, 1, 1);
        else if (handleMove(tornado, CooldownType.STAND_SP2, State.TORNADO)) {
            setSand(true);
            playSound(JSoundRegistry.FOOL_LAUNCH, 1, 1);
        }
    }

    @Override
    public void initSpecial3() {
        if (curMove != null && curMove.id == pound.id && getMoveStun() < 12) initSlam(3);
        if (canAttack() && handleMove(sandclone, CooldownType.STAND_SP3, State.CREATE)) {
            setSand(true);
            playSound(SoundEvents.BLOCK_SAND_PLACE, 1, 1);
        }
    }

    @Override
    public void initUtil() {
        if (!canAttack()) return;
        LivingEntity user = getUser();
        if (user != null && user.isOnGround() && handleMove(sandwave, CooldownType.UTILITY, State.SAND_WAVE)) {
            setSand(true);
            setWave(true);
            setFree(false);

            playSound(JSoundRegistry.FOOL_BARK1, 1, 1);
        } else if (handleMove(glide, CooldownType.UTILITY, State.GLIDE)) {
            setSand(true);
            setFree(false);

            playSound(JSoundRegistry.FOOL_GLIDE, 1, 1);
        }
    }

    @Override
    public boolean shouldOffsetHeight() {
        if (getState() == State.GLIDE || getState() == State.SAND_WAVE || getState() == State.BLOCK) return false;
        return super.shouldOffsetHeight();
    }

    @Override
    public boolean canAttack() {
        if (hasUser()) {
            LivingEntity user = getUserOrThrow();
            if (JUtils.isAffectedByTimeStop(user) || user.hasStatusEffect(JStatusRegistry.DAZED)) return false;
            if (curMove != null && curMove.id == glide.id) return true;
            return getMoveStun() < 1;
        }
        return false;
    }

    @Override
    public boolean setMove(AbstractMove<?, ? super TheFoolEntity> move, @Nullable State animState) {
        if (getUser() != null && getUser().isSneaking()) {
            setSand(true);
            return super.setMove(move.copy().withMoveDistance(move.getMoveDistance() / 2f), animState);
        } else return super.setMove(move, animState);
    }

    @Override
    public void desummon() {
        // Remove everything that The Fool summoned before removing the stand itself
        if (sandClone != null) sandClone.kill();
        for (FallingBlockEntity sand : sands) sand.discard();
        super.desummon();
    }

    @Override
    public void specialAttack(Attack attack, Set<LivingEntity> entities) {
        LivingEntity user = this.getUser();

        switch (attack.id) {
            case (1) -> {
                for (LivingEntity ent : entities)
                    ent.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 5, 19, true, false));
            }
            case (2) -> {
                if (this.getMoveStun() < 11)
                    for (LivingEntity ent : entities)
                        ent.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 20, 0));
            }
            case (4) -> {
                for (LivingEntity ent : entities) {
                    Vec3d vel = ent.getVelocity();
                    ent.setVelocity(vel.x, (this.getMoveStun() > 14) ? 0.5 : -1, vel.y);
                    ent.velocityModified = true;
                }
            }
            case (5) -> {
                if (entities.isEmpty()) return;

                setSand(true);
                Vec3d pos = getEyePos();

                // Display sand effect
                PacketByteBuf buf = PacketByteBufs.create();

                buf.writeShort(11);
                buf.writeDouble(pos.x);
                buf.writeDouble(pos.y);
                buf.writeDouble(pos.z);
                buf.writeDouble(0.5);

                for (ServerPlayerEntity sendPlayer : PlayerLookup.around((ServerWorld) world, pos, 96))
                    ServerChannelFeedbackPacket.send(sendPlayer, buf);
            }
            case (6) -> {
                if (user == null) return;

                Vec3d pos = user.getEyePos();

                // Display sand effect
                PacketByteBuf buf = PacketByteBufs.create();

                buf.writeShort(11);
                buf.writeDouble(pos.x);
                buf.writeDouble(pos.y);
                buf.writeDouble(pos.z);
                buf.writeDouble(2);

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
                            hDiv = 9.5;
                        }
                        createFoolishSand(new Vec3d(Math.sin(h) / hDiv, y, Math.cos(h) / hDiv));
                    }
                } else {
                    //todo: sand clone - copies player armor, aggros STUPID PEOPLE

                    // Summon clone
                    if (user instanceof ServerPlayerEntity player) {
                        PlayerCloneEntity playerCloneEntity = new PlayerCloneEntity(world);
                        playerCloneEntity.copyPositionAndRotation(player);
                        playerCloneEntity.setMaster(player);
                        playerCloneEntity.markSand();

                        setSandClone(playerCloneEntity);
                    } else if (user instanceof MobEntity mob) {
                        EntityType<?> entityType = mob.getType();
                        MobEntity newMob = (MobEntity) entityType.create(world);

                        if (newMob == null) {
                            JCraft.LOGGER.error("Failed to create sand clone of " + mob + " in world " + world);
                            return;
                        }

                        newMob.copyPositionAndRotation(this);
                        newMob.setBaby(mob.isBaby());

                        if (mob.hasCustomName()) {
                            newMob.setCustomName(mob.getCustomName());
                            newMob.setCustomNameVisible(mob.isCustomNameVisible());
                        }

                        newMob.age = mob.age;

                        setSandClone(newMob);
                    }

                    world.spawnEntity(sandClone);
                }
            }
            case (7) -> {
                if (entities.isEmpty()) return;

                superTarget = JUtils.getUserIfStand(entities.stream().findFirst().orElseThrow());
                for (int i = 0; i < 8; i++) {
                    FallingBlockEntity sand = FallingBlockEntity.spawnFromBlock(world, superTarget.getBlockPos(), JObjectRegistry.FOOLISH_SAND_BLOCK.getDefaultState());
                    sand.timeFalling = -32767;
                    sand.noClip = true;
                    sand.dropItem = false;
                    sand.setBoundingBox(new Box(0, 0, 0, 0, 0, 0));
                    sand.setNoGravity(true);
                    sands.add(sand);
                }
            }
            case (8) -> {
                for (LivingEntity ent : entities)
                    ent.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 15, 0));
            }
            case (9) -> setSand(false); // Ends transformation state
            case (10) -> {
                if (user == null) return;

                switch (slamType) {
                    case (2) -> {
                        Vec3d leftVec = user.getRotationVector().rotateY(1.75f);
                        for (int i = 0; i < 8; i++) {
                            leftVec = leftVec.rotateY(-3.141592f / 8).normalize();
                            createFoolishSand(new Vec3d(leftVec.x / 4, 0.25, leftVec.z / 4));
                        }
                    }
                    case (3) -> {
                        Vec3d rotVec = user.getRotationVector();
                        for (double i = 0; i < 8; i++)
                            for (double j = 0; j < i; j++) {
                                double hDiv = 5.0 * (1 + j / i);
                                createFoolishSand(new Vec3d(rotVec.x * Math.sqrt(i) / hDiv, j / 5.0, rotVec.z * Math.sqrt(i) / hDiv));
                            }
                    }
                    default -> {
                    }
                }
            }
            case (11) -> {
                SandTornadoEntity sandTornado = new SandTornadoEntity(JEntityTypeRegistry.SAND_TORNADO, world);
                sandTornado.setMaster(user);
                sandTornado.refreshPositionAndAngles(getX(), getY() + 1.5, getZ(), getYaw(), getPitch());
                world.spawnEntity(sandTornado);
            }
        }
    }

    private void createFoolishSand(Vec3d vel) {
        BlockPos midBlockPos = getBlockPos().add(0, 1, 0);
        if (world.getBlockState(midBlockPos).isOpaque()) return;
        FallingBlockEntity sand = FallingBlockEntity.spawnFromBlock(world, midBlockPos, JObjectRegistry.FOOLISH_SAND_BLOCK.getDefaultState());
        sand.setHurtEntities(5f, 5);
        sand.setVelocity(vel);
        sand.velocityModified = true;
        sand.velocityDirty = true;
        sand.intersectionChecked = false;
        sand.dropItem = false;
        world.spawnEntity(sand);
    }

    private void setSandClone(MobEntity clone) {
        //JCraft.LOGGER.info("Setting sand clone to: " + clone + " from " + sandClone);
        if (sandClone != null) sandClone.kill();
        this.sandClone = clone;
        if (clone == null) return;
        applySandCloneModifiers(clone);
    }

    public static void applySandCloneModifiers(LivingEntity entity) {
        if (entity == null) {
            JCraft.LOGGER.error("Tried to apply sand clone attribute modifiers to invalid entity!");
            return;
        }
        EntityAttributeInstance maxHealthAttribute = entity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (maxHealthAttribute == null) {
            JCraft.LOGGER.error("Tried to apply sand clone attribute modifiers to entity with no max health attribute!");
            return;
        }
        maxHealthAttribute.addPersistentModifier(
                new EntityAttributeModifier("Sand Clone Max Health Modifier", -1.0, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
        );
    }

    private static final BlockState sandState = Blocks.SAND.getDefaultState();

    @Override
    public void tick() {
        super.tick();

        if (hasUser()) {
            LivingEntity user = Objects.requireNonNull(getUser());

            if (world.isClient) {
                if (age % 2 == 0) {
                    Vec3d pos = getPos();
                    // If the fool is using any morphing attack, the amount of sand multiplies, and the stand itself changes color
                    int particleNum = isWave() ? 32 : 1 + MathHelper.clamp(getMoveStun() / 2, 0, 5) * (isSand() ? 2 : 1);
                    int height = isWave() || blocking ? 1 : 2;

                    for (int i = 0; i < particleNum; i++) {
                        ParticleEffect effect = (isWave() && random.nextFloat() * 0.5f > 0) ?
                                new BlockStateParticleEffect(ParticleTypes.BLOCK, sandState) :
                                new BlockStateParticleEffect(ParticleTypes.FALLING_DUST, sandState);
                        world.addParticle(
                                effect,
                                pos.x + random.nextTriangular(0, 1),
                                pos.y + random.nextTriangular(height / 2f, height / 2f),
                                pos.z + random.nextTriangular(0, 1),
                                0, 0, 0);
                    }
                }
            } else {
                AbstractMove<?, ? super TheFoolEntity> attack = curMove;
                if (lastRemoteInputTime - age > 4) updateRemoteInputs(0, 0, false);
                if (attack != null) {
                    if (attack.id == slam.id && slamType != 1) queuedAttack = null;
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
                } else if (!blocking && getMoveStun() < 1) { // If idle, reset back to normal material
                    setSand(false);
                    setWave(false);
                }

                // Suffocating Sandstorm logic
                if (sands.isEmpty()) {
                    superTarget = null;
                } else {
                    superTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 10, 0, true, false));
                    superTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 40, 0, true, false));

                    if (age % 20 == 0) {
                        sands.get(0).discard();
                        sands.remove(0);
                    }

                    Vec3d targetPos = superTarget.getPos().add(0, superTarget.getHeight() / 2, 0);

                    int i = 0;
                    int j = 1;
                    for (FallingBlockEntity sand : sands) {
                        if (sand == null || sand.isRemoved())
                            continue;
                        i++;
                        j *= -1;

                        Vec3d newVel = sand.getVelocity().multiply(0.25).add( // Suppress current velocity
                                // And add tracking
                                targetPos.subtract(
                                        // MathHelper.sin(t) * 2, (isEven ? MathHelper.sin(t) : MathHelper.cos(t)) * 2, MathHelper.cos(t) * 2
                                        sand.getPos().add(
                                                random.nextDouble() - 0.5 + Math.sin(age * i / 10.0 * j),
                                                random.nextDouble() * 2 - 1,
                                                random.nextDouble() - 0.5 + Math.cos(age * i / 10.0 * j))
                                ).normalize().multiply(0.5)
                        );

                        sand.setVelocity(newVel);
                        sand.velocityModified = true;
                    }
                }

                // Sand clone logic
                if (sandClone != null && sandClone.age > 200)
                    setSandClone(null);
            }
        }
    }

    // Animation code
    public enum State implements StandAnimationState<TheFoolEntity> {
        IDLE(builder -> builder.loop("animation.thefool.idle")),
        SWIPE(builder -> builder.playAndHold("animation.thefool.swipe")),
        BLOCK((theFool, builder) -> builder.loop("animation.thefool." +
                (theFool.isSand() ? "crouchblock" : "block"))),
        COMBO(builder -> builder.playAndHold("animation.thefool.combo")),
        AIR_BARRAGE(builder -> builder.loop("animation.thefool.airbarrage")),
        LAUNCH(builder -> builder.playAndHold("animation.thefool.launch")),
        POUND_UP(builder -> builder.playAndHold("animation.thefool.poundup")),
        POUND_DOWN(builder -> builder.playAndHold("animation.thefool.pounddown")),
        CHARGE(builder -> builder.loop("animation.thefool.charge")),
        CHARGE_HIT(builder -> builder.playAndHold("animation.thefool.charge_hit")),
        CREATE(builder -> builder.playAndHold("animation.thefool.create")),
        SAND_WAVE(builder -> builder.loop("animation.thefool.sandwave")),
        SANDSTORM(builder -> builder.playAndHold("animation.thefool.sandstorm")),
        GLIDE(builder -> builder.loop("animation.thefool.glide")),
        TORNADO(builder -> builder.loop("animation.thefool.tornado")),
        DRILL(builder -> builder.loop("animation.thefool.drill"));

        private final BiConsumer<TheFoolEntity, AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this((fool, builder) -> animator.accept(builder));
        }

        State(BiConsumer<TheFoolEntity, AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(TheFoolEntity attacker, AnimationBuilder builder) {
            animator.accept(attacker, builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @Nullable String getSummonAnimation() {
        return "animation.thefool.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
