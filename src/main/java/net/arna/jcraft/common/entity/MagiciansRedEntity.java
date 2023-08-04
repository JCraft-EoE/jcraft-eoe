package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.attack.AttackType;
import net.arna.jcraft.common.entity.projectile.AnkhProjectile;
import net.arna.jcraft.common.entity.projectile.LifeDetectorEntity;
import net.arna.jcraft.common.entity.projectile.RedBindEntity;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
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
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.function.Consumer;

public class MagiciansRedEntity extends StandEntity<MagiciansRedEntity, MagiciansRedEntity.State> {
    public static final Attack redirect = new Attack(5, 5, 0.75f, 10, 7, 0, 0f, 0f, AttackType.BOX)
            .setMobility(MobilityType.TELEPORT) // this is a LIE, it just tells the ai to use it at a range of >3m
            .setInfo("Redirect", "redirects all the users ankhs to where they're looking");
    public static final Attack light = new Attack(0, JCraft.lightCooldown, 0.75f, 8, 5, 1.5, 5f, 0.75f, AttackType.BOX, 0.8f, -0.1f, 0, JSoundRegistry.IMPACT_1)
            .crouchingVariation(redirect)
            .setInfo("Punch", "quick combo starter");
    public static final Attack heavy = new Attack(1, 14, 1f, 22, 12, 1.75, 7f, 0.5f, AttackType.BOX, 0.5f, 0.6f, 0, JSoundRegistry.TW_KICK_HIT)
            .setLaunch()
            .setInfo("Low Kick", "medium windup knockdown");
    public static final Attack barrage = Attack.barrageAttack(2, 17, 0.75f, 60, 0, 2, 0.4f, 0.25f, 0, 0, 3)
            .armorPoints((byte) 1)
            .setInfo("Flamethrower", "fast reliable damage cash-out tool, no stun, burns for 3 seconds");
    public static final Attack crossfire = new Attack(3, 12, 0.75f, 10, 8, 0, 0f, 0f, AttackType.BOX)
            .setRanged(true)
            .setInfo("Crossfire", "fires 3 stunning ankhs");
    public static final Attack crossfirevariation = new Attack(4, 30, 0.75f, 17, 12, 0, 0f, 0f, AttackType.BOX)
            .setRanged(true)
            .setInfo("Crossfire Variation", "summons 6 ankhs that orbit around the user, crouch to increase orbit distance");
    public static final Attack crossfirehurricane = new Attack(6, 60, 0.75f, 22, 18, 0, 0f, 0f, AttackType.BOX)
            .setInfo("Crossfire Hurricane", "summons slow, homing fire hurricane that knocks down, lasts for 3 seconds after hitting anything");
    public static final Attack redbind = new Attack(8, 20, 0.75f, 22, 12, 1.5, 3, 0, AttackType.BOX, 0.75f, 0, 0, JSoundRegistry.IMPACT_3)
            .setInfo("Red Bind", "on hit, wraps opponent in fiery rings that launch them in the direction they are hit");
    public static final Attack detector = new Attack(7, 25, 0.75f, 20, 13, 0, 0f, 0f, AttackType.BOX)
            .setRanged(true)
            .crouchingVariation(redbind)
            .setInfo("Life Detector", "tracks down nearby life, lasts 15s");

    private static final int variationAnkhs = 6;
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

        moves = List.of(light, heavy, barrage, crossfire, crossfirehurricane, crossfirevariation, redbind, detector);
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!canAttack()) return;
        if (getUserOrThrow().isSneaking()) {
            setAttack(redirect, State.REDIRECT);
            playSound(JSoundRegistry.MR_REDIRECT, 1, 1);
        } else
            handleAttack(light, JCraft.standLightCD, State.LIGHT);
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;
        if (handleAttack(heavy, JCraft.standHeavyCD, State.HEAVY))
            playSound(JSoundRegistry.MR_HEAVY, 1, 1);
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleAttack(barrage, JCraft.standBarrageCD, State.BARRAGE))
            playSound(JSoundRegistry.MR_BARRAGE, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (!canAttack()) return;
        if (handleAttack(crossfire, JCraft.standS1CD, State.CROSSFIRE))
            playSound(JSoundRegistry.MR_CROSSFIRE, 1, 1);
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        if (handleAttack(crossfirehurricane, JCraft.standUltCD, State.CROSSFIRE_HURRICANE))
            playSound(JSoundRegistry.MR_ULT, 1, 1);
    }

    @Override
    public void initSpecial2() {
        if (!canAttack()) return;
        if (handleAttack(crossfirevariation, JCraft.standS2CD, State.CROSSFIRE_VARIATION))
            playSound(JSoundRegistry.MR_CROSSFIRE, 1, 1);
    }

    @Override
    public void initSpecial3() {
        if (!canAttack()) return;
        if (handleAttack(redbind, JCraft.standS3CD, State.RED_BIND))
            playSound(JSoundRegistry.MR_REDBIND, 1, 1);
    }

    @Override
    public void initUtil() {
        if (!canAttack() || !hasUser()) return;
        if (handleAttack(detector, JCraft.utilCD, State.DETECTOR))
            playSound(JSoundRegistry.MR_DETECTOR, 1, 1);
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        if (!hasUser()) return;
        LivingEntity user = getUserOrThrow();
        Vec3d eyePos = user.getEyePos();

        switch (attack.id) {
            case (1) -> {
                for (LivingEntity ent : entities) {
                    if (!JUtils.isBlocking(ent))
                        ent.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 40, 0));
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
                        getBoundingBox().expand(32), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                if (!ankhs.isEmpty()) {
                    Vec3d pos = JUtils.raycastAll(user, eyePos, eyePos.add(user.getRotationVector().multiply(24)), RaycastContext.FluidHandling.NONE);

                    for (AnkhProjectile ankh : ankhs) {
                        if (ankh.getOwner() == user) {
                            ankh.setVariation(false);
                            ankh.setVelocity(pos.subtract(ankh.getPos()).normalize().multiply(0.6));
                        }
                    }
                }
            }
            case (6) -> {
                hurricaneTime = 50; // In quad ticks
                hurricanePos = this.getPos();
            }
            case (7) -> {
                LifeDetectorEntity lifeDetector = new LifeDetectorEntity(JEntityTypeRegistry.LIFE_DETECTOR, world);
                lifeDetector.setMaster(user);
                lifeDetector.refreshPositionAndAngles(getX(), getY() + 1.5, getZ(), getYaw(), getPitch());
                world.spawnEntity(lifeDetector);
            }
            case (8) -> {
                if (!hasUser() || entities.isEmpty()) return;

                LivingEntity master = getUserOrThrow();
                LivingEntity boundEntity = JUtils.getUserIfStand(entities.get(0));

                if (JUtils.isBlocking(boundEntity)) return;

                // Remove Stand
                StandEntity<?, ?> stand = ((IEntityDataSaver) boundEntity).getStand();
                if (stand != null) {
                    stand.curAttack = null;
                    stand.setMoveStun(0);
                    stand.desummon();
                }

                // Stun
                boundEntity.removeStatusEffect(JStatusRegistry.DAZED);
                StandEntity.stun(boundEntity, RedBindEntity.ticksToLive, 0);

                // Create and bind
                RedBindEntity redBind = new RedBindEntity(JEntityTypeRegistry.RED_BIND, world);
                redBind.setPosition(boundEntity.getPos());
                redBind.setMaster(master);
                redBind.setBoundEntity(boundEntity);
                world.spawnEntity(redBind);
            }
        }
    }

    @Override
    public void tick() {
        if (age == 1) playSound(JSoundRegistry.MR_SUMMON, 1f, 1f);
        super.tick();

        if (hasUser()) {
            LivingEntity user = getUserOrThrow();
            if (world.isClient) {
                if (getState() == State.BARRAGE) {
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
                                new Box(hurricanePos.add(32.0, 32.0, 32.0), hurricanePos.subtract(32.0, 32.0, 32.0)),
                                EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e -> e != vehicle && e != this && e != user));

                        if (!nearbyEnts.isEmpty()) {
                            Vec3d avgPos = Vec3d.ZERO;
                            for (LivingEntity livingEntity : nearbyEnts)
                                avgPos = avgPos.add(livingEntity.getEyePos());
                            avgPos = avgPos.multiply(1.0 / nearbyEnts.size());

                            hurricanePos = hurricanePos.add(avgPos.subtract(hurricanePos).normalize().multiply(0.5));
                        }

                        // Damage
                        List<LivingEntity> toHurt = world.getEntitiesByClass(LivingEntity.class,
                                new Box(hurricanePos.add(2.5, 1, 2.5), hurricanePos.subtract(2.5, 1, 2.5)),
                                EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e -> e != this && e != user && e != vehicle));
                        toHurt.remove(this);
                        toHurt.remove(user);

                        for (LivingEntity living : toHurt) {
                            LivingEntity target = JUtils.getUserIfStand(living);
                            if (hurricaneTime > 1) {
                                damageLogic(world, target, new Vec3d(Math.sin(age / 10.0) * 3, 0.0, Math.cos(age / 10.0) * 3), 10, 1, false, 0.5f, true, 5, DamageSource.mob(user), user);
                                if (hurricaneTime > 15)
                                    hurricaneTime = 15; // Allows for zoning up until it hits something
                            } else {
                                target.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 20, 0));
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

                this.setAlpha((float) MathHelper.clamp(255.0 * squaredDistanceTo(user) / 2, 0.0, 255.0) / 255f);
            }
        }
    }

    // Animation code
    public enum State implements StandAnimationState<MagiciansRedEntity> {
        IDLE(builder -> builder.loop("animation.mr.idle")),
        LIGHT(builder -> builder.playAndHold("animation.mr.light")),
        BLOCK(builder -> builder.loop("animation.mr.block")),
        HEAVY(builder -> builder.playAndHold("animation.mr.heavy")),
        BARRAGE(builder -> builder.playAndHold("animation.mr.barrage")),
        CROSSFIRE(builder -> builder.playAndHold("animation.mr.crossfire")),
        CROSSFIRE_HURRICANE(builder -> builder.playAndHold("animation.mr.crossfirehurricane")),
        CROSSFIRE_VARIATION(builder -> builder.playAndHold("animation.mr.crossfirevariation")),
        REDIRECT(builder -> builder.playAndHold("animation.mr.redirect")),
        RED_BIND(builder -> builder.playAndHold("animation.mr.redbind")),
        DETECTOR(builder -> builder.playAndHold("animation.mr.detector"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(MagiciansRedEntity stand, AnimationBuilder builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @Nullable String getSummonAnimation() {
        return "animation.mr.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
