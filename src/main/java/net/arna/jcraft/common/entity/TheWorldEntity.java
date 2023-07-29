package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.attack.AttackType;
import net.arna.jcraft.common.attack.HitBoxData;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.function.Consumer;

public class TheWorldEntity extends StandEntity<TheWorldEntity, TheWorldEntity.State> {
    public static final Attack crm1 = new Attack(0, JCraft.lightCooldown, 0.75f, 14, 8, 1.5, 6f, 1f, AttackType.BOX, 0.85f, 0.25f, 0, JSoundRegister.IMPACT_1)
            .appendHitbox(new HitBoxData(0, 0, 1))
            .setInfo("Low Kick", "slower, higher stun");
    public static final Attack light = new Attack(0, JCraft.lightCooldown, 0.75f, 7, 5, 1.5, 5f, 0.75f, AttackType.BOX, 0.5f, -0.1f, 0, JSoundRegister.IMPACT_1)
            .crouchingVariation(crm1)
            .setInfo("Punch", "quick combo starter");
    public static final Attack barrage = new Attack(2, 17, 0.75f, 50, 0, 2, 1f, 0.1f, AttackType.BARRAGE, 2, 0, 3)
            .setInfo("Barrage", "fast reliable combo starter/extender, high stun");
    public static final Attack donut = new Attack(1, 14, 1f, 48, 26, 2, 9f, 0.0f, AttackType.BOX, 3.25f, 0, 0, JSoundRegister.TW_DONUT_HIT)
            .setHitspark(2)
            .appendHitbox(new HitBoxData(0, 0, 1.5))
            .hyperArmor()
            .setInfo("Donut", "slow, uninterruptable combo starter/extender, 1.5s stun on whiff");
    public static final Attack charge = new Attack(4, 20, 7.5f, 19, 5, 1.5, 5f, 0.25f, AttackType.CHARGE, 1, 0, State.CHARGE_HIT.ordinal(), JSoundRegister.TW_CHARGE_HIT)
            .setRanged(true)
            .disableBackstab()
            .setBlockstun(11)
            .setInfo("Forward Charge", "The World detaches from the user and lunges forward, combo starter");
    public static final Attack roundhouse = new Attack(3, 8, 0.75f, 13, 7, 1.75, 5f, 0.3f, AttackType.BOX, 0.45f, -0.1f, 0, JSoundRegister.TW_KICK_HIT)
            .setBlockstun(12)
            .setInfo("Roundhouse", "fast poke, low stun");
    public static final Attack timestop = new Attack(6, 70, 52, 45, 4, AttackType.TIMESTOP)
            .setUB(true)
            .setInfo("Timestop", "4 seconds");
    public static final Attack feignbarrage = new Attack(5, 30, 0.75f, 50, 5, 0, 0f, 0f, AttackType.COUNTER)
            .setInfo("Feign Barrage", "counter, 0.25s windup, 2.25s duration, teleports and knocks down on hit");
    public static final Attack counterfollowup = new Attack(7, 0, 0.75f, 9, 5, 1.75, 6f, 0.7f, AttackType.BOX, 0.8f, 0.1f, 0, JSoundRegister.IMPACT_4)
            .appendHitbox(new HitBoxData(1.25))
            .hyperArmor()
            .setLaunch()
            .setInfo("Counter (Hit)", "quick, armored knockdown");
    private static final Attack timeskip = new Attack(-2, 18, 2, 2)
            .setMobility(MobilityType.TELEPORT)
            .setInfo("Timeskip", "14m range");
    private static final Attack counterMiss = new Attack(8, 0, 10, 11);

    public TheWorldEntity(World worldIn) {
        super(StandType.THE_WORLD, worldIn);
        super.initialize();
        idleRotation = 225f;

        pros = List.of(
                "fast m1",
                "counter",
                "versatile ranged moves",
                "timestop & timeskip"
        );

        cons = List.of(
                "no knockdowns or knockbacks",
                "heavy is useless outside of combos"
        );

        description = "Mid Range DOMINATOR";

        freespace =
                """
                        BNBs:
                            the saucy racist
                            ppl without timeskips will suffer, but so will people with timeskips :)))
                            (M1>)Charge>M1>Roundhouse>Barrage>M1>Donut>Timestop{  }
                            the no ts racist
                            Donut>Roundhouse>Charge>M1>Barrage>M1""";

        moves = List.of(light, donut, barrage, roundhouse, timestop, charge, feignbarrage, timeskip);
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
        if (getUserOrThrow().isSneaking())
            handleAttack(crm1, JCraft.standLightCD, State.LOW);
        else handleAttack(light, JCraft.standLightCD, State.LIGHT);
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;
        if (handleAttack(donut, JCraft.standHeavyCD, State.DONUT))
            playSound(JSoundRegister.TW_DONUT, 1, 1);
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleAttack(barrage, JCraft.standBarrageCD, State.BARRAGE))
            playSound(JSoundRegister.TW_BARRAGE, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (!canAttack()) return;
        if (handleAttack(roundhouse, JCraft.standS1CD, State.ROUNDHOUSE))
            playSound(JSoundRegister.TW_KICK, 1, 1);
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        if (handleAttack(timestop, JCraft.standUltCD, State.TIME_STOP))
            playSound(JSoundRegister.TW_TS, 1, 1);
    }

    @Override
    public void initSpecial2() {
        if (!canAttack()) return;
        if (handleAttack(charge, JCraft.standS2CD, State.CHARGE))
            playSound(JSoundRegister.TW_CHARGE, 1, 1);
    }

    @Override
    public void initSpecial3() {
        if (!canAttack()) return;
        if (handleAttack(feignbarrage, JCraft.standS3CD, State.BARRAGE))
            playSound(JSoundRegister.TW_BARRAGE, 1, 1);
    }

    @Override
    public void initUtil() {
        if (!canAttack()) return;
        handleAttack(timeskip, JCraft.utilCD, State.TIMESKIP);
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        switch (attack.id) {
            case (-2) -> {
                if (tsTime > 0) return;
                timeSkip(14, JSoundRegister.TIME_SKIP);
            }
            case (1) -> {
                LivingEntity user = this.getUser();
                // If missed, stun the user for 1.5 seconds
                if (entities.isEmpty()) stun(user, 30, 0);
                    // If hit, impale and set position to middle of arm
                else for (LivingEntity entity : entities) {
                    Vec3d pos = this.getPos().add(this.getRotationVector().multiply(1.5));
                    entity.teleport(pos.x, entity.getY(), pos.z);
                }
            }
            case (7) -> {
                for (LivingEntity entity : entities)
                    entity.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 35, 0, true, false));
            }
        }
    }

    @Override
    public void counter(Entity entity, DamageSource source) {
        super.counter(entity, source);

        if (entity == null || !hasUser()) return;
        LivingEntity user = getUserOrThrow();
        Vec3d behind = entity.getPos().subtract(entity.getRotationVector());

        user.setVelocity(0, 0, 0);
        user.velocityModified = true;

        user.teleport(behind.x, behind.y, behind.z);

        user.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, entity.getEyePos());

        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.removeStatusEffect(JStatusRegister.DAZED);
            stun(livingEntity, 20, 0);
            if (entity.getFirstPassenger() instanceof StandEntity<?, ?> stand) stand.cancelAttack();
        }

        setAttack(counterfollowup, State.COUNTER_HIT);

        playSound(JSoundRegister.TIME_SKIP, 1, 1);
        playSound(JSoundRegister.TW_COUNTER, 1, 1);
    }

    @Override
    public void whiffCounter() {
        setAttack(counterMiss, State.COUNTER_MISS);
        stun(getUser(), counterMiss.moveStun, 0);
    }

    @Override
    public void tick() {
        if (age == 1) {
            playSound(JSoundRegister.TW_SUMMON, 1f, 1f);
            playSound(JSoundRegister.MUDA_DA, 1f, 1f);
        }

        super.tick();

        if (hasUser())
            this.setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(getUser()) / 2, 0.0, 255.0) / 255f);
    }

    // Animation code
    public enum State implements StandAnimationState<TheWorldEntity> {
        IDLE(builder -> builder.loop("animation.theworld.idle")),
        LIGHT(builder -> builder.playAndHold("animation.theworld.light")),
        BLOCK(builder -> builder.loop("animation.theworld.block")),
        DONUT(builder -> builder.playAndHold("animation.theworld.donut")),
        BARRAGE(builder -> builder.loop("animation.theworld.barrage")),
        TIME_STOP(builder -> builder.playAndHold("animation.theworld.timestop")),
        CHARGE(builder -> builder.loop("animation.theworld.charge")),
        CHARGE_HIT(builder -> builder.playAndHold("animation.theworld.charge_hit")),
        ROUNDHOUSE(builder -> builder.playAndHold("animation.theworld.roundhouse")),
        COUNTER_HIT(builder -> builder.playAndHold("animation.theworld.counter_hit")),
        COUNTER_MISS(builder -> builder.playAndHold("animation.theworld.counter_miss")),
        LOW(builder -> builder.playAndHold("animation.theworld.low")),
        TIMESKIP(builder -> builder.loop("animation.theworld.idle"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(TheWorldEntity stand, AnimationBuilder builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @Nullable String getSummonAnimation() {
        return "animation.theworld.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
