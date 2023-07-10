package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.util.Attack;
import net.arna.jcraft.common.util.AttackType;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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
    public static final Attack light = new Attack(0, 2, 0.75f, 14, 7, 1.5, 5f, 0.75f, AttackType.BOX, 0.6f, 0.2f, 0, JSoundRegister.IMPACT_3)
            .setInfo("Punch", "quick combo starter");
    public static final Attack legcrusher = new Attack(4, 20, 0.75f, 22, 16, 1.75, 7f, 0.25f, AttackType.BOX, 1.6f, 0.2f, 0, JSoundRegister.TW_KICK_HIT)
            .setHitspark(2)
            .setInfo("Leg Crusher", "high stun, medium windup");
    public static final Attack poisonspew = new Attack(5, 20, 0.75f, 14, 10, 2, 0f, 0, AttackType.BOX)
            .setUB(true)
            .setInfo("Poison Spew", "fires an acid projectile that slows enemies and persists on the surface it hits for 5s/crouch for a charged variation that fires 5 slower shots");
    public static final Attack chargedspew = new Attack(7, 30, 0.75f, 26, 20, 2, 0f, 0, AttackType.BOX)
            .setUB(true)
            .setInfo("Charged Spew", "");
    public static final Attack barrage = new Attack(2, 17, 0.75f, 60, 0, 2, 1f, 0.25f, AttackType.BARRAGE, 1, 0, 3, JSoundRegister.IMPACT_3)
            .setInfo("Barrage", "fast reliable combo starter/extender, medium stun");
    public static final Attack donut = new Attack(1, 18, 1f, 36, 17, 2, 10f, 0.0f, AttackType.BOX, 1.4f, 0, 0, JSoundRegister.TW_DONUT_HIT)
            .setHitspark(2)
            .setInfo("Donut", "slow combo starter/extender");
    public static final Attack memorydisk = new Attack(6, 30, 1f, 34, 22, 2, 7f, 0.0f, AttackType.BOX, 1f, 0, 0, JSoundRegister.IMPACT_2)
            .setHitspark(2)
            .setArmor(true)
            .setUB(true)
            .setInfo("Memory Disk", "uninterruptable, mining fatigue & weakness for 30s");
    public static final Attack standdisk = new Attack(3, 30, 1f, 34, 22, 2, 8f, 0.0f, AttackType.BOX, 1f, 0, 0, JSoundRegister.IMPACT_2)
            .setHitspark(2)
            .setArmor(true)
            .setUB(true)
            .setInfo("Stand Disk/Melt your Heart", "uninterruptable, removes enemy stand for 8s/in remote mode, long windup, creates a sphere of poison projectiles");
    public static final Attack meltyourheart = new Attack(8, 40, 1f, 50, 40, 2, 3f, 1.0f, AttackType.BOX, 1f, 0, 0, JSoundRegister.IMPACT_2)
            .setArmor(true)
            .setUB(true)
            .setLaunch()
            .setInfo("", "");

    //public static Attack gun = new Attack(-1,20, 21, 15, 1, 0.75f, AttackType.BOX).setRanged(true).setInfo("Gun", "fully aimable, combo starter");

    public WhitesnakeEntity(World worldIn) {
        super(StandType.WHITE_SNAKE, worldIn);
        super.initialize();
        idleRotation = 220f;

        pros = List.of(
                "coverage on all ranges",
                "high versatility",
                "accessible win condition"
        );

        cons = List.of(
                "no mobility options",
                "slow pokes"
        );

        description = "All Range DISABLER";

        freespace =
                "BNBs:\n" +
                        "    (Memory Disk/Stand Disk>)M1>Barrage>Leg Crusher>Donut>Poison";

        moves = List.of(light, donut, barrage, memorydisk, standdisk, legcrusher, poisonspew,
                new Attack().setInfo("Pilot Mode", ""));
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!canAttack()) return;
        handleAttack(light, JCraft.standLightCD, 2);
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleAttack(barrage, JCraft.standBarrageCD, 5))
            playSound(JSoundRegister.WS_BARRAGE, 1, 1);
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;
        if (handleAttack(donut, JCraft.standHeavyCD, 4))
            playSound(JSoundRegister.WS_DONUT, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (!canAttack()) return;
        if (handleAttack(memorydisk, JCraft.standS1CD, 8))
            playSound(JSoundRegister.WS_MEMORY_DISC, 1, 1);
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        if (getRemote() && handleAttack(meltyourheart, JCraft.standUltCD, 18))
            playSound(JSoundRegister.WS_MYH, 1, 1);
        else if (handleAttack(standdisk, JCraft.standUltCD, 8))
            playSound(JSoundRegister.WS_STAND_DISC, 1, 1);
    }

    @Override
    public void initSpecial2() {
        if (!canAttack()) return;
        if (handleAttack(legcrusher, JCraft.standS2CD, 6))
            playSound(JSoundRegister.WS_LEGCRUSH, 1, 1);
    }

    @Override
    public void initSpecial3() {
        if (!canAttack()) return;

        if (getUser().isSneaking())
            handleAttack(chargedspew, JCraft.standS3CD, 17);
        else
            handleAttack(poisonspew, JCraft.standS3CD, 7);
    }

    @Override
    public void initMiddleClick() {
        if (!canAttack()) return;
        NbtCompound userData = ((IEntityDataSaver) getUser()).getPersistentData();
        if (userData.getInt(JCraft.utilCD) > 0) return;
        setRemote(!getRemote());
        userData.putInt(JCraft.utilCD, 20);
        //HandleAttack(gun, JCraft.standMMBCD, 9);
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        LivingEntity user = getUser();
        switch (attack.id) {
            case (3) -> { // Stand Disc
                for (LivingEntity ent : entities)
                    ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.STANDLESS, 160, 0, true, false));
            }
            case (5) -> { // Poison Spew
                WSAcidProjectile acidProjectile = new WSAcidProjectile(world, user);
                acidProjectile.setVelocity(user, user.getPitch(), user.getYaw(), 0, 1.33F, 0);
                acidProjectile.setPosition(getEyePos());
                world.spawnEntity(acidProjectile);
            }
            case (6) -> {
                for (LivingEntity ent : entities) {
                    ent.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 600, 0));
                    ent.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 600, 0));
                }
            }
            case (7) -> { // Charged Spew
                for (int i = 0; i < 5; i++) {
                    WSAcidProjectile acidProjectile = new WSAcidProjectile(world, user);
                    acidProjectile.setVelocity(user, user.getPitch(), user.getYaw() - 75F + i * 37.5F, 0, 0.66F, 0);
                    acidProjectile.setPosition(getEyePos());
                    world.spawnEntity(acidProjectile);
                }
            }
            case (8) -> { // Melt your Heart
                for (int i = 0; i < 10; i++) {
                    float yaw = i * 36F - 180F + i * 3.6F;
                    for (int j = 0; j < 10; j++) {
                        WSAcidProjectile acidProjectile = new WSAcidProjectile(world, user);
                        acidProjectile.markMeltYourHeart();
                        acidProjectile.setVelocity(user, j * 36F - 180F, yaw, 0, 0.66F, 0);
                        acidProjectile.setPosition(getEyePos());
                        world.spawnEntity(acidProjectile);
                    }
                }
            }
        }
    }

    @Override
    public void tick() {
        if (age == 1) playSound(JSoundRegister.WS_SUMMON, 1f, 1f);
        super.tick();

        if (getRemote()) {
            if (!world.isClient) {
                double f = getRemoteForwardInput();
                double s = getRemoteSideInput();
                boolean jump = getRemoteJumpInput();

                Vec3d pos = getPos();

                // 3 ticks of inertia, helping movement be fluid as well as dealing with packet drops
                if (lastRemoteInputTime - age > 4) updateRemoteInputs(0, 0, false);
                Vec3d rotVec = new Vec3d(getRotationVector().x, 0, getRotationVector().z).normalize();

                double dragMult = getMoveStun() > 0 ? 0.2 : 0.4;
                double moveSpeed = 0.24;
                //HitResult groundCheck = this.world.raycast(new RaycastContext(getEyePos(), pos.add(0, -1.0E-5F, 0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
                boolean onGround = isOnGround();

                if (getState() < 2) { // Replace idle anim
                    if (s > 0) setStateNoReset(onGround ? 12 : 16);
                    if (s < 0) setStateNoReset(onGround ? 11 : 15);
                    if (f < 0) setStateNoReset(onGround ? 10 : 14);
                    if (f > 0) setStateNoReset(onGround ? 9 : 13);
                }

                if (onGround) { // If grounded
                    if (jump && getMoveStun() < 1) {
                        remoteSpeed = new Vec3d(remoteSpeed.x, 0.25, remoteSpeed.z);
                        setRemoteJumpInput(false);
                    }
                } else {
                    //JCraft.LOGGER.info("Airborne");
                    moveSpeed = 0.024;
                    remoteSpeed = remoteSpeed.add(0, -9.81 / 200, 0); // Account for gravity
                    dragMult = 0.4;
                }

                remoteSpeed = remoteSpeed
                        .add(rotVec.multiply(f * moveSpeed)) // Forward movement
                        .add(rotVec.rotateY(1.5707963f).multiply(s * moveSpeed)) // Side movement
                ;

                remoteSpeed = remoteSpeed.multiply(dragMult, 1, dragMult);

                if (pos.add(remoteSpeed).squaredDistanceTo(getUser().getPos()) > 400)
                    remoteSpeed.multiply(-1);

                addVelocity(remoteSpeed.x, remoteSpeed.y, remoteSpeed.z);
            }

            velocityDirty = true;
            velocityModified = true;
        } else if (hasUser())
            setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(getUser()) / 2, 0.0, 255.0) / 255f);
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

    @SuppressWarnings("rawtypes")
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationController controller = event.getController();
        AnimationBuilder builder = new AnimationBuilder();

        if (playSummonAnim) {
            controller.setAnimation(builder.playOnce("animation.whitesnake.summon"));
            return PlayState.CONTINUE;
        }

        if (getSameState()) controller.markNeedsReload();
        int state = getState();
        switch (state) {
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

            case 17 -> controller.setAnimation(builder.playAndHold("animation.whitesnake.acidspew_charged"));
            case 18 -> controller.setAnimation(builder.playAndHold("animation.whitesnake.meltyourheart"));
        }
        controller.setAnimationSpeed(state > 8 ? 1.2 : 1);
        return PlayState.CONTINUE;
    }
}
