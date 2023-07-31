package net.arna.jcraft.common.entity;

import com.google.common.collect.Lists;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.attack.AttackType;
import net.arna.jcraft.common.entity.projectile.WSAcidProjectile;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.function.Consumer;

public class WhiteSnakeEntity extends StandEntity<WhiteSnakeEntity, WhiteSnakeEntity.State> {
    public static final Attack light = new Attack(0, JCraft.lightCooldown, 0.75f, 14, 7, 1.5, 5f, 0.75f, AttackType.BOX, 0.6f, 0.2f, 0, JSoundRegistry.IMPACT_3)
            .setInfo("Punch", "quick combo starter");
    public static final Attack donut = new Attack(1, 18, 1f, 36, 17, 2, 10f, 0.0f, AttackType.BOX, 1.4f, 0, 0, JSoundRegistry.TW_DONUT_HIT)
            .setHitspark(2)
            .setInfo("Donut", "slow combo starter/extender");
    public static final Attack barrage = Attack.barrageAttack(2, 17, 0.75f, 60, 0, 2, 1f, 0.25f, 1, 0, 3, JSoundRegistry.IMPACT_3)
            .setInfo("Barrage", "fast reliable combo starter/extender, medium stun");
    public static final Attack standdisk = new Attack(3, 30, 1f, 34, 22, 2, 8f, 0.0f, AttackType.BOX, 1f, 0, 0, JSoundRegistry.IMPACT_2)
            .setHitspark(2)
            .hyperArmor()
            .setUB(true)
            .setInfo("Stand Disk", "uninterruptable, removes enemy stand for 8s");
    public static final Attack legcrusher = new Attack(4, 20, 0.75f, 22, 16, 1.75, 7f, 0.25f, AttackType.BOX, 1.6f, 0.2f, 0, JSoundRegistry.TW_KICK_HIT)
            .setHitspark(2)
            .setInfo("Leg Crusher", "high stun, medium windup");
    public static final Attack memorydisk = new Attack(6, 30, 1f, 34, 22, 2, 7f, 0.0f, AttackType.BOX, 1f, 0, 0, JSoundRegistry.IMPACT_2)
            .setHitspark(2)
            .hyperArmor()
            .setUB(true)
            .setInfo("Memory Disk", "uninterruptable, mining fatigue & weakness for 30s");
    public static final Attack chargedspew = new Attack(7, 30, 0.75f, 26, 20, 2, 0f, 0, AttackType.BOX)
            .setUB(true)
            .setInfo("Charged Spew", "fires 5, slower acid balls");
    public static final Attack poisonspew = new Attack(5, 20, 0.75f, 14, 10, 2, 0f, 0, AttackType.BOX)
            .setUB(true)
            .crouchingVariation(chargedspew)
            .setInfo("Poison Spew", "fires an acid projectile that slows enemies and persists on the surface it hits for 5s");
    public static final Attack meltyourheart = new Attack(8, 40, 1f, 50, 40, 2, 3f, 1.0f, AttackType.BOX, 1f, 0, 0, JSoundRegistry.IMPACT_2)
            .hyperArmor()
            .setUB(true)
            .setLaunch()
            .setInfo("Melt your Heart", "remote-only and armored, expels a sphere of poison");

    public WhiteSnakeEntity(World worldIn) {
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
                """
                        BNBs:
                            the el mayo
                            Memory Disk>M1>Barrage>Leg Crusher>Stand Disk>M1
                            the gazebo
                            M1>Barrage>Leg Crusher>Donut>M1""";

        moves = Lists.newArrayList(light, donut, barrage, memorydisk, standdisk, legcrusher, poisonspew,
                new Attack().setInfo("Pilot Mode", ""));
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!canAttack()) return;
        handleAttack(light, JCraft.standLightCD, State.LIGHT);
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleAttack(barrage, JCraft.standBarrageCD, State.BARRAGE))
            playSound(JSoundRegistry.WS_BARRAGE, 1, 1);
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;
        if (handleAttack(donut, JCraft.standHeavyCD, State.DONUT))
            playSound(JSoundRegistry.WS_DONUT, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (!canAttack()) return;
        if (handleAttack(memorydisk, JCraft.standS1CD, State.DISC))
            playSound(JSoundRegistry.WS_MEMORY_DISC, 1, 1);
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        if (getRemote() && handleAttack(meltyourheart, JCraft.standUltCD, State.MELT_YOUR_HEART))
            playSound(JSoundRegistry.WS_MYH, 1, 1);
        else if (handleAttack(standdisk, JCraft.standUltCD, State.DISC))
            playSound(JSoundRegistry.WS_STAND_DISC, 1, 1);
    }

    @Override
    public void initSpecial2() {
        if (!canAttack()) return;
        if (handleAttack(legcrusher, JCraft.standS2CD, State.LEG_CRUSHER))
            playSound(JSoundRegistry.WS_LEGCRUSH, 1, 1);
    }

    @Override
    public void initSpecial3() {
        if (!canAttack() || !hasUser()) return;

        if (getUserOrThrow().isSneaking())
            handleAttack(chargedspew, JCraft.standS3CD, State.ACID_SPEW_CHARGED);
        else handleAttack(poisonspew, JCraft.standS3CD, State.ACID_SPEW);
    }

    @Override
    public void initUtil() {
        if (!canAttack() || !hasUser()) return;
        NbtCompound userData = ((IEntityDataSaver) getUserOrThrow()).getPersistentData();
        if (userData.getInt(JCraft.utilCD) > 0) return;
        boolean newRemote = !getRemote();
        setRemote(newRemote);

        // Update movelist
        if (newRemote)
            moves.set(4, meltyourheart);
        else
            moves.set(4, standdisk);
        userData.putInt(JCraft.utilCD, 20);
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        if (!hasUser()) return;

        LivingEntity user = getUserOrThrow();
        switch (attack.id) {
            case (3) -> { // Stand Disc
                for (LivingEntity ent : entities)
                    ent.addStatusEffect(new StatusEffectInstance(JStatusRegistry.STANDLESS, 160, 0, true, false));
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
        if (age == 1) playSound(JSoundRegistry.WS_SUMMON, 1f, 1f);
        super.tick();

        if (!getRemote()) {
            if (hasUser()) setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(getUser()) / 2, 0.0, 255.0) / 255f);
            return;
        }

        if (world.isClient) return;

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

        if (getState() == State.IDLE) { // Replace idle anim
            if (s > 0) setStateNoReset(onGround ? State.RIGHT : State.RIGHT_DASH);
            if (s < 0) setStateNoReset(onGround ? State.LEFT : State.LEFT_DASH);
            if (f < 0) setStateNoReset(onGround ? State.BACKWARD : State.BACKWARD_DASH);
            if (f > 0) setStateNoReset(onGround ? State.FORWARD : State.FORWARD_DASH);
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
                .add(rotVec.rotateY(1.5707963f).multiply(s * moveSpeed)); // Side movement

        remoteSpeed = remoteSpeed.multiply(dragMult, 1, dragMult);

        if (pos.add(remoteSpeed).squaredDistanceTo(getUserOrThrow().getPos()) > 400)
            remoteSpeed.multiply(-1);

        addVelocity(remoteSpeed.x, remoteSpeed.y, remoteSpeed.z);
        velocityDirty = true;
        velocityModified = true;
    }

    // Animation code
    public enum State implements StandAnimationState<WhiteSnakeEntity> {
        IDLE(builder -> builder.loop("animation.whitesnake.idle")),
        LIGHT(builder -> builder.playAndHold("animation.whitesnake.light")),
        BLOCK(builder -> builder.loop("animation.whitesnake.block")),
        DONUT(builder -> builder.playAndHold("animation.whitesnake.donut")),
        BARRAGE(builder -> builder.loop("animation.whitesnake.barrage")),
        LEG_CRUSHER(builder -> builder.playAndHold("animation.whitesnake.legcrusher")),
        ACID_SPEW(builder -> builder.playAndHold("animation.whitesnake.acidspew")),
        ACID_SPEW_CHARGED(builder -> builder.playAndHold("animation.whitesnake.acidspew_charged")),
        DISC(builder -> builder.playAndHold("animation.whitesnake.disc")),

        FORWARD(builder -> builder.loop("animation.whitesnake.forw")),
        BACKWARD(builder -> builder.loop("animation.whitesnake.back")),
        LEFT(builder -> builder.loop("animation.whitesnake.left")),
        RIGHT(builder -> builder.loop("animation.whitesnake.right")),
        FORWARD_DASH(builder -> builder.loop("animation.whitesnake.fdash")),
        BACKWARD_DASH(builder -> builder.loop("animation.whitesnake.bdash")),
        LEFT_DASH(builder -> builder.loop("animation.whitesnake.ldash")),
        RIGHT_DASH(builder -> builder.loop("animation.whitesnake.rdash")),

        MELT_YOUR_HEART(builder -> builder.playAndHold("animation.whitesnake.meltyourheart"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(WhiteSnakeEntity stand, AnimationBuilder builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @Nullable String getSummonAnimation() {
        return "animation.whitesnake.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
