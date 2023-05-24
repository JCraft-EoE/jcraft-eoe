package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.network.s2c.ServerChannelFeedback;
import net.arna.jcraft.common.util.Attack;
import net.arna.jcraft.common.util.AttackType;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.registry.JEntityTypeRegister;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
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

public class KillerQueenEntity extends StandEntity implements IAnimatable, IAnimationTickable {
    AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

    public static Attack light = new Attack(2, 0.75f, 19, 0, 1.5, 3.5f, 0.75f, AttackType.MULTIHIT, 1f, 0, List.of(6, 11), JSoundRegister.IMPACT_4)
            .setInfo("Dual Punch", "quick combo starter");
    public static Attack heavy = new Attack(12, 0.75f, 24, 16, 2, 9f, 1.75f, AttackType.BOX, 0.5f, 0, 0, JSoundRegister.IMPACT_4).setHitspark(2).setArmor(true).setLaunch()
            .setInfo("Haymaker", "slow, uninterruptable launcher");
    public static Attack barrage = new Attack(17, 0.75f, 50, 0, 1.5, 1f, 0.1f, AttackType.BARRAGE, 1, 0, 3, JSoundRegister.IMPACT_4)
            .setInfo("Barrage", "fast reliable combo starter/extender, medium stun");
    public static Attack bombplant = new Attack(30, 1, 20, 12, 1.5, 0f, 0.0f, AttackType.BOX)
            .setUB(true)
            .setInfo("Bomb Plant", "crouch to plant on the ground below you, stealthily");
    public static Attack detonate = new Attack(1, 1, 6, 5, 0, 0f, 0.0f, AttackType.BOX)
            .setInfo("Detonate", "slight windup");
    public static Attack sha = new Attack(45, 20, 16, 0, AttackType.BOX).setRanged(true)
            .setInfo("Sheer Heart Attack", "creates an automatic, heat-seeking sub-stand that explodes on contact, reflects 25% damage back to owner");
    public ItemEntity coin;
    public Entity bombEntity;
    public Vec3d bombBlock;

    public KillerQueenEntity(EntityType<? extends StandEntity> type, World worldIn) {
        super(type, worldIn);
        super.initialize();
        idleRotation = -30f;

        description = "Explosive SETPLAY";

        pros = List.of(
                "good stun",
                "excellent setups",
                "easy knockdowns",
                "good zoning"
        );

        cons = List.of(
                "limited pressure tools",
                "below-average speed",
                "limited combo tools"
        );

        freespace = "BNBs:\n" +
                "    -Standard bomb plant confirm and SHA setup\n" +
                "    M1>Barrage>Bomb plant>Detonate(>Sheer Heart Attack)\n" +
                "    -Confirm while bomb plant is on cd\n" +
                "    M1>Barrage>Heavy(>Sheer Heart Attack)";


        moves = List.of(light, heavy, barrage, bombplant, detonate, sha
                , new Attack().setRanged(true).setInfo("Coin Toss", "overrides current bomb with an aimable coin")
                , new Attack().setMobility(MobilityType.DASH).setInfo("Explosive Dash", "slight aoe damage, 3D movement tool"));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
    }

    public Vec3d getBombPos() {
        if (this.bombEntity != null) {
            return this.bombEntity.getPos();
        }
        if (this.bombBlock != null) {
            return this.bombBlock;
        }
        return null;
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
    public void initHeavyAttack() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(heavy, JCraft.standHeavyCD, 4)) {
            this.playSound(JSoundRegister.KQ_UPPERCUT, 1, 1);
            this.playSound(JSoundRegister.KQ_HEAVY, 1, 1);
        }
    }

    @Override
    public void initBarrage() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(barrage, JCraft.standBarrageCD, 5)) {
            this.playSound(JSoundRegister.KQ_BARRAGE, 1, 1);
        }
    }

    @Override
    public void initSpecial1() {
        if (!this.canAttack()) {
            return;
        }
        LivingEntity user = this.getUser();
        NbtCompound userData = ((IEntityDataSaver) user).getPersistentData();
        if (user.isInSneakingPose() && userData.getInt(JCraft.standS1CD) < 1) {
            BlockPos downBlock = user.getBlockPos().down();
            boolean notAir = (world.getBlockState(downBlock).getBlock() != Blocks.AIR && world.getBlockState(downBlock).getBlock() != Blocks.CAVE_AIR && world.getBlockState(downBlock).getBlock() != Blocks.VOID_AIR);
            if (notAir) {
                this.bombEntity = null;
                this.bombBlock = user.getPos().add(0, -0.5, 0);
                userData.putInt(JCraft.standS1CD, bombplant.cooldown * 20);
            }
        } else {
            handleAttack(bombplant, JCraft.standS1CD, 7);
            this.bombBlock = null;
        }

        if (this.coin != null)
            this.coin.discard();
    }

    @Override
    public void initUlt() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(detonate, JCraft.standUltCD, 6)) {
            this.playSound(JSoundRegister.KQ_DETONATE, 1, 1);
        }
    }

    @Override
    public void initSpecial2() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(sha, JCraft.standS2CD, 8)) {
            //this.playSound(ModSoundRegister.KQ_SHA,1, 1);
        }
    }

    @Override
    public void initSpecial3() {
        if (!this.canAttack())
            return;

        LivingEntity user = this.getUser();
        NbtCompound playerData = ((IEntityDataSaver) user).getPersistentData();
        if (playerData.getInt(JCraft.standS3CD) > 0) {
            return;
        }

        Vec3d lookVec = user.getRotationVector().multiply(0.75);
        this.coin = new ItemEntity(this.world, user.getX(), user.getY() + user.getHeight() * 2 / 3, user.getZ(), new ItemStack(JObjectRegistry.KQCOIN, 1), lookVec.x, lookVec.y, lookVec.z);
        this.coin.setPickupDelayInfinite();

        this.world.spawnEntity(this.coin);

        this.bombEntity = this.coin;
        this.bombBlock = null;

        playerData.putInt(JCraft.standS3CD, 500); // 25s coin toss cd
        playerData.putInt(JCraft.standUltCD, 20); // 1s detonate cd (prevents IUB)
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        LivingEntity user = this.getUser();
        if (attack == bombplant) {
            if (entities.size() > 0) { // Living entities take priority
                bombEntity = entities.get(0);
                bombBlock = null;
            } else { // If none are found, re-do an optimized hitbox check for any entity type
                Vec3d rotVec = getRotationVector();
                Vec3d boxCenter = getPos().add(0, user.getHeight() / 2, 0).add(rotVec);
                Vec3d halfBox = new Vec3d(0.5, 0.5, 0.5);
                List<Entity> hit = world.getEntitiesByClass(Entity.class,
                        new Box(boxCenter.subtract(halfBox), boxCenter.add(halfBox))
                        , EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                hit.remove(this);
                hit.remove(user);

                if (hit.size() > 0) {
                    bombEntity = hit.get(0);
                    bombBlock = null;
                }
            }
        } else if (attack == detonate) {
            if (bombEntity instanceof LivingEntity livingEntity) {
                world.createExplosion(user, livingEntity.getX(), livingEntity.getY() + livingEntity.getHeight() / 2, livingEntity.getZ(), 2f, Explosion.DestructionType.NONE);
                livingEntity.addStatusEffect(new StatusEffectInstance(JStatusRegister.Knockdown, 35, 0, true, false));
            } else {
                Vec3d bombPos = null;

                if (bombEntity != null) {
                    bombPos = bombEntity.getPos();
                    if (bombEntity instanceof ItemEntity) {
                        bombEntity.kill();
                    }
                }
                if (bombBlock != null) {
                    bombPos = bombBlock;
                }

                if (bombPos != null) {
                    world.createExplosion(user, bombPos.x, bombPos.y, bombPos.z, 2f, Explosion.DestructionType.NONE);

                    List<LivingEntity> toKD = world.getEntitiesByClass(
                            LivingEntity.class,
                            new Box(bombPos.add(2.2, 2.2, 2.2), bombPos.add(-2.2, -2.2, -2.2)),
                            EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR
                    );

                    for (LivingEntity livingEntity : toKD) {
                        livingEntity.addStatusEffect(new StatusEffectInstance(JStatusRegister.Knockdown, 35, 0, true, false));
                    }

                    world.playSound(bombPos.x, bombPos.y, bombPos.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.75f, 1, true);
                }
            }

            bombEntity = null;
            bombBlock = null;
        } else if (attack == sha) {
            SheerHeartAttackEntity sha = new SheerHeartAttackEntity(JEntityTypeRegister.SHEER_HEART_ATTACK, world);
            sha.setOwner(user);
            sha.copyPositionAndRotation(this);

            world.spawnEntity(sha);
        }
    }

    @Override
    public void initMiddleClick() {
        if (!this.canAttack())
            return;
        LivingEntity user = this.getUser();

        NbtCompound playerData = ((IEntityDataSaver) user).getPersistentData();
        if (playerData.getInt(JCraft.standMMBCD) > 0) {
            return;
        }

        Vec3d lookVec = user.getRotationVector().multiply(0.9);
        world.createExplosion(user,
                user.getX() - lookVec.x,
                user.getY() + user.getHeight() / 2 - lookVec.y,
                user.getZ() - lookVec.z,
                1f, Explosion.DestructionType.NONE);

        user.setVelocity(user.getVelocity().add(lookVec));
        user.velocityModified = true;

        playerData.putInt(JCraft.standMMBCD, 360); // 18s explosive dash cooldown
        this.playSound(JSoundRegister.KQ_DETONATE, 1, 1);
    }

    @Override
    public void desummon() {
        if (coin != null) {
            coin.discard();
        }

        super.desummon();
    }

    @Override
    public MoveSelectionResult SpecificMoveSelectionCriterion(Attack attack, MobEntity mob, LivingEntity target, int stunTicks, int enemyMoveStun, double distance, StandEntity enemyStand, Attack enemyAttack) {
        Vec3d bombPos = this.getBombPos();
        if (bombPos != null && attack == detonate && target.squaredDistanceTo(bombPos) < 9.0D) {
            return MoveSelectionResult.USE;
        }
        return MoveSelectionResult.PASS;
    }

    @Override
    public void tick() {
        if (age == 1) {
            this.world.playSound(null, this.getX(), this.getY(), this.getZ(), JSoundRegister.STAND_SUMMON, SoundCategory.PLAYERS, 1f, 1f);
        }

        super.tick();

        if (hasUser()) {
            LivingEntity user = this.getUser();
            if (world.isClient) {
                this.setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(user) / 2, 0.0, 255.0) / 255f);
            } else {
                if (user instanceof PlayerEntity playerEntity) {
                    boolean bombExists = (bombEntity != null || bombBlock != null);

                    double dX1 = 0;
                    double dY1 = 0;
                    double dZ1 = 0;
                    double dX2 = 0;
                    double dY2 = 0;
                    double dZ2 = 0;

                    Box bBox = null;

                    if (bombEntity != null) { // If the bomb isn't a block
                        dX1 = bombEntity.getX();
                        dY1 = bombEntity.getY();
                        dZ1 = bombEntity.getZ();

                        bBox = bombEntity.getBoundingBox();

                        dX2 = bBox.getXLength();
                        dY2 = bBox.getYLength();
                        dZ2 = bBox.getZLength();
                    } else if (bombBlock != null) { // If the bomb is a block
                        dX1 = bombBlock.getX();
                        dY1 = bombBlock.getY();
                        dZ1 = bombBlock.getZ();

                        dX2 = dY2 = dZ2 = 1.41;
                    }

                    if (bombExists) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeShort(4);

                        buf.writeDouble(dX1);
                        buf.writeDouble(dY1);
                        buf.writeDouble(dZ1);

                        buf.writeDouble(dX2);
                        buf.writeDouble(dY2);
                        buf.writeDouble(dZ2);

                        boolean anyInRange = false;
                        Vec3d bPos = this.getBombPos();
                        Vec3d v1 = bPos.add(3, 3, 3);
                        Vec3d v2 = bPos.add(-3, -3, -3);
                        List<LivingEntity> list = this.world.getEntitiesByClass(LivingEntity.class, new Box(v1, v2), EntityPredicates.VALID_LIVING_ENTITY);
                        list.remove(bombEntity);
                        for (LivingEntity l :
                                list) {
                            if (l.squaredDistanceTo(bPos) < 9) {
                                anyInRange = true;
                                break;
                            }
                        }

                        buf.writeBoolean(anyInRange);

                        if (bBox == null || bBox.getAverageSideLength() > 0) {
                            if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
                                ServerChannelFeedback.send(serverPlayerEntity, buf);
                            }
                        }
                    }
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
        if (this.getSameState()) {
            controller.markNeedsReload();
        }
        switch (this.getState()) {
            default -> controller.setAnimation(builder.loop("animation.kq.idle"));
            case 2 -> controller.setAnimation(builder.playAndHold("animation.kq.light"));
            case 3 -> controller.setAnimation(builder.loop("animation.kq.block"));
            case 4 -> controller.setAnimation(builder.playAndHold("animation.kq.heavy"));
            case 5 -> controller.setAnimation(builder.loop("animation.kq.barrage"));
            case 6 -> controller.setAnimation(builder.playAndHold("animation.kq.detonate"));
            case 7 -> controller.setAnimation(builder.playAndHold("animation.kq.bombplant"));
            case 8 -> controller.setAnimation(builder.playAndHold("animation.kq.sha"));
        }
        return PlayState.CONTINUE;
    }
}
