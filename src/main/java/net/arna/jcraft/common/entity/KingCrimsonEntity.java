package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.network.s2c.ShaderActivationPacket;
import net.arna.jcraft.common.network.s2c.ShaderDeactivationPacket;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.HitResult;
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

import java.util.LinkedList;
import java.util.List;

public class KingCrimsonEntity extends StandEntity implements IAnimatable, IAnimationTickable {
    public static final Attack light = new Attack(0, 3, 0.85f, 23, 0, 1.5, 4f, 0.1f, AttackType.MULTIHIT, 2f, -0.1f, List.of(10, 16), JSoundRegister.IMPACT_4)
            .setInfo("Dual Chop", "quick combo starter");
    public static final Attack barrage = new Attack(3, 17, 0.85f, 50, 0, 1.5, 1f, 0.1f, AttackType.BARRAGE, 1, 0, 3)
            .setInfo("Barrage", "fast reliable combo starter/extender/finisher, medium stun, knocks back");
    public static final Attack overhead = new Attack(2, 8, 0.85f, 32, 22, 2, 9f, 1.5f, AttackType.BOX, 0.5f)
            .setHitspark(2)
            .setArmor(true)
            .setLaunch()
            .setInfo("Overhead Hook", "long windup, knockdown", AttackQueue.HEAVY);
    public static final Attack heavy = new Attack(1, 13, 0.85f, 19, 12, 1.5, 6f, 0.2f, AttackType.BOX, 1.25f, 0, 0)
            .setInfo("Vertical Chop", "medium windup combo starter, has a true followup in the form of a slow, armored knockdown", AttackQueue.HEAVY)
            .setFollowup(overhead);
    public static final Attack eyechop = new Attack(4, 20, 1f, 50, 37, 1.75, 9f, 0.3f, AttackType.BOX, 3, -0.3f)
            .setHitspark(2)
            .setInfo("Eye Chop/Blood Throw", "blindness on hit, donut combo extender/crouch to throw a stunning, blinding blood projectile");
    public static final Attack bloodthrow = new Attack(5, 25, 15, 10, 10, AttackType.BOX)
            .setInfo("Blood Throw", "");
    public static final Attack donut = new Attack(6, 15, 1f, 60, 42, 2, 14f, 0.0f, AttackType.BOX, 6, 0.1f)
            .setHitspark(2)
            .setArmor(true)
            .setInfo("Donut", "huge windup, 6s hitstun");
    public static final Attack epitaph = new Attack(7, 30, 34, 4, 0, -1, AttackType.COUNTER)
            .setInfo("Epitaph/Move Cancel", "when used raw, 0.2s windup, 1.5s counter; cancels move when used during one");
    public static final Attack timeerase = new Attack(8, 50, 15, 5, 6, AttackType.BOX)
            .setInfo("Time Erase", "6 seconds duration"); // TE = (moveStun-initTime)/20

    public static final TrackedData<Integer> TIMEERASETIME;

    public List<Entity> timeEraseEntities;
    public List<Vec3d> timeErasePositions;

    public KingCrimsonEntity(World worldIn) {
        super(StandType.KING_CRIMSON, worldIn);
        super.initialize();

        idleDistance = 1f;
        idleRotation = -65f;

        this.ignoreCameraFrustum = true;

        description = "Close-Range Deadly STRIKER";

        pros = List.of(
                "high damage output",
                "counter",
                "easy setups",
                "priceless move cancel"
        );

        cons = List.of(
                "below average speed",
                "only armored option is heavy followup",
                "limited pressure",
                "hard to master"
        );

        freespace = """
                BNB:
                    the red racist
                    Donut>M1>Eye Chop>M1>Barrage>
                    ...Move Cancel>M1>Heavy~Overhead
                    ...Time Erase
                    the gamer
                    M1>Barrage>delay.Move Cancel>M1>Heavy~Overhead""";

        moves = List.of(light, heavy, barrage, eyechop, timeerase, donut, epitaph
                , new Attack().setMobility(MobilityType.TELEPORT).setInfo("Timeskip", "15m range"));
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
            playSound(JSoundRegister.EYE_CHOP, 1, 1);
        }
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        if (getTETime() > 0) {
            CancelTE();
            return;
        }
        if (handleAttack(timeerase, JCraft.standUltCD, 8)) {
            if (getUser() instanceof ServerPlayerEntity player) {
                player.networkHandler.sendPacket(new PlaySoundS2CPacket(JSoundRegister.TIME_ERASE, SoundCategory.PLAYERS, getX(), getY(), getZ(), 1, 1, 0));
                ShaderActivationPacket.send(player, this, 0, 120, ShaderActivationPacket.Type.CRIMSON);
            }
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
            if (getMoveStun() < 1) {
                // When used raw, epitaphs
                handleAttack(epitaph, JCraft.standS3CD, 9);
            } else {
                // When used during a move, cancels it and puts time erase on cooldown
                this.curAttack = null;
                this.queuedAttack = null;
                this.setMoveStun(2);
                this.setState(0); // Basically state 1, but runs logic once

                NbtCompound playerData = ((IEntityDataSaver) user).getPersistentData();
                if (playerData.getInt(JCraft.standUltCD) < 140) {
                    playerData.putInt(JCraft.standUltCD, 140);
                } // 7 second time erase cooldown

                // Particle effects
                Vec3d oPos = user.getPos();
                Box bBox = user.getBoundingBox();
                for (ServerPlayerEntity serverPlayer :
                        ((ServerWorld) world).getPlayers()) {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeShort(2);
                    buf.writeDouble(oPos.x);
                    buf.writeDouble(oPos.y);
                    buf.writeDouble(oPos.z);
                    buf.writeDouble(bBox.getXLength());
                    buf.writeDouble(bBox.getYLength());
                    buf.writeDouble(bBox.getZLength());
                    ServerChannelFeedbackPacket.send(serverPlayer, buf);
                }
            }
        }
    }

    private static final Attack barrageFinisher = new Attack(9, 17, 0.85f, 50, 0, 1.5, 1f, 1.1f, AttackType.BARRAGE, 0.5f, 0, 3).setHitspark(2).setLaunch();

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        Vec3d rotVec = getRotationVector();
        switch (attack.id) {
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
                bloodProjectile.setVelocity(user, user.getPitch(), user.getYaw(), 0, 1.33F, 0);
                bloodProjectile.setPosition(getEyePos());
                world.spawnEntity(bloodProjectile);
            }
            case (6) -> {
                // If hit, impale and set position to middle of arm
                for (LivingEntity entity : entities) {
                    Vec3d pos = this.getPos().add(rotVec.multiply(1.5));
                    entity.teleport(pos.x, entity.getY(), pos.z);
                }
            }
            case (8) -> {
                timeEraseEntities = new LinkedList<>();
                timeErasePositions = new LinkedList<>();

                Vec3d pos = this.getEyePos();

                this.setTETime((int) (timeerase.stun * 20));
                this.curAttack = null;
                this.setBoundingBox(new Box(0, 0, 0, 0, 0, 0));

                List<Entity> toCatch = world.getEntitiesByClass(Entity.class,
                        new Box(pos.add(96.0, 96.0, 96.0), pos.subtract(96.0, 96.0, 96.0)), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                toCatch.remove(this);
                toCatch.remove(getUser());

                for (Entity entity : toCatch) {
                    timeEraseEntities.add(entity);
                    timeErasePositions.add(entity.getPos());
                }
            }
        }
    }

    @Override
    public void initMiddleClick() {
        if (!canAttack()) return;

        if (hasUser()) {
            LivingEntity user = getUser();
            NbtCompound playerData = ((IEntityDataSaver) user).getPersistentData();
            if (playerData.getInt(JCraft.utilCD) > 0) return;
            Vec3d oPos = user.getPos();

            HitResult hitResult = world.raycast(new RaycastContext(user.getEyePos(), user.getEyePos().add(user.getRotationVector().multiply(16)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
            Vec3d pos = hitResult.getPos();

            user.teleport(pos.x, pos.y, pos.z);

            playerData.putInt(JCraft.utilCD, 300); // 15 second timeskip cooldown

            if (playerData.getInt(JCraft.standUltCD) < 60) {
                playerData.putInt(JCraft.standUltCD, 60);
            } // 3 second time erase cooldown

            // Move everything around user slightly
            if (world.getGameRules().getBoolean(JCraft.KINGCRIMSON_TELEPORT_EFFECT)) {
                Vec3d vec = new Vec3d(8, 8, 8);

                List<LivingEntity> toMove = world.getEntitiesByClass(LivingEntity.class, new Box(pos.subtract(vec), pos.add(vec)), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
                for (LivingEntity entity : toMove) {
                    Vec3d ePos = entity.getPos();

                    entity.setYaw(entity.getYaw() + random.nextFloat() * 10);
                    entity.setPitch(entity.getPitch() + random.nextFloat() * 10);

                    Vec3d tPos = ePos.add(new Vec3d(random.nextDouble() * 2, random.nextDouble() * 2, random.nextDouble() * 2));

                    entity.teleport(tPos.x, tPos.y, tPos.z);
                    if (!world.isSpaceEmpty(entity))
                        entity.teleport(ePos.x, ePos.y, ePos.z);
                }
            }

            Box bBox = user.getBoundingBox();
            for (ServerPlayerEntity serverPlayer :
                    ((ServerWorld) world).getPlayers()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeShort(2);
                buf.writeDouble(oPos.x);
                buf.writeDouble(oPos.y);
                buf.writeDouble(oPos.z);
                buf.writeDouble(bBox.getXLength());
                buf.writeDouble(bBox.getYLength());
                buf.writeDouble(bBox.getZLength());
                ServerChannelFeedbackPacket.send(serverPlayer, buf);
            }

            world.playSound(null, pos.x, pos.y, pos.z, JSoundRegister.TE_TP, SoundCategory.PLAYERS, 1f, 1f);
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

    private void CancelTE() {
        LivingEntity user = getUser();
        NbtCompound userData = ((IEntityDataSaver) user).getPersistentData();
        userData.putInt(JCraft.standUltCD, userData.getInt(JCraft.standUltCD) - getTETime() * 2);
        setTETime(0);
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
        if (!world.isClient) {
            if (attack == overhead)
                this.queuedAttack = null;

            int teTime = this.getTETime();

            if (teTime > 0) {
                setTETime(teTime - 1);

                if (blocking)
                    CancelTE();

                if (attack != null)
                    if (getMoveStun() < (attack.moveStun - attack.realInitTime() * 2 / 3))
                        CancelTE();

                // Only a player user has to see the time erase trackers
                if (age % 2 == 0 && user instanceof ServerPlayerEntity serverPlayerEntity) {
                    int i = -1;
                    for (Vec3d pos : timeErasePositions) {
                        i++;

                        Box box = timeEraseEntities.get(i).getBoundingBox();
                        if (box.getAverageSideLength() > 0.1) {
                            PacketByteBuf buf = PacketByteBufs.create();
                            buf.writeShort(2);

                            buf.writeDouble(pos.x);
                            buf.writeDouble(pos.y);
                            buf.writeDouble(pos.z);

                            buf.writeDouble(box.getXLength());
                            buf.writeDouble(box.getYLength());
                            buf.writeDouble(box.getZLength());

                            ServerChannelFeedbackPacket.send(serverPlayerEntity, buf);
                        }
                    }
                }

                user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 10, 9, true, false));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 10, 0, true, false));
                user.removeStatusEffect(JStatusRegister.DAZED); // You are unstunnable inside of time erase
                Box noBox = new Box(0, 0, 0, 0, 0, 0);
                user.setBoundingBox(noBox);
                user.noClip = true;

                if (getTETime() < 1) {
                    if (user instanceof ServerPlayerEntity player)
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(JSoundRegister.TIME_ERASE_EXIT, SoundCategory.PLAYERS, getX(), getY(), getZ(), 1, 1, 0));

                    for (Entity entity : timeEraseEntities) {
                        Vec3d tePos = timeErasePositions.get(timeEraseEntities.indexOf(entity));
                        entity.teleport(tePos.x, tePos.y, tePos.z);
                    }
                }
            } else {
                setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(user) / 2, 0.0, 255.0) / 255f);
            }

            setSilent(teTime > 0);

            if (user.hasCustomName())
                user.setCustomNameVisible(teTime <= 0);
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
        }
        return PlayState.CONTINUE;
    }
}
