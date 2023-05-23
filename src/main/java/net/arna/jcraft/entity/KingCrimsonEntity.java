package net.arna.jcraft.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.effects.ModStatusRegister;
import net.arna.jcraft.registry.ModSoundRegister;
import net.arna.jcraft.util.*;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
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

import java.util.LinkedList;
import java.util.List;

public class KingCrimsonEntity extends StandEntity implements IAnimatable, IAnimationTickable {
    AnimationFactory animationFactory = new AnimationFactory(this);

    //maybe convert to grab - SPECIAL 2: Eye chop

    public static Attack light = new Attack(3, 0.85f, 23, 0, 1.5, 4f, 0.1f, AttackType.MULTIHIT, 2f, -0.1f, List.of(10, 16), ModSoundRegister.IMPACT_4)
            .setInfo("Dual Chop", "quick combo starter");
    public static Attack barrage = new Attack(17, 0.85f, 50, 0, 1.5, 1f, 0.1f, AttackType.BARRAGE, 1, 0, 3)
            .setInfo("Barrage", "fast reliable combo starter/extender/finisher, medium stun, knocks back");
    public static Attack overhead = new Attack(8, 0.85f, 32, 22, 2, 9f, 1.5f, AttackType.BOX, 0.5f).setHitspark(2).setArmor(true).setLaunch()
            .setInfo("Overhead Hook", "long windup, knockdown", AttackQueue.HEAVY);
    public static Attack heavy = new Attack(13, 0.85f, 19, 12, 1.5, 6f, 0.2f, AttackType.BOX, 1.25f, 0, 0)
            .setInfo("Vertical Chop", "medium windup combo starter, has a true followup in the form of a slow, armored knockdown", AttackQueue.HEAVY).setFollowup(overhead);
    public static Attack eyechop = new Attack(20, 1f, 50, 37, 1.75, 9f, 0.3f, AttackType.BOX, 3, -0.3f).setHitspark(2)
            .setInfo("Eye Chop/Blood Throw", "blindness on hit, donut combo extender/crouch to throw a stunning, blinding blood projectile");
    public static Attack bloodthrow = new Attack(25, 15, 10, 10, AttackType.BOX)
            .setInfo("Blood Throw", "");
    public static Attack donut = new Attack(15, 1f, 60, 42, 2, 14f, 0.0f, AttackType.BOX, 6, 0.1f).setHitspark(2)
            .setInfo("Donut", "huge windup, 6s hitstun");
    public static Attack timeerase = new Attack(50, 15, 5, 6, AttackType.BOX)
            .setInfo("Time Erase", "6 seconds duration"); // TE = (moveStun-initTime)/20
    public static Attack epitaph = new Attack(30, 34, 4, 0, -1, AttackType.COUNTER)
            .setInfo("Epitaph/Move Cancel", "when used raw, 0.2s windup, 1.5s counter; cancels move when used during one");

    public static TrackedData<Integer> TIMEERASETIME;

    public List<Entity> timeEraseEntities;
    public List<Vec3d> timeErasePositions;

    public KingCrimsonEntity(EntityType<? extends StandEntity> type, World worldIn) {
        super(type, worldIn);
        super.Initialize();

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

        freespace = "BNB:\n" +
                "    the red racist\n" +
                "    Donut>M1>Eye Chop>M1>Barrage>\n" +
                "    ...Move Cancel>M1>Heavy~Overhead\n" +
                "    ...Time Erase\n" +
                "    the gamer\n" +
                "    M1>Barrage>delay.Move Cancel>M1>Heavy~Overhead";

        moves = List.of(light, heavy, barrage, eyechop, timeerase, donut, epitaph
                , new Attack().setMobility(MobilityType.TELEPORT).setInfo("Timeskip", "15m range"));
    }

    static {
        TIMEERASETIME = DataTracker.registerData(KingCrimsonEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    public int getTETime() { return this.dataTracker.get(TIMEERASETIME); }
    public void setTETime(int teTime) { this.dataTracker.set(TIMEERASETIME, teTime); }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.getDataTracker().startTracking(TIMEERASETIME, 0);
    }

    // Moveset
    @Override
    public void InitLightAttack() {
        if (!this.CanAttack()) { return; }
        if (HandleAttack(light, JCraft.standLightCD, 2)) {
            this.playSound(ModSoundRegister.KC_DUAL_CHOP,1, 1);
        }
    }

    @Override
    public void InitHeavyAttack() {
        if (hasUser()) {
            if (getUser().hasStatusEffect(ModStatusRegister.Dazed)) { return; }
            boolean idling = this.getMoveStun() < 1;

            if (this.curAttack != heavy) {
                if (idling && HandleAttack(heavy, JCraft.standHeavyCD, 10)) {
                    this.playSound(ModSoundRegister.KC_HEAVY, 1, 1);
                }
            } else if (this.getMoveStun() < 7) {
                SetAttack(overhead, 4);
                this.playSound(ModSoundRegister.KC_HEAVY2, 1, 1);
            }
        }
    }

    @Override
    public void InitBarrage() {
        if (!this.CanAttack()) { return; }
        if (HandleAttack(barrage, JCraft.standBarrageCD, 6)) {
            this.playSound(ModSoundRegister.KC_BARRAGE,1, 1);
        }
    }

    @Override
    public void InitSpecial1() {
        if (!this.CanAttack()) { return; }
        if (getUser().isSneaking() && HandleAttack(bloodthrow, JCraft.standS1CD, 11)) {
            getUser().damage(DamageSource.MAGIC, 0.1f);
        } else if (HandleAttack(eyechop, JCraft.standS1CD, 7)) {
            this.playSound(ModSoundRegister.EYE_CHOP, 1, 1);
        }
    }

    @Override
    public void InitUlt() {
        if (!this.CanAttack()) { return; }
        if (getTETime() > 0) {
            CancelTE();
            return;
        }
        if (HandleAttack(timeerase, JCraft.standUltCD, 8)) {
            if (getUser() instanceof ServerPlayerEntity player)
                player.networkHandler.sendPacket(new PlaySoundS2CPacket(ModSoundRegister.TIME_ERASE, SoundCategory.PLAYERS, getX(), getY(), getZ(), 1, 1, 0));
        }
    }

    @Override
    public void InitSpecial2() {
        if (!this.CanAttack()) { return; }
        if (HandleAttack(donut, JCraft.standS2CD, 5)) {
            this.playSound(ModSoundRegister.KC_DONUT,1, 1);
        }
    }

    @Override
    public void InitSpecial3() {
        if (hasUser()) {
            LivingEntity user = this.getUser();
            ITimeStop timeStop = (ITimeStop) user;
            if (user.hasStatusEffect(ModStatusRegister.Dazed) || timeStop.getTimeStopTicks() > 0) {
                return;
            }
            if (this.getMoveStun() < 1) {
                // When used raw, epitaphs
                HandleAttack(epitaph, JCraft.standS3CD, 9);
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
                for (ServerPlayerEntity serverPlayer:
                        ((ServerWorld)world).getPlayers()) {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeShort(2);
                    buf.writeDouble(oPos.x);
                    buf.writeDouble(oPos.y);
                    buf.writeDouble(oPos.z);
                    buf.writeDouble(bBox.getXLength());
                    buf.writeDouble(bBox.getYLength());
                    buf.writeDouble(bBox.getZLength());
                    ServerPlayNetworking.send(serverPlayer, JCraft.serverFeedbackChannel, buf);
                }
            }
        }
    }

    private static final Attack barrageFinisher = new Attack(17, 0.85f, 50, 0, 1.5, 1f, 1.1f, AttackType.BARRAGE, 0.5f, 0, 3).setHitspark(2).setLaunch();
    @Override
    public void SpecialAttack(Attack attack, List<LivingEntity> entities) {
        Vec3d rotVec = this.getRotationVector();
        if (attack == barrage && this.getMoveStun() < 4) {
            this.curAttack = barrageFinisher;
        } else if (attack == overhead) {
            for (LivingEntity ent : entities) {
                ent.addStatusEffect(new StatusEffectInstance(ModStatusRegister.Knockdown, 35, 0));
            }
        } else if (attack == eyechop) {
            for (LivingEntity ent : entities) {
                ent.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0));
            }
        } else if (attack == donut) {
            // If hit, impale and set position to middle of arm
            for (LivingEntity entity : entities) {
                Vec3d pos = this.getPos().add(rotVec.multiply(1.5));
                entity.teleport(pos.x, entity.getY(), pos.z);
            }
        } else if (attack == timeerase) {
            timeEraseEntities = new LinkedList<>();
            timeErasePositions = new LinkedList<>();

            Vec3d pos = this.getEyePos();

            this.setTETime((int) (timeerase.stun * 20));
            this.curAttack = null;
            this.setBoundingBox(new Box(0,0,0,0,0,0));

            List<Entity> toCatch = world.getEntitiesByClass(Entity.class,
                    new Box(pos.add(96.0, 96.0, 96.0), pos.subtract(96.0, 96.0, 96.0)), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

            toCatch.remove(this);
            toCatch.remove(getUser());

            for (Entity entity : toCatch) {
                timeEraseEntities.add(entity);
                timeErasePositions.add(entity.getPos());
            }
        } else if (attack == bloodthrow) {
            LivingEntity user = this.getUser();
            BloodProjectile bloodProjectile = new BloodProjectile(world, user);
            bloodProjectile.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
            bloodProjectile.setVelocity(user, user.getPitch(), user.getYaw(), 0, 1F, 0);
            bloodProjectile.setPosition(getEyePos());
            world.spawnEntity(bloodProjectile);
        }
    }

    @Override
    public void InitMiddleClick() {
        if (!this.CanAttack()) { return; }

        if (!this.world.isClient() && hasUser()) {
            LivingEntity user = this.getUser();
            NbtCompound playerData = ((IEntityDataSaver) user).getPersistentData();
            if (playerData.getInt(JCraft.standMMBCD) > 0) { return; }
            Vec3d oPos = user.getPos();

            HitResult hitResult = this.world.raycast(new RaycastContext(user.getEyePos(), user.getEyePos().add(user.getRotationVector().multiply(16)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
            Vec3d pos = hitResult.getPos();

            user.teleport(pos.x, pos.y, pos.z);

            playerData.putInt(JCraft.standMMBCD, 300); // 15 second timeskip cooldown

            if (playerData.getInt(JCraft.standUltCD) < 60) {
                playerData.putInt(JCraft.standUltCD, 60); } // 3 second time erase cooldown

            // Move everything around user slightly
            if (this.world.getGameRules().getBoolean(JCraft.KINGCRIMSON_TELEPORT_EFFECT)) {
                Vec3d vec = new Vec3d(8, 8, 8);

                List<LivingEntity> toMove = this.world.getEntitiesByClass(LivingEntity.class, new Box(pos.subtract(vec), pos.add(vec)), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
                for (LivingEntity entity : toMove) {
                    Vec3d ePos = entity.getPos();

                    entity.setYaw(entity.getYaw() + random.nextFloat()*10);
                    entity.setPitch(entity.getPitch() + random.nextFloat()*10);

                    Vec3d tPos = ePos.add(new Vec3d(random.nextDouble()*2, random.nextDouble()*2, random.nextDouble()*2));

                    entity.teleport(tPos.x, tPos.y, tPos.z);
                    if (!this.world.isSpaceEmpty(entity)) { entity.teleport(ePos.x, ePos.y, ePos.z); }
                }
            }

            Box bBox = user.getBoundingBox();
            for (ServerPlayerEntity serverPlayer:
                    ((ServerWorld)world).getPlayers()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeShort(2);
                buf.writeDouble(oPos.x);
                buf.writeDouble(oPos.y);
                buf.writeDouble(oPos.z);
                buf.writeDouble(bBox.getXLength());
                buf.writeDouble(bBox.getYLength());
                buf.writeDouble(bBox.getZLength());
                ServerPlayNetworking.send(serverPlayer, JCraft.serverFeedbackChannel, buf);
            }

            this.world.playSound(null, pos.x, pos.y, pos.z, ModSoundRegister.TE_TP, SoundCategory.PLAYERS, 1f, 1f);
        }
    }

    @Override
    public void Desummon() {
        if ( this.getTETime() < 1 ) {
            super.Desummon();
        }
    }

    @Override
    public void Counter(Entity entity, DamageSource source) {
        super.Counter(entity, source);
        if (entity == null || !hasUser())
            return;
        LivingEntity user = this.getUser();
        Vec3d ePos = entity.getPos();
        Vec3d uPos = user.getPos();

        entity.teleport(uPos.x, uPos.y, uPos.z);
        user.teleport(ePos.x, ePos.y, ePos.z);
        user.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, entity.getEyePos());

        if (entity instanceof LivingEntity livingEntity) {
            Stun(livingEntity, 20, 0);
            if (entity.getFirstPassenger() instanceof StandEntity stand)
                stand.CancelAttack();
        }

        this.world.playSound(null, uPos.x, uPos.y, uPos.z, ModSoundRegister.TE_TP, SoundCategory.PLAYERS, 1f, 1f);
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
        NbtCompound userData = ((IEntityDataSaver)user).getPersistentData();
        userData.putInt(JCraft.standUltCD, userData.getInt(JCraft.standUltCD) - getTETime() * 2);
        setTETime(0);
    }

    @Override
    public void tick() {
        if (age == 1) { this.world.playSound(null, this.getX(), this.getY(), this.getZ(), ModSoundRegister.KC_SUMMON, SoundCategory.PLAYERS, 1f, 1f); }
        super.tick();

        LivingEntity user = this.getUser();
        if (user == null) { return; }

        Attack attack = this.curAttack;
        if (!this.world.isClient()) {
            if (attack != null) {
                if (attack == overhead) {
                    this.queuedAttack = null;
                }
            }

            int teTime = this.getTETime();
            if (teTime > 0) {
                this.setTETime(teTime - 1);

                if (blocking)
                    CancelTE();

                if (attack != null) {
                    if (this.getMoveStun() < (attack.moveStun - attack.realInitTime() * 2 / 3))
                        CancelTE();
                }

                // Only a player user has to see the time erase trackers
                if (age%2 == 0 && user instanceof PlayerEntity playerEntity) {
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

                            ServerPlayNetworking.send((ServerPlayerEntity) playerEntity, JCraft.serverFeedbackChannel, buf);
                        }
                    }
                }

                user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 10, 9, true, false));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 10, 0, true, false));
                user.removeStatusEffect(ModStatusRegister.Dazed); // You are unstunnable inside of time erase
                Box noBox = new Box(0, 0, 0, 0, 0, 0);
                user.setBoundingBox(noBox);
                user.noClip = true;

                if (this.getTETime() < 1) {
                    if (user instanceof ServerPlayerEntity player)
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(ModSoundRegister.TIME_ERASE_EXIT, SoundCategory.PLAYERS, getX(), getY(), getZ(), 1, 1, 0));

                    for (Entity entity : timeEraseEntities) {
                        Vec3d tePos = timeErasePositions.get(timeEraseEntities.indexOf(entity));
                        entity.teleport(tePos.x, tePos.y, tePos.z);
                    }
                }

            } else {
                this.setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(user) / 2, 0.0, 255.0) / 255f);
            }

            this.setSilent(teTime > 0);

            if (user.hasCustomName())
                user.setCustomNameVisible(teTime <= 0);
        }
    }

    // Animation code
    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
    }
    @Override
    public AnimationFactory getFactory() { return this.animationFactory; }
    @Override
    public int tickTimer() { return age; }
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) { AnimationController controller = event.getController();
        AnimationBuilder builder = new AnimationBuilder();

        if (age < 20 && getState() < 2) { controller.setAnimation(builder.playOnce("animation.kingcrimson.summon")); return PlayState.CONTINUE; }

        if (this.getSameState()) { controller.markNeedsReload(); }
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
