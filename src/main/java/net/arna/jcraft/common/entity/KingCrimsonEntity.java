package net.arna.jcraft.common.entity;

import io.netty.buffer.Unpooled;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.network.s2c.ShaderActivationPacket;
import net.arna.jcraft.common.network.s2c.ShaderDeactivationPacket;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.JPacketRegistry;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class KingCrimsonEntity extends StandEntity {
    public static final Attack light = new Attack(0, 3, 0.85f, 23, 0, 1.5, 4f, 0.1f, AttackType.MULTIHIT, 2f, -0.1f, List.of(10, 16), JSoundRegister.IMPACT_4)
            .setInfo("Dual Chop", "quick combo starter");
    public static final Attack barrage = new Attack(3, 17, 0.85f, 50, 0, 1.5, 1f, 0.1f, AttackType.BARRAGE, 1, 0, 3)
            .setInfo("Barrage", "fast reliable combo starter/extender/finisher, medium stun, knocks back");
    public static final Attack overhead = new Attack(2, 8, 0.85f, 32, 22, 2, 9f, 1.5f, AttackType.BOX, 0.55f)
            .setHitspark(2)
            .hyperArmor()
            .setLaunch()
            .setInfo("Overhead Hook", "long windup, knockdown", AttackQueue.HEAVY);
    public static final Attack heavy = new Attack(1, 13, 0.85f, 19, 12, 1.5, 6f, 0.2f, AttackType.BOX, 1.25f, 0, 0)
            .appendHitbox(new Attack.HitboxData(0, 0.5, 1))
            .setInfo("Vertical Chop", "medium windup combo starter, has a true followup in the form of a slow, armored knockdown", AttackQueue.HEAVY)
            .setFollowup(overhead);
    public static final Attack bloodthrow = new Attack(5, 25, 15, 10, 10, AttackType.BOX)
            .setRanged(true)
            .setInfo("Blood Throw", "");
    public static final Attack eyechop = new Attack(4, 20, 1f, 50, 37, 1.75, 9f, 0.3f, AttackType.BOX, 3, -0.3f)
            .setHitspark(2)
            .appendHitbox(new Attack.HitboxData(0, 0.5, 1))
            .crouchingVariation(bloodthrow)
            .setInfo("Eye Chop/Blood Throw", "blindness on hit, donut combo extender/crouch to throw a stunning, blinding blood projectile");
    public static final Attack donut = new Attack(6, 15, 1f, 60, 42, 1.75, 14f, 0.0f, AttackType.BOX, 4, 0.1f)
            .setHitspark(2)
            .hyperArmor()
            .setInfo("Donut", "huge windup, 4s hitstun");
    public static final Attack prediction = new Attack(9, 30, 104, 4, 0, -1, AttackType.BOX)
            .setInfo("Prediction", "");
    public static final Attack epitaph = new Attack(7, 30, 34, 4, 0, -1, AttackType.COUNTER)
            .crouchingVariation(prediction)
            .setInfo("Prediction/Epitaph/Move Cancel", """
                    standing: shows future location of nearby entities, said entities can be forced into it using Time Erase (20s cooldown)
                              you are slowed down while predicting
                    crouching: 0.2s windup, 1.5s counter
                    during a move: cancels it (puts Time Erase on a 7 second cooldown but doesn't require it to be usable)""");
    public static final Attack timeerase = new Attack(8, 50, 15, 5, 6, AttackType.BOX)
            .setInfo("Time Erase", "6 seconds duration, cancellable by doing anything with King Crimson"); // TE = (moveStun-initTime)/20

    public static final TrackedData<Integer> TIMEERASETIME;
    private final Map<Entity, Vec3d> predictionInfo = new WeakHashMap<>();

    public KingCrimsonEntity(World worldIn) {
        super(StandType.KING_CRIMSON, worldIn);
        super.initialize();

        idleDistance = 1f;
        idleRotation = -65f;

        ignoreCameraFrustum = true;

        description = "Close-Range Deadly STRIKER";

        pros = List.of(
                "high damage output",
                "priceless move cancel",
                "counter",
                "easy setups"
        );

        cons = List.of(
                "below average speed",
                "slow, limited armored options",
                "limited pressure",
                "hard to master"
        );

        freespace = """
                BNBs:
                    the gamer (THE bnb)
                    M1>Barrage>delay.Move Cancel>M1>Heavy~Overhead
                    
                    the loop zoopler (sub optimal damage for a setup that kills them if you guess right)
                    Eye Chop>Donut>M1>Heavy~Overhead>Time Erase
                    
                    the red racist (death)
                    Donut>M1>Eye Chop>M1>Barrage>
                    ...Move Cancel>M1>Heavy~Overhead
                    ...Time Erase""";

        moves = List.of(light, heavy, barrage, eyechop, timeerase, donut, epitaph, timeskip);
    }

    static {
        TIMEERASETIME = DataTracker.registerData(KingCrimsonEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    public int getTETime() {
        return this.dataTracker.get(TIMEERASETIME);
    }

    public void setTETime(int teTime) {
        this.dataTracker.set(TIMEERASETIME, teTime);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.getDataTracker().startTracking(TIMEERASETIME, 0);
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!canAttack()) return;
        if (handleAttack(light, JCraft.standLightCD, 2))
            playSound(JSoundRegister.KC_DUAL_CHOP, 1, 1);
    }

    @Override
    public void initHeavyAttack() {
        if (hasUser()) {
            if (getUser().hasStatusEffect(JStatusRegister.DAZED))
                return;
            boolean idling = getMoveStun() < 1;

            if (curAttack != heavy) {
                if (idling && handleAttack(heavy, JCraft.standHeavyCD, 10)) {
                    playSound(JSoundRegister.KC_HEAVY, 1, 1);
                }
            } else if (getMoveStun() < 7) {
                setAttack(overhead, 4);
                playSound(JSoundRegister.KC_HEAVY2, 1, 1);
            }
        }
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleAttack(barrage, JCraft.standBarrageCD, 6))
            playSound(JSoundRegister.KC_BARRAGE, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (!canAttack()) return;
        if (getUser().isSneaking() && handleAttack(bloodthrow, JCraft.standS1CD, 11)) {
            getUser().damage(DamageSource.MAGIC, 0.1f);
        } else if (handleAttack(eyechop, JCraft.standS1CD, 7)) {
            playSound(JSoundRegister.KC_EYE_CHOP, 1, 1);
        }
    }

    private void beginPrediction() {
        if (!(getUser() instanceof ServerPlayerEntity player)) return;

        for (Entity entity : KingCrimsonEntity.getEntitiesToCatch(world, this, player))
            predictionInfo.put(entity, entity.getPos());
        
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(true);
        buf.writeVarInt(prediction.moveStun - prediction.initTime);
        ServerPlayNetworking.send(player, JPacketRegistry.S2C_TIME_ERASE_PREDICTION_STATE, buf);
    }

    private void finishPrediction() {
        for (Map.Entry<Entity, Vec3d> prediction : predictionInfo.entrySet()) {
            Entity entity = prediction.getKey();
            if (entity == null) continue;
            
            Vec3d pos = prediction.getValue();
            entity.teleport(pos.x, pos.y, pos.z);
        }

        if (getUser() instanceof ServerPlayerEntity player) {
            ServerPlayNetworking.send(player, JPacketRegistry.S2C_EPITAPH_STATE, new PacketByteBuf(Unpooled.buffer().writeBoolean(false)));
            ServerPlayNetworking.send(player, JPacketRegistry.S2C_TIME_ERASE_PREDICTION_STATE, new PacketByteBuf(Unpooled.buffer().writeBoolean(false)));
        }

        predictionInfo.clear();
        moveCancel();
    }

    @Override
    public void initUlt() {
        // If predicting, and Time Erase isn't on cooldown
        if (curAttack != null && curAttack.id == 9) {
            NbtCompound playerData = ((IEntityDataSaver) getUser()).getPersistentData();
            if (playerData.getInt(JCraft.standUltCD) <= 0) {
                playerData.putInt(JCraft.standUltCD, 400);
                finishPrediction();
            }
        }

        // If not predicting, do other Time Erase logic
        if (!canAttack())
            return;

        if (getTETime() > 0) {
            cancelTE();
            return;
        }

        if (handleAttack(timeerase, JCraft.standUltCD, 8)) {
            if (getUser() instanceof ServerPlayerEntity player)
                player.networkHandler.sendPacket(new PlaySoundS2CPacket(JSoundRegister.TIME_ERASE, SoundCategory.PLAYERS, getX(), getY(), getZ(), 1, 1, 0));
        }
    }

    @Override
    public void initSpecial2() {
        if (!canAttack()) return;
        if (handleAttack(donut, JCraft.standS2CD, 5))
            playSound(JSoundRegister.KC_DONUT, 1, 1);
    }

    @Override
    public void initSpecial3() {
        if (hasUser()) {
            LivingEntity user = this.getUser();
            ITimeStop timeStop = (ITimeStop) user;
            if (user.hasStatusEffect(JStatusRegister.DAZED) || timeStop.getTimeStopTicks() > 0)
                return;

            NbtCompound playerData = ((IEntityDataSaver) user).getPersistentData();
            boolean start = getMoveStun() < 1;

            if (start) {
                if (user.isSneaking())
                    handleAttack(epitaph, JCraft.standS3CD, 9);
                else if (handleAttack(prediction, JCraft.standS3CD, 12)) {
                    predictionInfo.clear();
                    playSound(JSoundRegister.KC_EPITAPH, 1, 1);

                    // Send epitaph state start
                    if (user instanceof ServerPlayerEntity player)
                        ServerPlayNetworking.send(player, JPacketRegistry.S2C_EPITAPH_STATE, new PacketByteBuf( Unpooled.buffer().writeBoolean(true)) );
                }
            } else {
                // When used during a move, cancels it and puts time erase on cooldown
                moveCancel();

                // 7 second time erase cooldown
                if (playerData.getInt(JCraft.standUltCD) < 140)
                    playerData.putInt(JCraft.standUltCD, 140);

                // Particle effects
                Vec3d oPos = user.getPos();
                Box bBox = user.getBoundingBox();
                for (ServerPlayerEntity serverPlayer : ((ServerWorld) world).getPlayers()) {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeVarInt(2);
                    buf.writeDouble(oPos.x);
                    buf.writeDouble(oPos.y);
                    buf.writeDouble(oPos.z);
                    buf.writeDouble(bBox.getXLength());
                    buf.writeDouble(bBox.getYLength());
                    buf.writeDouble(bBox.getZLength());
                    ServerChannelFeedbackPacket.send(serverPlayer, buf);
                }

                // Stop epitaph state
                if (user instanceof ServerPlayerEntity player)
                    ServerPlayNetworking.send(player, JPacketRegistry.S2C_EPITAPH_STATE, new PacketByteBuf( Unpooled.buffer().writeBoolean(false)) );
            }
        }
    }

    private static final Attack timeskip = new Attack(-2, 15, 2, 2)
            .setMobility(MobilityType.TELEPORT)
            .setInfo("Timeskip", "16m range");
    @Override
    public void initUtil() {
        if (!canAttack()) return;
        if (handleAttack(timeskip, JCraft.utilCD, 0)) {
            LivingEntity user = getUser();

            Vec3d pos = user.getPos();
            Box bBox = user.getBoundingBox();

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(2);
            buf.writeDouble(pos.x);
            buf.writeDouble(pos.y);
            buf.writeDouble(pos.z);
            buf.writeDouble(bBox.getXLength());
            buf.writeDouble(bBox.getYLength());
            buf.writeDouble(bBox.getZLength());

            PlayerLookup.world((ServerWorld) world).forEach(
                    serverPlayer -> ServerChannelFeedbackPacket.send(serverPlayer, buf)
            );
        }
    }

    private void moveCancel() {
        curAttack = null;
        queuedAttack = null;
        setMoveStun(2);
        setState(0); // Basically state 1, but runs logic once
    }

    private static final Attack barrageFinisher = new Attack(10, 17, 0.85f, 50, 0, 1.5, 1f, 1.1f, AttackType.BARRAGE, 0.5f, 0, 3)
            .setHitspark(2)
            .setLaunch()
            .setInfo("Barrage (Final Hit)", "");

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        switch (attack.id) {
            case (-2) -> timeSkip(16, JSoundRegister.TE_TP);
            case (2) -> {
                for (LivingEntity ent : entities)
                    ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 35, 0));
            }
            case (3) -> {
                if (getMoveStun() < 4)
                    curAttack = barrageFinisher;
            }
            case (4) -> {
                for (LivingEntity ent : entities)
                    ent.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0));
            }
            case (5) -> {
                LivingEntity user = getUser();
                BloodProjectile bloodProjectile = new BloodProjectile(world, user);
                bloodProjectile.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                bloodProjectile.setVelocity(user, user.getPitch(), user.getYaw(), 0, user.isSneaking() ? 1.33F : 0.66F, 0);
                bloodProjectile.setPosition(getEyePos());
                world.spawnEntity(bloodProjectile);
            }
            case (6) -> {
                // If hit, impale and set position to middle of arm
                for (LivingEntity entity : entities) {
                    Vec3d pos = getPos().add(getRotationVector().multiply(1.5));
                    entity.teleport(pos.x, entity.getY(), pos.z);
                }
            }
            case (8) -> {
                setTETime((int) (timeerase.stun * 20));

                curAttack = null;

                Vec3d pos = getEyePos();
                List<Entity> toCatch = world.getEntitiesByClass(Entity.class,
                        new Box(pos.add(96.0, 96.0, 96.0), pos.subtract(96.0, 96.0, 96.0)), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                LivingEntity user = getUser();
                toCatch.remove(this);
                toCatch.remove(user);

                if (user instanceof ServerPlayerEntity player) {
                    // Shader handling
                    ShaderActivationPacket.send(player, this, 0, 120, ShaderActivationPacket.Type.CRIMSON);

                    PlayerCloneEntity playerCloneEntity = new PlayerCloneEntity(PlayerCloneEntity.getCloneType(player), world);

                    playerCloneEntity.setShouldRenderForMaster(false);
                    playerCloneEntity.disableDrops();
                    playerCloneEntity.disableItemExchange();

                    // Copy properties
                    playerCloneEntity.setMaster(player);

                    doppelganger = playerCloneEntity;
                } else if (user instanceof MobEntity mob) { //Code sourced from MobEntity.class convertTo()
                    EntityType<?> entityType = mob.getType();
                    MobEntity newMob = (MobEntity) entityType.create(world);

                    if (newMob == null) {
                        JCraft.LOGGER.error("Failed to create King Crimson clone mob of type " + entityType + " in world " + world);
                        return;
                    }

                    // Copy properties
                    newMob.setBaby(mob.isBaby());
                    if (mob.hasCustomName()) {
                        newMob.setCustomName(mob.getCustomName());
                        newMob.setCustomNameVisible(mob.isCustomNameVisible());
                    }
                    newMob.age = mob.age;

                    newMob.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0);
                    newMob.setEquipmentDropChance(EquipmentSlot.OFFHAND, 0);

                    newMob.setEquipmentDropChance(EquipmentSlot.HEAD, 0);
                    newMob.setEquipmentDropChance(EquipmentSlot.CHEST, 0);
                    newMob.setEquipmentDropChance(EquipmentSlot.LEGS, 0);
                    newMob.setEquipmentDropChance(EquipmentSlot.FEET, 0);

                    doppelganger = newMob;
                }

                // Copy rotation
                doppelganger.copyPositionAndRotation(user);
                doppelganger.setHeadYaw(user.getHeadYaw());
                doppelganger.setBodyYaw(user.getBodyYaw());

                // Copy equipment
                doppelganger.equipStack(EquipmentSlot.MAINHAND, user.getMainHandStack().copy());
                doppelganger.equipStack(EquipmentSlot.OFFHAND, user.getOffHandStack().copy());
                doppelganger.equipStack(EquipmentSlot.HEAD, user.getEquippedStack(EquipmentSlot.HEAD).copy());
                doppelganger.equipStack(EquipmentSlot.CHEST, user.getEquippedStack(EquipmentSlot.CHEST).copy());
                doppelganger.equipStack(EquipmentSlot.LEGS, user.getEquippedStack(EquipmentSlot.LEGS).copy());
                doppelganger.equipStack(EquipmentSlot.FEET, user.getEquippedStack(EquipmentSlot.FEET).copy());

                // Copy health and make immortal
                doppelganger.setHealth(user.getHealth());
                doppelganger.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 32767, 9, true, false));

                // Set and summon King Crimson replica, make it block forever
                summonFakeKC();

                // Look at enemy
                doppelganger.setTarget(user.getAttacker());

                world.spawnEntity(doppelganger);
            }
            case (9) -> beginPrediction();
        }
    }

    @Override
    public void desummon() {
        if (this.getTETime() < 1)
            super.desummon();
    }

    @Override
    public void counter(Entity entity, DamageSource source) {
        super.counter(entity, source);
        if (entity == null || !hasUser())
            return;
        LivingEntity user = getUser();

        Vec3d ePos = entity.getPos();
        if (!entity.isInsideWall()) {
            Vec3d uPos = user.getPos();

            entity.teleport(uPos.x, uPos.y, uPos.z);
            user.teleport(ePos.x, ePos.y, ePos.z);
        }

        user.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, entity.getEyePos());

        if (entity instanceof LivingEntity livingEntity) {
            stun(livingEntity, 20, 0);
            if (entity.getFirstPassenger() instanceof StandEntity stand)
                stand.cancelAttack();
        }

        world.playSound(null, ePos.x, ePos.y, ePos.z, JSoundRegister.TE_TP, SoundCategory.PLAYERS, 1f, 1f);
    }

    private static final Attack counterMiss = new Attack(8, 0, 20, 21, -1, AttackType.BOX);
    @Override
    public void whiffCounter() {
        setAttack(counterMiss, 13);
        stun(getUser(), counterMiss.moveStun, 0);
        playSound(JSoundRegister.KC_RAGE, 1, 1);
    }

    @Override
    protected Box calculateBoundingBox() {
        if (getTETime() > 0) {
            double x = getX();
            double y = getY();
            double z = getZ();
            return new Box(x, y, z, x, y + 0.1, z);
        }
        return super.calculateBoundingBox();
    }

    private MobEntity doppelganger;
    private void summonFakeKC() {
        ((IEntityDataSaver) doppelganger).getPersistentData().putInt("StandID", 3);
        StandEntity kingCrimson = JCraft.summon(world, doppelganger);
        kingCrimson.blocking = true;
        kingCrimson.setMoveStun(32767);
        kingCrimson.setSilent(true);
    }
    private void cancelTE() {
        LivingEntity user = getUser();
        NbtCompound userData = ((IEntityDataSaver) user).getPersistentData();
        userData.putInt(JCraft.standUltCD, userData.getInt(JCraft.standUltCD) - getTETime() * 2);
        setTETime(0);
        doppelganger.discard();
        if (user instanceof ServerPlayerEntity serverPlayer)
            ShaderDeactivationPacket.send(serverPlayer, ShaderActivationPacket.Type.CRIMSON);
    }

    @Override
    public void tick() {
        if (age == 1) playSound(JSoundRegister.KC_SUMMON, 1f, 1f);
        super.tick();

        LivingEntity user = this.getUser();
        if (user == null) return;
        Attack attack = curAttack;

        boolean userIsPlayer = false;
        ServerPlayerEntity playerEntity = null;
        if (user instanceof ServerPlayerEntity serverPlayer) {
            userIsPlayer = true;
            playerEntity = serverPlayer;
        }

        if (getState() == 12) {
            if (getMoveStun() == prediction.moveStun - prediction.initTime) beginPrediction(); // Clientside prediction, serverside is in specialAttack()

            if (age % 2 == 0) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 10, 2, true, false));

                updatePredictions(predictionInfo.entrySet(), getMoveStun());
            }
        }

        if (!world.isClient()) {
            if (attack != null) {
                if (attack.id == overhead.id)
                    this.queuedAttack = null;
            }

            // Handle time erase
            int teTime = this.getTETime();
            if (teTime > 0) {
                setTETime(teTime - 1);

                if (blocking)
                    cancelTE();

                if (attack != null)
                    if (getMoveStun() < (attack.moveStun - attack.realInitTime() * 2 / 3))
                        cancelTE();

                // Invulnerability and invisibility
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 10, 9, true, false));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 10, 0, true, false));
                // Inability to be stunned
                user.removeStatusEffect(JStatusRegister.DAZED);
                // Inability to be hit (by projectiles)
                Box noBox = new Box(0, 0, 0, 0, 0, 0);
                user.setBoundingBox(noBox);
                user.noClip = true;

                if (getTETime() < 1) {
                    // Play exit noise
                    if (userIsPlayer)
                        playerEntity.networkHandler.sendPacket(new PlaySoundS2CPacket(JSoundRegister.TIME_ERASE_EXIT, SoundCategory.PLAYERS, getX(), getY(), getZ(), 1, 1, 0));

                    // Doppelgänger disappears at the end of Time Erase
                    doppelganger.discard();

                    /* Return targets to position
                    for (TimeEraseData timeEraseData : timeEraseInfo) {
                        Vec3d tePos = timeEraseData.getPosition();
                        timeEraseData.getEntity().teleport(tePos.x, tePos.y, tePos.z);
                    }
                     */
                }
            } else {
                setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(user) / 2, 0.0, 255.0) / 255f);
            }

            setSilent(teTime > 0);

            if (user.hasCustomName())
                user.setCustomNameVisible(teTime <= 0);
        }
    }

    public static List<Entity> getEntitiesToCatch(World world, StandEntity stand, PlayerEntity player) {
        if (world == null || stand == null) return List.of();

        return world.getEntitiesByClass(Entity.class, stand.getBoundingBox().expand(64),
                EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e -> e != stand && e != player));
    }

    public static void updatePredictions(Set<Map.Entry<Entity, Vec3d>> predictionsSet, int ticksLeft) {
        for (Map.Entry<Entity, Vec3d> prediction : predictionsSet) {
            Entity entity = prediction.getKey();
            if (entity == null || !entity.isAlive()) continue;
            World world = entity.getWorld();

            Vec3d currentPos = entity.getPos().add(0, 0.1, 0);
            Vec3d futurePos = currentPos;
            boolean changed = false;

            // If in air and not in a liquid, account for drop
            if (!entity.isOnGround() && !entity.isSubmergedInWater() && !entity.isInLava()) {
                //JCraft.LOGGER.info("Target is in air");
                futurePos = futurePos.add(0, (-9.81 / 400) * ticksLeft * ticksLeft, 0);
                changed = true;
            }

            // If moving faster than 0.01 m/s, account for distance traveled
            Vec3d velocity = entity.getVelocity();
            if (entity instanceof ServerPlayerEntity player) // EXTREMELY cursed implementation of player velocity because NOTHING ELSE WORKS
                velocity = ((IEntityDataSaver) player).getDesiredVelocity();
            //JCraft.LOGGER.info("Target is moving at a velocity of: " + velocity);
            if (velocity.lengthSquared() > 0.0001) {
                Vec3d velocityComp = new Vec3d(velocity.x * ticksLeft, Math.max(0, velocity.y * ticksLeft), velocity.z * ticksLeft);
                //JCraft.LOGGER.info("Modified velocity: " + velocityComp);
                futurePos = futurePos.add(velocityComp);
                changed = true;
            }

            // Collision check between current and extrapolated future position
            if (changed) {
                //JCraft.LOGGER.info("Predicted position changed, time left: " + timeLeft);
                BlockHitResult hitResult = world.raycast(new RaycastContext(currentPos, futurePos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.SOURCE_ONLY, entity));
                prediction.setValue(hitResult.getPos());
            }
        }
    }


    // Animations
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
            controller.setAnimation(builder.playOnce("animation.kingcrimson.summon"));
            return PlayState.CONTINUE;
        }
        if (getSameState()) controller.markNeedsReload();
        switch (this.getState()) {
            default -> controller.setAnimation(builder.loop("animation.kingcrimson.idle"));
            case 2 -> controller.setAnimation(builder.playAndHold("animation.kingcrimson.dual_chop"));
            case 3 -> controller.setAnimation(builder.loop("animation.kingcrimson.block"));
            case 4 -> controller.setAnimation(builder.playAndHold("animation.kingcrimson.overhead"));
            case 5 -> controller.setAnimation(builder.playAndHold("animation.kingcrimson.donut"));
            case 6 -> controller.setAnimation(builder.loop("animation.kingcrimson.barrage"));
            case 7 -> controller.setAnimation(builder.playAndHold("animation.kingcrimson.eye_chop"));
            case 8 -> controller.setAnimation(builder.playAndHold("animation.kingcrimson.time_erase"));
            case 9 -> controller.setAnimation(builder.playAndHold("animation.kingcrimson.epitaph"));
            case 10 -> controller.setAnimation(builder.playAndHold("animation.kingcrimson.heavy"));
            case 11 -> controller.setAnimation(builder.playAndHold("animation.kingcrimson.bloodthrow"));
            case 12 -> controller.setAnimation(builder.playAndHold("animation.kingcrimson.predict"));
            case 13 -> controller.setAnimation(builder.playAndHold("animation.kingcrimson.counter_miss"));
        }
        return PlayState.CONTINUE;
    }
}
