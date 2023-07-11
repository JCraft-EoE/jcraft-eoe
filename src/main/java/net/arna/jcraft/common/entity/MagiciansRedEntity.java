package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.JEntityTypeRegister;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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

import java.util.List;

public class MagiciansRedEntity extends StandEntity implements IAnimatable, IAnimationTickable {
    public static final Attack light = new Attack(0, 2, 0.75f, 8, 5, 1.5, 5f, 0.75f, AttackType.BOX, 0.75f, -0.1f, 0, JSoundRegister.IMPACT_1)
            .setInfo("Punch", "quick combo starter");
    public static final Attack heavy = new Attack(1, 17, 1f, 22, 12, 1.75, 7f, 0.5f, AttackType.BOX, 0.5f, 0.6f, 0, JSoundRegister.TW_KICK_HIT)
            .setLaunch()
            .setInfo("Low Kick", "knockdown provider, medium windup");
    public static final Attack barrage = new Attack(2, 17, 0.75f, 60, 0, 2, 0.4f, 0.25f, AttackType.BARRAGE, 1.5f, 0, 3)
            .setInfo("Flamethrower", "fast reliable combo starter/extender, high stun, burns");
    public static final Attack crossfire = new Attack(3, 20, 0.75f, 10, 8, 0, 0f, 0f, AttackType.BOX)
            .setRanged(true)
            .setInfo("Crossfire", "fires 3 stunning ankhs");
    public static final Attack crossfirevariation = new Attack(4, 30, 0.75f, 17, 12, 0, 0f, 0f, AttackType.BOX)
            .setRanged(true)
            .setInfo("Crossfire Variation", "summons 6 ankhs that orbit around the user, crouch to increase orbit distance");
    public static final Attack redirect = new Attack(5, 5, 0.75f, 10, 7, 0, 0f, 0f, AttackType.BOX).setMobility(MobilityType.TELEPORT)
            .setInfo("Redirect", "redirects all the users ankhs to where they're looking");
    // and so begins my terrible misuse of my own AI flags, the tldr here being that its simply called whenever the enemy is >3 blocks away, which is great here
    public static final Attack crossfirehurricane = new Attack(6, 60, 0.75f, 22, 18, 0, 0f, 0f, AttackType.BOX)
            .setInfo("Crossfire Hurricane", "summons slow, homing fire hurricane that knocks down, lasts for 3 seconds after hitting anything");
    public static final Attack redbind = new Attack(8, 20, 0.75f, 22, 12, 1.5, 5, 0, AttackType.BOX, 0.75f, 0, 0, JSoundRegister.IMPACT_3)
            .setInfo("Red Bind", "medium windup, good stun");
    public static final Attack detector = new Attack(7, 25, 0.75f, 20, 13, 0, 0f, 0f, AttackType.BOX)
            .setRanged(true)
            .crouchingVariation(redbind)
            .setInfo("Life Detector/Red Bind", "tracks down nearby life, lasts 15s/crouch for a whip attack");

    private Vec3d hurricanePos;
    private int hurricaneTime;

    public MagiciansRedEntity(World worldIn) {
        super(StandType.MAGICIANS_RED, worldIn);
        super.initialize();
        idleRotation = 225f;

        description = "Tailor-made, Blazing ZONER";

        pros = List.of(
                "incredible setups",
                "high damage",
                "two knockdowns"
        );

        cons = List.of(
                "easily blockable projectiles",
                "slower than average",
                "no mobility options",
                "no armored options"
        );

        freespace = """
                PASSIVE: Fire Resistance

                BNBs:
                    Hurricane>[opponent in air]Barrage>M1>Crossfire>Red Bind>M1>Low Kick>Variation
                    Red Bind>M1>Barrage>M1>Crossfire>Low Kick>Hurricane+Variation""";

        moves = List.of(light, heavy, barrage, crossfire, crossfirehurricane, crossfirevariation, redirect, detector);
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!this.canAttack()) return;
        handleAttack(light, JCraft.standLightCD, 2);
    }

    @Override
    public void initHeavyAttack() {
        if (!this.canAttack()) return;
        if (handleAttack(heavy, JCraft.standHeavyCD, 4))
            playSound(JSoundRegister.MR_HEAVY, 1, 1);
    }

    @Override
    public void initBarrage() {
        if (!this.canAttack()) return;
        if (handleAttack(barrage, JCraft.standBarrageCD, 5))
            playSound(JSoundRegister.MR_BARRAGE, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (!this.canAttack()) return;
        if (handleAttack(crossfire, JCraft.standS1CD, 6))
            playSound(JSoundRegister.MR_CROSSFIRE, 1, 1);
    }

    @Override
    public void initUlt() {
        if (!this.canAttack()) return;
        if (handleAttack(crossfirehurricane, JCraft.standUltCD, 7))
            playSound(JSoundRegister.MR_ULT, 1, 1);
    }

    @Override
    public void initSpecial2() {
        if (!this.canAttack()) return;
        if (handleAttack(crossfirevariation, JCraft.standS2CD, 8))
            playSound(JSoundRegister.MR_CROSSFIRE, 1, 1);
    }

    @Override
    public void initSpecial3() {
        if (!this.canAttack()) return;
        if (handleAttack(redirect, JCraft.standS3CD, 9))
            playSound(JSoundRegister.MR_REDIRECT, 1, 1);
    }

    @Override
    public void initUtil() {
        if (!this.canAttack()) return;
        if (getUser().isSneaking() && handleAttack(redbind, JCraft.utilCD, 10)) {
            playSound(JSoundRegister.MR_REDBIND, 1, 1);
        } else if (handleAttack(detector, JCraft.utilCD, 11)) {
            playSound(JSoundRegister.MR_DETECTOR, 1, 1);
        }
    }

    private static final int variationAnkhs = 6;

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        LivingEntity user = this.getUser();
        Vec3d eyePos = user.getEyePos();

        switch (attack.id) {
            case (1) -> {
                for (LivingEntity ent : entities) {
                    if (!JUtils.isBlocking(ent))
                        ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 40, 0));
                }
            }
            case (2) -> {
                for (LivingEntity ent : entities) ent.setOnFireFor(3);
            }
            case (3) -> {
                for (int i = 0; i < 3; i++) {
                    AnkhProjectile ankh = new AnkhProjectile(world, user);
                    ankh.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1F, 5F);
                    ankh.setPosition(eyePos);
                    world.spawnEntity(ankh);
                }
            }
            case (4) -> {
                int orbitRange = user.isSneaking() ? 6 : 4;
                for (int i = 0; i < variationAnkhs; i++) {
                    AnkhProjectile ankh = new AnkhProjectile(world, user);
                    ankh.setVelocity(0.0, 1.0, 0.0);
                    ankh.setPosition(eyePos.add(0.0, 1.0, 0.0));
                    ankh.setVariation(true);
                    ankh.setOrbitRange(orbitRange);
                    ankh.setOrbitOffset((360f / variationAnkhs) * i);
                    world.spawnEntity(ankh);
                }
            }
            case (5) -> {
                List<AnkhProjectile> ankhs = world.getEntitiesByClass(AnkhProjectile.class,
                        new Box(eyePos.add(32.0, 32.0, 32.0), eyePos.subtract(32.0, 32.0, 32.0)), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                if (ankhs.size() > 0) {
                    HitResult hitResult = this.world.raycast(new RaycastContext(eyePos, eyePos.add(user.getRotationVector().multiply(24)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));

                    for (AnkhProjectile ankh : ankhs) {
                        if (ankh.getOwner() == user) {
                            ankh.setVariation(false);
                            ankh.setVelocity(hitResult.getPos().subtract(ankh.getPos()).normalize());
                        }
                    }
                }
            }
            case (6) -> {
                hurricaneTime = 50; // In quad ticks
                hurricanePos = this.getPos();
            }
            case (7) -> {
                LifeDetectorEntity lifeDetector = new LifeDetectorEntity(JEntityTypeRegister.LIFE_DETECTOR, world);
                lifeDetector.setMaster(user);
                lifeDetector.refreshPositionAndAngles(getX(), getY() + 1.5, getZ(), getYaw(), getPitch());
                world.spawnEntity(lifeDetector);
            }
        }
    }

    @Override
    public void tick() {
        if (age == 1) playSound(JSoundRegister.MR_SUMMON, 1f, 1f);
        super.tick();

        if (hasUser()) {
            LivingEntity user = this.getUser();
            if (world.isClient) {
                if (this.getState() == 5) {
                    Vec3d rotVec = getRotationVector();
                    Vec3d mouthPos = getEyePos().add(rotVec);
                    for (int i = 0; i < 16; i++) {
                        Vec3d vel = user.getVelocity().add(
                                rotVec
                                        .rotateX(random.nextFloat() - 0.5f)
                                        .rotateY(random.nextFloat() - 0.5f)
                                        .rotateZ(random.nextFloat() - 0.5f)
                                        .multiply(0.2)
                        );
                        this.world.addParticle(
                                random.nextInt(6) == 5 ? ParticleTypes.LAVA : ParticleTypes.FLAME,
                                mouthPos.x, mouthPos.y, mouthPos.z,
                                vel.x, vel.y, vel.z
                        );
                    }
                }
            } else {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 20, 0, true, false));

                Entity vehicle = user.getVehicle();

                // Run every four ticks because the hurricane's meant to be slow, and it's convenient for CPU usage
                if (this.age % 4 == 0) {
                    if (hurricaneTime > 0) {
                        hurricaneTime -= 1;

                        // Homing
                        List<LivingEntity> nearbyEnts = world.getEntitiesByClass(LivingEntity.class,
                                new Box(hurricanePos.add(32.0, 32.0, 32.0), hurricanePos.subtract(32.0, 32.0, 32.0)), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
                        nearbyEnts.remove(this);
                        nearbyEnts.remove(user);
                        if (vehicle != null)
                            nearbyEnts.remove(vehicle);

                        if (!nearbyEnts.isEmpty()) {
                            Vec3d avgPos = Vec3d.ZERO;
                            for (LivingEntity livingEntity : nearbyEnts)
                                avgPos = avgPos.add(livingEntity.getEyePos());
                            avgPos = avgPos.multiply(1.0 / nearbyEnts.size());

                            hurricanePos = hurricanePos.add(avgPos.subtract(hurricanePos).normalize().multiply(0.5));
                        }

                        // Damage
                        List<LivingEntity> toHurt = world.getEntitiesByClass(LivingEntity.class,
                                new Box(hurricanePos.add(2.5, 1, 2.5), hurricanePos.subtract(2.5, 1, 2.5)), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
                        toHurt.remove(this);
                        toHurt.remove(user);
                        if (vehicle != null)
                            toHurt.remove(vehicle);

                        for (LivingEntity living : toHurt) {
                            LivingEntity target = JUtils.getUserIfStand(living);
                            if (hurricaneTime > 1) {
                                damageLogic(world, target, new Vec3d(Math.sin(age / 10.0) * 3, 0.0, Math.cos(age / 10.0) * 3), 10, 1, false, 0.5f, true, 5, DamageSource.mob(user), user);
                                if (hurricaneTime > 15)
                                    hurricaneTime = 15; // Allows for zoning up until it hits something
                            } else {
                                target.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 20, 0));
                            }
                        }

                        // Particles
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeShort(10);

                        buf.writeDouble(hurricanePos.x);
                        buf.writeDouble(hurricanePos.y);
                        buf.writeDouble(hurricanePos.z);

                        for (ServerPlayerEntity sendPlayer : ((ServerWorld) world).getPlayers())
                            ServerChannelFeedbackPacket.send(sendPlayer, buf);
                    }
                }

                this.setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(user) / 2, 0.0, 255.0) / 255f);
            }
        }
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

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationController<E> controller = event.getController();
        AnimationBuilder builder = new AnimationBuilder();
        if (playSummonAnim) {
            controller.setAnimation(builder.playOnce("animation.mr.summon"));
            return PlayState.CONTINUE;
        }
        if (getSameState()) controller.markNeedsReload();
        switch (this.getState()) {
            default -> controller.setAnimation(builder.loop("animation.mr.idle"));
            case 2 -> controller.setAnimation(builder.playAndHold("animation.mr.light"));
            case 3 -> controller.setAnimation(builder.loop("animation.mr.block"));
            case 4 -> controller.setAnimation(builder.playAndHold("animation.mr.heavy"));
            case 5 -> controller.setAnimation(builder.playAndHold("animation.mr.barrage"));
            case 6 -> controller.setAnimation(builder.playAndHold("animation.mr.crossfire"));
            case 7 -> controller.setAnimation(builder.playAndHold("animation.mr.crossfirehurricane"));
            case 8 -> controller.setAnimation(builder.playAndHold("animation.mr.crossfirevariation"));
            case 9 -> controller.setAnimation(builder.playAndHold("animation.mr.redirect"));
            case 10 -> controller.setAnimation(builder.playAndHold("animation.mr.redbind"));
            case 11 -> controller.setAnimation(builder.playAndHold("animation.mr.detector"));

            //default -> throw new IllegalStateException("Unexpected value: " + this.getState());
        }
        return PlayState.CONTINUE;
    }
}
