package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.attack.AttackType;
import net.arna.jcraft.common.attack.HitBoxData;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegister;
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

import java.util.ArrayList;
import java.util.List;

//todo: move all inhale code to StarPlatinumEntity.java
public abstract sealed class AbstractStarPlatinumEntity<E extends AbstractStarPlatinumEntity<E, S>, S extends Enum<S> & StandAnimationState<E>> extends StandEntity<E, S>
        permits StarPlatinumEntity, SPTWEntity {
    public static final Attack crm1 = new Attack(7, JCraft.lightCooldown, 0.75f, 14, 8, 1.5, 6f, 0.25f, AttackType.BOX, 1f, -0.4f, 0, JSoundRegister.IMPACT_1)
            .setLaunch()
            .appendHitbox(new HitBoxData(0, 0.35, 1.25))
            .setInfo("Uppercut", "slower combo starter, launches");
    public static final Attack light = new Attack(0, JCraft.lightCooldown, 0.75f, 7, 5, 1.5, 5f, 0.75f, AttackType.BOX, 0.5f, -0.1f, 0, JSoundRegister.IMPACT_1)
            .crouchingVariation(crm1)
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
    protected static final Attack jump = new Attack(-2, 18, 14, 5)
            .setMobility(MobilityType.DASH)
            .setInfo("Stand Jump", "jumps in looked direction with slight upward bias, you must stay on the ground until Star Platinum jumps");

    // Inhale
    private static final TrackedData<Integer> INHALE_TIME;

    static {
        INHALE_TIME = DataTracker.registerData(AbstractStarPlatinumEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    protected AbstractStarPlatinumEntity(StandType type, Class<S> stateClass, World worldIn) {
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
        dataTracker.startTracking(INHALE_TIME, 0);
    }

    protected void setInhaleTime(int time) {
        dataTracker.set(INHALE_TIME, time);
    }

    protected int getInhaleTime() {
        return dataTracker.get(INHALE_TIME);
    }

    @Override
    public void desummon() {
        if (tsTime > 0) return;
        super.desummon();
    }

    // Moveset
    @Override
    public abstract void initLightAttack();

    @Override
    public abstract void initHeavyAttack();

    @Override
    public abstract void initBarrage();

    @Override
    public abstract void initSpecial1();

    @Override
    public abstract void initSpecial2();

    @Override
    public abstract void initSpecial3();

    @Override
    public abstract void initUlt();

    @Override
    public abstract void initUtil();

    @Override
    public abstract void specialAttack(Attack attack, List<LivingEntity> entities);

    @Override
    public void tick() {
        if (age == 1) playSound(JSoundRegister.STAR_PLATINUM_SUMMON, 1f, 1f);
        super.tick();
        LivingEntity user = getUser();

        if (user == null) return;
        Vec3d rotVec = getRotationVector();
        Vec3d fPos = getEyePos().add(rotVec.multiply(1.5));

        if (world.isClient) {
            setAlpha((float) MathHelper.clamp(255.0 * squaredDistanceTo(getUser()) / 2, 0.0, 255.0) / 255f);

            if (getInhaleTime() > 0) {
                Vec3d addVel = getRotationVector().add(random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1);
                Vec3d particlePos = fPos.add(addVel);

                world.addParticle(ParticleTypes.POOF,
                        particlePos.x,
                        particlePos.y,
                        particlePos.z,
                        -addVel.x / 10.0, -addVel.y / 10.0, -addVel.z / 10.0);
            }
        } else if (getInhaleTime() > 0) {
            setInhaleTime(getInhaleTime() - 1);

            if (getInhaleTime() > 0)
                setRotationOffset(90);
            else setRotationOffset(225);

            if (age % 2 != 0) return;
            List<Entity> filter = new ArrayList<>(List.of(this, user));
            if (user.hasVehicle()) filter.add(user.getVehicle());

            List<? extends Entity> toInhale = JUtils.generateHitbox(world, fPos, 2, Entity.class, filter);
            for (Entity entity : toInhale) {
                entity.setVelocity(entity.getVelocity()
                        .subtract(rotVec.x, 0, rotVec.z)
                        .multiply(0.2 * entity.distanceTo(this)));

                entity.velocityModified = true;

                if (entity instanceof ServerPlayerEntity player)
                    player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
            }
        }
    }

    // Animation code

}
