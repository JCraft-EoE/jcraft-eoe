package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.attack.AttackType;
import net.arna.jcraft.common.component.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.projectile.BubbleProjectile;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class KQBTDEntity extends AbstractKillerQueenEntity<KQBTDEntity, KQBTDEntity.State> {
    public static final Attack heavy = new Attack(2, 12, 0.75f, 9, 5, 1, 7.5f, 1.1f, AttackType.BOX, 0.5f, 0, 0, JSoundRegistry.IMPACT_4)
            .setHitspark(2)
            .setLaunch()
            .setInfo("Elbow", "fast, short-range knockback");
    public static final Attack barrage = Attack.copyOf(KillerQueenEntity.barrage);
    public static final Attack bombplant = Attack.copyOf(KillerQueenEntity.bombplant);
    public static final Attack bubblecounter = new Attack(7, 27, 20, 5, 0, 1, AttackType.COUNTER)
            .setInfo("Stray Cat Counter", "0.25s windup counter, turns opponent into your primary bomb");
    public static final Attack bubble = new Attack(5, 23, 0.75f, 18, 15, 0, 0f, 0.0f, AttackType.BOX)
            .setRanged(true)
            .crouchingVariation(bubblecounter)
            .setInfo("Stray Cat Bubble", "launches an explosive bubble");
    public static final Attack detonate = new Attack(6, 1, 0.75f, 6, 5, 0, 0f, 0.0f, AttackType.BOX)
            .setInfo("Detonate", "crouch with a bomb planted within 20s on a living being to activate Bites the Dust");
    public static final Attack btdplant = new Attack(7, 50, 1, 24, 14, 1.5, 0f, 0.0f, AttackType.BOX, 0.5f)
            .setUB(true)
            .setBlockstun(8)
            .setInfo("Bites the Dust Plant", "press the same button to detonate, sending the affected enemy back to their ");
    private static final Attack counterMiss = new Attack(8, 0, 15, 16, 1, AttackType.BOX);

    private BubbleProjectile bubbleProjectile;
    private Entity btdEntity;
    private Vec3d btdPos;
    private boolean detonateBTD = false;

    //private NbtCompound userData;
    //private NbtCompound targetData;

    public KQBTDEntity(World worldIn) {
        super(StandType.KILLER_QUEEN_BITES_THE_DUST, State.class, worldIn);

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

        freespace = """
                    BNBs:
                    the kitty cat
                    M1~Low>Barrage>Bomb Plant/Bites the Dust Plant
                    
                    the ol razzle dazzle
                    (Already bomb planted) M1~Low>Barrage>M1>Elbow>Detonate""";

        moves = List.of(KillerQueenEntity.light, heavy, KillerQueenEntity.barrage, KillerQueenEntity.bombplant, Attack.unusable, bubble, btdplant
                , new Attack().setMobility(MobilityType.DASH).setInfo("Explosive Dash", "slight aoe damage, 3D movement tool"));

        super.initialize();
    }

    @Override
    protected void detonate() {
        super.detonate();
        detonateBTD = false;
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;
        if (handleAttack(heavy, CooldownType.STAND_HEAVY, State.HEAVY))
            playSound(JSoundRegistry.KQBTD_ELBOW, 1, 1);
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleAttack(barrage, CooldownType.STAND_BARRAGE, State.BARRAGE))
            playSound(JSoundRegistry.KQ_BARRAGE, 1, 1);
    }

    private static final int bombplantCD = (int) (bombplant.cooldown * 20);
    @Override
    public void initSpecial1() {
        if (!canAttack() || !hasUser()) return;
        LivingEntity user = getUserOrThrow();
        CooldownsComponent cooldowns = JComponents.getCooldowns(user);

        if (user.isInSneakingPose() && cooldowns.getCooldown(CooldownType.STAND_SP1) < 1) {
            BlockPos downBlock = user.getBlockPos().down();
            boolean notAir = (world.getBlockState(downBlock).getBlock() != Blocks.AIR && world.getBlockState(downBlock).getBlock() != Blocks.CAVE_AIR && world.getBlockState(downBlock).getBlock() != Blocks.VOID_AIR);
            if (notAir) {
                this.bombEntity = null;
                this.bombBlock = user.getPos().add(0, -0.5, 0);
                cooldowns.setCooldown(CooldownType.STAND_SP1, bombplantCD);
            }
        } else {
            handleAttack(bombplant, CooldownType.STAND_SP1, State.BOMB_PLANT);
            this.bombBlock = null;
        }
    }

    @Override
    public void initUlt() {

    }

    @Override
    public void initSpecial2() {
        if (!canAttack() || !hasUser()) return;
        if (getUserOrThrow().isSneaking() && handleAttack(bubblecounter, CooldownType.STAND_SP2, State.BUBBLE_COUNTER)) {
            //playSound(JSoundRegister.KQBTD_COUNTER, 1, 1);
        } else if (handleAttack(bubble, CooldownType.STAND_SP2, State.BUBBLE))
            playSound(JSoundRegistry.KQ_UPPERCUT, 1, 1);
    }

    @Override
    public void initSpecial3() {
        if (btdEntity != null && handleAttack(detonate, CooldownType.STAND_ULT, State.DETONATE)) {
            playSound(JSoundRegistry.KQ_DETONATE, 1, 1);
            detonateBTD = true;
            return;
        }
        if (!canAttack()) return;
        if (handleAttack(btdplant, CooldownType.STAND_SP3, State.BTD_PLANT)) {
            //playSound(JSoundRegister.KQ_UPPERCUT, 1 ,1);
        }
    }

    @Override
    public void specialAttack(Attack attack, Set<LivingEntity> entities) {
        LivingEntity user = this.getUser();
        switch (attack.id) {
            case (2) -> {
                for (LivingEntity ent : entities)
                    ent.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 5, 4, true, false));
            }
            case (4) -> {
                if (user == null) return;

                if (entities.isEmpty()) { // If none are found, re-do an optimized hitbox check for any entity type
                    Vec3d rotVec = getRotationVector();
                    Vec3d boxCenter = getPos().add(0, user.getHeight() / 2, 0).add(rotVec);
                    Vec3d halfBox = new Vec3d(0.5, 0.5, 0.5);
                    List<Entity> hit = world.getEntitiesByClass(Entity.class,
                            new Box(boxCenter.subtract(halfBox), boxCenter.add(halfBox)),
                            EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                    hit.remove(this);
                    hit.remove(user);

                    if (!hit.isEmpty()) {
                        bombEntity = hit.get(0);
                        bombBlock = null;
                    }
                } else { // Living entities take priority
                    bombEntity = JUtils.getUserIfStand(entities.stream().findFirst().orElseThrow());
                    bombBlock = null;

                    /*
                    targetData = new NbtCompound();
                    userData = new NbtCompound();

                    bombEntity.writeNbt(targetData);
                    user.writeNbt(userData);
                     */
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
                        livingEntity.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 35, 0, true, false));

                        Vec3d pos = btdEntity.getPos();
                        JCraft.createParticle((ServerWorld) getWorld(), pos.x, pos.y + 2, pos.z, -4);
                        Vec3d v1 = pos.add(3, 3, 3);
                        Vec3d v2 = pos.add(-3, -3, -3);
                        List<LivingEntity> list = world.getEntitiesByClass(LivingEntity.class, new Box(v1, v2),
                                EntityPredicates.VALID_LIVING_ENTITY.and(e -> e != user.getVehicle() && e != user && e != this && e != btdEntity));

                        for (LivingEntity l : list)
                            if (l.squaredDistanceTo(pos) < 9) {
                                if (l.squaredDistanceTo(pos) < 2.25)
                                    world.createExplosion(user, l.getX(), l.getY() + l.getHeight() / 2, l.getZ(), 1.5f, Explosion.DestructionType.NONE);
                                else world.createExplosion(user, l.getX(), l.getY() + l.getHeight() / 2, l.getZ(), 1f, Explosion.DestructionType.NONE);
                            }

                        livingEntity.teleport(btdPos.x, btdPos.y, btdPos.z);
                        btdEntity = null;
                    }
                } else {
                    if (bombEntity instanceof LivingEntity livingEntity) {
                        explode(user, livingEntity.getPos());
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

                        if (bombPos != null)
                            explode(user, bombPos);
                    }

                    bombEntity = null;
                    bombBlock = null;
                }
            }
            case (7) -> {
                if (entities.isEmpty()) return;

                btdEntity = JUtils.getUserIfStand(entities.stream().findFirst().orElseThrow());
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

                StandEntity<?, ?> stand = JUtils.getStand(livingEntity);
                if (stand != null)
                    stand.cancelAttack();
            }

            bombEntity = entity;
            bombBlock = null;
            //playSound(JSoundRegister.BTD_COUNTER_HIT, 1, 1);
        }
    }

    @Override
    public void whiffCounter() {
        setAttack(counterMiss, State.COUNTER_MISS);
        stun(getUser(), counterMiss.moveStun, 0);
    }

    @Override
    public void desummon() {
        super.desummon();
    }

    @Override
    public MoveSelectionResult specificMoveSelectionCriterion(Attack attack, MobEntity mob, LivingEntity target, int stunTicks, int enemyMoveStun, double distance, StandEntity<?, ?> enemyStand, Attack enemyAttack) {
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
        if (age == 1) playSound(JSoundRegistry.KQBTD_SUMMON, 1f, 1f);
        super.tick();

        if (hasUser()) {
            LivingEntity user = getUserOrThrow();

            if (world.isClient) return;

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

    private void displayBTDParticles(ServerPlayerEntity playerEntity, Entity bombEntity) {
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
            List<LivingEntity> list = world.getEntitiesByClass(LivingEntity.class, new Box(v1, v2),
                    EntityPredicates.VALID_LIVING_ENTITY.and(e -> e != bombEntity));
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
    public enum State implements StandAnimationState<KQBTDEntity> {
        IDLE(builder -> builder.loop("animation.kqbtd.idle")),
        LIGHT(builder -> builder.playAndHold("animation.kqbtd.light")),
        BLOCK(builder -> builder.loop("animation.kqbtd.block")),
        HEAVY(builder -> builder.playAndHold("animation.kqbtd.heavy")),
        BARRAGE(builder -> builder.loop("animation.kqbtd.barrage")),
        DETONATE(builder -> builder.playAndHold("animation.kqbtd.detonate")),
        BOMB_PLANT(builder -> builder.playAndHold("animation.kqbtd.bombplant")),
        BUBBLE(builder -> builder.playAndHold("animation.kqbtd.bubble")),
        LOW(builder -> builder.playAndHold("animation.kqbtd.low")),
        BUBBLE_COUNTER(builder -> builder.playAndHold("animation.kqbtd.bubblecounter")),
        COUNTER_MISS(builder -> builder.playAndHold("animation.kqbtd.counter_miss")),
        BTD_PLANT(builder -> builder.playAndHold("animation.kqbtd.btdplant"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(KQBTDEntity stand, AnimationBuilder builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @NotNull String getSummonAnimation() {
        return "animation.kqbtd.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }

    @Override
    protected State getLightState() {
        return State.LIGHT;
    }

    @Override
    protected State getLowState() {
        return State.LOW;
    }

    @Override
    protected State getDetonateState() {
        return State.DETONATE;
    }
}
