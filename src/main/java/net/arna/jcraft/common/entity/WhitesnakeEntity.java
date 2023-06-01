package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.Attack;
import net.arna.jcraft.common.util.AttackType;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.HitResult;
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

    public static Attack light = new Attack(0, 2, 0.75f, 14, 7, 1.5, 5f, 0.75f, AttackType.BOX, 0.6f, 0.2f, 0, JSoundRegister.IMPACT_3)
            .setInfo("Punch", "quick combo starter");
    public static Attack legcrusher = new Attack(4, 20, 0.75f, 22, 16, 1.5, 5f, 0.25f, AttackType.BOX, 1.6f, 0.2f, 0, JSoundRegister.TW_KICK_HIT).setHitspark(2)
            .setInfo("Leg Crusher", "high stun, medium windup");
    public static Attack poisonspew = new Attack(5, 23, 0.75f, 14, 10, 2, 0f, 0, AttackType.BOX)
            .setUB(true)
            .setInfo("Poison Spew", "forces enemy into crawling for 4s, no stun"); //todo: make poison pool on ground
    public static Attack barrage = new Attack(2, 17, 0.75f, 60, 0, 2, 1f, 0.25f, AttackType.BARRAGE, 1, 0, 3, JSoundRegister.IMPACT_3)
            .setInfo("Barrage", "fast reliable combo starter/extender, medium stun");
    public static Attack donut = new Attack(1, 18, 1f, 36, 17, 2, 12f, 0.0f, AttackType.BOX, 1.4f, 0, 0, JSoundRegister.TW_DONUT_HIT).setHitspark(2)
            .setInfo("Donut", "slow combo starter/extender");
    public static Attack memorydisk = new Attack(6, 45, 1f, 34, 22, 2, 6f, 0.0f, AttackType.BOX, 1f, 0, 0, JSoundRegister.IMPACT_2).setHitspark(2).setArmor(true)
            .setUB(true)
            .setInfo("Memory Disk", "uninterruptable, mining fatigue & weakness for 30s");
    public static Attack standdisk = new Attack(3, 36, 1f, 34, 22, 2, 6f, 0.0f, AttackType.BOX, 1f, 0, 0, JSoundRegister.IMPACT_2).setArmor(true)
            .setUB(true)
            .setInfo("Stand Disk", "uninterruptable, removes enemy stand for 8s");
    //public static Attack gun = new Attack(-1,20, 21, 15, 1, 0.75f, AttackType.BOX).setRanged(true)
    //        .setInfo("Gun", "fully aimable, combo starter");

    public WhitesnakeEntity(World worldIn) {
        super(StandType.WHITE_SNAKE, worldIn);
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
                        "    (Memory Disk/Stand Disk>)M1>Barrage>Leg Crusher>Donut>Poison";

        moves = List.of(light, donut, barrage, standdisk, memorydisk, legcrusher, poisonspew,
                new Attack().setInfo("Pilot Mode", ""));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!this.canAttack()) return;
        handleAttack(light, JCraft.standLightCD, 2);
    }

    @Override
    public void initBarrage() {
        if (!this.canAttack()) return;
        if (handleAttack(barrage, JCraft.standBarrageCD, 5))
            this.playSound(JSoundRegister.WS_BARRAGE, 1, 1);
    }

    @Override
    public void initHeavyAttack() {
        if (!this.canAttack()) return;
        if (handleAttack(donut, JCraft.standHeavyCD, 4))
            this.playSound(JSoundRegister.WS_DONUT, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (!this.canAttack()) return;
        if (handleAttack(standdisk, JCraft.standS1CD, 8))
            this.playSound(JSoundRegister.WS_DISK, 1, 1);
    }

    @Override
    public void initUlt() {
        if (!this.canAttack()) return;
        if (handleAttack(memorydisk, JCraft.standUltCD, 8))
            this.playSound(JSoundRegister.WS_DISK, 1, 1);
    }

    @Override
    public void initSpecial2() {
        if (!this.canAttack()) return;
        if (handleAttack(legcrusher, JCraft.standS2CD, 6))
            this.playSound(JSoundRegister.WS_LEGCRUSH, 1, 1);
    }

    @Override
    public void initSpecial3() {
        if (!this.canAttack()) return;
        handleAttack(poisonspew, JCraft.standS3CD, 7);
    }

    @Override
    public void initMiddleClick() {
        if (!this.canAttack()) return;
        NbtCompound userData = ((IEntityDataSaver) getUser()).getPersistentData();
        if (userData.getInt(JCraft.standMMBCD) > 0) return;
        setRemote(!getRemote());
        userData.putInt(JCraft.standMMBCD, 30);
        //HandleAttack(gun, JCraft.standMMBCD, 9);
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        Vec3d rotVec = this.getRotationVector();

        switch (attack.id) {
            case (3) -> { // Stand Disc
                for (LivingEntity ent : entities)
                    ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.STANDLESS, 160, 0, true, false));
            }
            case (5) -> { // Poison Spew
                for (LivingEntity ent : entities)
                    ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.WSPOISON, 80, 1));
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
                for (PlayerEntity sendPlayer : world.getPlayers())
                    if (sendPlayer instanceof ServerPlayerEntity serverPlayerEntity)
                        ServerChannelFeedbackPacket.send(serverPlayerEntity, buf);
            }
            case (6) -> {
                for (LivingEntity ent : entities) {
                    ent.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 600, 0));
                    ent.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 600, 0));
                }
            }
        }
    }

    @Override
    public void tick() {
        if (age == 1) this.world.playSound(null, this.getX(), this.getY(), this.getZ(), JSoundRegister.STAND_SUMMON, SoundCategory.PLAYERS, 1f, 1f);
        super.tick();

        if (!this.world.isClient()) {
            if (getRemote()) {
                double f = getRemoteForwardInput();
                double s = getRemoteSideInput();
                boolean jump = getRemoteJumpInput();

                Vec3d pos = getPos();

                // 3 ticks of inertia, helping movement be fluid as well as dealing with packet drops
                if (lastRemoteInputTime - age > 4) updateRemoteInputs(0, 0, false);
                Vec3d rotVec = new Vec3d(getRotationVector().x, 0, getRotationVector().z);

                double dragMult = getMoveStun() > 0 ? 0.4 : 0.6;
                double moveSpeed = 1;
                HitResult groundCheck = this.world.raycast(new RaycastContext(getEyePos(), pos.add(0, -1.0E-5F, 0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
                boolean onGround = groundCheck.getType() != HitResult.Type.MISS;

                if (getState() < 2) { // Replace idle anim
                    if (s > 0) setStateNoReset(onGround ? 12 : 16);
                    if (s < 0) setStateNoReset(onGround ? 11 : 15);
                    if (f < 0) setStateNoReset(onGround ? 10 : 14);
                    if (f > 0) setStateNoReset(onGround ? 9 : 13);
                }

                if (onGround) { // If grounded
                    if (jump && getMoveStun() < 1) {
                        remoteSpeed = new Vec3d(remoteSpeed.x, 0.3, remoteSpeed.z);
                        setRemoteJumpInput(false);
                    }
                } else {
                    //JCraft.LOGGER.info("Airborne");
                    moveSpeed = 0.1;
                    remoteSpeed = remoteSpeed.add(0, -9.81 / 200, 0); // Account for gravity
                    dragMult = 0.7;
                }

                remoteSpeed = remoteSpeed
                        .add(rotVec.multiply(f * moveSpeed)) // Forward movement
                        .add(rotVec.rotateY(1.5707963f).multiply(s * moveSpeed)) // Side movement
                ;

                remoteSpeed = remoteSpeed.multiply(dragMult, 1, dragMult);

                if (pos.add(remoteSpeed).squaredDistanceTo(getUser().getPos()) > 400) {
                    remoteSpeed.multiply(-0.1);
                }

                //todo: make this actually respect the WS collider
                Vec3d newPos = pos.add(remoteSpeed);
                /*
                HitResult upCast = this.world.raycast(new RaycastContext(pos.add(0, 0.1, 0), pos.add(0, 1.8, 0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
                if (upCast.getType() != HitResult.Type.MISS) {
                    newPos = pos;
                    remoteSpeed.multiply(-0.1);
                }
                 */
                HitResult downCast = this.world.raycast(new RaycastContext(pos.add(0, 0.1, 0), newPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
                if (downCast.getType() != HitResult.Type.MISS) {
                    newPos = downCast.getPos();
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

        if (age < 20 && getState() < 2) {
            controller.setAnimation(builder.playOnce("animation.whitesnake.summon"));
            return PlayState.CONTINUE;
        }

        if (this.getSameState()) controller.markNeedsReload();
        switch (this.getState()) {
            default -> controller.setAnimation(builder.loop("animation.whitesnake.idle"));
            case 2 -> controller.setAnimation(builder.playAndHold("animation.whitesnake.light"));
            case 3 -> controller.setAnimation(builder.loop("animation.whitesnake.block"));
            case 4 -> controller.setAnimation(builder.playAndHold("animation.whitesnake.donut"));
            case 5 -> controller.setAnimation(builder.loop("animation.whitesnake.barrage"));
            case 6 -> controller.setAnimation(builder.playAndHold("animation.whitesnake.legcrusher"));
            case 7 -> controller.setAnimation(builder.playAndHold("animation.whitesnake.acidspew"));
            case 8 -> controller.setAnimation(builder.playAndHold("animation.whitesnake.disc"));

            case 9 -> controller.setAnimation(builder.loop("animation.whitesnake.forw"));
            case 10 -> controller.setAnimation(builder.loop("animation.whitesnake.back"));
            case 11 -> controller.setAnimation(builder.loop("animation.whitesnake.left"));
            case 12 -> controller.setAnimation(builder.loop("animation.whitesnake.right"));
            case 13 -> controller.setAnimation(builder.loop("animation.whitesnake.fdash"));
            case 14 -> controller.setAnimation(builder.loop("animation.whitesnake.bdash"));
            case 15 -> controller.setAnimation(builder.loop("animation.whitesnake.ldash"));
            case 16 -> controller.setAnimation(builder.loop("animation.whitesnake.rdash"));
        }

        return PlayState.CONTINUE;
    }
}
