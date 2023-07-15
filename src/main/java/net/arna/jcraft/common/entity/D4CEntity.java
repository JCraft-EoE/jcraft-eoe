package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
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

public class D4CEntity extends StandEntity implements IAnimatable, IAnimationTickable {
    public static final Attack light = new Attack(0, 2, 0.75f, 15, 9, 1.5, 5f, 0.75f, AttackType.BOX, 1.5f, -0.1f, 0, JSoundRegister.IMPACT_2)
            .setInfo("Chop", "quick combo starter");
    public static final Attack barrage = new Attack(2, 17, 0.75f, 70, 0, 2, 0.8f, 0.25f, AttackType.BARRAGE, 2, 0, 3, JSoundRegister.IMPACT_2)
            .setInfo("Barrage", "fast reliable combo starter/extender, high stun");
    public static final Attack heavy = new Attack(1, 15, 1, 25, 14, 2, 8f, 1.5f, AttackType.BOX, 0.6f, -0.2f, 0, JSoundRegister.IMPACT_2)
            .setHitspark(2)
            .hyperArmor()
            .setLaunch()
            .setInfo("Charge", "user & stand charge forward, uninterruptable launcher");
    public static final Attack dimhop_others = new Attack(3, 60, 1, 60, 40, 1.5, 0f, 0.0f, AttackType.BOX)
            .setInfo("Dimensional Hop", "travels to a random dimension at exact coordinates, if user was hit in the last 30s, he is forced back, certified death button");
    public static final Attack grab = new Attack(4, 25, 0.75f, 21, 12, 1.5, 0f, 0.0f, AttackType.BOX, 2, 0, 0, null)
            .setUB(false)
            .setStunOverride(true)
            .setStunType(0)
            .setInfo("Grab/Summon Gun", "unblockable, combo finisher/crouch to give yourself the gun");
    public static final Attack grabhit = new Attack(5, 0, 0.75f, 34, 0, 2, 4f, 0f, AttackType.MULTIHIT, 0.5f, 0, List.of(11, 17, 26), JSoundRegister.IMPACT_1);
    public static final Attack givegun = new Attack(6, 25, 14, 10, 0, 0.75f, AttackType.BOX)
            .setInfo("Grab", "unblockable, combo finisher");
    public static final Attack counter = new Attack(7, 30, 35, 5, 0, 0.75f, AttackType.COUNTER)
            .setInfo("Counter", "0.25s startup, 1.5s duration, high damage, knocks back when hit");
    public static final Attack clonespawn = new Attack(8, 40, 1, 50, 40, 0, 0f, 0.0f, AttackType.BOX)
            .setRanged(true)
            .setInfo("Dimensional Clone", "summons an unlimited number of servants");
    public static final Attack flag = new Attack(9, 20, 60, 10, 0, 0, AttackType.BOX)
            .setInfo("Dimensional Phase", "hides in a flag in an un-stunnable, floating state")
            .setMobility(MobilityType.HIGHJUMP);
    public static ServerWorld auWorld;

    public D4CEntity(World worldIn) {
        super(StandType.D4C, worldIn);
        super.initialize();
        idleRotation = -45f;

        description = "All Range, Multipurpose TRICKSTER";

        pros = List.of(
                "good combo tools",
                "counter",
                "easy setups",
                "good pressure"
        );

        cons = List.of(
                "requires preparation",
                "slow barrage with less damage"
        );

        freespace =
                """
                        Passive - multiversal guns attract and blow up, including ones obtained via M3
                        BNB:
                            (M1)>Gun>M1>Barrage>M1>Grab""";

        moves = List.of(light, heavy, barrage, dimhop_others, clonespawn, grab, counter, flag);

        if (world.isClient) return;
        auWorld = getServer().getWorld(JDimensionRegister.AU_DIMENSION_KEY);
    }

    @Override
    public void initLightAttack() {
        if (!this.canAttack()) return;
        if (handleAttack(light, JCraft.standLightCD, 2)) {
            this.playSound(JSoundRegister.D4C_LIGHT, 1, 1);
        }
    }

    @Override
    public void initHeavyAttack() {
        if (!this.canAttack()) return;
        if (handleAttack(heavy, JCraft.standHeavyCD, 4)) {
            this.playSound(JSoundRegister.D4C_HEAVY, 1, 1);
            Entity ent = this.getUser();
            if (ent.isOnGround()) {
                ent.setVelocity(ent.getVelocity().add(this.getRotationVector().multiply(0.75)).add(0.0, 0.15, 0.0));
                ent.velocityModified = true;
            }
        }
    }

    @Override
    public void initBarrage() {
        if (!this.canAttack()) return;
        if (handleAttack(barrage, JCraft.standBarrageCD, 5)) {
            this.playSound(JSoundRegister.D4C_BARRAGE, 1, 1);
        }
    }

    @Override
    public void initSpecial1() {
        if (!this.canAttack()) return;
        if (handleAttack(clonespawn, JCraft.standS1CD, 6)) {
            this.playSound(JSoundRegister.D4C_DIMHOP, 1, 1);
        }
    }

    @Override
    public void initUlt() {
        // Ability to cancel dimension hop
        if (this.curAttack == dimhop_others) {
            this.setMoveStun(0);
            this.curAttack = null;
        }

        if (!this.canAttack()) return;

        LivingEntity user = getUser();
        if (user instanceof ServerPlayerEntity serverPlayer) { // Logic for cancelling dimhop early, and generating failsafe data
            if (user.getWorld().getRegistryKey().equals(JDimensionRegister.AU_DIMENSION_KEY)) {
                boolean isStored = false; // Should always be true
                for (DimValues dimV : JCraft.pastDimensions) {
                    if (dimV.user != user)
                        continue;
                    isStored = true;
                    dimV.timer = 1;
                }

                if (!isStored) { // If not stored, force your way back
                    BlockPos spawnPos = serverPlayer.getSpawnPointPosition(); // Prioritize spawn point
                    if (spawnPos == null) {
                        spawnPos = serverPlayer.getBlockPos();
                    } // Use current position if all else fails
                    JCraft.pastDimensions.add(
                            new DimValues(user
                                    , new Vec3d(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ())
                                    , serverPlayer.getSpawnPointDimension()
                            )
                    );
                }
            }
        }

        if (handleAttack(dimhop_others, JCraft.standUltCD, 6)) {
            this.playSound(JSoundRegister.D4C_DIMHOP, 1, 1);
        }
    }

    @Override
    public void initSpecial2() {
        if (!this.canAttack()) return;
        if (getUser().isSneaking() && handleAttack(givegun, JCraft.standS2CD, 10)) {
            this.playSound(JSoundRegister.D4C_THROW, 1, 1);
            this.equipStack(EquipmentSlot.MAINHAND, JObjectRegistry.FVREVOLVER.getDefaultStack());
        } else if (handleAttack(grab, JCraft.standS2CD, 7)) {
            this.playSound(JSoundRegister.D4C_THROW, 1, 1);
            this.equipStack(EquipmentSlot.MAINHAND, JObjectRegistry.FVREVOLVER.getDefaultStack());
        }
    }

    @Override
    public void initSpecial3() {
        if (!this.canAttack()) return;
        handleAttack(counter, JCraft.standS3CD, 8);
    }

    @Override
    protected Box calculateBoundingBox() {
        if (getState() == 11) {
            double x = getX();
            double y = getY();
            double z = getZ();
            return new Box(x + 0.5, y + 0.5, z + 0.5, x - 0.5, y, z - 0.5);
        }
        return super.calculateBoundingBox();
    }

    @Override
    public void initUtil() {
        if (!this.canAttack()) return;
        if (handleAttack(flag, JCraft.utilCD, 11)) {
            getUser().addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, flag.moveStun, 0, true, false));
            getUser().addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, flag.moveStun, 0, true, false));
            playSound(JSoundRegister.D4C_UTILITY, 1, 1);
        }
    }

    /* -- OLD GUN THROW CODE
                Vec3d rotVec = this.getRotationVector();
                Vec3d eyePos = this.getEyePos();

                ItemEntity revolver1 = new ItemEntity(EntityType.ITEM, world);
                revolver1.setStack(new ItemStack(JObjectRegistry.FVREVOLVER, 1));
                revolver1.setPickupDelay(100);
                revolver1.setPosition(eyePos.add(rotVec.rotateY(90)));
                revolver1.setVelocity(rotVec.rotateY(95).multiply(1.5));

                ItemEntity revolver2 = new ItemEntity(EntityType.ITEM, world);
                revolver2.setStack(new ItemStack(JObjectRegistry.FVREVOLVER, 1));
                revolver2.setPickupDelay(100);
                revolver2.setPosition(eyePos.add(rotVec.rotateY(-90)));
                revolver2.setVelocity(rotVec.rotateY(-95).multiply(1.5));

                world.spawnEntity(revolver1);
                world.spawnEntity(revolver2);
    */

    private static final Attack grabhitfinal = new Attack(10, 0, 0.75f, 34, 0, 2, 4f, 1.2f, AttackType.MULTIHIT, 0.45f, 0, List.of(11, 17, 26), JSoundRegister.IMPACT_1)
            .setHitspark(2)
            .setLaunch();
    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        Entity player = this.getUser();
        switch (attack.id) {
            case (3) -> {
                ChunkPos origin = getChunkPos();
                ServerWorld world = (ServerWorld) getWorld();

                for (int x = -3; x < 4; x++) {
                    for (int z = -3; z < 4; z++) {
                        int cX = origin.x + x;
                        int cZ = origin.z + z;
                        JCraft.preloadChunk(auWorld, cX, cZ);
                        ChunkSection[] orSec = world.getChunk(cX, cZ).getSectionArray();
                        ChunkSection[] auSec = auWorld.getChunk(cX, cZ).getSectionArray();
                        System.arraycopy(orSec, 0, auSec, 0, Math.min(orSec.length, auSec.length));
                    }
                }

                List<Entity> toHop = new ArrayList<>(entities);
                toHop.add(player);
                int heightOffset = auWorld.getHeight() - world.getHeight();
                for (Entity entity : toHop) {
                    if (entity instanceof LivingEntity living)
                        living.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 300, 4, true, false));
                    JCraft.DimensionHop(entity, heightOffset / 2);
                }
            }
            case (4) -> {
                if (entities.size() > 0) {
                    // Grab bypasses and disables block
                    for (LivingEntity ent : entities) {
                        stun(ent, 34, 0);
                        if (ent.getFirstPassenger() instanceof StandEntity stand)
                            stand.blocking = false;
                    }

                    setAttack(grabhit, 9);
                } else {
                    this.getMainHandStack().decrement(1);
                }
            }
            case (5) -> {
                if (this.getMoveStun() == 17)
                    this.curAttack = grabhitfinal;
                this.playSound(JSoundRegister.REVOLVER_FIRE, 1, 1);
            }
            case (6) -> {
                if (player instanceof PlayerEntity playerEntity) {
                    playerEntity.giveItemStack(JObjectRegistry.FVREVOLVER.getDefaultStack());
                    this.getMainHandStack().decrement(1);
                }
            }
            case (8) -> {
                ItemStack weapon = new ItemStack(Items.IRON_SWORD);
                weapon.setDamage(249);

                if (player instanceof PlayerEntity playerEntity) {
                    PlayerCloneEntity playerCloneEntity = new PlayerCloneEntity(JEntityTypeRegister.PLAYER_ENTITY_CLONE, this.world);
                    playerCloneEntity.copyPositionAndRotation(playerEntity);
                    playerCloneEntity.setMaster(playerEntity);

                    world.spawnEntity(playerCloneEntity);
                    playerCloneEntity.equipStack(EquipmentSlot.MAINHAND, weapon);
                } else if (player instanceof MobEntity mob) { //Code sourced from MobEntity.class convertTo()
                    EntityType<?> entityType = mob.getType();
                    MobEntity newMob = (MobEntity) entityType.create(world);

                    if (newMob == null) {
                        JCraft.LOGGER.error("Failed to create D4C clone mob of type " + entityType + " in world " + world);
                        return;
                    }

                    newMob.copyPositionAndRotation(mob);
                    newMob.setBaby(mob.isBaby());

                    if (mob.hasCustomName()) {
                        newMob.setCustomName(mob.getCustomName());
                        newMob.setCustomNameVisible(mob.isCustomNameVisible());
                    }

                    newMob.age = mob.age;
                    IEntityDataSaver newMobData = (IEntityDataSaver) newMob;
                    newMobData.getPersistentData().putInt("StandID", 0);

                    world.spawnEntity(newMob);
                    newMob.equipStack(EquipmentSlot.MAINHAND, weapon);
                }
            }
            case (9) -> {
                int duration = flag.moveStun - flag.initTime;
                getUser().addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, duration, 0, true, false));
                getUser().addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, duration, 2, true, false));
            }
            case (10) -> this.getMainHandStack().decrement(1);
        }
    }

    @Override
    public void counter(Entity entity, DamageSource source) {
        super.counter(entity, source);

        if (entity == null || !hasUser()) {
            return;
        }
        LivingEntity user = this.getUser();
        if (!source.isProjectile() && !source.isMagic()) {
            Vec3d trueKnockback = entity.getPos().subtract(user.getPos()).normalize().multiply(1.5);
            entity.addVelocity(trueKnockback.x, 0.5, trueKnockback.z);
            entity.velocityModified = true;

            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.damage(DamageSource.mob(user), 10);
                stun(livingEntity, 20, 3);

                StandEntity stand = ( (IEntityDataSaver)livingEntity ).getStand();
                if (stand != null)
                    stand.cancelAttack();
            }

            this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1f, 1f);
            this.playSound(JSoundRegister.D4C_COUNTER, 1, 1);
        }
    }

    private static final Attack counterMiss = new Attack(8, 0, 10, 11);
    @Override
    public void whiffCounter() {
        setAttack(counterMiss, 12);
        stun(getUser(), counterMiss.moveStun, 0);
    }

    @Override
    public void tick() {
        if (age == 1) {
            this.playSound(JSoundRegister.STAND_SUMMON, 1, 1);
            this.playSound(JSoundRegister.D4C_SUMMON, 1, 1);
        }

        super.tick();

        if (hasUser())
            this.setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(getUser()) / 2, 0.0, 255.0) / 255f);
    }

    // Animation code
    final AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.animationFactory;
    }

    @Override
    public int tickTimer() {
        return age;
    }

    @SuppressWarnings("SameReturnValue")
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationController<E> controller = event.getController();
        AnimationBuilder builder = new AnimationBuilder();

        if (playSummonAnim) {
            controller.setAnimation(builder.playOnce("animation.d4c.summon"));
            return PlayState.CONTINUE;
        }
        if (this.getSameState()) controller.markNeedsReload();
        switch (this.getState()) {
            default -> controller.setAnimation(builder.loop("animation.d4c.idle"));
            case 2 -> controller.setAnimation(builder.playAndHold("animation.d4c.light"));
            case 3 -> controller.setAnimation(builder.loop("animation.d4c.block"));
            case 4 -> controller.setAnimation(builder.playAndHold("animation.d4c.heavy"));
            case 5 -> controller.setAnimation(builder.loop("animation.d4c.barrage"));
            case 6 -> controller.setAnimation(builder.playAndHold("animation.d4c.dimhop"));
            case 7 -> controller.setAnimation(builder.playAndHold("animation.d4c.throw"));
            case 8 -> controller.setAnimation(builder.loop("animation.d4c.counter"));
            case 9 -> controller.setAnimation(builder.playAndHold("animation.d4c.throwhit"));
            case 10 -> controller.setAnimation(builder.playAndHold("animation.d4c.givegun"));
            case 11 -> controller.setAnimation(builder.playAndHold("animation.d4c.flag"));
            case 12 -> controller.setAnimation(builder.playAndHold("animation.d4c.counter_miss"));
        }
        return PlayState.CONTINUE;
    }
}
