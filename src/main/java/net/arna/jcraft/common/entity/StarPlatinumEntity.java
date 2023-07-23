package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.attack.AttackType;
import net.arna.jcraft.common.attack.HitBoxData;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.registry.JSoundRegister;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

import java.util.ArrayList;
import java.util.List;

public class StarPlatinumEntity extends StandEntity {
    public static final Attack light = new Attack(0, 2, 0.75f, 7, 5, 1.5, 5f, 0.75f, AttackType.BOX, 0.5f, -0.1f, 0, JSoundRegister.IMPACT_1)
            .setInfo("Punch", "quick combo starter");
    public static final Attack heavy = new Attack(1, 17, 1f, 30, 20, 2.0, 10f, 1.5f, AttackType.BOX, 0.7f)
            .setHitspark(2)
            .appendHitbox(new HitBoxData(0, 0, 1.5))
            .hyperArmor()
            .setLaunch()
            .setInfo("Star Breaker", "uninterruptable launcher");
    public static final Attack barrage = new Attack(2, 17, 0.75f, 60, 0, 2, 1f, 0.25f, AttackType.BARRAGE, 2, 0, 3)
            .setInfo("Barrage", "fast reliable combo starter/extender, high stun");
    public static final Attack starfinger = new Attack(3, 20, 0.75f, 20, 12, 1.75, 5f, -0.25f, AttackType.BOX, 1.5f, -0.25f)
            .setHitspark(2)
            .appendHitbox(new HitBoxData(2, 0.5, 1))
            .setInfo("Star Finger", "medium windup, combo starter/extender");
    public static final Attack lowkick = new Attack(4, 12, 0.75f, 12, 7, 1.5, 6f, 0.25f, AttackType.BOX, 0.4f, 0)
            .setInfo("Roundhouse", "fast poke, low stun");
    public static final Attack chargebarrage = new Attack(5, 26, 5f, 55, 5, 1.5, 0.6f, 0.4f, AttackType.CHARGEBARRAGE, 1, 0, 3)
            .setRanged(true)
            .disableBackstab()
            .setInfo("Advancing Barrage", "fast combo starter/extender, medium stun, extremely punishable on whiff");
    public static final Attack inhale = new Attack(6, 50, 5, 5, 4, AttackType.BOX)
            .setUB(true)
            .setInfo("Inhale", "vacuums nearby entities for 4 seconds");
    private static final Attack jump = new Attack(-2, 18, 14, 5)
            .setMobility(MobilityType.DASH)
            .setInfo("Stand Jump", "jumps in looked direction with slight upward bias, you must stay on the ground until Star Platinum jumps");

    // Inhale
    private static final TrackedData<Integer> INHALETIME;

    static {
        INHALETIME = DataTracker.registerData(StarPlatinumEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    public StarPlatinumEntity(World worldIn) {
        this(StandType.STAR_PLATINUM, worldIn);
    }

    protected StarPlatinumEntity(StandType type, World worldIn) {
        super(type, worldIn);
        super.initialize();
        idleRotation = 225f;

        description = "High Speed RUSHDOWN";

        pros = List.of(
                "fast m1",
                "long, damaging combos",
                "low cooldowns",
                "timestop & timeskip"
        );

        cons = List.of(
                "predictable playstyle",
                "weak ranged coverage"
        );

        freespace =
                """
                        BNBs:
                        ~ represents a queued attack
                                                
                            -the classic
                            M1>Barrage>M1>Low Kick>Advancing Barrage~M1~Star Finger~Star Breaker
                            
                            -the blowback
                            Inhale>...>Star Finger>Star Breaker>Barrage>...

                            -the poke
                            Star Finger>Low Kick>M1>Advancing Barrage~M1>Barrage>M1>Star Breaker""";

        moves = List.of(light, heavy, barrage, starfinger, inhale, lowkick, starfinger, jump);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(INHALETIME, 0);
    }

    private void setInhaleTime(int time) {
        dataTracker.set(INHALETIME, time);
    }

    public int getInhaleTime() {
        return dataTracker.get(INHALETIME);
    }

    @Override
    public void desummon() {
        if (tsTime > 0) return;
        super.desummon();
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!canAttack()) return;
        handleAttack(light, JCraft.standLightCD, 2);
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;
        if (handleAttack(heavy, JCraft.standHeavyCD, 4))
            playSound(JSoundRegister.STAR_BREAKER, 1, 1);
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleAttack(barrage, JCraft.standBarrageCD, 5))
            playSound(JSoundRegister.STAR_PLATINUM_BARRAGE, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (!canAttack()) return;
        if (handleAttack(starfinger, JCraft.standS1CD, 6))
            playSound(JSoundRegister.STAR_FINGER, 1, 1);
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        if (handleAttack(inhale, JCraft.standUltCD, 7)) {
            //playSound(JSoundRegister.STAR_SUCK, 1, 1);
        }
    }

    @Override
    public void initSpecial2() {
        if (!canAttack()) return;
        if (handleAttack(lowkick, JCraft.standS2CD, 8))
            playSound(JSoundRegister.STAR_PLATINUM_KICK, 1, 1);
    }

    @Override
    public void initSpecial3() {
        if (!canAttack()) return;
        // Uses a copy because otherwise the main one gets overwritten by specialAttack()
        if (handleAttack(Attack.copyOf(chargebarrage), JCraft.standS3CD, 5))
            playSound(JSoundRegister.STAR_PLATINUM_ADVANCING_BARRAGE, 1, 1);
    }

    @Override
    public void initUtil() {
        if (!canAttack() || !hasUser() || !getUserOrThrow().isOnGround()) return;
        handleAttack(jump, JCraft.utilCD, 9);
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        switch (attack.id) {
            case (-2) -> {
                if (!hasUser()) return;
                LivingEntity user = getUserOrThrow();
                if (!user.isOnGround()) return;

                Vec3d jumpVel = getRotationVector().multiply(1.5).add(0, 0.5, 0);

                user.addVelocity(jumpVel.x, jumpVel.y, jumpVel.z);
                user.velocityModified = true;

                if (user instanceof ServerPlayerEntity player)
                    player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
            }
            case (5) -> {
                if (curAttack == null || entities.isEmpty()) return;
                Vec3d avgPos = Vec3d.ZERO;
                float c = 0;
                for (LivingEntity ent : entities) {
                    if (ent instanceof StandEntity) continue;
                    avgPos = avgPos.add(ent.getPos());
                    c += 1f;
                }
                avgPos = avgPos.multiply(1f / c);
                lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, avgPos);
                curAttack.attackDist = (float) avgPos.distanceTo(getPos());
            }
            case (6) -> setInhaleTime((int) (inhale.stun * 20));
        }
    }

    @Override
    public void tick() {
        if (age == 1) playSound(JSoundRegister.STAR_PLATINUM_SUMMON, 1f, 1f);
        super.tick();
        LivingEntity user = getUser();

        if (user != null) {
            Vec3d rotVec = getRotationVector();
            Vec3d fPos = getEyePos().add(rotVec.multiply(1.5));

            if (world.isClient) {
                setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(getUser()) / 2, 0.0, 255.0) / 255f);

                if (getInhaleTime() > 0) {
                    Vec3d addVel = getRotationVector().add(random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1);
                    Vec3d particlePos = fPos.add(addVel);

                    world.addParticle(ParticleTypes.POOF,
                            particlePos.x,
                            particlePos.y,
                            particlePos.z,
                            -addVel.x / 10.0, -addVel.y / 10.0, -addVel.z / 10.0);
                }
            } else {
                if (getInhaleTime() > 0) {
                    setInhaleTime(getInhaleTime() - 1);

                    if (getInhaleTime() > 0)
                        setRotationOffset(90);
                    else
                        setRotationOffset(225);

                    if (age % 2 == 0) {
                        List<Entity> filter = new ArrayList<>(List.of(this, user));
                        if (user.hasVehicle()) filter.add(user.getVehicle());

                        List<? extends Entity> toInhale = JUtils.generateHitbox(world, fPos, 2, Entity.class, filter);
                        for (Entity entity : toInhale) {
                            entity.setVelocity(
                                    entity.getVelocity().subtract(rotVec.x, 0, rotVec.z).multiply(0.2 * entity.distanceTo(this))
                            );

                            entity.velocityModified = true;

                            if (entity instanceof ServerPlayerEntity player)
                                player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
                        }
                    }
                }
            }
        }
    }

    // Animation code
    @Override
    protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
        AnimationController<E> controller = event.getController();
        AnimationBuilder builder = new AnimationBuilder();

        if (playSummonAnim) {
            controller.setAnimation(builder.playOnce("animation.starplatinum.summon"));
            return PlayState.CONTINUE;
        }

        if (getSameState()) controller.markNeedsReload();
        switch (getState()) {
            case 2 -> controller.setAnimation(builder.playAndHold("animation.starplatinum.punch"));
            case 3 -> controller.setAnimation(builder.loop("animation.starplatinum.block"));
            case 4 -> controller.setAnimation(builder.playAndHold("animation.starplatinum.heavy"));
            case 5 -> controller.setAnimation(builder.loop("animation.starplatinum.barrage"));
            case 6 -> controller.setAnimation(builder.playAndHold("animation.starplatinum.star_finger"));
            case 7 -> controller.setAnimation(builder.playAndHold("animation.starplatinum.inhale"));
            case 8 -> controller.setAnimation(builder.playAndHold("animation.starplatinum.low_kick"));
            case 9 -> controller.setAnimation(builder.playAndHold("animation.starplatinum.jump"));
            default -> controller.setAnimation(builder.loop(getInhaleTime() > 0 ? "animation.starplatinum.inhaleidle" : "animation.starplatinum.idle"));
        }

        return PlayState.CONTINUE;
    }
}
