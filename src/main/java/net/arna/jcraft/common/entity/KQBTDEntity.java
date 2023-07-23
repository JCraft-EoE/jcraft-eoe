package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.attack.AttackType;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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

import java.util.List;

public class KQBTDEntity extends KillerQueenEntity implements IAnimatable, IAnimationTickable {
    public static final Attack light = Attack.copyOf(KillerQueenEntity.light);
    public static final Attack heavy = new Attack(2, 12, 0.75f, 9, 5, 1, 7.5f, 1.1f, AttackType.BOX, 0.5f, 0, 0, JSoundRegister.IMPACT_4).setHitspark(2).setLaunch()
            .setInfo("Elbow", "fast, short-range knockback");
    public static final Attack barrage = Attack.copyOf(KillerQueenEntity.barrage);
    public static final Attack bombplant = Attack.copyOf(KillerQueenEntity.bombplant);
    public static final Attack bubblecounter = new Attack(7, 27, 20, 5, 0, 1, AttackType.COUNTER)
            .setInfo("Stray Cat Counter", "");
    public static final Attack bubble = new Attack(5, 23, 0.75f, 18, 15, 0, 0f, 0.0f, AttackType.BOX).setRanged(true)
            .crouchingVariation(bubblecounter)
            .setInfo("Stray Cat", "launches an explosive bubble/crouch for a 0.25s windup counter");
    public static final Attack detonate = new Attack(6, 1, 0.75f, 6, 5, 0, 0f, 0.0f, AttackType.BOX)
            .setInfo("Detonate", "crouch with a bomb planted within 20s on a living being to activate Bites the Dust");
    public static final Attack btdplant = new Attack(7, 50, 1, 24, 14, 1.5, 0f, 0.0f, AttackType.BOX, 0.5f)
            .setUB(true)
            .setBlockstun(8)
            .setInfo("Bites the Dust Plant", "press the same button to detonate, sending the affected enemy back to their ");

    private BubbleProjectile bubbleProjectile;
    private Entity bombEntity, btdEntity;
    private Vec3d bombBlock, btdPos;

    //private NbtCompound userData;
    //private NbtCompound targetData;

    public KQBTDEntity(World worldIn) {
        super(StandType.KILLER_QUEEN_BITES_THE_DUST, worldIn);
        super.initialize();

        description = "Ascended Explosive SETPLAY";

        pros = List.of(
                "good stun",
                "excellent setups",
                "easy knockdowns and knockbacks",
                "good zoning"
        );

        cons = List.of(
                "limited pressure tools",
                "no armored moves"
        );

        freespace = "BNBs:\n" +
                "    M1>Barrage>Coin Toss>M1>Heavy>Detonate";

        moves = List.of(KillerQueenEntity.light, heavy, KillerQueenEntity.barrage, KillerQueenEntity.bombplant, detonate, bubble
                , new Attack().setRanged(true).setInfo("Coin Toss", "overrides current bomb with an aimable coin")
                , new Attack().setMobility(MobilityType.DASH).setInfo("Explosive Dash", "slight aoe damage, 3D movement tool"));
    }

    // Necessary, otherwise it simply doesn't reference the correct ones
    @Override
    public Vec3d getBombPos() {
        if (this.bombEntity != null)
            return this.bombEntity.getPos();
        if (this.bombBlock != null)
            return this.bombBlock;
        return null;
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;
        if (handleAttack(heavy, JCraft.standHeavyCD, 4))
            playSound(JSoundRegister.KQBTD_ELBOW, 1, 1);
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleAttack(barrage, JCraft.standBarrageCD, 5))
            playSound(JSoundRegister.KQ_BARRAGE, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (!canAttack() || !hasUser()) return;
        LivingEntity user = getUserOrThrow();
        NbtCompound playerData = ((IEntityDataSaver) user).getPersistentData();

        if (user.isInSneakingPose() && playerData.getInt(JCraft.standS1CD) < 1) {
            BlockPos downBlock = user.getBlockPos().down();
            boolean notAir = (world.getBlockState(downBlock).getBlock() != Blocks.AIR && world.getBlockState(downBlock).getBlock() != Blocks.CAVE_AIR && world.getBlockState(downBlock).getBlock() != Blocks.VOID_AIR);
            if (notAir) {
                this.bombEntity = null;
                this.bombBlock = user.getPos().add(0, -0.5, 0);
                playerData.putInt(JCraft.standS1CD, bombplant.cooldown * 20);
            }
        } else {
            handleAttack(bombplant, JCraft.standS1CD, 7);
            this.bombBlock = null;
        }
    }

    boolean detonateBTD = false;

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        if (handleAttack(detonate, JCraft.standUltCD, 6)) {
            playSound(JSoundRegister.KQ_DETONATE, 1, 1);
            detonateBTD = false;
        }
    }

    @Override
    public void initSpecial2() {
        if (!canAttack() || !hasUser()) return;
        if (getUserOrThrow().isSneaking() && handleAttack(bubblecounter, JCraft.standS2CD, 10)) {
            //playSound(JSoundRegister.KQBTD_COUNTER, 1, 1);
        } else if (handleAttack(bubble, JCraft.standS2CD, 8))
            playSound(JSoundRegister.KQ_UPPERCUT, 1, 1);
    }

    @Override
    public void initSpecial3() {
        if (btdEntity != null && handleAttack(detonate, JCraft.ultCD, 6)) {
            playSound(JSoundRegister.KQ_DETONATE, 1, 1);
            detonateBTD = true;
            return;
        }
        if (!canAttack()) return;
        if (handleAttack(btdplant, JCraft.standS3CD, 12)) {
            //playSound(JSoundRegister.KQ_UPPERCUT, 1 ,1);
        }
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        LivingEntity user = this.getUser();
        switch (attack.id) {
            case (2) -> {
                for (LivingEntity ent : entities)
                    ent.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 5, 4, true, false));
            }
            case (4) -> {
                if (user == null) return;

                if (entities.size() > 0) { // Living entities take priority
                    bombEntity = entities.get(0);
                    bombBlock = null;

                    /*
                    targetData = new NbtCompound();
                    userData = new NbtCompound();

                    bombEntity.writeNbt(targetData);
                    user.writeNbt(userData);
                     */
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
            }
            case (5) -> {
                if (user == null) return;

                bubbleProjectile = new BubbleProjectile(world, user);
                bubbleProjectile.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                bubbleProjectile.setVelocity(user, user.getPitch(), user.getYaw(), 0, 0.5f, 0f);
                bubbleProjectile.setPosition(getPos().add(0, 1.25, 0));
                world.spawnEntity(bubbleProjectile);

                bombEntity = bubbleProjectile;
                bombBlock = null;
            }
            case (6) -> {
                if (user == null) return;

                if (detonateBTD) {
                    if (btdEntity instanceof LivingEntity livingEntity) {
                        world.createExplosion(user, livingEntity.getX(), livingEntity.getY() + livingEntity.getHeight() / 2, livingEntity.getZ(), 2f, Explosion.DestructionType.NONE);
                        livingEntity.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 35, 0, true, false));

                        Vec3d pos = btdEntity.getPos();
                        JCraft.createParticle((ServerWorld) getWorld(), pos.x, pos.y + 2, pos.z, -4);
                        Vec3d v1 = pos.add(3, 3, 3);
                        Vec3d v2 = pos.add(-3, -3, -3);
                        List<LivingEntity> list = world.getEntitiesByClass(LivingEntity.class, new Box(v1, v2), EntityPredicates.VALID_LIVING_ENTITY);

                        if (user.getVehicle() instanceof LivingEntity livingVehicle)
                            list.remove(livingVehicle);

                        list.remove(user);
                        list.remove(this);
                        list.remove(livingEntity);

                        for (LivingEntity l : list)
                            if (l.squaredDistanceTo(pos) < 9) {
                                if (l.squaredDistanceTo(pos) < 2.25) {
                                    world.createExplosion(user, l.getX(), l.getY() + l.getHeight() / 2, l.getZ(), 1.5f, Explosion.DestructionType.NONE);
                                } else {
                                    world.createExplosion(user, l.getX(), l.getY() + l.getHeight() / 2, l.getZ(), 1f, Explosion.DestructionType.NONE);
                                }
                            }

                        livingEntity.teleport(btdPos.x, btdPos.y, btdPos.z);
                        btdEntity = null;
                    }
                } else {
                    if (bombEntity instanceof LivingEntity livingEntity) {
                        world.createExplosion(user, livingEntity.getX(), livingEntity.getY() + livingEntity.getHeight() / 2, livingEntity.getZ(), 2f, Explosion.DestructionType.NONE);
                        livingEntity.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 35, 0, true, false));
                    /*
                    if (user.isSneaking()) {
                        if (targetData != null && userData != null) {
                            if (bombEntity.isAlive()) {
                                if (bombEntity instanceof PlayerEntity playerEntity) {
                                    // Antidupe
                                    targetData.put("Inventory", playerEntity.getInventory().writeNbt(new NbtList()));
                                    targetData.put("EnderItems", playerEntity.getEnderChestInventory().toNbtList());

                                    // Correct position and velocity update
                                    ServerPlayerEntity serverPlayer = ((ServerPlayerEntity) playerEntity);
                                    serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(bombEntity));
                                    NbtList list = targetData.getList("Pos", 6);
                                    serverPlayer.teleport(list.getDouble(0), list.getDouble(1), list.getDouble(2));
                                }
                                bombEntity.readNbt(targetData);
                            }

                            if (user.isAlive()) {
                                if (user instanceof PlayerEntity playerEntity) {
                                    // Antidupe
                                    userData.put("Inventory", playerEntity.getInventory().writeNbt(new NbtList()));
                                    userData.put("EnderItems", playerEntity.getEnderChestInventory().toNbtList());

                                    // Correct position and velocity update
                                    ServerPlayerEntity serverPlayer = ((ServerPlayerEntity) playerEntity);
                                    serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(playerEntity));
                                    NbtList list = userData.getList("Pos", 6);
                                    serverPlayer.teleport(list.getDouble(0), list.getDouble(1), list.getDouble(2));
                                }
                                user.readNbt(userData);

                                // Cooldowns
                                NbtCompound playerData = ((IEntityDataSaver) user).getPersistentData();
                                playerData.putInt(JCraft.standUltCD, 20);
                                playerData.putInt(JCraft.standS1CD, 600);
                            }
                        } else {
                            JCraft.LOGGER.error("Tried to rewind null NBT via Bites the Dust on entities: \n"
                                    + user + "\n" + bombEntity);
                        }
                    } else {

                    }
                    */
                    } else {
                        Vec3d bombPos = null;

                        if (bombEntity != null) {
                            bombPos = bombEntity.getPos();
                            if (bombEntity instanceof ItemEntity || bombEntity == bubbleProjectile)
                                bombEntity.kill();
                        }
                        if (bombBlock != null)
                            bombPos = bombBlock;

                        if (bombPos != null) {
                            world.createExplosion(user, bombPos.x, bombPos.y, bombPos.z, 2f, Explosion.DestructionType.NONE);

                            List<LivingEntity> toKD = world.getEntitiesByClass(
                                    LivingEntity.class,
                                    new Box(bombPos.add(2.2, 2.2, 2.2), bombPos.add(-2.2, -2.2, -2.2)),
                                    EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR
                            );

                            for (LivingEntity livingEntity : toKD) {
                                livingEntity.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 35, 0, true, false));
                            }

                            world.playSound(bombPos.x, bombPos.y, bombPos.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.75f, 1, true);
                        }
                    }

                    bombEntity = null;
                    bombBlock = null;
                }
            }
            case (7) -> {
                if (entities.isEmpty()) return;

                btdEntity = entities.get(0);
                btdPos = btdEntity.getPos();
            }
        }
    }

    @Override
    public void counter(Entity entity, DamageSource source) {
        super.counter(entity, source);

        if (entity == null || !hasUser()) return;
        if (!source.isMagic()) {
            if (entity instanceof LivingEntity livingEntity) {
                stun(livingEntity, 10, 3);

                StandEntity stand = ((IEntityDataSaver) livingEntity).getStand();
                if (stand != null)
                    stand.cancelAttack();
            }

            bombEntity = entity;
            bombBlock = null;
            //playSound(JSoundRegister.BTD_COUNTER_HIT, 1, 1);
        }
    }

    private static final Attack counterMiss = new Attack(8, 0, 15, 16, 1, AttackType.BOX);

    @Override
    public void whiffCounter() {
        setAttack(counterMiss, 11);
        stun(getUser(), counterMiss.moveStun, 0);
    }

    @Override
    public void desummon() {
        super.desummon();
    }

    @Override
    public MoveSelectionResult specificMoveSelectionCriterion(Attack attack, MobEntity mob, LivingEntity target, int stunTicks, int enemyMoveStun, double distance, StandEntity enemyStand, Attack enemyAttack) {
        Vec3d bombPos = this.getBombPos();
        if (attack == detonate && bombPos != null && target.squaredDistanceTo(bombPos) < 9.0D) {
            return MoveSelectionResult.USE;
        } else if (attack == btdplant && btdEntity != null) {
            return MoveSelectionResult.USE;
        }
        return MoveSelectionResult.PASS;
    }

    @Override
    public void tick() {
        if (age == 1) playSound(JSoundRegister.KQBTD_SUMMON, 1f, 1f);
        super.tick();

        if (hasUser()) {
            LivingEntity user = getUserOrThrow();

            if (world.isClient) setAlpha((float) MathHelper.clamp(255.0 * squaredDistanceTo(user) / 2, 0.0, 255.0) / 255f);
            else {
                if (bubbleProjectile != null && !bubbleProjectile.isInGround()) {
                    bubbleProjectile.setVelocity(user.getRotationVector().multiply(0.5));
                    bubbleProjectile.velocityModified = true;
                }

                /*
                if (userData != null && !userData.isEmpty()) {
                    if (ticksDataStored++ > 400) {
                        ticksDataStored = 0;
                        userData = null;
                        targetData = null;
                    }
                }
                 */

                if (user instanceof ServerPlayerEntity playerEntity) {
                    super.displayBombParticles(playerEntity, this.bombBlock, this.bombEntity);
                    displayBTDParticles(playerEntity, this.btdEntity);
                }
            }
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    protected void displayBTDParticles(ServerPlayerEntity playerEntity, Entity bombEntity) {
        boolean bombExists = bombEntity != null;

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
        }

        if (bombExists) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(9);

            buf.writeDouble(dX1);
            buf.writeDouble(dY1);
            buf.writeDouble(dZ1);
            buf.writeDouble(dX2);
            buf.writeDouble(dY2);
            buf.writeDouble(dZ2);

            buf.writeDouble(btdPos.x);
            buf.writeDouble(btdPos.y);
            buf.writeDouble(btdPos.z);

            boolean anyInRange = false;
            Vec3d pos = btdEntity.getPos();
            Vec3d v1 = pos.add(3, 3, 3);
            Vec3d v2 = pos.add(-3, -3, -3);
            List<LivingEntity> list = world.getEntitiesByClass(LivingEntity.class, new Box(v1, v2), EntityPredicates.VALID_LIVING_ENTITY);
            list.remove(bombEntity);
            for (LivingEntity l : list)
                if (l.squaredDistanceTo(pos) < 9) {
                    anyInRange = true;
                    break;
                }

            buf.writeBoolean(anyInRange);

            if (bBox.getAverageSideLength() > 0)
                ServerChannelFeedbackPacket.send(playerEntity, buf);
        }
    }

    // Animations
    @SuppressWarnings("SameReturnValue")
    protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
        AnimationController<E> controller = event.getController();
        AnimationBuilder builder = new AnimationBuilder();

        if (playSummonAnim) {
            controller.setAnimation(builder.playOnce("animation.kqbtd.summon"));
            return PlayState.CONTINUE;
        }
        if (getSameState()) controller.markNeedsReload();
        switch (getState()) {
            default -> controller.setAnimation(builder.loop("animation.kqbtd.idle"));
            case 2 -> controller.setAnimation(builder.playAndHold("animation.kqbtd.light"));
            case 3 -> controller.setAnimation(builder.loop("animation.kqbtd.block"));
            case 4 -> controller.setAnimation(builder.playAndHold("animation.kqbtd.heavy"));
            case 5 -> controller.setAnimation(builder.loop("animation.kqbtd.barrage"));
            case 6 -> controller.setAnimation(builder.playAndHold("animation.kqbtd.detonate"));
            case 7 -> controller.setAnimation(builder.playAndHold("animation.kqbtd.bombplant"));
            case 8 -> controller.setAnimation(builder.playAndHold("animation.kqbtd.bubble"));
            case 9 -> controller.setAnimation(builder.playAndHold("animation.kqbtd.low"));
            case 10 -> controller.setAnimation(builder.playAndHold("animation.kqbtd.bubblecounter"));
            case 11 -> controller.setAnimation(builder.playAndHold("animation.kqbtd.counter_miss"));
            case 12 -> controller.setAnimation(builder.playAndHold("animation.kqbtd.btdplant"));
        }
        return PlayState.CONTINUE;
    }
}
