package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.Attack;
import net.arna.jcraft.common.util.AttackType;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
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

import java.util.List;

public class WhitesnakeEntity extends StandEntity implements IAnimatable, IAnimationTickable {
    AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

    public static Attack light = new Attack(2, 0.75f, 14, 7, 1.5, 5f, 0.75f, AttackType.BOX, 0.6f, 0.2f, 0, JSoundRegister.IMPACT_3)
            .setInfo("Punch", "quick combo starter");
    public static Attack legcrusher = new Attack(20, 0.75f, 22, 16, 1.5, 5f, 0.25f, AttackType.BOX, 1.6f, 0.2f, 0, JSoundRegister.TW_KICK_HIT).setHitspark(2)
            .setInfo("Leg Crusher", "high stun, medium windup");
    public static Attack poisonspew = new Attack(23, 0.75f, 14, 10, 2, 0f, 0, AttackType.BOX)
            .setUB(true)
            .setInfo("Poison Spew", "forces enemy into crawling for 4s, no stun"); //todo: make poison pool on ground
    public static Attack barrage = new Attack(17, 0.75f, 60, 0, 2, 1f, 0.25f, AttackType.BARRAGE, 1, 0, 3, JSoundRegister.IMPACT_3)
            .setInfo("Barrage", "fast reliable combo starter/extender, medium stun");
    public static Attack donut = new Attack(18, 1f, 36, 17, 2, 12f, 0.0f, AttackType.BOX, 1.4f, 0, 0, JSoundRegister.TW_DONUT_HIT).setHitspark(2)
            .setInfo("Donut", "slow combo starter/extender");
    public static Attack memorydisk = new Attack(45, 1f, 34, 22, 2, 6f, 0.0f, AttackType.BOX, 1f, 0, 0, JSoundRegister.IMPACT_2).setHitspark(2).setArmor(true)
            .setUB(true)
            .setInfo("Memory Disk", "uninterruptable, mining fatigue & weakness for 30s");
    public static Attack standdisk = new Attack(36, 1f, 34, 22, 2, 6f, 0.0f, AttackType.BOX, 1f, 0, 0, JSoundRegister.IMPACT_2).setArmor(true)
            .setUB(true)
            .setInfo("Stand Disk", "uninterruptable, removes enemy stand for 8s");
    public static Attack gun = new Attack(20, 21, 15, 1, 0.75f, AttackType.BOX).setRanged(true)
            .setInfo("Gun", "fully aimable, combo starter");

    public WhitesnakeEntity(EntityType<? extends StandEntity> type, World worldIn) {
        super(type, worldIn);
        super.initialize();
        idleRotation = 220f;

        pros = List.of(
                "fast m1",
                "high versatility",
                "accessible win condition"
        );

        cons = List.of(
                "no mobility options",
                "unsafe pokes"
        );

        description = "All Range SPECIALIST";

        freespace =
                "BNBs:\n" +
                        "    (Memory Disk/Stand Disk>)M1>Barrage>Leg Crusher>Donut>Gun>Poison";

        moves = List.of(light, donut, barrage, standdisk, memorydisk, legcrusher, poisonspew, gun);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
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
        if (handleAttack(barrage, JCraft.standBarrageCD, 5)) {
            this.playSound(JSoundRegister.WS_BARRAGE, 1, 1);
        }
    }

    @Override
    public void initHeavyAttack() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(donut, JCraft.standHeavyCD, 4)) {
            this.playSound(JSoundRegister.WS_DONUT, 1, 1);
        }
    }

    @Override
    public void initSpecial1() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(standdisk, JCraft.standS1CD, 8)) {
            this.playSound(JSoundRegister.WS_DISK, 1, 1);
        }
    }

    @Override
    public void initUlt() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(memorydisk, JCraft.standUltCD, 8)) {
            this.playSound(JSoundRegister.WS_DISK, 1, 1);
        }
    }

    @Override
    public void initSpecial2() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(legcrusher, JCraft.standS2CD, 6)) {
            this.playSound(JSoundRegister.WS_LEGCRUSH, 1, 1);
        }
    }

    @Override
    public void initSpecial3() {
        if (!this.canAttack()) {
            return;
        }
        handleAttack(poisonspew, JCraft.standS3CD, 7);
    }

    @Override
    public void initMiddleClick() {
        if (!this.canAttack()) {
            return;
        }
        NbtCompound userData = ((IEntityDataSaver) getUser()).getPersistentData();
        if (userData.getInt(JCraft.standMMBCD) > 0) {
            return;
        }
        setRemote(!getRemote());
        userData.putInt(JCraft.standMMBCD, 60);
        //HandleAttack(gun, JCraft.standMMBCD, 9);
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        LivingEntity user = this.getUser();
        Vec3d rotVec = this.getRotationVector();

        if (attack == poisonspew) {
            if (!world.isClient()) {
                for (LivingEntity ent : entities) {
                    ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.WSPOISON, 80, 1));
                }

                PacketByteBuf buf = PacketByteBufs.create();

                Vec3d backPos = this.getEyePos().subtract(rotVec.multiply(3));
                Vec3d headPos = this.getEyePos().add(rotVec);

                buf.writeShort(5);

                buf.writeDouble(backPos.x);
                buf.writeDouble(backPos.y);
                buf.writeDouble(backPos.z);

                buf.writeDouble(headPos.x);
                buf.writeDouble(headPos.y);
                buf.writeDouble(headPos.z);

                for (PlayerEntity sendPlayer : world.getPlayers()) {
                    if (sendPlayer instanceof ServerPlayerEntity serverPlayerEntity) {
                        ServerChannelFeedbackPacket.send(serverPlayerEntity, buf);
                    }
                }
            }
        } else if (attack == standdisk) {
            for (LivingEntity ent : entities) {
                ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.STANDLESS, 160, 0, true, false));
            }
        } else if (attack == memorydisk) {
            for (LivingEntity ent : entities) {
                ent.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 600, 0));
                ent.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 600, 0));
            }
        } else if (attack == gun) { // TODO: visuals on WS, CMOON and D4C guns
            this.playSound(JSoundRegister.WS_GUN, 1, 1);

            Box box = this
                    .getBoundingBox()
                    .stretch(user.getRotationVec(1.0F).multiply(1024))
                    .expand(1.0D);

            EntityHitResult hitResult = ProjectileUtil.raycast(
                    this,
                    this.getEyePos(),
                    this.getEyePos().add(user.getRotationVector().multiply(1024)),
                    box,
                    EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR,
                    1024
            );

            if (hitResult != null) {
                Entity entity = hitResult.getEntity();
                if (entity instanceof LivingEntity livingEntity) {
                    DamageLogic(world, livingEntity, Vec3d.ZERO, (int) attack.stun * 20, 1, false, 6, false, DamageSource.mob(user), user);
                }
            }
        }
    }

    @Override
    public void tick() {
        if (age == 1) {
            this.world.playSound(null, this.getX(), this.getY(), this.getZ(), JSoundRegister.STAND_SUMMON, SoundCategory.PLAYERS, 1f, 1f);
        }

        super.tick();

        if (!this.world.isClient()) {
            if (getRemote()) {
                double f = getRemoteForwardInput();
                double s = getRemoteSideInput();
                // 3 ticks of inertia, helping movement be fluid as well as dealing with packet drops
                if (lastRemoteInputTime - age > 4) {
                    updateRemoteInputs(0, 0, false);
                }
                Vec3d rotVec = new Vec3d(getRotationVector().x, 0, getRotationVector().z);

                double dragMult = getMoveStun() > 0 ? 0.4 : 0.6;
                double moveSpeed = 1;
                HitResult groundCheck = this.world.raycast(new RaycastContext(getEyePos(), getPos().add(0, -1.0E-5F, 0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));

                if (groundCheck.getType() != HitResult.Type.MISS) { // If grounded
                    if (getRemoteJumpInput() && getMoveStun() < 1) {
                        remoteSpeed = new Vec3d(remoteSpeed.x, 0.3, remoteSpeed.z);
                        setRemoteJumpInput(false);
                    }
                } else {
                    //JCraft.LOGGER.info("Airborne");
                    moveSpeed = 0.1;
                    remoteSpeed = remoteSpeed.add(0, -9.81 / 200, 0); // Account for gravity
                    dragMult = 0.5;
                }

                remoteSpeed = remoteSpeed
                        .add(rotVec.multiply(f * moveSpeed)) // Forward movement
                        .add(rotVec.rotateY(1.5707963f).multiply(s * moveSpeed)) // Side movement
                ;

                remoteSpeed = remoteSpeed.multiply(dragMult, 1, dragMult);

                if (getPos().add(remoteSpeed).squaredDistanceTo(getUser().getPos()) > 400) {
                    remoteSpeed.multiply(-0.1);
                }

                Vec3d newPos = getPos().add(remoteSpeed);
                HitResult hitResult = this.world.raycast(new RaycastContext(getPos().add(0, 0.1, 0), newPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));

                if (hitResult.getType() != HitResult.Type.MISS) {
                    //JCraft.LOGGER.info("Its over");
                    newPos = hitResult.getPos();
                    remoteSpeed.multiply(-0.1);
                }

                this.setFreePos(new Vec3f(newPos));
            } else if (hasUser()) {
                this.setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(getUser()) / 2, 0.0, 255.0) / 255f);
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
            default -> controller.setAnimation(builder.loop("animation.whitesnake.idle"));
            case 2 -> controller.setAnimation(builder.playAndHold("animation.whitesnake.light"));
            case 3 -> controller.setAnimation(builder.loop("animation.whitesnake.block"));
            case 4 -> controller.setAnimation(builder.playAndHold("animation.whitesnake.donut"));
            case 5 -> controller.setAnimation(builder.loop("animation.whitesnake.barrage"));
            case 6 -> controller.setAnimation(builder.playAndHold("animation.whitesnake.legcrusher"));
            case 7 -> controller.setAnimation(builder.playAndHold("animation.whitesnake.poisonspew"));
            case 8 -> controller.setAnimation(builder.playAndHold("animation.whitesnake.disk"));
            case 9 -> controller.setAnimation(builder.playAndHold("animation.whitesnake.gun"));
        }
        return PlayState.CONTINUE;
    }
}
